package org.ece16.lof;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class BreastCancerWisconsinDataLoader {
	public static final String DEFAULT_DATA_PATH = "data/wdbc.data";
	public static final double threshold = 0.1;

	private String [] name = null;
	private String [] label = null;
	private int [] sample = null;
	private List<Integer> matrix2sample = null;
	private double [][] matrix = null;

	public BreastCancerWisconsinDataLoader(String filename) throws IOException {
		initFromFile(filename);
	}

	public List<DataNode> getDataNodes() {
		ArrayList<DataNode> nodes = new ArrayList<DataNode>();

		for(int row = 0; row < matrix.length; row++) {
			nodes.add(new DataNode(getName(row), getLabel(row), matrix[row]));  
		}

		return nodes;
	}

	public double [][] getMatrix() {
		return matrix;
	}

	public String getName(int i) {
		return name[matrix2sample.get(i)];
	}

	public String getLabel(int i) {
		return label[matrix2sample.get(i)];
	}

	public void initFromFile(String filename) throws IOException {
		List<String> lines = readLines(filename);
		List<double[]> filteredRows = new ArrayList<>();

		name = new String[lines.size()];
		label = new String[lines.size()];
		sample = new int[lines.size()];
		matrix2sample = new ArrayList<Integer>();

		int row = 0;
		int col = 0;

		for(String line: lines) {
			String [] cells = line.trim().split(",");
			double [] matrixRow = new double[cells.length - 2];
			boolean addRow = true;

			for(String cell: cells) {
				if(col >= 2) {
					matrixRow[col-2] = Double.parseDouble(cell);
				}

				col++;
			}

			if(cells[1].equals("M")) { 
				if(Math.random() > threshold) {
					addRow = false;
				}
			} 

			name[row] = cells[0];
			label[row] = cells[1];
			sample[row] = addRow ? filteredRows.size() : -1;

			if(addRow) {
				filteredRows.add(matrixRow);
				matrix2sample.add(row);
			}

			col = 0;
			row++;
		}

		matrix = new double[filteredRows.size()][];

		row = 0;
		for(double [] r: filteredRows) {
			matrix[row++] = r;
		}

		matrix2sample = new ArrayList<Integer>();
		for(row = 0; row < sample.length; row++) {
			if(sample[row] >= 0) {
				matrix2sample.add(row);
			}
		}
	}

	public static double [][] doubleMatrixFromFile(String filename) throws IOException {
		List<String> lines = readLines(filename);
		List<double[]> filteredRows = new ArrayList<>();
		double [][] matrix = new double [lines.size()][];

		int row = 0;
		int col = 0;

		int mCount = 0;
		int bCount = 0;

		for(String line: lines) {
			String [] cells = line.trim().split(",");
			double [] matrixRow = new double[cells.length - 2];
			boolean addRow = true;

			for(String cell: cells) {
				if(col >= 2) {
					matrixRow[col-2] = Double.parseDouble(cell);
				}

				col++;
			}

			matrix[row] = matrixRow;

			if(cells[1].equals("M")) { 
				if(Math.random() > threshold) {
					addRow = false;
				}

				mCount++; 
			} 
			else { 
				bCount++; 
			}

			printf("%d %s %s ", row, cells[0], cells[1]);

			if(addRow) {
				filteredRows.add(matrixRow);
				printf("include\n");
			}
			else {
				printf("skip\n");				
			}

			col = 0;
			row++;
		}

		double [][] filteredMatrix = new double [filteredRows.size()][];
		row = 0;
		for(double [] r: filteredRows) {
			filteredMatrix[row++] = r;
		}

		printf("file stats: M=%d B=%d B/(M+B)=%f\n", mCount, bCount, (float)((0.0+bCount)/(bCount + mCount)));

		return filteredMatrix;
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
