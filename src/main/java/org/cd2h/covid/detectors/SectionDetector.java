package org.cd2h.covid.detectors;

import org.apache.log4j.Logger;
import org.cd2h.covid.model.Line;
import org.cd2h.covid.model.Section;

public class SectionDetector {
    static Logger logger = Logger.getLogger(Section.class);
    Line prevLine = null;

    public boolean newSection(Line line) {
	String label = line.getRawText().toLowerCase();
	boolean result = false;
	
	if (label.matches("abstract *([:].*)?"))
	    result = true;
	else if (label.matches("summary *\\(.*\\)"))
	    result = true;
	else if (label.matches("([0-9].?)? *introduction"))
	    result = true;
	else if (label.matches("([0-9].?)? *importance"))
	    result = true;
	else if (label.matches("([0-9].?)? *main text *([:].*)?"))
	    result = true;
	else if (label.matches("([0-9].?)? *results?( and (discussions?|analys(is|es)))?"))
	    result = true;
	else if (label.matches("([0-9].?)? *materials?( and methods?)?"))
	    result = true;
	else if (label.matches("([0-9].?)? *methods?( and materials?)?"))
	    result = true;
	else if (label.matches("([0-9].?)? *conclusions?"))
	    result = true;
	else if (label.matches("([0-9].?)? *objectives?"))
	    result = true;
	else if (label.matches("([0-9].?)? *discussion"))
	    result = true;
	else if (label.matches("([0-9].?)? *limitations?"))
	    result = true;
	else if (label.matches("([0-9].?)? *summary"))
	    result = true;
	else if (label.matches("([0-9].?)? *disclaimers?"))
	    result = true;
	else if (label.matches("([0-9].?)? *acknowledge?ments?( and funding)? *([:].*)?"))
	    result = true;
	else if (label.matches("([0-9].?)? *funding( statements?)?"))
	    result = true;
	else if (label.matches("([0-9].?)? *supporting material"))
	    result = true;
	else if (label.matches("([0-9].?)? *figures?(( and)? (figure )?legends?)?"))
	    result = true;
	else if (label.matches("([0-9].?)? *competing financial interest statement"))
	    result = true;
	else if (label.matches("([0-9].?)? *(conflicts?|declaration) of interests? *([:].*)?"))
	    result = true;
	else if (label.matches("([0-9].?)? *interest of conflicts? *([:].*)?"))
	    result = true;
	else if (label.matches("([0-9].?)? *competing interests?"))
	    result = true;
	else if (label.matches("([0-9].?)? *authors?['’]? contributions?.*"))
	    result = true;
	else if (label.matches("([0-9].?)? *references? *([:].*)?"))
	    result = true;
	else if (label.matches("([0-9].?)? *contributions?"))
	    result = true;
	else if (label.matches("([0-9].?)? *foot ?notes?"))
	    result = true;
	else if (label.matches("([0-9].?)? *method(s|ology|ologies)"))
	    result = true;
	else if (label.matches("([0-9].?)? *(supplementary )?references *[:]?"))
	    result = true;
	else if (label.matches("([0-9].?)? *(supplementary )?legends *[:]?"))
	    result = true;
	else if (label.matches("([0-9].?)? *(supplementary )?tables *[:]?"))
	    result = true;
	else if (label.matches("([0-9].?)? *(supplementary )?figures *[:]?"))
	    result = true;
	else if (label.matches("([0-9].?)? *(supplementary )?methods *[:]?"))
	    result = true;
	else if (label.matches("([0-9].?)? *(supplementary )?materials? *[:]?"))
	    result = true;
	
	if (result)
	    logger.debug("*** new section header: " + line.getRawText());
	
	prevLine = line;
	return result;
    }
}
