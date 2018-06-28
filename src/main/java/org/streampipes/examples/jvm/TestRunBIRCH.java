package org.streampipes.examples.jvm;

import org.streampipes.examples.jvm.processor.birch.BIRCHParameters;
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
        String firstProperty = "x";
        String secondProperty = "y";

        BIRCHParameters staticParam = new BIRCHParameters(maxNodeEntries, distTreshold, firstProperty, secondProperty);

        BIRCH birch_object = new BIRCH(staticParam);

        for (int i = 0; i < dataset_list.size(); i++) {
            Map<String, Object> value = dataset_list.get(i);
            birch_object.onEvent(value);
        }

    }

}
