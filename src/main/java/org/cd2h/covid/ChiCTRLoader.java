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
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;
import org.jsoup.Jsoup;

import edu.uiowa.slis.GitHubTagLib.util.LocalProperties;
import edu.uiowa.slis.GitHubTagLib.util.PropertyLoader;

public class ChiCTRLoader {
	static Logger logger = LogManager.getLogger(ChiCTRLoader.class);
    protected static LocalProperties prop_file = null;
    static Connection conn = null;
    static int apiLimit = 100;
    
    // http://www.chictr.org.cn/exportxml.aspx?v=49543&users=13e469d6

    public static void main(String[] args) throws IOException, SQLException, ClassNotFoundException, DocumentException, InterruptedException {
	initialize();

//	simpleStmt("truncate covid_chictr.raw");
	
	scan();

	simpleStmt("refresh materialized view covid_chictr.study");
	simpleStmt("refresh materialized view covid_chictr.contact");
	simpleStmt("refresh materialized view covid_chictr.country");
	simpleStmt("refresh materialized view covid_chictr.criteria");
	simpleStmt("refresh materialized view covid_chictr.health_condition_code");
	simpleStmt("refresh materialized view covid_chictr.health_condition_keyword");
	simpleStmt("refresh materialized view covid_chictr.intervention_code");
	simpleStmt("refresh materialized view covid_chictr.intervention_keyword");
	simpleStmt("refresh materialized view covid_chictr.primary_outcome");
	simpleStmt("refresh materialized view covid_chictr.secondary_outcome");
	simpleStmt("refresh materialized view covid_chictr.secondary_sponsor");
	simpleStmt("refresh materialized view covid_chictr.secondary_id");
	simpleStmt("refresh materialized view covid_chictr.source_support");
	simpleStmt("refresh materialized view covid_chictr.ethics_review");

    }

    static public void scan() throws SQLException, IOException, DocumentException, InterruptedException {
	PreparedStatement stmt = conn.prepareStatement(
		"select trialid,web_address from who_ictrp.who where source_register='ChiCTR' and trialid not in (select id from covid_chictr.raw)");
	ResultSet rs = stmt.executeQuery();
	while (rs.next()) {
	    String trialID = rs.getString(1);
	    String web = rs.getString(2);
	    String id = web.substring(web.indexOf('=') + 1);
	    logger.info("trial_id: " + trialID + "\tid: " + id);
	    boolean success = false;
	    org.jsoup.nodes.Document doc = Jsoup.connect(web).timeout(0).get();
	    for (org.jsoup.nodes.Element element : doc.getElementsByClass("bt_subm")) {
		success = true;
		String description = element.text();
		String htmlURL = element.attr("href");
		logger.debug("description: " + description + "\turl: " + htmlURL);
		URL url = new URL(new URL(web), htmlURL);
		InputStream content = (InputStream) url.getContent();
		BufferedReader in = new BufferedReader(new InputStreamReader(content));
		SAXReader reader = new SAXReader(false);
		Document document = reader.read(in);
		logger.debug("trialid" + trialID + "\tcontent: " + document.asXML());
		PreparedStatement statement = conn.prepareStatement("insert into covid_chictr.raw values(?,?::xml)");
		statement.setString(1, trialID);
		statement.setString(2, document.asXML());
		statement.execute();
		statement.close();
	    }
	    if (!success)
		break;
	    Thread.sleep(1000);
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
