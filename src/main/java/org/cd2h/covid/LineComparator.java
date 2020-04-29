package org.cd2h.covid;

import java.util.Comparator;

import pl.edu.icm.cermine.structure.model.BxLine;

public class LineComparator implements Comparator<BxLine> {
    public int compare(BxLine o, BxLine o1) {
        BxLine line = ((BxLine) o);
        BxLine line1 = (BxLine) o1;
        if (Math.abs(line.getY() - line1.getY()) < 0.1) {
            // line.y = line1.y
            if (Math.abs(line.getX() - line1.getX()) < 0.1) {
        	return 0;
            } else if (line.getX() < line1.getX()) {
        	return -1;
            } else
        	return 1;
        } else if (line.getY() < line1.getY()) {
            // line < line1
            return -1;
        } else {
            // line > line1
            return 1;
        }
    }
}
