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
import org.apache.tika.exception.TikaException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.xml.sax.SAXException;

import edu.uiowa.slis.GitHubTagLib.util.LocalProperties;
import edu.uiowa.slis.GitHubTagLib.util.PropertyLoader;

public class CrossRefLoader {
    static Logger logger = Logger.getLogger(CrossRefLoader.class);
    protected static LocalProperties prop_file = null;
    static Connection conn = null;
    static int apiLimit = 100;
    static String filePrefix = "/Volumes/Pegasus0/COVID/";

    public static void main(String[] args) throws IOException, SQLException, ClassNotFoundException, InterruptedException, SAXException, TikaException {
	System.setProperty("java.awt.headless", "true");

	PropertyConfigurator.configure("/Users/eichmann/Documents/Components/log4j.info");
	initialize();

	scan_biorxiv();
	scan_litcovid();
    }

    static public void scan_biorxiv() throws SQLException, IOException, SAXException, TikaException {
	logger.info("");
	logger.info("scanning bioRxiv...");
	logger.info("");
	PreparedStatement fetchStmt = conn.prepareStatement(
		"select distinct doi from covid_biorxiv.biorxiv_current where doi not in (select doi from covid_crossref.raw_crossref) and doi not in (select doi from covid_crossref.raw_suppress)");
	ResultSet rs = fetchStmt.executeQuery();
	while (rs.next()) {
	    String doi = rs.getString(1);
	    fetchCrossRef(doi);
	}
    }
    
    static public void scan_litcovid() throws SQLException, IOException, SAXException, TikaException {
	logger.info("");
	logger.info("scanning LitCOVID...");
	logger.info("");
	PreparedStatement fetchStmt = conn.prepareStatement(
		"select distinct e_location_id from covid_litcovid.e_location_id where e_id_type ='doi' and e_location_id not in (select doi from covid_crossref.raw_crossref) and e_location_id not in (select doi from covid_crossref.raw_suppress)");
	ResultSet rs = fetchStmt.executeQuery();
	while (rs.next()) {
	    String doi = rs.getString(1);
	    fetchCrossRef(doi);
	}
    }
    
    static void fetchCrossRef(String doi) {
	    logger.info("DOI: " + doi);
	try {
	    URL theURL = new URL("https://api.crossref.org/v1/works/" + doi);
	    BufferedReader reader = new BufferedReader(new InputStreamReader(theURL.openConnection().getInputStream()));

	    JSONObject results = new JSONObject(new JSONTokener(reader));
	    // JSONArray resultArray = results.getJSONArray("rels");
	    logger.info("results: " + results.toString(3));
	    PreparedStatement citeStmt = conn.prepareStatement("insert into covid_crossref.raw_crossref values (?,?::jsonb)");
	    citeStmt.setString(1, doi);
	    citeStmt.setString(2, results.toString(3));
	    citeStmt.executeUpdate();
	    citeStmt.close();
	} catch (Exception e) {
	    logger.error("Exception raised: " + e);
	    PreparedStatement citeStmt;
	    try {
		citeStmt = conn.prepareStatement("insert into covid_crossref.raw_suppress values (?)");
		citeStmt.setString(1, doi);
		citeStmt.executeUpdate();
		citeStmt.close();
	    } catch (SQLException e1) {
		logger.error("Exception raised: " + e1);
	    }
	}
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
