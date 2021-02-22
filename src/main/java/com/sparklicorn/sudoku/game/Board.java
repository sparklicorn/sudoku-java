package com.sparklicorn.sudoku.game;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * The standard 9x9 Sudoku board.
 * <br/><br/>
 * This SudokuBoard can also keep track of candidate values in each cell
 * through somewhat cumbersome bitmasking.
 * <br/>Values in the backing array are bitmasks in the interval [0x0, 0x1FF].
 * Each bit represents a candidate digit.
 * <br/>For example,
 * <ul>
 * <li><code>000000001</code> represents the candidate value of 1.</li>
 * <li><code>000000010</code> represents the candidate value of 2.</li>
 * <li><code>100000000</code> represents the candidate value of 9.</li>
 * Multiple candidate values are represented by a combination of set bits.
 * <li><code>000000011</code> represents the candidate values 1 and 2.</li>
 * <li><code>000100011</code> represents the candidate values 1, 2, 6.</li>
 * <li><code>101010101</code> represents the candidate values 1, 3, 5, 7, 9.</li>
 * And lastly,
 * <li><code>000000000</code> represents no possible candidate values.</li>
 * </ul>
 */
public class Board implements ISudokuBoard, Serializable {

	private static final long serialVersionUID = -6346231818157404798L;

	/* ****************************************************
	 * 			NOTES
	 *
	 * Board values are represented as powers of 2, allowing for multiple
	 * values per cell (i.e. candidate values) in the form of a bitstring.
	 *
	 * Examples:
	 * board[x] = 1
	 * 1 => (0 0000 0001)
	 * (0 0000 0001) has only the first bit from the left set.
	 * board[x] real value is 1.
	 *
	 * board[y] = 64
	 * 64 => (0 0100 0000)
	 * (0 0100 0000) has only the 7th bit from the left set.
	 * board[y] real value is 7.
	 *
	 * Example: board[z] = 482
	 * 482 => (1 1110 0010)
	 * (1 1110 0010) has bits 2, 6, 7, 8, 9 set.
	 * board[z] real value may be 2, 6, 7, 8, 9, to be resolved by user later.
	 ******************************************************/

	public static final int NUM_DIGITS = 9;

	public static final int NUM_ROWS = NUM_DIGITS;
    public static final int NUM_COLS = NUM_DIGITS;
    public static final int NUM_REGIONS = NUM_DIGITS;
	public static final int NUM_REGION_ROWS = 3;
	public static final int NUM_REGION_COLS = 3;
    public static final int NUM_CELLS = NUM_ROWS * NUM_COLS;

    public static final int MIN_VALUE = 0;
    public static final int MAX_VALUE = 9;

	/** Represents the combination of all candidate values.*/
	public static final int ALL = 0x1ff;

	public static final int[][] ROW_INDICES = new int[NUM_DIGITS][];
	public static final int[][] COL_INDICES = new int[NUM_DIGITS][];
	public static final int[][] REGION_INDICES = new int[NUM_DIGITS][];

	public static final int[] INDEX_ROW = new int[NUM_CELLS];
	public static final int[] INDEX_COL = new int[NUM_CELLS];
	public static final int[] INDEX_REGION = new int[NUM_CELLS];

	static {
		for (int i = 0; i < NUM_DIGITS; i++) {
			ROW_INDICES[i] = getRowIndices(i);
			COL_INDICES[i] = getColIndices(i);
			REGION_INDICES[i] = getRegionIndices(i);
			INDEX_ROW[i] = getRowForIndex(i);
			INDEX_COL[i] = getColForIndex(i);
			INDEX_REGION[i] = getRegionForIndex(i);
		}
	}

	public static int getRowForIndex(int i) {
		return i / 9;
	}

	public static int getColForIndex(int i) {
		return i % 9;
	}

	public static int getRegionForIndex(int i) {
		int r = i / 9;
		int c = i % 9;
		return (r / NUM_REGION_ROWS) * NUM_REGION_COLS + c/NUM_REGION_COLS;
	}

