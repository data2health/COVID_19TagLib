package org.cd2h.covid;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import edu.uiowa.GRID.Address;
import edu.uiowa.GRID.Institute;
import edu.uiowa.GeoNames.GeoName;
import edu.uiowa.NLP_grammar.syntaxTree;
import edu.uiowa.NLP_grammar.syntaxMatch.syntaxMatch;
import edu.uiowa.NLP_grammar.syntaxMatch.syntaxMatcher;
import edu.uiowa.NLP_grammar.syntaxMatch.comparator.emailComparator;
import edu.uiowa.NLP_grammar.syntaxMatch.comparator.entityComparator;
import edu.uiowa.NLP_grammar.syntaxMatch.comparator.urlComparator;
import edu.uiowa.PubMedCentral.comparator.PersonComparator;
import edu.uiowa.PubMedCentral.entity.Activity;
import edu.uiowa.PubMedCentral.entity.AnatomicalStructure;
import edu.uiowa.PubMedCentral.entity.BiologicalFunction;
import edu.uiowa.PubMedCentral.entity.BodyPart;
import edu.uiowa.PubMedCentral.entity.ClinicalTrialRegistration;
import edu.uiowa.PubMedCentral.entity.Collaboration;
import edu.uiowa.PubMedCentral.entity.ConceptualRelationship;
import edu.uiowa.PubMedCentral.entity.Discipline;
import edu.uiowa.PubMedCentral.entity.Disease;
import edu.uiowa.PubMedCentral.entity.Event;
import edu.uiowa.PubMedCentral.entity.Finding;
import edu.uiowa.PubMedCentral.entity.FunctionalConcept;
import edu.uiowa.PubMedCentral.entity.FunctionalRelationship;
import edu.uiowa.PubMedCentral.entity.Group;
import edu.uiowa.PubMedCentral.entity.GroupAttribute;
import edu.uiowa.PubMedCentral.entity.HumanProcess;
import edu.uiowa.PubMedCentral.entity.Injury;
import edu.uiowa.PubMedCentral.entity.IntellectualProduct;
import edu.uiowa.PubMedCentral.entity.Language;
import edu.uiowa.PubMedCentral.entity.ManufacturedObject;
import edu.uiowa.PubMedCentral.entity.NaturalProcess;
import edu.uiowa.PubMedCentral.entity.OrganicChemical;
import edu.uiowa.PubMedCentral.entity.Organism;
import edu.uiowa.PubMedCentral.entity.OrganismAttribute;
import edu.uiowa.PubMedCentral.entity.Organization;
import edu.uiowa.PubMedCentral.entity.PathologicalFunction;
import edu.uiowa.PubMedCentral.entity.Person;
import edu.uiowa.PubMedCentral.entity.PhysicalRelationship;
import edu.uiowa.PubMedCentral.entity.PhysiologicalFunction;
import edu.uiowa.PubMedCentral.entity.PlaceName;
import edu.uiowa.PubMedCentral.entity.QualitativeConcept;
import edu.uiowa.PubMedCentral.entity.QuantitativeConcept;
import edu.uiowa.PubMedCentral.entity.Relationship;
import edu.uiowa.PubMedCentral.entity.Resource;
import edu.uiowa.PubMedCentral.entity.SpatialConcept;
import edu.uiowa.PubMedCentral.entity.SpatialRelationship;
import edu.uiowa.PubMedCentral.entity.Substance;
import edu.uiowa.PubMedCentral.entity.TemporalConcept;
import edu.uiowa.PubMedCentral.entity.TemporalRelationship;
import edu.uiowa.PubMedCentral.entity.TranscriptionFactor;
import edu.uiowa.concept.Concept;
import edu.uiowa.concept.ExhaustiveVectorConceptRecognizer;
import edu.uiowa.concept.detector.GRIDDetector;
import edu.uiowa.concept.detector.GeoNamesDetector;
import edu.uiowa.concept.detector.UMLSDetector;
import edu.uiowa.entity.Entity;
import edu.uiowa.extraction.LocalProperties;
import edu.uiowa.extraction.Template;
import edu.uiowa.extraction.TemplateInstantiator;
import edu.uiowa.lex.Sentence;
import edu.uiowa.lex.basicLexerToken;

public class BioRxivInstantiator extends TemplateInstantiator {
    Hashtable<String, Person> personHash = new Hashtable<String, Person>();
    Hashtable<String, Resource> activityHash = new Hashtable<String, Resource>();
    Hashtable<String, Resource> anatomicalStructureHash = new Hashtable<String, Resource>();
    Hashtable<String, Resource> biologicalFunctionHash = new Hashtable<String, Resource>();
    Hashtable<String, Resource> bodyPartHash = new Hashtable<String, Resource>();
    Hashtable<String, Resource> conceptHash = new Hashtable<String, Resource>();
    Hashtable<String, Resource> temporalConceptHash = new Hashtable<String, Resource>();
    Hashtable<String, Resource> qualitativeConceptHash = new Hashtable<String, Resource>();
    Hashtable<String, Resource> quantitativeConceptHash = new Hashtable<String, Resource>();
    Hashtable<String, Resource> functionalConceptHash = new Hashtable<String, Resource>();
    Hashtable<String, Resource> spatialConceptHash = new Hashtable<String, Resource>();
    Hashtable<String, Resource> disciplineHash = new Hashtable<String, Resource>();
    Hashtable<String, Resource> diseaseHash = new Hashtable<String, Resource>();
    Hashtable<String, Resource> entityHash = new Hashtable<String, Resource>();
    Hashtable<String, Resource> eventHash = new Hashtable<String, Resource>();
    Hashtable<String, Resource> findingHash = new Hashtable<String, Resource>();
    Hashtable<String, Resource> functionalRelationshipHash = new Hashtable<String, Resource>();
    Hashtable<String, Resource> groupHash = new Hashtable<String, Resource>();
    Hashtable<String, Resource> groupAttributeHash = new Hashtable<String, Resource>();
    Hashtable<String, Resource> humanProcessHash = new Hashtable<String, Resource>();
    Hashtable<String, Resource> injuryHash = new Hashtable<String, Resource>();
    Hashtable<String, Resource> intellectualProductHash = new Hashtable<String, Resource>();
    Hashtable<String, Resource> languageHash = new Hashtable<String, Resource>();
    Hashtable<String, Resource> manufacturedObjectHash = new Hashtable<String, Resource>();
    Hashtable<String, Resource> naturalProcessHash = new Hashtable<String, Resource>();
    Hashtable<String, Resource> organicChemicalHash = new Hashtable<String, Resource>();
    Hashtable<String, Resource> organismHash = new Hashtable<String, Resource>();
    Hashtable<String, Resource> organismAttributeHash = new Hashtable<String, Resource>();
    Hashtable<String, Resource> organizationHash = new Hashtable<String, Resource>();
    Hashtable<String, Resource> pathologicalFunctionHash = new Hashtable<String, Resource>();
    Hashtable<String, Resource> physicalRelationshipHash = new Hashtable<String, Resource>();
    Hashtable<String, Resource> physiologicalFunctionHash = new Hashtable<String, Resource>();
    Hashtable<String, Resource> processHash = new Hashtable<String, Resource>();
    Hashtable<String, Resource> relationshipHash = new Hashtable<String, Resource>();
    Hashtable<String, Resource> spatialRelationshipHash = new Hashtable<String, Resource>();
    Hashtable<String, Resource> substanceHash = new Hashtable<String, Resource>();
    Hashtable<String, Resource> techniqueHash = new Hashtable<String, Resource>();
    Hashtable<String, Resource> temporalRelationshipHash = new Hashtable<String, Resource>();
    Hashtable<String, Resource> placeNameHash = new Hashtable<String, Resource>();
    Hashtable<String, Resource> transcriptionFactorHash = new Hashtable<String, Resource>();
    
