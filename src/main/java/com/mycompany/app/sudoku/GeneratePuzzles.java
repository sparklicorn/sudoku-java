package com.mycompany.app.sudoku;

import com.mycompany.app.util.ThreadPool;

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
        Runnable work = () -> {
            System.out.println(Generator.generatePuzzle(clues).getSimplifiedString());
        };

        ThreadPool.repeatWork(work, numPuzzles, numThreads, null);
    }
}
