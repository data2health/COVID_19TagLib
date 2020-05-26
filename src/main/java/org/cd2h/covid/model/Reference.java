package org.cd2h.covid.model;

import java.util.Vector;

import org.apache.log4j.Logger;

public class Reference {
    static Logger logger = Logger.getLogger(Reference.class);
    static enum Style {UNKNOWN, NUMBERED, BRACKETED, PARENTHESIZED, NAME_YEAR};
    
    int seqNum = 0;
    String name = null;
    int year = 0;
    String reference = null;
    Vector<Line> lines = new Vector<Line>();
    
    public Reference(Line line) {
	lines.add(line);
	this.reference = line.rawText;
    }
    
    public Reference(Line line, String reference) {
	lines.add(line);
	this.reference = reference;
    }
    
    public Reference(int seqNum, Line line, String reference) {
	this.seqNum = seqNum;
	lines.add(line);
	this.reference = reference;
    }
    
    public Reference(String name, int year, Line line, String reference) {
	this.name = name;
	this.year = year;
	lines.add(line);
	this.reference  = reference;
    }
    
    public void addText(Line line) {
	lines.add(line);
	reference += " " + line.rawText;
    }
    
    public void addText(Line line, String reference) {
	lines.add(line);
	reference += " " + reference;
    }
    
    public void regenerate() {
	reference = "";
	for (Line line : lines) {
	    reference += (reference.length() == 0 ? "" : " ") + line.rawText;
	}
    }
    
    public void setYear(int year) {
	this.year = year;
    }
    
    public void dump() {
	if (seqNum > 0)
	    logger.info("\t\treference: " + seqNum + " : " + reference);
	else
	    logger.info("\t\treference: " + name + " : " + year + " : " + reference);
	if (logger.isDebugEnabled()) {
	    for(Line line : lines) {
		line.dump();
	    }
	}
    }

}