	public static int getIndexInRegion(int i) {
		int r = i / 9;
		int c = i % 9;
		return (r % NUM_REGION_ROWS) * NUM_REGION_COLS + (c % NUM_REGION_COLS);
	}

	/**
	 * Looks up the digit value associated with the given mask.
	 * If the mask does not represent a single value, then 0 is returned.
	 * @param mask - bitstring board value.
	 * @return A digit from 0 to 9. A return of zero means the value is empty.
	 */
	public static int decode(int mask) {
		return DECODER2[mask];
	}

	/**
	 * Returns the mask value associated with the given digit.
	 */
	public static int encode(int digit) {
		return 1 << (digit - 1);
	}

	private static final int[] DECODER2 = new int[512];
	static {
		for (int i = 1; i <= 9; i++) {
			DECODER2[encode(i)] = i;
		}
	}

	/**
	 * Determines whether the given bitstring board value represents a
	 * real Sudoku board value (and not a combination of multiple values).
	 * @param bits - bitstring board value.
	 * @return True if the given bitstring represents an actual Sudoku board
	 * value; otherwise false.
	 */
	public static boolean isSingleDigit(int bits) {
		return DECODER2[bits] > 0;
	}

	// Candidate values of the Sudoku board.
	protected int[] values;
	protected int numClues;

	// cache of undiscovered digits by row
	protected int[] usedDigitsByRow;
	protected int[] usedDigitsByRegion;
	protected int[] usedDigitsByCol;
	// cache of empty cells by row
	protected int[] filledCellsByRow;
	protected int[] filledCellsByRegion;
	protected int[] filledCellsByCol;

	/** Creates an empty Sudoku Board*/
	public Board() {
		clear();
	}

	/**
	 * Creates a Board from a string of values.
	 * Each block of 9 characters will be associated with a row on the board.
	 * The string length must exactly match the number of board spaces.
	 * Characters that are not 1 through 9 in the given string will be
	 * considered empty spaces.
	 * @param values - String of values that will be used to populate the board.
	 */
	public Board(String str) {
		if (str == null)
			throw new NullPointerException("Given Board string is null.");

		//Empty row shorthand.
		str = str.replaceAll("-", "000000000");

		if (str.length() > NUM_CELLS) {
			str = str.substring(0, NUM_CELLS);
		}

		while (str.length() < NUM_CELLS) {
			str += "0";
		}

		//Non-conforming characters to ZERO.
		str = str.replaceAll("[^1-9]", "0");

		int[] values = new int[NUM_CELLS];
		for (int i = 0; i < NUM_CELLS; i++) {
			values[i] = str.charAt(i) - '0';
		}

		setupBoardFromValues(values);
	}

	/**
	 * Creates a Board with the values provided by the given array.
	 * Each block of 9 values will be associated with a row on the board.
	 * The length of the array must exactly match the number of board spaces.
	 * Values that are out of the range [0, 9] will be consider empty spaces.
	 * @param values - Array of Sudoku values that will be used to populate
	 * the board.
	 */
	public Board(int[] values) {
		setupBoardFromValues(values);
	}

	private void setupBoardFromValues(int[] values) {
		if (values.length != NUM_CELLS) {
			throw new IllegalArgumentException(
				String.format("Number of values must be %d, is %d", NUM_CELLS, values.length)
			);
		}

		usedDigitsByRow = new int[9];
		filledCellsByRow = new int[9];
		usedDigitsByRegion = new int[9];
		filledCellsByRegion = new int[9];
		usedDigitsByCol = new int[9];
		filledCellsByCol = new int[9];
		this.values = new int[NUM_CELLS];
		for (int i = 0; i < NUM_CELLS; i++) {
			int digit = values[i];
			if (digit > 0) {
				setDigitAt(i, digit);
			} else {
				this.values[i] = ALL;
			}
		}
	}

