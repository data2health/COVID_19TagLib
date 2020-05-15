package org.cd2h.covid;

import java.util.Vector;

import org.cd2h.covid.model.Document;

public class DocumentQueue {
    int capacity = 5;
    Vector<Document> documentQueue = new Vector<Document>();
    boolean completed = false;
    
    public synchronized boolean atCapacity() {
	return documentQueue.size() >= capacity;
    }
    
    public synchronized void queue(Document document) {
	documentQueue.add(document);
	completed = false;
    }
    
    public void completed() {
	completed = true;
    }
    
    public int size() {
	return documentQueue.size();
    }
    
    public synchronized boolean isCompleted() {
	return documentQueue.size() == 0 && completed;
    }
    
    public synchronized Document dequeue() {
	if (documentQueue.size() == 0)
	    return null;
	else
	    return documentQueue.remove(0);
    }

}
