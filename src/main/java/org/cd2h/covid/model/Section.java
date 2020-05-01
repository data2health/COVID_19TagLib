package org.cd2h.covid.model;

import java.util.Vector;

import org.apache.log4j.Logger;

public class Section {
    static Logger logger = Logger.getLogger(Section.class);
    public static enum Category {FRONT, ABSTRACT, BODY, REFERENCES, MISC, SUPPLEMENTAL};
    
    Category category = null;
    Line labelLine = null;
    String label = null;
    int labelHeight = 0;
    String labelFont = null;
    Vector<Line> lines = new Vector<Line>();
    
    public Section(Category category, String label) {
	this.category = category;
	this.label = label;
    }

    public Section(Category category, Line line) {
	this.category = category;
	this.labelLine = line;
	this.label = line.rawText;
	this.labelHeight = (int)line.height;
	this.labelFont = line.getMostPopularFont();
    }

    public void addLine(Line line) {
	lines.add(line);
	if (lines.size() > 1)
	    line.spacing = (int)line.y - (int)lines.elementAt(lines.size()-2).y;
    }
    
    public void dump() {
	logger.info("\tsection: " + category + " : " + label + "\t" + labelHeight + " : " + labelFont);
	for (Line line : lines) {
	    line.dump();
	}
    }
}
