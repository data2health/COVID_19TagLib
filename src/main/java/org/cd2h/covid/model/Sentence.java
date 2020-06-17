package org.cd2h.covid.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Hashtable;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.cd2h.covid.model.Reference.Style;

import pl.edu.icm.cermine.structure.model.BxChunk;
import pl.edu.icm.cermine.structure.model.BxWord;

public class Sentence {
    static Logger logger = Logger.getLogger(Sentence.class);
    static Pattern numberedCitationPattern = Pattern.compile("^([^\\[(]+)?\\[(([0-9]+([-–][0-9]+)?)(,([0-9]+([-–][0-9]+)?))*)[\\])]([..,:;?!])?");
    static Pattern numberedPrefixCitationPattern = Pattern.compile("^([^\\[(]+)?\\[(([0-9]+([-–][0-9]+)?)),");
    static Pattern numberedSuffixCitationPattern = Pattern.compile("^(([0-9]+([-–][0-9]+)?)*)[\\])]([..,:;?!])?");
    static Pattern nameYearPrefixCitationPattern = Pattern.compile("^\\(([a-zA-Z]+),?");
    static Pattern nameYearSuffixCitationPattern = Pattern.compile("^([^)]+)\\)([..,:;?!])?(.*)");

    Vector<BxWord> words = new Vector<BxWord>();
    StringBuffer trimmedString = new StringBuffer();
    Vector<Citation> citations = new Vector<Citation>();
    Vector<String> figureMentions = new Vector<String>();
    Vector<String> tableMentions = new Vector<String>();
    
    public Sentence(BxWord word) {
	words.add(word);
    }
    
    public void addWord(BxWord word) {
	words.add(word);
    }
    
    public void citationScan(Vector<Reference> references, Style style) {
	switch (style) {
	case NUMBERED:
	case BRACKETED:
	case PARENTHESIZED:
	    numberedScan(references);
	    break;
	case NAME_YEAR:
	    nameYearScan(references);
	    break;
	case UNKNOWN:
	    break;
	}
    }
    
    static Pattern figurePattern = Pattern.compile("^(.*) +(\\([^)]*Fig(?:\\.|ure)[^)]*\\))(.*)$");
    static Pattern tablePattern = Pattern.compile("^(.*) +(\\([^)]*Table[^)]*\\))(.*)$");
    
    public void strip() {
	strip(figureMentions, figurePattern);
	strip(tableMentions, tablePattern);
    }
    
    public void strip(Vector<String> mentions, Pattern pattern) {
	Matcher matcher = pattern.matcher(trimmedString);
	while (matcher.matches()) {
	    logger.info("\tstrip pattern match: " + trimmedString);
	    for (int j = 1; j <= matcher.groupCount(); j++) {
		logger.info("\t\tmatch " + j + ": " + matcher.group(j));
	    }
	    trimmedString = new StringBuffer(matcher.group(1) + matcher.group(3));
	    mentions.add(matcher.group(2));
	    matcher = pattern.matcher(trimmedString);
	}	
    }
    
    static Pattern parentStartPattern = Pattern.compile("^(.*)\\(([0-9]+([-–][0-9]+)?)((,[0-9]+([-–][0-9]+)?)*)(,)?(\\))?(.*)$");
    static Pattern parentEndPattern =          Pattern.compile("^([0-9]+([-–][0-9]+)?)(,)?(\\))?(.*)$");

