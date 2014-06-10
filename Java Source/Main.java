package it.poliba.sisinflab.dinoia.sssw2013;

import java.io.*;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Vector;

import com.csvreader.CsvReader;

public class Main {

	/**
	 * @param args
	 *            args[0] is the csv file name args[1] is the number of distinct
	 *            items (movies, books, etc) selected
	 */
	public static void main(String[] args) {

		int switchValue = 0;

		if (args.length > 0) {

			if (args[0].equals("--cosine-generate-data"))
				switchValue = 1;
			if (args[0].equals("--jaccard-generate-data"))
				switchValue = 2;
			if (args[0].equals("--profile-generate"))
				switchValue = 3;
			if (args[0].equals("--cosine-recommend"))
				switchValue = 4;
			if (args[0].equals("--jaccard-recommend"))
				switchValue = 5;
		}
		switch (switchValue) {
		case (1):
			if (args.length < 3) {
				System.out.println();
				System.out.println("-------------------------------");
				System.out.println();
				System.out
						.println("To generate the data needed to compute a recommendation based on cosine similarity you need to use the following sysntax: ");
				System.out
						.println("--cosine-generate-data data-file.cvs number-of-items");
				System.exit(1);
			} else {
				File file = new File(args[1]);
				int number = Integer.parseInt(args[2], 10);
				generatePropertiesFile(file, number);
				generateItemsFile(file, number);
				new GenerateCosineSimilarityData(file, number);
			}
			break;
		case (2):
			if (args.length < 3) {
				System.out.println();
				System.out.println("-------------------------------");
				System.out.println();
				System.out
						.println("To generate the data needed to compute a recommendation based on Jaccard similarity you need to use the following sysntax: ");
				System.out
						.println("--jaccard-generate-data data-file.cvs number-of-items");
				System.exit(1);
			} else {
				File file = new File(args[1]);
				int number = Integer.parseInt(args[2], 10);
				generatePropertiesFile(file, number);
				generateItemsFile(file, number);
				new GenerateJaccardSimilarityData(file, number);
			}
			break;
		case (3):
			if (args.length < 3) {
				System.out.println();
				System.out.println("-------------------------------");
				System.out.println();
				System.out
						.println("To generate a random user profile you need to use the following syntax: ");
				System.out
						.println("--profile-generate data-file.cvs number-of-items-in-the-profile");
				System.exit(1);
			} else {
				File file = new File(args[1]);
				int number = Integer.parseInt(args[2], 10);
				new GenerateProfileData(file, number);
			}
			break;
		case (4):
			if (args.length < 2) {
				System.out.println();
				System.out.println("-------------------------------");
				System.out.println();
				System.out
						.println("To compute a recommendation based on cosine similarity you need to use the following syntax: ");
				System.out
						.println("--cosine-recommend data-file.cvs");
				System.exit(1);
			} else {
				File file = new File(args[1]);
				new Recommend(file, "cosine");
			}
			break;
		case (5):
			if (args.length < 2) {
				System.out.println();
				System.out.println("-------------------------------");
				System.out.println();
				System.out
						.println("To compute a recommendation based on Jaccard similarity you need to use the following syntax: ");
				System.out
						.println("--jaccard-recommend data-file.cvs");
				System.exit(1);
			} else {
				File file = new File(args[1]);
				new Recommend(file, "jaccard");
			}
			break;
		default:
			System.out.println();
			System.out.println("-------------------------------");
			System.out.println();
			System.out.println("A Simple Content Based Recommender System");
			System.out.println();
			System.out.println("-------------------------------");
			System.out.println();
			System.out
					.println("Generate the data needed to compute a recommendation based on cosine similarity: ");
			System.out
					.println("--cosine-generate-data data-file.cvs number-of-items");
			System.out.println();
			System.out
					.println("Generate the data needed to compute a recommendation based on Jaccard similarity: ");
			System.out
					.println("--jccard-generate-data data-file.cvs number-of-items");
			System.out.println();
			System.out.println("Generate a random user profile: ");
			System.out
					.println("--profile-generate data-file.cvs number-of-items");
			System.out.println();
			System.out
					.println("Compute a recommendation based on cosine similarity: ");
			System.out
					.println("--cosine-recommend data-file.cvs user-profile.csv");
			System.out.println();
			System.out
					.println("Compute a recommendation based on Jaccard similarity: ");
			System.out
					.println("--jaccard-recommend data-file.cvs user-profile.csv");
			System.out.println();
			System.exit(1);
		}

	}

	private static void generatePropertiesFile(File file, int number) {

		File absolutePath = new File(file.getAbsolutePath());

		String name = absolutePath.toString().split("\\.")[0];
		Vector<String> v = new Vector<String>();

		try {
			// BufferedReader input = new BufferedReader(new FileReader(data));
			CsvReader input = new CsvReader(file.getAbsolutePath());
			input.readHeaders();
			String key;

			while (input.readRecord()) {
				key = input.get(1);
				if (!(v.contains(key)))
					v.add(key);
			}
			input.close();

			Collections.sort(v);

			Enumeration<String> properties = v.elements();
			File output1 = new File(name + "_properties.txt");
			File output2 = new File(name + "_properties_worth.csv");
			FileWriter fw1 = new FileWriter(output1);
			FileWriter fw2 = new FileWriter(output2);
			while (properties.hasMoreElements()) {
				String element = properties.nextElement();
				fw1.write(element + "\n");
				fw1.flush();
				fw2.write("\"" + element + "\"," + "\"1\" \n");
				fw2.flush();
			}
			fw1.close();
			fw2.close();

		}

		catch (FileNotFoundException fne) {
			System.err.println("Sorry, I cannot find the file " + file
					+ ". It looks like it does not exist.");
		}

		// Gestione delle eccezioni
		catch (IOException ioException) {
			System.out.println("An error occurred while manipulating files.");
		}
	}

	private static void generateItemsFile(File file, int number) {

		
		File absolutePath = new File(file.getAbsolutePath());
		String name = absolutePath.toString().split("\\.")[0];
		
		CsvReader input;

		Vector<String> items = new Vector<String>();

		try {
			
			
			File similaritiesFile = new File(name + "_items.txt");
			FileWriter fw = new FileWriter(similaritiesFile);
			
			input = new CsvReader(file.getAbsolutePath());
			input.readHeaders();

			while (input.readRecord() && items.size() < number) {
				String s = input.get(0);
				if (!(items.contains(s))) {
					items.add(s);
					fw.write(s+"\n");
				}
			}
			input.close();
			fw.close();

			Collections.sort(items);
			
		}

		catch (FileNotFoundException fne) {
			System.err.println("Sorry, I cannot find the file " + file
					+ ". It looks like it does not exist.");
		}

		// Gestione delle eccezioni
		catch (IOException ioException) {
			System.out.println("An error occurred while manipulating files.");
		}
	}

}
