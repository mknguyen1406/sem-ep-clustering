package org.streampipes.examples.jvm;

import org.streampipes.examples.jvm.processor.birch.BIRCHParameters;
import org.streampipes.examples.jvm.processor.birch.cftree.CFTree;
import org.streampipes.examples.jvm.processor.birch.sizeOf.ObjectSizeFetcher;
import org.streampipes.examples.jvm.testData.Dataset;

import java.util.List;
import java.util.Map;

import org.streampipes.examples.jvm.processor.birch.BIRCH;

public class TestRunBIRCH {

    public static void main(String[] args) {

        Dataset dataset_object = new Dataset();
        dataset_object.createDataset(10000);
        List<Map<String, Object>> dataset_list = dataset_object.getDataset();

        int maxNodeEntries = 3;
        float distTreshold = 25;
        int memoryLimit = 1000; // in MB
        int memoryLimitPeriodicCheck = 10000;  // verify memory usage after every 10000 inserted instances
        String firstProperty = "x";
        String secondProperty = "y";

        BIRCHParameters staticParam = new BIRCHParameters(maxNodeEntries, distTreshold, firstProperty, secondProperty);

        BIRCH birchObject = new BIRCH(staticParam);
        CFTree birchTree = birchObject.getBirchTree();
        // comment the following three lines, if you do not want auto rebuild based on memory usage constraints
        // if auto-rebuild is not active, you need to set distThreshold by hand
        birchTree.setAutomaticRebuild(true);
        birchTree.setMemoryLimitMB(memoryLimit);
        birchTree.setPeriodicMemLimitCheck(memoryLimitPeriodicCheck); // verify memory usage after every memoryLimitPeriodicCheck


        for (int i = 0; i < dataset_list.size(); i++) {
            Map<String, Object> value = dataset_list.get(i);
            birchObject.onEvent(value);
        }

        System.out.println(ObjectSizeFetcher.getObjectSize(birchTree));

    }

}
