package com.daemo.myfirsttrip.common;

import android.content.Intent;
import android.os.Bundle;

import java.util.Collection;
import java.util.Locale;

public class Utils {

    public static String getTag(Object inst) {
        return getTag(inst.getClass());
    }

    private static String getTag(Class clazz) {
        String className = clazz.getSimpleName();
        return className.isEmpty() ? "Anonymous Class" : className;
    }

    public static String debugIntent(Intent data) {
        if (data == null) return "Intent is null";
        String msg = "Intent has action: " + data.getAction() + "\n";
        msg += "and extras:\n";
        if (data.getExtras() != null) {
            msg += debugBundle(data.getExtras());
        } else msg += "null";
        return msg;
    }

    public static String debugBundle(Bundle bundle) {
        if (bundle == null) return "Bundle is null";
        StringBuilder msg = new StringBuilder("Bundle is:\n");
        for (String key : bundle.keySet()) {
            Object value = bundle.get(key);
            if (value instanceof Bundle) msg.append(debugBundle((Bundle) value));
            else if (value instanceof Collection)
                msg.append(printList((Collection) value)).append("\n");
            else if (value != null) {
                msg.append(String.format("\t%s\t%s\t(%s)\n", key, value.toString(), value.getClass().getName()));
            }
        }
        return msg.toString();
    }

    private static String printList(Collection list) {
        StringBuilder res = new StringBuilder("{");
        res.append(System.getProperty("line.separator"));
        int index = 0;
        for (Object o : list)
            res.append(String.format(Locale.getDefault(), "\titem %d:\t%s\t(%s)", index++, o.toString(), o.getClass().getName()))
                    .append(System.getProperty("line.separator"));

        res.append("}");
        return res.toString();
    }
}