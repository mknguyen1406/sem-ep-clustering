package org.streampipes.examples.jvm.processor.birch.KMeans;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.streampipes.examples.jvm.processor.birch.KMeans.input.ClusterWithMean;
import org.streampipes.examples.jvm.processor.birch.KMeans.input.DoubleArray;


/**
 * An implementation of the K-means algorithm (J. MacQueen, 1967).
 * <br/><br/>
 *
 * The K-means  algorithm steps are (text from Wikipedia) : 1) Choose the number of clusters, k.
 * * 2) Randomly generate k clusters and determine the cluster centers, or directly
 * generate k random points as cluster centers. 3) Assign each point to the
 * nearest cluster center. 4) Recompute the new cluster centers. 5) Repeat the two
 * previous steps until some convergence criterion is met (usually that the
 * assignment hasn't changed).
 *
 * @author Philippe Fournier-Viger
 */
public class AlgoKMeans {

    // The list of clusters generated
    protected List<ClusterWithMean> clusters = null;

    // A random number generator because K-Means is a randomized algorithm
    protected final static Random random = new Random(System.currentTimeMillis());

    // For statistics
    protected long startTimestamp; // the start time of the latest execution
    protected long endTimestamp;  // the end time of the latest execution
    long iterationCount; // the number of iterations that was performed

    /**
     * Default constructor
     */
    public AlgoKMeans() {

    }

    /**
     * Run the K-Means algorithm
     * @param instances an input file containing a list of vectors of double values
     * @param k the parameter
     * @return a list of clusters (some of them may be empty)
     */
    public List<ClusterWithMean> runAlgorithm(List<DoubleArray> instances, int k)  {
        // record the start time
        startTimestamp =  System.currentTimeMillis();
        // reset the number of iterations
        iterationCount =0;

        // variables to store the minimum and maximum values in vectors
        double minValue = Integer.MAX_VALUE;
        double maxValue = 0;


        // For each instance
        for(DoubleArray instance : instances){
            for(double value : instance.data){
                if(value < minValue){
                    minValue = value;
                }
                if(value > maxValue){
                    maxValue = value;
                }
            }
        }

        // Get the size of vectors
        int vectorsSize = instances.get(0).data.length;

        // if the user ask for only one cluster!
        if(k == 1) {
            // Create a single cluster and return it
            clusters = new ArrayList<ClusterWithMean>();
            ClusterWithMean cluster = new ClusterWithMean(vectorsSize);
            for(DoubleArray vector : instances) {
                cluster.addVector(vector);
            }
            cluster.setMean(new DoubleArray(new double[vectorsSize]));
            cluster.recomputeClusterMean();
            clusters.add(cluster);

            // record end time
            endTimestamp =  System.currentTimeMillis();
            return clusters;
        }

        // SPECIAL CASE: If only one vector
        if (instances.size() == 1) {
            // Create a single cluster and return it
            clusters = new ArrayList<ClusterWithMean>();
            DoubleArray vector = instances.get(0);
            ClusterWithMean cluster = new ClusterWithMean(vectorsSize);
            cluster.addVector(vector);
            cluster.recomputeClusterMean();
            cluster.setMean(new DoubleArray(new double[vectorsSize]));
            clusters.add(cluster);

            // record end time
            endTimestamp =  System.currentTimeMillis();
            return clusters;
        }

        // if the user asks for more cluster then there is data,
        // we set k to the number of data points.
        if(k > instances.size()) {
            k = instances.size();
        }

        applyAlgorithm(k, instances, minValue, maxValue,
                vectorsSize);

        // record end time
        endTimestamp =  System.currentTimeMillis();

        // return the clusters
        return clusters;
    }

    /**
     * Apply the K-means algorithm
     * @param k the parameter k
     * @param vectors the list of initial vectors
     * @param minValue the min value
     * @param maxValue the max value
     * @param vectorsSize  the vector size
     */
    void applyAlgorithm(int k,
                        List<DoubleArray> vectors, double minValue, double maxValue,
                        int vectorsSize) {
        // apply kmeans
        clusters = applyKMeans(k, vectors, minValue, maxValue, vectorsSize);
    }

