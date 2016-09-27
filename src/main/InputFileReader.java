package main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;

public class InputFileReader implements Callable<Void> {

	File file;
	BlockingQueue<Integer>[] q;
	int splitLimit;

	public InputFileReader(File file, BlockingQueue<Integer>[] q, int splitLimit) {
		this.file = file;
		this.q = q;
		this.splitLimit = splitLimit;
	}

	public Void call() {
		BufferedReader br = null;
		String possibleInteger = null;

		try {
			br = new BufferedReader(new FileReader(file));

			while ((possibleInteger = br.readLine()) != null) {
				int value = Integer.parseInt(possibleInteger);
				System.out.println(String.format("INFO: [%s] Reading integer %s from file %s on thread: %s", this.getClass().getName(), value, file.getName(),
						Thread.currentThread().getName()));
				q[value / splitLimit].put(value);
			}

			System.out.println(String.format("INFO: [%s] Done reading file %s on thread: %s", this.getClass().getName(), file.getName(), Thread.currentThread().getName()));
		} catch (FileNotFoundException e) {
			System.err.println(String.format("ERROR: [%s] File %s not found on thread: %s", this.getClass().getName(), this.file, Thread.currentThread().getName()));
			e.printStackTrace();
		} catch (IOException e) {
			System.err.println(String.format("ERROR: [%s] IOException on file %s on thread: %s", this.getClass().getName(), this.file, Thread.currentThread().getName()));
			e.printStackTrace();
		} catch (NumberFormatException e) {
			System.err.println(String.format("WARN: [%s] Invalid integer %s in file %s on thread: %s", this.getClass().getName(), possibleInteger, this.file,
					Thread.currentThread().getName()));
			e.printStackTrace();
		} catch (InterruptedException e) {
			System.err.println(String.format("ERROR: [%s] InterruptedException on thread: %s", this.getClass().getName(), Thread.currentThread().getName()));
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		return null;
	}

}