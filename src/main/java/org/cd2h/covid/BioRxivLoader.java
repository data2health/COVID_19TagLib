package org.cd2h.covid;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import edu.uiowa.slis.GitHubTagLib.util.LocalProperties;
import edu.uiowa.slis.GitHubTagLib.util.PropertyLoader;

public class BioRxivLoader {
	static Logger logger = Logger.getLogger(BioRxivLoader.class);
	protected static LocalProperties prop_file = null;
	static Connection conn = null;
	static int apiLimit = 100;

	public static void main(String[] args) throws IOException, SQLException, ClassNotFoundException {
		PropertyConfigurator.configure(args[0]);
		initialize();

//		 simpleStmt("truncate covid.raw_biorxiv");

//		items();
		fetch();

		// simpleStmt("refresh materialized view covid.book");
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

	static public void items() throws SQLException, IOException {
		URL theURL = new URL("https://connect.biorxiv.org/relate/collection_json.php?grp=181");
		BufferedReader reader = new BufferedReader(new InputStreamReader(theURL.openConnection().getInputStream()));

		JSONObject results = new JSONObject(new JSONTokener(reader));
		JSONArray resultArray = results.getJSONArray("rels");
		logger.trace("array: " + resultArray.toString(3));

		for (int i = 0; i < resultArray.length(); i++) {
			if (resultArray.isNull(i))
				continue;
			JSONObject theObject = resultArray.getJSONArray(i).getJSONObject(0);
			logger.info("object: " + theObject.toString(3));

			PreparedStatement citeStmt = conn.prepareStatement("insert into covid.raw_biorxiv values (?::jsonb)");
			citeStmt.setString(1, theObject.toString());
			citeStmt.executeUpdate();
			citeStmt.close();
		}
	}

	static public void fetch() throws SQLException, IOException {
		PreparedStatement fetchStmt = conn.prepareStatement("select doi from covid.biorxiv_current where doi not in (select doi from covid.biorxiv_meta_raw) order by pub_date");
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
		//		for (int i = 0; i < resultArray.length(); i++) {
//			if (resultArray.isNull(i))
//				continue;
//			JSONObject theObject = resultArray.getJSONArray(i).getJSONObject(0);
//			logger.info("object: " + theObject.toString(3));
//
//			PreparedStatement citeStmt = conn.prepareStatement("insert into covid.raw_biorxiv values (?::jsonb)");
//			citeStmt.setString(1, theObject.toString());
//			citeStmt.executeUpdate();
//			citeStmt.close();
//		}
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