    void parenthesizedScan(Vector<Reference> references) {
	String matchString = null;
	logger.info("parenthesized citation scan: " + toString(words));
	trimmedString = new StringBuffer();
	int state = 0;
	for (int i = 0; i < words.size(); i++) {
	    BxWord word = words.elementAt(i);
	    switch (state) {
	    case 0:
		// looking for the start (and perhaps end) of a parenthesized reference
		Matcher matcher = parentStartPattern.matcher(word.toText());
		if (matcher.matches()) {
		    logger.info("\tstart pattern match: " + word.toText());
		    for (int j = 0; j <= 9; j++) {
			logger.info("\t\tmatch " + j + ": " + matcher.group(j));
		    }
		    if (matcher.group(8) != null && matcher.group(8).equals(")")) {
			// single item or range	
			if (numberedCitationMatcher(references, matcher.group(2)+(matcher.group(4) == null ? "" : matcher.group(4)))) {
			    trimmedString.append((matcher.group(1).length() > 0 ? " " : "") + matcher.group(1));
			    trimmedString.append(matcher.group(9));
			} else {
			    trimmedString.append((trimmedString.length() == 0 ? "" : " ") + word.toText());
			}
		    } else if (matcher.group(7) != null && matcher.group(7).equals(",")) {
			logger.info("shifting to state 1");
			state = 1;
			matchString = matcher.group(2) + ",";
			trimmedString.append((matcher.group(1).length() > 0 ? " " : "") + matcher.group(1));
		    } else {
			trimmedString.append((trimmedString.length() == 0 ? "" : " ") + word.toText());
		    }
		} else {
		    trimmedString.append((trimmedString.length() == 0 ? "" : " ") + word.toText());
		}
		break;
	    case 1:
		// looking for the continuation (and perhaps end) of a parenthesized reference
		matcher = parentEndPattern.matcher(word.toText());
		if (matcher.matches()) {
		    logger.info("\tend pattern match: " + word.toText());
		    for (int j = 0; j <= 5; j++) {
			logger.info("\t\tmatch " + j + ": " + matcher.group(j));
		    }
		    matchString += matcher.group(1);
		    if (numberedCitationMatcher(references, matchString)) {
			matchString = "";
		    }
		    trimmedString.append(matcher.group(5));
		    if (matcher.group(3) == null)
			state = 0;
		}
		break;
	    }
	}
	logger.info("\ttrimmed: " + trimmedString.toString());
    }
    
    void superscriptScan(Vector<Reference> references) {
	logger.info("superscript citation scan: " + toString(words));
	trimmedString = new StringBuffer();

	for (int i = 0; i < words.size(); i++) {
	    BxWord word = words.elementAt(i);
	    if (word.getChild(word.childrenCount()-1).getHeight() / word.getHeight() < 0.8 && Math.abs(word.getY() - word.getChild(word.childrenCount()-1).getY()) < 1.0) {
		logger.info("candidate superscript: " + word.toText() + "\theight: " + word.getHeight() + "\ty: " + word.getY());
		StringBuffer citationString = new StringBuffer();
		for (int j = word.childrenCount() - 1; j >= 0; j--) {
		    BxChunk chunk = word.getChild(j);
		    if (chunk.getHeight() / word.getHeight() > 0.8 || Math.abs(word.getY() - chunk.getY()) > 1.0)
			break;
		    logger.info("\tsuperscript chunk: " + chunk.toText() + "\theight: " + chunk.getHeight() + "\ty: " + chunk.getY());
		    citationString.insert(0, chunk.toText());
		}
		if (numberedCitationMatcher(references,citationString.toString())) {
		    trimmedString.append((trimmedString.length() == 0 ? "" : " ") + word.toText().substring(0,word.toText().length()-citationString.length()));
		} else {
		    trimmedString.append((trimmedString.length() == 0 ? "" : " ") + word.toText());
		}
	    } else {
		trimmedString.append((trimmedString.length() == 0 ? "" : " ") + word.toText());
	    }
	}
	logger.info("\ttrimmed: " + trimmedString.toString());
    }
    
    void numberedScan(Vector<Reference> references) {
	logger.info("numbered citation scan: " + toString(words));
	for (int i = 0; i < words.size(); i++) {
	    BxWord word = words.elementAt(i);
	    Matcher matcher = numberedCitationPattern.matcher(word.toText());
	    if (matcher.matches()) {
		logger.info("\tpattern match: " + word.toText());
		String prefix = matcher.group(1);
		String citation = matcher.group(2);
		String suffix = matcher.group(8);
		logger.info("\t\tprefix: " + prefix);
		logger.info("\t\tcitation: " + citation);
		logger.info("\t\tsuffix: " + suffix);
		if (!numberedCitationMatcher(references, citation)) {
		    logger.info("\t\tnon-match: " + citation);
		    trimmedString.append((trimmedString.length() == 0 ? "" : " ") + "(" + citation + ")");
		}
		if (prefix != null)
		    trimmedString.append((trimmedString.length() == 0 ? "" : " ") + prefix);
		if (suffix != null)
		    trimmedString.append(suffix);
	    } else {
		Matcher prefixMatcher = numberedPrefixCitationPattern.matcher(word.toText());
		if (prefixMatcher.matches()) {
		    logger.info("\tprefix pattern match: " + word.toText());
		    String prefix = prefixMatcher.group(1);
		    String citation = prefixMatcher.group(2);
		    logger.info("\t\tprefix: " + prefix);
		    logger.info("\t\tcitation: " + citation);
		    if (prefix != null)
			trimmedString.append((trimmedString.length() == 0 ? "" : " ") + prefix);
		    word = words.elementAt(++i);
		    boolean continuation = false;
		    Matcher suffixMatcher = numberedSuffixCitationPattern.matcher(word.toText());
		    while (!suffixMatcher.matches()) {
			logger.info("\t\tcontinuation: " + word.toText());
			citation += "," + word.toText();
			if (i + 1 == words.size()) {
			    trimmedString.append((trimmedString.length() == 0 ? "" : " ") + citation);
			    logger.info("\ttrimmed: " + trimmedString.toString());
			    return;
			}
			word = words.elementAt(++i);
			suffixMatcher = numberedSuffixCitationPattern.matcher(word.toText());
			continuation = true;
		    }
		    citation += (continuation ? "" : ",") + suffixMatcher.group(1);
		    String suffix = suffixMatcher.group(4);
		    logger.info("\t\tcitation: " + citation);
		    logger.info("\t\tsuffix: " + suffix);
		    if (!numberedCitationMatcher(references, citation)) {
			logger.info("\t\tnon-match: " + citation);
			trimmedString.append((trimmedString.length() == 0 ? "" : " ") + "(" + citation + ")");
		    }
		    if (suffix != null)
			trimmedString.append(suffix);
		} else {
		    trimmedString.append((trimmedString.length() == 0 ? "" : " ") + word.toText());
		}
	    }
	}
	logger.info("\ttrimmed: " + trimmedString.toString());
    }
    
