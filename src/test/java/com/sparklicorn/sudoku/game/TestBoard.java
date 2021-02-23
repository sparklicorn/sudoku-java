package com.sparklicorn.sudoku.game;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.*;

public class TestBoard {

	private static List<BoardFixture> boardFixtures;
    private static final String boardFixturesFileName = "sudoku-board-fixtures.txt";

    @BeforeClass
    public static void beforeClass() throws Exception {
        System.out.print("Loading test fixtures... ");
        boardFixtures = BoardFixture.loadFromFile(boardFixturesFileName);
        if (boardFixtures.size() == 0) {
            throw new Exception("Test fixtures failed to load");
        }
        System.out.printf("Done. (%d fixtures loaded)%n", boardFixtures.size());
    }

	@Test
    public void isRowValid() {
        for(BoardFixture fixture : boardFixtures) {
            for (int row = 0; row < Board.NUM_ROWS; row++) {
                assertEquals(
                    String.format(
                        "Row (%d) [%s] of board %n%s%n%s%n",
						row,
                        fixture.getRowAsString(row),
                        fixture.board.getSimplifiedString(),
                        fixture.board.toString()
                    ),
                    fixture.validRows.contains(row),
                    fixture.board.isRowValid(row)
                );
            }
        }
    }

    @Test
    public void isColValid() {
        for(BoardFixture fixture : boardFixtures) {
            for (int col = 0; col < Board.NUM_COLS; col++) {
                assertEquals(
                    String.format(
                        "Col (%d) [%s] of board %n%s%n%s%n",
                        col,
						fixture.getColumnAsString(col),
                        fixture.board.getSimplifiedString(),
                        fixture.board.toString()
                    ),
                    fixture.validCols.contains(col),
                    fixture.board.isColValid(col)
                );
            }
        }
    }

    @Test
    public void isRegionValid() {
        for(BoardFixture fixture : boardFixtures) {
            for (int region = 0; region < Board.NUM_REGIONS; region++) {
				assertEquals(
					String.format(
						"Region (%d) [%s] of board %n%s%n%s%n",
						region,
						fixture.getRegionAsString(region),
						fixture.board.getSimplifiedString(),
						fixture.board.toString()
					),
					fixture.validRegions.contains(region),
					fixture.board.isRegionValid(region)
				);
            }
        }
    }

    @Test
    public void isRowFull() {
        for(BoardFixture fixture : boardFixtures) {
            for (int row = 0; row < Board.NUM_ROWS; row++) {
                assertEquals(
                    String.format(
                        "Row (%d) [%s] of board %n%s%n%s%n",
                        row,
						fixture.getRowAsString(row),
                        fixture.board.getSimplifiedString(),
                        fixture.board.toString()
                    ),
                    fixture.fullRows.contains(row),
                    fixture.board.isRowFull(row)
                );
            }
        }
    }

    @Test
    public void isColFull() {
        for(BoardFixture fixture : boardFixtures) {
            for (int col = 0; col < Board.NUM_COLS; col++) {
                assertEquals(
                    String.format(
                        "Col (%d) [%s] of board %n%s%n%s%n",
                        col,
						fixture.getColumnAsString(col),
                        fixture.board.getSimplifiedString(),
                        fixture.board.toString()
                    ),
                    fixture.fullCols.contains(col),
                    fixture.board.isColFull(col)
                );
            }
        }
    }

    @Test
    public void isRegionFull() {
        for(BoardFixture fixture : boardFixtures) {
            for (int region = 0; region < Board.NUM_REGIONS; region++) {
				assertEquals(
					String.format(
						"Region (%d) [%s] of board %n%s%n%s%n",
						region,
						fixture.getRegionAsString(region),
						fixture.board.getSimplifiedString(),
						fixture.board.toString()
					),
					fixture.fullRegions.contains(region),
					fixture.board.isRegionFull(region)
				);
            }
        }
    }

    @Test
    public void isValid() {
        for(BoardFixture fixture : boardFixtures) {
            assertEquals(
                String.format(
                    "Board %n%s%n%s%n",
                    fixture.board.getSimplifiedString(),
                    fixture.board.toString()
                ),
                fixture.isValid,
                fixture.board.isValid()
            );
        }
    }

    @Test
    public void isFull() {
        for(BoardFixture fixture : boardFixtures) {
            assertEquals(
                String.format(
                    "Board %n%s%n%s%n",
                    fixture.board.getSimplifiedString(),
                    fixture.board.toString()
                ),
                fixture.isFull,
                fixture.board.isFull()
            );
        }
    }

