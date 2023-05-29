package org.cd2h.covid.gkg;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.Observable;
import java.util.Observer;
import java.util.Properties;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.cd2h.covid.IntegerQueue;
import org.cd2h.covid.PropertyLoader;

import com.ibm.tspaces.Field;
import com.ibm.tspaces.Tuple;
import com.ibm.tspaces.TupleSpace;
import com.ibm.tspaces.TupleSpaceException;

import edu.uiowa.NLP_grammar.SimpleStanfordParserBridge;
import edu.uiowa.NLP_grammar.linkGrammarParserBridge;
import edu.uiowa.NLP_grammar.parser;
import edu.uiowa.NLP_grammar.syntaxTree;
import edu.uiowa.lex.HTMLLexer;
import edu.uiowa.lex.Sentence;
import edu.uiowa.lex.SentenceGenerator;
import edu.uiowa.lex.basicSentenceGenerator;
import edu.uiowa.pos_tagging.BrillTagger;
import edu.uiowa.pos_tagging.POStagger;

public class Parser implements Observer, Runnable {
	static Logger logger = Logger.getLogger(Parser.class);
	static Properties prop_file = PropertyLoader.loadProperties("gkg");
	static IntegerQueue queue = new IntegerQueue();
	static boolean useTSpace = false;
    static TupleSpace theTSpace = null;
    static String host = "192.168.2.72";

	public static void main(String[] args) throws IOException, ClassNotFoundException, SQLException, InterruptedException, TupleSpaceException {
		PropertyConfigurator.configure("log4j.info");
		BrillTagger.initialize(false);
		Connection conn = getConnection();
		
		if (args.length > 0 && args[0].equals("-hub")) {
			useTSpace = true;
			logger.info("initializing TSpace connection...");
			theTSpace = new TupleSpace("gkg", host);
			load(conn);
		} else if (args.length > 0 && args[0].equals("-client")) {
			useTSpace = true;
			logger.info("initializing TSpace connection...");
			theTSpace = new TupleSpace("gkg", host);
			launch();
		} else {
			load(conn);
			launch();
		}

	}
	
	static void load(Connection conn) throws SQLException, TupleSpaceException {
		PreparedStatement stmt = conn.prepareStatement("select foo.date,foo.id,source_url from"
				+ "	(select date,id,source_url,row_number() over(partition by date order by id) as rownum from"
				+ "		(select id,source_url from gkg_local.document where html is not null and id not in (select id from gkg_local.sentence)) as fud"
				+ "	natural join"
				+ "		gkg_local.fragment_staging"
				+ "	 ) as foo"
				+ " where rownum <= 100"
				+ " order by 1,2;");
//		PreparedStatement stmt = conn.prepareStatement("select id,source_url from gkg_local.document where html is not null and id not in (select id from gkg_local.sentence) limit 1000000");
		ResultSet rs = stmt.executeQuery();
		while (rs.next()) {
			String date = rs.getString(1);
			int id = rs.getInt(2);
			String url = rs.getString(3);
			logger.info("date:" + date + " url: " + id + " : " + url);
			if (useTSpace) {
				theTSpace.write(new Tuple("parse", id));
			} else
				queue.queue(id);
		}
		stmt.close();
	}
	
	static void launch() throws ClassNotFoundException, SQLException, InterruptedException {
		int maxCrawlerThreads = Runtime.getRuntime().availableProcessors();
//		int maxCrawlerThreads = 1;
		Thread[] scannerThreads = new Thread[maxCrawlerThreads];

		for (int i = 0; i < maxCrawlerThreads; i++) {
		    logger.info("starting thread " + i);
		    Thread theThread = new Thread(new Parser(i));
		    theThread.setPriority(Math.max(theThread.getPriority() - 2, Thread.MIN_PRIORITY));
		    theThread.start();
		    scannerThreads[i] = theThread;
		}

		for (int i = 0; i < maxCrawlerThreads; i++) {
		    scannerThreads[i].join();
		}
	}

