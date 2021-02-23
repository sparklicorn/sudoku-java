package com.sparklicorn.sudoku.game;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import com.google.gson.Gson;

public class BoardFixture {
    static final String INVALID_SOLUTION = "NO SOLUTION";

    public String boardString;
    public String solutionString;

    public int numClues;
    public boolean isFull;
    public boolean isValid;
    public boolean isComplete;

    public List<Integer> validRows;
    public List<Integer> validCols;
    public List<Integer> validRegions;
    public List<Integer> fullRows;
    public List<Integer> fullCols;
    public List<Integer> fullRegions;

    public transient Board board;

    public static List<BoardFixture> loadFromFile(String filename) throws IOException {
        List<BoardFixture> fixtures = new ArrayList<>();

        Scanner scanner = new Scanner(TestBoard.class.getResourceAsStream(filename));
        Gson gson = new Gson();
        while (scanner.hasNextLine()) {
            BoardFixture fixture = gson.fromJson(scanner.nextLine(), BoardFixture.class);
            fixture.getBoard();
            fixtures.add(fixture);
        }
		scanner.close();

        return fixtures;
    }

    Board getBoard() {
        if (this.board == null) {
            this.board = new Board(this.boardString);
        }
        return this.board;
    }

    String getRowAsString(int row) {
        StringBuilder strb = new StringBuilder();
		for (int i : Board.getRowIndices(row)) {
			strb.append(board.getDigitAt(i));
		}
        return strb.toString();
    }

    String getColumnAsString(int col) {
        StringBuilder strb = new StringBuilder();
        for (int i : Board.getColIndices(col)) {
            strb.append(board.getDigitAt(i));
        }
        return strb.toString();
    }

    String getRegionAsString(int region) {
		StringBuilder strb = new StringBuilder();
        for (int i : Board.getRegionIndices(region)) {
            strb.append(board.getDigitAt(i));
        }
        return strb.toString();
    }
}
