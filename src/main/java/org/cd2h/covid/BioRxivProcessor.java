package org.cd2h.covid;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import edu.uiowa.NLP_grammar.FragmentGenerator;
import edu.uiowa.NLP_grammar.ParseFragment;
import edu.uiowa.NLP_grammar.SegmentParser;
import edu.uiowa.NLP_grammar.SimpleStanfordParserBridge;
import edu.uiowa.NLP_grammar.TextSegment;
import edu.uiowa.NLP_grammar.TextSegmentElement;
import edu.uiowa.NLP_grammar.syntaxTree;
import edu.uiowa.PubMedCentral.BiomedicalSentenceGenerator;
import edu.uiowa.extraction.LocalProperties;
import edu.uiowa.extraction.PropertyLoader;
import edu.uiowa.extraction.TemplatePromoter;
import edu.uiowa.lex.biomedicalLexerMod;

public class BioRxivProcessor implements Runnable {
    static Logger logger = Logger.getLogger(BioRxivProcessor.class);
    protected static LocalProperties prop_file = null;
    static StringQueue doiQueue = new StringQueue();
    static String mode = "";

    public static void main(String[] args) throws Exception {
	PropertyConfigurator.configure(args[0]);
	prop_file = PropertyLoader.loadProperties("biorxiv");
	Connection conn = getConnection();
	PreparedStatement stmt = null;
	if (args.length > 1)
	    mode = args[1];
	switch (mode) {
	case "parse":
	    stmt = conn.prepareStatement("select doi from covid_biorxiv.document where doi not in (select doi from covid_biorxiv.parse)");
	    break;
	case "fragment":
	    stmt = conn.prepareStatement("select distinct doi from covid_biorxiv.parse where doi not in (select doi from covid_biorxiv.fragment)");
	    break;
	}
	int skip = 0;
	ResultSet rs = stmt.executeQuery();
	while (rs.next()) {
	    String doi = rs.getString(1);
	    if (++skip < 10)
		continue;
	    logger.debug("queueing : " + doi);
	    doiQueue.queue(doi);
	}
	logger.info("\t" + doiQueue.size() + " files queued.");

	int maxCrawlerThreads = Runtime.getRuntime().availableProcessors() == 8 ? Runtime.getRuntime().availableProcessors() / 4 : (Runtime.getRuntime().availableProcessors() * 3) / 4;
	maxCrawlerThreads = 1;
	Thread[] scannerThreads = new Thread[maxCrawlerThreads];

	for (int i = 0; i < maxCrawlerThreads; i++) {
	    logger.info("starting thread " + i);
	    Thread theThread = new Thread(new BioRxivProcessor(i));
	    theThread.setPriority(Math.max(theThread.getPriority() - 2, Thread.MIN_PRIORITY));
	    theThread.start();
	    scannerThreads[i] = theThread;
	}

	for (int i = 0; i < maxCrawlerThreads; i++) {
	    scannerThreads[i].join();
	}
	logger.info("parsing completed.");
    }
    
    int threadID = 0;
    Connection conn = null;
    SegmentParser theParser = null;
    FragmentGenerator theGenerator = null;
    
    public BioRxivProcessor(int threadID) throws Exception {
	this.threadID = threadID;
	conn = getConnection();
	theParser = new SegmentParser(new biomedicalLexerMod(), new SimpleStanfordParserBridge(), new BiomedicalSentenceGenerator(conn));
    }

    @Override
    public void run() {
	while (!doiQueue.isCompleted()) {
	    String doi = doiQueue.dequeue();
	    if (doi == null)
		return;
	    try {
		switch (mode) {
		case "parse":
		    try {
			parse(doi);
		    } catch (Exception e) {
			logger.error("Exception raised parsing : " + doi, e);
		    }
		    break;
		case "fragment":
		    fragment(doi);
		    break;
		}
	    } catch (Exception e) {
		logger.error("error raised proccessing doi:", e);
	    }
	}
    }