	static Connection getConnection() throws ClassNotFoundException, SQLException {
		String use_ssl = prop_file.getProperty("nihdb.use.ssl", "false");
		logger.debug("Database SSL: " + use_ssl);

		String databaseHost = prop_file.getProperty("db.host", "localhost");
		logger.debug("Database Host: " + databaseHost);

		String databaseName = prop_file.getProperty("db.name", "loki");
		logger.debug("Database Name: " + databaseName);

		String db_url = prop_file.getProperty("nihdb.url", "jdbc:postgresql://" + databaseHost + "/" + databaseName);
		logger.debug("Database URL: " + db_url);

		Class.forName("org.postgresql.Driver");
		Properties props = new Properties();
		props.setProperty("user", "eichmann");
		props.setProperty("password", "translational");
		if (use_ssl.equals("true")) {
			props.setProperty("sslfactory", "org.postgresql.ssl.NonValidatingFactory");
			props.setProperty("ssl", "true");
		}
		Connection conn = DriverManager.getConnection(db_url, props);
		conn.setAutoCommit(false);

		return conn;

	}
	
	int threadID = 0;
	Connection conn = null;
	parser theParser = new SimpleStanfordParserBridge();
	int id = 0;
	int seqnum = 0;
	
	public Parser(int threadID) throws ClassNotFoundException, SQLException {
		this.threadID = threadID;
		conn = getConnection();
	}

	@Override
	public void run() {
		if (useTSpace) {
			try {
				Tuple theTuple = theTSpace.waitToTake("parse", new Field(Integer.class));
				while (theTuple != null) {
					int id = (Integer)theTuple.getField(1).getValue();
					try {
						fetchDocument(id);
					} catch (Exception e) {
						logger.error("error raised proccessing url:", e);
					}
					theTuple = theTSpace.waitToTake("parse", new Field(Integer.class));
				}
			} catch (TupleSpaceException e) {
				logger.error("tspace exception:" + e);
				return;
			}
		} else {
			while (!queue.isCompleted()) {
				Integer idString = queue.dequeue();
				if (idString == null) {
					logger.info("[" + threadID + "] done.");
					return;
				}
				id = idString;
				try {
					fetchDocument(id);
				} catch (Exception e) {
					logger.error("error raised proccessing url:", e);
				}
			}
		}
		logger.info("[" + threadID + "] done.");
	}
	
	void fetchDocument(int id) throws SQLException, IOException {
		logger.info("[" + threadID + "] " + id);
		this.id = id;
		seqnum = 0;
		PreparedStatement stmt = conn.prepareStatement("select html from gkg_local.document where id = ?");
		stmt.setInt(1, id);
		ResultSet rs = stmt.executeQuery();
		while (rs.next()) {
			String html = rs.getString(1);
			logger.debug(html);
			HTMLLexer theLexer = new HTMLLexer();
		    POStagger theTagger = new BrillTagger();
		    SentenceGenerator theSentGen = new basicSentenceGenerator();
			theLexer.addObserver(theTagger);
			theTagger.addObserver(theSentGen);
			theSentGen.addObserver(this);
			theLexer.process(html);
		}
		stmt.close();
		conn.commit();
	}

	public void update(Observable arg0, Object arg1) {
		Sentence theSentence = (Sentence) arg1;

		if (theSentence.tokenCount() == 0)
			return;
		
		String cleanText = theSentence.getCleanText();
		
		if (cleanText.length() > 1000)
			return;

		logger.info("[" + threadID + "] " + theSentence);
		
		try {
			PreparedStatement sentStmt = conn.prepareStatement("insert into gkg_local.sentence values(?,?,?,?)");
			sentStmt.setInt(1, id);
			sentStmt.setInt(2, ++seqnum);
			sentStmt.setString(3, cleanText);
			sentStmt.setString(4, theSentence.toString());
			sentStmt.execute();
			sentStmt.close();
			
			if (theSentence.theSentence.firstElement().toString().equals("</SYM")) {
				logger.info("[" + threadID + "] skipping");
				return;
			}
			
			int seqnum2 = 0;
			theParser.setParseCount(5);
			Vector<syntaxTree> theTrees = theParser.parsesAndAssemblies(theSentence);
			Enumeration<syntaxTree> parseEnum = theTrees.elements();
			while (parseEnum.hasMoreElements()) {
				syntaxTree aTree = parseEnum.nextElement();
				if (theParser instanceof linkGrammarParserBridge)
					((linkGrammarParserBridge) theParser).tagTree(theSentence.theSentence, aTree);
				logger.debug("[" + threadID + "] parse alternative: " + aTree.treeString());
				if (logger.isDebugEnabled())
					aTree.print();
				PreparedStatement parseStmt = conn.prepareStatement("insert into gkg_local.parse values(?,?,?,?)");
				parseStmt.setInt(1, id);
				parseStmt.setInt(2, seqnum);
				parseStmt.setInt(3, ++seqnum2);
				parseStmt.setString(4, aTree.treeString());
				parseStmt.execute();
				parseStmt.close();
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}