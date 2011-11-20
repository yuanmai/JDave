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
package jdave.runner;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import jdave.ExpectationFailedException;
import jdave.Specification;
import jdave.util.Fields;

/**
 * @author Joni Freeman
 * @author Pekka Enberg
 */
public class ExecutingBehavior extends Behavior {
    private final Class<?> contextType;
    private final Class<? extends Specification<?>> specType;
    private Object context;
    private final MethodInvoker invoker;

    public ExecutingBehavior(Method method, Class<? extends Specification<?>> specType, Class<?> contextType,
                             MethodInvoker invoker) {
        super(contextType, method);
        this.specType = specType;
        this.contextType = contextType;
        this.invoker = invoker;
    }

    public ExecutingBehavior(Method method, Class<? extends Specification<?>> specType, Class<?> contextType) {
        this(method, specType, contextType, new MethodInvoker() {
            public void invokeMethod(Method method, Specification<?> spec, Object context)
                    throws Throwable {
                method.invoke(context);
            }
        });
    }

    @Override
    public void run(final IBehaviorResults results) {
        try {
            Specification<?> spec = newSpecification();
            if (spec.needsThreadLocalIsolation()) {
                runInNewThread(results, spec);
            } else {
                runInCurrentThread(results, spec);
            }
        } catch (Throwable e) {
            results.error(method, e);
        }
    }

    private void runInCurrentThread(final IBehaviorResults results, final Specification<?> spec) {
        runSpec(results, spec);
    }

    private void runInNewThread(final IBehaviorResults results, final Specification<?> spec) {
        ExecutorService executor = Executors.newSingleThreadExecutor(new ThreadFactory() {
            public Thread newThread(Runnable r) {
                return new Thread(r);
            }
        });
        executor.submit(new Callable<Void>() {
            public Void call() throws Exception {
                runSpec(results, spec);
                return null;
            }
        });
        executor.shutdown();
        try {
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void runSpec(IBehaviorResults results, Specification<?> spec) {
        boolean error = false;
        try {
            spec.create();
            context = newContext(spec);
            invoker.invokeMethod(method, spec, context);
            spec.verifyMocks();
            results.expected(method);
        } catch (InvocationTargetException e) {
            error = true;
            if (e.getCause().getClass().equals(ExpectationFailedException.class)) {
                results.unexpected(method, (ExpectationFailedException) e.getCause());
            } else {
                results.error(method, e.getCause());
            }
        } catch (ExpectationFailedException e) {
            error = true;
            results.unexpected(method, e);
        } catch (Throwable t) {
            error = true;
            results.error(method, t);
        } finally {
            try {
                destroy(spec);
            } catch (Throwable e) {
                // Do not mask the first error.
                if (!error) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    private void destroy(Specification<?> spec) throws Exception {
        try {
            destroyContext();
        } finally {
            try {
                spec.fireAfterContextDestroy(context);
            } finally {
                spec.destroy();
            }
        }
    }

    protected Specification<?> newSpecification() {
        try {
            return specType.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected Object newContext(Specification<?> spec) throws Exception {
        Object context = newContextInstance(spec);
        spec.fireAfterContextInstantiation(context);
        Object contextObject = spec.getContextObjectFactory().newContextObject(context);
        Fields.set(spec, "be", contextObject);
        Fields.set(spec, "context", contextObject);
        spec.fireAfterContextCreation(context, contextObject);
        return context;
    }

    protected void destroyContext() throws Exception {
        if (context != null) {
            invokeDisposer(context);
        }
    }

    private Object newContextInstance(Specification<?> spec) {
        try {
            Constructor<?> constructor = contextType.getDeclaredConstructor(contextType.getEnclosingClass());
            return constructor.newInstance(spec);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void invokeDisposer(Object context) throws Exception {
        Method method;
        try {
            method = context.getClass().getMethod(DefaultSpecIntrospection.DISPOSER_NAME);
            method.invoke(context);
        } catch (NoSuchMethodException e) {
            // destroy method is not required
        }
    }
}
