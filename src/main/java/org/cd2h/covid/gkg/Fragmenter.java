package org.cd2h.covid.gkg;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.cd2h.covid.IntegerQueue;

import com.ibm.tspaces.Field;
import com.ibm.tspaces.Tuple;
import com.ibm.tspaces.TupleSpace;
import com.ibm.tspaces.TupleSpaceException;

import edu.uiowa.NLP_grammar.FragmentGenerator;
import edu.uiowa.NLP_grammar.ParseFragment;
import edu.uiowa.NLP_grammar.SimpleStanfordParserBridge;
import edu.uiowa.NLP_grammar.parser;
import edu.uiowa.PubMedCentral.AcknowledgementInstantiator;
import edu.uiowa.PubMedCentral.comparator.AuthorSet;
import edu.uiowa.UMLS.Concept;
import edu.uiowa.extraction.EnsembleDecorator;
import edu.uiowa.extraction.LocalProperties;
import edu.uiowa.extraction.PropertyLoader;
import edu.uiowa.extraction.TemplatePromoter;

public class Fragmenter implements Runnable {
	static Logger logger = Logger.getLogger(Fragmenter.class);
	static LocalProperties prop_file = PropertyLoader.loadProperties("gkg");
	static IntegerQueue queue = new IntegerQueue();
	static boolean useTSpace = false;
    static TupleSpace theTSpace = null;
    static String host = "192.168.2.72";

	public static void main(String[] args) throws Exception {
		PropertyConfigurator.configure("log4j.info");
		Connection conn = getConnection();
		
		if (args.length > 0 && args[0].equals("-hub")) {
			useTSpace = true;
			logger.info("initializing TSpace connection...");
			theTSpace = new TupleSpace("gkg_fragment", host);
			load(conn);
		} else if (args.length > 0 && args[0].equals("-client")) {
			useTSpace = true;
			logger.info("initializing TSpace connection...");
			theTSpace = new TupleSpace("gkg_fragment", host);
			launch(conn);
		} else {
			load(conn);
			launch(conn);
		}
	}
	
	static void load(Connection conn) throws SQLException, TupleSpaceException {
		PreparedStatement stmt = conn.prepareStatement("select foo.date,foo.id from"
		+ "	(select date,id,row_number() over(partition by date order by id) as rownum from"
		+ "		(select distinct id from gkg_local.parse where id not in (select id from gkg_local.fragment)) as fud"
		+ "	natural join"
		+ "		gkg_local.fragment_staging"
		+ "	 ) as foo"
		+ " where rownum <= 1000"
		+ " order by 1,2");
//		PreparedStatement stmt = conn.prepareStatement("select distinct id from gkg_local.parse where id not in (select id from gkg_local.fragment) limit 1000");
		ResultSet rs = stmt.executeQuery();
		while (rs.next()) {
			String date = rs.getString(1);
			int id = rs.getInt(2);
			logger.info("date: " + date + " : " + id);
			if (useTSpace) {
				theTSpace.write(new Tuple("fragment", id));
			} else
				queue.queue(id);
		}
		stmt.close();
	}
	
	static void launch(Connection conn) throws Exception {
		int maxCrawlerThreads = Runtime.getRuntime().availableProcessors();
//		int maxCrawlerThreads = 1;
		Thread[] scannerThreads = new Thread[maxCrawlerThreads];

		Concept.initialize(conn);
		
		for (int i = 0; i < maxCrawlerThreads; i++) {
		    logger.info("starting thread " + i);
		    Thread theThread = new Thread(new Fragmenter(i));
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
		PreparedStatement stmt = conn.prepareStatement("set search_path to extraction");
		stmt.execute();
		stmt.close();
		conn.setAutoCommit(false);
		conn.setNetworkTimeout(null, 0);

		return conn;

	}
	
	Connection conn = null;
	int threadID = 0;
	parser theParser = new SimpleStanfordParserBridge();
	int id = 0;
	int seqnum = 0;
	AuthorSet authorSet = new AuthorSet();
	FragmentGenerator theGenerator = null;
	
	public Fragmenter(int threadID) throws ClassNotFoundException, SQLException, Exception {
		conn = getConnection();
		theGenerator = new FragmentGenerator(new EnsembleDecorator(conn), new AcknowledgementInstantiator(prop_file, conn, threadID), new TemplatePromoter(conn));
		this.threadID = threadID;
	}

	@Override
	public void run() {
		if (useTSpace) {
			try {
				Tuple theTuple = theTSpace.waitToTake("fragment", new Field(Integer.class));
				while (theTuple != null) {
					int id = (Integer)theTuple.getField(1).getValue();
					try {
						fragmentDocument(id);
					} catch (Exception e) {
						logger.error("error raised proccessing url:", e);
					}
					theTuple = theTSpace.waitToTake("fragment", new Field(Integer.class));
				}
			} catch (TupleSpaceException e) {
				logger.error("tspace exception:" + e);
				return;
			}
		} else {
			while (!queue.isCompleted()) {
				Integer idString = queue.dequeue();
				if (idString == null) {
					logger.info("[" + threadID + "] done.");
					return;
				}
				id = idString;
				try {
					fragmentDocument(id);
				} catch (Exception e) {
					logger.error("error raised proccessing url:", e);
				}
			}
		}
		logger.info("[" + threadID + "] done.");
	}
	
	void fragmentDocument(int id) throws Exception {
		logger.info("[" + threadID + "] " + id);
//		PreparedStatement stmt = conn.prepareStatement("select parse.seqnum,seqnum2,parse from gkg_local.parse where id=? order by 1,2");
		PreparedStatement stmt = conn.prepareStatement("select parse.seqnum,seqnum2,parse,ratio from gkg_local.parse,gkg_local.sentence,gkg_local.sentence_frequency,gkg_local.host_map where parse.id=sentence.id and parse.seqnum=sentence.seqnum and sentence.sentence=sentence_frequency.sentence and sentence.id=host_map.id and host_map.host=sentence_frequency.host and sentence.id=? and ratio<0.8 order by 1,2");
		stmt.setInt(1, id);
		ResultSet rs = stmt.executeQuery();
		while (rs.next()) {
			int seqnum = rs.getInt(1);
			int seqnum2 = rs.getInt(2);
			String parse = rs.getString(3);
			logger.debug("parse: " + seqnum + " : " + seqnum2 + " : " + parse);
		    for (ParseFragment fragment : theGenerator.fragments(id, parse)) {
				logger.debug("\tfragment: " + fragment.getFragmentString());
				logger.debug("\t\tparse: " + fragment.getFragmentParse());

				PreparedStatement parseStmt = conn.prepareStatement("insert into gkg_local.fragment values(?,?,?,?,?)");
				parseStmt.setInt(1, id);
				parseStmt.setInt(2, seqnum);
				parseStmt.setInt(3, seqnum2);
				parseStmt.setString(4, fragment.getFragmentString());
				parseStmt.setString(5, fragment.getFragmentParse());
				parseStmt.execute();
				parseStmt.close();
		    }
		}
		stmt.close();
		conn.commit();
	}

}