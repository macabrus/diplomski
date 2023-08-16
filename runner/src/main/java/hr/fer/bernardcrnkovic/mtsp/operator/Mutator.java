package hr.fer.bernardcrnkovic.mtsp.operator;

import hr.fer.bernardcrnkovic.mtsp.model.Solution;

import java.util.*;

/* collection of static methods for mutating solution */
public class Mutator {
    // swaps single pair in solution
    public static Solution singleSwap(Solution s, Random rand) {
        var e1 = rand.nextInt(1, s.tours.length);
        var e2 = rand.nextInt(1, s.tours.length);
        var tmp = s.tours[e1];
        s.tours[e1] = s.tours[e2];
        s.tours[e2] = tmp;
        s.setEvaluated(false);
        return s;
    }

    // swaps two segments in solution
    public static Solution segmentSwap(Solution s, Random rand) {
        int len = s.tours.length;
        var ranges = rand.ints(4, 1, len).sorted().toArray();
        int a1 = ranges[0];
        int a2 = ranges[1];
        int b1 = ranges[2];
        int b2 = ranges[3];
        var orig = Arrays.stream(s.tours).boxed().toList();
        var l = new ArrayList<Integer>();
        l.addAll(orig.subList(0, a1));
        l.addAll(orig.subList(b1, b2));
        l.addAll(orig.subList(a2, b1));
        l.addAll(orig.subList(a1, a2));
        l.addAll(orig.subList(b2, orig.size()));
        s.tours = l.stream().mapToInt(i -> i).toArray();
        s.setEvaluated(false);
        return s;
    }

    // inverts one segment in solution
    public static Solution invertSwap(Solution s, Random rand) {
        int len = s.tours.length;
        var ranges = rand.ints(2, 1, len).sorted().toArray();
        int a1 = ranges[0];
        int a2 = ranges[1];
        for (int i = 0; i < (a2 - a1) / 2; i++) {
            s.tours[a1 + i] = s.tours[a2 - 1 - i];
        }
        s.setEvaluated(false);
        return s;
    }



}
