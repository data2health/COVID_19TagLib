package org.cd2h.covid.model;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import pl.edu.icm.cermine.structure.model.BxLine;

public class Line {
	static Logger logger = LogManager.getLogger(Line.class);
    BxLine internalLine = null;
    String mostPopularFont = null;
    double x = 0.0;
    double y = 0.0;
    double height = 0.0;
    double width = 0.0;
    int spacing = 0;

    String rawText = null;
    
    public Line(BxLine line) {
	internalLine = line;
	rawText = line.toText();
	mostPopularFont = line.getMostPopularFontName();
	x = line.getX();
	y = line.getY();
	width = line.getWidth();
	height = line.getHeight();
	
    }
    
    public String getMostPopularFont() {
        return mostPopularFont;
    }

    public void setMostPopularFont(String mostPopularFont) {
        this.mostPopularFont = mostPopularFont;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getHeight() {
        return height;
    }

    public void setHeight(double height) {
        this.height = height;
    }

    public double getWidth() {
        return width;
    }

    public void setWidth(double width) {
        this.width = width;
    }

    public int getSpacing() {
        return spacing;
    }

    public void setSpacing(int spacing) {
        this.spacing = spacing;
    }

    public String getRawText() {
        return rawText;
    }

    public void setRawText(String rawText) {
        this.rawText = rawText;
    }

    public void dump() {
	logger.info("\t\tline: [" + String.format("%6.2f %6.2f %5.2f %6.2f %2d", x, y, height, width, spacing) + "] " + rawText + "\t\t" + mostPopularFont);
    }
}
