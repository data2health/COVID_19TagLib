package org.cd2h.covid.model;

import java.util.Vector;

import org.apache.log4j.Logger;

public class Document {
    static Logger logger = Logger.getLogger(Document.class);
    String doi = null;
    String fileName = null;
    Vector<Page> pages = new Vector<Page>();
    
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
    
    public void dump() {
	logger.info("Document: " + doi + " : " + fileName);
	for (Page page : pages) {
	    page.dump();
	}
    }
}
