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

package org.streampipes.examples.jvm.processor.denStream;

import org.streampipes.model.graph.DataProcessorInvocation;
import org.streampipes.wrapper.routing.SpOutputCollector;
import org.streampipes.wrapper.standalone.engine.StandaloneEventProcessorEngine;

import java.util.List;

import java.util.Map;
import java.util.ArrayList;

public class DenStream extends StandaloneEventProcessorEngine<DenStreamParameters> {

    private DenStreamParameters params;

    private float epsilonMicro;          //Default value = 3
    private float epsilonMacro;          //Default value = 3
    private int minPointsMicro;          //Default value = 2
    private int minPointsMacro;          //Default value = 2
    private float beta;             //Default value = 0.2
    private float mu;               //Default value = 1 NOTE: beta*mu > 1 !!!
    private float lambda;           //Default value = 0.25
    private int initN;              //Default value = 10

    private int count;
    private List<MicroCluster> p_micro_cluster;
    private List<MicroCluster> o_micro_cluster;
    private List<MicroCluster> macro_cluster;
    private long timestamp = 0;
    private long tp;
    private boolean initialized;
    private List<DenPoint> initBuffer;

    public DenStream(DenStreamParameters params) {
        super(params);

        epsilonMicro = params.getEpsilonMicro();    //Default value = 3
        epsilonMacro = params.getEpsilonMacro();    //Default value = 3
        minPointsMicro = params.getMinPointsMicro();  //Default value = 2
        minPointsMacro = params.getMinPointsMacro();  //Default value = 2
        beta = params.getBeta();          //Default value = 0.2
        mu = params.getMu();              //Default value = 1
        lambda = params.getLambda();      //Default value = 0.25
        initN = params.getInitN();        //Default value = 10

        initialized = false;
        tp = Math.round(1 / lambda * Math.log((beta * mu) / (beta * mu - 1)));
        macro_cluster = new ArrayList<MicroCluster>();
        p_micro_cluster = new ArrayList<MicroCluster>();
        o_micro_cluster = new ArrayList<MicroCluster>();
        initBuffer = new ArrayList<DenPoint>();
    }

    @Override
    public void onInvocation(DenStreamParameters denStreamParameters, DataProcessorInvocation dataProcessorInvocation) {
        this.params = denStreamParameters;
    }

    @Override
    public void onEvent(Map<String, Object> in, String s, SpOutputCollector out) {
        count++;

        Float x = Float.parseFloat(String.valueOf(in.get(params.getFirstProperty())));
        Float y = Float.parseFloat(String.valueOf(in.get(params.getSecondProperty())));

        DenPoint point = new DenPoint(x, y);

        timestamp++;
        point.setTimestamp(timestamp);
        if (!initialized) {
            initBuffer.add(point);
            if (initBuffer.size() >= initN) {
                DBScan(initBuffer, p_micro_cluster, true);
                initialized = true;
            }

        } else {
            Merging(point);
        }

        if (timestamp % tp == 0) {
            List<MicroCluster> removalList = new ArrayList<MicroCluster>();

            for (int i = 0; i < p_micro_cluster.size(); i++) {
                if (p_micro_cluster.get(i).getWeight() < beta * mu) {
                    removalList.add(p_micro_cluster.get(i));
                }
            }

            for (int i = 0; i < removalList.size(); i++) {
                p_micro_cluster.remove(removalList.get(i));
            }

            for (int i = 0; i < o_micro_cluster.size(); i++) {
                long t0 = o_micro_cluster.get(i).getCreationTime();
                float xsi1 = (float) Math.pow(2, (-lambda * (timestamp - t0 + tp))) - 1;
                float xsi2 = (float) Math.pow(2, (-lambda * tp)) - 1;
                float xsi = xsi1 / xsi2;

                if (o_micro_cluster.get(i).getWeight() < xsi) {
                    removalList.add(o_micro_cluster.get(i));
                }
            }

            for (int i = 0; i < removalList.size(); i++) {
                o_micro_cluster.remove(removalList.get(i));
            }

        }

        if ((count % 1000) == 0) { //Recalculate macroclusters after a specified amount of iterations
            calculateMacroclusters();
            System.out.println("Point: " + point.toString() + " Closest MC: " + getClosestMacroclusterId(point));
        }

        out.onEvent(in);
    }

