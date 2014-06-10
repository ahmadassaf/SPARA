package it.poliba.sisinflab.dinoia.sssw2013;

import java.io.*;
import java.util.HashSet;
import java.util.Vector;

import com.csvreader.CsvReader;

public class GenerateProfileData {

	public GenerateProfileData(File file, int number) {
		this.generateProfileFile(file, number);
	}

	private void generateProfileFile(File file, int number) {

		CsvReader input;

		File absolutePath = new File(file.getAbsolutePath());
		String name = absolutePath.toString().split("\\.")[0];

		HashSet<String> items = new HashSet<String>();

		try {
			input = new CsvReader(file.getAbsolutePath());
			input.readHeaders();

			input.readRecord();

			items.add(input.get(0));

			while (input.readRecord()) {
				String item = input.get(0);
				items.add(item);
			}
			Vector<String> itemsVector = new Vector<String>(items);

			File profileFile = new File(name + "_profile.csv");
			FileWriter fw = new FileWriter(profileFile);
			int i = 0;
			do {
				long rand = Math.round(items.size() * Math.random());
				try{
					fw.write("\"" + itemsVector.elementAt((int) rand) + "\",\""
							+ "1" + "\"" + "\n");
					itemsVector.remove((int)rand);
					i ++;
				}
				catch(IndexOutOfBoundsException iobe){}
			} while (i < number);
			System.out.println("[PROFILE GENERATED] " + profileFile);
			fw.close();

		} catch (FileNotFoundException e) {
			System.err.println("I cannot find the file "
					+ file.getAbsolutePath() + "containing the weights.");
			e.printStackTrace();
		} catch (IOException e) {
			System.err.println("I/O problems while reading the file "
					+ file.getAbsolutePath());
			e.printStackTrace();
		}
	}
}
