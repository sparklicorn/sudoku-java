package com.mycompany.app.drivers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

import com.mycompany.app.gui.SudokuGuiDemo;
import com.mycompany.app.sudoku.Board;
import com.mycompany.app.sudoku.GenerateConfigs;
import com.mycompany.app.sudoku.GeneratePuzzles;
import com.mycompany.app.sudoku.Generator;
import com.mycompany.app.sudoku.Solver;
import com.mycompany.app.sudoku.puzzles.GeneratedPuzzles;
import com.mycompany.app.util.ThreadPool;

/**
 * Main driver for the Sudoku project. Commands/Options:
 *
 * "play" Open the GUI to play Sudoku. Default if commands omitted.
 *
 * "configs [integer n (1)] [boolean normalize (false)]" Generate 'n' number of
 * Sudoku configurations, optionally normalized. n - Number of configurations to
 * generate. normalize - Normalize the configurations so that the top row reads
 * '123456789'.
 *
 * "puzzles [integer n (1)] [integer clues (27)]" Generate 'n' number of Sudoku
 * puzzles with 'clues' number of clues.
 *
 * "benchy" Runs puzzle solver benchmarking.
 */
public class Main {

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        // default command
        String command = "play";

        if (args != null) {
            if (args.length >= 1) {
                command = args[0];
            }
        }

        switch (command) {
            case "play":
                if (args.length >= 2) {
                    SudokuGuiDemo.main(new String[] { args[1] });
                } else {
                    SudokuGuiDemo.main(null);
                }
                break;
            case "configs":
                if (args.length >= 3) {
                    GenerateConfigs.main(new String[] { args[1], args[2] });
                } else if (args.length >= 2) {
                    GenerateConfigs.main(new String[] { args[1] });
                } else {
                    GenerateConfigs.main(null);
                }
                break;
            case "puzzles":
                if (args.length >= 3) {
                    GeneratePuzzles.main(new String[] { args[1], args[2] });
                } else if (args.length >= 2) {
                    GeneratePuzzles.main(new String[] { args[1] });
                } else {
                    GeneratePuzzles.main(null);
                }
                break;
            case "solve":
                if (args.length >= 2) {
                    Board board = new Board(args[1]);
                    System.out.println("Finding solutions for board:");
                    System.out.println(board);
                    Solver.findAllSolutions(board, (b) -> {
                        System.out.println(b.getSimplifiedString());
                    });
                } else {
                    System.out.println(
                        "Usage: solve {board string, ex: ...234...657...198" +
                        "...............................................................}"
                    );
                }
                break;
            case "generate":
                int numConfigs = Integer.valueOf(args[1]);
                Board[] configs = new Board[numConfigs];
                int interval = numConfigs / 100;
                for (int i = 0; i < numConfigs; i++) {
                    if (i % interval == 0 && i > 0) {
                        System.out.print('.');
                    }
                    configs[i] = Generator.generateConfig();
                }
                System.out.println('#');

                ThreadMXBean bean = ManagementFactory.getThreadMXBean();
                long start = bean.getCurrentThreadCpuTime();
                File stringsFile = new File("test-strings.txt");
                PrintWriter pw = new PrintWriter(stringsFile);
                for (Board b : configs) {
                    pw.println(b.getSimplifiedString());
                }
                pw.close();
                long end = bean.getCurrentThreadCpuTime();
                System.out.printf("Wrote configs in %d ms.%n", TimeUnit.NANOSECONDS.toMillis(end - start));

                start = bean.getCurrentThreadCpuTime();
                File serialFile = new File("test-serial.txt");
                FileOutputStream f = new FileOutputStream(serialFile);
                ObjectOutputStream o = new ObjectOutputStream(f);
                for (Board b : configs) {
                    o.writeObject(b);
                }
                o.close();
                f.close();
                end = bean.getCurrentThreadCpuTime();
                System.out.printf("Serialized configs in %d ms.%n", TimeUnit.NANOSECONDS.toMillis(end - start));

                start = bean.getCurrentThreadCpuTime();
                Scanner scanner = new Scanner(stringsFile);
                int index = 0;
                while (scanner.hasNextLine()) {
                    String line = scanner.nextLine();
                    configs[index % configs.length] = new Board(line);
                }
                end = bean.getCurrentThreadCpuTime();
                System.out.printf("Read configs in %d ms.%n", TimeUnit.NANOSECONDS.toMillis(end - start));

                start = bean.getCurrentThreadCpuTime();
                FileInputStream fi = new FileInputStream(serialFile);
                ObjectInputStream oi = new ObjectInputStream(fi);
                Object obj;
                int count = 0;
                try {
                    while ((obj = oi.readObject()) != null) {
                        configs[count++ % configs.length] = (Board) obj;
                    }
                } catch (Exception e) {
                    // do nothing
                }

                end = bean.getCurrentThreadCpuTime();
                System.out.printf("Deserialized %d configs in %d ms.%n", count,
                        TimeUnit.NANOSECONDS.toMillis(end - start));
                break;
            case "benchy":
                boolean verbose = false;
                if (args.length > 1) {
                    verbose = args[1].equals("true");
                }

                benchy(verbose);
                break;
            default:
                System.out.println("Sudoku: Command not recognized.");
        }
    }

    private static void benchy(boolean verbose) {
        List<Board> boards = GeneratedPuzzles.convertStringsToBoards(
            GeneratedPuzzles.PUZZLES_24_1000
        );

        System.out.printf("%d boards loaded.%n", boards.size());

        List<Runnable> timedBoardSolvers = new ArrayList<>();
        List<Long> solveTimes = Collections.synchronizedList(new ArrayList<>());
        for (Board b : boards) {
            timedBoardSolvers.add(() -> {
                long cpuTime = timeCpuExecution(() -> {
                    Board solution = Solver.solve(b);
                    if (verbose) {
                        System.out.printf("%s  =>  %s%n", b.getSimplifiedString(), solution.getSimplifiedString());
                    }
                });
                // System.out.printf("Puzzle solved in %d ms.%n", TimeUnit.NANOSECONDS.toMillis(cpuTime));
                solveTimes.add(cpuTime);
            });
        }

        final long startRealTime = System.currentTimeMillis();
        final int numThreads = Runtime.getRuntime().availableProcessors();
        ThreadPool.doBatch(
            timedBoardSolvers,
            numThreads,
            () -> {
                long totalCpuTime = 0L;
                for (long time : solveTimes) {
                    totalCpuTime += time;
                }
                System.out.printf(
                    "%nReal time to solve all puzzles: %s.%n",
                    formatDuration(System.currentTimeMillis() - startRealTime)
                );
                System.out.printf(
                    "Total cpu time to solve all puzzles: %s [%s / thread].%n",
                    formatDuration(TimeUnit.NANOSECONDS.toMillis(totalCpuTime)),
                    formatDuration(TimeUnit.NANOSECONDS.toMillis(totalCpuTime / numThreads))
                    );
                System.out.printf("Using %d threads.%n", numThreads);
            }
        );
    }

    private static String formatDuration(long milli) {
        long mins = milli / 1000L / 60L;
        long secs = (milli / 1000L) % 60L;
        milli %= 1000L;

        String secString = String.format("%d.%d s", secs, milli);
        if (mins > 0L) {
            return String.format("%d m   %s", mins, secString);
        }

        return secString;
    }

    private static long timeCpuExecution(Runnable runnable) {
        ThreadMXBean bean = ManagementFactory.getThreadMXBean();
        long start = bean.getCurrentThreadCpuTime();
        runnable.run();
        long end = bean.getCurrentThreadCpuTime();
        return end - start;
    }
}