    PersonComparator personComparator = new PersonComparator();
    String doi = null;

    public BioRxivInstantiator(LocalProperties prop_file, Connection conn, String  doi) throws ClassNotFoundException, SQLException {
	super(prop_file, conn);
	this.doi = doi;
	initialize();
    }
    
    private void initialize() {
	breakToken.put("(", "");
	breakToken.put(")", "");
	breakToken.put(":", "");
	breakToken.put("award", "");
	breakToken.put("Award", "");
	breakToken.put("grant", "");
	breakToken.put("Grant", "");
	breakToken.put("contract", "");
	breakToken.put("Contract", "");
	breakToken.put("awards", "");
	breakToken.put("Awards", "");
	breakToken.put("grants", "");
	breakToken.put("Grants", "");
	breakToken.put("contracts", "");
	breakToken.put("Contracts", "");
    }

    @Override
    protected void instantiateEntity(int id, syntaxTree constituent, Template template) throws Exception {
	// not used in this application
    }

   @Override
    protected void instantiateEntity(String id, syntaxTree constituent, Template template) throws Exception {
	logger.debug("matched template: " + template);
	Resource resource = null;
	logger.info("instantiating template relation: " + template.relation);
	switch (template.relation) {
	case "clinical_trial":
	    ClinicalTrialRegistration clinicalTrial = clinicalTrialMatch(constituent, template.tgrep);
	    if (clinicalTrial == null) {
		// unlike some of the other patterns, these drive on specific strings in the text, so a null match is frequent
		logger.info("clinical_trial instantiation failed! : " + clinicalTrial);
		logger.info("\t\t" + template.tgrep);
		logger.info("\t\t" + constituent.getFragmentString());
		logger.info("\t\t" + constituent.treeString());
		break;
	    }
	    storeClinicalTrial(doi, clinicalTrial);
	    constituent.setEntityClass("ClinicalTrial");
	    bindNamedEntity(constituent, template, clinicalTrial);
	    break;
	case "activity":
	     resource = resourceMatch(constituent, template);
	    if (resource == null) {
		logger.info("activity instantiation failed! : " + resource);
		logger.info("\t\t" + template.tgrep);
		logger.info("\t\t" + constituent.getFragmentString());
		logger.info("\t\t" + constituent.treeString());
		break;
	    }
	    storeResource(doi, resource, activityHash, "activity");
	    constituent.setEntityClass("Activity");
	    bindNamedEntity(constituent, template, resource);
	    break;
	case "anatomical_structure":
	     resource = resourceMatch(constituent, template);
	    if (resource == null) {
		logger.info("anatomicalStructure instantiation failed! : " + resource);
		logger.info("\t\t" + template.tgrep);
		logger.info("\t\t" + constituent.getFragmentString());
		logger.info("\t\t" + constituent.treeString());
		break;
	    }
	    storeResource(doi, resource, anatomicalStructureHash, "anatomical_structure");
	    constituent.setEntityClass("AnatomicalStructure");
	    bindNamedEntity(constituent, template, resource);
	    break;
	case "biological_function":
	    resource = resourceMatch(constituent, template);
	    if (resource == null) {
		logger.info("biologicalFunction instantiation failed! : " + resource);
		logger.info("\t\t" + template.tgrep);
		logger.info("\t\t" + constituent.getFragmentString());
		logger.info("\t\t" + constituent.treeString());
		break;
	    }
	    storeResource(doi, resource, biologicalFunctionHash, "biological_function");
	    constituent.setEntityClass("BiologicalFunction");
	    bindNamedEntity(constituent, template, resource);
	    break;
	case "body_part":
	    resource = resourceMatch(constituent, template);
	    if (resource == null) {
		logger.info("bodyPart instantiation failed! : " + resource);
		logger.info("\t\t" + template.tgrep);
		logger.info("\t\t" + constituent.getFragmentString());
		logger.info("\t\t" + constituent.treeString());
		break;
	    }
	    storeResource(doi, resource, bodyPartHash, "body_part");
	    constituent.setEntityClass("BodyPart");
	    bindNamedEntity(constituent, template, resource);
	    break;
	case "concept":
		   resource = resourceMatch(constituent, template);
		    if (resource == null) {
			logger.info("concept instantiation failed! : " + resource);
			logger.info("\t\t" + template.tgrep);
			logger.info("\t\t" + constituent.getFragmentString());
			logger.info("\t\t" + constituent.treeString());
			break;
		    }
		    storeResource(doi, resource, conceptHash, "concept");
		    constituent.setEntityClass("Concept");
		    bindNamedEntity(constituent, template, resource);
		    break;
	case "temporal_concept":
	   resource = resourceMatch(constituent, template);
	    if (resource == null) {
		logger.info("temporal_concept instantiation failed! : " + resource);
		logger.info("\t\t" + template.tgrep);
		logger.info("\t\t" + constituent.getFragmentString());
		logger.info("\t\t" + constituent.treeString());
		break;
	    }
	    storeResource(doi, resource, temporalConceptHash, "temporal_concept");
	    constituent.setEntityClass("TemporalConcept");
	    bindNamedEntity(constituent, template, resource);
	    break;
	case "qualitative_concept":
		   resource = resourceMatch(constituent, template);
		    if (resource == null) {
			logger.info("qualitative_concept instantiation failed! : " + resource);
			logger.info("\t\t" + template.tgrep);
			logger.info("\t\t" + constituent.getFragmentString());
			logger.info("\t\t" + constituent.treeString());
			break;
		    }
		    storeResource(doi, resource, qualitativeConceptHash, "qualitative_concept");
		    constituent.setEntityClass("QualitativeConcept");
		    bindNamedEntity(constituent, template, resource);
		    break;
	case "quantitative_concept":
		   resource = resourceMatch(constituent, template);
		    if (resource == null) {
			logger.info("quantitative_concept instantiation failed! : " + resource);
			logger.info("\t\t" + template.tgrep);
			logger.info("\t\t" + constituent.getFragmentString());
			logger.info("\t\t" + constituent.treeString());
			break;
		    }
		    storeResource(doi, resource, quantitativeConceptHash, "quantitative_concept");
		    constituent.setEntityClass("QuantitativeConcept");
		    bindNamedEntity(constituent, template, resource);
		    break;
	case "functional_concept":
		   resource = resourceMatch(constituent, template);
		    if (resource == null) {
			logger.info("functional_concept instantiation failed! : " + resource);
			logger.info("\t\t" + template.tgrep);
			logger.info("\t\t" + constituent.getFragmentString());
			logger.info("\t\t" + constituent.treeString());
			break;
		    }
		    storeResource(doi, resource, functionalConceptHash, "functional_concept");
		    constituent.setEntityClass("FunctionalConcept");
		    bindNamedEntity(constituent, template, resource);
		    break;
	case "spatial_concept":
		   resource = resourceMatch(constituent, template);
		    if (resource == null) {
			logger.info("spatial_concept instantiation failed! : " + resource);
			logger.info("\t\t" + template.tgrep);
			logger.info("\t\t" + constituent.getFragmentString());
			logger.info("\t\t" + constituent.treeString());
			break;
		    }
		    storeResource(doi, resource, spatialConceptHash, "spatial_concept");
		    constituent.setEntityClass("SpatialConcept");
		    bindNamedEntity(constituent, template, resource);
		    break;
	case "discipline":
		   resource = resourceMatch(constituent, template);
		    if (resource == null) {
			logger.info("discipline instantiation failed! : " + resource);
			logger.info("\t\t" + template.tgrep);
			logger.info("\t\t" + constituent.getFragmentString());
			logger.info("\t\t" + constituent.treeString());
			break;
		    }
		    storeResource(doi, resource, disciplineHash, "discipline");
		    constituent.setEntityClass("Discipline");
		    bindNamedEntity(constituent, template, resource);
		    break;
	case "disease":
		   resource = resourceMatch(constituent, template);
		    if (resource == null) {
			logger.info("disease instantiation failed! : " + resource);
			logger.info("\t\t" + template.tgrep);
			logger.info("\t\t" + constituent.getFragmentString());
			logger.info("\t\t" + constituent.treeString());
			break;
		    }
		    storeResource(doi, resource, diseaseHash, "disease");
		    constituent.setEntityClass("Disease");
		    bindNamedEntity(constituent, template, resource);
		    break;
	case "entity":
	    resource = resourceMatch(constituent, template);
	    if (resource == null) {
		logger.info("entity instantiation failed! : " + resource);
		logger.info("\t\t" + template.tgrep);
		logger.info("\t\t" + constituent.getFragmentString());
		logger.info("\t\t" + constituent.treeString());
		break;
	    }
	    storeResource(doi, resource, entityHash, "entity");
	    constituent.setEntityClass("Entity");
	    bindNamedEntity(constituent, template, resource);
	    break;
	case "event":
	    resource = resourceMatch(constituent, template);
	    if (resource == null) {
		logger.info("event instantiation failed! : " + resource);
		logger.info("\t\t" + template.tgrep);
		logger.info("\t\t" + constituent.getFragmentString());
		logger.info("\t\t" + constituent.treeString());
		break;
	    }
	    storeResource(doi, resource, eventHash, "event");
	    constituent.setEntityClass("Event");
	    bindNamedEntity(constituent, template, resource);
	    break;
	case "finding":
	    resource = resourceMatch(constituent, template);
	    if (resource == null) {
		logger.info("finding instantiation failed! : " + resource);
		logger.info("\t\t" + template.tgrep);
		logger.info("\t\t" + constituent.getFragmentString());
		logger.info("\t\t" + constituent.treeString());
		break;
	    }
	    storeResource(doi, resource, findingHash, "finding");
	    constituent.setEntityClass("Event");
	    bindNamedEntity(constituent, template, resource);
	    break;
	case "functional_relationship":
	    resource = resourceMatch(constituent, template);
	    if (resource == null) {
		logger.info("functional_relationship instantiation failed! : " + resource);
		logger.info("\t\t" + template.tgrep);
		logger.info("\t\t" + constituent.getFragmentString());
		logger.info("\t\t" + constituent.treeString());
		break;
	    }
	    storeResource(doi, resource, functionalRelationshipHash, "functional_relationship");
	    constituent.setEntityClass("FunctionalRelationship");
	    bindNamedEntity(constituent, template, resource);
	    break;
	case "group":
	    resource = resourceMatch(constituent, template);
	    if (resource == null) {
		logger.info("group instantiation failed! : " + resource);
		logger.info("\t\t" + template.tgrep);
		logger.info("\t\t" + constituent.getFragmentString());
		logger.info("\t\t" + constituent.treeString());
		break;
	    }
	    storeResource(doi, resource, groupHash, "grp");
	    constituent.setEntityClass("Group");
	    bindNamedEntity(constituent, template, resource);
	    break;
	case "group_attribute":
	    resource = resourceMatch(constituent, template);
	    if (resource == null) {
		logger.info("group_attribute instantiation failed! : " + resource);
		logger.info("\t\t" + template.tgrep);
		logger.info("\t\t" + constituent.getFragmentString());
		logger.info("\t\t" + constituent.treeString());
		break;
	    }
	    storeResource(doi, resource, groupAttributeHash, "group_attribute");
	    constituent.setEntityClass("GroupAttribute");
	    bindNamedEntity(constituent, template, resource);
	    break;
	case "human_process":
	    resource = resourceMatch(constituent, template);
	    if (resource == null) {
		logger.info("human_process instantiation failed! : " + resource);
		logger.info("\t\t" + template.tgrep);
		logger.info("\t\t" + constituent.getFragmentString());
		logger.info("\t\t" + constituent.treeString());
		break;
	    }
	    storeResource(doi, resource, humanProcessHash, "human_process");
	    constituent.setEntityClass("HumanProcess");
	    bindNamedEntity(constituent, template, resource);
	    break;
	case "injury":
	    resource = resourceMatch(constituent, template);
	    if (resource == null) {
		logger.info("injury instantiation failed! : " + resource);
		logger.info("\t\t" + template.tgrep);
		logger.info("\t\t" + constituent.getFragmentString());
		logger.info("\t\t" + constituent.treeString());
		break;
	    }
	    storeResource(doi, resource, injuryHash, "injury");
	    constituent.setEntityClass("Injury");
	    bindNamedEntity(constituent, template, resource);
	    break;
	case "intellectual_product":
	    resource = resourceMatch(constituent, template);
	    if (resource == null) {
		logger.info("intellectual_product instantiation failed! : " + resource);
		logger.info("\t\t" + template.tgrep);
		logger.info("\t\t" + constituent.getFragmentString());
		logger.info("\t\t" + constituent.treeString());
		break;
	    }
	    storeResource(doi, resource, intellectualProductHash, "intellectual_product");
	    constituent.setEntityClass("IntellectualProduct");
	    bindNamedEntity(constituent, template, resource);
	    break;
	case "language":
	    resource = resourceMatch(constituent, template);
	    if (resource == null) {
		logger.info("language instantiation failed! : " + resource);
		logger.info("\t\t" + template.tgrep);
		logger.info("\t\t" + constituent.getFragmentString());
		logger.info("\t\t" + constituent.treeString());
		break;
	    }
	    storeResource(doi, resource, languageHash, "language");
	    constituent.setEntityClass("Language");
	    bindNamedEntity(constituent, template, resource);
	    break;
	case "manufactured_object":
	    resource = resourceMatch(constituent, template);
	    if (resource == null) {
		logger.info("manufactured_object instantiation failed! : " + resource);
		logger.info("\t\t" + template.tgrep);
		logger.info("\t\t" + constituent.getFragmentString());
		logger.info("\t\t" + constituent.treeString());
		break;
	    }
	    storeResource(doi, resource, manufacturedObjectHash, "manufactured_object");
	    constituent.setEntityClass("ManufacturedObject");
	    bindNamedEntity(constituent, template, resource);
	    break;
	case "natural_process":
	    resource = resourceMatch(constituent, template);
	    if (resource == null) {
		logger.info("natural_process instantiation failed! : " + resource);
		logger.info("\t\t" + template.tgrep);
		logger.info("\t\t" + constituent.getFragmentString());
		logger.info("\t\t" + constituent.treeString());
		break;
	    }
	    storeResource(doi, resource, naturalProcessHash, "natural_process");
	    constituent.setEntityClass("NaturalProcess");
	    bindNamedEntity(constituent, template, resource);
	    break;
	case "organic_chemical":
	    resource = resourceMatch(constituent, template);
	    if (resource == null) {
		logger.info("organic_chemical instantiation failed! : " + resource);
		logger.info("\t\t" + template.tgrep);
		logger.info("\t\t" + constituent.getFragmentString());
		logger.info("\t\t" + constituent.treeString());
		break;
	    }
	    storeResource(doi, resource, organicChemicalHash, "organic_chemical");
	    constituent.setEntityClass("OrganicChemical");
	    bindNamedEntity(constituent, template, resource);
	    break;
	case "organism":
	    resource = resourceMatch(constituent, template);
	    if (resource == null) {
		logger.info("organism instantiation failed! : " + resource);
		logger.info("\t\t" + template.tgrep);
		logger.info("\t\t" + constituent.getFragmentString());
		logger.info("\t\t" + constituent.treeString());
		break;
	    }
	    storeResource(doi, resource, organismHash, "organism");
	    constituent.setEntityClass("Organism");
	    bindNamedEntity(constituent, template, resource);
	    break;
	case "organism_attribute":
	    resource = resourceMatch(constituent, template);
	    if (resource == null) {
		logger.info("organism_attribute instantiation failed! : " + resource);
		logger.info("\t\t" + template.tgrep);
		logger.info("\t\t" + constituent.getFragmentString());
		logger.info("\t\t" + constituent.treeString());
		break;
	    }
	    storeResource(doi, resource, organismAttributeHash, "organism_attribute");
	    constituent.setEntityClass("OrganismAttribute");
	    bindNamedEntity(constituent, template, resource);
	    break;
	case "organization":
	    resource = resourceMatch(constituent, template);
	    if (resource == null) {
		logger.info("organization instantiation failed! : " + resource);
		logger.info("\t\t" + template.tgrep);
		logger.info("\t\t" + constituent.getFragmentString());
		logger.info("\t\t" + constituent.treeString());
		break;
	    }
	    storeResource(doi, resource, organizationHash, "organization");
	    constituent.setEntityClass("Organization");
	    bindNamedEntity(constituent, template, resource);
	    break;
	case "pathological_function":
	    resource = resourceMatch(constituent, template);
	    if (resource == null) {
		logger.info("organism instantiation failed! : " + resource);
		logger.info("\t\t" + template.tgrep);
		logger.info("\t\t" + constituent.getFragmentString());
		logger.info("\t\t" + constituent.treeString());
		break;
	    }
	    storeResource(doi, resource, pathologicalFunctionHash, "pathological_function");
	    constituent.setEntityClass("PathologicalFunction");
	    bindNamedEntity(constituent, template, resource);
	    break;
	case "physical_relationship":
	    resource = resourceMatch(constituent, template);
	    if (resource == null) {
		logger.info("physical_relationship instantiation failed! : " + resource);
		logger.info("\t\t" + template.tgrep);
		logger.info("\t\t" + constituent.getFragmentString());
		logger.info("\t\t" + constituent.treeString());
		break;
	    }
	    storeResource(doi, resource, physicalRelationshipHash, "physical_relationship");
	    constituent.setEntityClass("PhysicalRelationship");
	    bindNamedEntity(constituent, template, resource);
	    break;
	case "physiological_function":
	    resource = resourceMatch(constituent, template);
	    if (resource == null) {
		logger.info("organism instantiation failed! : " + resource);
		logger.info("\t\t" + template.tgrep);
		logger.info("\t\t" + constituent.getFragmentString());
		logger.info("\t\t" + constituent.treeString());
		break;
	    }
	    storeResource(doi, resource, physiologicalFunctionHash, "physiological_function");
	    constituent.setEntityClass("PhysiologicalFunction");
	    bindNamedEntity(constituent, template, resource);
	    break;
	case "process":
	    resource = resourceMatch(constituent, template);
	    if (resource == null) {
		logger.info("process instantiation failed! : " + resource);
		logger.info("\t\t" + template.tgrep);
		logger.info("\t\t" + constituent.getFragmentString());
		logger.info("\t\t" + constituent.treeString());
		break;
	    }
	    storeResource(doi, resource, processHash, "process");
	    constituent.setEntityClass("Process");
	    bindNamedEntity(constituent, template, resource);
	    break;
	case "relationship":
	    resource = resourceMatch(constituent, template);
	    if (resource == null) {
		logger.info("relationship instantiation failed! : " + resource);
		logger.info("\t\t" + template.tgrep);
		logger.info("\t\t" + constituent.getFragmentString());
		logger.info("\t\t" + constituent.treeString());
		break;
	    }
	    storeResource(doi, resource, relationshipHash, "relationship");
	    constituent.setEntityClass("Relationship");
	    bindNamedEntity(constituent, template, resource);
	    break;
	case "spatial_relationship":
	    resource = resourceMatch(constituent, template);
	    if (resource == null) {
		logger.info("spatial_relationship instantiation failed! : " + resource);
		logger.info("\t\t" + template.tgrep);
		logger.info("\t\t" + constituent.getFragmentString());
		logger.info("\t\t" + constituent.treeString());
		break;
	    }
	    storeResource(doi, resource, spatialRelationshipHash, "spatial_relationship");
	    constituent.setEntityClass("SpatialRelationship");
	    bindNamedEntity(constituent, template, resource);
	    break;
	case "substance":
	    resource = resourceMatch(constituent, template);
	    if (resource == null) {
		logger.info("substance instantiation failed! : " + resource);
		logger.info("\t\t" + template.tgrep);
		logger.info("\t\t" + constituent.getFragmentString());
		logger.info("\t\t" + constituent.treeString());
		break;
	    }
	    storeResource(doi, resource, substanceHash, "substance");
	    constituent.setEntityClass("Substance");
	    bindNamedEntity(constituent, template, resource);
	    break;
	case "temporal_relationship":
	    resource = resourceMatch(constituent, template);
	    if (resource == null) {
		logger.info("temporal_relationship instantiation failed! : " + resource);
		logger.info("\t\t" + template.tgrep);
		logger.info("\t\t" + constituent.getFragmentString());
		logger.info("\t\t" + constituent.treeString());
		break;
	    }
	    storeResource(doi, resource, temporalRelationshipHash, "temporal_relationship");
	    constituent.setEntityClass("TemporalRelationship");
	    bindNamedEntity(constituent, template, resource);
	    break;
	case "transcription_factor":
	    resource = resourceMatch(constituent, template);
	    if (resource == null) {
		logger.info("organism instantiation failed! : " + resource);
		logger.info("\t\t" + template.tgrep);
		logger.info("\t\t" + constituent.getFragmentString());
		logger.info("\t\t" + constituent.treeString());
		break;
	    }
	    storeResource(doi, resource, transcriptionFactorHash, "transcription_factor");
	    constituent.setEntityClass("TranscriptionFactor");
	    bindNamedEntity(constituent, template, resource);
	    break;
	default:
	    break;
	}
	
    }
    
