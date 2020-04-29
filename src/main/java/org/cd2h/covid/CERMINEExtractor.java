package org.cd2h.covid;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.cd2h.covid.model.Document;
import org.cd2h.covid.model.Line;
import org.cd2h.covid.model.Page;
import org.jdom.Element;

import edu.uiowa.slis.GitHubTagLib.util.LocalProperties;
import edu.uiowa.slis.GitHubTagLib.util.PropertyLoader;
import pl.edu.icm.cermine.ComponentConfiguration;
import pl.edu.icm.cermine.ContentExtractor;
import pl.edu.icm.cermine.configuration.ExtractionConfigProperty;
import pl.edu.icm.cermine.configuration.ExtractionConfigRegister;
import pl.edu.icm.cermine.exception.AnalysisException;
import pl.edu.icm.cermine.exception.TransformationException;
import pl.edu.icm.cermine.structure.ITextCharacterExtractor;
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
    
    static boolean hasLineNumbers = false;
    static boolean hasPageNumbers = false;
    static int widthCutoff = 0;
    static int xCutoff = 0;
    static int yCutoff = Integer.MAX_VALUE;

    public static void main(String[] args) throws Exception {
	System.setProperty("java.awt.headless", "true");
	PropertyConfigurator.configure(args[0]);
	prop_file = PropertyLoader.loadProperties("zotero");
	conn = getConnection();
	
	Document doc = null;
	
	PreparedStatement stmt = conn.prepareStatement("select doi from covid_biorxiv.biorxiv_map where url ~ ?");
	stmt.setString(1, args[1]);
	ResultSet rs = stmt.executeQuery();
	while (rs.next()) {
	    doc = new Document(rs.getString(1), args[1]);
	}
	
	acquireBxDocument(doc);
	doc.dump();
    }
    
    static void test1() throws AnalysisException, IOException {
        ContentExtractor extractor = getContentExtractor();
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

    static void test3(String fileName) throws AnalysisException, IOException, TransformationException {
//        InputStream is = new FileInputStream("/Users/eichmann/downloads/test/2020.04.21.054221v1.full.cermstr");
//        TrueVizToBxDocumentReader reader = new TrueVizToBxDocumentReader();
//        Reader r = new InputStreamReader(is, "UTF-8");
//        BxDocument bxDoc = new BxDocument().setPages(reader.read(r));

        ContentExtractor extractor = getContentExtractor();
	InputStream inputStream = new FileInputStream("/Users/eichmann/downloads/test/"+fileName);
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
                for (int j = 0; j < zone.childrenCount(); j++) {
                    BxLine line = zone.getChild(j);
                    logger.info("\t\tline: [" + String.format("%6.2f %6.2f %4.2f %6.2f", line.getX(),line.getY(),line.getHeight(),line.getWidth()) + "] " + line.toText() + "\t" + line.getMostPopularFontName());
                }
            }
        }
    }

    static void acquireBxDocument(Document doc) throws AnalysisException, IOException, TransformationException {
//        InputStream is = new FileInputStream("/Users/eichmann/downloads/test/2020.04.21.054221v1.full.cermstr");
//        TrueVizToBxDocumentReader reader = new TrueVizToBxDocumentReader();
//        Reader r = new InputStreamReader(is, "UTF-8");
//        BxDocument bxDoc = new BxDocument().setPages(reader.read(r));

        ContentExtractor extractor = getContentExtractor();
 	InputStream inputStream = new FileInputStream("/Users/eichmann/downloads/test/"+doc.getFileName());
	extractor.setPDF(inputStream);

        for (BxImage image : (List<BxImage>)extractor.getImages("")) {
            logger.info("image: [" + String.format("%6.2f %6.2f", image.getX(),image.getY()) + "] " + image.getFilename() + " : " + image.toString());
        }
	BxDocument bxDoc = extractor.getBxDocument();
	logger.info("# pages: " + bxDoc.childrenCount());
	documentStats(bxDoc);
        for (BxPage bxpage : bxDoc.asPages()) {
            Page page = new Page(bxpage);
            doc.addPage(page);
            Vector<BxLine> lines = new Vector<BxLine>();
            logger.info("page: [" + String.format("%6.2f %6.2f %4.2f %6.2f", bxpage.getX(),bxpage.getY(),bxpage.getHeight(),bxpage.getWidth()) + "] " + bxpage.getId() + " : " + bxpage.getMostPopularFontName());
            for (BxImage image : bxpage.getImages()) {
                logger.info("\timage: [" + String.format("%6.2f %6.2f", image.getX(),image.getY()) + "] " + image.getFilename() + " : " + image.getPath());
            }
            for (int i = 0; i < bxpage.childrenCount(); i++) {
        	BxZone zone = bxpage.getChild(i);
                if (zone.getY() < 15.00 || (hasLineNumbers && ((int)zone.getX()) <= xCutoff && ((int)zone.getWidth()) <= widthCutoff) || (hasPageNumbers && (int)zone.getY() >= yCutoff))
                    continue;
                logger.info("\tzone: [" + String.format("%6.2f %6.2f %4.2f %6.2f", zone.getX(),zone.getY(),zone.getHeight(),zone.getWidth()) + "] " + zone.getId() + " : " + zone.getLabel());
                for (int j = 0; j < zone.childrenCount(); j++) {
                    BxLine line = zone.getChild(j);
                    lines.add(line);
                    logger.info("\t\tline: [" + String.format("%6.2f %6.2f %4.2f %6.2f", line.getX(),line.getY(),line.getHeight(),line.getWidth()) + "] " + line.toText() + "\t" + line.getMostPopularFontName());
                }
            }
            sortLines(page, lines);
        }
	for (BxImage image : bxDoc.asImages()) {
	    logger.info("\timage: [" + String.format("%6.2f %6.2f", image.getX(), image.getY()) + "] " + image.getFilename() + " : " + image.getPath());
	}
    }
    
    static void sortLines(Page page, Vector<BxLine> lines) {
	int[] lineSpacings = new int[1000];
	boolean updated = false;
	Comparator<BxLine> comparator = new LineComparator();
	Collections.sort(lines, comparator);
	logger.info("");
	int prevY = 0;
	for (BxLine line : lines) {
	    int currY = Math.max(0, (int)line.getY() - prevY);
            logger.info("\tsorted line: [" + String.format("%6.2f %6.2f %4.2f %6.2f", line.getX(),line.getY(),line.getHeight(),line.getWidth()) + "] " + currY + " : " + line.toText() + "\t" + line.getMostPopularFontName());
            if (prevY > 0)
        	lineSpacings[currY]++;
            prevY = (int)line.getY();
	}
	logger.info("");
	for (int i = 0; i < lines.size() - 1; i++) {
	    boolean continuation = false;
	    while (i < lines.size() - 1 && Math.abs(lines.elementAt(i).getY() - lines.elementAt(i + 1).getY()) < 0.1) {
		logger.info("continuation match line: [" + String.format("%6.2f %6.2f %4.2f %6.2f", lines.elementAt(i + 1).getX(), lines.elementAt(i + 1).getY(), lines.elementAt(i + 1).getHeight(), lines.elementAt(i + 1).getWidth()) + "] " + lines.elementAt(i + 1).toText());
		for (int j = 0; j < lines.elementAt(i + 1).childrenCount(); j++) {
		    lines.elementAt(i).addWord(lines.elementAt(i + 1).getChild(j));
		}
		lines.remove(i + 1);
		continuation = true;
		updated = true;
	    }
            if (continuation)
        	logger.info("\tupdated line: [" + String.format("%6.2f %6.2f %4.2f %6.2f", lines.elementAt(i).getX(),lines.elementAt(i).getY(),lines.elementAt(i).getHeight(),lines.elementAt(i).getWidth()) + "] " + lines.elementAt(i).toText());
	}
	logger.info("");
	if (updated) {
	    lineSpacings = new int[1000];
	    prevY = 0;
	    for (BxLine line : lines) {
		int currY = (int)line.getY() - prevY;
		logger.info("\tsorted line: [" + String.format("%6.2f %6.2f %4.2f %6.2f", line.getX(), line.getY(), line.getHeight(), line.getWidth()) + "] " + currY + " : " + line.toText() + "\t" + line.getMostPopularFontName());
		if (prevY > 0)
		    lineSpacings[currY]++;
		prevY = (int)line.getY();
	    }
	    logger.info("");
	}
        for (int i = 0; i < 1000; i++) {
            if (lineSpacings[i] < 1)
        	continue;
            logger.info("lineSpacings[" + i + "] : " + lineSpacings[i]);
        }
        for (BxLine line : lines) {
            page.addLine(new Line(line));            
        }
        page.addLineSpacing(lineSpacings);
    }

    static public void initialize() throws ClassNotFoundException, SQLException {
	prop_file = PropertyLoader.loadProperties("zotero");

	conn = getConnection();
    }
    
    public static ContentExtractor getContentExtractor() throws AnalysisException {
	// ITextCharacterExtractor.java logic to skip images
//        if (!ExtractionConfigRegister.get().getBooleanProperty(ExtractionConfigProperty.IMAGES_EXTRACTION)) {
//            return;
//        }

	logger.info("property: " + ExtractionConfigProperty.IMAGES_EXTRACTION + " : " + ExtractionConfigRegister.get().getBooleanProperty(ExtractionConfigProperty.IMAGES_EXTRACTION));
	ComponentConfiguration config = new ComponentConfiguration();
	ITextCharacterExtractor charExtractor = new ITextCharacterExtractor();
	charExtractor.setPagesLimits(1000, 1000);
	config.setCharacterExtractor(charExtractor);
        ContentExtractor extractor = new ContentExtractor();
        extractor.setConf(config);
        
        return extractor;
    }
    
    public static boolean patternCheck(int width, BxDocument bxDoc) {
        for (BxPage page : bxDoc.asPages()) {
            for (int i = 0; i < page.childrenCount(); i++) {
        	BxZone zone = page.getChild(i);
                for (int j = 0; j < zone.childrenCount(); j++) {
                    BxLine line = zone.getChild(j);
                    if ((int)line.getWidth() != width)
                	continue;
                    logger.info("\tline: " + line.toText());
                    if (!line.toText().trim().matches("[0-9][0-9]*"))
                	return false;
                }
            }
        }
	return true;
    }
    
    public static void documentStats(BxDocument bxDoc) {
	int[] horzizontalFrequencies = new int[1000];
	int[] widthFrequencies = new int[1000];
	int lineCount = 0;
	int widthCum = 0;
	int horzCum = 0;
	
	BxPage secondPage = bxDoc.getChild(1);
	BxZone secondZone = secondPage.getChild(secondPage.childrenCount()-1);
	String zoneText = secondZone.toText();
	logger.info("page zone candidate: " + zoneText);
	hasPageNumbers = zoneText.length() < 50 && zoneText.endsWith("2");
	if (hasPageNumbers)
	    yCutoff = (int)secondZone.getY() - 1;
	logger.info("hasPageNumbers: " + hasPageNumbers);

        for (BxPage page : bxDoc.asPages()) {
            for (int i = 0; i < page.childrenCount(); i++) {
        	BxZone zone = page.getChild(i);
                for (int j = 0; j < zone.childrenCount(); j++) {
                    BxLine line = zone.getChild(j);
                    lineCount++;
                    if (line.getX() > 100.00 || !line.toText().trim().matches("[0-9][0-9]*"))
                	continue;
                    logger.trace("\tline: " + line.toText());
                    widthFrequencies[(int)line.getWidth()]++;
                    horzizontalFrequencies[(int)line.getX()]++;
               }
            }
        }
        logger.info("line count: " + lineCount);
        for (int i = 0; i < 1000; i++) {
            if (widthFrequencies[i] < 9)
        	continue;
            widthCum += widthFrequencies[i];
            widthCutoff = i;
            logger.info("width[" + i + "] : " + widthFrequencies[i] + " : " + widthCum);
        }
        logger.info("widthCutoff: " + widthCutoff);
        for (int i = 0; i < 1000; i++) {
            if (horzizontalFrequencies[i] < 9)
        	continue;
            horzCum += horzizontalFrequencies[i];
            xCutoff = i;
            logger.info("horz[" + i + "] : " + horzizontalFrequencies[i] + " : " + horzCum);
        }
        logger.info("xCutoff: " + xCutoff);
        hasLineNumbers = widthCutoff > 0 && xCutoff > 0;
    }
    
    public static void documentStats2(BxDocument bxDoc) {
	int[] horzizontalFrequencies = new int[1000];
	int[] widthFrequencies = new int[1000];
	int lineCount = 0;
	int widthCum = 0;
	int heightCum = 0;
	
        for (BxPage page : bxDoc.asPages()) {
            for (int i = 0; i < page.childrenCount(); i++) {
        	BxZone zone = page.getChild(i);
                for (int j = 0; j < zone.childrenCount(); j++) {
                    BxLine line = zone.getChild(j);
                    if (line.getY() < 15.00)
                	continue;
                    lineCount++;
                    widthFrequencies[(int)line.getWidth()]++;
                }
            }
        }
        logger.info("line count: " + lineCount);
        for (int i = 0; i < 1000; i++) {
            if (widthFrequencies[i] < 10)
        	continue;
            if (patternCheck(i,bxDoc)) {
        	logger.info("numeric zone: " + i);
            }
            widthCum += widthFrequencies[i];
            if (widthCum < lineCount / 2)
        	widthCutoff = i + 5;
            logger.info("width[" + i + "] : " + widthFrequencies[i] + " : " + widthCum);
        }
        logger.info("widthCutoff: " + widthCutoff);

        for (BxPage page : bxDoc.asPages()) {
            for (int i = 0; i < page.childrenCount(); i++) {
        	BxZone zone = page.getChild(i);
                for (int j = 0; j < zone.childrenCount(); j++) {
                    BxLine line = zone.getChild(j);
                    if (line.getY() < 15.00)
                	continue;
                    if (((int)line.getWidth()) > widthCutoff)
                	continue;
                    horzizontalFrequencies[(int)line.getX()]++;
                }
            }
        }
        for (int i = 0; i < 1000; i++) {
            if (horzizontalFrequencies[i] == 0)
        	continue;
            heightCum += horzizontalFrequencies[i];
            if (heightCum > lineCount / 2)
        	continue;
            xCutoff = i;
            logger.info("horz[" + i + "] : " + horzizontalFrequencies[i] + " : " + heightCum);
        }
        logger.info("xCutoff: " + xCutoff);
        hasLineNumbers = widthCutoff < 66;
        logger.info("hasLineNumbers: " + hasLineNumbers);
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
