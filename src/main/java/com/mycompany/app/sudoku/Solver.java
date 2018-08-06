package com.mycompany.app.sudoku;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.mycompany.app.sudoku.Board.*;

/**
 * 
 * 
 * @author Jeff
 */
public class Solver {
	
	private static interface SolutionFoundPolicy {
		public boolean execute(Board b);
	}

	/**
	 * Attempts to solve the given Sudoku board, returning the first
	 * solution found, or null if no solution was found.
	 * <br/>
	 * This may take a long time, and possibly never return, depending on
	 * how much of the board is already solved.  Generally, boards with
	 * around 20 clues should have little problem solving on modern processors.
	 * @param board - the Sudoku board to work on.
	 * @return A new ISudokuBoard representing the puzzle solution.
	 */
	public static Board solve(Board board) {
		Board[] result = new Board[1];
		searchForSolution(board, (b) -> {
			result[0] = b;
			return false;
		});
		return result[0];
	}
	
	/**
	 * Attempts to solve the given Sudoku board.
	 * <br/>This may take a long time, and possibly never return, depending on
	 * how much of the board is already solved.  Generally, boards with
	 * around 20 clues should have little problem solving on modern processors.
	 * @param board - the Sudoku board to work on.
	 * @return A set containing all the solutions for the given Sudoku board.
	 */
	public static Set<Board> getAllSolutions(Board board) {	
		HashSet<Board> result = new HashSet<>();
		searchForSolution(board, (b) -> {
			//System.out.println("found solution: " + b.getSimplifiedString());
			result.add(b);
			return true;
		});
		return result;
	}

	/**
	 * Determines if the given board solves uniquely to the provided solution.
	 * <br/>This may take a long time, and possibly never return, depending on
	 * how much of the board is already solved.  Generally, boards with
	 * around 20 clues should have little problem solving on modern processors.
	 * @param board - the Sudoku board to solve.
	 * @param solution - the solution that the board should solve to.
	 * @return True if the board has one unique solution equivalent to the
	 * one provided; otherwise false.
	 */
	public static boolean solvesUniquely(Board board, Board solution) {
		//p.execute(b) => false if b is alternate solution.
		//When p.execute == false, search stops and returns false.
		//When p.execute == true, search continues.
		//Search returns true when the search is exhausted.
		AtomicBoolean result = new AtomicBoolean(false);
		searchForSolution(board, (b) -> {
			//System.out.println(b.getSimplifiedString());
			result.set(b.equals(solution));
			return result.get();
		});
		return result.get();
	}
	
	private static boolean searchForSolution(Board board, SolutionFoundPolicy p) {
		//This will be reused and repopulated by board.getCandidates(list)
		//	to reduce overhead.
		List<Integer> candidates = new ArrayList<>(9);
		Queue<Board> q = new ArrayDeque<>();
		q.offer(new Board(board));

		while (!q.isEmpty()) {
			Board b = q.poll();

			/*if (!b.isValid()) {
				System.out.print('*');
				continue;
			}*/

			if (!b.isFull()) {
				reduce(b);
			}

			if (b.isFull()) {
				if (!p.execute(b)) {
					return false;
				}
			} else {
				int index = pickEmptyCell(b);
				if (index > 0) {
					for (Board c : getCandidateBoards(b, index, candidates)) {
						q.offer(c);
					}
					//putCellCandidatesInQueue(b, index, q, candidates);
				}
			}
		}

		return true;
	}

	/**
	 * Searches for the index of the empty cell in board which has the fewest
	 * number of candidates. Starts at the top of the board.
	 */
	private static int pickEmptyCell(Board b) {
		int index = -1;
		int digits = 10; //number of digit options for cell at index
		for (int i = 0; i < NUM_CELLS; i++) {
			int opts = Integer.bitCount(b.getMaskAt(i));
			if (opts > 1 && opts < digits) {
				index = i;
				digits = opts;
				if (digits == 2) //stop early. Won't find a cell with fewer candidates.
					break;
			}
		}
		return index;
	}

