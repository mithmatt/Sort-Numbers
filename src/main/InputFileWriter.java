package main;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;
import java.util.concurrent.Callable;

public class InputFileWriter implements Callable<Void> {

	File file;
	int length, maxValue;

	public InputFileWriter(File file, int length, int maxValue) {
		this.file = file;
		this.length = length;
		this.maxValue = maxValue;
	}

	public Void call() {
		Random rand = new Random();
		BufferedWriter bw = null;
		int value;

		try {
			file.createNewFile();
			bw = new BufferedWriter(new FileWriter(file));

			for (int i = 0; i < length; i++) {
				value = rand.nextInt(maxValue);
				System.out.println(String.format("INFO: [%s] Writing integer %s to file %s on thread: %s", this.getClass().getName(), value, file.getName(),
						Thread.currentThread().getName()));
				bw.write(String.valueOf(value) + "\n");
			}
			bw.flush();
			bw.close();

			System.out.println(String.format("INFO: [%s] Done writing file %s on thread: %s", this.getClass().getName(), file.getName(), Thread.currentThread().getName()));
		} catch (IOException e) {
			System.out.println(String.format("ERROR: [%s] IOException on %s from thread: %s", this.getClass().getName(), file.getName(), Thread.currentThread().getName()));
			e.printStackTrace();
		} finally {
			if (bw != null) {
				try {
					bw.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return null;
	}

}