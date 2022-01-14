package org.cd2h.covid;

import java.io.FileInputStream;
import java.io.IOException;
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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.sax.BodyContentHandler;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.xml.sax.SAXException;

import edu.uiowa.slis.GitHubTagLib.util.LocalProperties;
import edu.uiowa.slis.GitHubTagLib.util.PropertyLoader;

public class ArXivLoader {
	static Logger logger = LogManager.getLogger(ArXivLoader.class);
    protected static LocalProperties prop_file = null;
    static Connection conn = null;
    static String searchlink = "https://arxiv.org/search/advanced?advanced=&terms-0-operator=AND&terms-0-term=COVID-19&terms-0-field=title&terms-1-operator=OR&terms-1-term=SARS-CoV-2&terms-1-field=abstract&terms-3-operator=OR&terms-3-term=COVID-19&terms-3-field=abstract&terms-4-operator=OR&terms-4-term=SARS-CoV-2&terms-4-field=title&terms-5-operator=OR&terms-5-term=coronavirus&terms-5-field=title&terms-6-operator=OR&terms-6-term=coronavirus&terms-6-field=abstract&classification-physics_archives=all&classification-include_cross_list=include&date-filter_by=all_dates&date-year=&date-from_date=&date-to_date=&date-date_type=submitted_date&abstracts=show&size=200&order=-announced_date_first&source=home-covid-19&start=";
    static String filePrefix = "/Volumes/Pegasus0/COVID_arXiv/";

    public static void main(String[] args) throws IOException, ClassNotFoundException, SQLException, SAXException, TikaException {
	initialize();
	search();
	fetchPDFs();
	scan_pdf();
    }
    
    static void search() throws IOException, SQLException {
	for (int page = 0; ; page += 200) {
	    logger.info("page: " + page);
	    int count = 0;
	    Document doc = Jsoup.connect(searchlink + page).timeout(0).get();
	    for (Element element : doc.getElementsByClass("arxiv-result")) {
		String link = element.getElementsByTag("a").first().attr("href");
		if (check(link)) {
		    continue;
		}
		logger.info("hit: " + link);
		fetchHTML(link);
		count++;
	    }
	    if  (count < 200)
		break;
	}

    }
    
    static void fetchHTML(String url) throws IOException, SQLException {
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
    
    static void fetchPDFs() throws SQLException, IOException {
	PreparedStatement stmt = conn.prepareStatement("select id,value from covid_arxiv.metadata where name='citation_pdf_url' and id not in (select id from covid_arxiv.map)");
	ResultSet rs = stmt.executeQuery();
	while (rs.next()) {
	    String id = rs.getString(1);
	    String url = rs.getString(2);
	    logger.info("id: " + id + "\turl: " + url);
	    URL fetchURL = new URL(url);
	    logger.info("\tfetching... " + fetchURL);
	    Files.copy(fetchURL.openStream(), Paths.get(filePrefix + fetchURL.getFile().substring(fetchURL.getFile().lastIndexOf('/') + 1)+".pdf"), StandardCopyOption.REPLACE_EXISTING);
	    PreparedStatement citeStmt = conn.prepareStatement("insert into covid_arxiv.map values (?,?)");
	    citeStmt.setString(1, id);
	    citeStmt.setString(2, url);
	    citeStmt.executeUpdate();
	    citeStmt.close();
	}
	stmt.close();
    }

    static public void scan_pdf() throws SQLException, IOException, SAXException, TikaException {
	logger.info("");
	logger.info("Extracting text from PDF...");
	logger.info("");
	PreparedStatement fetchStmt = conn.prepareStatement(
		"select id,url from covid_arxiv.map where id not in (select id from covid_arxiv.text)");
	ResultSet rs = fetchStmt.executeQuery();
	while (rs.next()) {
	    String id = rs.getString(1);
	    String fetchURL = rs.getString(2)+".pdf";
	    String file = filePrefix + fetchURL.substring(fetchURL.lastIndexOf('/') + 1);
	    logger.info("id: " + id + "\tfile: " + file);

	    try {
		String contents = parseToPlainText(file);
		PreparedStatement citeStmt = conn.prepareStatement("insert into covid_arxiv.text values (?,?)");
		citeStmt.setString(1, id);
		citeStmt.setString(2, contents);
		citeStmt.executeUpdate();
		citeStmt.close();
	    } catch (Exception e) {
		logger.error("Exception raised: " + e);
	    }

	}
    }
    static boolean check(String url) throws SQLException {
	int count = 0;
	
	PreparedStatement stmt = conn.prepareStatement("select count(*) from covid_arxiv.metadata where id = ?");
	stmt.setString(1, url.substring(url.lastIndexOf('/') + 1));
	ResultSet rs = stmt.executeQuery();
	while (rs.next()) {
	    count = rs.getInt(1);
	}
	stmt.close();

	return count > 0;
    }

    static public String parseToPlainText(String path) throws IOException, SAXException, TikaException {
	BodyContentHandler handler = new BodyContentHandler(-1);
	FileInputStream str = new FileInputStream(path);
	AutoDetectParser parser = new AutoDetectParser();
	Metadata metadata = new Metadata();
	parser.parse(str, handler, metadata);
	return handler.toString();
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
