package com.sparklicorn.sudoku.game.generators;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class GeneratePuzzles {

    public static void main(String[] args) {

        int numClues = 27;
        int numPuzzles = 10;

        if (args != null) {
            if (args.length >= 1) {
                numPuzzles = Integer.parseInt(args[0]);
            }
            if (args.length >= 2) {
                numClues = Integer.parseInt(args[1]);
            }
        }

        final int clues = numClues;

        int numThreads = Runtime.getRuntime().availableProcessors();

        BlockingQueue<Runnable> workQueue = new ArrayBlockingQueue<>(numPuzzles);
        ThreadPoolExecutor pool = new ThreadPoolExecutor(numThreads, numThreads, 1L, TimeUnit.SECONDS, workQueue);
        pool.prestartAllCoreThreads();

        for (int n = 0; n < numPuzzles; n++) {
            pool.submit(() -> {
                System.out.println(Generator.generatePuzzle(clues).getSimplifiedString());
            });
        }

        pool.shutdown();
    }
}
