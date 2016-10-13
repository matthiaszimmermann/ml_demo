package org.ece16.surus;

import java.io.File;
import java.io.IOException;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class BostonHousingDataLoader {
	public static final String DEFAULT_DATA_PATH = "data/housing.data";
	
	public static double [][] doubleMatrixFromFile(String filename) throws IOException {
		List<String> lines = readLines(filename);
		double [][] matrix = new double [lines.size()][];
		
		int row = 0;
		int col = 0;
		
        for(String line: lines) {
			String [] cells = line.trim().split("\\s+");
			matrix[row] = new double[cells.length];
			
			for(String cell: cells) {
                matrix[row][col++] = Double.parseDouble(cell);
            }

			col = 0;
            row++;
        }

        return matrix;
	}
	
	protected static List<String> readLines(String filename) throws IOException {
        List<String> list = new ArrayList<>();
        Scanner s = new Scanner(new File(filename));
		
        while (s.hasNextLine()){
            list.add(s.nextLine());
        }
		
        s.close();
		
		return list;
	}
	
	public static void main(String [] args) throws IOException {
		double [][] m = null;
		int row = 0;
		int col = 0;
		
		if(args.length > 0) {
			m = doubleMatrixFromFile(args[0]);
		}
		else {
			m = doubleMatrixFromFile(DEFAULT_DATA_PATH);
		}
		
		for(row = 0; row < m.length; row++) {
			int cols = m[row].length;
			
			printf("m[%d,%d]={", row, col);
			
			for(col = 0; col < cols; col++) {
			  print(m[row][col]);
			  
			  if(col < cols - 1) {
				  print(", ");
			  }
			  else {
			      print("}");
			  }
			}
			
			println();
		}
	}
	
	private static void printf(String f, Object... args) {
		print(String.format(f, args));
	}
	
	private static void println() {
		System.out.println();
	}
	
	private static void print(Object o) {
		System.out.print(o);
	}
}
