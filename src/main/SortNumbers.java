package main;

import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class SortNumbers {

	private static final String BASE_DIR = "/sort-numbers";
	private static String INPUT_DIR, OUTPUT_DIR;

	private static final int NO_OF_INPUT_FILES = 20;
	private static final int NO_OF_OUTPUT_FILES = 10;
	private static final int LENGTH_OF_INPUT_FILES = 50;
	private static final int MAX_INT_VALUE = Integer.MAX_VALUE;

	private static final int NO_OF_THREADS_GENERATE_INPUT = 10;
	private static final int NO_OF_THREADS_READ_INPUT = 10;
	private static final int NO_OF_THREADS_WRITE_OUTPUT = 10;

	private static final int DEFAULT_QUEUE_SIZE = 10;

	private static int SPLIT_SIZE;

	private static BlockingQueue<Integer>[] q = null;
	private static Map<Callable<Void>, Future<Void>> executingTasks;

	static {
		INPUT_DIR = BASE_DIR + "/input";
		OUTPUT_DIR = BASE_DIR + "/output";
		SPLIT_SIZE = MAX_INT_VALUE / NO_OF_OUTPUT_FILES;
	}

	public static void main(String[] args) {
		String inputDirectory = (Paths.get("")).toAbsolutePath().toString() + INPUT_DIR;
		checkAndCreateDirectory(inputDirectory);
		String outputDirectory = (Paths.get("")).toAbsolutePath().toString() + OUTPUT_DIR;
		checkAndCreateDirectory(outputDirectory);

		NumberGenerator.generateFiles(inputDirectory, NO_OF_INPUT_FILES, LENGTH_OF_INPUT_FILES, MAX_INT_VALUE, NO_OF_THREADS_GENERATE_INPUT);

		q = new ArrayBlockingQueue[NO_OF_OUTPUT_FILES];

		ExecutorService readFileExecutor = Executors.newFixedThreadPool(NO_OF_THREADS_READ_INPUT);
		List<Callable<Void>> readTasksList = startReadingInputFiles(inputDirectory, q);

		ExecutorService writeFileExecutor = Executors.newFixedThreadPool(NO_OF_THREADS_WRITE_OUTPUT);
		List<Callable<Void>> writeTasksList = startWritingOutputFiles(outputDirectory, writeFileExecutor, q);

		try {
			readFileExecutor.invokeAll(readTasksList);
			readFileExecutor.shutdownNow();

			if (readFileExecutor.isShutdown()) {
				for (Callable<Void> task : writeTasksList) {
					((OutputFileWriter) task).stopExecution();
					executingTasks.get(task).get();
					executingTasks.remove(task);
				}
			}

			if (executingTasks.isEmpty())
				writeFileExecutor.shutdownNow();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
	}

	public static List<Callable<Void>> startReadingInputFiles(String inputDirectory, BlockingQueue<Integer>[] q) {
		List<Callable<Void>> readTasksList = new ArrayList<Callable<Void>>();
		Callable<Void> task = null;

		for (int i = 0; i < q.length; i++)
			q[i] = new ArrayBlockingQueue<Integer>(DEFAULT_QUEUE_SIZE);

		File directory = new File(inputDirectory);

		for (File file : directory.listFiles()) {
			if (file.isFile()) {
				task = new InputFileReader(file, q, SPLIT_SIZE);
				readTasksList.add(task);
			}
		}

		return readTasksList;
	}

	public static List<Callable<Void>> startWritingOutputFiles(String outputDirectory, ExecutorService writeFileExecutor, BlockingQueue<Integer>[] q) {
		executingTasks = new HashMap<Callable<Void>, Future<Void>>();

		List<Callable<Void>> writeTasksList = new ArrayList<Callable<Void>>(NO_OF_OUTPUT_FILES);
		Callable<Void> task = null;

		for (int i = 0; i < q.length; i++) {
			task = new OutputFileWriter(outputDirectory + "/output-" + i, q[i]);
			writeTasksList.add(task);
			executingTasks.put(task, writeFileExecutor.submit(task));
		}

		return writeTasksList;
	}

	private static boolean deleteDirectory(File directory) {
		boolean deleted = false;

		if (directory.exists())
			for (File file : directory.listFiles())
				deleted = file.isDirectory() ? deleteDirectory(file) : file.delete();

		return directory.delete() && deleted;
	}

	private static boolean checkAndCreateDirectory(String dirName) {
		File directory = new File(dirName);

		if (directory.isDirectory())
			deleteDirectory(directory);

		return directory.mkdirs();
	}

}