	/**
	 * Creates a Board that is the copy of the one given.
	 * @param other - Board object to copy values from.
	 */
	public Board(Board other) {
		values = new int[NUM_CELLS];
		usedDigitsByRow = new int[9];
		filledCellsByRow = new int[9];
		usedDigitsByRegion = new int[9];
		filledCellsByRegion = new int[9];
		usedDigitsByCol = new int[9];
		filledCellsByCol = new int[9];
		System.arraycopy(other.values, 0, values, 0, NUM_CELLS);
		System.arraycopy(other.usedDigitsByRow, 0, usedDigitsByRow, 0, 9);
		System.arraycopy(other.filledCellsByRow, 0, filledCellsByRow, 0, 9);
		System.arraycopy(other.usedDigitsByRegion, 0, usedDigitsByRegion, 0, 9);
		System.arraycopy(other.filledCellsByRegion, 0, filledCellsByRegion, 0, 9);
		System.arraycopy(other.usedDigitsByCol, 0, usedDigitsByCol, 0, 9);
		System.arraycopy(other.filledCellsByCol, 0, filledCellsByCol, 0, 9);
		numClues = other.numClues;
	}

	/** Clears all values on the board.*/
	public void clear() {
		values = new int[NUM_CELLS];
		Arrays.fill(values, ALL);
		usedDigitsByRow = new int[9];
		usedDigitsByRegion = new int[9];
		usedDigitsByCol = new int[9];
		filledCellsByRow = new int[9];
		filledCellsByRegion = new int[9];
		filledCellsByCol = new int[9];
		numClues = 0;
	}

	public int getUsedDigitsInRow(int row) {
		return usedDigitsByRow[row];
	}

	public int getFilledCellsInRow(int row) {
		return filledCellsByRow[row];
	}

	public int getNumClues() {
		return numClues;
	}

	/** Returns the number of empty spaces on the board.*/
	public int getNumEmptySpaces() {
		return NUM_CELLS - countClues();
	}

	public int countClues() {
		int result = 0;
		for (int v : this) {
			if (decode(v) > 0) {
				result++;
			}
		}
		return result;
	}

	private void addUsedDigit(int index, int digit) {
		usedDigitsByRow[getRowForIndex(index)] |= encode(digit);
		usedDigitsByCol[getColForIndex(index)] |= encode(digit);
		usedDigitsByRegion[getRegionForIndex(index)] |= encode(digit);
	}

	private void removeUsedDigit(int index, int digit) {
		usedDigitsByRow[getRowForIndex(index)] &= ~encode(digit);
		usedDigitsByCol[getColForIndex(index)] &= ~encode(digit);
		usedDigitsByRegion[getRegionForIndex(index)] &= ~encode(digit);
	}

	private void addFilledCell(int index) {
		filledCellsByRow[getRowForIndex(index)] |= 1 << getColForIndex(index);
		filledCellsByCol[getColForIndex(index)] |= 1 << getRowForIndex(index);
		filledCellsByRegion[getRegionForIndex(index)] |= 1 << getIndexInRegion(index);
	}

	private void removeFilledCell(int index) {
		filledCellsByRow[getRowForIndex(index)] &= ~(1 << getColForIndex(index));
		filledCellsByCol[getColForIndex(index)] &= ~(1 << getRowForIndex(index));
		filledCellsByRegion[getRegionForIndex(index)] &= ~(1 << getIndexInRegion(index));
	}

	@Override
	public int[] getDigits(int[] digits) {
		for (int i = 0; i < NUM_CELLS; i++) {
			digits[i] = decode(values[i]);
		}
		return digits;
	}

	/**
	 * Retrieves the board as an array of masks.
	 * @param masks - The array to populate with the board values.
	 * @return The board populated array, or if the array was too small,
	 * a newly allocated array with the populated values.
	 */
	public int[] getMasks(int[] masks) {
		if (masks.length < NUM_CELLS) {
			masks = new int[NUM_CELLS];
		}
		System.arraycopy(this.values, 0, masks, 0, NUM_CELLS);
		return masks;
	}

	@Override
	public int getDigitAt(int index) {
		return decode(values[index]);
	}

