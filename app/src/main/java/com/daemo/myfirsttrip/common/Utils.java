package com.daemo.myfirsttrip.common;

public class Utils {

    public static String getTag(Object inst) {
        return getTag(inst.getClass());
    }

    private static String getTag(Class clazz) {
        String className = clazz.getSimpleName();
        return className.isEmpty() ? "Anonymous Class" : className;
    }
}