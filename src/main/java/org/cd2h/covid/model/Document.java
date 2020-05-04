package org.cd2h.covid.model;

import java.util.Vector;

import org.apache.log4j.Logger;
import org.cd2h.covid.detectors.SectionDetector;
import org.cd2h.covid.model.Section.Category;

public class Document {
    static Logger logger = Logger.getLogger(Document.class);
    String doi = null;
    String fileName = null;
    Vector<Page> pages = new Vector<Page>();
    Vector<Section> sections = new Vector<Section>();
    
    Section frontMatter = null;
    Section references = null;
    
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
	Section current = new Section(this, Category.FRONT, "Front Matter");
	sections.add(current);
	frontMatter = current;
	
	for (Page page : pages) {
	    for (Line line : page.lines) {
		Category result = detector.newSection(line);
		if (result != null) {
		    current = new Section(this, result, line);
		    sections.add(current);
		    if (result == Category.REFERENCES)
			references = current;
		} else
		    current.addLine(line);
	    }
	}
	
	if (references != null)
	    references.segmentReferences();
	// now for each of the categories, further refine the structure
	for (Section section : sections) {
	    switch (section.category) {
	    case FRONT:
		break;
	    case ABSTRACT:
		break;
	    case BODY:
		break;
	    case REFERENCES:
		break;
	    case MISC:
		break;
	    case SUPPLEMENTAL:
		break;
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
