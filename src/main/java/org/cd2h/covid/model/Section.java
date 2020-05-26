package org.cd2h.covid.model;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.cd2h.covid.CERMINEExtractor;
import org.cd2h.covid.model.Reference.Style;

import pl.edu.icm.cermine.structure.model.BxWord;

public class Section {
    static Logger logger = Logger.getLogger(Section.class);
    static DecimalFormat formatter = new DecimalFormat("0000.00");
    public static enum Category {FRONT, ABSTRACT, BODY, REFERENCES, MISC, SUPPLEMENTAL};
    
    /*
     * Unmatched patterns
     * 	(1) ...
     *   1) ...
     *   1 ...
     */
    static Pattern numberedReferencePattern = Pattern.compile("^([0-9]+)\\. +(.*)");
    static Pattern bracketNumberedReferencePattern = Pattern.compile("^\\[([0-9]+)\\]\\.? *(.*)");
    static Pattern parenNumberedReferencePattern = Pattern.compile("^\\(?([0-9]+)\\)\\.? *(.*)");
    static Pattern nameYearReferencePattern = Pattern.compile("^(.*)\\(([0-9]{4})\\) *(.*)");
    static Pattern trailingYearReferencePattern = Pattern.compile("^(.*)\\(([0-9]{4})\\)\\.$");
    
    Document parent = null;
    Category category = null;
    Line labelLine = null;
    String label = null;
    int labelHeight = 0;
    String labelFont = null;
    Vector<Line> lines = new Vector<Line>();
    public Style reference_style = Style.UNKNOWN;
    Vector<Reference> references = new Vector<Reference>();
    Vector<Sentence> sentences = new Vector<Sentence>();
    
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
    
    public void segment() {
	Sentence current = null;
	Line prevLine = null;
	BxWord prev = null;
	logger.info("segmenting section:");
	for (Line line : lines) {
	    line.dump();
	    for (int i = 0; i < line.internalLine.childrenCount(); i++) {
		BxWord word = line.internalLine.getChild(i);
		if (current == null) {
		    current = new Sentence(word);
		    sentences.add(current);
		} else {
		    if ((terminalPunctuation(prev) && capitalized(word))
			|| (prevLine != null && i == 0 && capitalized(word) && !prev.getMostPopularFontName().equals(word.getMostPopularFontName()))
			|| (prevLine != null && i == 0 && prevLine.getSpacing() > 0 && prevLine.getSpacing() + 10 < line.getSpacing())) {
			current = new Sentence(word);
			sentences.add(current);
		    } else
			current.addWord(word);
		}
		prev = word;
	    }
	    prevLine = line;
	}
	for (Sentence sentence : sentences) {
	    sentence.citationScan(parent.references == null ? Style.UNKNOWN :  parent.references.reference_style);
	}
    }
    
    boolean terminalPunctuation(BxWord word) {
	String wordString = word.toText();
	return wordString.endsWith(".") || wordString.endsWith("!") || wordString.endsWith("?");
    }
    
    boolean capitalized(BxWord word) {
	String wordString = word.toText();
	return Character.isUpperCase(wordString.charAt(0));
    }
    
