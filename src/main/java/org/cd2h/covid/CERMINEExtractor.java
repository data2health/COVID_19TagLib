package org.cd2h.covid;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.jdom.Element;

import edu.uiowa.slis.GitHubTagLib.util.LocalProperties;
import edu.uiowa.slis.GitHubTagLib.util.PropertyLoader;
import pl.edu.icm.cermine.ContentExtractor;
import pl.edu.icm.cermine.exception.AnalysisException;
import pl.edu.icm.cermine.exception.TransformationException;
import pl.edu.icm.cermine.structure.model.BxDocument;
import pl.edu.icm.cermine.structure.model.BxLine;
import pl.edu.icm.cermine.structure.transformers.TrueVizToBxDocumentReader;

public class CERMINEExtractor {
    static Logger logger = Logger.getLogger(CERMINEExtractor.class);
    static DecimalFormat formatter = new DecimalFormat("####.00");
    protected static LocalProperties prop_file = null;
    static Connection conn = null;
    static String filePrefix = "/Volumes/Pegasus0/COVID/";

    public static void main(String[] args) throws Exception {
	System.setProperty("java.awt.headless", "true");
	PropertyConfigurator.configure("/Users/eichmann/Documents/Components/log4j.info");
	
	test2();
    }
    
    static void test1() throws AnalysisException, IOException {
	ContentExtractor extractor = new ContentExtractor();
	InputStream inputStream = new FileInputStream(filePrefix+"2020.04.21.042911v1.full.pdf");
	extractor.setPDF(inputStream);
	Element result = extractor.getContentAsNLM();
	logger.info(result.toString());
    }

    static void test2() throws AnalysisException, IOException, TransformationException {
        InputStream is = new FileInputStream("/Users/eichmann/Downloads/test/2020.04.21.054221v1.full.cermstr");
        TrueVizToBxDocumentReader reader = new TrueVizToBxDocumentReader();
        Reader r = new InputStreamReader(is, "UTF-8");
        BxDocument bxDoc = new BxDocument().setPages(reader.read(r));

        double avgDiffZone = 0;
        int countDZ = 0;

        for (BxLine line : bxDoc.asLines()) {
            logger.info("line: [" + formatter.format(line.getX()) + " " + formatter.format(line.getY()) + " " + formatter.format(line.getHeight()) + " " + formatter.format(line.getWidth()) + " " + line.getMostPopularFontName() + "] " + line.toText());
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