   private void storeResource(String doi, Resource resource, Hashtable<String, Resource> resourceHash, String sqlName) throws SQLException {
	int id = 0;
	logger.info("storing resource: doi: " + doi + "\tsqlname: " + sqlName + "\tresource: " + resource);
	Resource match = resourceHash.get(resource.toString());
	if (match != null) {
	    resource.setID(match.getID());
	} else {
	    try {
		PreparedStatement insert = conn.prepareStatement("insert into covid_model."+sqlName+"("+sqlName+",umls_id,umls_match_string) values(?,?,?)", Statement.RETURN_GENERATED_KEYS);
		insert.setString(1, resource.toString());
		insert.setString(2, resource.getUmlsConcept());
		insert.setString(3, resource.getUmlsMatchString());
		insert.execute();
		ResultSet rs = insert.getGeneratedKeys();
		while (rs.next()) {
		    id = rs.getInt(1);
		    logger.info("instantiating "+sqlName+" id: " + id + " : " + resource);
		    resource.setID(id);
		    resourceHash.put(resource.toString(), resource);
		}
		insert.close();
	    } catch (SQLException e) {
		if (e.getSQLState().equals("23505")) {
		    conn.rollback();
		    PreparedStatement select = conn.prepareStatement("select id from covid_model."+sqlName+" where "+sqlName+" = ?");
		    select.setString(1, resource.toString());
		    ResultSet rs = select.executeQuery();
		    while (rs.next()) {
			id = rs.getInt(1);
			logger.debug(sqlName+" id: " + id);
			resource.setID(id);
			resourceHash.put(resource.toString(), resource);
		    }
		    select.close();

		} else {
		    e.printStackTrace();
		}
	    } finally {
		conn.commit();
	    }
	}
//	for (UMLSMatch umatch : resource.getUmlsMatches()) {
//	    logger.info("\t" + umatch.getCui() + " : " + umatch.getMatchString());
//	    for (edu.uiowa.UMLS.Concept concept : edu.uiowa.UMLS.Concept.getByTerm(umatch.getMatchString())) {
//		logger.info("\t\tconcept: " + concept);
//		for (Semantics semantics : concept.getSemanticsVector()) {
//		    logger.info("\t\t\tsemantics: " + semantics);
//		}
//	    }
//	}
	try {
	    PreparedStatement insert = conn.prepareStatement("insert into covid_model."+sqlName+"_mention("+sqlName+"_id,doi) values(?,?)");
	    insert.setInt(1, resource.getID());
	    insert.setString(2, doi);
	    insert.execute();
	} catch (SQLException e) {
	    if (e.getSQLState().equals("23505")) {
		conn.rollback();
	    } else {
		e.printStackTrace();
	    }
	} finally {
	    conn.commit();
	}
	
   }

