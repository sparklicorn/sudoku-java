package com.mycompany.app.sudoku;

public class GeneratePuzzles {
    
    public static void main(String[] args) {

        int numClues = 27;
        int numPuzzles = 1;

        if (args != null) {
            if (args.length >= 1) {
                numClues = Integer.parseInt(args[0]);
            }
            if (args.length >= 2) {
                numPuzzles = Integer.parseInt(args[1]);
            }
        }
        
        for (int i = 0; i < numPuzzles; i++) {
            for (Board b : Generator.generatePuzzles(numClues)) {
                System.out.println(b.getSimplifiedString());
            }
            System.out.println();
        }
        
    }

}