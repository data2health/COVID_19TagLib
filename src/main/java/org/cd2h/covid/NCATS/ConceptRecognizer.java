package org.cd2h.covid.NCATS;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Hashtable;
import java.util.Observable;
import java.util.Observer;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Vector;

import org.apache.log4j.Logger;

import edu.uiowa.lex.Sentence;
import edu.uiowa.lex.SentenceGenerator;
import edu.uiowa.lex.basicLexerToken;
import edu.uiowa.lex.biomedicalLexer;
import edu.uiowa.lex.conceptSentenceGenerator;
import edu.uiowa.lex.lexer;
import edu.uiowa.pos_tagging.BrillTagger;
import edu.uiowa.pos_tagging.POStagger;
import edu.uiowa.pos_tagging.extensibleNounPhraseGenerator;

public class ConceptRecognizer implements Observer {
	public enum Direction { LEFT, RIGHT, BOTH };
	
	boolean use_ssl = false;
	static Logger logger = Logger.getLogger(ConceptRecognizer.class);
	static final boolean parseNPs = false;

	Direction theDirection = Direction.LEFT;
	Connection conn = null;
	Vector<Concept> leftDrugs = new Vector<Concept>();
	Vector<Concept> rightDrugs = new Vector<Concept>();
	static boolean cachesLoaded = false;
	static Hashtable<String, Vector<Integer>> drugCache = new Hashtable<String, Vector<Integer>>();

	public ConceptRecognizer() throws ClassNotFoundException, SQLException {
		Properties props = new Properties();
		props.setProperty("user", "eichmann");
		props.setProperty("password", "translational");

		if (use_ssl) {
			props.setProperty("sslfactory", "org.postgresql.ssl.NonValidatingFactory");
			props.setProperty("ssl", "true");
		}

		Class.forName("org.postgresql.Driver");
		conn = DriverManager.getConnection("jdbc:postgresql://localhost/loki", props);

		// get the tagger cache loaded while everyone else is blocked
		BrillTagger.initialize(true);

		loadCaches(conn, drugCache);
		cachesLoaded = true;
	}

	public ConceptRecognizer(Connection conn) {
		this.conn = conn;

		// get the tagger cache loaded while everyone else is blocked
		BrillTagger.initialize(true);

		logger.info("loading caches...");

		loadCaches(conn, drugCache);
	}

	static synchronized void loadCaches(Connection conn,  Hashtable<String, Vector<Integer>> cache) {
		int count =  0;
		if (cachesLoaded)
			return;

		PreparedStatement stmt;
		try {
			stmt = conn.prepareStatement("select id,lower(medication) from covid_ncats.medication");
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				int id = rs.getInt(1);
				String name = rs.getString(2);

				Vector<Integer> cacheVector = cache.get(name);

				if (cacheVector == null) {
					cacheVector = new Vector<Integer>();
					cacheVector.add(id);
					cache.put(name, cacheVector);
				} else {
					cacheVector.add(id);
				}

				count++;
			}
			stmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		logger.info("\tcache: " + count);
	}

	public void reset() {
		leftDrugs = new Vector<Concept>();
	}

	public void parse(String buffer) {
		if (parseNPs)
			parseNPs(buffer);
		else
			parseSentence(buffer);
	}

	void parseNPs(String buffer) {
		lexer theLexer = new biomedicalLexer();
		extensibleNounPhraseGenerator theNPGen = new extensibleNounPhraseGenerator();
		theNPGen.addTag(basicLexerToken.JJ);
		theNPGen.addTag(basicLexerToken.JJR);
		theNPGen.addTag(basicLexerToken.JJS);
		theNPGen.addTag(basicLexerToken.RB);
		theNPGen.addTag(basicLexerToken.DT);
		theNPGen.addTag(basicLexerToken.IN);
		theNPGen.addTag(basicLexerToken.VBN);
		POStagger theTagger = null;

		BrillTagger.initialize(true);
		theTagger = new BrillTagger();
		theTagger.reset();
		theLexer.addObserver(theTagger);
		theTagger.addObserver(theNPGen);
		theTagger.addObserver(this);
		theNPGen.addObserver(this);
		try {
			theLexer.process(buffer);
		} catch (IOException e) {
			logger.error("Exception: " + e);
		}
	}

