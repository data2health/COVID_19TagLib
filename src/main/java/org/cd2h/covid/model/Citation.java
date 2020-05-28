package org.cd2h.covid.model;

public class Citation {
    Sentence referencingSentence = null;
    Reference citedReference = null;
    String citationString = null;
    
    public Citation(Sentence sentence, Reference reference, String citation) {
	referencingSentence = sentence;
	citedReference = reference;
	citationString = citation;
    }
}
