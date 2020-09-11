package com.sparklicorn.sudoku.util;

@FunctionalInterface
public interface Callback<T> {

    public void call(T arg);

}
