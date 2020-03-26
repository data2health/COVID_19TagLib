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

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import edu.uiowa.slis.GitHubTagLib.util.LocalProperties;
import edu.uiowa.slis.GitHubTagLib.util.PropertyLoader;

public class LitCOVIDLoader {
    static Logger logger = Logger.getLogger(LitCOVIDLoader.class);
    protected static LocalProperties prop_file = null;
    static Connection conn = null;
    // https://eutils.ncbi.nlm.nih.gov/entrez/eutils/efetch.fcgi?db=pubmed&id=31978945&retmode=xml

    public static void main(String[] args) throws Exception {
	PropertyConfigurator.configure("log4j.info");
	initialize();
	
//	stageList();
	fetchRecords();
    }
    
    static void stageList() throws IOException, SQLException {
	simpleStmt("truncate litcovid.litcovid");
	
	URL theURL = new URL("https://www.ncbi.nlm.nih.gov/research/coronavirus-api/export");
	BufferedReader reader = new BufferedReader(new InputStreamReader(theURL.openConnection().getInputStream()));
	String buffer = null;
	while ((buffer = reader.readLine()) != null) {
	    if (buffer.startsWith("#") || buffer.startsWith("pmid"))
		continue;
	    logger.info(buffer);
	    String[] split = buffer.split("\t");

	    PreparedStatement citeStmt = conn.prepareStatement("insert into litcovid.litcovid values (?,?,?)");
	    citeStmt.setInt(1, Integer.parseInt(split[0]));
	    citeStmt.setString(2, split[1]);
	    citeStmt.setString(3, split[2]);
	    citeStmt.executeUpdate();
	    citeStmt.close();
	}
    }

    static void fetchRecords() throws Exception {
	PreparedStatement fetchStmt = conn.prepareStatement("select pmid from litcovid.litcovid where pmid not in (select pmid from litcovid.raw)");
	ResultSet rs = fetchStmt.executeQuery();
	while (rs.next()) {
	    int pmid = rs.getInt(1);
	    parseDocument(pmid);
	}
    }

    @SuppressWarnings("unchecked")
    static Element parseDocument(int pmid) throws Exception {
	logger.info("scanning " + pmid + "...");

	InputStream is = (new URL("https://eutils.ncbi.nlm.nih.gov/entrez/eutils/efetch.fcgi?db=pubmed&retmode=xml&id="+pmid)).openStream();
	SAXReader reader = new SAXReader(false);

	// <!ELEMENT MedlineCitationSet (MedlineCitation*, DeleteCitation?)>

	Document document = reader.read(is);
	Element root = document.getRootElement();
	logger.info("document root: " + root.asXML());
	for (Element citation : (List<Element>) root.selectNodes("PubmedArticle/MedlineCitation")) {
	    logger.info("citation:" + citation.asXML());

	    PreparedStatement citeStmt = conn.prepareStatement("insert into litcovid.raw values (?,?::xml)");
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
