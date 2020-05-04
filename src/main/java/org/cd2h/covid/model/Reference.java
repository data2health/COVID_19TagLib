package org.cd2h.covid.model;

import org.apache.log4j.Logger;

public class Reference {
    static Logger logger = Logger.getLogger(Reference.class);
    int seqNum = 0;
    String name = null;
    int year = 0;
    String reference = null;
    
    public Reference(String reference) {
	this.reference = reference;
    }
    
    public Reference(int seqNum, String reference) {
	this.seqNum = seqNum;
	this.reference = reference;
    }
    
    public Reference(String name, int year, String reference) {
	this.name = name;
	this.year = year;
	this.reference  = reference;
    }
    
    public void addText(String text) {
	reference += " " + text;
    }
    
    public void setYear(int year) {
	this.year = year;
    }
    
    public void dump() {
	if (seqNum > 0)
	    logger.info("\t\treference: " + seqNum + " : " + reference);
	else
	    logger.info("\t\treference: " + name + " : " + year + " : " + reference);

    }

}
