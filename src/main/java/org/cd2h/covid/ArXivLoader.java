package org.cd2h.covid;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import edu.uiowa.slis.GitHubTagLib.util.LocalProperties;
import edu.uiowa.slis.GitHubTagLib.util.PropertyLoader;

public class ArXivLoader {
    static Logger logger = Logger.getLogger(ArXivLoader.class);
    protected static LocalProperties prop_file = null;
    static Connection conn = null;
    static String searchlink = "https://arxiv.org/search/advanced?advanced=&terms-0-operator=AND&terms-0-term=COVID-19&terms-0-field=title&terms-1-operator=OR&terms-1-term=SARS-CoV-2&terms-1-field=abstract&terms-3-operator=OR&terms-3-term=COVID-19&terms-3-field=abstract&terms-4-operator=OR&terms-4-term=SARS-CoV-2&terms-4-field=title&terms-5-operator=OR&terms-5-term=coronavirus&terms-5-field=title&terms-6-operator=OR&terms-6-term=coronavirus&terms-6-field=abstract&classification-physics_archives=all&classification-include_cross_list=include&date-filter_by=all_dates&date-year=&date-from_date=&date-to_date=&date-date_type=submitted_date&abstracts=show&size=200&order=-announced_date_first&source=home-covid-19&start=";

    public static void main(String[] args) throws IOException, ClassNotFoundException, SQLException {
	PropertyConfigurator.configure("/Users/eichmann/Documents/Components/log4j.info");
	initialize();

	for (int page = 0; ; page += 200) {
	    int count = 0;
	    Document doc = Jsoup.connect(searchlink + page).timeout(0).get();
	    for (Element element : doc.getElementsByClass("arxiv-result")) {
		String link = element.getElementsByTag("a").first().attr("href");
		logger.info("hit: " + link);
		fetch(link);
		count++;
	    }
	    if  (count < 200)
		break;
	}

    }
    
    static void fetch(String url) throws IOException, SQLException {
	String id = url.substring(url.lastIndexOf('/') + 1);
	Document doc = Jsoup.connect(url).timeout(0).get();
	for (Element element : doc.getElementsByTag("meta")) {
	    String name = element.attr("name");
	    if (name == null || name.length() == 0)
		name = element.attr("property");
	    String value = element.attr("content");
	    logger.info("\tmeta: " + name + " : " + value);
	    PreparedStatement stmt = conn.prepareStatement("insert into covid_arxiv.metadata values(?,?,?)");
	    stmt.setString(1, id);
	    stmt.setString(2, name);
	    stmt.setString(3, value);
	    stmt.execute();
	    stmt.close();
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
