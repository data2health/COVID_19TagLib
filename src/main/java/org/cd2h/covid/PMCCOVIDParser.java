package org.cd2h.covid;

import java.sql.*;
import java.util.*;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import edu.uiowa.lex.*;
import edu.uiowa.NLP_grammar.*;
import edu.uiowa.extraction.LocalProperties;
import edu.uiowa.extraction.PropertyLoader;

public class PMCCOVIDParser implements Runnable {
	static Logger logger = Logger.getLogger(PMCCOVIDParser.class);

	protected static LocalProperties prop_file = null;
	static IntegerQueue pmcidQueue = new IntegerQueue();
	static String mode = "parse";

	static String idString = null;
	int threadID = 0;
	Connection conn = null;
	SegmentParser theParser = null;
	boolean haveParseResults = false;

	public static void main(String[] args) throws Exception {
		PropertyConfigurator.configure(args[0]);
		prop_file = PropertyLoader.loadProperties("biorxiv");
		Connection conn = getConnection();

		PreparedStatement stmt = null;
		if (args.length > 1)
			mode = args[1];
		switch (mode) {
		case "parse":
			stmt = conn.prepareStatement("select distinct pmcid from covid_pmc.paragraph where pmcid not in (select pmcid from covid_pmc.sentence)");
			break;
		case "fragment":
			stmt = conn.prepareStatement("select distinct pmcid from covid_pmc.parse where pmcid not in (select pmcid from covid_pmc.fragment)");
			break;
		}
		int skip = 0;
		ResultSet rs = stmt.executeQuery();
		while (rs.next()) {
			int pmcid = rs.getInt(1);
			if (++skip < 10)
				continue;
			logger.debug("queueing : " + pmcid);
			pmcidQueue.queue(pmcid);
		}
		logger.info("\t" + pmcidQueue.size() + " articles queued.");

		int maxCrawlerThreads = Math.min(8, Runtime.getRuntime().availableProcessors());
//		int maxCrawlerThreads = 1;
		Thread[] scannerThreads = new Thread[maxCrawlerThreads];

		for (int i = 0; i < maxCrawlerThreads; i++) {
			logger.info("starting thread " + i);
			Thread theThread = new Thread(new PMCCOVIDParser(i, getConnection()));
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

	public PMCCOVIDParser(int threadID, Connection conn) throws Exception {
		this.threadID = threadID;
		this.conn = conn;

		PreparedStatement pathStmt = conn.prepareStatement("set search_path to covid_pmc");
		pathStmt.executeUpdate();
		pathStmt.close();

		theParser = new SegmentParser(new biomedicalLexer(), new basicSentenceGenerator());
	}

	public void run() {
		while (!pmcidQueue.isCompleted()) {
			Integer pmcid = pmcidQueue.dequeue();
			if (pmcid == null)
				return;
			try {
				switch (mode) {
				case "parse":
					try {
						processParagraph(pmcid);
						conn.commit();
					} catch (Exception e) {
						logger.error("Exception raised parsing : " + pmcid, e);
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
		PreparedStatement missStmt = conn.prepareStatement("insert into covid_pmc.miss values(?)");
		missStmt.setInt(1, id);
		missStmt.execute();
		missStmt.close();
	}

	void processParagraph(int id) throws Exception {
		Statement stmt = conn.createStatement();
		ResultSet rs = stmt.executeQuery("select seqnum, seqnum2, seqnum3, seqnum4, seqnum5, seqnum6, p from covid_pmc.paragraph where pmcid = " + id);

		while (rs.next()) {
			int seqnum = rs.getInt(1);
			int seqnum2 = rs.getInt(2);
			int seqnum3 = rs.getInt(3);
			int seqnum4 = rs.getInt(4);
			int seqnum5 = rs.getInt(5);
			int seqnum6 = rs.getInt(6);
			String paragraph = rs.getString(7);
			logger.info("[" + threadID + "] pmcid: " + id + ":\t" + paragraph);
			TextSegment segment = theParser.parse(paragraph);
			int sentenceCount = 0;
			for (TextSegmentElement element : segment.getElementVector()) {
				logger.info("[" + threadID + "]\tsentence: " + element.getSentence());
				haveParseResults = true;

				PreparedStatement sentStmt = conn.prepareStatement("insert into covid_pmc.sentence values(?,?,?,?,?,?,?,?,?,?)");
				sentStmt.setInt(1, id);
				sentStmt.setInt(2, seqnum);
				sentStmt.setInt(3, seqnum2);
				sentStmt.setInt(4, seqnum3);
				sentStmt.setInt(5, seqnum4);
				sentStmt.setInt(6, seqnum5);
				sentStmt.setInt(7, seqnum6);
				sentStmt.setInt(8, ++sentenceCount);
				sentStmt.setString(9, element.getSentence().getCleanText());
				sentStmt.setString(10, element.getSentence().toString());
				sentStmt.execute();
				sentStmt.close();

				int parseCount = 0;
				for (syntaxTree theTree : element.getParseVector()) {
					logger.info("[" + threadID + "]\t\tparse: " + theTree.treeString());

					PreparedStatement parseStmt = conn.prepareStatement("insert into covid_pmc.parse values(?,?,?,?,?,?,?,?,?,?)");
					parseStmt.setInt(1, id);
					parseStmt.setInt(2, seqnum);
					parseStmt.setInt(3, seqnum2);
					parseStmt.setInt(4, seqnum3);
					parseStmt.setInt(5, seqnum4);
					parseStmt.setInt(6, seqnum5);
					parseStmt.setInt(7, seqnum6);
					parseStmt.setInt(8, sentenceCount);
					parseStmt.setInt(9, ++parseCount);
					parseStmt.setString(10, theTree.treeString());
					parseStmt.execute();
					parseStmt.close();
				}
			}
		}
		stmt.close();
	}

}
