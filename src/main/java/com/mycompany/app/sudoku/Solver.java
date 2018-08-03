package com.mycompany.app.sudoku;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import static com.mycompany.app.sudoku.Board.*;

/**
 * TODO WIP
 * 
 * @author Jeff
 */
public class Solver {
	
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
		//This will be reused and repopulated by board.getCandidates(list)
		//	to reduce overhead.
		//Holds candidate values for a specific index in a board.
		List<Integer> candidates = new ArrayList<>(9);
		
		Queue<Board> q = new ArrayDeque<>();
		q.offer(new Board(board));

		while (!q.isEmpty()) {
			Board b = q.poll();
			reduce(b);
			
			boolean isValid = b.isValid();
			boolean isFull = b.isFull();
			
			if (isValid && isFull) {
				return b;
				
			} else if (isValid) {
				//find empty cell with least amount of options
				int index = -1; //index of cell
				int digits = 9; //number of digits options for cell at index
				for (int i = 0; i < NUM_CELLS; i++) {
					int opts = Integer.bitCount(b.getMaskAt(i));
					if (opts > 1 && opts < digits) {
						index = i;
						digits = opts;
						if (digits == 2) {
							break;
						}
					}
				}
				
				if (index > -1) {
					candidates.clear();
					b.getCandidates(index, candidates);
					System.out.println(candidates);
					for (Integer option : candidates) {
						Board bCopy = new Board(b);
						bCopy.setValueAt(index, option);
						q.offer(bCopy);
					}
				}
			}
		}
		
		return null;
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
		//This will be reused and repopulated by board.getCandidates(list)
		//	to reduce overhead.
		List<Integer> candidates = new ArrayList<>(9);
				
		HashSet<Board> result = new HashSet<>();
		Queue<Board> q = new ArrayDeque<>();
		q.offer(new Board(board));
		//int[] values = new int[NUM_CELLS];

		while (!q.isEmpty()) {
			Board b = q.poll();
			reduce(b);
			//b.getValues(values);
			if (b.isSolved()) {
				result.add(b);
				
			} else if (b.isValid()) {
				//find empty cell with least amount of options
				int index = -1; //index of cell
				int digits = 9; //number of digits options for cell at index
				for (int i = 0; i < NUM_CELLS; i++) {
					int opts = Integer.bitCount(b.board[i]);
					if (opts > 1 && opts < digits) {
						index = i;
						digits = opts;
					}
				}
				
				if (index > -1) {
					candidates.clear();
					b.getCandidates(index, candidates);
					for (Integer option : candidates) {
						Board bCopy = new Board(b);
						bCopy.setValueAt(index, option);
						q.offer(bCopy);
					}
				}
			}
		}
		
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
		//This will be reused and repopulated by board.getCandidates(list)
		//	to reduce overhead.
		List<Integer> candidates = new ArrayList<>(9);
		boolean solved = false;
		Queue<Board> q = new ArrayDeque<>();
		q.offer(new Board(board));
		//int[] values = new int[NUM_CELLS];

		while (!q.isEmpty()) {
			Board b = q.poll();
			reduce(b); //O(1)
			//b.getValues(values);

			if (b.isSolved()) {
				if (!b.equals(solution)) {
					return false; //stop early if this is an alternate solution
				}
				solved = true;

			} else if (b.isValid()) {
				//find empty cell with least amount of options
				int index = -1; //index of cell
				int digits = 9; //number of digits options for cell at index
				for (int i = 0; i < NUM_CELLS; i++) {
					int opts = Integer.bitCount(b.getMaskAt(i));
					if (opts > 1 && opts < digits) {
						index = i;
						digits = opts;
					}
				}

				if (index > -1) {
					candidates.clear();
					b.getCandidates(index, candidates);
					for (Integer option : candidates) {
						Board bCopy = new Board(b);
						bCopy.setValueAt(index, option);
						q.offer(bCopy);
					}
				}
			}
		}

		return solved;
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
	    boolean changed = true;
	    int[] masks = board.getMasks(new int[NUM_CELLS]);
	    
	    //Track positions that are not already reduced.
	    ArrayList<Integer> indices = new ArrayList<>();
	    for (int i = 0; i < NUM_CELLS; i++) {
	    	if (board.getValueAt(i) == 0) {
	    		indices.add(i);
	    		masks[i] = ALL;
	    	}
	    }
	    
	    while (changed) {
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
	    }
	    
	    return overallChange;
	}
	
	private static boolean reduce(int[] masks, int index) {
		
	    int initial = masks[index];
	    
	    //check if this cell is already reduced to one number
	    //(if it is a power of 2)
	    if (decode(initial) > 0) {
	        return false;
	    }
	    
	    int candidates = ALL;
	    
	    //look in row
	    candidates = reduceRow(masks, index, candidates);
	    
	    //look in column
	    candidates = reduceCol(masks, index, candidates);
	    
	    //look region-wise
	    candidates = reduceRegion(masks, index, candidates);
	    
	    masks[index] = candidates;
	    return (initial - candidates) > 0;
	}

	private static int reduceRegion(int[] masks, int index, int candidates) {
		//determine which grid section we are in
	    int gr = (index / 9) / 3;
	    int gc = (index % 9) / 3;
	    for (int i = 0; i < 9; i++) { //i = offset from
	    	int bi = gr*27 + gc*3 + (i / 3)*9 + (i%3);
	        if (bi == index) {
	            continue;
	        }
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
	        if (i == index) {
	            continue;
	        }
	        int v = masks[i];
	        if (decode(v) > 0) {
	            if ((candidates ^ v) < candidates) {
	                candidates ^= v;
	            }
	        }
	    }
		return candidates;
	}

	//Attempt to reduce the value of 
	private static int reduceRow(int[] masks, int index, int candidates) {
		int r = index / 9;
		for (int i = r * 9; i < (r + 1) * 9; i++) {
			
			//ignore self
	        if (i == index) {
	            continue;
	        }
	        
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
