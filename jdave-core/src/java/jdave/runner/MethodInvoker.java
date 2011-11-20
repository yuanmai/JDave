package jdave.runner;

import jdave.Specification;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public interface MethodInvoker {
    void invokeMethod(Method method, Specification<?> spec, Object context)
            throws Throwable;
}
