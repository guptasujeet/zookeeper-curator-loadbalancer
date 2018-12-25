package com.example.common;

/**
 * Created by Sujeet on 25/12/18.
 */
public class LoadMetric {

    private double load;

    public LoadMetric() {
    }

    public LoadMetric(double load) {
        this.load = load;
    }

    public double getLoad() {
        return load;
    }

    public void setLoad(double load) {
        this.load = load;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        LoadMetric that = (LoadMetric) o;

        return Double.compare(that.load, load) == 0;
    }

    @Override
    public int hashCode() {
        long temp = Double.doubleToLongBits(load);
        return (int) (temp ^ (temp >>> 32));
    }

    @Override
    public String toString() {
        return "LoadMetric{" +
                "load=" + load +
                '}';
    }
}
