package main;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NumberGenerator {

	public static void generateFiles(String dirName, int noOfFiles, int lengthOfFile, int maxIntValue, int noOfThreads) {
		ExecutorService executor = Executors.newFixedThreadPool(noOfThreads);
		List<Callable<Void>> tasksList = new ArrayList<Callable<Void>>(noOfFiles);

		for (int i = 0; i < noOfFiles; i++)
			tasksList.add(new InputFileWriter(new File(dirName + "/file-" + i), lengthOfFile, maxIntValue));

		try {
			executor.invokeAll(tasksList);
			executor.shutdownNow();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}