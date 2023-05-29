package org.cd2h.covid.gkg;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.cd2h.covid.PropertyLoader;
import org.cd2h.covid.StringQueue;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class Crawler implements Runnable {
	static Logger logger = Logger.getLogger(Crawler.class);
	static Properties prop_file = PropertyLoader.loadProperties("gkg");
	static StringQueue queue = new StringQueue();

	public static void main(String[] args) throws IOException, ClassNotFoundException, SQLException, InterruptedException {
		PropertyConfigurator.configure("log4j.info");
		Connection conn = getConnection();
		
		PreparedStatement stmt = conn.prepareStatement("select source_url from gkg_local.source_url_covid where source_url not in (select source_url from gkg_local.document) limit 1000000");
		ResultSet rs = stmt.executeQuery();
		while (rs.next()) {
			String url = rs.getString(1);
			logger.info("url: " + url);
			queue.queue(url);
		}
		stmt.close();

		int maxCrawlerThreads = Runtime.getRuntime().availableProcessors() * 2;
//		maxCrawlerThreads = 1;
		Thread[] scannerThreads = new Thread[maxCrawlerThreads];

		for (int i = 0; i < maxCrawlerThreads; i++) {
		    logger.info("starting thread " + i);
		    Thread theThread = new Thread(new Crawler(i));
		    theThread.setPriority(Math.max(theThread.getPriority() - 2, Thread.MIN_PRIORITY));
		    theThread.start();
		    scannerThreads[i] = theThread;
		}

		for (int i = 0; i < maxCrawlerThreads; i++) {
		    scannerThreads[i].join();
		}
	}

	static Connection getConnection() throws ClassNotFoundException, SQLException {
		String use_ssl = prop_file.getProperty("nihdb.use.ssl", "false");
		logger.debug("Database SSL: " + use_ssl);

		String databaseHost = prop_file.getProperty("db.host", "localhost");
		logger.debug("Database Host: " + databaseHost);

		String databaseName = prop_file.getProperty("db.name", "loki");
		logger.debug("Database Name: " + databaseName);

		String db_url = prop_file.getProperty("nihdb.url", "jdbc:postgresql://" + databaseHost + "/" + databaseName);
		logger.debug("Database URL: " + db_url);

		Class.forName("org.postgresql.Driver");
		Properties props = new Properties();
		props.setProperty("user", "eichmann");
		props.setProperty("password", "translational");
		if (use_ssl.equals("true")) {
			props.setProperty("sslfactory", "org.postgresql.ssl.NonValidatingFactory");
			props.setProperty("ssl", "true");
		}
		Connection conn = DriverManager.getConnection(db_url, props);
		// conn.setAutoCommit(false);

		return conn;

	}
	
	Connection conn = null;
	int threadID = 0;

	public Crawler(int threadID) throws ClassNotFoundException, SQLException {
		this.threadID = threadID;
		conn = getConnection();
	}

	@Override
	public void run() {
		while (!queue.isCompleted()) {
			String url = queue.dequeue();
			if (url == null) {
				logger.info("[" + threadID + "] done.");
				return;
			}
			try {
				fetchDocument(url);
			} catch (Exception e) {
				logger.error("error raised proccessing url:", e);
			}
		}
		logger.info("[" + threadID + "] done.");
	}
	
	void fetchDocument(String url) throws SQLException {
		logger.info("[" + threadID + "] " + url);
		try {
			Document doc = Jsoup.connect(url).timeout(60000).get();
			PreparedStatement insStmt = conn.prepareStatement("insert into gkg_local.document(source_url,visited,html) values(?, now(), ?)");
			insStmt.setString(1, url);
			insStmt.setString(2, doc.toString());
			insStmt.execute();
			insStmt.close();
		} catch (Exception e) {
			PreparedStatement insStmt = conn.prepareStatement("insert into gkg_local.document(source_url,visited,html) values(?, now(), null)");
			insStmt.setString(1, url);
			insStmt.execute();
			insStmt.close();
		}
	}
}