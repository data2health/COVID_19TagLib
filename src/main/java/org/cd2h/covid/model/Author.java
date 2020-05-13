package org.cd2h.covid.model;

import java.util.Vector;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

public class Author {
    static Logger logger = Logger.getLogger(Author.class);

    String name = "";
    String affiliations = "";
    Pattern refPattern = Pattern.compile("");
    
    public Author(String chunk) {
	name += chunk;
    }
    
    public void addNameChar(String addition) {
	name += addition;
    }
    
    public void addAffiliationChar(String addition) {
	affiliations += addition;
    }
    
    public void matchAffiliations(Vector<Affiliation> affs) {
	logger.info("author: " + name);
	logger.info("\taffiliation string: " + affiliations);
    }
}
