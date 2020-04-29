package org.cd2h.covid.model;

import java.util.Vector;

import org.apache.log4j.Logger;

import pl.edu.icm.cermine.structure.model.BxPage;

public class Page {
    static Logger logger = Logger.getLogger(Page.class);
    BxPage internalPage = null;
    int pageNumber = 0;
    Vector<Line> lines = new Vector<Line>();
    int[] lineSpacing = null;
    
    public Page(BxPage page) {
	this.internalPage = page;
	pageNumber = Integer.parseInt(page.getId());
    }
    
    public void addLines(Vector<Line> lines) {
	this.lines.addAll(lines);
    }
    
    public void addLine(Line line) {
	lines.add(line);
    }
    
    public void addLineSpacing(int[] lineSpacing) {
	this.lineSpacing = lineSpacing;
    }
    
    public void dump() {
	logger.info("\tpage: " + pageNumber);
	for (Line line : lines) {
	    line.dump();
	}
    }
}