    public void parse(String doi) throws SQLException {
	logger.info("[" + threadID + "] processing: " + doi);
	PreparedStatement stmt = conn.prepareStatement("select seqnum,sentnum,trimmed_text from covid_biorxiv.sentence where trimmed_text is not null and trimmed_text != '' and doi = ?");
	stmt.setString(1, doi);
	ResultSet rs = stmt.executeQuery();
	while (rs.next()) {
	    int seqnum = rs.getInt(1);
	    int sentnum = rs.getInt(2);
	    String sentence = rs.getString(3);
	    int parseCount = 0;
		logger.info("[" + threadID + "] sentence: " + sentence);
	    TextSegment segment = theParser.parse(sentence);
	    for (TextSegmentElement element : segment.getElementVector()) {
		logger.info("[" + threadID + "] sentence: " + element.getSentence());
		
		PreparedStatement sentStmt = conn.prepareStatement("update covid_biorxiv.sentence set pos_tags = ? where doi = ? and seqnum = ? and sentnum = ?");
		sentStmt.setString(1, element.getSentence().toString());
		sentStmt.setString(2, doi);
		sentStmt.setInt(3, seqnum);
		sentStmt.setInt(4, sentnum);
		sentStmt.execute();
		sentStmt.close();
		
		for (syntaxTree theTree : element.getParseVector()) {
		    logger.info("[" + threadID + "]\tparse: " + theTree.treeString());

		    PreparedStatement parseStmt = conn.prepareStatement("insert into covid_biorxiv.parse values(?,?,?,?,?)");
		    parseStmt.setString(1, doi);
		    parseStmt.setInt(2, seqnum);
		    parseStmt.setInt(3, sentnum);
		    parseStmt.setInt(4, ++parseCount);
		    parseStmt.setString(5, theTree.treeString());
		    parseStmt.execute();
		    parseStmt.close();
		}
	    }
	}
	stmt.close();
	conn.commit();
    }

    public void fragment(String doi) throws Exception {
	theGenerator = new FragmentGenerator(new BioRxivDecorator(conn), new BioRxivInstantiator(prop_file, conn, doi), new TemplatePromoter(conn));
	PreparedStatement sourceStmt = conn.prepareStatement("select seqnum, sentnum, parsenum, parse from covid_biorxiv.parse where doi = ? order by seqnum,sentnum");
	sourceStmt.setString(1, doi);
	ResultSet sourceRS = sourceStmt.executeQuery();
	while (sourceRS.next()) {
	    int seqnum = sourceRS.getInt(1);
	    int sentnum = sourceRS.getInt(2);
	    int parsenum = sourceRS.getInt(3);
	    String parseString = sourceRS.getString(4);
	    logger.debug("[" + threadID + "] : " + doi + " : " + seqnum + " : " + sentnum + " : " + parsenum + " : " + parseString);
	    for (ParseFragment fragment : theGenerator.fragments(doi, parseString)) {
		logger.info("\tfragment: " + fragment.getFragmentString());
		logger.info("\t\tparse: " + fragment.getFragmentParse());

		PreparedStatement fragStmt = conn.prepareStatement("insert into covid_biorxiv.fragment values(?,?,?,?,?)");
		fragStmt.setString(1, doi);
		fragStmt.setInt(2, seqnum);
		fragStmt.setInt(3, sentnum);
		fragStmt.setString(4, fragment.getFragmentString());
		fragStmt.setString(5, fragment.getFragmentParse());
		fragStmt.execute();
		fragStmt.close();
	    }
	}
	sourceStmt.close();
 	conn.commit();
     }

    public static Connection getConnection() throws SQLException, ClassNotFoundException {
	Class.forName("org.postgresql.Driver");
	Properties props = new Properties();
	props.setProperty("user", prop_file.getProperty("jdbc.user"));
	props.setProperty("password", prop_file.getProperty("jdbc.password"));
	Connection conn = DriverManager.getConnection(prop_file.getProperty("jdbc.url"), props);
	conn.setAutoCommit(false);
	return conn;
    }

    public void simpleStmt(String queryString) {
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
