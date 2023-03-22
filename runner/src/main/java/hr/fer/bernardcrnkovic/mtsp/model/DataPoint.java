package hr.fer.bernardcrnkovic.mtsp.model;

import java.time.Instant;

public class DataPoint {
    public DataPoint(Instant x, double y) {
        this.x = x;
        this.y = y;
    }
    /* Unix epoch format */
    Instant x;

    /* Value */
    double y;


    public Instant getX() {
        return x;
    }

    public double getY() {
        return y;
    }
}
