package org.ece16.dl4j.mnist;

import java.io.ByteArrayOutputStream;
import java.io.RandomAccessFile;

import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Prints MNIST images on stdout.
 * The MNIST file format supported by this class is described at {@linktourl http://yann.lecun.com/exdb/mnist/}.
 * The implementation below is adapted from {@linktourl https://github.com/jeffgriffith/mnist-reader}.
 */
public class MnistReader {
	public static final int LABEL_FILE_MAGIC_NUMBER = 2049;
	public static final int IMAGE_FILE_MAGIC_NUMBER = 2051;

	public static void main(String [] arg) {
		if(arg.length < 2 || arg.length > 3) {
			System.err.println("usage: java org.ece16.dl4j.MnistReader label-file image-file [image-number]");
			System.exit(1);
		}

		String file_label = arg[0];
		String file_image = arg[1];

		int [] labels = MnistReader.getLabels(file_label);
		List<int[][]> images = MnistReader.getImages(file_image);

		if(labels.length != images.size()) {
			throw new RuntimeException("Number of labels and images don't match");
		}

		if(images.get(0).length != 28) {
			throw new RuntimeException("Number of pixel is not 28");
		}

		if(images.get(0)[0].length != 28) {
			throw new RuntimeException("Number of pixel columms is not 28");
		}

		if(arg.length == 2) {
			for(int i = 0; i < Math.min(5, labels.length); i++) {
				printf("+---[ID:%05d, LABEL:%d]------+\n", i, labels[i]);
				printf("%s", MnistReader.renderImage(images.get(i)));
			}
		}
		else {
			int image_number = Integer.parseInt(arg[2]);
			Scanner scanner = new Scanner(System.in);

			while(image_number >= 0 && image_number < labels.length) {
				printf("+---[ID:%05d, LABEL:%d]------+\n", image_number, labels[image_number]);
				printf("%s", MnistReader.renderImage(images.get(image_number)));
				printf("+----------------------------+\n");
				printf("next id: ");

				if(scanner.hasNextInt()) {
					image_number = scanner.nextInt();
				}
				else {
					image_number = -1;
				}
			}

			scanner.close();
		}
	}

	public static int[] getLabels(String infile) {
		ByteBuffer bb = loadFileToByteBuffer(infile);

		assertMagicNumber(LABEL_FILE_MAGIC_NUMBER, bb.getInt());

		int numLabels = bb.getInt();
		int[] labels = new int[numLabels];

		for (int i = 0; i < numLabels; ++i) {
			labels[i] = bb.get() & 0xFF; // To unsigned
		}

		return labels;
	}

	public static List<int[][]> getImages(String infile) {
		ByteBuffer bb = loadFileToByteBuffer(infile);

		assertMagicNumber(IMAGE_FILE_MAGIC_NUMBER, bb.getInt());

		int numImages = bb.getInt();
		int numRows = bb.getInt();
		int numColumns = bb.getInt();
		List<int[][]> images = new ArrayList<>();

		for (int i = 0; i < numImages; i++) {
			images.add(readImage(numRows, numColumns, bb));
		}

		return images;
	}

	private static int[][] readImage(int numRows, int numCols, ByteBuffer bb) {
		int[][] image = new int[numRows][];

		for (int row = 0; row < numRows; row++) {
			image[row] = readRow(numCols, bb);
		}

		return image;
	}

	private static int[] readRow(int numCols, ByteBuffer bb) {
		int[] row = new int[numCols];

		for (int col = 0; col < numCols; ++col) {
			row[col] = bb.get() & 0xFF; // To unsigned
		}

		return row;
	}

	private static void assertMagicNumber(int expectedMagicNumber, int magicNumber) {
		if (expectedMagicNumber != magicNumber) {
			switch (expectedMagicNumber) {
			case LABEL_FILE_MAGIC_NUMBER:
				throw new RuntimeException("This is not a label file.");
			case IMAGE_FILE_MAGIC_NUMBER:
				throw new RuntimeException("This is not an image file.");
			default:
				throw new RuntimeException(
						String.format("Expected magic number %d, found %d", expectedMagicNumber, magicNumber)
						);
			}
		}
	}

	private static void printf(String format, Object... args) {
		System.out.printf(format, args);
	}

	private static ByteBuffer loadFileToByteBuffer(String infile) {
		return ByteBuffer.wrap(loadFile(infile));
	}

	private static byte[] loadFile(String infile) {
		try {
			RandomAccessFile f = new RandomAccessFile(infile, "r");
			FileChannel chan = f.getChannel();
			long fileSize = chan.size();
			ByteBuffer bb = ByteBuffer.allocate((int) fileSize);
			chan.read(bb);
			bb.flip();
			ByteArrayOutputStream baos = new ByteArrayOutputStream();

			for (int i = 0; i < fileSize; i++) {
				baos.write(bb.get());
			}

			chan.close();
			f.close();

			return baos.toByteArray();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private static String renderImage(int[][] image) {
		StringBuffer sb = new StringBuffer();

		for (int row = 0; row < image.length; row++) {
			sb.append("|");

			for (int col = 0; col < image[row].length; col++) {
				int pixelVal = image[row][col];
				if (pixelVal == 0) {
					sb.append(" ");
				}
				else if (pixelVal < 256 / 3) {
					sb.append(".");
				}
				else if (pixelVal < 2 * (256 / 3)) {
					sb.append("x");
				}
				else {
					sb.append("X");
				}
			}

			sb.append("|\n");
		}

		return sb.toString();
	}
}