   private Resource resourceMatch(syntaxTree constituent, Template template) throws Exception {
       Resource theResource = null;
	syntaxMatcher theMatcher = new syntaxMatcher(template.tgrep);
	logger.info(template.relation + " tgrep: " + template.tgrep + "\tconstituent: " + constituent.treeString());
	if (theMatcher.isMatch(constituent)) {
	    Vector<basicLexerToken> matchVector = theMatcher.matchesAsTokens();
	    logger.info(template.relation + " vector: " + matchVector);
	    theResource = instantiateResource(template, pruneMatchVector(matchVector));
	    logger.info(template.relation + " entity: " + theResource);

	    ExhaustiveVectorConceptRecognizer umlsRecognizer = new ExhaustiveVectorConceptRecognizer(new UMLSDetector(), ExhaustiveVectorConceptRecognizer.Direction.BOTH, false);
	    List umlsResults = umlsRecognizer.recognize(new Sentence(matchVector), getScanFence(constituent));
	    logger.info("UMLS results: " + umlsResults);
	    for (Concept concept : (List<Concept>)umlsResults) {
		Vector<edu.uiowa.UMLS.Concept> umlsConcepts = (Vector<edu.uiowa.UMLS.Concept>)concept.getKey();
		if (umlsConcepts.size() == 1) {
		    theResource.setUmlsMatch(umlsConcepts.get(0).getConceptID(),(String)concept.getPhrase() + " := " + umlsConcepts.get(0));
		    logger.info("\tUMLS match: " + umlsConcepts.get(0));
		} else {
		    Hashtable<String,String> cuiHash = new Hashtable<String,String>();
		    for (edu.uiowa.UMLS.Concept umlsConcept : umlsConcepts) {
			if (cuiHash.containsKey(umlsConcept.getConceptID()))
			    continue;
			cuiHash.put(umlsConcept.getConceptID(), "");
			theResource.setUmlsMatch(umlsConcept.getConceptID(),(String)umlsConcept.getTerm() + " :? " + umlsConcept);
			logger.info("\tUMLS option: " + umlsConcept);
		    }
		}
	    }
	}
	return theResource;
   }
   
