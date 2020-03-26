package org.cd2h.covid;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import edu.uiowa.slis.GitHubTagLib.util.LocalProperties;
import edu.uiowa.slis.GitHubTagLib.util.PropertyLoader;

public class ZoteroLoader {
    static Logger logger = Logger.getLogger(ZoteroLoader.class);
    protected static LocalProperties prop_file = null;
    static Connection conn = null;
    static int apiLimit = 100;

    public static void main(String[] args) throws IOException, SQLException, ClassNotFoundException {
	PropertyConfigurator.configure(args[0]);
	initialize();

	simpleStmt("truncate covid_zotero.raw_zotero");
	
	items();

	simpleStmt("refresh materialized view covid_zotero.book");
	simpleStmt("refresh materialized view covid_zotero.book_collection");
	simpleStmt("refresh materialized view covid_zotero.book_creator");
	simpleStmt("refresh materialized view covid_zotero.book_tag");

	simpleStmt("refresh materialized view covid_zotero.book_section");
	simpleStmt("refresh materialized view covid_zotero.book_section_collection");
	simpleStmt("refresh materialized view covid_zotero.book_section_creator");
	simpleStmt("refresh materialized view covid_zotero.book_section_tag");

	simpleStmt("refresh materialized view covid_zotero.journal_article");
	simpleStmt("refresh materialized view covid_zotero.journal_article_collection");
	simpleStmt("refresh materialized view covid_zotero.journal_article_creator");
	simpleStmt("refresh materialized view covid_zotero.journal_article_tag");

	simpleStmt("refresh materialized view covid_zotero.note");
	simpleStmt("refresh materialized view covid_zotero.note_tag");

	simpleStmt("refresh materialized view covid_zotero.report");
	simpleStmt("refresh materialized view covid_zotero.report_collection");
	simpleStmt("refresh materialized view covid_zotero.report_creator");
	simpleStmt("refresh materialized view covid_zotero.report_tag");

	simpleStmt("refresh materialized view covid_zotero.webpage");
	simpleStmt("refresh materialized view covid_zotero.webpage_collection");
	simpleStmt("refresh materialized view covid_zotero.webpage_creator");
	simpleStmt("refresh materialized view covid_zotero.webpage_tag");
    }

    static public void items() throws SQLException, IOException {
	int responseCount = 0;
	int offset = 0;

	do {
	    URL theURL = new URL("https://api.zotero.org/groups/2465841/items?limit=" + apiLimit + "&start=" + offset);
	    BufferedReader reader = new BufferedReader(new InputStreamReader(theURL.openConnection().getInputStream()));

	    JSONArray resultArray = new JSONArray(new JSONTokener(reader));
	    responseCount = resultArray.length();
	    logger.info("offset: " + offset + "\tresponseCount: " + responseCount);
	    offset += responseCount;
	    logger.trace("array: " + resultArray.toString(3));

	    for (int i = 0; i < resultArray.length(); i++) {
		JSONObject theObject = resultArray.getJSONObject(i);
		logger.debug("object: " + theObject.toString(3));

		PreparedStatement citeStmt = conn.prepareStatement("insert into covid_zotero.raw_zotero values (?::jsonb)");
		citeStmt.setString(1, theObject.toString());
		citeStmt.executeUpdate();
		citeStmt.close();
	    }
	} while (responseCount == apiLimit);
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
