/*
 * Copyright 2007 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package jdave.unfinalizer;

import java.lang.instrument.Instrumentation;

import jdave.Specification;
import jdave.junit4.JDaveRunner;

import org.jmock.Expectations;
import org.junit.runner.RunWith;

/**
 * @author Tuomas Karkkainen
 */
@RunWith(JDaveRunner.class)
public class InstrumentationAgentSpec extends Specification<Void> {
    public class WhenPremainIsCalled {
        public void unfinalizingTransformerIsAdded() {
            final Instrumentation instrumentation = mock(Instrumentation.class);
            checking(new Expectations() {
                {
                    one(instrumentation).addTransformer(with(any(DelegatingClassFileTransformer.class)));
                }
            });
            InstrumentationAgent.agentmain(null, instrumentation);
        }
    }
}
