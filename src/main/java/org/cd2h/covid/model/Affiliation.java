package org.cd2h.covid.model;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Affiliation {
	static Logger logger = LogManager.getLogger(Affiliation.class);

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
