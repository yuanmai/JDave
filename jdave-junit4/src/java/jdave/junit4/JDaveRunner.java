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
package jdave.junit4;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;

import jdave.IStringComparisonFailure;
import jdave.Specification;
import jdave.runner.SpecRunner;
import jdave.tools.SpecdoxRunner;
import junit.framework.ComparisonFailure;
import org.junit.Rule;
import org.junit.internal.runners.statements.InvokeMethod;
import org.junit.rules.RunRules;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.manipulation.Filter;
import org.junit.runner.manipulation.Filterable;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;
import org.junit.runners.model.TestClass;

/**
 * The JDaveRunner is a JUnit4 test runner implementation for JDave
 * specifications. Just tag your JDave specification class with the
 * <code>@RunWith(JDaveRunner.class)</code> annotation and you're good to go!
 * 
 * @author Lasse Koskela
 */
public class JDaveRunner extends Runner implements Filterable {
    private final Class<? extends Specification<?>> spec;
    private final Description description;
    private Filter filter;

    @SuppressWarnings("unchecked")
    public JDaveRunner(final Class<?> testClass) {
        if (Specification.class.isAssignableFrom(testClass)) {
            spec = (Class<? extends Specification<?>>) testClass;
        } else if (isAContext(testClass)) {
            spec = (Class<? extends Specification<?>>) testClass.getDeclaringClass();
        } else {
            throw new IllegalArgumentException("Testclass is not a Specification or a Context: "
                    + testClass);
        }
        Specification.setStringComparisonFailure(new IStringComparisonFailure() {
            public void fail(final String message, final String expected, final String actual) {
                throw new ComparisonFailure(message, expected, actual);
            }
        });
        description = DescriptionFactory.create(this.spec);
    }

    private boolean isAContext(final Class<?> contextClass) {
        final boolean isMemberClass = contextClass.isMemberClass();
        if (!isMemberClass) {
            return false;
        }
        final boolean isStatic = Modifier.isStatic(contextClass.getModifiers());
        if (isStatic) {
            return false;
        }
        final Class<?> declaringClass = contextClass.getDeclaringClass();
        final boolean declaringClassIsASpecification = Specification.class
                .isAssignableFrom(declaringClass);
        return declaringClassIsASpecification;
    }

    @Override
    public Description getDescription() {
        return description;
    }

    @Override
    public void run(final RunNotifier notifier) {
        (new SpecRunner() {
            public void invokeMethod(Method aMethod, Specification<?> spec, Object context)
                    throws Throwable {
                FrameworkMethod method = new FrameworkMethod(aMethod);
                Statement statement = new InvokeMethod(method, context);
                statement = withTestRules(method, context, statement);
                statement = withTestRules(method, spec, statement);
                statement.evaluate();
            }
        }).run(spec, new JDaveCallback(notifier, filter));
        new SpecdoxRunner().generate(spec);
    }

    protected List<TestRule> getTestRules(Object target) {
        return new TestClass(target.getClass()).getAnnotatedFieldValues(target, Rule.class, TestRule.class);
    }

    private Statement withTestRules(FrameworkMethod method, Object target,
			Statement statement) {
		List<TestRule> testRules= getTestRules(target);
		return testRules.isEmpty() ? statement :
			new RunRules(statement, testRules,  describeChild(method, target.getClass()));
	}

	protected Description describeChild(FrameworkMethod method, Class testClass) {
		return Description.createTestDescription(testClass, method.getName(), method.getAnnotations());
	}

    public void filter(final Filter filter) {
        this.filter = filter;
    }
}
