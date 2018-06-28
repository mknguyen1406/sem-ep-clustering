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
import org.streampipes.wrapper.params.binding.EventProcessorBindingParams;

public class DenStreamParameters extends EventProcessorBindingParams {

    private float epsilonMicro;
    private float epsilonMacro;     //Used for DBScan of macroclusters
    private int minPointsMicro;
    private int minPointsMacro;     //Used for DBScan of macroclusters
    private float beta;
    private float mu;
    private float lambda;
    private int initN;
    private String firstProperty;
    private String secondProperty;

    public DenStreamParameters(DataProcessorInvocation graph,
                               Float epsilonMicro,
                               Float epsilonMacro,
                               Integer minPointsMicro,
                               Integer minPointsMacro,
                               Float beta,
                               Float mu,
                               Float lambda,
                               Integer initN,
                               String firstProperty,
                               String secondProperty)
    {
        super(graph);
        this.epsilonMicro = epsilonMicro;
        this.epsilonMacro = epsilonMacro;
        this.minPointsMicro = minPointsMicro;
        this.minPointsMacro = minPointsMacro;
        this.beta = beta;
        this.mu = mu;
        this.lambda = lambda;
        this.initN = initN;
        this.firstProperty = firstProperty;
        this.secondProperty = secondProperty;
    }

    /**
     * Only for testing purposes!
     * @param epsilonMicro
     * @param beta
     * @param mu
     * @param lambda
     * @param initN
     * @param minPointsMicro
     * @param firstProperty
     * @param secondProperty
     */
    public DenStreamParameters(Float epsilonMicro,
                               Float epsilonMacro,
                               Integer minPointsMicro,
                               Integer minPointsMacro,
                               Float beta,
                               Float mu,
                               Float lambda,
                               Integer initN,
                               String firstProperty,
                               String secondProperty)
    {
        this.epsilonMicro = epsilonMicro;
        this.epsilonMacro = epsilonMacro;
        this.minPointsMicro = minPointsMicro;
        this.minPointsMacro = minPointsMacro;
        this.beta = beta;
        this.mu = mu;
        this.lambda = lambda;
        this.initN = initN;
        this.firstProperty = firstProperty;
        this.secondProperty = secondProperty;
    }

    public float getEpsilonMicro() {
        return epsilonMicro;
    }

    public float getEpsilonMacro() {
        return epsilonMacro;
    }

    public int getMinPointsMicro() {
        return minPointsMicro;
    }

    public int getMinPointsMacro() {
        return minPointsMacro;
    }

    public float getBeta() {
        return beta;
    }

    public float getMu() {
        return mu;
    }

    public float getLambda() {
        return lambda;
    }

    public int getInitN() {
        return initN;
    }

    public  String getFirstProperty(){ return firstProperty; }

    public  String getSecondProperty(){ return secondProperty; }
}
