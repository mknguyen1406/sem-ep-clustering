package org.streampipes.examples.jvm;

import org.streampipes.examples.jvm.processor.denStream.DenStreamParameters;
import org.streampipes.examples.jvm.processor.denStream.DenStream;
import org.streampipes.examples.jvm.testData.Dataset;

import java.util.List;
import java.util.Map;

public class TestRunDenStream {

    public static void main(String[] args) {

        Dataset dataset_object = new Dataset();
        dataset_object.createDataset(10000);
        List<Map<String, Object>> dataset_list = dataset_object.getDataset();

        float epsilonMicro = 3;
        float epsilonMacro = 30;
        int minPointsMicro = 2;
        int minPointsMacro = 3;
        float beta = 0.8f;
        float mu = 3;
        float lambda = 0.25f;
        int initN = 10;
        String firstProperty = "x";
        String secondProperty = "y";

        DenStreamParameters staticParam = new DenStreamParameters(epsilonMicro, epsilonMacro, minPointsMicro, minPointsMacro, beta, mu, lambda, initN, firstProperty, secondProperty);

        DenStream denStream_object = new DenStream(staticParam);

        for (int i = 0; i < dataset_list.size(); i++) {
            Map<String, Object> value = dataset_list.get(i);
            denStream_object.onEvent(value);
        }

    }

}
