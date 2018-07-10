package org.streampipes.examples.jvm.testData;

import java.lang.reflect.Array;
import java.util.*;

public class Dataset {

    private int n;  //Amount of records
    private int k;  //Amount of clusters
    private double dist;    //Average distance between clusters
    private double rad; //Average radius of cluster
    private double tol; //Tolerance factor
    private List<Map<String, Object>> dataset;

    public Dataset(int n, int k, double dist, double rad, double tol) {
        this.n = n;
        this.k = k;
        this.dist = dist;
        this.rad = rad;
        this.tol = tol;
        dataset = new ArrayList<>();
    }

    public void createDataset() {

        double r = Math.random();

        for (int i = 0; i < k; i++) {
            int clusterId = i;
            double[] clusterCenter = new double[2];

            clusterCenter[0] = applyTolerance(dist) * i;
            clusterCenter[1] = applyTolerance(dist) * i;

            for (int j = 0; j < n / k; j++) {
                Random randomo = new Random();
                Map<String, Object> record = new HashMap<>();
                record.put("originalID", clusterId);
                record.put("x", (randomo.nextGaussian() * applyTolerance(rad) + clusterCenter[0]));
                record.put("y", (randomo.nextGaussian() * applyTolerance(rad) + clusterCenter[1]));
                dataset.add(record);
            }
        }
        Collections.shuffle(dataset);
    }

    public List<Map<String, Object>> getDataset() {
        return dataset;
    }


    private double applyTolerance(double v) {
        double amp = Math.random() * tol * v * 2 - tol * v;
        return v + amp;
    }
}