	@Override
	public void setDigitAt(int index, int digit) {
		if (digit < 0 || digit > 9) {
			throw new IllegalArgumentException("Value is out of bounds.");
		}

		int prevValue = decode(values[index]);
		if (digit > 0) {
			values[index] = encode(digit);
			addUsedDigit(index, digit);
			addFilledCell(index);
			if (prevValue == 0) {
				numClues++;
			} else {
				removeUsedDigit(index, digit);
			}
		} else {
			values[index] = ALL;
			if (prevValue > 0) {
				numClues--;
				removeUsedDigit(index, digit);
				removeFilledCell(index);
			}
		}
	}

	/**
	 * Returns the mask value on the board at the given position.
	 * This bitmask represents the candidates values for that position.
	 * <br/>See {@link Board} for information about how the bitmask is used.
	 * @param index - The position on the board [0, 80], where 0 represents
	 * the top-left position, and 80 the bottom-right.
	 */
	public int getMaskAt(int index) {
		return values[index];
	}

	/**
	 * Sets the mask value on the board at the given position.
	 * This bitmask represents the candidates values for that position.
	 * <br/>See {@link Board} for information about how the bitmask is used.
	 * @param index - The position on the board [0, 80], where 0 represents
	 * the top-left position, and 80 the bottom-right.
	 * @param mask - The bitmask to set at this position.
	 */
	public void setMaskAt(int index, int mask) {
		if (mask < 0 || mask > ALL) {
			throw new IllegalArgumentException("Mask value is out of bounds.");
		}

		int newDigit = decode(mask);
		setDigitAt(index, newDigit); // updates numClues and other cached data if necessary
		values[index] = mask;
	}

	/**
	 * <em>Two boards are equal if they contain the same configuration of values.</em>
	 * <br/><br/>
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj instanceof Board) {
			return Arrays.equals(values, ((Board) obj).values);
		}
		return false;
	}

	/** Returns a string representing the Sudoku board in a condensed form.*/
	public String getSimplifiedString() {
		StringBuilder strb = new StringBuilder();
		for (int i : values) {
			int v = decode(i);
			if (v > 0) {
				strb.append(v);
			} else {
				strb.append('.');
			}
		}
		return strb.toString();
	}

	@Override
	public String toString() {
		StringBuilder strb = new StringBuilder("  ");

	    for (int i = 0; i < NUM_CELLS; i++) {
	    	if (isSingleDigit(values[i])) {
	    		strb.append(decode(values[i]));
	    	} else {
	    		strb.append('.');
	    	}

	        if (((((i+1) % 9) % 3) == 0) && (((i+1) % 9) != 0)) {
	            strb.append(" | ");
	        } else {
	        	strb.append("   ");
	        }

	        if (((i+1) % 9) == 0) {
	        	strb.append(System.lineSeparator());

	        	if (i == NUM_CELLS - 1) {
	        		break;
	        	}

	            if (((Math.floor((i+1) / 9) % 3) == 0) && ((Math.floor(i/9) % 8) != 0)) {
	            	strb.append(" -----------|-----------|------------");
	            	strb.append(System.lineSeparator());
	            } else {
	                strb.append("            |           |            ");
	            	strb.append(System.lineSeparator());
	            }
	            strb.append("  ");
	        }
	    }

		return strb.toString();
	}

	@Override
	public int hashCode() {
		return Arrays.hashCode(values);
	}

	// Converts candidates bitstring held in this.values[index] to a list of candidate digits.
	@Override
	public List<Integer> getCandidates(int index, List<Integer> list) {
		int mask = values[index];
		int digit = decode(mask);
		if (digit > 0) {
			list.add(digit);
		} else {
			for (int shift = 0; shift < 9; shift++) {
				if ((mask & (1 << shift)) > 0) {
					list.add(shift + 1);
				}
			}
		}
		return list;
	}

	@Override
	public Iterator<Integer> iterator() {
		return Arrays.stream(values).iterator();
	}

	/**
	 * Returns a deep copy of this board.
	 */
	public Board copy() {
		return new Board(this);
	}

