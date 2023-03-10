package hr.fer.bernardcrnkovic.mtsp.io;

import hr.fer.bernardcrnkovic.mtsp.model.Problem;

import java.util.regex.Pattern;


/**
 * Handles loading TSPLib format problems
 */
public class Loader {
    public Problem loadTSPLib(String tsplibProblem) {
        String[] lines;
        var namePattern = Pattern.compile("NAME:\s*(?<name>.*)\n");
        var commentPattern = Pattern.compile("COMMENT:\s*(?<comment>.*)\n");
        // var commentPattern = Pattern.compile("COMMENT:\s*(?<comment>.*)\n");
        // for (var line : lines) {
            // line.matches();
            // if ())
        // }
        return null;
    }
}