    /**
     * Only for testing purposes
     *
     * @param in
     */
    public void onEvent(Map<String, Object> in) {
        count++;

        float x = Float.parseFloat(in.get("x").toString());
        float y = Float.parseFloat(in.get("y").toString());

        DenPoint point = new DenPoint(x, y);

        timestamp++;
        point.setTimestamp(timestamp);
        if (!initialized) {
            initBuffer.add(point);
            if (initBuffer.size() >= initN) {
                DBScan(initBuffer, p_micro_cluster, true);
                initialized = true;
            }

        } else {
            Merging(point);
        }

        if (timestamp % tp == 0) {
            List<MicroCluster> removalList = new ArrayList<MicroCluster>();

            for (int i = 0; i < p_micro_cluster.size(); i++) {
                if (p_micro_cluster.get(i).getWeight() < beta * mu) {
                    removalList.add(p_micro_cluster.get(i));
                }
            }

            for (int i = 0; i < removalList.size(); i++) {
                p_micro_cluster.remove(removalList.get(i));
            }

            for (int i = 0; i < o_micro_cluster.size(); i++) {
                long t0 = o_micro_cluster.get(i).getCreationTime();
                float xsi1 = (float) Math.pow(2, (-lambda * (timestamp - t0 + tp))) - 1;
                float xsi2 = (float) Math.pow(2, (-lambda * tp)) - 1;
                float xsi = xsi1 / xsi2;

                if (o_micro_cluster.get(i).getWeight() < xsi) {
                    removalList.add(o_micro_cluster.get(i));
                }
            }

            for (int i = 0; i < removalList.size(); i++) {
                o_micro_cluster.remove(removalList.get(i));
            }

        }

        if ((count % 1000) == 0) { //Recalculate macroclusters after a specified amount of iterations
            calculateMacroclusters();
            System.out.println("Point: " + point.x + ", " + point.y + " Closest MC: " + getClosestMacroclusterId(point));
        }


    }

    @Override
    public void onDetach() {

    }

    public void DBScan(List<DenPoint> initBuffer, List<MicroCluster> cluster_list, boolean micro) {

        List<MicroCluster> tempClusterList = new ArrayList<>();

        for (int i = 0; i < initBuffer.size(); i++) {
            DenPoint point = initBuffer.get(i);
            if (!point.covered) {
                point.covered = true;
                List<Integer> neighbourhood = getNeighbourhoodIDs(point, initBuffer, micro);
                int minPoints;
                if (micro) {     //Different minPoints for micro and macro clustering
                    minPoints = minPointsMicro;
                } else {
                    minPoints = minPointsMacro;
                }
                if (neighbourhood.size() >= minPoints) {
                    MicroCluster mc = new MicroCluster(point.toArray(), 2, timestamp, lambda, timestamp);
                    expandCluster(mc, initBuffer, neighbourhood, micro);
                    if (micro) {
                        cluster_list.add(mc);
                    } else {
                        //For macro clustering create a new list every time and overwrite
                        tempClusterList.add(mc);
                    }
                } else {
                    point.covered = false;
                }
            }

        }

        if (!micro)
            macro_cluster = tempClusterList;
    }

    /**
     * Calculates final clusters after offline DBScan of potential micro clusters
     */
    public void calculateMacroclusters() {

        List<DenPoint> mc_centroids = new ArrayList<DenPoint>();

        for (MicroCluster mc : p_micro_cluster) {
            mc_centroids.add(mc.getCenter(timestamp, false));
        }

        DBScan(mc_centroids, macro_cluster, false);

        int count = 1;
        if (macro_cluster.size() > 0) {
            for (MicroCluster mc : macro_cluster) {
                mc.setId(count);
                count++;
            }
        }

        System.out.println("Macroclusters:");
        for (MicroCluster c : macro_cluster) {
            DenPoint center = c.getCenter(0, false);
            System.out.println("X: " + center.x + ", Y: " + center.y + ", ID: " + c.getId());
        }
    }

