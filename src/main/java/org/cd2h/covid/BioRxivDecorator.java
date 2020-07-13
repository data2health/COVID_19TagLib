package org.cd2h.covid;

import java.sql.Connection;

import org.apache.log4j.Logger;

import edu.uiowa.NLP_grammar.syntaxTree;
import edu.uiowa.NLP_grammar.syntaxMatch.syntaxMatch;
import edu.uiowa.NLP_grammar.syntaxMatch.syntaxMatchFunction;
import edu.uiowa.NLP_grammar.syntaxMatch.syntaxMatcher;
import edu.uiowa.NLP_grammar.syntaxMatch.comparator.placeNameComparator;
import edu.uiowa.PubMedCentral.comparator.GeoNameComparator;
import edu.uiowa.UMLS.Concept;
import edu.uiowa.UMLS.Semantics;
import edu.uiowa.UMLS.comparators.activityComparator;
import edu.uiowa.UMLS.comparators.anatomicalStructureComparator;
import edu.uiowa.UMLS.comparators.biologicalFunctionComparator;
import edu.uiowa.UMLS.comparators.bodyPartComparator;
import edu.uiowa.UMLS.comparators.conceptComparator;
import edu.uiowa.UMLS.comparators.conceptualRelationshipComparator;
import edu.uiowa.UMLS.comparators.disciplineComparator;
import edu.uiowa.UMLS.comparators.diseaseComparator;
import edu.uiowa.UMLS.comparators.entityComparator;
import edu.uiowa.UMLS.comparators.eventComparator;
import edu.uiowa.UMLS.comparators.findingComparator;
import edu.uiowa.UMLS.comparators.functionalConceptComparator;
import edu.uiowa.UMLS.comparators.functionalRelationshipComparator;
import edu.uiowa.UMLS.comparators.groupAttributeComparator;
import edu.uiowa.UMLS.comparators.groupComparator;
import edu.uiowa.UMLS.comparators.humanProcessComparator;
import edu.uiowa.UMLS.comparators.injuryComparator;
import edu.uiowa.UMLS.comparators.intellectualProductComparator;
import edu.uiowa.UMLS.comparators.languageComparator;
import edu.uiowa.UMLS.comparators.manufacturedObjectComparator;
import edu.uiowa.UMLS.comparators.naturalProcessComparator;
import edu.uiowa.UMLS.comparators.organicChemicalComparator;
import edu.uiowa.UMLS.comparators.organismAttributeComparator;
import edu.uiowa.UMLS.comparators.organismComparator;
import edu.uiowa.UMLS.comparators.organizationComparator;
import edu.uiowa.UMLS.comparators.pathologicalFunctionComparator;
import edu.uiowa.UMLS.comparators.physicalRelationshipComparator;
import edu.uiowa.UMLS.comparators.physiologicalFunctionComparator;
import edu.uiowa.UMLS.comparators.processComparator;
import edu.uiowa.UMLS.comparators.qualitativeConceptComparator;
import edu.uiowa.UMLS.comparators.quantitativeConceptComparator;
import edu.uiowa.UMLS.comparators.relationshipComparator;
import edu.uiowa.UMLS.comparators.spatialConceptComparator;
import edu.uiowa.UMLS.comparators.spatialRelationshipComparator;
import edu.uiowa.UMLS.comparators.substanceComparator;
import edu.uiowa.UMLS.comparators.temporalConceptComparator;
import edu.uiowa.UMLS.comparators.temporalRelationshipComparator;
import edu.uiowa.UMLS.comparators.transcriptionFactorComparator;
import edu.uiowa.entity.named.PlaceName;
import edu.uiowa.extraction.Decorator;

public class BioRxivDecorator extends Decorator {
    static Logger logger = Logger.getLogger(BioRxivDecorator.class);
    
    Connection conn = null;

    public BioRxivDecorator(Connection conn) throws Exception {
	super();
	logger.info("BioRxivDecorator");
	this.conn = conn;
	initialize(conn);
    }
    
