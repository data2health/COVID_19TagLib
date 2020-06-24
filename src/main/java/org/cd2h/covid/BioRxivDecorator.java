package org.cd2h.covid;

import java.sql.Connection;

import org.apache.log4j.Logger;

import edu.uiowa.NLP_grammar.syntaxTree;
import edu.uiowa.NLP_grammar.syntaxMatch.syntaxMatch;
import edu.uiowa.NLP_grammar.syntaxMatch.syntaxMatchFunction;
import edu.uiowa.NLP_grammar.syntaxMatch.syntaxMatcher;
import edu.uiowa.UMLS.Concept;
import edu.uiowa.UMLS.comparators.anatomicalStructureComparator;
import edu.uiowa.UMLS.comparators.biologicalFunctionComparator;
import edu.uiowa.UMLS.comparators.bodyPartComparator;
import edu.uiowa.UMLS.comparators.diseaseComparator;
import edu.uiowa.UMLS.comparators.eventComparator;
import edu.uiowa.UMLS.comparators.findingComparator;
import edu.uiowa.UMLS.comparators.injuryComparator;
import edu.uiowa.UMLS.comparators.manufacturedObjectComparator;
import edu.uiowa.UMLS.comparators.organicChemicalComparator;
import edu.uiowa.UMLS.comparators.organismComparator;
import edu.uiowa.UMLS.comparators.pathologicalFunctionComparator;
import edu.uiowa.UMLS.comparators.physiologicalFunctionComparator;
import edu.uiowa.UMLS.comparators.transcriptionFactorComparator;
import edu.uiowa.extraction.Decorator;

public class BioRxivDecorator extends Decorator {
    static Logger logger = Logger.getLogger(BioRxivDecorator.class);
    
    Connection conn = null;

    public BioRxivDecorator(Connection conn) throws Exception {
	super();
	this.conn = conn;
	initialize(conn);
    }
    
    private void initialize(Connection conn) throws Exception {
	Concept.initialize(conn);
    }

    @Override
    public boolean decorateTree(syntaxTree theTree) throws Exception {
	decorateTree(theTree, "AnatomicalFunction", new anatomicalStructureComparator());
	decorateTree(theTree, "BiologicalFunction", new biologicalFunctionComparator());
//	decorateTree(theTree, "BodyPart", new bodyPartComparator());
	decorateTree(theTree, "Disease", new diseaseComparator());
	decorateTree(theTree, "Event", new eventComparator());
	decorateTree(theTree, "Finding", new findingComparator());
	decorateTree(theTree, "Injury", new injuryComparator());
	decorateTree(theTree, "ManufacuredObject", new manufacturedObjectComparator());
	decorateTree(theTree, "OrganicChemical", new organicChemicalComparator());
	decorateTree(theTree, "Organism", new organismComparator());
	decorateTree(theTree, "PathologicalFunction", new pathologicalFunctionComparator());
	decorateTree(theTree, "PhysiologicalFunction", new physiologicalFunctionComparator());
	decorateTree(theTree, "TranscriptionFactor", new transcriptionFactorComparator());
	return false;
    }

    private boolean decorateTree(syntaxTree theTree, String entity, syntaxMatchFunction function) throws Exception {
	return decorateTree2(theTree, entity, entity, function);
    }
    
    private boolean decorateTree2(syntaxTree theTree, String entity, String entityClass, syntaxMatchFunction function) throws Exception {
	decorateTree(theTree, "is" + entity + "(NN)", entityClass, function);
	decorateTree(theTree, "is" + entity + "(NNP)", entityClass, function);
	decorateTree(theTree, "is" + entity + "(NNPS)", entityClass, function);
	decorateTree(theTree, "is" + entity + "(NNS)", entityClass, function);
	decorateTree(theTree, "is" + entity + "(NP)", entityClass, function);	
	decorateTree(theTree, "is" + entity + "(NAC)", entityClass, function);	
	decorateTree(theTree, "is" + entity + "(NX)", entityClass, function);	
	return true;
    }
    
    private boolean decorateTree(syntaxTree theTree, String pattern, String entity, syntaxMatchFunction function) throws Exception {
	syntaxMatcher theMatcher = new syntaxMatcher(pattern);
	theMatcher.registerFunction(pattern, function);
	theMatcher.registerFunction("isAnatomicalStructure", new anatomicalStructureComparator());
	theMatcher.registerFunction("isBodyPart", new bodyPartComparator());
	theMatcher.registerFunction("isDisease", new diseaseComparator());
	theMatcher.registerFunction("isEvent", new eventComparator());
	theMatcher.registerFunction("isFinding", new findingComparator());
	theMatcher.registerFunction("isInjury", new injuryComparator());
	theMatcher.registerFunction("isManufacturedObject", new manufacturedObjectComparator());
	theMatcher.registerFunction("isOrganicChemical", new organicChemicalComparator());
	theMatcher.registerFunction("isOrganism", new organismComparator());
	theMatcher.registerFunction("isPathologicalFunction", new pathologicalFunctionComparator());
	theMatcher.registerFunction("isPhysiologicalFunction", new physiologicalFunctionComparator());
	theMatcher.registerFunction("isTranscriptionFactor", new transcriptionFactorComparator());
	theMatcher.registerFunction("isBiologicalFunction", new biologicalFunctionComparator());
	
	if (theMatcher.hasMatch(theTree)) {
	    logger.debug("<<<<< match >>>>>");
	    for (syntaxMatch theMatchNode : theMatcher.matches()) {
		if (theMatchNode.getPhrase().getEntity() != null)
		    continue;
		if (entity.equals("*"))
		    theMatchNode.getPhrase().setEntity(theMatchNode.getPhrase().getEntityClass());
		else
		    theMatchNode.getPhrase().setEntity(entity);
		logger.debug("match node: " + theMatchNode.getPhrase().treeString());
		if (theMatchNode.getPhrase().getFragmentStringVector2().size() == 0) {
		    logger.debug("** fragment is empty!");
		} else
		    logger.debug("fragment: " + theMatchNode.getPhrase().getFragmentStringVector2().firstElement());
		for (int i = 1; i <= theMatchNode.matchCount(); i++)
		    logger.debug("\tmatch slot [" + i + "]: " + theMatchNode.getMatch(i).treeString());
	    }
	}

	return true;
    }
}
