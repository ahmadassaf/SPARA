package it.poliba.sisinflab.dinoia.sssw2013;

import java.io.*;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;

import it.poliba.sisinflab.dinoia.sssw2013.util.Pair;
import it.poliba.sisinflab.dinoia.sssw2013.util.ValueComparator;

import com.csvreader.CsvReader;

public class Recommend {

	public Recommend(File file, String similarity) {
		this.computeRecommendation(file, similarity);
	}

	private void computeRecommendation(File file, String similarity) {

		CsvReader input;

		Hashtable<String, Hashtable<HashSet<String>, Float>> similarities = new Hashtable<String, Hashtable<HashSet<String>, Float>>();
		Vector<Pair<String, Integer>> profile = new Vector<Pair<String, Integer>>();
		Vector<String> itemsToRecommend = new Vector<String>();
		Hashtable<String, Float> propertiesWorth = new Hashtable<String, Float>();

		HashMap<String, Float> recommendation = new HashMap<String, Float>();
		ValueComparator bvc = new ValueComparator(recommendation);
		TreeMap<String, Float> sortedRecommendation = new TreeMap<String, Float>(
				bvc);

		File absolutePath = new File(file.getAbsolutePath());
		String name = absolutePath.toString().split("\\.")[0];
		try {
			input = new CsvReader(name + "_" + similarity + "_similarities.csv");
			while (input.readRecord()) {
				String property = input.get(0);
				String[] pair = input.get(1).split("@#@");
				Float sim = new Float(input.get(2));
				if (!(similarities.containsKey(property))) {
					Hashtable<HashSet<String>, Float> tempHashtable = new Hashtable<HashSet<String>, Float>();
					HashSet<String> tempHashSet = new HashSet<String>();
					tempHashSet.add(pair[0]);
					tempHashSet.add(pair[1]);
					tempHashtable.put(tempHashSet, sim);
					similarities.put(property, tempHashtable);
				} else {
					HashSet<String> tempHashSet = new HashSet<String>();
					tempHashSet.add(pair[0]);
					tempHashSet.add(pair[1]);
					if (!(similarities.get(property).containsKey(tempHashSet))) {
						similarities.get(property).put(tempHashSet, sim);
					}
				}
			}
			input.close();

			String item;
			BufferedReader itemsFileReader = new BufferedReader(new FileReader(
					name + "_items.txt"));
			while ((item = itemsFileReader.readLine()) != null) {
				itemsToRecommend.add(item);
			}
			itemsFileReader.close();

			input = new CsvReader(name + "_profile.csv");
			while (input.readRecord()) {
				profile.add(new Pair<String, Integer>(input.get(0),
						new Integer(input.get(1))));
				itemsToRecommend.remove(input.get(0));
			}
			input.close();
			
			input = new CsvReader(name + "_properties_worth.csv");
			while (input.readRecord()) {
				propertiesWorth.put(input.get(0), new Float(input.get(1)));
			}
			input.close();

			int propertiesNumber = similarities.size();

			for (String movie : itemsToRecommend) {
				float recommendationValue = 0;
				for (Pair<String, Integer> profileMovie : profile) {
					float sumP = 0;
					Enumeration<String> properties = similarities.keys();
					while (properties.hasMoreElements()) {
						String property = properties.nextElement();
						float propertyWorth = propertiesWorth.get(property);
						HashSet<String> tempHashSet1 = new HashSet<String>();
						tempHashSet1.add(movie);
						tempHashSet1.add(profileMovie.getLeft());
						float value = similarities.get(property).get(
					 			tempHashSet1);
						sumP += propertyWorth*value;
//						if(value > 0){
//							System.out.println("EUREKA!");
//							System.out.println(tempHashSet1);
//							System.out.println(property);
//							System.out.println(value);
//							
//						}
						
					}
					int rating = profileMovie.getRight();
					sumP = (rating*sumP)/propertiesNumber;
					recommendationValue += sumP;
				}
				recommendationValue /= profile.size(); 
				recommendation.put(movie, recommendationValue);
			}

			sortedRecommendation.putAll(recommendation);
			
			File recommendationFile = new File(name + "_" +similarity + "_recommendation.csv");
			FileWriter fw = new FileWriter(recommendationFile);
			for (Map.Entry<String, Float> entry : sortedRecommendation.entrySet())
				fw.write("\""+ entry.getKey() + "\",\"" + entry.getValue() + "\"" + "\n");
			fw.close();
			
			System.out.println("[RECOMMENDATION GENERATED]" + recommendationFile);			
			
		} catch (FileNotFoundException e) {
			System.err.println("Can't find " + name + "_" + similarity
					+ "_similarities.csv");
			e.printStackTrace();
		} catch (IOException ioe) {
			System.err.println("I/O Error while manipulating files");
			ioe.printStackTrace();
		}

	}
}
