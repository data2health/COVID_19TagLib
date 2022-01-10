package org.cd2h.covid;

import java.sql.*;
import java.util.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import edu.uiowa.lex.*;
import edu.uiowa.NLP_grammar.*;
import edu.uiowa.extraction.LocalProperties;
import edu.uiowa.extraction.PropertyLoader;

public class LitCOVIDParser implements Runnable {
	static Logger logger = LogManager.getLogger(LitCOVIDParser.class);

	protected static LocalProperties prop_file = null;
	static IntegerQueue pmidQueue = new IntegerQueue();
	static String mode = "parse";

	static String idString = null;
	int threadID = 0;
	Connection conn = null;
	SegmentParser theParser = null;
	boolean haveParseResults = false;

	public static void main(String[] args) throws Exception {
		prop_file = PropertyLoader.loadProperties("biorxiv");
		Connection conn = getConnection();

		PreparedStatement stmt = null;
		if (args.length > 0)
			mode = args[0];
		switch (mode) {
		case "parse":
			stmt = conn.prepareStatement("select pmid from covid_litcovid.article where pmid not in (select pmid from covid_litcovid.parse)");
			break;
		case "fragment":
			stmt = conn.prepareStatement("select distinct pmid from covid_litcovid.parse where pmid not in (select pmid from covid_litcovid.fragment)");
			break;
		}
		int skip = 0;
		ResultSet rs = stmt.executeQuery();
		while (rs.next()) {
			int pmid = rs.getInt(1);
			if (++skip < 10)
				continue;
			logger.debug("queueing : " + pmid);
			pmidQueue.queue(pmid);
		}
		logger.info("\t" + pmidQueue.size() + " articles queued.");

		int maxCrawlerThreads = Math.min(16, Runtime.getRuntime().availableProcessors());
//	 int maxCrawlerThreads = 1;
		Thread[] scannerThreads = new Thread[maxCrawlerThreads];

		for (int i = 0; i < maxCrawlerThreads; i++) {
			logger.info("starting thread " + i);
			Thread theThread = new Thread(new LitCOVIDParser(i, getConnection()));
			theThread.setPriority(Math.max(theThread.getPriority() - 2, Thread.MIN_PRIORITY));
			theThread.start();
			scannerThreads[i] = theThread;
		}
		for (int i = 0; i < maxCrawlerThreads; i++) {
			scannerThreads[i].join();
		}
	}

	public static Connection getConnection() throws SQLException, ClassNotFoundException {
		Class.forName("org.postgresql.Driver");
		Properties props = new Properties();
		props.setProperty("user", prop_file.getProperty("jdbc.user"));
		props.setProperty("password", prop_file.getProperty("jdbc.password"));
		Connection conn = DriverManager.getConnection(prop_file.getProperty("jdbc.url"), props);
		conn.setAutoCommit(false);
		return conn;
	}

	public LitCOVIDParser(int threadID, Connection conn) throws Exception {
		this.threadID = threadID;
		this.conn = conn;

		PreparedStatement pathStmt = conn.prepareStatement("set search_path to covid_litcovid");
		pathStmt.executeUpdate();
		pathStmt.close();

		theParser = new SegmentParser(new biomedicalLexer(), new SimpleStanfordParserBridge(),
				new basicSentenceGenerator());
	}

	public void run() {
		while (!pmidQueue.isCompleted()) {
			Integer pmid = pmidQueue.dequeue();
			if (pmid == null)
				return;
			try {
				switch (mode) {
				case "parse":
					try {
						processTitle(pmid);
						processAbstract(pmid);
						conn.commit();
					} catch (Exception e) {
						logger.error("Exception raised parsing : " + pmid, e);
					}
					break;
				case "fragment":
//					fragment(pmid);
					break;
				}
			} catch (Exception e) {
				logger.error("error raised proccessing doi:", e);
			}
		}
	}