    public void segmentReferences() {
	if (lines.isEmpty())
	    return;
	if (numberedReferencePattern.matcher(lines.firstElement().rawText).matches()) {
	    logger.info("*** numbered citations");
	    reference_style = Style.NUMBERED;
	    scanNumberedReferences(numberedReferencePattern);
	} else if (bracketNumberedReferencePattern.matcher(lines.firstElement().rawText).matches()) {
	    logger.info("*** bracketed numbered citations");
	    reference_style = Style.BRACKETED;
	    scanNumberedReferences(bracketNumberedReferencePattern);
	} else if (parenNumberedReferencePattern.matcher(lines.firstElement().rawText).matches()) {
	    logger.info("*** paren numbered citations");
	    reference_style = Style.PARENTHESIZED;
	    scanNumberedReferences(parenNumberedReferencePattern);
	} else if (nameYearReferencePattern.matcher(lines.firstElement().rawText).matches()) {
	    logger.info("*** name/year citations");
	    reference_style = Style.NAME_YEAR;
	    scanNameYearReferences(nameYearReferencePattern);
	} else {
	    logger.info("*** unknown citation scheme");
	    int[] leftMargin = new int[2000];
	    int[] spacing = new int[2000];
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
		logger.debug("leftMargin[" + i + "] : " + leftMargin[i]);
	    }
	    for (int i = 0; i < spacing.length; i++) {
		if (spacing[i] == 0)
		    continue;
		logger.debug("spacing[" + i + "] : " + spacing[i]);
	    }
	    logger.debug("trailing year count: " + trailingYearCount);
	    
	    if (trailingYearCount > lines.size() / 4) { // totally heuristic guess at cutoff
		logger.debug("*** trailing year citations");
		scanTrailingYearReferences(trailingYearReferencePattern);
	    } else {
		storeStats(lines.size(), 0);
	    }
	}
    }
    
    void scanNumberedReferences(Pattern pattern) {
	references = new Vector<Reference> ();
	Reference current = null;
	for (Line line : lines) {
	    logger.debug("\t\t\tline: " + line.rawText);
	    Matcher matcher = pattern.matcher(line.rawText);
	    if (matcher.matches()) {
		logger.debug("\t\t\treference start: " + line.rawText);
		current = new Reference(Integer.parseInt(matcher.group(1)), line, matcher.group(2));
		references.add(current);
	    } else {
		logger.debug("\t\t\treference continuation: " + line.rawText);
		logger.debug("\t\t\tdelta: " + formatter.format((line.getY()-current.lines.lastElement().getY())));
		current.addText(line);
	    }
	}
	checkReferences(references);
	storeStats(lines.size(), references.size());
    }
    
    void scanNameYearReferences(Pattern pattern) {
	references = new Vector<Reference> ();	
	Reference current = null;
	for (Line line : lines) {
	    logger.debug("\t\t\tline: " + line.rawText);
	    Matcher matcher = pattern.matcher(line.rawText);
	    if (matcher.matches()) {
		logger.debug("\t\t\treference start: " + line.rawText);
		current = new Reference(matcher.group(1),Integer.parseInt(matcher.group(2)),line,matcher.group(3));
		references.add(current);
	    } else {
		logger.debug("\t\t\treference continuation: " + line.rawText);
		current.addText(line);
	    }
	}
	checkReferences(references);
	storeStats(lines.size(), references.size());
    }
    
    void scanTrailingYearReferences(Pattern pattern) {
	references = new Vector<Reference>();
	Reference current = null;
	for (Line line : lines) {
	    logger.debug("\t\t\tline: " + line.rawText);
	    Matcher matcher = pattern.matcher(line.rawText);
	    if (matcher.matches()) {
		logger.debug("\t\t\treference end: " + line.rawText);
		if (current == null) //single line reference
		    current = new Reference(line, matcher.group(1));
		else
		    current.addText(line, matcher.group(1));
		current.setYear(Integer.parseInt(matcher.group(2)));
		current = null;
	    } else if (current == null) {
		logger.debug("\t\t\treference start: " + line.rawText);
		current = new Reference(line);
		references.add(current);
	    } else {
		logger.debug("\t\t\treference continuation: " + line.rawText);
		current.addText(line);
	    }
	}
	
	checkReferences(references);
	storeStats(lines.size(), references.size());
    }
    
    void checkReferences(Vector<Reference> references) {
	if (references.size() > 0 && references.lastElement().lines.size() > 3) {
	    Reference ref = references.lastElement();
	    logger.debug("candidate runaway: ");
	    ref.dump();
	    for (int i = 1; i < ref.lines.size(); i++) {
		double delta = ref.lines.elementAt(i).getY()-ref.lines.elementAt(i-1).getY();
		logger.debug(i+"\tdelta: " + formatter.format(delta));
		if (delta < 0.0 || delta > (ref.lines.elementAt(i-1).getHeight())*2) {
		    Section newSection = new Section(parent,Category.MISC,(String)null);
		    while (i < ref.lines.size()) {
			newSection.addLine(ref.lines.remove(i));
		    }
		    int index = parent.sections.indexOf(this);
		    logger.debug("adding section after index: " + index);
		    parent.sections.add(index+1, newSection);
		    ref.regenerate();
		}
	    }
	}
	for (int i = 0; i < references.size(); i++) {
	    Reference ref = references.elementAt(i);
	    storeReference(i+1,ref);
	}
    }
    
    public void storeReference(int seqnum, Reference reference) {
	try {
	    PreparedStatement stmt = parent.conn.prepareStatement("insert into covid_biorxiv.reference(doi,seqnum,count,name,year,reference) values(?,?,?,?,?,?)");
	    stmt.setString(1, parent.doi);
	    stmt.setInt(2, seqnum);
	    stmt.setInt(3, reference.lines.size());
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
	    PreparedStatement stmt = parent.conn.prepareStatement("insert into covid_biorxiv.reference_stats values(?,?,?)");
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
//	    for (Line line : lines) {
//		line.dump();
//	    }
	    for (Sentence sentence : sentences) {
		logger.info("\t\tsentence: " + sentence);
	    }
	}
    }
    
    public void store(String doi, int seqnum) throws SQLException {
	PreparedStatement stmt = parent.conn.prepareStatement("insert into covid_biorxiv.section values(?,?,?,?)");
	stmt.setString(1, doi);
	stmt.setInt(2, seqnum);
	stmt.setString(3, category.toString());
	stmt.setString(4, label);
	stmt.execute();
	stmt.close();
	
	for (int i = 0; i < sentences.size(); i++) {
	    sentences.elementAt(i).store(parent.conn, doi, seqnum, i);
	}
    }
}