   private Resource instantiateResource(Template template, Vector<basicLexerToken> vector) {
       switch (template.relation) {
       case "clinical_trial":
	   return new ClinicalTrialRegistration(vector);
       case "activity":
	   return new Activity(vector);
       case "anatomical_structure":
	   return new AnatomicalStructure(vector);
       case "biological_function":
	   return new BiologicalFunction(vector);
       case "body_part":
	   return new BodyPart(vector);
       case "concept":
	   return new edu.uiowa.PubMedCentral.entity.Concept(vector);
       case "temporal_concept":
	   return new TemporalConcept(vector);
       case "qualitative_concept":
	   return new QualitativeConcept(vector);
       case "quantitative_concept":
	   return new QuantitativeConcept(vector);
       case "functional_concept":
	   return new FunctionalConcept(vector);
       case "spatial_concept":
	   return new SpatialConcept(vector);
       case "conceptual_relationship":
	   return new ConceptualRelationship(vector);
       case "discipline":
	   return new Discipline(vector);
       case "disease":
	   return new Disease(vector);
       case "entity":
 	   return new edu.uiowa.PubMedCentral.entity.Entity(vector);
       case "event":
	   return new Event(vector);
       case "finding":
	   return new Finding(vector);
       case "functional_relationship":
	   return new FunctionalRelationship(vector);
       case "group_attribute":
	   return new GroupAttribute(vector);
       case "group":
	   return new Group(vector);
       case "human_process":
	   return new HumanProcess(vector);
       case "injury":
	   return new Injury(vector);
       case "intellectual_product":
	   return new IntellectualProduct(vector);
       case "language":
	   return new Language(vector);
       case "manufactured_object":
	   return new ManufacturedObject(vector);
       case "natural_process":
	   return new NaturalProcess(vector);
       case "organic_chemical":
	   return new OrganicChemical(vector);
       case "organism":
	   return new Organism(vector);
       case "organism_attribute":
	   return new OrganismAttribute(vector);
       case "pathological_function":
	   return new PathologicalFunction(vector);
       case "physical_relationship":
	   return new PhysicalRelationship(vector);
       case "physiological_function":
	   return new PhysiologicalFunction(vector);
       case "process":
	   return new edu.uiowa.PubMedCentral.entity.Process(vector);
       case "relationship":
	   return new Relationship(vector);
       case "spatial_relationship":
	   return new SpatialRelationship(vector);
       case "substance":
	   return new Substance(vector);
       case "temporal_relationship":
	   return new TemporalRelationship(vector);
       case "transcription_factor":
	   return new TranscriptionFactor(vector);
       }
       return null;
   }