    public int getClosestMacroclusterId(DenPoint point) {
        int id = 0;

        if (macro_cluster.size() > 0) {
            MicroCluster nearestMacroCluster = nearestCluster(point, macro_cluster, false);
            return nearestMacroCluster.getId();
        } else {
            return id;
        }
    }

    public List<Integer> getNeighbourhoodIDs(DenPoint point, List<DenPoint> points, boolean micro) {
        List<Integer> neighbourIDs = new ArrayList<Integer>();
        for (int i = 0; i < points.size(); i++) {
            DenPoint testPoint = points.get(i);
            if (!testPoint.covered) {
                float dist = Distance(testPoint, point);
                float epsilon;
                if (micro) { //different epsilon for micro and macro clustering
                    epsilon = epsilonMicro;
                } else {
                    epsilon = epsilonMacro;
                }
                if (dist < epsilon) {
                    neighbourIDs.add(i);
                }
            }
        }
        return neighbourIDs;
    }

    public void expandCluster(MicroCluster mc, List<DenPoint> points, List<Integer> neighbourhood, boolean micro) {
        for (int i = 0; i < neighbourhood.size(); i++) {
            DenPoint testPoint = points.get(neighbourhood.get(i));
            if (!testPoint.covered) {
                testPoint.covered = true;
                mc.insert(testPoint, timestamp);
                List<Integer> neighbourhood2 = getNeighbourhoodIDs(testPoint, initBuffer, micro);
                int minPoints;
                if (micro) {     //Different minPoints for micro and macro clustering
                    minPoints = minPointsMicro;
                } else {
                    minPoints = minPointsMacro;
                }
                if (neighbourhood.size() >= minPoints) {
                    expandCluster(mc, points, neighbourhood2, micro);
                }
            }
        }
    }

    public void Merging(DenPoint point) {
        Boolean merged = false;
        if (p_micro_cluster.size() != 0) {
            MicroCluster x = nearestCluster(point, p_micro_cluster, true);
            MicroCluster xCopy = x.copy();

            xCopy.insert(point, timestamp);

            if (xCopy.getRadius(timestamp, true) <= epsilonMicro) {
                x.insert(point, timestamp);
                merged = true;
            }
        }

        if (!merged && o_micro_cluster.size() != 0) {
            MicroCluster x = nearestCluster(point, o_micro_cluster, true);
            MicroCluster xCopy = x.copy();
            xCopy.insert(point, timestamp);

            if (xCopy.getRadius(timestamp, true) <= epsilonMicro) {
                x.insert(point, timestamp);
                merged = true;
                if (x.getWeight() > beta * mu) {
                    o_micro_cluster.remove(x);
                    p_micro_cluster.add(x);
                }
            }
        }

        if (!merged) {
            o_micro_cluster.add(new MicroCluster(point.toArray(), 2, timestamp, lambda, timestamp));
        }
    }

    public MicroCluster nearestCluster(DenPoint point, List<MicroCluster> cl, boolean micro) {
        MicroCluster min = null;
        float minDist = 0;
        for (int c = 0; c < cl.size(); c++) {
            MicroCluster cluster = cl.get(c);
            if (min == null) {
                min = cluster;
            }
            float dist = Distance(point, cluster.getCenter(timestamp, micro));
            dist -= cluster.getRadius(timestamp, micro);

            if (c == 0) {
                minDist = dist;
            } else {
                if (dist < minDist) {
                    minDist = dist;
                    min = cluster;

                }
            }
        }

        return min;

    }

    public float Distance(DenPoint a, DenPoint b) {
        float sumSquaredDiffs = 0;
        float tempX = 0;
        float tempY = 0;

        tempX = a.x - b.x;
        tempY = a.y - b.y;
        sumSquaredDiffs = tempX * tempX + tempY * tempY;

        return (float) Math.sqrt(sumSquaredDiffs);
    }
}
