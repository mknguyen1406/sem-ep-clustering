package org.streampipes.examples.jvm.testData;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Dataset {

    private List<Map<String, Object>> dataset;

    public void createDataset(int n) {

        dataset = new ArrayList<>();

        for (int i = 0; i < n; i++) {

            if (Math.random()<0.5){
                Map<String, Object> values = new HashMap<String, Object>();

                values.put("x", (Double)(Math.random() * 25));
                values.put("y", (Double)(Math.random() * 25));

                dataset.add(values);
            } else{
                Map<String, Object> values = new HashMap<String, Object>();

                values.put("x", (Double)((Math.random() * 25)+200));
                values.put("y", (Double)((Math.random() * 25)+200));

                dataset.add(values);
            }
        }
    }

    public List<Map<String, Object>> getDataset() {
        return dataset;
    }


}
