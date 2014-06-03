package com.mwdb.phase3;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.mwdb.util.MyComparator;

public class KNN {
	
	public void findNearestNeighboursAndAssignClass(Map<String,Integer> gestureClassMap,ArrayList<String> gesturesList,String query){
		
		HashMap<Integer,Integer> labelCountMap = new HashMap<Integer,Integer>();
		Map<Integer, Integer> sortedMap = new LinkedHashMap<Integer, Integer>();
		for (int i = 0; i < gesturesList.size(); i++) {
			if(gestureClassMap.containsKey(gesturesList.get(i))){
				int label = gestureClassMap.get(gesturesList.get(i));
				if(labelCountMap.containsKey(label)){
					labelCountMap.put(label, labelCountMap.get(label)+1);
				}
				else{
					labelCountMap.put(label, 1);
				}
			}
		}
			sortedMap = sortByValuesFromMap(labelCountMap);
			Iterator<Integer> itr  = sortedMap.keySet().iterator();
			System.out.println("Class label for the given gesture "+ query+" is: "+itr.next());
		
	}

	// sort it in descending order based on values. The key with highest value is the label to the query.
	// Sort Map by values
			public static LinkedHashMap<Integer, Integer> sortByValuesFromMap(
					Map<Integer, Integer> labelCountMap) {
				// long startTime = System.nanoTime();
				List<Integer> mapKeys = new ArrayList<Integer>(labelCountMap.keySet());
				List<Integer> mapValues = new ArrayList<Integer>(labelCountMap.values());
				Collections.sort(mapValues, new MyComparator());

				Collections.sort(mapKeys);

				LinkedHashMap<Integer, Integer> sortedMap = new LinkedHashMap<Integer, Integer>();

				Iterator<Integer> valueIt = mapValues.iterator();
				while (valueIt.hasNext()) {
					Integer val = (Integer) valueIt.next();
					Iterator<Integer> keyIt = mapKeys.iterator();

					while (keyIt.hasNext()) {
						Integer key = (Integer) keyIt.next();
						Integer val1 = labelCountMap.get(key);
						Integer val2 = val;

						if (val2.equals(val1)) {
							mapKeys.remove(key);
							sortedMap.put((Integer) key, (Integer) val);
							break;
						}

					}

				}
				// long endTime = System.nanoTime();
				// System.out.println("The time taken to sort TF -IDF values is:"+(endTime-startTime)/Math.pow(10,
				// 9));
				return sortedMap;
			}
}