    @Test
    public void isComplete() {
        for(BoardFixture fixture : boardFixtures) {
            assertEquals(
                String.format(
                    "Board %n%s%n%s%n",
                    fixture.board.getSimplifiedString(),
                    fixture.board.toString()
                ),
                fixture.isComplete,
                fixture.board.isComplete()
            );
        }
    }

	@Test
	public void testGetNumEmptySpaces() {
		for(BoardFixture fixture : boardFixtures) {
            assertEquals(
                String.format(
                    "Board %n%s%n%s%n",
                    fixture.board.getSimplifiedString(),
                    fixture.board.toString()
                ),
                Board.NUM_CELLS - fixture.board.numClues,
                fixture.board.getNumEmptySpaces()
            );
        }
	}

	@Test
	public void testGetNumClues() {
		for(BoardFixture fixture : boardFixtures) {
            assertEquals(
                String.format(
                    "Board %n%s%n%s%n",
                    fixture.board.getSimplifiedString(),
                    fixture.board.toString()
                ),
                fixture.numClues,
                fixture.board.getNumClues()
            );
        }
	}

	@Test
	public void testGetValues() {
		Board b = new Board();
		assertArrayEquals(new int[81], b.getDigits(new int[Board.NUM_CELLS]));

		b = new Board("59..1...218......5....6.4.97.......3.48.29.6...5.7..8......32..93.14.......2.7..8");
		assertArrayEquals(new int[] {
			5, 9, 0, 0, 1, 0, 0, 0, 2, 1, 8, 0, 0, 0, 0, 0, 0, 5, 0, 0, 0, 0, 6, 0, 4, 0, 9, 7,
			0, 0, 0, 0, 0, 0, 0, 3, 0, 4, 8, 0, 2, 9, 0, 6, 0, 0, 0, 5, 0, 7, 0, 0, 8, 0, 0, 0,
			0, 0, 0, 3, 2, 0, 0, 9, 3, 0, 1, 4, 0, 0, 0, 0, 0, 0, 0, 2, 0, 7, 0, 0, 8
		}, b.getDigits(new int[Board.NUM_CELLS]));

		b = new Board("11111111111111111111111111111111111111111111111111111111111111111111111111111111.");
		assertArrayEquals(new int[] {
			1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
			1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
			1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0
		}, b.getDigits(new int[Board.NUM_CELLS]));

		b = new Board(".....1111111111111111111111111111111111111111111111111111111111111111111111111111");
		assertArrayEquals(new int[] {
			0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
			1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
			1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1
		}, b.getDigits(new int[Board.NUM_CELLS]));

		b = new Board(".................................................................................");
		assertArrayEquals(new int[81], b.getDigits(new int[Board.NUM_CELLS]));

		b = new Board(new int[Board.NUM_CELLS]);
		assertArrayEquals(new int[Board.NUM_CELLS], b.getDigits(new int[81]));
	}

	@Test
	public void testGetValueAt() {
		Board b = new Board();
		for (int i = 0; i < Board.NUM_CELLS; i++) {
			assertEquals(0, b.getDigitAt(i));
		}

		String bStr = "590010002180000005000060409700000003048029060005070080000003200930140000000207008";
		b = new Board(bStr);
		for (int i = 0; i < Board.NUM_CELLS; i++) {
			assertEquals(bStr.charAt(i) - '0', b.getDigitAt(i));
		}

		bStr = "111111111111111111111111111111111111111111111111111111111111111111111111111111110";
		b = new Board(bStr);
		for (int i = 0; i < Board.NUM_CELLS; i++) {
			assertEquals(bStr.charAt(i) - '0', b.getDigitAt(i));
		}

		bStr = "000001111111111111111111111111111111111111111111111111111111111111111111111111111";
		b = new Board(bStr);
		for (int i = 0; i < Board.NUM_CELLS; i++) {
			assertEquals(bStr.charAt(i) - '0', b.getDigitAt(i));
		}

		bStr = "000000000000000000000000000000000000000000000000000000000000000000000000000000000";
		b = new Board(bStr);
		for (int i = 0; i < Board.NUM_CELLS; i++) {
			assertEquals(bStr.charAt(i) - '0', b.getDigitAt(i));
		}

		b = new Board(new int[Board.NUM_CELLS]);
		for (int i = 0; i < Board.NUM_CELLS; i++) {
			assertEquals(0, b.getDigitAt(i));
		}
	}

