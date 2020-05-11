package org.cd2h.covid.model;

public class Author {
    String name = "";
    String affiliations = "";
    
    public Author(String chunk) {
	name += chunk;
    }
    
    public void addNameChar(String addition) {
	name += addition;
    }
    
    public void addAffiliationChar(String addition) {
	affiliations += addition;
    }
}