    private void initialize(Connection conn) throws Exception {
//	Concept.initialize(conn, true);
	PlaceName.initialize();
    }

    @Override
    public boolean decorateTree(syntaxTree theTree) throws Exception {
//	decorateTree(theTree, "Activity", new activityComparator());
//	decorateTree(theTree, "AnatomicalStructure", new anatomicalStructureComparator());
//	decorateTree(theTree, "BiologicalFunction", new biologicalFunctionComparator());
//	decorateTree(theTree, "BodyPart", new bodyPartComparator());
//	decorateTree(theTree, "Concept", new conceptComparator());
//	decorateTree(theTree, "TemporalConcept", new temporalConceptComparator());
//	decorateTree(theTree, "QualitativeConcept", new qualitativeConceptComparator());
//	decorateTree(theTree, "QuantitativeConcept", new quantitativeConceptComparator());
//	decorateTree(theTree, "FunctionalConcept", new functionalConceptComparator());
//	decorateTree(theTree, "SpatialConcept", new spatialConceptComparator());
//	decorateTree(theTree, "ConceptualRelationship", new conceptualRelationshipComparator());
//	decorateTree(theTree, "Discipline", new disciplineComparator());
//	decorateTree(theTree, "Disease", new diseaseComparator());
//	decorateTree(theTree, "Entity", new entityComparator());
//	decorateTree(theTree, "Event", new eventComparator());
//	decorateTree(theTree, "Finding", new findingComparator());
//	decorateTree(theTree, "FunctionalRelationship", new functionalRelationshipComparator());
//	decorateTree(theTree, "Group", new groupComparator());
//	decorateTree(theTree, "GroupAttribute", new groupAttributeComparator());
//	decorateTree(theTree, "HumanProcess", new humanProcessComparator());
//	decorateTree(theTree, "Injury", new injuryComparator());
//	decorateTree(theTree, "IntellectualProduct", new intellectualProductComparator());
//	decorateTree(theTree, "Language", new languageComparator());
//	decorateTree(theTree, "ManufacuredObject", new manufacturedObjectComparator());
//	decorateTree(theTree, "NaturalProcess", new naturalProcessComparator());
//	decorateTree(theTree, "OrganicChemical", new organicChemicalComparator());
//	decorateTree(theTree, "Organism", new organismComparator());
//	decorateTree(theTree, "OrganismAttribute", new organismAttributeComparator());
//	decorateTree(theTree, "Organization", new organizationComparator());
//	decorateTree(theTree, "PathologicalFunction", new pathologicalFunctionComparator());
//	decorateTree(theTree, "PhysicalRelationship", new physicalRelationshipComparator());
//	decorateTree(theTree, "PhysiologicalFunction", new physiologicalFunctionComparator());
//	decorateTree(theTree, "Process", new processComparator());
//	decorateTree(theTree, "Relationship", new relationshipComparator());
//	decorateTree(theTree, "SpatialRelationship", new spatialRelationshipComparator());
//	decorateTree(theTree, "Substance", new substanceComparator());
//	decorateTree(theTree, "TemporalRelationship", new temporalRelationshipComparator());
//	decorateTree(theTree, "TranscriptionFactor", new transcriptionFactorComparator());

	decorateTree(theTree, "Geoname", new GeoNameComparator());

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
	theMatcher.registerFunction("isActivity", new activityComparator());
	theMatcher.registerFunction("isAnatomicalStructure", new anatomicalStructureComparator());
	theMatcher.registerFunction("isBiologicalFunction", new biologicalFunctionComparator());
	theMatcher.registerFunction("isBodyPart", new bodyPartComparator());
	theMatcher.registerFunction("isConcept", new conceptComparator());
	theMatcher.registerFunction("isTemporalConcept", new temporalConceptComparator());
	theMatcher.registerFunction("isQualitativeConcept", new qualitativeConceptComparator());
	theMatcher.registerFunction("isQuantitativeConcept", new quantitativeConceptComparator());
	theMatcher.registerFunction("isFunctionalConcept", new functionalConceptComparator());
	theMatcher.registerFunction("isSpatialConcept", new spatialConceptComparator());
	theMatcher.registerFunction("isConceptualRelationship", new conceptualRelationshipComparator());
	theMatcher.registerFunction("isDiscipline", new disciplineComparator());
	theMatcher.registerFunction("isDisease", new diseaseComparator());
	theMatcher.registerFunction("isEntity", new entityComparator());
	theMatcher.registerFunction("isEvent", new eventComparator());
	theMatcher.registerFunction("isFinding", new findingComparator());
	theMatcher.registerFunction("isFunctionalRelationship", new functionalRelationshipComparator());
	theMatcher.registerFunction("isGroup", new groupComparator());
	theMatcher.registerFunction("isGroupAttribute", new groupAttributeComparator());
	theMatcher.registerFunction("isHumanProcess", new humanProcessComparator());
	theMatcher.registerFunction("isInjury", new injuryComparator());
	theMatcher.registerFunction("isIntellectualProduct", new intellectualProductComparator());
	theMatcher.registerFunction("isLanguage", new languageComparator());
	theMatcher.registerFunction("isManufacturedObject", new manufacturedObjectComparator());
	theMatcher.registerFunction("isNaturalProcess", new naturalProcessComparator());
	theMatcher.registerFunction("isOrganicChemical", new organicChemicalComparator());
	theMatcher.registerFunction("isOrganism", new organismComparator());
	theMatcher.registerFunction("isOrganismAttribute", new organismAttributeComparator());
	theMatcher.registerFunction("isOrganization", new organizationComparator());
	theMatcher.registerFunction("isPathologicalFunction", new pathologicalFunctionComparator());
	theMatcher.registerFunction("isPhysicalRelationship", new physicalRelationshipComparator());
	theMatcher.registerFunction("isPhysiologicalFunction", new physiologicalFunctionComparator());
	theMatcher.registerFunction("isProcess", new processComparator());
	theMatcher.registerFunction("isRelationship", new relationshipComparator());
	theMatcher.registerFunction("isSpatialRelationship", new spatialRelationshipComparator());
	theMatcher.registerFunction("isSubstance", new substanceComparator());
	theMatcher.registerFunction("isTemporalRelationship", new temporalRelationshipComparator());
	theMatcher.registerFunction("isTranscriptionFactor", new transcriptionFactorComparator());

//	theMatcher.registerFunction("isPlaceName", new placeNameComparator());
	theMatcher.registerFunction("isGeoName", new GeoNameComparator());
	
	logger.info("pattern: " + pattern + "\tentity: " + entity + "\ttree: " + theTree.trimmedPhraseAsString());
	if (theMatcher.hasMatch(theTree)) {
	    logger.info("<<<<< matched >>>>>");
	    for (syntaxMatch theMatchNode : theMatcher.matches()) {
		if (entity.equals("*"))
		    theMatchNode.getPhrase().setEntity(theMatchNode.getPhrase().getEntityClass());
		else if (Semantics.getByEntityName(entity) == null)
		    theMatchNode.getPhrase().setEntity(entity);
		else {
		    theMatchNode.getPhrase().setEntity(Semantics.getByEntityName(entity));
		    logger.info("entity: " + entity + "\tmatch node: " + theMatchNode.getPhrase().treeString() + "\t" + theMatchNode.getPhrase().getParent().getFragmentString(true,true) + "\t" + theMatchNode.getPhrase().getUMLSSemantics());
		}
		if (theMatchNode.getPhrase().getFragmentStringVector2().size() == 0) {
		    logger.info("** fragment is empty!");
		} else
		    logger.info("fragment: " + theMatchNode.getPhrase().getFragmentStringVector2().firstElement());
//		for (int i = 1; i <= theMatchNode.matchCount(); i++)
//		    logger.info("\tmatch slot [" + i + "]: " + theMatchNode.getMatch(i).treeString());
	    }
	}

	return true;
    }
}
