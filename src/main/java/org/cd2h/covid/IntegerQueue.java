package org.cd2h.covid;

import java.util.Vector;

public class IntegerQueue {
    int capacity = 5;
    Vector<Integer> integerQueue = new Vector<Integer>();
    boolean completed = false;
    
    public synchronized boolean atCapacity() {
	return integerQueue.size() >= capacity;
    }
    
    public synchronized void queue(Integer integer) {
	integerQueue.add(integer);
	completed = false;
    }
    
    public void completed() {
	completed = true;
    }
    
    public int size() {
	return integerQueue.size();
    }
    
    public synchronized boolean isCompleted() {
	return integerQueue.size() == 0 && completed;
    }
    
    public synchronized Integer dequeue() {
	if (integerQueue.size() == 0)
	    return null;
	else
	    return integerQueue.remove(0);
    }

}
