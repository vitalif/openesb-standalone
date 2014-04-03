package net.openesb.standalone.utils;

import java.lang.reflect.Method;

/**
 *
 * @author David BRASSELY (brasseld at gmail.com)
 * @author OpenESB Community
 */
public final class ReflectionUtils {
    
    /**
     * Utility method to invoke a method using reflection. This is kind of a
     * sloppy implementation, since we don't account for overloaded methods.
     *
     * @param obj contains the method to be invoked
     * @param method name of the method to be invoked
     * @param params parameters, if any
     * @return returned object, if any
     */
    public static Object invoke(Object obj, String method, Object... params)
            throws Throwable {
        Object result = null;

        try {
            for (Method m : obj.getClass().getDeclaredMethods()) {
                if (m.getName().equals(method)) {
                    result = m.invoke(obj, params);
                    break;
                }
            }

            return result;
        } catch (java.lang.reflect.InvocationTargetException itEx) {
            throw itEx.getTargetException();
        }
    }
}
