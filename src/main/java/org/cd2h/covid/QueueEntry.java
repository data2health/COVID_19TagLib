package org.cd2h.covid;

class QueueEntry {
	String doi = null;
	int pmcid = 0;
	int pmid = 0;
	
	int seqnum = 0;
	int seqnum2 = 0;
	int seqnum3 = 0;
	int seqnum4 = 0;
	int seqnum5 = 0;
	int seqnum6 = 0;
	int sentnum = 0;
	
	QueueEntry(String doi, int pmcid, int pmid) {
		this.doi = doi;
		this.pmcid = pmcid;
		this.pmid = pmid;
	}
	
	public String toString() {
		return "[" + doi + " : " + pmcid + " : " + pmid + "]";
	}
}