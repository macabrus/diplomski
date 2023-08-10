package hr.fer.bernardcrnkovic.mtsp;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import hr.fer.bernardcrnkovic.mtsp.model.Problem;
import hr.fer.bernardcrnkovic.mtsp.model.Run;

import java.io.IOException;

public class Parsing2 {
    public static void main(String[] args) throws IOException {
        ObjectMapper mapper = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
        var prob = mapper.readValue(Parsing.class.getResourceAsStream("/bayg29-run.json"), Run.class);
        //printMatrix(prob.distances);
    }
}
