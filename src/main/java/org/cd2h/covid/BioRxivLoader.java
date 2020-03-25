package org.cd2h.covid;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import edu.uiowa.slis.GitHubTagLib.util.LocalProperties;
import edu.uiowa.slis.GitHubTagLib.util.PropertyLoader;

public class BioRxivLoader {
    static Logger logger = Logger.getLogger(BioRxivLoader.class);
    protected static LocalProperties prop_file = null;
    static Connection conn = null;
    static int apiLimit = 100;
    static String filePrefix = "/Volumes/Pegasus0/COVID/";

    public static void main(String[] args) throws IOException, SQLException, ClassNotFoundException {
	PropertyConfigurator.configure(args[0]);
	initialize();

//	scan_feed();

//	fetch();
	
	scan_html();

	// simpleStmt("refresh materialized view covid.book_collection");
	// simpleStmt("refresh materialized view covid.book_creator");
	// simpleStmt("refresh materialized view covid.book_tag");
	//
	// simpleStmt("refresh materialized view covid.book_section");
	// simpleStmt("refresh materialized view
	// covid.book_section_collection");
	// simpleStmt("refresh materialized view covid.book_section_creator");
	// simpleStmt("refresh materialized view covid.book_section_tag");
	//
	// simpleStmt("refresh materialized view covid.journal_article");
	// simpleStmt("refresh materialized view
	// covid.journal_article_collection");
	// simpleStmt("refresh materialized view
	// covid.journal_article_creator");
	// simpleStmt("refresh materialized view covid.journal_article_tag");
	//
	// simpleStmt("refresh materialized view covid.note");
	// simpleStmt("refresh materialized view covid.note_tag");
	//
	// simpleStmt("refresh materialized view covid.report");
	// simpleStmt("refresh materialized view covid.report_collection");
	// simpleStmt("refresh materialized view covid.report_creator");
	// simpleStmt("refresh materialized view covid.report_tag");
	//
	// simpleStmt("refresh materialized view covid.webpage");
	// simpleStmt("refresh materialized view covid.webpage_collection");
	// simpleStmt("refresh materialized view covid.webpage_creator");
	// simpleStmt("refresh materialized view covid.webpage_tag");
    }

    static public void scan_feed() throws SQLException, IOException {
	simpleStmt("truncate covid.raw_biorxiv");

	URL theURL = new URL("https://connect.biorxiv.org/relate/collection_json.php?grp=181");
	BufferedReader reader = new BufferedReader(new InputStreamReader(theURL.openConnection().getInputStream()));

	JSONObject results = new JSONObject(new JSONTokener(reader));
	JSONArray resultArray = results.getJSONArray("rels");
	logger.info("array: " + resultArray.toString(3));

	for (int i = 0; i < resultArray.length(); i++) {
	    if (resultArray.isNull(i))
		continue;
	    JSONObject theObject = resultArray.getJSONObject(i);
	    logger.info("object: " + theObject.toString(3));

	    PreparedStatement citeStmt = conn.prepareStatement("insert into covid.raw_biorxiv values (?::jsonb)");
	    citeStmt.setString(1, theObject.toString());
	    citeStmt.executeUpdate();
	    citeStmt.close();
	}

	simpleStmt("refresh materialized view covid.biorxiv_current");
    }
    
    static public void scan_html() throws SQLException, IOException {
	PreparedStatement fetchStmt = conn.prepareStatement("select doi,link from covid.biorxiv_current");
	ResultSet rs = fetchStmt.executeQuery();
	while (rs.next()) {
	    String doi = rs.getString(1);
	    String link = rs.getString(2);
	    logger.info("doi: " + doi + "\tlink: " + link);
	    Document doc = Jsoup.connect(link).timeout(0).get();
	    Element element = doc.getElementsByClass("article-dl-pdf-link").first();
	    logger.info("element: " + element);
	    String href = element.attr("href");
	    logger.info("\thref: " + href);
	    
	    String old_href = null;
	    PreparedStatement urlStmt = conn.prepareStatement("select url from covid.biorxiv_map where doi = ?");
	    urlStmt.setString(1, doi);
	    ResultSet urlRS =  urlStmt.executeQuery();
	    while (urlRS.next()) {
		old_href = urlRS.getString(1);
	    }
	    urlStmt.close();
	    
	    if (old_href == null) {
		// new entry
		PreparedStatement citeStmt = conn.prepareStatement("insert into covid.biorxiv_map values (?,?)");
		citeStmt.setString(1, doi);
		citeStmt.setString(2, href);
		citeStmt.executeUpdate();
		citeStmt.close();
		URL fetchURL = new URL(new URL(link.replace("http", "https")), href);
		logger.info("\t\tfetching... " + fetchURL);
		Files.copy(fetchURL.openStream(), Paths.get(filePrefix+fetchURL.getFile().substring(fetchURL.getFile().lastIndexOf('/')+1)), StandardCopyOption.REPLACE_EXISTING);
	    } else if (!href.equals(old_href)) {
		// updated entry
		PreparedStatement citeStmt = conn.prepareStatement("update covid.biorxiv_map set url = ? where doi = ?");
		citeStmt.setString(1, href);
		citeStmt.setString(2, doi);
		citeStmt.executeUpdate();
		citeStmt.close();
		URL fetchURL = new URL(new URL(link.replace("http", "https")), href);
		logger.info("\t\tfetching... " + fetchURL);
		Files.copy(fetchURL.openStream(), Paths.get(filePrefix+fetchURL.getFile().substring(fetchURL.getFile().lastIndexOf('/')+1)), StandardCopyOption.REPLACE_EXISTING);
	    } else {
		logger.info("\t\tskipping...");
	    }
	}
	fetchStmt.close();
    }

    static public void fetch() throws SQLException, IOException {
	PreparedStatement fetchStmt = conn
		.prepareStatement("select doi from covid.biorxiv_current where doi not in (select doi from covid.biorxiv_meta_raw) order by pub_date");
	ResultSet rs = fetchStmt.executeQuery();
	while (rs.next()) {
	    String doi = rs.getString(1);
	    logger.info("DOI: " + doi);
	    try {
		URL theURL = new URL("https://api.rxivist.org/v1/papers/" + doi);
		BufferedReader reader = new BufferedReader(new InputStreamReader(theURL.openConnection().getInputStream()));

		JSONObject results = new JSONObject(new JSONTokener(reader));
		// JSONArray resultArray = results.getJSONArray("rels");
		logger.info("results: " + results.toString(3));
		PreparedStatement citeStmt = conn.prepareStatement("insert into covid.biorxiv_meta_raw values (?,?::jsonb)");
		citeStmt.setString(1, doi);
		citeStmt.setString(2, results.toString(3));
		citeStmt.executeUpdate();
		citeStmt.close();
	    } catch (java.io.FileNotFoundException e) {
		logger.error("\tDOI not found...");
	    }

	}
	// for (int i = 0; i < resultArray.length(); i++) {
	// if (resultArray.isNull(i))
	// continue;
	// JSONObject theObject = resultArray.getJSONArray(i).getJSONObject(0);
	// logger.info("object: " + theObject.toString(3));
	//
	// PreparedStatement citeStmt = conn.prepareStatement("insert into
	// covid.raw_biorxiv values (?::jsonb)");
	// citeStmt.setString(1, theObject.toString());
	// citeStmt.executeUpdate();
	// citeStmt.close();
	// }
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
