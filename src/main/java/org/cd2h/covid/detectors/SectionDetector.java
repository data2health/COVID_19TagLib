package org.cd2h.covid.detectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.cd2h.covid.BioRxivProcessor;
import org.cd2h.covid.model.Line;
import org.cd2h.covid.model.Section.Category;

public class SectionDetector {
	static Logger logger = LogManager.getLogger(BioRxivProcessor.class);
    Line prevLine = null;

    public Category newSection(Line line) {
	String label = line.getRawText().toLowerCase().trim();
	Category result = null;
	
	if (label.matches("abstract *([:].*)?"))
	    result = Category.ABSTRACT;
	else if (label.matches("summary *\\(.*\\)"))
	    result = Category.ABSTRACT;
	else if (label.matches("([0-9].?)? *introduction *([:].*)?"))
	    result = Category.BODY;
	else if (label.matches("([0-9].?)? *background *([:].*)?"))
	    result = Category.BODY;
	else if (label.matches("([0-9].?)? *importance"))
	    result = Category.BODY;
	else if (label.matches("([0-9].?)? *main text *([:].*)?"))
	    result = Category.BODY;
	else if (label.matches("([0-9].?)? *results?( and (discussions?|analys(is|es)))? *([:].*)?"))
	    result = Category.BODY;
	else if (label.matches("([0-9].?)? *materials?( and methods?)? *([:].*)?"))
	    result = Category.BODY;
	else if (label.matches("([0-9].?)? *methods?( and materials?)? *([:].*)?"))
	    result = Category.BODY;
	else if (label.matches("([0-9].?)? *conclusions? *([:].*)?"))
	    result = Category.BODY;
	else if (label.matches("([0-9].?)? *objectives?"))
	    result = Category.BODY;
	else if (label.matches("([0-9].?)? *discussions?( and conclusions)? *([:].*)?"))
	    result = Category.BODY;
	else if (label.matches("([0-9].?)? *limitations?"))
	    result = Category.BODY;
	else if (label.matches("([0-9].?)? *summary"))
	    result = Category.BODY;
	else if (label.matches("([0-9].?)? *disclaimers?"))
	    result = Category.MISC;
	else if (label.matches("([0-9].?)? *acknowledge?ments?( and funding)? *([:].*)?"))
	    result = Category.MISC;
	else if (label.matches("([0-9].?)? *funding( statements?)? *([:].*)?"))
	    result = Category.MISC;
	else if (label.matches("([0-9].?)? *supporting material"))
	    result = Category.SUPPLEMENTAL;
	else if (label.matches("([0-9].?)? *main figure legend"))
	    result = Category.SUPPLEMENTAL;
	else if (label.matches("([0-9].?)? *figures?(( and)? (figure )?legends?)? *([:].*)?"))
	    result = Category.SUPPLEMENTAL;
	else if (label.matches("([0-9].?)? *table(( and)? (figure )?legends?)? *([:].*)?"))
	    result = Category.SUPPLEMENTAL;
	else if (label.matches("([0-9].?)? *competing financial interest statement"))
	    result = Category.MISC;
	else if (label.matches("([0-9].?)? *competing( financial)? interests? *([:].*)?"))
	    result = Category.MISC;
	else if (label.matches("([0-9].?)? *(conflicts?|declarations?) of interests? *([:].*)?"))
	    result = Category.MISC;
	else if (label.matches("([0-9].?)? *declarations? *([:].*)?"))
	    result = Category.MISC;
	else if (label.matches("([0-9].?)? *interest of conflicts? *([:].*)?"))
	    result = Category.MISC;
	else if (label.matches("([0-9].?)? *competing interests?"))
	    result = Category.MISC;
	else if (label.matches("([0-9].?)? *authors?['’]? contributions?.*"))
	    result = Category.MISC;
	else if (label.matches("([0-9].?)? *author['’]s? contributions?.*"))
	    result = Category.MISC;
	else if (label.matches("([0-9].?)? *references?( and notes)? *([:].*)?"))
	    result = Category.REFERENCES;
	else if (label.matches("([0-9].?)? *bibliography"))
	    result = Category.REFERENCES;
	else if (label.matches("([0-9].?)? *contributions?"))
	    result = Category.MISC;
	else if (label.matches("([0-9].?)? *foot ?notes?"))
	    result = Category.MISC;
	else if (label.matches("([0-9].?)? *method(s|ology|ologies)"))
	    result = Category.BODY;
	else if (label.matches("([0-9].?)? *(supplement(al|ary) )?references *[:]?"))
	    result = Category.MISC;
	else if (label.matches("([0-9].?)? *(supplement(al|ary) )?legends *[:]?"))
	    result = Category.MISC;
	else if (label.matches("([0-9].?)? *(supplement(al|ary) )?tables *[:]?"))
	    result = Category.MISC;
	else if (label.matches("([0-9].?)? *(supplement(al|ary) )?figures( and tables)? *[:]?"))
	    result = Category.MISC;
	else if (label.matches("([0-9].?)? *(supplement(al|ary) )?methods *[:]?"))
	    result = Category.MISC;
	else if (label.matches("([0-9].?)? *(supplement(al|ary) )?materials? *[:]?"))
	    result = Category.MISC;
	else if (label.matches("([0-9].?)? *(supplement(al|ary) )?appendix *[:]?"))
	    result = Category.MISC;
	
	if (result != null)
	    logger.debug("*** new section header: " + line.getRawText());
	
	prevLine = line;
	return result;
    }
}