    PlaceName placeNameMatch(syntaxTree theNode, String pattern) throws Exception {
	PlaceName thePlaceName = null;
	syntaxMatcher theMatcher = null;
	try {
	    theMatcher = new syntaxMatcher(pattern);
	} catch (Exception e1) {
	    logger.error("*** error parsing placeNameMatch pattern: " + pattern);
	    System.exit(0);
	}
	if (theMatcher.isMatch(theNode)) {
	    Vector<basicLexerToken> matchVector = theMatcher.matchesAsTokens();
	    logger.debug("placeName vector: " + matchVector);
	    thePlaceName = new PlaceName(pruneMatchVector(matchVector));
	    logger.debug("placeName entity: " + thePlaceName);

	    ExhaustiveVectorConceptRecognizer geonameRecognizer = new ExhaustiveVectorConceptRecognizer(new GeoNamesDetector(), false);
	    List geoResults = geonameRecognizer.recognize(new Sentence(matchVector), getScanFence(theNode));
	    logger.debug("GeoNames results: " + geoResults);
	    for (Concept concept : (List<Concept>) geoResults) {
		Vector<GeoName> geonames = (Vector<GeoName>) concept.getKey();
		if (thePlaceName.getGeonamesID() == 0 && geonames.size() == 1) {
		    thePlaceName.setGeonamesID(geonames.get(0).getId());
		    thePlaceName.setGeonamesMatchString((String) concept.getPhrase() + " := " + geonames.get(0));
		} else if (thePlaceName.getGeonamesID() == 0) {
		    boolean firstAddress = true;
		    for (GeoName geoname : geonames) {
			syntaxMatcher locationMatcher = null;
			String matchPattern = null;
			try {
			    matchPattern = syntaxTree.generateNodePattern(theNode) + " >>[S <<(/" + geoname.getAncestorContext(true) + "/) ]";
			    locationMatcher = new syntaxMatcher(matchPattern);
			    if (locationMatcher != null && locationMatcher.hasMatch(theNode)) {
				Vector<basicLexerToken> addressMatchVector = locationMatcher.matchesAsTokens();
				logger.debug("GeoName option: " + geoname);
				logger.debug("location match pattern: " + syntaxTree.generateNodePattern(theNode) + " >>[S <<(/" + geoname.getAncestorContext(true) + "/) ]");
				logger.debug("\tGeoName match vector: " + addressMatchVector);
				if (firstAddress) {
				    thePlaceName.setGeonamesID(geoname.getId());
				    thePlaceName.setGeonamesMatchString((String) concept.getPhrase() + " :< " + geoname.getAncestorContext(true));
				    firstAddress = false;
				}
			    }
			} catch (Exception e) {
			    logger.error("placeNameMatch: trapping erronous pattern: " + matchPattern);
			}
		    }
		    if (firstAddress) {
			GeoName heuristicMatch = heuristicGeoNameMatch(geonames);
			if (heuristicMatch != null) {
			    thePlaceName.setGeonamesID(heuristicMatch.getId());
			    thePlaceName.setGeonamesMatchString(concept.getPhrase() + " :? " + heuristicMatch.getAncestorContext());
			}
		    }
		}
	    }
	}

	return thePlaceName;
    }
    
