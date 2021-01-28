package org.cd2h.covid;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import edu.uiowa.PubChem.recognizer.Concept;
import edu.uiowa.PubChem.recognizer.ConceptRecognizer;

public class BioRxivPubChemExtractor implements Runnable {
	static Logger logger = Logger.getLogger(BioRxivPubChemExtractor.class);
	static Vector<String> doiVector = new Vector<String>();
    static DecimalFormat formatter = new DecimalFormat("00");

	static Connection getConnection() throws ClassNotFoundException, SQLException {
		Connection conn = null;
		Properties prop_file = PropertyLoader.loadProperties("loader");

		String use_ssl = prop_file.getProperty("nihdb.use.ssl", "false");
		logger.debug("Database SSL: " + use_ssl);

		String databaseHost = prop_file.getProperty("db.host");
		logger.debug("Database Host: " + databaseHost);

		String databaseName = prop_file.getProperty("db.name");
		logger.debug("Database Name: " + databaseName);

		String db_url = prop_file.getProperty("db.url");
		logger.debug("Database URL: " + db_url);

		Class.forName("org.postgresql.Driver");
		Properties props = new Properties();
		props.setProperty("user", prop_file.getProperty("db.user.name"));
		props.setProperty("password", prop_file.getProperty("db.user.password"));
		if (use_ssl.equals("true")) {
			props.setProperty("sslfactory", "org.postgresql.ssl.NonValidatingFactory");
			props.setProperty("ssl", "true");
		}
		conn = DriverManager.getConnection(db_url, props);
		conn.setAutoCommit(false);

		return conn;
	}

	public static void main(String[] args) throws ClassNotFoundException, SQLException, InterruptedException {
		PropertyConfigurator.configure(args[0]);
		Connection initialConn = getConnection();
		int maxCrawlerThreads = Runtime.getRuntime().availableProcessors();
		Thread[] matcherThreads = new Thread[maxCrawlerThreads];

		logger.info("populating queue...");
		PreparedStatement fetchStmt = initialConn.prepareStatement("select distinct doi from covid_biorxiv.sentence where not exists (select doi from covid_biorxiv.pubchem_processed where pubchem_processed.doi=sentence.doi) order by doi desc");
		ResultSet fetchRS = fetchStmt.executeQuery();
		while (fetchRS.next()) {
			doiVector.add(fetchRS.getString(1));
		}
		fetchStmt.close();
		logger.info("done.");
		
		if (doiVector.size() == 0)
			return;
		
		for (int i = 0; i < maxCrawlerThreads; i++) {
			Thread theThread = new Thread(new BioRxivPubChemExtractor(i));
			theThread.setPriority(Math.max(theThread.getPriority() - 2, Thread.MIN_PRIORITY));
			theThread.start();
			matcherThreads[i] = theThread;
		}
		for (int i = 0; i < maxCrawlerThreads; i++) {
			matcherThreads[i].join();
		}

		initialConn.close();
	}

	public synchronized String dequeue() {
		if (doiVector.size() == 0)
			return null;
		else
			return doiVector.remove(0);
	}

	ConceptRecognizer conceptRecognizer = null;
	Connection conn = null;
	int threadID = 0;

	public BioRxivPubChemExtractor(int threadID) throws ClassNotFoundException, SQLException {
		this.threadID = threadID;
		conn = getConnection();
		conceptRecognizer = new ConceptRecognizer(conn);
	}

	@Override
	public void run() {
		while (doiVector.size() > 0) {
			try {
				String  doi = dequeue();
				if (doi == null)
					return;
				index(doi);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	void index(String doi) throws SQLException {
		logger.info("indexing " + doi);
		PreparedStatement fetchStmt = conn.prepareStatement("select seqnum,sentnum,full_text from covid_biorxiv.sentence where doi = ?");
		fetchStmt.setString(1, doi);
		ResultSet fetchRS = fetchStmt.executeQuery();
		while (fetchRS.next()) {
			int seqnum = fetchRS.getInt(1);
			int sentnum = fetchRS.getInt(2);
			String sentence = fetchRS.getString(3);

			logger.info("["+formatter.format(threadID)+"] preprint: " + doi);
			logger.info("["+formatter.format(threadID)+"]\tseqnum: " + seqnum +  "\tsentnum: " + sentnum + "\tsentence: " + sentence);

			conceptRecognizer.parseSentences(sentence);

			cacheMatches(doi,seqnum,sentnum, conceptRecognizer.getLeftCompounds(), conceptRecognizer.getRightCompounds(), "pubchem_sentence_compound");
			cacheMatches(doi,seqnum,sentnum, conceptRecognizer.getLeftGenes(), conceptRecognizer.getRightGenes(), "pubchem_sentence_gene");
			cacheMatches(doi,seqnum,sentnum, conceptRecognizer.getLeftProteins(), conceptRecognizer.getRightProteins(), "pubchem_sentence_protein");
			cacheMatches(doi,seqnum,sentnum, conceptRecognizer.getLeftSubstances(), conceptRecognizer.getRightSubstances(), "pubchem_sentence_substance");
			conceptRecognizer.reset();
		}
		
		PreparedStatement cacheStmt = conn.prepareStatement("insert into covid_biorxiv.pubchem_processed values (?)");
		cacheStmt.setString(1, doi);
		cacheStmt.execute();

		conn.commit();
	}

	void cacheMatches(String doi, int seqnum, int sentnum,  Vector<Concept> leftVector, Vector<Concept> rightVector, String tableName) throws SQLException {
		Map<String, Concept> conceptMap = new HashMap<String, Concept>();

		for (Concept concept : leftVector) {
			Concept targetConcept = conceptMap.get(concept.getID() + " " + concept.getPhrase());
			if (targetConcept == null) {
				conceptMap.put(concept.getID() + " " + concept.getPhrase(), concept);
			} else {
				targetConcept.incrementCount();
			}
		}

		for (Concept concept : rightVector) {
			Concept targetConcept = conceptMap.get(concept.getID() + " " + concept.getPhrase());
			if (targetConcept == null) {
				conceptMap.put(concept.getID() + " " + concept.getPhrase(), concept);
			} else {
				targetConcept.incrementCount();
			}
		}

		PreparedStatement cacheStmt = conn.prepareStatement("insert into covid_biorxiv."+tableName+" values (?,?,?,?,?,?)");
		for (Concept concept : conceptMap.values()) {
			logger.debug("\t\tstoring cui: " + concept.getID() + " : " + concept.getCount() + " : " + concept.getPhrase());
			cacheStmt.setString(1, doi);
			cacheStmt.setInt(2, seqnum);
			cacheStmt.setInt(3, sentnum);
			cacheStmt.setString(4, concept.getID());
			cacheStmt.setString(5, concept.getPhrase());
			cacheStmt.setInt(6, concept.getCount());
			cacheStmt.execute();
		}

//		if (conceptMap.size() == 0) {
//			// insert dummy to prevent subsequent scans
//			cacheStmt.setString(1, doi);
//			cacheStmt.setInt(2, seqnum);
//			cacheStmt.setInt(3, sentnum);
//			cacheStmt.setString(4, "");
//			cacheStmt.setString(5, "");
//			cacheStmt.setInt(6, 0);
//			cacheStmt.execute();
//		}
		cacheStmt.close();
	}

}
