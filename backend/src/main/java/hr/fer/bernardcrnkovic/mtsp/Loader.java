package hr.fer.bernardcrnkovic.mtsp;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Loader {
    public Problem loadTSPLib(String path) {
        String[] lines;
        try {
            lines = Files.readString(Path.of(path)).split("\n");
        } catch (IOException e) {
            return null;
        }
        var namePattern = Pattern.compile("NAME: (?<name>.*)\n");
        var commentPattern = Pattern.compile("COMMENT: (?<comment>.*)\n");
        for (var line : lines) {
            line.matches();
            if ())
        }
    }
}
