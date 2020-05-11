package org.cd2h.covid.model;

import org.apache.log4j.Logger;

public class Affiliation {
    static Logger logger = Logger.getLogger(Section.class);

    String link = null;
    String affiliation = "";
    
    public Affiliation(String link) {
	this.link = link;
    }
    
    public void addLinkChar(String addition) {
	link += addition;
    }
    
    public void addAffiliationChar(String addition) {
	affiliation += addition;
    }
}
