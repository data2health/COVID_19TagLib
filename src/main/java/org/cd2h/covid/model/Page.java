package org.cd2h.covid.model;

import java.util.Vector;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import pl.edu.icm.cermine.structure.model.BxPage;

public class Page {
	static Logger logger = LogManager.getLogger(Page.class);
    BxPage internalPage = null;
    int pageNumber = 0;
    String mostPopularFont = null;
    Vector<Line> lines = new Vector<Line>();
    int[] lineSpacing = null;
    
    public Page(BxPage page) {
	this.internalPage = page;
	pageNumber = Integer.parseInt(page.getId());
	mostPopularFont = page.getMostPopularFontName();
    }
    
    public void addLines(Vector<Line> lines) {
	this.lines.addAll(lines);
    }
    
    public void addLine(Line line) {
	lines.add(line);
	if (lines.size() > 1)
	    line.spacing = (int)line.y - (int)lines.elementAt(lines.size()-2).y;
    }
    
    public void addLineSpacing(int[] lineSpacing) {
	this.lineSpacing = lineSpacing;
    }
    
    public void dump() {
	logger.info("\tpage: " + pageNumber + ", font: " + mostPopularFont);
	for (Line line : lines) {
	    line.dump();
	}
    }
}
