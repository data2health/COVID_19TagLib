package org.cd2h.covid.model;

import java.util.Vector;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.cd2h.covid.detectors.SectionDetector;
import org.cd2h.covid.model.Section.Category;

import pl.edu.icm.cermine.structure.model.BxChunk;
import pl.edu.icm.cermine.structure.model.BxWord;

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
		segmentFrontMatter(section);
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
    
    Pattern affPattern = Pattern.compile("^[1-9*].*");
    enum Mode {TITLE, AUTHOR, AFFILIATION, OTHER};
    void segmentFrontMatter(Section section) {
	Mode mode = Mode.TITLE;
	String title = section.lines.firstElement().rawText;
	Vector<Line> authors = new Vector<Line>();
	Vector<Line> affiliations = new Vector<Line>();
	Vector<Line> others = new Vector<Line>();
	
	Line prev = section.lines.firstElement();
	for (int i  = 1; i < section.lines.size(); i++) {
	    Line line = section.lines.elementAt(i);
	    line.dump();
//	    for (int j = 0; j < line.internalLine.getFirstChild().childrenCount(); j++) {
//		BxChunk chunk = line.internalLine.getFirstChild().getChild(j);
//		logger.info("chunk: " + chunk.toText()  + " : " + chunk.getFontName()  + " : " + chunk.getHeight());
//	    }
	    switch (mode) {
	    case TITLE:
		if (line.mostPopularFont.equals(prev.mostPopularFont) && line.spacing < line.height * 2 && Math.abs(line.getHeight() - prev.getHeight()) < 1.0) {
		    title += " " + line.rawText;
		} else {
		    mode = Mode.AUTHOR;
		    authors.add(line);
		}
		break;
	    case AUTHOR:
		logger.info("auth line height " + line.getHeight() +  "\tnext chunk y: " + line.internalLine.getFirstChild().getFirstChild().getHeight());
		if (Math.abs(prev.internalLine.getFirstChild().getFirstChild().getHeight() - line.internalLine.getFirstChild().getFirstChild().getHeight()) < 2.0) {
		    authors.add(line);
		} else {
		    mode = Mode.AFFILIATION;
		    affiliations.add(line);
		}
		break;
	    case AFFILIATION:
		logger.info("aff line y " + line.getY() +  "\tnext chunk y: " + line.internalLine.getFirstChild().getFirstChild().getFirstChild().SIZE);
		if (line.spacing  > 0 && line.spacing < line.height * 3) {
		    affiliations.add(line);
		} else {
		    mode = Mode.OTHER;
		    others.add(line);
		}
		break;
	    case OTHER:
		others.add(line);
		break;
	    }
	    prev = line;
	}
	
	logger.info("title: " + title);
	logger.info("authors:");
	for (Line line : authors) {
	    line.dump();
	}
	logger.info("affiliations:");
	for (Line line : affiliations) {
	    line.dump();
	}
	logger.info("others:");
	for (Line line : others) {
	    line.dump();
	}
	
	logger.info("scanning affiliations:");
	Vector<Affiliation> affs = new Vector<Affiliation>();
	Affiliation affiliation = null;
	for (Line line : affiliations) {
//	    line.dump();
	    for (int i = 0; i < line.internalLine.childrenCount(); i++) {
		BxWord word = line.internalLine.getChild(i);
		logger.debug("\tword: " + word.toText());
		for (int j = 0; j < word.childrenCount(); j++) {
		    BxChunk chunk = word.getChild(j);
		    if (affs.size() == 0 || Math.abs(line.getHeight() - chunk.getHeight()) > 1.5) {
			logger.debug("\t\tprefix: " + chunk.toText() + " : " + chunk.getHeight());
			if (j == 0 || chunk.toText().matches("[*†]")) {
			    affiliation = new Affiliation(chunk.toText());
			    affs.add(affiliation);
			} else
			    affiliation.addLinkChar(chunk.toText());
		    } else {
			logger.debug("\t\tchunk: " + chunk.toText() + " : " + chunk.getHeight());
			affiliation.addAffiliationChar(chunk.toText());
		    }
		}
		affiliation.addAffiliationChar(" " );
	    }
	}
	for (Affiliation aff : affs) {
	    logger.info("affiliation: " + aff.link + " : " + aff.affiliation);
	}

	logger.info("scanning authors:");
	Vector<Author> auths = new Vector<Author>();
	Author author = null;
	for (Line line : authors) {
	    line.dump();
	    for (int i = 0; i < line.internalLine.childrenCount(); i++) {
		BxWord word = line.internalLine.getChild(i);
		logger.info("\tword: " + word.toText());
		if (word.toText().equals("and") || word.toText().equals("Authors:") || word.toText().equals("Affiliations:")) {
		    author = null;
		    continue;
		}
		for (int j = 0; j < word.childrenCount(); j++) {
		    BxChunk chunk = word.getChild(j);
		    if (auths.size() > 0 && Math.abs(line.getHeight() - chunk.getHeight()) > 2.5) {
			logger.info("\t\tprefix: " + chunk.toText() + " : " + chunk.getHeight());
			if (j == 0) {
			    author = new Author(chunk.toText());
			    auths.add(author);
			} else {
			    if (author == null) {
				author = new Author(chunk.toText());
				auths.add(author);
			    } else
				author.addAffiliationChar(chunk.toText());
			}
		    } else {
			logger.info("\t\tchunk: " + chunk.toText() + " : " + chunk.getHeight());
			if (author == null) {
			    author = new Author(chunk.toText());
			    auths.add(author);
			} else if (chunk.toText().equals(",")) {
			    author = null;
			} else if (chunk.toText().matches("[*†]")) {
			    author.addAffiliationChar(chunk.toText());
			} else
			    author.addNameChar(chunk.toText());
		    }
		}
		if (author != null)
		    author.addNameChar(" " );
	    }
	}
	
	logger.info("matching affiliations: ");
	for (Author auth : auths) {
	    auth.matchAffiliations(affs);
	}
	
	logger.info("authors:");
	for (Author auth : auths) {
	    logger.info("author: " + auth.name + " (" + auth.affiliations + ")");
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
