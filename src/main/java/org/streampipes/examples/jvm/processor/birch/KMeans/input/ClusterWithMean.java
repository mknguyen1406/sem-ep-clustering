package org.streampipes.examples.jvm.processor.birch.KMeans.input;

public class ClusterWithMean extends Cluster {
    private DoubleArray mean;  // the mean of the vectors in this cluster

    DoubleArray sum; // the sum of all vectors in this clusters
    // (used to calculate the mean efficiently)

    private int id; // ID used for assignment of points

    /**
     * Constructor
     * @param vectorsSize the size of the vectors to be stored in this cluster
     */
    public ClusterWithMean(int vectorsSize){
        super();
        sum = new DoubleArray(new double[vectorsSize]);
    }

    /**
     * Setter for the mean of this cluster.
     * @param mean A vector of double that will be set as the mean of this cluster.
     */
    public void setMean(DoubleArray mean){
        this.mean = mean;
    }

    /**
     * Add a vector of doubles to this cluster.
     * @param vector The vector of doubles to be added.
     */
    public void addVector(DoubleArray vector) {
        super.addVector(vector);
        for(int i=0; i < vector.data.length; i++){
            sum.data[i] += vector.data[i];
        }
    }

    /**
     * Getter for the mean
     * @return return the mean of this cluster
     */
    public DoubleArray getmean() {
        return mean;
    }

    /**
     * This method is called by clustering algorithms to recompute the mean
     * of the cluster.
     */
    public void recomputeClusterMean() {
        for(int i=0; i < sum.data.length; i++){
            mean.data[i] = sum.data[i] / vectors.size();
        }
    }


    /**
     * Method to remove a vector from this cluster and update
     * the internal sum of vectors at the same time.
     * @param vector  the vector to be removed
     */
    public void remove(DoubleArray vector) {
        super.remove(vector);
        // remove from sum
        for(int i=0; i < vector.data.length; i++){
            sum.data[i] -= vector.data[i];
        }

    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
