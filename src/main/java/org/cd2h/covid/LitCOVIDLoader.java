package org.cd2h.covid;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import edu.uiowa.slis.GitHubTagLib.util.LocalProperties;
import edu.uiowa.slis.GitHubTagLib.util.PropertyLoader;

public class LitCOVIDLoader implements Runnable {
	static Logger logger = LogManager.getLogger(LitCOVIDLoader.class);
	protected static LocalProperties prop_file = null;
	static Connection conn = null;
	// https://eutils.ncbi.nlm.nih.gov/entrez/eutils/efetch.fcgi?db=pubmed&id=31978945&retmode=xml

	static IntegerQueue pmidQueue = new IntegerQueue();

	public static void main(String[] args) throws Exception {
		initialize();

		stageList();
		fetchRecords();

		int maxCrawlerThreads = Runtime.getRuntime().availableProcessors() / 4;
//		maxCrawlerThreads = 1;
		Thread[] scannerThreads = new Thread[maxCrawlerThreads];

		for (int i = 0; i < maxCrawlerThreads; i++) {
		    logger.info("starting thread " + i);
		    Thread theThread = new Thread(new LitCOVIDLoader(i));
		    theThread.setPriority(Math.max(theThread.getPriority() - 2, Thread.MIN_PRIORITY));
		    theThread.start();
		    scannerThreads[i] = theThread;
		}

		for (int i = 0; i < maxCrawlerThreads; i++) {
		    scannerThreads[i].join();
		}

		refreshViews();
	}

	static void stageList() throws IOException, SQLException {
		simpleStmt("truncate covid_litcovid.litcovid");

		URL theURL = new URL("https://www.ncbi.nlm.nih.gov/research/coronavirus-api/export");
		BufferedReader reader = new BufferedReader(new InputStreamReader(theURL.openConnection().getInputStream()));
		String buffer = null;
		while ((buffer = reader.readLine()) != null) {
			if (buffer.startsWith("#") || buffer.startsWith("pmid"))
				continue;
			logger.debug(buffer);
			String[] split = buffer.split("\t");

			PreparedStatement citeStmt = conn.prepareStatement("insert into covid_litcovid.litcovid values (?,?,?)");
			citeStmt.setInt(1, Integer.parseInt(split[0]));
			citeStmt.setString(2, split[1]);
			citeStmt.setString(3, split.length > 2 ? split[2] : null);
			citeStmt.executeUpdate();
			citeStmt.close();
		}
	}

	static void fetchRecords() throws Exception {
		PreparedStatement fetchStmt = conn.prepareStatement(
				"select pmid from covid_litcovid.litcovid where pmid not in (select pmid from covid_litcovid.raw)");
		ResultSet rs = fetchStmt.executeQuery();
		while (rs.next()) {
			int pmid = rs.getInt(1);
			pmidQueue.queue(pmid);
		}
	}

	static void refreshViews() {
		simpleStmt("refresh materialized view covid_litcovid.article");
		simpleStmt("refresh materialized view covid_litcovid.article_title");
		simpleStmt("refresh materialized view covid_litcovid.vernacular_title");
		simpleStmt("refresh materialized view covid_litcovid.e_location_id");
		simpleStmt("refresh materialized view covid_litcovid.abstract");
		simpleStmt("refresh materialized view covid_litcovid.author");
		simpleStmt("refresh materialized view covid_litcovid.author_identifier");
		simpleStmt("refresh materialized view covid_litcovid.author_affiliation");
		simpleStmt("refresh materialized view covid_litcovid.language");
		simpleStmt("refresh materialized view covid_litcovid.data_bank");
		simpleStmt("refresh materialized view covid_litcovid.accession_number");
		simpleStmt("refresh materialized view covid_litcovid.grant_info");
		simpleStmt("refresh materialized view covid_litcovid.publication_type");
		simpleStmt("refresh materialized view covid_litcovid.medline_journal_info");
		simpleStmt("refresh materialized view covid_litcovid.chemical");
		simpleStmt("refresh materialized view covid_litcovid.suppl_mesh_name");
		simpleStmt("refresh materialized view covid_litcovid.citation_subset");
		simpleStmt("refresh materialized view covid_litcovid.comments_corrections");
		simpleStmt("refresh materialized view covid_litcovid.gene_symbol");
		simpleStmt("refresh materialized view covid_litcovid.mesh_heading");
		simpleStmt("refresh materialized view covid_litcovid.mesh_qualifier");
		simpleStmt("refresh materialized view covid_litcovid.personal_name_subject");
		simpleStmt("refresh materialized view covid_litcovid.other_id");
		simpleStmt("refresh materialized view covid_litcovid.other_abstract");
		simpleStmt("refresh materialized view covid_litcovid.keyword");
		simpleStmt("refresh materialized view covid_litcovid.space_flight_mission");
		simpleStmt("refresh materialized view covid_litcovid.investigator");
		simpleStmt("refresh materialized view covid_litcovid.investigator_identifier");
		simpleStmt("refresh materialized view covid_litcovid.investigator_affiliation");
		simpleStmt("refresh materialized view covid_litcovid.general_note");
		simpleStmt("refresh materialized view covid_litcovid.history");
		simpleStmt("refresh materialized view covid_litcovid.article_id");
		simpleStmt("refresh materialized view covid_litcovid.object");
		simpleStmt("refresh materialized view covid_litcovid.reference");
		simpleStmt("refresh materialized view covid_litcovid.reference_article_id");

	}
	
