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

import org.streampipes.examples.jvm.config.PeJvmConfig;
import org.streampipes.model.DataProcessorType;
import org.streampipes.model.graph.DataProcessorDescription;
import org.streampipes.model.graph.DataProcessorInvocation;
import org.streampipes.model.schema.PropertyScope;
import org.streampipes.model.util.SepaUtils;
import org.streampipes.sdk.builder.ProcessingElementBuilder;
import org.streampipes.sdk.builder.StreamRequirementsBuilder;
import org.streampipes.sdk.extractor.ProcessingElementParameterExtractor;
import org.streampipes.sdk.helpers.*;
import org.streampipes.wrapper.standalone.ConfiguredEventProcessor;
import org.streampipes.wrapper.standalone.declarer.StandaloneEventProcessingDeclarer;

public class DenStreamController extends StandaloneEventProcessingDeclarer<DenStreamParameters> {

    @Override
    public DataProcessorDescription declareModel() {
        return ProcessingElementBuilder.create("denstream", "DenStream", "DenStream Description")
                .category(DataProcessorType.ENRICH)
                .iconUrl(PeJvmConfig.getIconUrl("Numerical_Filter_Icon_HQ"))
                .requiredStream(StreamRequirementsBuilder.create().requiredPropertyWithUnaryMapping(EpRequirements
                        .numberReq(), Labels.from("x", "Specifies the first property that should be clustered", ""), PropertyScope.NONE).build())
                .requiredStream(StreamRequirementsBuilder.create().requiredPropertyWithUnaryMapping(EpRequirements
                        .numberReq(), Labels.from("y", "Specifies the second property that should be clustered", ""), PropertyScope.NONE).build())
                .outputStrategy(OutputStrategies.append())
                .requiredFloatParameter("epsilonMicro","Epsilon (Micro cluster)", "Specifies epsilon for micro clustering. See description for more information.")
                .requiredFloatParameter("epsilonMacro","Epsilon (Macro cluster)", "Specifies epsilon for macro clustering. See description for more information.")
                .requiredIntegerParameter("minPointsMicro","Minimum points (Micro cluster)", "Specifies the minimal number of points for a micro cluster.")
                .requiredIntegerParameter("minPointsMacro","Minimum points (Macro cluster)", "Specifies the minimal number of points for a macro cluster.")
                .requiredFloatParameter("beta","Beta", "Specifies beta. See description for more information.")
                .requiredFloatParameter("mu","Mu", "Specifies mu. See description for more information. NOTE: beta*mu must be greater than 1.")
                .requiredFloatParameter("lambda","Lambda", "lambda epsilon. See description for more information.")
                .requiredIntegerParameter("initN","Initial number of clusters", "Specifies the initial number of clusters.")
                .supportedProtocols(SupportedProtocols.kafka())
                .supportedFormats(SupportedFormats.jsonFormat())
                .build();

    }

    @Override
    public ConfiguredEventProcessor<DenStreamParameters> onInvocation
            (DataProcessorInvocation sepa) {
        ProcessingElementParameterExtractor extractor = ProcessingElementParameterExtractor.from(sepa);

        Float epsilonMicro = extractor.singleValueParameter("epsilonMicro", Float.class);
        Float epsilonMacro = extractor.singleValueParameter("epsilonMacro", Float.class);
        Integer minPointsMicro = extractor.singleValueParameter("minPointsMicro", Integer.class);
        Integer minPointsMacro = extractor.singleValueParameter("minPointsMacro", Integer.class);
        Float beta = extractor.singleValueParameter("beta", Float.class);
        Float mu = extractor.singleValueParameter("mu", Float.class);
        Float lambda = extractor.singleValueParameter("lambda", Float.class);
        Integer initN = extractor.singleValueParameter("initN", Integer.class);

        String firstProperty = extractor.mappingPropertyValue("x");
//        String firstProperty = SepaUtils.getMappingPropertyName(sepa,
//                "x", true);
        String secondProperty = extractor.mappingPropertyValue("y");
//                SepaUtils.getMappingPropertyName(sepa,
//                "y", true);

        DenStreamParameters staticParam = new DenStreamParameters(sepa,
                epsilonMicro,
                epsilonMacro,
                minPointsMicro,
                minPointsMacro,
                beta,
                mu,
                lambda,
                initN,
                firstProperty,
                secondProperty);

        return new ConfiguredEventProcessor<>(staticParam, () -> new DenStream(staticParam));
    }
}
