package org.cd2h.covid.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.cd2h.covid.model.Reference.Style;

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
    
    public Sentence(BxWord word) {
	words.add(word);
    }
    
    public void addWord(BxWord word) {
	words.add(word);
    }
    
    public void citationScan(Style style) {
	switch (style) {
	case NUMBERED:
	case BRACKETED:
	case PARENTHESIZED:
	    numberedScan();
	    break;
	case NAME_YEAR:
	    nameYearScan();
	    break;
	case UNKNOWN:
	    break;
	}
    }
    
    void numberedScan() {
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
			word = words.elementAt(++i);
			suffixMatcher = numberedSuffixCitationPattern.matcher(word.toText());
			continuation = true;
		    }
		    citation += (continuation ? "" : ",") + suffixMatcher.group(1);
		    String suffix = suffixMatcher.group(4);
		    logger.info("\t\tcitation: " + citation);
		    logger.info("\t\tsuffix: " + suffix);
		    if (suffix != null)
			trimmedString.append(suffix);
		} else {
		    trimmedString.append((trimmedString.length() == 0 ? "" : " ") + word.toText());
		}
	    }
	}
	logger.info("\ttrimmed: " + trimmedString.toString());
    }
    
    void nameYearScan() {
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
		if (suffix != null)
		    trimmedString.append(suffix);
	    } else {
		trimmedString.append((trimmedString.length() == 0 ? "" : " ") + word.toText());
	    }
	}
	logger.info("\ttrimmed: " + trimmedString.toString());
    }
    
    public String toString() {
	return toString(words);
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
	stmt.setString(5, null);
	stmt.execute();
	stmt.close();
    }
}
