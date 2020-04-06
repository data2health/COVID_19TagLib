package org.cd2h.covid;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import edu.uiowa.slis.GitHubTagLib.util.LocalProperties;
import edu.uiowa.slis.GitHubTagLib.util.PropertyLoader;
// https://www.clinicaltrialsregister.eu/ctr-search/rest/download/full?query=eudract_number:2020-000890-25&mode=current_page

public class EudraCTLoader {
    static Logger logger = Logger.getLogger(EudraCTLoader.class);
    protected static LocalProperties prop_file = null;
    static Connection conn = null;
    static String requestTemplate = "https://www.clinicaltrialsregister.eu/ctr-search/rest/download/full?query=eudract_number:xxx&mode=current_page";
    static Pattern pattern = Pattern.compile("^([A-Z](\\.[0-9]+|\\.)*)( ([^:]+)(: (.*))?)?$");

    public static void main(String[] args) throws ClassNotFoundException, SQLException, IOException, InterruptedException, KeyManagementException, NoSuchAlgorithmException {
	PropertyConfigurator.configure("/Users/eichmann/Documents/Components/log4j.info");
	initialize();
	trustAllCerts();
	
	scan();
	parse();

    }

    static public void scan() throws SQLException, IOException, InterruptedException {
	PreparedStatement fetchStmt = conn.prepareStatement("select trialid from who_ictrp.who where source_register='EU Clinical Trials Register' and trialid not in (select id from eudract.raw)");
	ResultSet rs = fetchStmt.executeQuery();
	while (rs.next()) {
	    String trialID = rs.getString(1);
	    String url = requestTemplate.replace("xxx", trialID.substring(5,trialID.length()-3));
	    logger.info("id: " + trialID + "\turl: " + url);
	    URL theURL = new URL(url);
	    StringBuffer result = new StringBuffer();
	    String buffer = null;
	    BufferedReader reader = new BufferedReader(new InputStreamReader(theURL.openConnection().getInputStream()));
	    while ((buffer = reader.readLine()) != null) {
		logger.info("\tbuffer: " + buffer);
		result.append((result.length() > 1 ? "\n" : "") + buffer);
	    }
	    reader.close();
	    
	    PreparedStatement stmt = conn.prepareStatement("insert into eudract.raw values(?,?)");
	    stmt.setString(1, trialID);
	    stmt.setString(2, result.toString());
	    stmt.execute();
	    stmt.close();
	}
	fetchStmt.close();
    }
    
    static public void parse() throws SQLException, IOException {
	PreparedStatement fetchStmt = conn.prepareStatement("select id,content from eudract.raw");
	ResultSet rs = fetchStmt.executeQuery();
	while (rs.next()) {
	    String trialID = rs.getString(1);
	    String contents = rs.getString(2);
	    BufferedReader reader = new BufferedReader(new StringReader(contents));
	    String buffer = null;
	    while ((buffer = reader.readLine()) != null) {
		logger.info(trialID + " buffer: " + buffer);
		Matcher matcher = pattern.matcher(buffer);
		if (matcher.matches()) {
		    String category = matcher.group(1);
		    String description = matcher.group(4);
		    String value = matcher.group(6);
		    logger.info("\tcategory: " + category);
		    logger.info("\tdescription: " + description);
		    logger.info("\tvalue: " + value);
		}
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

    static void trustAllCerts() throws NoSuchAlgorithmException, KeyManagementException {
	/*
	 * fix for Exception in thread "main" javax.net.ssl.SSLHandshakeException: sun.security.validator.ValidatorException: PKIX path building failed:
	 * sun.security.provider.certpath.SunCertPathBuilderException: unable to find valid certification path to requested target
	 */
	TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
	    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
		return null;
	    }

	    public void checkClientTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
	    }

	    public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
	    }

	} };

	SSLContext sc = SSLContext.getInstance("SSL");
	sc.init(null, trustAllCerts, new java.security.SecureRandom());
	HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

	// Create all-trusting host name verifier
	HostnameVerifier allHostsValid = new HostnameVerifier() {
	    @SuppressWarnings("unused")
	    public boolean verify1(String hostname, SSLSession session) {
		return true;
	    }

	    public boolean verify(String arg0, SSLSession arg1) {
		return true;
	    }
	};
	// Install the all-trusting host verifier
	HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);

    }
}
