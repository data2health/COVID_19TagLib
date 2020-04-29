package org.cd2h.covid.model;

import org.apache.log4j.Logger;

import pl.edu.icm.cermine.structure.model.BxLine;

public class Line {
    static Logger logger = Logger.getLogger(Line.class);
    BxLine internalLine = null;
    String rawText = null;
    
    public Line(BxLine line) {
	internalLine = line;
	rawText = line.toText();
    }
    
    public void dump() {
	logger.info("\t\tline: " + rawText);
    }
}