	/**
	 * Determines whether the given Sudoku board is valid.
	 * <br/>A board is valid if every row, column, and 3x3 region is valid,
	 * meaning none contain duplicate digits.
	 * <br/>Validity of the board only means that the configuration is
	 * acceptable.  It does not mean the configuration is correct.
	 * <br/>The board does not need to be complete to be valid. A blank board
	 * is valid by the above definition.
	 * @return True if the board is valid; otherwise false.
	 */
	public boolean isValid() {

		//Check for positions with no candidates.
		/*for (int i = 0; i < NUM_CELLS; i++) {
			int v = board[i];
			if (v == 0)
				return false;
		}*/

		for (int x = 0; x < 9; x++) {
			if (!isRowValid(x)) {
				return false;
			}

			if (!isColValid(x)) {
				return false;
			}

			if (!isRegionValid(x)) {
				return false;
			}
		}
		return true;
	}

	public static int[] getRowIndices(int row) {
		int[] result = new int[NUM_DIGITS];
		for (int i = 0; i < NUM_DIGITS; i++) {
			result[i] = row * NUM_DIGITS + i;
		}
		return result;
	}

	public static int[] getColIndices(int col) {
		int[] result = new int[NUM_DIGITS];
		for (int i = 0; i < NUM_DIGITS; i++) {
			result[i] = col + i * NUM_DIGITS;
		}
		return result;
	}

	public static int[] getRegionIndices(int region) {
		int[] result = new int[NUM_DIGITS];
		int gr = region / NUM_REGION_ROWS;
		int gc = region % NUM_REGION_COLS;
		for (int i = 0; i < NUM_DIGITS; i++) {
			result[i] = gr*NUM_DIGITS*NUM_REGION_ROWS + gc*NUM_REGION_COLS +
				(i/NUM_REGION_ROWS)*NUM_DIGITS + (i%NUM_REGION_COLS);
		}
		return result;
	}

	/**
	 * Determines whether the given row on the given board is valid.
	 * <br/>A row is considered valid if it contains no duplicate digits
	 * in any of the cells.
	 * <br/>The row does not need to be complete to be valid.
	 * @param row - the row of the board to evaluate.
	 * @return True if the row is valid; otherwise false.
	 */
	public boolean isRowValid(int row) {
		return Integer.bitCount(usedDigitsByRow[row]) == Integer.bitCount(filledCellsByRow[row]);
	}

	public boolean isRowFull(int row) {
		return filledCellsByRow[row] == ALL;
	}

	/**
	 * Determines whether the given column on the given board is valid.
	 * <br/>A column is considered valid if it contains no duplicate digits
	 * in any of the cells.
	 * <br/>The column does not need to be complete to be valid.
	 * @param col - the column of the board to evaluate.
	 * @return True if the column is valid; otherwise false.
	 */
	public boolean isColValid(int col) {
		return Integer.bitCount(usedDigitsByCol[col]) == Integer.bitCount(filledCellsByCol[col]);
	}

	public boolean isColFull(int col) {
		return filledCellsByCol[col] == ALL;
	}

	/**
	 * Determines whether the given region on the given board is valid.
	 * <br/>A region is considered valid if it contains no duplicate digits
	 * in any of the cells.
	 * <br/>The region does not need to be complete to be valid.
	 * @param region - the region of the board to evaluate.
	 * @return True if the region is valid; otherwise false.
	 */
	public boolean isRegionValid(int region) {
		return Integer.bitCount(usedDigitsByRegion[region]) == Integer.bitCount(filledCellsByRegion[region]);
	}

	public boolean isRegionFull(int region) {
		return filledCellsByRegion[region] == ALL;
	}

	/**
	 * Determines whether the given Sudoku board is full.
	 * <br/>A board is full if every cell contains a digit from 1 to 9.
	 * <br/>The configuration of the digits does not need to be valid
	 * for the board to be considered full.
	 * @return True if the board is full; otherwise false.
	 */
	public boolean isFull() {
		return numClues == NUM_CELLS;
	}

	/**
	 * Determines whether the given Sudoku board is solved.
	 * <br/>A board is solved if it is completely full of numbers and is
	 * valid.
	 * @return True if the board is solved; otherwise false.
	 */
	public boolean isComplete() {
		return isFull() && isValid();
	}

	public void kill() {
		this.values = null;
	}
}
