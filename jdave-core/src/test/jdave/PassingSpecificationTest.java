/*
 * Copyright 2006 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jdave;

import java.lang.reflect.Method;

import jdave.runner.SpecRunner;
import junit.framework.TestCase;

/**
 * @author Joni Freeman
 */
public class PassingSpecificationTest extends TestCase {
    private SpecRunner runner;
    private Method actualMethod;
    
    @Override
    protected void setUp() throws Exception {
        runner = new SpecRunner();
    }
    
    public void testShouldPassExpectation() {
        runner.run(new IntegerSpecification(), new SpecRunner.Results() {
            public void expected(Method method) {
                actualMethod = method;
            }
            public void unexpected(Method method) {
            }
        });
        assertEquals("isPositive", actualMethod.getName());
    }
    
    public class IntegerSpecification extends Specification<Integer> {
        @Context
        public class Zero {
            public Integer context() {
                return new Integer(0);
            }
            
            public void isPositive() {
                specify(should.be > -1);
            }
        }
    }
}
