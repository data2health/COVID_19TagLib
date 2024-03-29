package org.cd2h.covid;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
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
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.xml.sax.SAXException;

import edu.uiowa.slis.GitHubTagLib.util.LocalProperties;
import edu.uiowa.slis.GitHubTagLib.util.PropertyLoader;

public class BioRxivLoader {
	static Logger logger = LogManager.getLogger(BioRxivLoader.class);
	protected static LocalProperties prop_file = null;
	static Connection conn = null;
	static int apiLimit = 100;
	static String filePrefix = "/Volumes/Pegasus0/COVID/";

	public static void main(String[] args) throws IOException, SQLException, ClassNotFoundException, InterruptedException, SAXException, TikaException {
		System.setProperty("java.awt.headless", "true");

		initialize();

		new_scan_feed();

		if (args.length > 1 && args[1].equals("-metadata"))
			fetch();

		scan_html();
		scan_pdf();

	}

	static public void new_scan_feed() throws SQLException, IOException {
		int pageCount = 0;
		int totalFetched = 0;
		int count = 30;
		int cursor = 0;
		int total = Integer.MAX_VALUE;

//	while (cursor < total) {
		while (count == 30) {
			count = 0;

			logger.info("");
			logger.info("fetching page " + ++pageCount);
			logger.info("");

			URL theURL = new URL("https://api.biorxiv.org/covid19/" + cursor);
			BufferedReader reader = new BufferedReader(new InputStreamReader(theURL.openConnection().getInputStream()));

			JSONObject results = new JSONObject(new JSONTokener(reader));
			JSONArray resultArray = results.getJSONArray("collection");
			logger.trace("array: " + resultArray.toString(3));

			if (cursor == 0) {
				total = results.getJSONArray("messages").getJSONObject(0).getInt("total");
				logger.info("total: " + total);
			}

			for (int i = 0; i < resultArray.length(); i++) {
				if (resultArray.isNull(i))
					continue;
				JSONObject theObject = resultArray.getJSONObject(i);
				logger.trace("object: " + theObject.toString(3));
				String doi = theObject.getString("rel_doi");

				try {
					PreparedStatement citeStmt = conn.prepareStatement("insert into covid_biorxiv.raw_biorxiv(doi,raw) values (?,?::jsonb)");
					citeStmt.setString(1, doi);
					citeStmt.setString(2, theObject.toString());
					citeStmt.executeUpdate();
					citeStmt.close();
					totalFetched++;
				} catch (SQLException e) {
					if (e.getSQLState().equals("23505")) {
						continue;
					} else {
						e.printStackTrace();
					}
				}

				logger.info("DOI: " + doi + " : " + theObject.getString("rel_title"));
				count++;
			}
			cursor += 30;
		}

		logger.info("total preprints fetched: " + totalFetched);

		simpleStmt("refresh materialized view covid_biorxiv.biorxiv_current");
	}

	static public void scan_feed() throws SQLException, IOException {
		int count = 0;
		simpleStmt("truncate covid_biorxiv.raw_biorxiv");

		URL theURL = new URL("https://connect.biorxiv.org/relate/collection_json.php?grp=181");
		BufferedReader reader = new BufferedReader(new InputStreamReader(theURL.openConnection().getInputStream()));

		JSONObject results = new JSONObject(new JSONTokener(reader));
		JSONArray resultArray = results.getJSONArray("rels");
		logger.info("array: " + resultArray.toString(3));

		for (int i = 0; i < resultArray.length(); i++) {
			if (resultArray.isNull(i))
				continue;
			JSONObject theObject = resultArray.getJSONObject(i);
			logger.trace("object: " + theObject.toString(3));
			logger.info("DOI: " + theObject.getString("rel_doi"));
			logger.info("title: " + theObject.getString("rel_title"));

			PreparedStatement citeStmt = conn.prepareStatement("insert into covid_biorxiv.raw_biorxiv values (?::jsonb)");
			citeStmt.setString(1, theObject.toString());
			citeStmt.executeUpdate();
			citeStmt.close();

			count++;
		}

		logger.info("total preprints: " + count);

		simpleStmt("refresh materialized view covid_biorxiv.biorxiv_current");
	}

