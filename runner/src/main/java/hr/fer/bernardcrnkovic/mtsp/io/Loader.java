package hr.fer.bernardcrnkovic.mtsp.io;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import hr.fer.bernardcrnkovic.mtsp.model.Run;

import java.io.File;
import java.io.IOException;


/**
 * Handles loading TSPLib format problems
 */
public class Loader {

    private static final ObjectMapper mapper = new ObjectMapper()
        .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        .setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE)
        .registerModule(new JavaTimeModule());

    public static Run loadFromFile(String path) throws IOException {
        return mapper.readValue(new File(path), Run.class);
    }

    public static Run loadFromResource(String path) throws IOException {
        return mapper.readValue(Loader.class.getResource(path), Run.class);
    }

    public static String dump(Object o) throws JsonProcessingException {
        return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(o);
    }

}