	public String analyzeSentenceAndClose(String buffer) {
		String result = analyzeSentence(buffer);
		try {
			conn.close();
		} catch (SQLException e) {
			logger.error("Exception: ", e);
		}
		return result;
	}

	public String analyzeSentence(String buffer) {
		StringBuffer resultBuffer = new StringBuffer();
		Vector<basicLexerToken> theVector = stringToVector(buffer);
		reducingLeftConceptScan(theVector, drugCache, leftDrugs);
		return resultBuffer.toString().trim();
	}

	public void parseSentence(String buffer) {
		if (buffer == null)
			return;

		switch (theDirection) {
		case LEFT:
			leftConceptScan(stringToVector(buffer), drugCache, leftDrugs);
			break;
		case RIGHT:
			rightConceptScan(stringToVector(buffer), drugCache, rightDrugs);
			break;
		case BOTH:
			leftConceptScan(stringToVector(buffer), drugCache, leftDrugs);
			rightConceptScan(stringToVector(buffer), drugCache, rightDrugs);
			break;
		}
	}

	public void parseSentences(String buffer) {
		if (buffer == null)
			return;

		lexer theLexer = new biomedicalLexer();
		SentenceGenerator theSentGen = new conceptSentenceGenerator();
		BrillTagger theTagger = new BrillTagger();

		BrillTagger.initialize(false);
		theTagger = new BrillTagger();
		theTagger.reset();
		theLexer.addObserver(theTagger);
		theTagger.addObserver(theSentGen);
		theSentGen.addObserver(this);
		try {
			theLexer.process(buffer);
		} catch (IOException e) {
			logger.error("Exception: " + e);
		}
	}

	public Vector<Concept> getLeftCompounds() {
		return leftDrugs;
	}

	public void setLeftCompounds(Vector<Concept> leftCompounds) {
		this.leftDrugs = leftCompounds;
	}

	public Vector<Concept> getRightCompounds() {
		return rightDrugs;
	}

	public void setRightCompounds(Vector<Concept> rightCompounds) {
		this.rightDrugs = rightCompounds;
	}

	public void update(Observable o, Object t) {
		if (t instanceof basicLexerToken) {
			basicLexerToken token = (basicLexerToken) t;
			logger.trace("Token: " + token.text + "/" + token.tag);
		} else if (t instanceof Sentence) {
			String phrase = vectorToString(((Sentence) t).theSentence, 0, ((Sentence) t).theSentence.size() - 1, false);
			logger.debug("\tsentence: " + phrase);
			switch (theDirection) {
			case LEFT:
				leftConceptScan(((Sentence) t).theSentence, drugCache, leftDrugs);
				break;
			case RIGHT:
				rightConceptScan(((Sentence) t).theSentence, drugCache, rightDrugs);
				break;
			case BOTH:
				leftConceptScan(((Sentence) t).theSentence, drugCache, leftDrugs);
				rightConceptScan(((Sentence) t).theSentence, drugCache, rightDrugs);
				break;
			}
		} else if (t instanceof Vector) {
			String phrase = vectorToString((Vector) t, 0, ((Vector) t).size() - 1, false);
			logger.debug("\tphrase: " + phrase);
			switch (theDirection) {
			case LEFT:
				leftConceptScan((Vector) t, drugCache, leftDrugs);
				break;
			case RIGHT:
				rightConceptScan((Vector) t, drugCache, rightDrugs);
				break;
			case BOTH:
				leftConceptScan((Vector) t, drugCache, leftDrugs);
				rightConceptScan((Vector) t, drugCache, rightDrugs);
				break;
			}
		}
	}

	void reducingLeftConceptScan(Vector theVector, Hashtable<String, Vector<Integer>>  cache, Vector<Concept> cuiVector) {
		int id = 0;
		for (int i = 0; i < theVector.size(); i++) {
			for (int j = theVector.size() - 1; j >= i && id == 0; j--) {
				String subphrase = vectorToString(theVector, i, j, true).toLowerCase();
				logger.debug("\t\tleft subphrase (" + i + "," + j + "): " + subphrase);

				if (i == j && (((basicLexerToken) theVector.elementAt(i)).POS_tag == basicLexerToken.DT
						|| ((basicLexerToken) theVector.elementAt(i)).POS_tag == basicLexerToken.IN))
					continue;

				id = probe(subphrase, cache, cuiVector);

				if (id != 0) {
					logger.trace("i: " + i + "\tj: " + j + "\tvector: " + theVector);
					for (int k = i; k <= j; k++)
						theVector.removeElementAt(i);
					break;
				}
			}
			id = 0;
		}
	}

