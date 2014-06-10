package it.poliba.sisinflab.dinoia.sssw2013;

import java.io.*;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Vector;

import com.csvreader.CsvReader;

public class GenerateJaccardSimilarityData {

	private Hashtable<String, Hashtable<String, Vector<String>>> data;
	private Hashtable<String, Hashtable<String, Float>> jaccardSimilarities;

	public GenerateJaccardSimilarityData(File file, int number) {
		File absolutePath = new File(file.getAbsolutePath());
		String name = absolutePath.toString().split("\\.")[0];

		data = new Hashtable<String, Hashtable<String, Vector<String>>>();
		jaccardSimilarities = new Hashtable<String, Hashtable<String, Float>>();

		this.computePropertyJaccardSimilarity(file, name);

	}

	private void computePropertyJaccardSimilarity(File file, String name) {

		CsvReader input;

		Vector<String> properties = new Vector<String>();
		Vector<String> items = new Vector<String>();

		try {

			String property;

			File propertiesFile = new File(name + "_properties.txt");
			BufferedReader propertiesFileReader = new BufferedReader(
					new FileReader(propertiesFile));
			while ((property = propertiesFileReader.readLine()) != null) {
				properties.add(property);
			}
			propertiesFileReader.close();

			input = new CsvReader(file.getAbsolutePath());
			input.readHeaders();

			while (input.readRecord()) {
				String s = input.get(0);
				String p = input.get(1);
				String o = input.get(2);

				if (!(items.contains(s))) {
					items.add(s);
				}

				if (!(data.containsKey(p))) {
					Hashtable<String, Vector<String>> tempHashtable = new Hashtable<String, Vector<String>>();
					Vector<String> tempVector = new Vector<String>();
					tempVector.add(o);
					tempHashtable.put(s, tempVector);
					data.put(p, tempHashtable);
				} else {
					if (!(data.get(p).containsKey(s))) {
						Vector<String> tempVector = new Vector<String>();
						tempVector.add(o);
						data.get(p).put(s, tempVector);
					} else
						data.get(p).get(s).add(o);
				}
			}

			for (String p : properties) {
				Vector<String> itemsClone = new Vector<String>();
				for (String s : items)
					itemsClone.add(s);
				int i = 0;
				jaccardSimilarities.put(p, new Hashtable<String, Float>());
				while (!(itemsClone.isEmpty())) {
					String m1 = itemsClone.firstElement();
					itemsClone.remove(i);
					for (String m2 : itemsClone) {
						Vector<String> om1Vector = data.get(p).get(m1);
						Vector<String> om2Vector = data.get(p).get(m2);
						HashSet<String> tempHashSet = new HashSet<String>();
						float jaccardSimilarity = 0;

						if (om1Vector != null && om2Vector != null) {
							tempHashSet.addAll(om1Vector);
							tempHashSet.addAll(om2Vector);
							int union = tempHashSet.size();

							int intersection = 0;
							for (String om1 : om1Vector) {
								if (om2Vector.contains(om1))
									intersection++;
							}
							jaccardSimilarity = (float) intersection / union;
						}
						Vector<String> tempS = new Vector<String>();
						tempS.add(m1);
						tempS.add(m2);
						Collections.sort(tempS);

						jaccardSimilarities.get(p).put(
								tempS.elementAt(0) + "@#@" + tempS.elementAt(1),
								jaccardSimilarity);

						if (jaccardSimilarity > 0)
							System.out.println(p + " - " + m1 + " - " + m2
									+ " - " + jaccardSimilarity);
					}
				}
				i++;
			}

			// SERIALIZE similarities in a csv file
			File similaritiesFile = new File(name + "_jaccard_similarities.csv");
			FileWriter fw = new FileWriter(similaritiesFile);
			Enumeration<String> pEnumeration = jaccardSimilarities.keys();
			while (pEnumeration.hasMoreElements()) {
				String p = pEnumeration.nextElement();
				Hashtable<String, Float> temp = jaccardSimilarities.get(p);
				Enumeration<String> pairEnumeration = temp.keys();
				while (pairEnumeration.hasMoreElements()) {
					String pair = pairEnumeration.nextElement();
					float weight = temp.get(pair);
					fw.write("\"" + p + "\",\"" + pair + "\",\"" + weight + "\"\n");
				}
			}
			fw.close();
			
			System.out.println("[JACCARD SIMILARITIES GENERATED] " + similaritiesFile);

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
