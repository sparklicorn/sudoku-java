package com.mycompany.app.sudoku;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.Stack;
import java.util.concurrent.ThreadLocalRandom;

public class Main {

    public static void main(String[] args) {
        Board ba = new Board(".1......5..8...........2...56...........7.6...3..6...48..4.......93.......4.9....");
        Set<Board> set = Solver.getAllSolutions(ba);
        System.out.println("Found " + set.size() + " solutions!");
        for (Board _b : set) {
            System.out.println(_b);
        }
        System.out.println("Attempting to find any solution...");
        System.out.println(Solver.solve(ba));
        System.out.println(ba);
    }

    private static class Node {
        Board b;
        private List<Node> nexts;
        Node prev;
        boolean visited;

        Node(Board b, Node prev) {
            this.b = b;
            this.prev = prev;
            visited = false;
        }

        void visit() {
            visited = true;
        }
        
        void kill() { //release memory
            b = null;
            nexts = null;
        }
        
        private void findNexts() {
            if (nexts == null) {
                nexts = new ArrayList<>();
                for (int i = 0; i < Board.NUM_CELLS; i++) {
                    if (b.getValueAt(i) > 0) {
                        Board bCopy = new Board(b);
                        bCopy.setValueAt(i, 0);
                        if (bCopy.isValid()) {
                            nexts.add(new Node(bCopy, this));
                        }
                    }
                }
            }
        }
        
        List<Node> getNeighbors() {
            if (nexts == null)
                findNexts();
            return new ArrayList<>(nexts);
        }
        
        Node getNextUnvisited() {
            if (nexts == null)
                findNexts();
            ArrayList<Node> bag = new ArrayList<>();
            for (Node n : nexts) {
                if (n != null && !n.visited) {
                    bag.add(n);
                }
            }
            if (bag.isEmpty())
                return null;
            return bag.get(ThreadLocalRandom.current().nextInt(bag.size()));
        }

        @Override
        public int hashCode() {
            return b.hashCode();
        }

        @Override
        public boolean equals(Object other) {
            if (other == this)
                return true;
            if (other instanceof Node) {
                Node _other = (Node) other;
                return b.equals(_other.b);
            }
            return false;
        }

    }

    private static Set<Board> bruteforceSolveDFS(Board board) {
        HashSet<Board> result = new HashSet<>();
        Stack<Node> stack = new Stack<>();
        Node root = new Node(board, null);
        stack.push(root);

        int pollsPerDot = 1000000000 / 80;
        int pc = 0; //poll counter

        while (!stack.isEmpty()) {
            Node n = stack.peek();
            n.visit();

            if (++pc % pollsPerDot == 0) {
                System.out.print('.');
                if (pc % (pollsPerDot * 80) == 0) {
                    System.out.println();
                }
            }

            if (n.b.isFull()) {
                if(result.add(n.b)) {
                    System.out.println();
                    System.out.println(n.b.getSimplifiedString());
                }
                stack.pop();
                n.kill();
            } else {
                Node next = n.getNextUnvisited();
                if (next != null) {
                    stack.push(next);
                } else {
                    stack.pop();
                    n.kill();
                }
            }

        }

        return result;
    }

    private static Set<Board> bruteforceSolveBFS(Board board) {
        HashSet<Board> result = new HashSet<>();
        Queue<Board> q = new ArrayDeque<>();
        q.add(board);

        int pollsPerDot = 10000;
        int pc = 0; //poll counter

        while (!q.isEmpty()) {
            Board b = q.poll();

            if (++pc % pollsPerDot == 0) {
                System.out.print('.');
                if (pc % (pollsPerDot * 80) == 0) {
                    System.out.println();
                }
            }

            boolean full = b.isFull();

            if (full) {
                if(result.add(b)) {
                    System.out.println(b.getSimplifiedString());
                }

            } else {
                for (int i = 0; i < Board.NUM_CELLS; i++) {
                    if (b.getValueAt(i) == 0) {
                        for (int v = 1; v <= 9; v++) {
                            Board bCopy = new Board(b);
                            bCopy.setValueAt(i, v);
                            if (bCopy.isValid()) {
                               q.offer(bCopy);
                            }
                        }
                        break;
                    }
                }
            }
        }

        System.out.println();

        return result;
    }

}
