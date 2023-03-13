package hr.fer.bernardcrnkovic.mtsp.io;

import hr.fer.bernardcrnkovic.mtsp.model.Problem;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


/**
 * Handles loading TSPLib format problems
 */
public class Loader {
    public static Problem loadTSPLib(String tsplibProblem) {
        Problem problem = new Problem();
        /* Throws stack overflow error... */
        // var pat = Pattern.compile("(?<key>[A-Z_][A-Z0-9_]+)(:\\s*(?<val>.*?)\\n|\\n(?<val2>(.|\\n)*?)(?=\\n[A-Z_][A-Z0-9_]*(:?|\\n)))", Pattern.MULTILINE);
        /* First, parse into hashmap */
        var data = new HashMap<String, String>();
        var lines = tsplibProblem.split("\n");
        for (int i = 0; i < lines.length; i++) {
            var singleLineKey = Pattern.compile("^(?<key>[A-Z][A-Z0-9_]*):\\s+(?<val>.*)$");
            var matcher = singleLineKey.matcher(lines[i]);
            if (matcher.matches()) {
                data.put(matcher.group("key"), matcher.group("val"));
                continue;
            }
            var multiLineKey = Pattern.compile("^(?<key>[A-Z][A-Z0-9_]*)$");
            matcher = multiLineKey.matcher(lines[i]);
            if (matcher.matches()) {
                var val = new StringBuilder();
                for (int j = i + 1;
                     j < lines.length &&
                     !singleLineKey.matcher(lines[j]).matches() &&
                     !multiLineKey.matcher(lines[j]).matches(); j++) {
                    val.append(lines[j]).append(System.lineSeparator());
                }
                System.out.println("KEY: " + matcher.group("key"));
                System.out.println("VAL: " + val.toString());
                data.put(matcher.group("key"), val.toString());
            }
        }

        /* Now handle logic */
        problem.setLabel(data.getOrDefault("NAME", null));
        problem.setDescription(data.getOrDefault("COMMENT", null));
        System.out.println("HERE: " + data.get("EDGE_WEIGHT_FORMAT"));
        switch (data.get("EDGE_WEIGHT_FORMAT").strip()) {
            case "UPPER_ROW" -> {
                lines = data.get("EDGE_WEIGHT_SECTION").split("\n");
                for (int i = 0; i < lines.length; i++) {
                    var nums = lines[i].strip().split("\\s+");
                    for (int j = 0; j < nums.length; j++) {
                        problem.getCosts().put(Map.entry(i, j), Double.parseDouble(nums[j]));
                    }
                }
            }
            default -> throw new RuntimeException("Unknown format");
        }
        System.out.println(problem.getCosts());
        return problem;
    }
}