	static public void scan_html() throws SQLException, IOException, InterruptedException {
		logger.info("");
		logger.info("Fetching PDF links...");
		logger.info("");
		// clean up previously failed downloads
		simpleStmt("delete from covid_biorxiv.biorxiv_map where doi not in (select doi from covid_biorxiv.biorxiv_text)");
		PreparedStatement fetchStmt = conn.prepareStatement("select doi,link from covid_biorxiv.biorxiv_current where doi not in (select doi from covid_biorxiv.biorxiv_map) and doi not in (select doi from covid_biorxiv.biorxiv_suppress) order by doi");
		ResultSet rs = fetchStmt.executeQuery();
		while (rs.next()) {
			String doi = rs.getString(1);
			String link = rs.getString(2);
			logger.info("doi: " + doi + "\tlink: " + link);
			try {
				Document doc = Jsoup
								.connect(link)
								.userAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/14.0.3 Safari/605.1.15")
								.timeout(0).get();
				Element element = doc.getElementsByClass("article-dl-pdf-link").first();
				logger.trace("element: " + element);
				String href = element.attr("href");
				logger.trace("\thref: " + href);

				String old_href = null;
				PreparedStatement urlStmt = conn.prepareStatement("select url from covid_biorxiv.biorxiv_map where doi = ?");
				urlStmt.setString(1, doi);
				ResultSet urlRS = urlStmt.executeQuery();
				while (urlRS.next()) {
					old_href = urlRS.getString(1);
				}
				urlStmt.close();

				if (old_href == null) {
					// new entry
					PreparedStatement citeStmt = conn.prepareStatement("insert into covid_biorxiv.biorxiv_map values (?,?)");
					citeStmt.setString(1, doi);
					citeStmt.setString(2, href);
					citeStmt.executeUpdate();
					citeStmt.close();
					URL fetchURL = new URL(new URL(link), href);
					logger.info("\tfetching new... " + fetchURL);
					// for whatever reason, we're starting to see 301 permanent redirects on
					// fetchURL...
					URLConnection connection = fetchURL.openConnection();
					String redirect = connection.getHeaderField("Location");
					if (redirect != null) {
						logger.info("\t\tredirecting to... " + redirect);
						connection = new URL(new URL(redirect), href).openConnection();
					}
					Files.copy(connection.getInputStream(),
							Paths.get(filePrefix + fetchURL.getFile().substring(fetchURL.getFile().lastIndexOf('/') + 1)),
							StandardCopyOption.REPLACE_EXISTING);
				} else if (!href.equals(old_href)) {
					// updated entry
					PreparedStatement citeStmt = conn.prepareStatement("update covid_biorxiv.biorxiv_map set url = ? where doi = ?");
					citeStmt.setString(1, href);
					citeStmt.setString(2, doi);
					citeStmt.executeUpdate();
					citeStmt.close();
					URL fetchURL = new URL(new URL(link), href);
					logger.info("\tfetching updated... " + fetchURL);
					// for whatever reason, we're starting to see 301 permanent redirects on
					// fetchURL...
					URLConnection connection = fetchURL.openConnection();
					String redirect = connection.getHeaderField("Location");
					if (redirect != null) {
						logger.info("\t\tredirecting to... " + redirect);
						connection = new URL(new URL(redirect), href).openConnection();
					}
					Files.copy(connection.getInputStream(),
							Paths.get(filePrefix + fetchURL.getFile().substring(fetchURL.getFile().lastIndexOf('/') + 1)),
							StandardCopyOption.REPLACE_EXISTING);
				} else {
					logger.info("\tskipping...");
				}
			} catch (Exception e) {
				logger.error("Exception raised: " + e);
			}
			Thread.sleep(5000);
		}
		fetchStmt.close();
	}

	static public void scan_pdf() throws SQLException, IOException, SAXException, TikaException {
		logger.info("");
		logger.info("Extracting text from PDF...");
		logger.info("");
		PreparedStatement fetchStmt = conn.prepareStatement("select doi,url from covid_biorxiv.biorxiv_map where doi not in (select doi from covid_biorxiv.biorxiv_text)");
		ResultSet rs = fetchStmt.executeQuery();
		while (rs.next()) {
			String doi = rs.getString(1);
			String fetchURL = rs.getString(2);
			String file = filePrefix + fetchURL.substring(fetchURL.lastIndexOf('/') + 1);
			logger.info("DOI: " + doi + "\tfile: " + file);

			try {
				String contents = parseToPlainText(file);
				PreparedStatement citeStmt = conn.prepareStatement("insert into covid_biorxiv.biorxiv_text values (?,?)");
				citeStmt.setString(1, doi);
				citeStmt.setString(2, contents);
				citeStmt.executeUpdate();
				citeStmt.close();
			} catch (Exception e) {
				logger.error("Exception raised: " + e);
			}

		}
	}

	static public void fetch() throws SQLException, IOException, InterruptedException {
		logger.info("");
		logger.info("Fetching metadata...");
		logger.info("");
		PreparedStatement fetchStmt = conn.prepareStatement("select doi from covid_biorxiv.biorxiv_current where doi !~ '\\.200' and doi not in (select doi from covid_biorxiv.biorxiv_meta_raw) and doi not in (select doi from covid_biorxiv.biorxiv_fetch_suppress) order by pub_date");
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
				PreparedStatement citeStmt = conn.prepareStatement("insert into covid_biorxiv.biorxiv_meta_raw values (?,?::jsonb)");
				citeStmt.setString(1, doi);
				citeStmt.setString(2, results.toString(3));
				citeStmt.executeUpdate();
				citeStmt.close();
			} catch (java.io.FileNotFoundException e) {
				logger.error("\tDOI not found...");
			}
			Thread.sleep(5000);
		}
	}

	static public String parseToPlainText(String path) throws IOException, SAXException, TikaException {
		BodyContentHandler handler = new BodyContentHandler(-1);
		FileInputStream str = new FileInputStream(path);
		AutoDetectParser parser = new AutoDetectParser();
		Metadata metadata = new Metadata();
		parser.parse(str, handler, metadata);

//	String[] metadataNames = metadata.names();
//	for (String name : metadataNames) {
//	    System.out.println(name + " : " + metadata.get(name));
//	}

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