	int threadID = 0;

	public LitCOVIDLoader(int threadID) throws Exception {
		this.threadID = threadID;
	}

	@Override
	public void run() {
		while (!pmidQueue.isCompleted()) {
			Integer pmid = pmidQueue.dequeue();
			if (pmid == null)
				return;
			try {
				parseDocument(pmid);
			} catch (Exception e) {
				logger.error("error raised proccessing doi:", e);
			}
		}
	}

	@SuppressWarnings("unchecked")
	Element parseDocument(int pmid) throws Exception {
		logger.info("[" + threadID + "] scanning " + pmid + "...");
		boolean failed = true;
		Document document = null;
		do {
			try {
				InputStream is = (new URL("https://eutils.ncbi.nlm.nih.gov/entrez/eutils/efetch.fcgi?db=pubmed&retmode=xml&id=" + pmid)).openStream();
				SAXReader reader = new SAXReader(false);
				document = reader.read(is);
				failed = false;
			} catch (Exception e) {
				logger.error("exception raised parsing " + pmid + ", retrying...");
				Thread.sleep(5000);
				failed = true;
			}
		} while (failed);

		Element root = document.getRootElement();
		logger.trace("[" + threadID + "] document root: " + root.asXML());
		for (Element citation : (List<Element>) root.selectNodes("PubmedArticle")) {
			logger.info("[" + threadID + "] citation:" + citation.asXML());

			PreparedStatement citeStmt = conn.prepareStatement("insert into covid_litcovid.raw values (?,?::xml)");
			citeStmt.setInt(1, pmid);
			citeStmt.setString(2, citation.asXML());
			citeStmt.executeUpdate();
			citeStmt.close();
		}
		for (Element citation : (List<Element>) root.selectNodes("PubmedBookArticle")) {
			logger.info("[" + threadID + "] citation:" + citation.asXML());

			PreparedStatement citeStmt = conn.prepareStatement("insert into covid_litcovid.raw values (?,?::xml)");
			citeStmt.setInt(1, pmid);
			citeStmt.setString(2, citation.asXML());
			citeStmt.executeUpdate();
			citeStmt.close();
		}
		return root;
	}

	static public void initialize() throws ClassNotFoundException, SQLException {
		prop_file = PropertyLoader.loadProperties("zotero");

		conn = getConnection();
	}

	public static Connection getConnection() throws SQLException, ClassNotFoundException {
		Class.forName("org.postgresql.Driver");
		Properties props = new Properties();
		props.setProperty("user", prop_file.getProperty("jdbc.user"));
		props.setProperty("password", prop_file.getProperty("jdbc.password"));
		Connection conn = DriverManager.getConnection(prop_file.getProperty("jdbc.url"), props);
		return conn;
	}

	public static void simpleStmt(String queryString) {
		try {
			logger.info("executing " + queryString + "...");
			PreparedStatement beginStmt = conn.prepareStatement(queryString);
			beginStmt.executeUpdate();
			beginStmt.close();
		} catch (Exception e) {
			logger.error("Error in database initialization: " + e);
			e.printStackTrace();
		}
	}
}