    static Pattern numberedExtractionPattern = Pattern.compile("^([0-9]+)([-–]([0-9]+))?([,;](.*))?$");

    boolean numberedCitationMatcher(Vector<Reference> references, String citationString) {
	boolean matched = false;
	String buffer = citationString;
	while (buffer != null) {
	    Matcher matcher = numberedExtractionPattern.matcher(buffer);
	    if (matcher.matches()) {
		String start = matcher.group(1);
		String stop = matcher.group(3);
		String suffix = matcher.group(5);
		logger.info("\t\tmatch: " + start + "\tstop: " + stop);
		logger.info("\t\tsuffix: " + suffix);
		matched = true;
		for (int i = Integer.parseInt(start); i <= (stop == null ? Integer.parseInt(start) : Integer.parseInt(stop)); i++) {
		    Reference reference = numberedReferenceScan(references, i);
		    logger.info("\t\treference: " + reference);
		    Citation citation = new Citation(this, reference, citationString);
		    citations.add(citation);
		    if (reference != null)
			reference.addCitation(citation);
		}
		buffer = suffix;
	    } else
		break;
	}
	return matched;
    }
    
    Reference numberedReferenceScan(Vector<Reference> references, int seqnum) {
	for (Reference reference : references) {
	    logger.debug("\t\t\tcandidate: " + reference);
	    if (reference.seqNum == seqnum)
		return reference;
	}
	return null;
    }
    
    void nameYearScan(Vector<Reference> references) {
	logger.info("name-year citation scan: " + toString(words));
	for (int i = 0; i < words.size(); i++) {
	    BxWord word = words.elementAt(i);
	    Matcher matcher = nameYearPrefixCitationPattern.matcher(word.toText());
	    if (matcher.matches()) {
		String citation = matcher.group(1);
		logger.info("\tname prefix match: " + citation);
		word = words.elementAt(++i);
		boolean continuation = false;
		Matcher suffixMatcher = nameYearSuffixCitationPattern.matcher(word.toText());
		while (!suffixMatcher.matches()) {
		    logger.info("\t\tcontinuation: " + word.toText());
		    citation += " " + word.toText();
		    if (i < words.size()) {
			continuation = false;
		    } else
			continuation = true;
		    if (i+1 == words.size()) {
			trimmedString.append((trimmedString.length() == 0 ? "" : " ") + citation);
			logger.info("\ttrimmed: " + trimmedString.toString());
			return;
		    }
		    word = words.elementAt(++i);
		    suffixMatcher = nameYearSuffixCitationPattern.matcher(word.toText());
		}
//		if (!continuation) {
//		    trimmedString.append(citation);
//		    continue;
//		}
		citation += " " + suffixMatcher.group(1);
		String suffix = suffixMatcher.group(2);
		logger.info("\t\tcitation: " + citation);
		logger.info("\t\tsuffix: " + suffix);
		if (!nameYearCitationMatcher(references, citation)) {
		    logger.info("\t\tnon-match: " + citation);
		    trimmedString.append((trimmedString.length() == 0 ? "" : " ") + "(" + citation + ")");		    
		}
		if (suffix != null)
		    trimmedString.append(suffix);
	    } else {
		trimmedString.append((trimmedString.length() == 0 ? "" : " ") + word.toText());
	    }
	}
	logger.info("\ttrimmed: " + trimmedString.toString());
    }
    
