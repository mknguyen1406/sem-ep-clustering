/*
 * Copyright 2017 FZI Forschungszentrum Informatik
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.streampipes.examples.jvm.processor.birch;

import org.streampipes.examples.jvm.processor.birch.KMeans.AlgoKMeans;
import org.streampipes.examples.jvm.processor.birch.KMeans.input.ClusterWithMean;
import org.streampipes.examples.jvm.processor.birch.KMeans.input.DoubleArray;
import org.streampipes.examples.jvm.processor.birch.cftree.CFEntry;
import org.streampipes.examples.jvm.processor.birch.cftree.CFTree;
import org.streampipes.model.graph.DataProcessorInvocation;
import org.streampipes.wrapper.routing.SpOutputCollector;
import org.streampipes.wrapper.standalone.engine.StandaloneEventProcessorEngine;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BIRCH extends StandaloneEventProcessorEngine<BIRCHParameters> {

    private BIRCHParameters params;

    private int maxNodeEntries;
    private float distTreshold;
    private int numClusters;

    private int count;                              //Number of processed data evnts
    private int macroIterations;                    //Number of iterations until offline phase
    public CFTree cfTree;                           //CF tree
    public List<ClusterWithMean> macroclusters;     //List of macroclusters

    public BIRCH(BIRCHParameters params) {
        super(params);
        maxNodeEntries = params.getMaxNodeEntries();
        distTreshold = params.getDistTreshold();
        numClusters = params.getNumClusters();
        cfTree = new CFTree(maxNodeEntries, distTreshold);
        macroclusters = new ArrayList<>();
        macroIterations = 1000;
    }

    @Override
    public void onInvocation(BIRCHParameters BIRCHParameters, DataProcessorInvocation dataProcessorInvocation) {
        this.params = BIRCHParameters;
    }

    @Override
    public void onEvent(Map<String, Object> in, String s, SpOutputCollector out) {
        count++;

        double[] x = new double[2];

        x[0] = Double.parseDouble(String.valueOf(in.get(params.getFirstProperty())));
        x[1] = Double.parseDouble(String.valueOf(in.get(params.getSecondProperty())));

        // Read one instace at a time from the dataset
        // training birch, one instance at a time...
        boolean inserted = cfTree.insertEntry(x);
        if (!inserted) {
            System.err.println("ERROR: NOT INSERTED!");
            System.exit(1);
        }


        if ((count % macroIterations) == 0) { //Recalculate macroclusters after a specified amount of iterations
            calculateMacroclusters(numClusters);
            System.out.println("Point: "+x[0]+ ", "+x[1]+" Closest MC: "+getClosestMacroclusterId(x));
        }

        in.put("cluster", getClosestMacroclusterId(x));

        out.onEvent(in);
    }

    @Override
    public void onDetach() {

    }

    /**
     * Only for testing purposes!
     *
     * @param in
     */
    public void onEvent(Map<String, Object> in) {
        count++;

        double[] x = new double[2];

        x[0] = Double.parseDouble(String.valueOf(in.get(params.getFirstProperty())));
        x[1] = Double.parseDouble(String.valueOf(in.get(params.getSecondProperty())));

        // Read one instace at a time from the dataset
        // training birch, one instance at a time...
        boolean inserted = cfTree.insertEntry(x);
        if (!inserted) {
            System.err.println("ERROR: NOT INSERTED!");
            System.exit(1);
        }


        if ((count % macroIterations) == 0) { //Recalculate macroclusters after a specified amount of iterations
            calculateMacroclusters(numClusters);
            System.out.println("Point: "+x[0]+ ", "+x[1]+" Closest MC: "+getClosestMacroclusterId(x));
        }

        in.put("cluster", getClosestMacroclusterId(x));
    }

    public CFTree getCfTree() {
        return cfTree;
    }

    /**
     * Calculates macrocluster based on current microclusters
     * Is called after a specified amount of iterations
     * @param k
     */
    void calculateMacroclusters(int k) {

        //birch_tree.finishedInsertingData();

        List<CFEntry> microclusters = cfTree.getLeafEntries();  //Get all microclusters in the leafs of the birch tree
        List<DoubleArray> instances = new ArrayList<>(); //Creates ArrayList with instances for kMeans
        List<DoubleArray> seeds = new ArrayList<>(); //Creates ArrayList with seeds for kMeans

        for (CFEntry e : microclusters) {
            DoubleArray instance = new DoubleArray(e.getCenter());  //Adds each microcluster center to instances list for kMeans
            instances.add(instance);
        }

        AlgoKMeans km = new AlgoKMeans();

        if (macroclusters.size()!=0){
            for (ClusterWithMean c : macroclusters)
                seeds.add(c.getmean());
        }

        macroclusters = km.runAlgorithm(instances, k, seeds); //Run kMeans with macrocluster center as seeds

        int count = 1;
        for (ClusterWithMean c : macroclusters) {
            c.setId(count);
            count ++;
        }

        System.out.println("Macroclusters:");
        for (ClusterWithMean c : macroclusters) {
            System.out.println(c.getmean().toString()+" ID: "+c.getId());
        }

    }

    /**
     * Finds id of closest macrocluster
     * @param x
     * @return id of closest macrocluster
     */
    int getClosestMacroclusterId(double[] x) {
        int id = 0;
        double minDist = Double.MAX_VALUE;

        if (macroclusters!=null) {
            for (ClusterWithMean c : macroclusters) {
                DoubleArray mean = c.getmean();
                DoubleArray point = new DoubleArray(x);

                if (calculateDistance(mean, point) < minDist) {
                    minDist = calculateDistance(mean, point);
                    id = c.getId();
                }
            }
        }

        return id;
    }

    /**
     * Calculates distance between two data points
     * @param vector1
     * @param vector2
     * @return distance between two data points
     */
    double calculateDistance(DoubleArray vector1, DoubleArray vector2) {
        double sum = 0;
        for (int i = 0; i < vector1.data.length; i++) {
            sum += Math.pow(vector1.data[i] - vector2.data[i], 2);
        }
        return Math.sqrt(sum);
    }
}
