package org.cd2h.covid.model;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.cd2h.covid.CERMINEExtractor;

public class Section {
    static Logger logger = Logger.getLogger(Section.class);
    public static enum Category {FRONT, ABSTRACT, BODY, REFERENCES, MISC, SUPPLEMENTAL};
    
    /*
     * Unmatched patterns
     * 	(1) ...
     *   1) ...
     *   1 ...
     */
    static Pattern numberedReferencePattern = Pattern.compile("^([0-9]+)\\. +(.*)");
    static Pattern bracketNumberedReferencePattern = Pattern.compile("^\\[([0-9]+)\\]\\.? *(.*)");
    static Pattern nameYearReferencePattern = Pattern.compile("^(.*)\\(([0-9]{4})\\) *(.*)");
    static Pattern trailingYearReferencePattern = Pattern.compile("^(.*)\\(([0-9]{4})\\)\\.$");
    
    Document parent = null;
    Category category = null;
    Line labelLine = null;
    String label = null;
    int labelHeight = 0;
    String labelFont = null;
    Vector<Line> lines = new Vector<Line>();
    Vector<Reference> references = new Vector<Reference>();
    
    public Section(Document parent, Category category, String label) {
	this.parent = parent;
	this.category = category;
	this.label = label;
    }

    public Section(Document parent, Category category, Line line) {
	this.parent = parent;
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
    
    public void segmentReferences() {
	if (numberedReferencePattern.matcher(lines.firstElement().rawText).matches()) {
	    logger.info("*** numbered citations");
	    scanNumberedReferences(numberedReferencePattern);
	} else if (bracketNumberedReferencePattern.matcher(lines.firstElement().rawText).matches()) {
	    logger.info("*** bracketed numbered citations");
	    scanNumberedReferences(bracketNumberedReferencePattern);
	} else if (nameYearReferencePattern.matcher(lines.firstElement().rawText).matches()) {
	    logger.info("*** name/year citations");
	    scanNameYearReferences(nameYearReferencePattern);
	} else {
	    logger.info("*** unknown citation scheme");
	    int[] leftMargin = new int[1000];
	    int[] spacing = new int[1000];
	    int trailingYearCount = 0;
	    
	    for (Line line : lines) {
		leftMargin[(int)line.x]++;
		spacing[Math.max(0,line.getSpacing())]++;
		if (trailingYearReferencePattern.matcher(line.rawText).matches())
		    trailingYearCount++;
	    }
	    for (int i = 0; i < leftMargin.length; i++) {
		if (leftMargin[i] == 0)
		    continue;
		logger.info("leftMargin[" + i + "] : " + leftMargin[i]);
	    }
	    for (int i = 0; i < spacing.length; i++) {
		if (spacing[i] == 0)
		    continue;
		logger.info("spacing[" + i + "] : " + spacing[i]);
	    }
	    logger.info("trailing year count: " + trailingYearCount);
	    
	    if (trailingYearCount > lines.size() / 4) { // totally heuristic guess at cutoff
		logger.info("*** trailing year citations");
		scanTrailingYearReferences(trailingYearReferencePattern);
	    } else {
		storeStats(lines.size(), 0);
	    }
	}
    }
    
    void scanNumberedReferences(Pattern pattern) {
	references = new Vector<Reference> ();
	Reference current = null;
	int seqnum = 0;
	int count = 0;
	for (Line line : lines) {
	    logger.info("\t\t\tline: " + line.rawText);
	    Matcher matcher = pattern.matcher(line.rawText);
	    if (matcher.matches()) {
		logger.debug("\t\t\treference start: " + line.rawText);
		if (count > 0)
		    storeReference(seqnum,count,current);
		current = new Reference(Integer.parseInt(matcher.group(1)),matcher.group(2));
		references.add(current);
		seqnum++;
		count = 1;
	    } else {
		logger.debug("\t\t\treference continuation: " + line.rawText);
		current.addText(line.rawText);
		count++;
	    }
	}
	storeReference(seqnum,count,current);
	storeStats(lines.size(), references.size());
    }
    
    void scanNameYearReferences(Pattern pattern) {
	references = new Vector<Reference> ();	
	Reference current = null;
	int seqnum = 0;
	int count = 0;
	for (Line line : lines) {
	    logger.debug("\t\t\tline: " + line.rawText);
	    Matcher matcher = pattern.matcher(line.rawText);
	    if (matcher.matches()) {
		logger.debug("\t\t\treference start: " + line.rawText);
		if (count > 0)
		    storeReference(seqnum,count,current);
		current = new Reference(matcher.group(1),Integer.parseInt(matcher.group(2)),matcher.group(3));
		references.add(current);
		seqnum++;
		count = 1;
	    } else {
		logger.debug("\t\t\treference continuation: " + line.rawText);
		current.addText(line.rawText);
		count++;
	    }
	}
	storeReference(seqnum,count,current);
	storeStats(lines.size(), references.size());
    }
    
    void scanTrailingYearReferences(Pattern pattern) {
	references = new Vector<Reference>();
	Reference current = null;
	int seqnum = 1;
	int count = 0;
	for (Line line : lines) {
	    logger.info("\t\t\tline: " + line.rawText);
	    Matcher matcher = pattern.matcher(line.rawText);
	    if (matcher.matches()) {
		logger.info("\t\t\treference end: " + line.rawText);
		if (current == null) //single line reference
		    current = new Reference(matcher.group(1));
		else
		    current.addText(matcher.group(1));
		current.setYear(Integer.parseInt(matcher.group(2)));
		storeReference(seqnum,count,current);
		seqnum++;
		count = 0;
		current = null;
	    } else if (current == null) {
		logger.info("\t\t\treference start: " + line.rawText);
		current = new Reference(line.rawText);
		references.add(current);
		count++;
	    } else {
		logger.info("\t\t\treference continuation: " + line.rawText);
		current.addText(line.rawText);
		count++;
	    }
	}
	
	storeStats(lines.size(), references.size());
    }
    
    public void storeReference(int seqnum, int lines, Reference reference) {
	try {
	    PreparedStatement stmt = CERMINEExtractor.conn.prepareStatement("insert into covid_biorxiv.reference(doi,seqnum,count,name,year,reference) values(?,?,?,?,?,?)");
	    stmt.setString(1, parent.doi);
	    stmt.setInt(2, seqnum);
	    stmt.setInt(3, lines);
	    stmt.setString(4, reference.name);
	    stmt.setInt(5, reference.year);
	    stmt.setString(6, reference.reference);
	    stmt.execute();
	    stmt.close();
	} catch (SQLException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	    System.exit(0);
	}
    }
    
    public void storeStats(int lines, int refs) {
	try {
	    PreparedStatement stmt = CERMINEExtractor.conn.prepareStatement("insert into covid_biorxiv.reference_stats values(?,?,?)");
	    stmt.setString(1, parent.doi);
	    stmt.setInt(2, lines);
	    stmt.setInt(3, refs);
	    stmt.execute();
	    stmt.close();
	} catch (SQLException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	    System.exit(0);
	}
    }
    
    public void dump() {
	logger.info("\tsection: " + category + " : " + label + "\t" + labelHeight + " : " + labelFont);
	if (references != null && references.size() > 0) {
	    for (Reference reference : references) {
		reference.dump();
	    }
	} else {
	    for (Line line : lines) {
		line.dump();
	    }
	}
    }
}
