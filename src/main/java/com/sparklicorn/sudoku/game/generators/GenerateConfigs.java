package com.sparklicorn.sudoku.game.generators;

import java.util.HashSet;

import com.sparklicorn.sudoku.game.*;

public class GenerateConfigs {

    public static void main(String[] args) {
        int numConfigs = 1; //Number of configs to generate.
        boolean normalize = false;

        if (args != null && args.length > 0) {
            try {
                numConfigs = Math.max(Integer.parseInt(args[0]), 1);
            } catch (NumberFormatException ex) {
                System.err.println("Number of configurations to generate could not be parsed.");
                return;
            }
        }

        if (args != null && args.length > 1) {
            normalize = Boolean.parseBoolean(args[1]);
        }

        HashSet<Board> set = new HashSet<>();

        while (set.size() < numConfigs) {
            Board b = Generator.generateConfig();

            if (normalize) {
                b = SudokuUtility.normalize(b);
            }

            if (set.add(b)) {
                System.out.println(b.getSimplifiedString());
            }
        }
    }
}