	void storeMiss(int id) throws SQLException {
		logger.info("[" + threadID + "]\tempty parse result for: " + id);
		PreparedStatement missStmt = conn.prepareStatement("insert into covid_litcovid.miss values(?)");
		missStmt.setInt(1, id);
		missStmt.execute();
		missStmt.close();
	}

	void processTitle(int id) throws Exception {
		Statement stmt = conn.createStatement();
		ResultSet rs = stmt.executeQuery("select article_title from covid_litcovid.article_title where pmid = " + id);

		while (rs.next()) {
			String title = rs.getString(1);
			logger.info("[" + threadID + "] pmid: " + id + ":\t" + title);
			if (title.startsWith("[") && title.endsWith("]")) {
				title = title.substring(1, title.length() - 1);
				logger.debug("[" + threadID + "]\tnew title: " + title);
			} else if (title.startsWith("[") && title.endsWith("].")) {
				title = title.substring(1, title.length() - 2);
				logger.debug("[" + threadID + "]\tnew title: " + title);
			}
			TextSegment segment = theParser.parse(title);
			int sentenceCount = 0;
			for (TextSegmentElement element : segment.getElementVector()) {
				logger.info("[" + threadID + "]\tsentence: " + element.getSentence());
				haveParseResults = true;

				PreparedStatement sentStmt = conn
						.prepareStatement("insert into covid_litcovid.sentence values(?,?,?,?,?)");
				sentStmt.setInt(1, id);
				sentStmt.setInt(2, 0);
				sentStmt.setInt(3, ++sentenceCount);
				sentStmt.setString(4, element.getSentence().getText());
				sentStmt.setString(5, element.getSentence().toString());
				sentStmt.execute();
				sentStmt.close();

				int parseCount = 0;
				for (syntaxTree theTree : element.getParseVector()) {
					logger.info("[" + threadID + "]\t\tparse: " + theTree.treeString());

					PreparedStatement parseStmt = conn
							.prepareStatement("insert into covid_litcovid.parse values(?,?,?,?,?)");
					parseStmt.setInt(1, id);
					parseStmt.setInt(2, 0);
					parseStmt.setInt(3, sentenceCount);
					parseStmt.setInt(4, ++parseCount);
					parseStmt.setString(5, theTree.treeString());
					parseStmt.execute();
					parseStmt.close();
				}
			}
		}
		stmt.close();
	}

	void processAbstract(int id) throws Exception {
		Statement stmt = conn.createStatement();
		ResultSet rs = stmt.executeQuery("select seqnum,abstract from covid_litcovid.abstract where pmid = " + id);

		while (rs.next()) {
			int seqnum = rs.getInt(1);
			String abstr = rs.getString(2);
			logger.info("[" + threadID + "] pmid: " + id + " : " + seqnum + ":\t" + abstr);
			TextSegment segment = theParser.parse(abstr);
			int sentenceCount = 0;
			for (TextSegmentElement element : segment.getElementVector()) {
				logger.info("[" + threadID + "]\tsentence: " + element.getSentence());
				haveParseResults = true;

				PreparedStatement sentStmt = conn
						.prepareStatement("insert into covid_litcovid.sentence values(?,?,?,?,?)");
				sentStmt.setInt(1, id);
				sentStmt.setInt(2, seqnum);
				sentStmt.setInt(3, ++sentenceCount);
				sentStmt.setString(4, element.getSentence().getText());
				sentStmt.setString(5, element.getSentence().toString());
				sentStmt.execute();
				sentStmt.close();

				int parseCount = 0;
				for (syntaxTree theTree : element.getParseVector()) {
					logger.info("[" + threadID + "]\t\tparse: " + theTree.treeString());

					PreparedStatement parseStmt = conn
							.prepareStatement("insert into covid_litcovid.parse values(?,?,?,?,?)");
					parseStmt.setInt(1, id);
					parseStmt.setInt(2, seqnum);
					parseStmt.setInt(3, sentenceCount);
					parseStmt.setInt(4, ++parseCount);
					parseStmt.setString(5, theTree.treeString());
					parseStmt.execute();
					parseStmt.close();
				}
			}
		}
		stmt.close();
	}
}
