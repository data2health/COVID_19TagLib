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
import edu.uiowa.PubMedCentral.entity.AnatomicalStructure;
import edu.uiowa.PubMedCentral.entity.ClinicalTrialRegistration;
import edu.uiowa.PubMedCentral.entity.Collaboration;
import edu.uiowa.PubMedCentral.entity.Organization;
import edu.uiowa.PubMedCentral.entity.Person;
import edu.uiowa.PubMedCentral.entity.PlaceName;
import edu.uiowa.PubMedCentral.entity.Resource;
import edu.uiowa.PubMedCentral.entity.UMLSMatch;
import edu.uiowa.UMLS.Semantics;
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
    Hashtable<String, Resource> placeNameHash = new Hashtable<String, Resource>();
    Hashtable<String, Resource> organismHash = new Hashtable<String, Resource>();
    Hashtable<String, Resource> organicChemicalHash = new Hashtable<String, Resource>();
    Hashtable<String, Resource> eventHash = new Hashtable<String, Resource>();
    Hashtable<String, Resource> diseaseHash = new Hashtable<String, Resource>();
    Hashtable<String, Resource> anatomicalStructureHash = new Hashtable<String, Resource>();
    Hashtable<String, Resource> biologicalFunctionHash = new Hashtable<String, Resource>();
    Hashtable<String, Resource> bodyPartHash = new Hashtable<String, Resource>();
    Hashtable<String, Resource> findingHash = new Hashtable<String, Resource>();
    Hashtable<String, Resource> injuryHash = new Hashtable<String, Resource>();
    Hashtable<String, Resource> manufacturedObjectHash = new Hashtable<String, Resource>();
    Hashtable<String, Resource> pathologicalFunctionHash = new Hashtable<String, Resource>();
    Hashtable<String, Resource> physiologicalFunctionHash = new Hashtable<String, Resource>();
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
	switch (template.relation) {
	case "clinical_trial":
	    ClinicalTrialRegistration clinicalTrial = clinicalTrialMatch(constituent, template.tgrep);
	    if (clinicalTrial == null) {
		// unlike some of the other patterns, these drive on specific strings in the text, so a null match is frequent
		logger.debug("clinical_trial instantiation failed! : " + clinicalTrial);
		logger.debug("\t\t" + template.tgrep);
		logger.debug("\t\t" + constituent.getFragmentString());
		logger.debug("\t\t" + constituent.treeString());
		break;
	    }
	    storeClinicalTrial(doi, clinicalTrial);
	    constituent.setEntityClass("ClinicalTrial");
	    bindNamedEntity(constituent, template, clinicalTrial);
	    break;
	case "anatomical_structure":
	     resource = resourceMatch(constituent, template.tgrep);
	    if (resource == null) {
		logger.debug("anatomicalStructure instantiation failed! : " + resource);
		logger.debug("\t\t" + template.tgrep);
		logger.debug("\t\t" + constituent.getFragmentString());
		logger.debug("\t\t" + constituent.treeString());
		break;
	    }
	    storeResource(doi, resource, anatomicalStructureHash, "anatomical_structure");
	    constituent.setEntityClass("AnatomicalStructure");
	    bindNamedEntity(constituent, template, resource);
	    break;
	case "biological_function":
	    resource = resourceMatch(constituent, template.tgrep);
	    if (resource == null) {
		logger.debug("biologicalFunction instantiation failed! : " + resource);
		logger.debug("\t\t" + template.tgrep);
		logger.debug("\t\t" + constituent.getFragmentString());
		logger.debug("\t\t" + constituent.treeString());
		break;
	    }
	    storeResource(doi, resource, biologicalFunctionHash, "biological_function");
	    constituent.setEntityClass("BiologicalFunction");
	    bindNamedEntity(constituent, template, resource);
	    break;
	case "body_part":
	    resource = resourceMatch(constituent, template.tgrep);
	    if (resource == null) {
		logger.debug("bodyPart instantiation failed! : " + resource);
		logger.debug("\t\t" + template.tgrep);
		logger.debug("\t\t" + constituent.getFragmentString());
		logger.debug("\t\t" + constituent.treeString());
		break;
	    }
	    storeResource(doi, resource, bodyPartHash, "body_part");
	    constituent.setEntityClass("BodyPart");
	    bindNamedEntity(constituent, template, resource);
	    break;
	case "disease":
	   resource = resourceMatch(constituent, template.tgrep);
	    if (resource == null) {
		logger.debug("disease instantiation failed! : " + resource);
		logger.debug("\t\t" + template.tgrep);
		logger.debug("\t\t" + constituent.getFragmentString());
		logger.debug("\t\t" + constituent.treeString());
		break;
	    }
	    storeResource(doi, resource, diseaseHash, "disease");
	    constituent.setEntityClass("Disease");
	    bindNamedEntity(constituent, template, resource);
	    break;
	case "event":
	    resource = resourceMatch(constituent, template.tgrep);
	    if (resource == null) {
		logger.debug("event instantiation failed! : " + resource);
		logger.debug("\t\t" + template.tgrep);
		logger.debug("\t\t" + constituent.getFragmentString());
		logger.debug("\t\t" + constituent.treeString());
		break;
	    }
	    storeResource(doi, resource, eventHash, "event");
	    constituent.setEntityClass("Event");
	    bindNamedEntity(constituent, template, resource);
	    break;
	case "finding":
	    resource = resourceMatch(constituent, template.tgrep);
	    if (resource == null) {
		logger.debug("finding instantiation failed! : " + resource);
		logger.debug("\t\t" + template.tgrep);
		logger.debug("\t\t" + constituent.getFragmentString());
		logger.debug("\t\t" + constituent.treeString());
		break;
	    }
	    storeResource(doi, resource, findingHash, "finding");
	    constituent.setEntityClass("Event");
	    bindNamedEntity(constituent, template, resource);
	    break;
	case "injury":
	    resource = resourceMatch(constituent, template.tgrep);
	    if (resource == null) {
		logger.debug("injury instantiation failed! : " + resource);
		logger.debug("\t\t" + template.tgrep);
		logger.debug("\t\t" + constituent.getFragmentString());
		logger.debug("\t\t" + constituent.treeString());
		break;
	    }
	    storeResource(doi, resource, injuryHash, "injury");
	    constituent.setEntityClass("Injury");
	    bindNamedEntity(constituent, template, resource);
	    break;
	case "manufactored_object":
	    resource = resourceMatch(constituent, template.tgrep);
	    if (resource == null) {
		logger.debug("injury instantiation failed! : " + resource);
		logger.debug("\t\t" + template.tgrep);
		logger.debug("\t\t" + constituent.getFragmentString());
		logger.debug("\t\t" + constituent.treeString());
		break;
	    }
	    storeResource(doi, resource, manufacturedObjectHash, "manufactured_object");
	    constituent.setEntityClass("ManufacturedObject");
	    bindNamedEntity(constituent, template, resource);
	    break;
	case "organic_chemical":
	    resource = resourceMatch(constituent, template.tgrep);
	    if (resource == null) {
		logger.debug("organic_chemical instantiation failed! : " + resource);
		logger.debug("\t\t" + template.tgrep);
		logger.debug("\t\t" + constituent.getFragmentString());
		logger.debug("\t\t" + constituent.treeString());
		break;
	    }
	    storeResource(doi, resource, organicChemicalHash, "organic_chemical");
	    constituent.setEntityClass("OrganicChemical");
	    bindNamedEntity(constituent, template, resource);
	    break;
	case "organism":
	    resource = resourceMatch(constituent, template.tgrep);
	    if (resource == null) {
		logger.debug("organism instantiation failed! : " + resource);
		logger.debug("\t\t" + template.tgrep);
		logger.debug("\t\t" + constituent.getFragmentString());
		logger.debug("\t\t" + constituent.treeString());
		break;
	    }
	    storeResource(doi, resource, organismHash, "organism");
	    constituent.setEntityClass("Organism");
	    bindNamedEntity(constituent, template, resource);
	    break;
	case "pathological_function":
	    resource = resourceMatch(constituent, template.tgrep);
	    if (resource == null) {
		logger.debug("organism instantiation failed! : " + resource);
		logger.debug("\t\t" + template.tgrep);
		logger.debug("\t\t" + constituent.getFragmentString());
		logger.debug("\t\t" + constituent.treeString());
		break;
	    }
	    storeResource(doi, resource, pathologicalFunctionHash, "pathological_function");
	    constituent.setEntityClass("PathologicalFunction");
	    bindNamedEntity(constituent, template, resource);
	    break;
	case "physiological_function":
	    resource = resourceMatch(constituent, template.tgrep);
	    if (resource == null) {
		logger.debug("organism instantiation failed! : " + resource);
		logger.debug("\t\t" + template.tgrep);
		logger.debug("\t\t" + constituent.getFragmentString());
		logger.debug("\t\t" + constituent.treeString());
		break;
	    }
	    storeResource(doi, resource, physiologicalFunctionHash, "physiological_function");
	    constituent.setEntityClass("PhysiologicalFunction");
	    bindNamedEntity(constituent, template, resource);
	    break;
	case "transcription_factor":
	    resource = resourceMatch(constituent, template.tgrep);
	    if (resource == null) {
		logger.debug("organism instantiation failed! : " + resource);
		logger.debug("\t\t" + template.tgrep);
		logger.debug("\t\t" + constituent.getFragmentString());
		logger.debug("\t\t" + constituent.treeString());
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

   private Resource resourceMatch(syntaxTree constituent, String pattern) throws Exception {
       Resource theResource = null;
	syntaxMatcher theMatcher = new syntaxMatcher(pattern);
	if (theMatcher.isMatch(constituent)) {
	    Vector<basicLexerToken> matchVector = theMatcher.matchesAsTokens();
	    logger.debug("organism vector: " + matchVector);
	    theResource = new AnatomicalStructure(pruneMatchVector(matchVector));
	    logger.debug("biological function entity: " + theResource);

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
			theResource.setUmlsMatch(umlsConcept.getConceptID(),(String)concept.getPhrase() + " :? " + umlsConcept);
			logger.info("\tUMLS option: " + umlsConcept);
		    }
		}
	    }
	}
	return theResource;
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
