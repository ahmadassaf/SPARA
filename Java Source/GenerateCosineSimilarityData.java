package it.poliba.sisinflab.dinoia.sssw2013;

import java.io.*;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import com.csvreader.CsvReader;

public class GenerateCosineSimilarityData {
	
	private Hashtable<String, Vector<Float>> itemWeightsTable;
	private Hashtable<String, Hashtable<String, Float>> cosineSimilarities;

	public GenerateCosineSimilarityData(File file, int number) {
		this.computeIDF(file, number);

		File absolutePath = new File(file.getAbsolutePath());
		String name = absolutePath.toString().split("\\.")[0];

		File idfFile = new File(name + "_tf-idf.csv");
		this.generateVectorsFile(idfFile, file);

		File weightFile = new File(name + "_weights.csv");
		this.computePropertyCosineSimilarity(weightFile, name);

	}

	private void computeIDF(File file, int number) {
		Hashtable<String, Integer> table = new Hashtable<String, Integer>();

		File absolutePath = new File(file.getAbsolutePath());

		String name = absolutePath.toString().split("\\.")[0];

		try {
			CsvReader input = new CsvReader(file.getAbsolutePath());
			input.readHeaders();
			String key;

			while (input.readRecord()) {
				key = input.get(1) + "@#@" + input.get(2);
				if (table.containsKey(key)) {
					Integer count = (Integer) table.get(key);
					table.put(key, new Integer(count.intValue() + 1));
				} else
					table.put(key, new Integer(1));
			}
			input.close();

			Enumeration<String> propertyObjectPair = table.keys();
			List<String> l = Collections.list(propertyObjectPair);
			Collections.sort(l);

			File output = new File(name + "_tf-idf.csv");
			FileWriter fw = new FileWriter(output);
			Iterator<String> i = l.iterator();
			while (i.hasNext()) {
				key = i.next();
				double idf = Math.log10((double) number / table.get(key));
				fw.write("\"" + key + "\"" + "," + "\"" + idf + "\"" + "\n");
				fw.flush();
			}
			fw.close();
		}

		catch (FileNotFoundException fne) {
			System.err.println("Sorry, I cannot find the file " + file
					+ ". It looks like it does not exist.");
			fne.printStackTrace();
		}

		// Gestione delle eccezioni
		catch (IOException ioException) {
			System.err.println("An error occurred while manipulating files.");
			ioException.printStackTrace();
		}

	}

