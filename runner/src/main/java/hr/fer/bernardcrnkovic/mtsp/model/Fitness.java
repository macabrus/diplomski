package hr.fer.bernardcrnkovic.mtsp.model;

public class Fitness {
    double maxTourLength;
    double totalLength;

    public double getMaxTourLength() {
        return maxTourLength;
    }

    public double getTotalLength() {
        return totalLength;
    }

    public void setMaxTourLength(double maxTourLength) {
        this.maxTourLength = maxTourLength;
    }

    public void setTotalLength(double totalLength) {
        this.totalLength = totalLength;
    }

    @Override
    public String toString() {
        return "(neg_max_tour = %10s, neg_total = %10s)".formatted(maxTourLength, totalLength);
    }
}
