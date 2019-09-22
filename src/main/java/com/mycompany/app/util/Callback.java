package com.mycompany.app.util;

@FunctionalInterface
public interface Callback<T> {

    public void call(T arg);

}