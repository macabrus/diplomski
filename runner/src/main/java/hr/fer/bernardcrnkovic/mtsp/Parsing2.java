package hr.fer.bernardcrnkovic.mtsp;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import hr.fer.bernardcrnkovic.mtsp.algo.NSGA2;
import hr.fer.bernardcrnkovic.mtsp.model.Run;
import hr.fer.bernardcrnkovic.mtsp.operator.EncDec;

import java.io.IOException;

public class Parsing2 {
    public static void main(String[] args) throws IOException {
        ObjectMapper mapper = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
        var run = mapper.readValue(Parsing.class.getResourceAsStream("/bayg29-run.json"), Run.class);
        EncDec.encodeSolutions(run.state.population, run.problem);
        System.out.println("ok");
        System.out.println(run.problem.getNumSalesmen());
        var nsga = new NSGA2(run);
        nsga.addStopSignalSupplier(() -> false);
        nsga.run();
        //printMatrix(prob.distances);
    }
}
