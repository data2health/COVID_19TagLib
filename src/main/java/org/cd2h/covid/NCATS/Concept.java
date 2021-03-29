package org.cd2h.covid.NCATS;

public class Concept {
	String phrase = null;
	int id = 0;
	int count = 1;

	public Concept(String phrase, int id) {
		this.phrase = phrase;
		this.id = id;
	}

	public String getPhrase() {
		return phrase;
	}

	public void setPhrase(String phrase) {
		this.phrase = phrase;
	}

	public int getID() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public void incrementCount() {
		this.count++;
	}
}