	@Test
	public void testSetValueAt() {
		Board b = new Board();
		for (int i = 0; i < Board.NUM_CELLS; i++) {
			for (int v = 0; v <= 9; v++) {
				b.setDigitAt(i, v);
				assertEquals(v, b.getDigitAt(i));
			}
		}
	}

	@Test
	public void testEquals() {
		//Test comparison with empty board.
		Board emptyBoard = new Board();

		//Null reference.
		assertEquals(false, emptyBoard.equals(null));

		//Reflexive property.
		assertEquals(true, emptyBoard.equals(emptyBoard));

		//Symmetric property.
		Board x = new Board("793458261218963754456271893634712589185649327927385146541836972872194635369527418");
		Board y = new Board("793458261218963754456271893634712589185649327927385146541836972872194635369527418");
		Board z = new Board("523419687916837245478562391234678159681945732795321864352796418169284573847153926");
		assertEquals(true, x.equals(y));
		assertEquals(true, y.equals(x));
		assertEquals(false, x.equals(z));
		assertEquals(false, z.equals(x));
		assertEquals(false, y.equals(z));
		assertEquals(false, z.equals(y));

		//Transitive property.
		z = new Board("793458261218963754456271893634712589185649327927385146541836972872194635369527418");
		assertEquals(true, x.equals(y));
		assertEquals(true, y.equals(z));
		assertEquals(true, x.equals(z));

		z = new Board("523419687916837245478562391234678159681945732795321864352796418169284573847153926");
		//Consitent property.
		for (int i = 0; i < 1000; i++) {
			assertEquals(true, x.equals(y));
			assertEquals(false, x.equals(z));
		}

		//Reference equality.
		assertEquals(false, x == y);
		assertEquals(false, x == z);
		assertEquals(false, z == y);
		y = x;
		assertEquals(true, x == y);

		//Test equality of several different boards.
		for (int i = 0; i < boardFixtures.size(); i++) {
			Board boardA = boardFixtures.get(i).board;
			for (int j = i + 1; j < boardFixtures.size(); j++) {
				Board boardB = boardFixtures.get(j).board;
				assertEquals( false, boardA.equals(boardB));
			}
		}
	}

	@Test
	public void testCopy() {
		//Test empty board copy.
		Board b = new Board();
		Board b2 = b.copy();
		//b and b2 should be separate Board instances.
		assertEquals(false, b == b2);
		//But they should still be equal.
		assertEquals(true, b.equals(b2));
		assertEquals(true, b2.equals(b));
		//Each value on the boards should be the same.
		for (int i = 0; i < Board.NUM_CELLS; i++) {
			assertEquals(true, b.getDigitAt(i) == b2.getDigitAt(i));
			assertEquals(true, b.getMaskAt(i) == b2.getMaskAt(i));
		}

		String bStr = "590010002180000005000060409700000003048029060005070080000003200930140000000207008";
		b = new Board(bStr);
		b2 = b.copy();
		//b and b2 should be separate Board instances.
		assertEquals(false, b == b2);
		//But they should still be equal.
		assertEquals(true, b.equals(b2));
		assertEquals(true, b2.equals(b));
		//Each value on the boards should be the same.
		for (int i = 0; i < Board.NUM_CELLS; i++) {
			assertEquals(true, b.getDigitAt(i) == b2.getDigitAt(i));
			assertEquals(true, b.getMaskAt(i) == b2.getMaskAt(i));
		}

		bStr = "111111111111111111111111111111111111111111111111111111111111111111111111111111110";
		b = new Board(bStr);
		b2 = b.copy();
		//b and b2 should be separate Board instances.
		assertEquals(false, b == b2);
		//But they should still be equal.
		assertEquals(true, b.equals(b2));
		assertEquals(true, b2.equals(b));
		//Each value on the boards should be the same.
		for (int i = 0; i < Board.NUM_CELLS; i++) {
			assertEquals(true, b.getDigitAt(i) == b2.getDigitAt(i));
			assertEquals(true, b.getMaskAt(i) == b2.getMaskAt(i));
		}

		bStr = "000001111111111111111111111111111111111111111111111111111111111111111111111111111";
		b = new Board(bStr);
		b2 = b.copy();
		//b and b2 should be separate Board instances.
		assertEquals(false, b == b2);
		//But they should still be equal.
		assertEquals(true, b.equals(b2));
		assertEquals(true, b2.equals(b));
		//Each value on the boards should be the same.
		for (int i = 0; i < Board.NUM_CELLS; i++) {
			assertEquals(true, b.getDigitAt(i) == b2.getDigitAt(i));
			assertEquals(true, b.getMaskAt(i) == b2.getMaskAt(i));
		}
	}
}
