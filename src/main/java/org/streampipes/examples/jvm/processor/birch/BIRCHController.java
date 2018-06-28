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

public class BIRCHController extends StandaloneEventProcessingDeclarer<BIRCHParameters> {

    @Override
    public DataProcessorDescription declareModel() {
        return ProcessingElementBuilder.create("birch", "BIRCH", "BIRCH Description")
                .category(DataProcessorType.ENRICH)
                .iconUrl(PeJvmConfig.getIconUrl("Numerical_Filter_Icon_HQ"))
//                .requiredStream(StreamRequirementsBuilder.create().requiredPropertyWithUnaryMapping(EpRequirements
//                        .numberReq(), Labels.from("x", "Specifies the first property that should be clustered", ""), PropertyScope.NONE).build())
//                .requiredStream(StreamRequirementsBuilder.create().requiredPropertyWithUnaryMapping(EpRequirements
//                        .numberReq(), Labels.from("y", "Specifies the second property that should be clustered", ""), PropertyScope.NONE).build())

                .requiredStream(
                        StreamRequirementsBuilder.create()
                                .requiredPropertyWithUnaryMapping(
                                        EpRequirements.numberReq(),
                                        Labels.from("x", "Specifies the first property that should be clustered", ""),
                                        PropertyScope.NONE)
                                .requiredPropertyWithUnaryMapping(
                                        EpRequirements.numberReq(),
                                        Labels.from("y", "Specifies the second property that should be clustered", ""),
                                        PropertyScope.NONE)
                                .build())
                .outputStrategy(OutputStrategies.append(
                        EpProperties.integerEp(Labels.empty(),"cluster","http://cluster.de")
                ))
                .requiredFloatParameter("maxNodeEntries","Maximum Node Entries", "Specifies the maximum number of entries a node can comprise..")
                .requiredFloatParameter("distTreshold","Radius Threshold", "Specifies the maximum radius for a leaf node.")
                .supportedProtocols(SupportedProtocols.kafka())
                .supportedFormats(SupportedFormats.jsonFormat())
                .build();

    }

    @Override
    public ConfiguredEventProcessor<BIRCHParameters> onInvocation
            (DataProcessorInvocation sepa) {
        ProcessingElementParameterExtractor extractor = ProcessingElementParameterExtractor.from(sepa);

        Integer maxNodeEntries= extractor.singleValueParameter("maxNodeEntries", Integer.class);
        Float distTreshold = extractor.singleValueParameter("distTreshold", Float.class);


        String firstProperty = SepaUtils.getMappingPropertyName(sepa,
                "x", true);
        String secondProperty = SepaUtils.getMappingPropertyName(sepa,
                "y", true);

        BIRCHParameters staticParam = new BIRCHParameters(sepa, maxNodeEntries, distTreshold, firstProperty, secondProperty);

        return new ConfiguredEventProcessor<>(staticParam, () -> new BIRCH(staticParam));
    }
}
