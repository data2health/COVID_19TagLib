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
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.jdom.Element;

import edu.uiowa.slis.GitHubTagLib.util.LocalProperties;
import edu.uiowa.slis.GitHubTagLib.util.PropertyLoader;
import pl.edu.icm.cermine.ContentExtractor;
import pl.edu.icm.cermine.exception.AnalysisException;
import pl.edu.icm.cermine.exception.TransformationException;
import pl.edu.icm.cermine.structure.model.BxChunk;
import pl.edu.icm.cermine.structure.model.BxDocument;
import pl.edu.icm.cermine.structure.model.BxImage;
import pl.edu.icm.cermine.structure.model.BxLine;
import pl.edu.icm.cermine.structure.model.BxPage;
import pl.edu.icm.cermine.structure.model.BxWord;
import pl.edu.icm.cermine.structure.model.BxZone;
import pl.edu.icm.cermine.structure.transformers.TrueVizToBxDocumentReader;

public class CERMINEExtractor {
    static Logger logger = Logger.getLogger(CERMINEExtractor.class);
    static DecimalFormat formatter = new DecimalFormat("0000.00");
    protected static LocalProperties prop_file = null;
    static Connection conn = null;
    static String filePrefix = "/Volumes/Pegasus0/COVID/";

    public static void main(String[] args) throws Exception {
	System.setProperty("java.awt.headless", "true");
	PropertyConfigurator.configure("/Users/eichmann/Documents/Components/log4j.info");
	
	test4();
    }
    
    static void test1() throws AnalysisException, IOException {
	ContentExtractor extractor = new ContentExtractor();
	InputStream inputStream = new FileInputStream(filePrefix+"2020.04.21.042911v1.full.pdf");
	extractor.setPDF(inputStream);
	Element result = extractor.getContentAsNLM();
	logger.info(result.toString());
    }

    static void test2() throws AnalysisException, IOException, TransformationException {
	double headerLimit = 15.00;
	double counterLimit = 67.00;
	
        InputStream is = new FileInputStream("/Users/eichmann/downloads/test/2020.04.21.054221v1.full.cermstr");
        TrueVizToBxDocumentReader reader = new TrueVizToBxDocumentReader();
        Reader r = new InputStreamReader(is, "UTF-8");
        BxDocument bxDoc = new BxDocument().setPages(reader.read(r));

        double avgDiffZone = 0;
        int countDZ = 0;

        for (BxLine line : bxDoc.asLines()) {
            if (line.getY() < headerLimit)
        	continue;
            if (line.getX() < counterLimit)
        	continue;
          
            logger.info("line: [" + String.format("%6.2f %6.2f %4.2f %6.2f", line.getX(),line.getY(),line.getHeight(),line.getWidth()) + "] " + line.toText() + "\t" + line.getMostPopularFontName());
            if (Math.abs(line.getHeight() - 12.95) < 0.1)
        	for(int i = 0; i < line.childrenCount(); i++) {
        	    BxWord word = line.getChild(i);
        	    logger.info("\tword: " + word.toText());
        	    for (int j = 0; j < word.childrenCount(); j++) {
        		BxChunk chunk = word.getChild(j);
        		logger.info("\t\tchunk: " + chunk.toText() + " " + chunk.getHeight() + " " + chunk.getMostPopularFontName());
        	    }
        	}
        }
    }

    static void test3() throws AnalysisException, IOException, TransformationException {
	double headerLimit = 15.00;
	double counterLimit = 67.00;
	
//        InputStream is = new FileInputStream("/Users/eichmann/downloads/test/2020.04.21.054221v1.full.cermstr");
//        TrueVizToBxDocumentReader reader = new TrueVizToBxDocumentReader();
//        Reader r = new InputStreamReader(is, "UTF-8");
//        BxDocument bxDoc = new BxDocument().setPages(reader.read(r));

        ContentExtractor extractor = new ContentExtractor();
	InputStream inputStream = new FileInputStream("/Users/eichmann/downloads/test/2020.04.21.054221v1.full.pdf");
	extractor.setPDF(inputStream);
	BxDocument bxDoc = extractor.getBxDocument();

        for (BxPage page : bxDoc.asPages()) {
            logger.info("page: [" + String.format("%6.2f %6.2f %4.2f %6.2f", page.getX(),page.getY(),page.getHeight(),page.getWidth()) + "] " + page.getId());
            for (BxImage image : page.getImages()) {
                logger.info("\timage: [" + String.format("%6.2f %6.2f", image.getX(),image.getY()) + "] " + image.getFilename() + " : " + image.getPath());
            }
            for (int i = 0; i < page.childrenCount(); i++) {
        	BxZone zone = page.getChild(i);
                logger.info("\tzone: [" + String.format("%6.2f %6.2f %4.2f %6.2f", zone.getX(),zone.getY(),zone.getHeight(),zone.getWidth()) + "] " + zone.getId() + " : " + zone.getLabel());
//                for (int j = 0; j < zone.childrenCount(); j++) {
//                    BxLine line = zone.getChild(j);
//                    logger.info("\t\tline: [" + String.format("%6.2f %6.2f %4.2f %6.2f", line.getX(),line.getY(),line.getHeight(),line.getWidth()) + "] " + line.toText() + "\t" + line.getMostPopularFontName());
//                }
            }
        }
    }

    static void test4() throws AnalysisException, IOException, TransformationException {
	double headerLimit = 15.00;
	double counterLimit = 67.00;
	
//        InputStream is = new FileInputStream("/Users/eichmann/downloads/test/2020.04.21.054221v1.full.cermstr");
//        TrueVizToBxDocumentReader reader = new TrueVizToBxDocumentReader();
//        Reader r = new InputStreamReader(is, "UTF-8");
//        BxDocument bxDoc = new BxDocument().setPages(reader.read(r));

        ContentExtractor extractor = new ContentExtractor();
	InputStream inputStream = new FileInputStream("/Users/eichmann/downloads/test/2020.04.21.054221v1.full.pdf");
	extractor.setPDF(inputStream);

        for (BxImage image : (List<BxImage>)extractor.getImages("")) {
            logger.info("image: [" + String.format("%6.2f %6.2f", image.getX(),image.getY()) + "] " + image.getFilename() + " : " + image.toString());
        }
	BxDocument bxDoc = extractor.getBxDocument();
	logger.info("# pages: " + bxDoc.childrenCount());
        for (BxPage page : bxDoc.asPages()) {
            logger.info("page: [" + String.format("%6.2f %6.2f %4.2f %6.2f", page.getX(),page.getY(),page.getHeight(),page.getWidth()) + "] " + page.getId());
            for (int i = 0; i < page.childrenCount(); i++) {
        	BxZone zone = page.getChild(i);
                logger.info("\tzone: [" + String.format("%6.2f %6.2f %4.2f %6.2f", zone.getX(),zone.getY(),zone.getHeight(),zone.getWidth()) + "] " + zone.getId() + " : " + zone.getLabel());
                for (int j = 0; j < zone.childrenCount(); j++) {
                    BxLine line = zone.getChild(j);
                    logger.info("\t\tline: [" + String.format("%6.2f %6.2f %4.2f %6.2f", line.getX(),line.getY(),line.getHeight(),line.getWidth()) + "] " + line.toText() + "\t" + line.getMostPopularFontName());
                }
            }
        }
	for (BxImage image : bxDoc.asImages()) {
	    logger.info("\timage: [" + String.format("%6.2f %6.2f", image.getX(), image.getY()) + "] " + image.getFilename() + " : " + image.getPath());
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
