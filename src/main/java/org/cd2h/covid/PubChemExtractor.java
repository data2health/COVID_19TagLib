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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import edu.uiowa.PubChem.recognizer.Concept;
import edu.uiowa.PubChem.recognizer.ConceptRecognizer;

public class PubChemExtractor implements Runnable {
	static Logger logger = LogManager.getLogger(PubChemExtractor.class);
	static Vector<QueueEntry> queueVector = new Vector<QueueEntry>();
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
		Connection initialConn = getConnection();
		int maxCrawlerThreads = Runtime.getRuntime().availableProcessors();
		Thread[] matcherThreads = new Thread[maxCrawlerThreads];

		logger.info("populating queue...");
		PreparedStatement fetchStmt = initialConn.prepareStatement("select distinct doi,pmcid,pmid from covid_pubchem.process_queue order by doi,pmcid,pmid");
		ResultSet fetchRS = fetchStmt.executeQuery();
		while (fetchRS.next()) {
			queueVector.add(new QueueEntry(fetchRS.getString(1), fetchRS.getInt(2), fetchRS.getInt(3)));
		}
		fetchStmt.close();
		logger.info("done.");
		
		if (queueVector.size() == 0)
			return;
		
		for (int i = 0; i < maxCrawlerThreads; i++) {
			Thread theThread = new Thread(new PubChemExtractor(i));
			theThread.setPriority(Math.max(theThread.getPriority() - 2, Thread.MIN_PRIORITY));
			theThread.start();
			matcherThreads[i] = theThread;
		}
		for (int i = 0; i < maxCrawlerThreads; i++) {
			matcherThreads[i].join();
		}

		initialConn.close();
	}

	public synchronized QueueEntry dequeue() {
		if (queueVector.size() == 0)
			return null;
		else
			return queueVector.remove(0);
	}

	ConceptRecognizer conceptRecognizer = null;
	Connection conn = null;
	int threadID = 0;

	public PubChemExtractor(int threadID) throws ClassNotFoundException, SQLException {
		this.threadID = threadID;
		conn = getConnection();
		conceptRecognizer = new ConceptRecognizer(conn);
	}

	@Override
	public void run() {
		while (queueVector.size() > 0) {
			try {
				QueueEntry entry = dequeue();
				if (entry == null)
					return;
				index(entry);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	void index(QueueEntry entry) throws SQLException {
		logger.info("indexing " + entry);
		PreparedStatement fetchStmt = conn.prepareStatement("select seqnum,seqnum2,seqnum3,seqnum4,seqnum5,seqnum6,sentnum,sentence from covid.sentence_filter where doi = ? and pmcid = ? and pmid = ?");
		fetchStmt.setString(1, entry.doi);
		fetchStmt.setInt(2, entry.pmcid);
		fetchStmt.setInt(3, entry.pmid);
		ResultSet fetchRS = fetchStmt.executeQuery();
		while (fetchRS.next()) {
			entry.seqnum = fetchRS.getInt(1);
			entry.seqnum2 = fetchRS.getInt(2);
			entry.seqnum3 = fetchRS.getInt(3);
			entry.seqnum4 = fetchRS.getInt(4);
			entry.seqnum5 = fetchRS.getInt(5);
			entry.seqnum6 = fetchRS.getInt(6);
			entry.sentnum = fetchRS.getInt(7);
			String sentence = fetchRS.getString(8);

			logger.info("["+formatter.format(threadID)+"] preprint: " + entry);
			logger.info("["+formatter.format(threadID)+"]\tsentence: " + sentence);

			conceptRecognizer.parseSentences(sentence);

			cacheMatches(entry, conceptRecognizer.getLeftCompounds(), conceptRecognizer.getRightCompounds(), "sentence_compound_match");
			cacheMatches(entry, conceptRecognizer.getLeftGenes(), conceptRecognizer.getRightGenes(), "sentence_gene_match");
			cacheMatches(entry, conceptRecognizer.getLeftProteins(), conceptRecognizer.getRightProteins(), "sentence_protein_match");
			cacheMatches(entry, conceptRecognizer.getLeftSubstances(), conceptRecognizer.getRightSubstances(), "sentence_substance_match");
			conceptRecognizer.reset();
		}
		
		PreparedStatement cacheStmt = conn.prepareStatement("insert into covid_pubchem.processed values (?,?,?)");
		cacheStmt.setString(1, entry.doi);
		cacheStmt.setInt(2, entry.pmcid);
		cacheStmt.setInt(3, entry.pmid);
		cacheStmt.execute();

		conn.commit();
	}

	void cacheMatches(QueueEntry entry,  Vector<Concept> leftVector, Vector<Concept> rightVector, String tableName) throws SQLException {
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

		PreparedStatement cacheStmt = conn.prepareStatement("insert into covid_pubchem."+tableName+" values (?,?,?,?,?,?,?,?,?,?,?,?,?)");
		for (Concept concept : conceptMap.values()) {
			logger.info("\t\tstoring id: " + concept.getID() + " : " + concept.getCount() + " : " + concept.getPhrase() + " : " + tableName);
			cacheStmt.setString(1, entry.doi);
			cacheStmt.setInt(2, entry.pmcid);
			cacheStmt.setInt(3, entry.pmid);
			cacheStmt.setInt(4, entry.seqnum);
			cacheStmt.setInt(5, entry.seqnum2);
			cacheStmt.setInt(6, entry.seqnum3);
			cacheStmt.setInt(7, entry.seqnum4);
			cacheStmt.setInt(8, entry.seqnum5);
			cacheStmt.setInt(9, entry.seqnum6);
			cacheStmt.setInt(10, entry.sentnum);
			if (tableName.equals("sentence_protein_match"))
				cacheStmt.setString(11, concept.getID());
			else
				cacheStmt.setInt(11, Integer.parseInt(concept.getID()));
			cacheStmt.setString(12, concept.getPhrase());
			cacheStmt.setInt(13, concept.getCount());
			cacheStmt.execute();
		}

		cacheStmt.close();
	}

}
