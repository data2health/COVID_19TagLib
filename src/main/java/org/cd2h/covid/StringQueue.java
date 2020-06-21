package org.cd2h.covid;

import java.util.Vector;

public class StringQueue {
    int capacity = 5;
    Vector<String> stringQueue = new Vector<String>();
    boolean completed = false;
    
    public synchronized boolean atCapacity() {
	return stringQueue.size() >= capacity;
    }
    
    public synchronized void queue(String string) {
	stringQueue.add(string);
	completed = false;
    }
    
    public void completed() {
	completed = true;
    }
    
    public int size() {
	return stringQueue.size();
    }
    
    public synchronized boolean isCompleted() {
	return stringQueue.size() == 0 && completed;
    }
    
    public synchronized String dequeue() {
	if (stringQueue.size() == 0)
	    return null;
	else
	    return stringQueue.remove(0);
    }

}