    /**
     * Apply the K-means algorithm
     * @param k the parameter
     * @param vectors the list of initial vectors
     * @param minValue the min value
     * @param maxValue the max value
     * @param vectorsSize  the vector size
     */
    List<ClusterWithMean> applyKMeans(int k,
                                      List<DoubleArray> vectors, double minValue, double maxValue,
                                      int vectorsSize) {
        List<ClusterWithMean> newClusters = new ArrayList<ClusterWithMean>();

        // SPECIAL CASE: If only one vector
        if (vectors.size() == 1) {
            // Create a single cluster and return it
            DoubleArray vector = vectors.get(0);
            ClusterWithMean cluster = new ClusterWithMean(vectorsSize);
            cluster.addVector(vector);
            newClusters.add(cluster);
            return newClusters;
        }

        // (1) Randomly generate k empty clusters with a random mean (cluster
        // center)
        for(int i=0; i< k; i++){
            DoubleArray meanVector = generateRandomVector(minValue, maxValue, vectorsSize);
            ClusterWithMean cluster = new ClusterWithMean(vectorsSize);
            cluster.setMean(meanVector);
            newClusters.add(cluster);
        }

        // (2) Repeat the two next steps until the assignment hasn't changed
        boolean changed;
        while(true) {
            iterationCount++;
            changed = false;
            // (2.1) Assign each point to the nearest cluster center.

            // / for each vector
            for (DoubleArray vector : vectors) {
                // find the nearest cluster and the cluster containing the item
                ClusterWithMean nearestCluster = null;
                ClusterWithMean containingCluster = null;
                double distanceToNearestCluster = Double.MAX_VALUE;

                // for each cluster
                for (ClusterWithMean cluster : newClusters) {
                    // calculate the distance of the cluster mean to the vector
                    double distance = calculateDistance(cluster.getmean(), vector);
                    // if it is the smallest distance until now, record this cluster
                    // and the distance
                    if (distance < distanceToNearestCluster) {
                        nearestCluster = cluster;
                        distanceToNearestCluster = distance;
                    }
                    // if the cluster contain the vector already,
                    // remember that too!
                    if (cluster.contains(vector)) {
                        containingCluster = cluster;
                    }
                }

                // if the nearest cluster is not the cluster containing
                // the vector
                if (containingCluster != nearestCluster) {
                    // remove the vector from the containing cluster
                    if (containingCluster != null) {
                        containingCluster.remove(vector);
                    }
                    // add the vector to the nearest cluster
                    nearestCluster.addVector(vector);
                    changed = true;
                }
            }

            if(!changed){     // exit condition for main loop
                break;
            }

            // (2.2) Recompute the new cluster means
            for (ClusterWithMean cluster : newClusters) {
                cluster.recomputeClusterMean();
            }
        }

        return newClusters;
    }

    /**
     * Generate a random vector.
     * @param minValue  the minimum value allowed
     * @param maxValue  the maximum value allowed
     * @param vectorsSize the desired vector size
     * @return the random vector
     */
    DoubleArray generateRandomVector(double minValue, double maxValue,
                                     int vectorsSize) {
        // create a new vector
        double[] vector = new double[vectorsSize];
        // for each position generate a random number
        for(int i=0; i < vectorsSize; i++){
            vector[i] = (random.nextDouble() * (maxValue - minValue)) + minValue;
        }
        // return the vector
        return new DoubleArray(vector);
    }

    double calculateDistance(DoubleArray vector1, DoubleArray vector2) {
        double sum =0;
        for(int i=0; i< vector1.data.length; i++){
            sum += Math.pow(vector1.data[i] - vector2.data[i], 2);
        }
        return Math.sqrt(sum);
    }
}