	/**
	 * Picks the cell from board with the fewest number of candidates,
	 * then fills in the candidates for that cell in copies of board.  Places
	 * the copies into the queue.
	 * <br/>Since this method is called frequently while searching for Sudoku
	 * solutions, and Board.getCandidates() takes and returns a List, the caller may
	 * pass in a list object to be used instead of this method allocating a new one
	 * (and creating more garbage).
	 * @param board - the Sudoku board trying to be solved.
	 * @param queue - the queue of boards used to search for the solution(s).
	 * @param candidates - a list of Integers used internally.
	 * The list will be cleared and repopulated with the candidates for the
	 * cell described above.
	 */
	private static void putCellCandidatesInQueue(Board board, int cellIndex,
	Queue<Board> queue, List<Integer> candidates)
	{
		if (candidates == null)
			candidates = new ArrayList<Integer>(9);

		candidates.clear();
		board.getCandidates(cellIndex, candidates);
		for (int option : candidates) {
			Board bCopy = new Board(board);
			bCopy.setValueAt(cellIndex, option);
			queue.offer(bCopy);
		}
	}

	private static List<Board> getCandidateBoards(Board board, int cellIndex,
	List<Integer> candidates)
	{
		if (candidates == null)
			candidates = new ArrayList<Integer>(9);

		candidates.clear();
		board.getCandidates(cellIndex, candidates);
		List<Board> result = new ArrayList<>();
		for (int option : candidates) {
			Board bCopy = new Board(board);
			bCopy.setValueAt(cellIndex, option);
			result.add(bCopy);
		}
		return result;
	}

	/**
	 * Attempts to fill in the most obvious cells on the board.
	 * <br/>It is possible that this method solves the puzzle.
	 * @param board - the Sudoku board to work on.
	 * @return True if the board was changed as a result of this call;
	 * otherwise false.
	 */
	protected static boolean reduce(Board board) {
		//reduce until we can't reduce no more
	    boolean overallChange = false;
	    boolean changed = false;
	    int[] masks = board.getMasks(new int[NUM_CELLS]);
	    
	    //Track positions that are not already reduced.
	    ArrayList<Integer> indices = new ArrayList<>();
	    for (int i = 0; i < NUM_CELLS; i++) {
	    	if (board.getValueAt(i) == 0) {
	    		indices.add(i);
	    		masks[i] = ALL;
	    	}
	    }
		
		do {
			changed = false;
	        for (int i = indices.size() - 1; i >= 0; i--) {
	        	int j = indices.get(i);
	        	if (reduce(masks, j)) {
	                changed = true;
	                overallChange = true;
	                board.setMaskAt(j, masks[j]);
	            }
	        	if (decode(masks[j]) > 0) {
		    		indices.remove(i);
		    	}
	        }
		} while (changed);
	    
	    return overallChange;
	}
	
	private static boolean reduce(int[] masks, int index) {
	    int initial = masks[index];
	    
	    //check if this cell already contains a single digit
	    if (decode(initial) > 0)
	        return false;
	    
	    int candidates = ALL;
	    candidates = reduceRow(masks, index, candidates);
	    candidates = reduceCol(masks, index, candidates);
	    candidates = reduceRegion(masks, index, candidates);
	    masks[index] = candidates;
	    return (initial - candidates) > 0;
	}

	private static int reduceRegion(int[] masks, int index, int candidates) {
	    int gr = (index / 9) / 3; //grid row [0,2]
	    int gc = (index % 9) / 3; //grid column [0,2]
	    for (int i = 0; i < 9; i++) { //i = index of cells in region
	    	int bi = gr*27 + gc*3 + (i / 3)*9 + (i%3); //index of region cell i in masks
	        if (bi == index)
				continue;
				
	    	int v = masks[bi];
	        if (decode(v) > 0) {
	            if ((candidates ^ v) < candidates) {
	                candidates ^= v;
	            }
	        }
	    }
		return candidates;
	}

	private static int reduceCol(int[] masks, int index, int candidates) {
		int c = index % 9;
	    for (int i = c; i < 81; i += 9) {
	        if (i == index)
				continue;
				
	        int v = masks[i];
	        if (decode(v) > 0) {
	            if ((candidates ^ v) < candidates) {
	                candidates ^= v;
	            }
	        }
	    }
		return candidates;
	}

	private static int reduceRow(int[] masks, int index, int candidates) {
		int r = index / 9;
		for (int i = r * 9; i < (r + 1) * 9; i++) {
	        if (i == index)
	            continue;
	        
	        int v = masks[i];
	        if (decode(v) > 0) {
	        	if ((candidates ^ v) < candidates) {
	                candidates ^= v;
	            }
	        }
	    }
		return candidates;
	}
	
}