    static Pattern nameYearExtractionPattern = Pattern.compile("^([a-zA-Z]+)[^;]* +([0-9]+([a-z])?)(; +(.*))?$");

    boolean nameYearCitationMatcher(Vector<Reference> references, String citationString) {
	boolean matched = false;
	String buffer = citationString;
	while (buffer != null) {
	    Matcher matcher = nameYearExtractionPattern.matcher(buffer);
	    if (matcher.matches()) {
		String first = matcher.group(1);
		String year = matcher.group(2);
		String suffix = matcher.group(5);
		logger.info("\t\tmatch: " + first + "\t" + year);
		logger.info("\t\tsuffix: " + suffix);
		matched = true;
		Reference reference = nameYearReferenceScan(references, first, year);
		logger.info("\t\treference: " + reference);
		Citation citation = new Citation(this, reference, citationString);
		citations.add(citation);
		if (reference != null)
		    reference.addCitation(citation);
		buffer = suffix;
	    } else
		break;
	}
	return matched;
    }
    
    Reference nameYearReferenceScan(Vector<Reference> references, String name, String year) {
	for (Reference reference : references) {
	    logger.debug("\t\t\tcandidate: " + reference);
	    if (reference.name.startsWith(name) && reference.year != null && reference.year.equals(year))
		return reference;
	}
	return null;
    }
    
    public String toString() {
	if (trimmedString.length() == 0)
	    return toString(words);
	else
	    return trimmedString.toString();
    }
    
    public String toString(Vector<BxWord> elements) {
	StringBuffer buffer = new StringBuffer();
	for (BxWord word : elements) {
	    buffer.append((buffer.length() == 0 ? "" : " ") + word.toText());
	}
	return buffer.toString();
    }
    
    public void store(Connection conn, String doi, int seqnum, int sentnum) throws SQLException {
	PreparedStatement stmt = conn.prepareStatement("insert into covid_biorxiv.sentence values(?,?,?,?,?)");
	stmt.setString(1, doi);
	stmt.setInt(2, seqnum);
	stmt.setInt(3, sentnum);
	stmt.setString(4, toString());
	stmt.setString(5, trimmedString.toString());
	stmt.execute();
	stmt.close();
	
	Hashtable<Integer,Integer> refHash = new Hashtable<Integer,Integer>();
	for (Citation citation : citations) {
	    if (citation.citedReference == null || refHash.containsKey(citation.citedReference.seqNum))
		continue;
	    refHash.put(citation.citedReference.seqNum, 0);
	    
	    PreparedStatement citeStmt = conn.prepareStatement("insert into covid_biorxiv.citation values(?,?,?,?)");
	    citeStmt.setString(1, doi);
	    citeStmt.setInt(2, seqnum);
	    citeStmt.setInt(3, sentnum);
	    citeStmt.setInt(4, citation.citedReference.seqNum);
	    citeStmt.execute();
	    citeStmt.close();
	}

	Hashtable<String,Integer> tableHash = new Hashtable<String,Integer>();
	for (String table : tableMentions) {
	    if (tableHash.containsKey(table))
		continue;
	    tableHash.put(table, 0);
	    
	    PreparedStatement citeStmt = conn.prepareStatement("insert into covid_biorxiv.table_mention values(?,?,?,?)");
	    citeStmt.setString(1, doi);
	    citeStmt.setInt(2, seqnum);
	    citeStmt.setInt(3, sentnum);
	    citeStmt.setString(4, table);
	    citeStmt.execute();
	    citeStmt.close();
	}

	Hashtable<String,Integer> figureHash = new Hashtable<String,Integer>();
	for (String figure : figureMentions) {
	    if (figureHash.containsKey(figure))
		continue;
	    figureHash.put(figure, 0);
	    
	    PreparedStatement citeStmt = conn.prepareStatement("insert into covid_biorxiv.figure_mention values(?,?,?,?)");
	    citeStmt.setString(1, doi);
	    citeStmt.setInt(2, seqnum);
	    citeStmt.setInt(3, sentnum);
	    citeStmt.setString(4, figure);
	    citeStmt.execute();
	    citeStmt.close();
	}
    }
}