	private void generateVectorsFile(File idffile, File file) {

		CsvReader input;

		Hashtable<String, Vector<String>> itemsTable = new Hashtable<String, Vector<String>>();
		Hashtable<String, Double> idfTable = new Hashtable<String, Double>();
		Vector<String> keysVector = new Vector<String>();
		Vector<String> itemsVector = new Vector<String>();
		itemWeightsTable = new Hashtable<String, Vector<Float>>();

		File absolutePath = new File(file.getAbsolutePath());

		String name = absolutePath.toString().split("\\.")[0];

		try {
			input = new CsvReader(file.getAbsolutePath());
			input.readHeaders();
			String key;

			while (input.readRecord()) {
				key = input.get(0);
				if (!(itemsTable.containsKey(key))) {
					Vector<String> v = new Vector<String>();
					v.add(input.get(1) + "@#@" + input.get(2));
					itemsTable.put(key, v);
					itemsVector.add(key);
					itemWeightsTable.put(key, new Vector<Float>());
				} else {
					Vector<String> v = itemsTable.get(key);
					v.add(input.get(1) + "@#@" + input.get(2));
					itemsTable.put(key, v);
				}
			}
			input.close();

			this.printItemsTableToFile(itemsTable);

			input = new CsvReader(idffile.getAbsolutePath());

			while (input.readRecord()) {
				key = input.get(0);
				if (!(idfTable.containsKey(key))) {
					idfTable.put(key, new Double(input.get(1)));
					keysVector.add(key);
				}
			}
			input.close();

			Collections.sort(keysVector);

			System.out.println("Number of items: " + itemsVector.size());
			System.out.println("Number of values: " + keysVector.size());

			String header = "\"item name\"";

			Enumeration<String> keysVectorEnumeration = keysVector.elements();
			while (keysVectorEnumeration.hasMoreElements()) {
				String po = keysVectorEnumeration.nextElement();
				header += ",\"" + po + "\"";
				Enumeration<String> itemsVectorElements = itemsVector
						.elements();
				while (itemsVectorElements.hasMoreElements()) {
					String item = itemsVectorElements.nextElement();
					if (itemsTable.get(item).contains(po)) {
						itemWeightsTable.get(item).add(
								idfTable.get(po).floatValue());
					} else {
						itemWeightsTable.get(item).add(
								(new Float(0)).floatValue());
					}
				}
			}
			header += "\n";

			// this.printItemWeights(itemWeightsTable);

			File output = new File(name + "_weights.csv");
			FileWriter fw = new FileWriter(output);

			fw.write(header);

			Enumeration<String> itemEnumeration = itemWeightsTable.keys();
			while (itemEnumeration.hasMoreElements()) {
				String item = itemEnumeration.nextElement();
				String temp = "";
				temp += "\"" + item + "\"";
				Enumeration<Float> values = itemWeightsTable.get(item)
						.elements();
				while (values.hasMoreElements()) {
					temp += ",\"" + values.nextElement() + "\"";
				}
				fw.write(temp + "\n");
			}
			fw.close();

		}

		catch (FileNotFoundException fne) {
			System.err.println("Sorry, I cannot find the file " + file
					+ ". It looks like it does not exist.");
		}

		// Gestione delle eccezioni
		catch (IOException ioException) {
			System.err.println("An error occurred while manipulating files.");
			ioException.printStackTrace();
		}
	}

