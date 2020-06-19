package org.cd2h.covid.model;

import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

public class Author {
    static Logger logger = Logger.getLogger(Author.class);

    String name = "";
    String affiliationString = "";
    Pattern refPattern = Pattern.compile("^([^0-9]|[0-9]+)(-([0-9]+))?[,;]?(.*)$");
    Vector<Affiliation> affiliations = new Vector<Affiliation>();
    
    public Author(String chunk) {
	name += chunk;
    }
    
    public void addNameChar(String addition) {
	name += addition;
    }
    
    public void addAffiliationChar(String addition) {
	affiliationString += addition;
    }
    
    public void matchAffiliations(Vector<Affiliation> affs) {
	logger.info("author: " + name);
	logger.info("\taffiliation string: " + affiliationString);
	String buffer = affiliationString;
	while (buffer != null && buffer.length() > 0) {
	    Matcher matcher = refPattern.matcher(buffer);
	    if (matcher.matches()) {
		String aff1 = matcher.group(1);
		String aff2 = matcher.group(3);
		buffer = matcher.group(4);
		logger.info("\t\taff1: " + aff1 + "\taff2: " + aff2 + "\tbuffer: " + buffer);
		if (aff2 == null) {
		    Affiliation affiliation = getAffiliationByTag(affs, aff1);
		    if (affiliation != null) {
			affiliations.add(affiliation);
		    }
		} else {
		    //assuming that we have a numeric range
		    try {
			int first = Integer.parseInt(aff1);
			int last = Integer.parseInt(aff2);
			for (int i = first; i <= last; i++) {
			    Affiliation affiliation = getAffiliationByTag(affs, i + "");
			    if (affiliation != null) {
				affiliations.add(affiliation);
			    }
			}
		    } catch (NumberFormatException e) {
			logger.error("affiliation range parse failed: " + aff1 + " : " + aff2);;
		    }
		}
	    }
	}
	for (Affiliation affiliation : affiliations) {
	    logger.info("\taffiliation: " + affiliation.link + " : " + affiliation.affiliation);
	}
    }
    
    Affiliation getAffiliationByTag(Vector<Affiliation> affs, String tag) {
	for (Affiliation affiliation : affs) {
	    if (tag.equals(affiliation.link))
		return affiliation;
	}
	return null;
    }
}
