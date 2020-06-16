package org.cd2h.covid.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Vector;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.cd2h.covid.detectors.SectionDetector;
import org.cd2h.covid.model.Section.Category;

import pl.edu.icm.cermine.structure.model.BxChunk;
import pl.edu.icm.cermine.structure.model.BxWord;

public class Document {
    static Logger logger = Logger.getLogger(Document.class);
    Connection conn = null;
    String doi = null;
    String fileName = null;
    Vector<Page> pages = new Vector<Page>();
    Vector<Section> sections = new Vector<Section>();
    
    Section frontMatter = null;
    Section abstr = null;
    Section references = null;
    
    public Document(String doi, String fileName) {
	this.doi = doi;
	this.fileName = fileName;
    }
    
    public void setConnection(Connection conn) {
	this.conn = conn;
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
    
    public void section() throws SQLException {
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
		    if (result == Category.ABSTRACT)
			abstr = current;
		    if (result == Category.REFERENCES)
			references = current;
		} else
		    current.addLine(line);
	    }
	}
	
	if (references != null)
	    references.segmentReferences();
	
	int citationCount = 0;
	
	// now for each of the categories, further refine the structure
	for (Section section : sections) {
	    switch (section.category) {
	    case FRONT:
		segmentFrontMatter(section);
		break;
	    case ABSTRACT:
		section.segment();
		break;
	    case BODY:
		section.segment();
		citationCount += section.citationCount();
		break;
	    case REFERENCES:
		break;
	    case MISC:
		break;
	    case SUPPLEMENTAL:
		break;
	    }
	}
	
	if (references != null && Reference.numberedReferenceStyle(references.reference_style) && citationCount == 0) {
	    // no matches so probably superscript citation formatting
	    for (Section section : sections) {
		switch (section.category) {
		case FRONT:
		    break;
		case ABSTRACT:
		    break;
		case BODY:
		    section.rescanCitations(Reference.Style.NUMBERED);
		    citationCount += section.citationCount();
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

	if (references != null && Reference.numberedReferenceStyle(references.reference_style) && citationCount == 0) {
	    // no matches so let's try parenthesized citation formatting
	    for (Section section : sections) {
		switch (section.category) {
		case FRONT:
		    break;
		case ABSTRACT:
		    break;
		case BODY:
		    section.rescanCitations(Reference.Style.PARENTHESIZED);
		    citationCount += section.citationCount();
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
    }
    
    Pattern affPattern = Pattern.compile("^[1-9*].*");
    enum Mode {TITLE, AUTHOR, AFFILIATION, OTHER};
    void segmentFrontMatter(Section section) throws SQLException {
	Mode mode = Mode.TITLE;
	String title = "";
	Vector<Line> authors = new Vector<Line>();
	Vector<Line> affiliations = new Vector<Line>();
	Vector<Line> others = new Vector<Line>();
	
	Line prev = null;
	for (int i  = 0; i < section.lines.size(); i++) {
	    Line line = section.lines.elementAt(i);
	    if (logger.isDebugEnabled())
		line.dump();
	    switch (mode) {
	    case TITLE:
		if (prev == null || (line.mostPopularFont.equals(prev.mostPopularFont)
//			&& line.spacing < line.height * 2
			&& line.getHeight() == prev.getHeight()
		)) {
		    if (line.rawText.startsWith("Title") && line.rawText.indexOf(' ') > 0)
			title = line.rawText.substring(line.rawText.indexOf(' ') + 1);
		    else if (line.rawText.startsWith("Title")
			    || line.rawText.startsWith("Note:")
			    || line.rawText.equals("Page")
			    || line.rawText.equals("Article")
			    || line.rawText.equals("Original Article")
			    || line.rawText.equals("Original article")
			    || line.rawText.equals("1"))
			title = "";
		    else
			title += " " + line.rawText;
		} else {
		    mode = Mode.AUTHOR;
		    authors.add(line);
		}
		break;
	    case AUTHOR:
		logger.debug("auth line height " + line.getHeight() +  "\tnext chunk y: " + line.internalLine.getFirstChild().getFirstChild().getHeight());
		logger.debug(line.rawText);
		if (line.rawText.matches("^1[.]?.*")) {
		    mode = Mode.AFFILIATION;
		    affiliations.add(line);
		} else if (line.rawText.matches("Affiliations[:]?")) {
		    mode = Mode.AFFILIATION;
		} else if (Math.abs(prev.internalLine.getFirstChild().getFirstChild().getHeight() - line.internalLine.getFirstChild().getFirstChild().getHeight()) < 1.8
				&& (line.getSpacing() < line.getHeight() * 2.5 || authorPatternMatch(line))) {
		    authors.add(line);
		} else {
		    mode = Mode.AFFILIATION;
		    affiliations.add(line);
		}
		break;
	    case AFFILIATION:
		logger.debug("aff line y " + line.getY() +  "\tnext chunk y: " + line.internalLine.getFirstChild().getFirstChild().getFirstChild().SIZE);
		if (line.spacing  > 0 && line.spacing < line.height * 3) {
		    affiliations.add(line);
		} else if (line.internalLine.childrenCount() == 1 && i < section.lines.size() - 1 && line.getY() > section.lines.elementAt(i+1).internalLine.getHeight() - section.lines.elementAt(i+1).internalLine.getY()) {
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

	PreparedStatement stmt = conn.prepareStatement("update covid_biorxiv.document set title = ? where doi = ?");
	stmt.setString(1, title.trim());
	stmt.setString(2, doi);
	stmt.executeUpdate();
	stmt.close();
	
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
	double affTotal = 0.0;
	for (Line line : affiliations) {
	    affTotal += line.height;
	}
	double affAve = affTotal / affiliations.size();
	logger.debug("line height average: " + affAve);
	for (Line line : affiliations) {
//	    line.dump();
	    for (int i = 0; i < line.internalLine.childrenCount(); i++) {
		BxWord word = line.internalLine.getChild(i);
		logger.debug("\tword: " + word.toText());
		for (int j = 0; j < word.childrenCount(); j++) {
		    BxChunk chunk = word.getChild(j);
		    if (affs.size() == 0 || chunk.getHeight() / affAve < 0.75) {
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
	    try {
		PreparedStatement affStmt = conn.prepareStatement("insert into covid_biorxiv.institution values (?,?,?)");
		affStmt.setString(1, doi);
		affStmt.setString(2, aff.link.trim());
		affStmt.setString(3, aff.affiliation.trim());
		affStmt.executeUpdate();
		affStmt.close();
	    } catch (Exception e) {
		logger.error("exception storing institution: ", e);
	    }
	}

	logger.info("scanning authors:");
	Vector<Author> auths = new Vector<Author>();
	Author author = null;
	for (Line line : authors) {
	    if (logger.isDebugEnabled())
		line.dump();
	    for (int i = 0; i < line.internalLine.childrenCount(); i++) {
		BxWord word = line.internalLine.getChild(i);
		logger.debug("\tword: " + word.toText());
		if (word.toText().equals("and") || word.toText().startsWith("Author") || word.toText().startsWith("Affiliation")) {
		    author = null;
		    continue;
		}
		for (int j = 0; j < word.childrenCount(); j++) {
		    BxChunk chunk = word.getChild(j);
		    if (auths.size() > 0 && Math.abs(line.getHeight() - chunk.getHeight()) > 2.5) {
			logger.debug("\t\tprefix: " + chunk.toText() + " : " + chunk.getHeight());
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
			logger.debug("\t\tchunk: " + chunk.toText() + " : " + chunk.getHeight());
			if (author == null) {
			    author = new Author(chunk.toText());
			    auths.add(author);
			} else if (chunk.toText().equals(",") || chunk.toText().equals(";")) {
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
	
	int seqnum = 0;
	logger.debug("authors:");
	for (Author auth : auths) {
	    logger.debug("author: " + auth.name + " (" + auth.affiliationString + ")");
	    
	    PreparedStatement authStmt = conn.prepareStatement("insert into covid_biorxiv.author values (?,?,?,?)");
	    authStmt.setString(1, doi);
	    authStmt.setInt(2, ++seqnum);
	    authStmt.setString(3, auth.name.trim());
	    authStmt.setString(4, auth.affiliationString);
	    authStmt.executeUpdate();
	    authStmt.close();
	    
	    for (Affiliation aff : auth.affiliations) {
		try {
		    PreparedStatement affStmt = conn.prepareStatement("insert into covid_biorxiv.affiliation values (?,?,?)");
		    affStmt.setString(1, doi);
		    affStmt.setInt(2, seqnum);
		    affStmt.setString(3, aff.link.trim());
		    affStmt.executeUpdate();
		    affStmt.close();
		} catch (SQLException e) {
		    logger.error("exception storing affiliation: ", e);
		}
	    }
	}
    }
    
    boolean authorPatternMatch(Line line) {
	int count = 0;
	for (Character character : line.rawText.toCharArray()) {
	    if (character == ',')
		count++;
	}
	if (count == 0)
	    return false;
	else
	    return line.internalLine.childrenCount() / count < 4;
    }
    
    public void dump() throws SQLException {
	logger.info("Document: " + doi + " : " + fileName + " : " + pages.size() + " pages");
	if (logger.isDebugEnabled()) {
	    for (Page page : pages) {
		page.dump();
	    }
	}
	int count = 1;
	for (Section section : sections) {
	    section.dump();
	    section.store(doi, count++);
	}
    }
}