	private void computePropertyCosineSimilarity(File weightFile, String name) {
		CsvReader input;

		Hashtable<String, Vector<Float>> itemWeightsTable = new Hashtable<String, Vector<Float>>();
		Vector<String> keys = new Vector<String>();
		Vector<String> properties = new Vector<String>();
		Enumeration<String> itemsEnumeration;
		Vector<String> items = new Vector<String>();
		cosineSimilarities = new Hashtable<String, Hashtable<String, Float>>();

		try {

			String property;

			File propertiesFile = new File(name + "_properties.txt");

			input = new CsvReader(weightFile.getAbsolutePath());
			input.readRecord();
			int numberOfValues = input.getColumnCount();

			for (int i = 1; i < numberOfValues; i++)
				keys.add(input.get(i));
			Collections.sort(keys);

			BufferedReader propertiesFileReader = new BufferedReader(
					new FileReader(propertiesFile));
			while ((property = propertiesFileReader.readLine()) != null) {
				properties.add(property);
			}
			propertiesFileReader.close();
			Collections.sort(properties);

			while (input.readRecord()) {
				String item = input.get(0);
				Vector<Float> values = new Vector<Float>();
				for (int i = 1; i < numberOfValues; i++) {
					values.add(new Float(input.get(i)));
				}
				itemWeightsTable.put(item, values);
			}
			input.close();

			// ////////////////////

			// printItemWeights(itemWeightsTable);

			// ////////////////////

			itemsEnumeration = itemWeightsTable.keys();
			while (itemsEnumeration.hasMoreElements()) {
				items.add(itemsEnumeration.nextElement());
			}

			for (String p : properties) {
				Vector<String> itemsClone = new Vector<String>();
				for (String s : items)
					itemsClone.add(s);
				int i = 0;
				cosineSimilarities.put(p, new Hashtable<String, Float>());
				while (!(itemsClone.isEmpty())) {
					String m1 = itemsClone.firstElement();
					itemsClone.remove(i);
					Vector<Float> m1Weights = itemWeightsTable.get(m1);
					for (String m2 : itemsClone) {
						int j = 0;
						Enumeration<String> keysEnumerator = keys.elements();
						Vector<Float> m2Weights = itemWeightsTable.get(m2);
						float numerator = 0;
						float denominatorM1 = 0;
						float denominatorM2 = 0;
						while (keysEnumerator.hasMoreElements()) {
							String element = keysEnumerator.nextElement();
							if (element.contains(p)) {
								numerator += m1Weights.elementAt(j)
										* m2Weights.elementAt(j);
								denominatorM1 += Math.pow(
										m1Weights.elementAt(j), 2);
								denominatorM2 += Math.pow(
										m2Weights.elementAt(j), 2);

								if (m1Weights.elementAt(j) > 0
										&& m2Weights.elementAt(j) > 0)
									System.out.println(p + " - " + m1 + " - "
											+ m2 + " - " + keys.elementAt(j));
							}
							j++;
						}
						float weight = 0;
						if (numerator != 0) {
							weight = numerator
									/ (denominatorM1 * denominatorM2);
							// System.out.println(p + " - " + m1 + " - " + m2 +
							// " - " + weight);
						}
						Vector<String> tempKeys = new Vector<String>();
						tempKeys.add(m1);
						tempKeys.add(m2);
						Collections.sort(tempKeys);
						Hashtable<String, Float> temp = cosineSimilarities.get(p);
						temp.put(tempKeys.elementAt(0) +"@#@"+ tempKeys.elementAt(1),
								weight);
						cosineSimilarities.put(p, temp);
					}
				}
				i++;
			}
			
			// SERIALIZE similarities in a csv file
			File similaritiesFile = new File(name + "_cosine_similarities.csv");
			FileWriter fw = new FileWriter(similaritiesFile);
			Enumeration<String> pEnumeration = cosineSimilarities.keys();
			while(pEnumeration.hasMoreElements()){
				String p = pEnumeration.nextElement();
				Hashtable<String, Float> temp = cosineSimilarities.get(p);
				Enumeration<String> pairEnumeration = temp.keys();
				while(pairEnumeration.hasMoreElements()){
					String pair = pairEnumeration.nextElement();
					float weight = temp.get(pair);
					fw.write("\"" + p + "\",\"" + pair + "\",\"" + weight + "\"\n");
				}
			}
			fw.close();
			
			System.out.println("[COSINE SIMILARITIES GENERATED] " + similaritiesFile);
			

		} catch (FileNotFoundException e) {
			System.err.println("I cannot find the file "
					+ weightFile.getAbsolutePath() + "containing the weights.");
			e.printStackTrace();
		} catch (IOException e) {
			System.err.println("I/O problems while reading the file "
					+ weightFile.getAbsolutePath());
			e.printStackTrace();
		}
	}

	private void printItemsTableToFile(
			Hashtable<String, Vector<String>> itemsTable) {
		Enumeration<String> items = itemsTable.keys();
		String key;
		File output = new File("ItemsTable.txt");
		try {
			FileWriter fw = new FileWriter(output);
			while (items.hasMoreElements()) {
				key = items.nextElement();
				fw.write("--- ---" + key + "--- ---" + "\n");
				Enumeration<String> values = itemsTable.get(key).elements();
				while (values.hasMoreElements()) {
					fw.write(values.nextElement() + "\n");
				}
			}
			fw.close();
		} catch (IOException e) {
			System.err
					.println("I encountered some prolbems whie manipulating the file ItemsTable.txt");
			e.printStackTrace();
		}
	}

//	private void printItemWeights(
//			Hashtable<String, Vector<Float>> itemWeightsTable) {
//		Enumeration<String> items = itemWeightsTable.keys();
//		String key;
//		while (items.hasMoreElements()) {
//			key = items.nextElement();
//			System.out.println("\n " + "--- ---" + key + "--- ---" + "\n");
//			Enumeration<Float> values = itemWeightsTable.get(key).elements();
//			while (values.hasMoreElements()) {
//				System.out.print(values.nextElement() + ", ");
//			}
//			System.out.println();
//		}
//	}

}
