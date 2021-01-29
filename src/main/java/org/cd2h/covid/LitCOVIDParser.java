package org.cd2h.covid;

import java.sql.*;
import java.util.*;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import edu.uiowa.lex.*;
import edu.uiowa.NLP_grammar.*;

import com.ibm.tspaces.*;

public class LitCOVIDParser implements Runnable {
	static Logger logger = Logger.getLogger(LitCOVIDParser.class);

	public static boolean localConnection = false;
	public static String networkHostName = "deep-thought.local";
//    public static final String networkHostName = "localhost";
	static final String localFilePrefix = "http://127.0.0.1/eichmann/tagging/";

	static TupleSpace ts = null;
	static String host = "deep-thought.local";
//    static final String host = "localhost";

	static String idString = null;
	int threadID = 0;
	Connection conn = null;
	SegmentParser theParser = null;
	boolean haveParseResults = false;

	public static void main(String[] args) throws Exception {
		PropertyConfigurator.configure(args[0]);

		if (args.length > 1) {
			networkHostName = args[1];
			host = args[1];
		}

		logger.info("initializing tspace...");
		try {
			ts = new TupleSpace("LitCOVID", host);
		} catch (TupleSpaceException tse) {
			logger.error("TSpace error: " + tse);
		}

		int maxCrawlerThreads = Math.min(8, Runtime.getRuntime().availableProcessors());
//	 int maxCrawlerThreads = 1;
		Thread[] scannerThreads = new Thread[maxCrawlerThreads];

		for (int i = 0; i < maxCrawlerThreads; i++) {
			logger.info("starting thread " + i);
			Thread theThread = new Thread(new LitCOVIDParser(i, getConnection(localConnection)));
			theThread.setPriority(Math.max(theThread.getPriority() - 2, Thread.MIN_PRIORITY));
			theThread.start();
			scannerThreads[i] = theThread;
		}
		for (int i = 0; i < maxCrawlerThreads; i++) {
			scannerThreads[i].join();
		}
	}

	static Connection getConnection(boolean local) throws ClassNotFoundException, SQLException {
		Connection conn = null;
		Class.forName("org.postgresql.Driver");
		Properties props = new Properties();
		props.setProperty("user", "eichmann");
		props.setProperty("password", "translational");
		if (local) {
			conn = DriverManager.getConnection("jdbc:postgresql://localhost/loki", props);
		} else {
//	    props.setProperty("sslfactory", "org.postgresql.ssl.NonValidatingFactory");
//	    props.setProperty("ssl", "true");
			conn = DriverManager.getConnection("jdbc:postgresql://" + networkHostName + "/loki", props);
		}
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
		Tuple theTuple = null;

		try {
			theTuple = ts.waitToTake("litcovid_parse_request", new Field(String.class));

			while (theTuple != null) {
				idString = (String) theTuple.getField(1).getValue();
				logger.info("[" + threadID + "] consuming " + idString);
				try {
					int id = Integer.parseInt(idString);
					haveParseResults = false;

					// we first flush any existing data, as this might be the result of an update to
					// MEDLINE
					PreparedStatement reqStmt = conn
							.prepareStatement("delete from covid_litcovid.sentence where pmid = ?");
					reqStmt.setInt(1, id);
					reqStmt.execute();
					reqStmt.close();

					reqStmt = conn.prepareStatement("delete from covid_litcovid.parse where pmid = ?");
					reqStmt.setInt(1, id);
					reqStmt.execute();
					reqStmt.close();

					reqStmt = conn.prepareStatement("delete from covid_litcovid.miss where pmid = ?");
					reqStmt.setInt(1, id);
					reqStmt.execute();
					reqStmt.close();

					processTitle(id);
					processAbstract(id);

					if (!haveParseResults)
						storeMiss(id);

					reqStmt = conn.prepareStatement("delete from medline_local.parse_request where pmid = ?");
					reqStmt.setInt(1, id);
					reqStmt.execute();
					reqStmt.close();

					conn.commit();
				} catch (Exception e) {
					logger.error("exception raised: " + e);
					try {
						conn.rollback();
					} catch (SQLException e1) {
						logger.error("exception raised: " + e);
					}
					theParser = new SegmentParser(new biomedicalLexer(), new SimpleStanfordParserBridge(),
							new basicSentenceGenerator());
				}
				theTuple = ts.waitToTake("medline_parse_request", new Field(String.class));
			}
		} catch (TupleSpaceException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
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
		ResultSet rs = stmt.executeQuery("select title from covid_litcovid.article where pmid = " + id);

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
		ResultSet rs = stmt.executeQuery("select seqnum,abstract_text from covid_litcovid.abstr where pmid = " + id);

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
