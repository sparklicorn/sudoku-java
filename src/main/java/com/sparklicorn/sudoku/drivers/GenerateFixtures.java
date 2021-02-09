package com.sparklicorn.sudoku.drivers;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

import com.google.gson.Gson;
import com.sparklicorn.sudoku.game.*;
import com.sparklicorn.sudoku.game.generators.Generator;
import com.sparklicorn.sudoku.game.solvers.Solver;

public class GenerateFixtures {
    private static final ThreadLocalRandom rand = ThreadLocalRandom.current();

    private static class BoardFixture {
        static final String INVALID_SOLUTION = "NO SOLUTION";

        String boardString;
        String solutionString;

        int numClues;
        boolean isFull;
        boolean isValid;
        boolean isComplete;

        List<Integer> validRows;
        List<Integer> validCols;
        List<Integer> validRegions;
        List<Integer> fullRows;
        List<Integer> fullCols;
        List<Integer> fullRegions;

        BoardFixture(Board board) {
            this.boardString = board.getSimplifiedString();
            this.numClues = board.getNumClues();
            this.isFull = board.isFull();
            this.isValid = board.isValid();
            this.isComplete = board.isSolved();

            Set<Board> set = Solver.getAllSolutions(board);
            if (set.size() == 1) {
                this.solutionString = set.toArray(new Board[1])[0].getSimplifiedString();
            } else {
                this.solutionString = INVALID_SOLUTION;
            }

            validRows = new ArrayList<>();
            validCols = new ArrayList<>();
            validRegions = new ArrayList<>();
            fullRows = new ArrayList<>();
            fullCols = new ArrayList<>();
            fullRegions = new ArrayList<>();

            for (int i = 0; i < 9; i++) {
                if (board.isRowValid(i)) {
                    validRows.add(i);
                }
                if (board.isColValid(i)) {
                    validCols.add(i);
                }
                if (board.isRegionValid(i)) {
                    validRegions.add(i);
                }
                if (board.isRowFull(i)) {
                    fullRows.add(i);
                }
                if (board.isColFull(i)) {
                    fullCols.add(i);
                }
                if (board.isRegionFull(i)) {
                    fullRegions.add(i);
                }
            }
        }
    }

    public static void main(String[] args) throws FileNotFoundException {
        List<Board> boards = new ArrayList<>();

        // include some real configs
        System.out.println("Generating configs...");
        for (int n = 0; n < 100; n++) {
            Board board = Generator.generateConfig();
            System.out.println(board.getSimplifiedString());
            boards.add(board);
        }

        // include some real puzzles
        System.out.println("Generating puzzles...");
        for (int clues = 80; clues > 23; clues--) {
            for (int n = 0; n < 10; n++) {
                Board board = Generator.generatePuzzle(clues);
                System.out.println(board.getSimplifiedString());
                boards.add(board);
            }
        }

        // include some shuffled configs (full board but probably invalid)
        System.out.println("Generating shuffled configs...");
        for (int n = 0; n < 100; n++) {
            Board board = Generator.generateConfig();
            shuffle(board);
            System.out.println(board.getSimplifiedString());
            boards.add(board);
        }

        // include some shuffled puzzles (shuffled config, remove some items)
        System.out.println("Generating shuffled puzzles...");
        for (int n = 0; n < 1000; n++) {
            Board board = Generator.generateConfig();
            shuffle(board);
            removeRandomValues(board, rand.nextInt(9, 45));
            System.out.println(board.getSimplifiedString());
            boards.add(board);
        }

        shuffleItems(boards);

        PrintWriter pw = new PrintWriter("sudoku-board-fixtures.txt");
        Gson gson = new Gson();
        for (Board b : boards) {
            pw.println(gson.toJson(new BoardFixture(b)));
        }
        pw.close();
    }

    private static <T> List<T> shuffleItems(List<T> list) {
        int numSwaps = list.size() * 100;
        for (int n = 0; n < numSwaps; n++) {
            swap(list, rand.nextInt(list.size()), rand.nextInt(list.size()));
        }
        return list;
    }

    private static <T> void swap(List<T> list, int i, int j) {
        T temp = list.get(i);
        list.set(i, list.get(j));
        list.set(j, temp);
    }

    private static Board createRandomBoard(int numClues) {
        Board board = new Board();
        for (int i = 0; i < Board.NUM_CELLS; i++) {
            board.setValueAt(i, rand.nextInt(Board.NUM_CELLS));
        }
        return board;
    }

    private static void removeRandomValues(Board board, int numValuesToRemove) {
        int cluesRemaining = Math.max(board.getNumClues() - numValuesToRemove, 0);
        while (board.getNumClues() > cluesRemaining) {
            board.setValueAt(rand.nextInt(81), 0);
        }
    }

    private static void shuffle(Board board) {
        swapRandomValues(board, 1000);
    }

    private static void swapRandomValues(Board board, int numSwaps) {
        if (numSwaps < 1) {
            return;
        }

        for (int swap = 0; swap < numSwaps; swap++) {
            swap(board, rand.nextInt(81), rand.nextInt(81));
        }
    }

    private static void swap(Board board, int i, int j) {
        int temp = board.getValueAt(i);
        board.setValueAt(i, board.getValueAt(j));
        board.setValueAt(j, temp);
    }
}