    GeoName heuristicGeoNameMatch(Vector<GeoName> geonames) {
	GeoName aGeoName = null;
	GeoName lGeoName = null;
	GeoName pGeoName = null;
	GeoName sGeoName = null;
	GeoName otherGeoName = null;
	
	for (GeoName current : geonames) {
	    switch (current.getFeatureClass()) {
	    case "A" :
		if (aGeoName == null || aGeoName.getPopulation() < current.getPopulation())
		    aGeoName = current;
		break;
	    case "L" :
		if (lGeoName == null || lGeoName.getPopulation() < current.getPopulation())
		    lGeoName = current;
		break;
	    case "P" :
		if (pGeoName == null || pGeoName.getPopulation() < current.getPopulation())
		    pGeoName = current;
		break;
	    case "S" :
		if (sGeoName == null || sGeoName.getPopulation() < current.getPopulation())
		    sGeoName = current;
		break;
            default:
		if (otherGeoName == null || otherGeoName.getPopulation() < current.getPopulation())
		    otherGeoName = current;
		break;
	    }
	}
	
	if (pGeoName != null)
	    return pGeoName;
	
	if (aGeoName != null)
	    return aGeoName;
	
	if (lGeoName != null)
	    return lGeoName;
	
	if (sGeoName != null)
	    return sGeoName;
	
	return otherGeoName;
    }

    Organization organizationMatch(syntaxTree theNode, String pattern) throws Exception {
	logger.debug("organizationMatch: " + getScanFence(theNode) + " : " + theNode.treeString() + "\t" + pattern);
	Organization establishment = null;
	syntaxMatcher theMatcher = new syntaxMatcher(pattern);
	registerMatchFunctions(theMatcher);
	if (theMatcher.hasMatch(theNode)) {
	    Vector<basicLexerToken> matchVector = theMatcher.matchesAsTokens();
	    logger.debug("organization vector: " + matchVector);
	    establishment = new Organization(pruneMatchVector(matchVector));

	    ExhaustiveVectorConceptRecognizer gridRecognizer = new ExhaustiveVectorConceptRecognizer(new GRIDDetector(), false);
	    List theResults = gridRecognizer.recognize(new Sentence(matchVector), getScanFence(theNode));
	    logger.debug("GRID results: " + theResults);
	    boolean firstConcept = true;
	    for (Concept concept : (List<Concept>)theResults) {
		Vector<Institute> institutes = (Vector<Institute>)concept.getKey();
		if (firstConcept && institutes.size() == 1) {
		    establishment.setGridID(institutes.get(0).getId());
		    establishment.setGridMatchString((String)concept.getPhrase());
		} else if (firstConcept) {
		    boolean firstAddress = true;
		    for (Institute institute : institutes) {
			logger.debug("GRID option: " + institute);
			if (institute.getAddresses().size() > 0) {
			    Address address = institute.getAddresses().firstElement();
			    logger.debug("location match pattern: " + syntaxTree.generateNodePattern(theNode)+" >>[S <<(/" + address.getCity() + "|" + address.getState() + "|" + address.getCountryish() + "/) ]");
			    syntaxMatcher locationMatcher = null;
			    String matchPattern = null;
			    try {
				matchPattern = syntaxTree.generateNodePattern(theNode) + " >>[S <<(/" + address.getCity() + "|" + address.getState() + "|" + address.getCountryish() + "/) ]";
				locationMatcher = new syntaxMatcher(matchPattern);
				if (locationMatcher != null && locationMatcher.hasMatch(theNode)) {
				    Vector<basicLexerToken> addressMatchVector = locationMatcher.matchesAsTokens();
				    logger.debug("\tGRID address match vector: " + addressMatchVector);
				    if (firstAddress) {
					establishment.setGridID(institute.getId());
					establishment.setGridMatchString((String) concept.getPhrase());
					firstAddress = false;
				    }
				}
			    } catch (Exception e) {
				logger.error("organizationMatch: trapping erronous pattern: " + matchPattern);
			    }
			}
		    }
		}
		firstConcept = false;
	    }

	    ExhaustiveVectorConceptRecognizer geonameRecognizer = new ExhaustiveVectorConceptRecognizer(new GeoNamesDetector(), false);
	    List geoResults = geonameRecognizer.recognize(new Sentence(matchVector), getScanFence(theNode));
	    logger.debug("GeoNames results: " + geoResults);
	    firstConcept = true;
	    for (Concept concept : (List<Concept>)geoResults) {
		Vector<GeoName> geonames = (Vector<GeoName>)concept.getKey();
		if (firstConcept && geonames.size() == 1) {
		    establishment.setGeonamesID(geonames.get(0).getId());
		    establishment.setGeonamesMatchString((String)concept.getPhrase() + " := " + geonames.get(0).getAncestorContext());
		} else if (firstConcept) {
		    boolean firstAddress = true;
		    for (GeoName geoname : geonames) {
			logger.debug("GeoName option: " + geoname);
			logger.debug("location match pattern: " + syntaxTree.generateNodePattern(theNode) + " >>[S <<(/" + geoname.getAncestorContext(true) + "/) ]");
			syntaxMatcher locationMatcher = null;
			String matchPattern = null;
			try {
			    matchPattern = syntaxTree.generateNodePattern(theNode) + " >>[S <<(/" + geoname.getAncestorContext(true) + "/) ]";
			    locationMatcher = new syntaxMatcher(matchPattern);
			    if (locationMatcher != null & locationMatcher.hasMatch(theNode)) {
				Vector<basicLexerToken> addressMatchVector = locationMatcher.matchesAsTokens();
				logger.debug("\tGeoName match vector: " + addressMatchVector);
				if (firstAddress) {
				    establishment.setGeonamesID(geoname.getId());
				    establishment.setGeonamesMatchString((String) concept.getPhrase() + " :< " + geoname.getAncestorContext(true));
				    firstAddress = false;
				}
			    }
			} catch (Exception e) {
			    logger.error("organizationMatch: trapping erronous pattern: " + matchPattern);
			}
		    }
		    if (firstAddress) {
			GeoName heuristicMatch = heuristicGeoNameMatch(geonames);
			if (heuristicMatch != null) {
			    establishment.setGeonamesID(heuristicMatch.getId());
			    establishment.setGeonamesMatchString(concept.getPhrase() + " :? " + heuristicMatch.getAncestorContext());
			}
		    }
		}
		firstConcept = false;
	    }

	    logger.debug("organization entity: " + establishment);
	}
	return establishment;
    }
    