	void leftConceptScan(Vector theVector, Hashtable<String, Vector<Integer>> cache, Vector<Concept> cuiVector) {
		int id = 0;
		for (int i = 0; i < theVector.size(); i++) {
			for (int j = theVector.size() - 1; j >= i && id == 0; j--) {
				String subphrase = vectorToString(theVector, i, j, true).toLowerCase();
				logger.debug("\t\tleft subphrase (" + i + "," + j + "): " + subphrase);

				if (i == j && (((basicLexerToken) theVector.elementAt(i)).POS_tag == basicLexerToken.DT
						|| ((basicLexerToken) theVector.elementAt(i)).POS_tag == basicLexerToken.IN))
					continue;

				id = probe(subphrase, cache, cuiVector);

				if (id != 0) {
					i = j;
					break;
				}
			}
			id = 0;
		}
	}

	void rightConceptScan(Vector theVector, Hashtable<String, Vector<Integer>> cache, Vector<Concept> idVector) {
		int id = 0;
		for (int i = theVector.size() - 1; i >= 0; i--) {
			for (int j = 0; j <= i && id == 0; j++) {
				String subphrase = vectorToString(theVector, j, i, true).toLowerCase();
				logger.debug("\t\tright subphrase (" + j + "," + i + "): " + subphrase);

				if (i == j && (((basicLexerToken) theVector.elementAt(i)).POS_tag == basicLexerToken.DT
						|| ((basicLexerToken) theVector.elementAt(i)).POS_tag == basicLexerToken.IN))
					continue;

				id = probe(subphrase, cache, idVector);

				if (id != 0) {
					i = j;
					break;
				}
			}
			id = 0;
		}
	}

	int probe(String subphrase, Hashtable<String, Vector<Integer>> cache, Vector<Concept> cuiVector) {
		int id = 0;
		Vector<Integer> cacheVector = cache.get(subphrase);

		if (cacheVector == null)
			return 0;

		for (int cachedID : cacheVector) {
			id = cachedID;
			logger.trace("\t\t\t\tcached id: " + id + "\tphrase: " + subphrase);
			cuiVector.add(new Concept(subphrase, id));
		}

		return id;
	}

	Vector<basicLexerToken> stringToVector(String buffer) {
		Vector<basicLexerToken> result = new Vector<basicLexerToken>();
		StringTokenizer theTokenizer = new StringTokenizer(buffer);

		while (theTokenizer.hasMoreTokens())
			result.add(new basicLexerToken(theTokenizer.nextToken()));

		return result;
	}

	String vectorToString(Vector theVector, int start, int end, boolean smartSpacing) {
		StringBuffer temp = new StringBuffer();
		boolean quoteSeen = false;
		for (int i = start; i <= end; i++) {
			String token = null;
			if (theVector.elementAt(i) instanceof String)
				token = (String) theVector.elementAt(i);
			else if (theVector.elementAt(i) instanceof basicLexerToken)
				token = ((basicLexerToken) theVector.elementAt(i)).text;
			if (!smartSpacing)
				temp.append(token + " ");
			else {
				if (!quoteSeen && token.equals("\"") && (i == start || (i > start
						&& !SentenceGenerator.isPunctuation(((basicLexerToken) theVector.elementAt(i - 1)).text)))) {
					temp.append(token);
					quoteSeen = true;
				} else if (i < end && ((((basicLexerToken) theVector.elementAt(i + 1)).text).equals("'s")
						|| SentenceGenerator.isNonParenPunctuation(((basicLexerToken) theVector.elementAt(i + 1)).text)
						|| (quoteSeen
								&& SentenceGenerator.isQuote(((basicLexerToken) theVector.elementAt(i + 1)).text)))) {
					temp.append(token);
					quoteSeen = true;
				} else
					temp.append(token + " ");
			}
		}
		return temp.toString().trim();
	}
}
