package org.cd2h.covid.model;

import java.util.Vector;

import org.apache.log4j.Logger;
import org.cd2h.covid.detectors.SectionDetector;

public class Document {
    static Logger logger = Logger.getLogger(Document.class);
    String doi = null;
    String fileName = null;
    Vector<Page> pages = new Vector<Page>();
    Vector<Section> sections = new Vector<Section>();
    
    public Document(String doi, String fileName) {
	this.doi = doi;
	this.fileName = fileName;
    }
    
    public String getDoi() {
        return doi;
    }

    public void setDoi(String doi) {
        this.doi = doi;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void addPage(Page page) {
	pages.add(page);
    }
    
    public void section() {
	SectionDetector detector = new SectionDetector();
	Section current = new Section("Front Matter");
	sections.add(current);
	
	for (Page page : pages) {
	    for (Line line : page.lines) {
		if (detector.newSection(line)) {
		    current = new Section(line);
		    sections.add(current);
		} else
		    current.addLine(line);
	    }
	}
    }
    
    public void dump() {
	logger.info("Document: " + doi + " : " + fileName + " : " + pages.size() + " pages");
	if (logger.isDebugEnabled()) {
	    for (Page page : pages) {
		page.dump();
	    }
	}
	for (Section section : sections) {
	    section.dump();
	}
    }
}
