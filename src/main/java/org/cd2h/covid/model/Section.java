package org.cd2h.covid.model;

import java.util.Vector;

import org.apache.log4j.Logger;

public class Section {
    static Logger logger = Logger.getLogger(Section.class);
    String label = null;
    Vector<Line> lines = new Vector<Line>();
    
    public Section(String label) {
	this.label = label;
    }

    public Section(Line line) {
	this.label = line.rawText;
    }

    public void addLine(Line line) {
	lines.add(line);
	if (lines.size() > 1)
	    line.spacing = (int)line.y - (int)lines.elementAt(lines.size()-2).y;
    }
    
    public void dump() {
	logger.info("\tsection: " + label);
	for (Line line : lines) {
	    line.dump();
	}
    }
}
