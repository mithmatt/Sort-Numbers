package main;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.PriorityQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;

public class OutputFileWriter implements Callable<Void> {

	String filename;
	boolean keepExecuting;
	BlockingQueue<Integer> q;

	public OutputFileWriter(String filename, BlockingQueue<Integer> q) {
		this.filename = filename;
		this.q = q;
		this.keepExecuting = true;
	}

	public void stopExecution() {
		this.keepExecuting = false;
	}

	public Void call() {
		PriorityQueue<Integer> pq = new PriorityQueue<Integer>();

		do {
			try {
				if (!q.isEmpty())
					pq.offer(q.take());
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		} while (this.keepExecuting || !q.isEmpty());

		BufferedWriter bw = null;
		int value;
		File file = new File(filename);

		try {
			file.createNewFile();
			bw = new BufferedWriter(new FileWriter(file));

			while (!pq.isEmpty()) {
				value = pq.poll();
				System.out.println(String.format("INFO: [%s] Writing integer %s into file %s on thread: %s", this.getClass().getName(), value, file.getName(),
						Thread.currentThread().getName()));
				bw.write(String.valueOf(value) + "\n");
			}

			bw.flush();
			bw.close();

			System.out.println(String.format("INFO: [%s] Done writing file %s on thread: %s", this.getClass().getName(), file.getName(), Thread.currentThread().getName()));
		} catch (IOException e) {
			System.err.println(String.format("ERROR: [%s] IOException on file %s on thread: %s", this.getClass().getName(), file.getName(), Thread.currentThread().getName()));
			e.printStackTrace();
		}

		return null;
	}

}