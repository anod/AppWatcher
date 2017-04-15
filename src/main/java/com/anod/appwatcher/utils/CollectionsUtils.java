package com.anod.appwatcher.utils;


import java.util.ArrayList;
import java.util.List;

/**
 * @author algavris
 * @date 08/10/2016.
 */

public class CollectionsUtils {

    public interface Predicate<T> {
        boolean test(T t);
    }

    public static <T> List<T> filter(List<T> source, Predicate<? super T> predicate) {
        List<T> result = new ArrayList<T>(source.size());

        for (T el : source) {
            if (!predicate.test(el)) {
                result.add(el);
            }
        }

        return result;
    }
}
