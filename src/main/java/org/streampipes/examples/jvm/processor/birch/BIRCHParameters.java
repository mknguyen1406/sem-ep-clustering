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

import org.streampipes.model.graph.DataProcessorInvocation;
import org.streampipes.wrapper.params.binding.EventProcessorBindingParams;

public class BIRCHParameters extends EventProcessorBindingParams {

    private int maxNodeEntries;     //This is the branching factor B
    private float distTreshold;     //This is the leaf threshold R
    private int numClusters;        //This is the number of macroclusters k
    private String firstProperty;   //This is the first data stream property
    private String secondProperty;  //This is the second data stream property

    public BIRCHParameters(DataProcessorInvocation graph, Integer maxNodeEntries, Float distTreshold, Integer numClusters,String firstProperty, String secondProperty) {
        super(graph);
        this.numClusters = numClusters;
        this.maxNodeEntries = maxNodeEntries;
        this.distTreshold= distTreshold;
        this.firstProperty = firstProperty;
        this.secondProperty = secondProperty;
    }

    /**
     * Only for testing purposes!
     * @param maxNodeEntries
     * @param distTreshold
     * @param firstProperty
     * @param secondProperty
     */
    public BIRCHParameters(Integer maxNodeEntries, Float distTreshold, Integer numClusters, String firstProperty, String secondProperty) {
        this.maxNodeEntries = maxNodeEntries;
        this.distTreshold= distTreshold;
        this.numClusters = numClusters;
        this.firstProperty = firstProperty;
        this.secondProperty = secondProperty;
    }

    public int getMaxNodeEntries() {
        return maxNodeEntries;
    }

    public float getDistTreshold() {
        return distTreshold;
    }

    public int getNumClusters() { return numClusters; }

    public  String getFirstProperty(){ return firstProperty; }

    public  String getSecondProperty(){ return secondProperty; }

}
