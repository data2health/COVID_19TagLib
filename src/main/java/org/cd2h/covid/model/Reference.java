package org.cd2h.covid.model;

import java.util.Vector;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Reference {
	static Logger logger = LogManager.getLogger(Reference.class);
    static enum Style {UNKNOWN, NUMBERED, BRACKETED, PARENTHESIZED, NAME_YEAR};
    
    static public boolean numberedReferenceStyle(Style candidate) {
	if (candidate == Style.NUMBERED)
	    return true;
	if (candidate == Style.BRACKETED)
	    return true;
	if (candidate == Style.PARENTHESIZED)
	    return true;
	return false;
    }
    
    int seqNum = 0;
    String name = null;
    String year = null;
    String reference = null;
    Vector<Line> lines = new Vector<Line>();
    Vector<Citation> citations = new Vector<Citation>();
    
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
    
    public Reference(String name, String year, Line line, String reference) {
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
    
    public void addCitation(Citation citation) {
	citations.add(citation);
    }
    
    public void regenerate() {
	reference = "";
	for (Line line : lines) {
	    reference += (reference.length() == 0 ? "" : " ") + line.rawText;
	}
    }
    
    public void setYear(String year) {
	this.year = year;
    }
    
    public String toString() {
	if (seqNum > 0)
	    return seqNum + " : " + reference;
	else
	    return name + " : " + year + " : " + reference;
    }
    
    public void dump() {
	logger.info("\t\treference: " + toString());
	if (logger.isDebugEnabled()) {
	    for(Line line : lines) {
		line.dump();
	    }
	}
	for (Citation citation : citations) {
	    logger.info("\t\t\tciting sentence: " + citation.referencingSentence.toString());
	}
    }

}
