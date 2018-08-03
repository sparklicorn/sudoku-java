package com.mycompany.app.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.io.IOException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.HttpsURLConnection;
import javax.swing.JButton;
import javax.swing.JFrame;

import com.mycompany.app.sudoku.Board;
import com.mycompany.app.sudoku.Solver;
import com.mycompany.app.util.FileUtil;

public class SudokuGuiDemo {

	public static void main(String[] args) throws IOException {
		
		String str = "59..1...218......5....6.4.97.......3.48.29.6...5.7..8......32..93.14.......2.7..8";
		
		URL url = new URL("https://sudonicornoku.herokuapp.com/api/getrandom?clues=27");
		HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
		String content = FileUtil.getContent(con).trim();
		
		System.out.println("content: " + content);
		//System.out.println(content.length());
		
		//{"id":1,"puzzle":".1..54.2..6.1...5..9..6.1.........92....13...93..285..2.35..87...82.1..5....37..6"}
		
		Pattern p = Pattern.compile(".*puzzle\":\"([1-9.]+)\".*");
		Matcher m = p.matcher(content);
		if (m.matches()) {
			content = m.group(1);
		}
		
		Board b = new Board(content);
		System.out.println(b);
		
		JFrame f = new JFrame();
		f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		f.setBackground(Color.white);
		SudokuBoard panel = new SudokuBoard(b);
		panel.setPreferredSize(new Dimension(600, 600));
		f.getContentPane().add(panel, BorderLayout.CENTER);
		
		JButton btn = new JButton("Solve");
		btn.addActionListener((event) -> {
			Board solution = Solver.solve(panel.board);
			
			if (solution == null) {
				System.out.println("null solution");
				return;
			}
			
			for (int i = 0; i < Board.NUM_CELLS; i++) {
				panel.cells[i].digit = solution.getValueAt(i);
			}
			panel.repaint();
		});
		f.getContentPane().add(btn, BorderLayout.SOUTH);
		
		f.pack();
		f.setVisible(true);
		
		System.out.println(Solver.solve(b).getSimplifiedString());
		
	}

}
