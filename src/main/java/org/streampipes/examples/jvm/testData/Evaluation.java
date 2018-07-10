package org.streampipes.examples.jvm.testData;

import org.mapdb.Atomic;
import org.streampipes.empire.cp.common.utils.base.Math;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;


public class Evaluation {

    private List<Map<String, Object>> outputList;
    private String columnNames;  //E.g. "Id,Name"
    private StringBuilder builder;

    public Evaluation(String columnNames) {
        outputList = new ArrayList<>();
        builder = new StringBuilder();
        this.columnNames = columnNames;

        builder.append(columnNames + "\n");
    }

    public void add(Map<String, Object> in) {
        outputList.add(in);
    }

    /**
     * Adds cluster centers to each data point
     */
    public void addCenters() {
        List<Map<String, Object>> centers = getCenters();

        for (Map<String, Object> center : centers) {
            int clusterId = Integer.parseInt(center.get("assignedID").toString());
            double centerX = Double.parseDouble(center.get("centerX").toString());
            double centerY = Double.parseDouble(center.get("centerY").toString());

            for (Map<String, Object> data : outputList) {
                int dataCluster = Integer.parseInt(data.get("assignedID").toString());
                if (dataCluster == clusterId) {
                    data.put("centerX", centerX);
                    data.put("centerY", centerY);
                }
            }
        }
    }

    /**
     * Adds SSQ to each data point
     */
    public void addSSQ() {

        if (outputList.get(0).keySet().contains("centerX")) { //Only if centers have been calculated already

            for (Map<String, Object> data : outputList) {
                double centerX = Double.parseDouble(data.get("centerX").toString());
                double centerY = Double.parseDouble(data.get("centerY").toString());
                double x = Double.parseDouble(data.get("x").toString());
                double y = Double.parseDouble(data.get("y").toString());

                double distX = centerX - x;
                double distY = centerY - y;
                double squaredX = java.lang.Math.pow(distX, 2);
                double squaredY = java.lang.Math.pow(distY, 2);

                double SSQ = java.lang.Math.sqrt(squaredX + squaredY);

                data.put("SSQ", SSQ);
            }
        }
    }

    public void addIDMapping(){
        List<Map<String, Object>> IDMapping = getIDMapping();

        for (Map<String, Object> data : outputList) {
            int assignedID = Integer.parseInt(data.get("assignedID").toString());
            int mappedID = 0;

            for (Map<String, Object> assignment : IDMapping){
                int mapping = Integer.parseInt(assignment.get("assignedID").toString());
                if (mapping == assignedID) {
                    mappedID = Integer.parseInt(assignment.get("originalID").toString());
                    break;
                }
            }

            data.put("mappedID",mappedID);
        }
    }

    /**
     * Maps each assigned ID with the correspondent original ID
     * original ID is assigned according to number of data points
     */
    private List<Map<String, Object>> getIDMapping() {
        List<Map<String, Object>> assignments = new ArrayList<>();
        List<Map<String, Object>> assignedCenters = getCenters();

        for (Map<String, Object> assignedCenter : assignedCenters) {
            Map<String, Object> assignment = new HashMap<>();
            int assignedID = Integer.parseInt(assignedCenter.get("assignedID").toString());

            assignment.put("assignedID", assignedID);

            //Create an array to count the number of original IDs for one assigned cluster ID
            //Note that the number of assigned clusters is one greater than the number of original clusters
            int[] originalIDCount = new int[assignedCenters.size() - 1];

            for (Map<String, Object> data : outputList) {
                int dataAssignedID = Integer.parseInt(data.get("assignedID").toString());

                if (dataAssignedID == assignedID) {
                    int dataOriginalID = Integer.parseInt(data.get("originalID").toString());
                    originalIDCount[dataOriginalID]++;
                }
            }

            int originalID = getOriginalID(originalIDCount);
            assignment.put("originalID", originalID);

            assignments.add(assignment);
        }
        return assignments;
    }

    private int getOriginalID(int[] idArray) {
        int originalID = 0;
        int maxNumber = 0;
        int count;

        for (int i = 0; i < idArray.length; i++) {
            count = idArray[i];
            if (count > maxNumber) {
                originalID = i;
                maxNumber = count;
            }
        }

        return originalID;
    }


    private List<Map<String, Object>> getCenters() {
        List<Map<String, Object>> centers = new ArrayList<>();
        int k = getClusterNumber();

        for (int i = 0; i <= k; i++) {

            Map<String, Object> center = new HashMap<>();

            double[] sum = new double[2];
            int count = 0;

            for (Map<String, Object> data : outputList) {
                int cluster = Integer.parseInt(data.get("assignedID").toString());
                if (cluster == i) {
                    sum[0] += Double.parseDouble(data.get("x").toString());
                    sum[1] += Double.parseDouble(data.get("y").toString());
                    count++;
                }
            }

            center.put("centerX", sum[0] / count);
            center.put("centerY", sum[1] / count);
            center.put("assignedID", i);

            centers.add(center);
        }

        return centers;
    }

    /**
     * Return the highest cluster id and therefore the number of clusters
     *
     * @return
     */
    private int getClusterNumber() {
        int max = 0;
        for (Map<String, Object> data : outputList) {
            int cluster = Integer.parseInt(data.get("assignedID").toString());
            if (cluster > max)
                max = cluster;
        }
        return max;
    }

    public void generateCSV() {
        PrintWriter pw = null;
        try {
            pw = new PrintWriter(new File("NewData.csv"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        for (Map<String, Object> data : outputList) {
            String line = "";
            for (String key : data.keySet()) {
                line = line + data.get(key) + ",";
            }
            line = line.substring(0, line.length() - 1) + '\n';
            builder.append(line);
        }
        pw.write(builder.toString());
        pw.close();
        System.out.println("done!");
    }

}