    int getScanFence(syntaxTree theNode) {
	int count = theNode.getLeadingLeafCount();
	if (count > 0)
	    return count;
	
	syntaxTree constituent = theNode.getConstituent(0);
	if (constituent.getConstituent(0) == null)
	    return 0;
	else
	    return constituent.getLeadingLeafCount();
    }

    protected void registerMatchFunctions(syntaxMatcher theMatcher) {
	theMatcher.registerFunction("isEntity", new entityComparator());
	theMatcher.registerFunction("isEmail", new emailComparator());
	theMatcher.registerFunction("isURL", new urlComparator());
    }

    Collaboration collaborationMatch(syntaxTree theNode, String pattern) throws Exception {
	Collaboration collaboration = null;
	syntaxMatcher theMatcher = new syntaxMatcher(pattern);
	if (theMatcher.isMatch(theNode)) {
	    logger.debug("collaboration vector: " + theMatcher.matchesAsTokens());
	    collaboration = new Collaboration(pruneMatchVector(theMatcher.matchesAsTokens()));
	    logger.debug("collaboration entity: " + collaboration);
	}
	return collaboration;
    }

    ClinicalTrialRegistration clinicalTrialMatch(syntaxTree theNode, String pattern) throws Exception {
	ClinicalTrialRegistration clinicalTrial = null;
	syntaxMatcher theMatcher = new syntaxMatcher(pattern);
//	if (theMatcher.isMatch(theNode)) {
//	    logger.info("clinical trial vector: " + theMatcher.matchesAsTokens());
//	    clinicalTrial = new ClinicalTrialRegistration(theMatcher.matchesAsTokens());
//	    logger.info("clinical trial entity: " + clinicalTrial);
//	}
	if (theMatcher.hasMatch(theNode)) {
	    Enumeration<syntaxMatch> theEnum = theMatcher.matches(theNode);
	    while (theEnum.hasMoreElements()) {
		syntaxMatch theMatchNode = theEnum.nextElement();
		logger.info("clinical trial match pattern: " + pattern + "\tnode: " + theMatchNode.getPhrase().treeString() + "\t" + theMatchNode.getPhrase().getFragmentString());
		for (int i = 1; i <= theMatchNode.matchCount(); i++)
		    logger.info("\tmatch slot [" + i + "]: " + theMatchNode.getMatch(i).trimmedPhraseAsString());
		if (theMatchNode.matchCount() == 1)
		    clinicalTrial = new ClinicalTrialRegistration(theMatchNode.getMatch(1).trimmedPhraseAsString());
		else
		    clinicalTrial = new ClinicalTrialRegistration(theMatchNode.getMatch(1).trimmedPhraseAsString(),theMatchNode.getMatch(2).trimmedPhraseAsString());
	    }
	}
	return clinicalTrial;
    }

    void storePerson(String doi, Person thePerson) throws SQLException {
	int id = 0;

	Person match = personHash.get(thePerson.toString());
	if (match != null) {
	    thePerson.setID(match.getID());
	} else {
	    try {
		PreparedStatement insert = conn.prepareStatement("insert into person(first_name,last_name,middle_name,title,appendix) values(?,?,?,?,?)", Statement.RETURN_GENERATED_KEYS);
		insert.setString(1, thePerson.getFirstName());
		insert.setString(2, thePerson.getSurname());
		insert.setString(3, thePerson.getMiddleName());
		insert.setString(4, thePerson.getTitle());
		insert.setString(5, thePerson.getAppendix());
		insert.execute();
		ResultSet rs = insert.getGeneratedKeys();
		while (rs.next()) {
		    id = rs.getInt(1);
		    logger.info("new person id: " + id + "\t" + thePerson);
		    thePerson.setID(id);
		    personHash.put(thePerson.toString(), thePerson);
		}
		insert.close();
	    } catch (SQLException e) {
		if (e.getSQLState().equals("23505")) {
		    conn.rollback();
		    PreparedStatement select = conn.prepareStatement("select id from person where first_name = ? and last_name = ? and middle_name = ? and title = ? and appendix = ?");
		    select.setString(1, thePerson.getFirstName());
		    select.setString(2, thePerson.getSurname());
		    select.setString(3, thePerson.getMiddleName());
		    select.setString(4, thePerson.getTitle());
		    select.setString(5, thePerson.getAppendix());
		    boolean found = false;
		    ResultSet rs = select.executeQuery();
		    while (rs.next()) {
			id = rs.getInt(1);
			logger.debug("person id: " + id);
			thePerson.setID(id);
			personHash.put(thePerson.toString(), thePerson);
			found = true;
		    }
		    select.close();
		    if (!found)
			logger.error("failed to retrieve person: " + thePerson);
		} else {
		    e.printStackTrace();
		}
	    } finally {
		conn.commit();
	    }
	}
	try {
	    PreparedStatement insert = conn.prepareStatement("insert into person_mention(person_id,doi) values(?,?)");
	    insert.setInt(1, thePerson.getID());
	    insert.setString(2, doi);
	    insert.execute();
	} catch (SQLException e) {
	    if (e.getSQLState().equals("23505")) {
		conn.rollback();
	    } else {
		e.printStackTrace();
	    }
	} finally {
	    conn.commit();
	}
    }

     void storeClinicalTrial(String doi, ClinicalTrialRegistration clinicalTrial) throws SQLException {
	PreparedStatement insert = conn.prepareStatement("insert into clinical_trial values(?,?,?)");
	insert.setString(1, doi);
	insert.setString(2, clinicalTrial.getPrefix());
	insert.setString(3, clinicalTrial.getIdentifier());
	insert.execute();
	insert.close();
	conn.commit();
    }
    

    @Override
    public void resolveID(int id, Entity elementAt) throws SQLException {
	// TODO Auto-generated method stub
	
    }

}
