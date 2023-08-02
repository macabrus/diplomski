package hr.fer.bernardcrnkovic.mtsp.model;

import java.util.Arrays;

public class Salesman {
    int depot;
    int[] tour;

    public int getDepot() {
        return depot;
    }

    public void setDepot(int depot) {
        this.depot = depot;
    }

    public int[] getTour() {
        return tour;
    }

    public void setTour(int[] tour) {
        this.tour = tour;
    }

    @Override
    public String toString() {
        return "Salesman{" +
                "depot=" + depot +
                ", tour=" + Arrays.toString(tour) +
                '}';
    }
}
