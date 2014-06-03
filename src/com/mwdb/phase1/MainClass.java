package com.mwdb.phase1;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import matlabcontrol.MatlabInvocationException;
import matlabcontrol.MatlabProxy;

import com.mwdb.phase2.AllComponents;
import com.mwdb.phase2.LatentDirichletAllocater;
import com.mwdb.phase2.SVD;
import com.mwdb.phase3.DecisionTreeCalculator;
import com.mwdb.phase3.KNN;
import com.mwdb.phase3.LSH;
import com.mwdb.phase3.Node;
import com.mwdb.phase3.SupportVectorMachines;
import com.mwdb.util.PrecomputeUtil;

public class MainClass {

	public static PhaseOne phaseOne = new PhaseOne();
	public static Scanner sc = new Scanner(System.in);
	public static int WINDOW_LENGTH;
	public static int SHIFT_LENGTH;
	public static double NUMBER_OF_OBJECTS;
	// final output map containing the top three latent semantics for each
	// sensor
	public static Map<Integer, Map<Integer, Map<String, Double>>> finalSVDMap = new HashMap<Integer, Map<Integer, Map<String, Double>>>();

	public static Map<Integer, Map<Integer, Map<String, Double>>> finalPCAMap = new HashMap<Integer, Map<Integer, Map<String, Double>>>();

	public static Map<String, Map<Integer, Map<Integer, Double>>> precomputedTopicMap = new HashMap<String, Map<Integer, Map<Integer, Double>>>();
	public static Map<String, Map<Integer, Map<Integer, Double>>> precomputedPCATopicMap = new HashMap<String, Map<Integer, Map<Integer, Double>>>();

	public static Map<String, Map<Integer, Map<Integer, Double>>> precomputedLDATopicMap;
	public static Map<String, Map<Integer, Map<Integer, Double>>> precomputedTopicQueryMap;
	public static Map<String, Map<Integer, Map<String, Word>>> queryMap = new HashMap<String, Map<Integer, Map<String, Word>>>();
	public static Map<String, Double> queryVocabulary = new HashMap<String, Double>();
	public static Map<String, Double> queryVocabulary2 = new HashMap<String, Double>();
	public static Map<String, Double> queryVocabulary1 = new HashMap<String, Double>();
	// pre computed document map for document set
	public static Map<String, Map<Integer, Map<String, Word>>> precomputedMap = new HashMap<String, Map<Integer, Map<String, Word>>>();
	public static Map<String, Double> vocabulary = new HashMap<String, Double>();
	public static Map<String, Double> dfMap = new HashMap<String, Double>();
	public static int r;
	public static Map<String, Map<String, Double>> vocabulary2 = new HashMap<String, Map<String, Double>>();
	static PrecomputeUtil pUtil = new PrecomputeUtil();
	static LatentDirichletAllocater allocater;
	static PrecomputeUtil putilQuery;
	private static List<AllComponents> components = new ArrayList<AllComponents>();
	public static Map<Integer, String> labelMap = new HashMap<Integer, String>();
	
	public static Map<Integer, Integer> labelHelperMap = new HashMap<Integer, Integer>();
	public static Map<Integer,String> indexGestureMap = new HashMap<Integer, String>();
	private static double[][] featureMatrix;
	public static Map<Integer, String> featureNameMap;
	public static Map<Integer, Integer> testMap = new HashMap<>();;
	private static class ValueComparatorIDF implements Comparator<Double> {

		@Override
		public int compare(Double d1, Double d2) {
			return d2.compareTo(d1);
		}
	}

	// Sort Map by values
	public static LinkedHashMap<String, Double> sortByValuesFromMap(
			Map<String, Double> docMaps) {
		// long startTime = System.nanoTime();
		List<String> mapKeys = new ArrayList<String>(docMaps.keySet());
		List<Double> mapValues = new ArrayList<Double>(docMaps.values());
		Collections.sort(mapValues, new ValueComparatorIDF());

		Collections.sort(mapKeys);

		LinkedHashMap<String, Double> sortedMap = new LinkedHashMap<String, Double>();

		Iterator<Double> valueIt = mapValues.iterator();
		while (valueIt.hasNext()) {
			Double val = (Double) valueIt.next();
			Iterator<String> keyIt = mapKeys.iterator();

			while (keyIt.hasNext()) {
				String key = (String) keyIt.next();
				Double coef1 = docMaps.get(key);
				Double coef2 = val;

				if (coef2.equals(coef1)) {
					mapKeys.remove(key);
					sortedMap.put((String) key, (Double) val);
					break;
				}

			}

		}
		// long endTime = System.nanoTime();
		// System.out.println("The time taken to sort TF -IDF values is:"+(endTime-startTime)/Math.pow(10,
		// 9));
		return sortedMap;
	}
	
	
	/**
	 * @param args
	 * @throws MatlabInvocationException
	 * @throws IOException
	 */
	public static void main(String[] args) throws MatlabInvocationException,
			IOException {
		System.out.println("MWDB Project");
		// String gestureFileName;
		System.out.println("Enter the folder name:");
		String folderName = sc.next();

		System.out.println("Enter the Window Length:");
		WINDOW_LENGTH = sc.nextInt();

		System.out.println("Enter the Shift Length:");
		SHIFT_LENGTH = sc.nextInt();
		
		Map<String, Double[][]> normalizedMap = phaseOne
				.getNormalizedValues(folderName);
		NUMBER_OF_OBJECTS = normalizedMap.size() * 20;
		// System.out.println("Number of docs:"+NUMBER_OF_OBJECTS);
		System.out.println("Enter the value of r");
		r = sc.nextInt();
		sc.nextLine();
		/*phaseOne.setBandMap(phaseOne.getGaussianBands(r));
		precomputeandCreateIndexMap(normalizedMap);
		*/
		double[][] inputVectors = getGestureFeatureMatrixForDecisionTree();

		indexGestureMap = getIndexGestureNames();
		
		System.out.println("Enter project-1 or project-2 or project 3");
		int ch = sc.nextInt();
		sc.nextLine();
		if (ch == 1)
			project1Module(folderName);
		else if (ch == 2) {
			pUtil.precomputeSVDMap(precomputedMap, normalizedMap.size());
			SVD svdDocSet = new SVD(pUtil);
			Map<Integer, Double[][]> svdMap = svdDocSet.getMatrixForSVD();
			allocater = new LatentDirichletAllocater(pUtil);
			oneAMenu(svdMap, svdDocSet);
			precomputedTopicMap = createPrecomputedSVDTopicsMap(finalSVDMap,
					pUtil);
			while (true) {
				System.out.println("Enter the task choice");
				System.out.println("Task Menu");
				System.out.println("1. 1 b");
				System.out.println("2. 1 c");
				System.out.println("3. 2 and 3");
				System.out.println("4. Exit");
				ch = sc.nextInt();
				if (ch == 1)
					oneBMenu();
				else if (ch == 2)
					oneCMenu();
				else if (ch == 3)
					twoBCMenu();
				else
					break;
			}
		} else if(ch ==3) {
			
			// menu for task 3
			while (true) {
				System.out.println("Enter the task choice");
				System.out.println("Task Menu");
				System.out.println("1. Task 2");
				System.out.println("2. Task 3");
				System.out.println("3. Task 5");
				System.out.println("4. Task 6");
				System.out.println("5. Exit");
				ch = sc.nextInt();
				sc.nextLine();
				if (ch == 1)
					Phase3task2menu(inputVectors);
				else if (ch == 2)
					Phase3task3menu(inputVectors);
				else if (ch == 3)
					Phase3task5menu();
				else if(ch == 4)
					Phase3task6menu();
				else
					break;
			}

		}
		System.out.println("Thank you");
	}

	private static void Phase3task6menu() {
		System.out.println("Phase 3---Task6 UI");
	}

	private static void Phase3task5menu() {
		System.out.println("Phase 3---Task 5");
	}

	private static void Phase3task3menu(double[][] inputVectors) throws MatlabInvocationException {
		System.out.println("Phase 3---Task 3");
		
		while(true)
		{
			System.out.println("1. K-nearest neighbors \t 2. Decision tree classification \t 3. n-ary SVM classification \t4. Exit");
	        System.out.println("Enter the choice:");
	        int c = sc.nextInt();
	        sc.nextLine();
	        
	        if(c==1)
	        {
	        	System.out.println("K-nearest neighbours \n");
	        	
	        	System.out.println("Enter query name:");
	    		String query = sc.nextLine();
	    		
	    		System.out.println("Enter the value of k:");
	    		int k = Integer.parseInt(sc.nextLine());
	    		Map<String,Double> sortedMap =  callLsh(inputVectors, k, query);
	    		Iterator<String> itr = sortedMap.keySet().iterator();
	    		int cnt = 0;
	    		ArrayList<String> gesturesList = new ArrayList<String>();
	    		//System.out.println("Top nearest neighbours:");
	    		while(itr.hasNext()){
	    			if(cnt < k){
	    				gesturesList.add(itr.next());
	    			}
	    			else{
	    				break;
	    			}
	    			cnt++;
	    		}
	    		KNN knn = new KNN();
	    		Map<String,Integer> gestureClassMap = getGestureNamesIndexMap();
	    		knn.findNearestNeighboursAndAssignClass(gestureClassMap, gesturesList);
	        	
	        }
	        else if(c==2)
	        	
	        {
	        	double[][] testmatrix = getTestGestureFeatureMatrix();
	        	System.out.println("Decision Tree classification");
	    		Node root = new Node();
				DecisionTreeCalculator dCalculator = new DecisionTreeCalculator(labelMap, labelHelperMap);
				Map<Integer, List<Double>> featuresMap = dCalculator.generateFeatureListFromInputMatrix(inputVectors);
				root = dCalculator.generateAttributeValuesForRoot(featuresMap);
				
				featuresMap.remove(root.getIndex());
				root = dCalculator.buildDecisionTree(root, featuresMap);
				dCalculator.traverseTree(root, gestureFeatures);
				dCalculator.printLabels();
				getIrrelevantLabelsOfGestures(dCalculator);
	        }
	        else if(c==3)
	        {
	        	System.out.println("SVM based n-ary classification \n");
	        	try {
					SupportVectorMachines supportVectorMachines = new SupportVectorMachines(getGestureFeatureMatrixForDecisionTree(), phaseOne.getProxy(), labelHelperMap);
					supportVectorMachines.createModel();
				} catch (MatlabInvocationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	        }
	        else
	        {
	        	break;
	        }
		}
	}
	public static Map<Integer,String> getIndexGestureNames(){
		Map<Integer,String> indexGestureNames = new HashMap<Integer,String>();
		String fileName = "Label.csv";
		BufferedReader br = null;
		String line = "";
		//String[] gestureNames = new String;
		int i =0;
		try {
			 
			br = new BufferedReader(new FileReader(fileName));
			while ((line = br.readLine()) != null) {
				//String
			        // use comma as separator
			String[] split = line.split(",");
			labelMap.put(Integer.parseInt(split[0]), split[1]);
			 indexGestureNames.put(i++, line.split(",")[0]+".csv");
	 
			}
		
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	 
		//System.out.println("Done");
	  return indexGestureNames;
		
		
		
	}
	public static Map<String,Integer> getGestureNamesIndexMap(){
		Map<String,Integer> gestureNamesIndexMap = new HashMap<String,Integer>();
		String fileName = "Label.csv";
		BufferedReader br = null;
		String line = "";
		//String[] gestureNames = new String;
		int i =0;
		try {
			 
			br = new BufferedReader(new FileReader(fileName));
			while ((line = br.readLine()) != null) {
				//String
				gestureNamesIndexMap.put(line.split(",")[0]+".csv",Integer.parseInt(line.split(",")[1]));
	 
			}
		
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	 
		//System.out.println("Done");
	  return gestureNamesIndexMap;
		
		
		
	}
	private static void Phase3task2menu(double[][] inputVectors) throws MatlabInvocationException {
		// TODO Auto-generated method stub
		System.out.println("Phase 3---Task 2");
		getKNNOutput(inputVectors);
	}


	private static void getKNNOutput(double[][] inputVectors)
			throws MatlabInvocationException {
		System.out.println("Enter query name:");
		String query = sc.nextLine();
		
		System.out.println("Enter the value of t nearest neighbors:");
		int t = Integer.parseInt(sc.nextLine());
		Map<String, Double> simCoefMap = callLsh(inputVectors, t, query);
		Iterator<String> itr = simCoefMap.keySet().iterator();
		int cnt = 0;
		System.out.println("Top nearest neighbours:");
		while(itr.hasNext()){
			if(cnt < t){
				System.out.println(itr.next());
			}
			else{
				break;
			}
			cnt++;
		}
	}
	
	public static Map<String, Double> callLsh(double[][] inputVectors,int t, String query) throws MatlabInvocationException{
		LSH lsh = new LSH();
		lsh.calculateBucketRange();
		lsh.calculateHashFunctions(inputVectors,indexGestureMap);
		//lsh.calculateHashFunctionsForQuery(queryArr)
		
		String folder = "X,Y,Z,W";
		String[] folders = folder.split(",");
		int i = 0;
		List<String> queryList = new ArrayList<String>();
		for (AllComponents component : components) {
			PrecomputeUtil pUtil = component.getPrecomputeUtil();
			String gesture = folders[i] + "\\" + query;
			Map<String, Double[][]> normalizedMap = phaseOne
					.getNormalizedValues(gesture);
			component.getQueryComputedMap(normalizedMap);
			pUtil.precomputeQuerySVDMap(component.queryMap, component.queryMap.size());
			createPrecomputedSVDTopicsForQueryMap(component.finalSVDMap, pUtil);
			component.precomputedTopicQueryMap.putAll(precomputedTopicQueryMap);
			precomputedTopicQueryMap.clear();
			queryList.add(gesture);
			i++;
		}

		int number = Integer.parseInt(query.substring(0, query.indexOf('.')));
		int index = number -1;
		double[] queryArr = null;
		if(index < inputVectors.length){
			queryArr = inputVectors[index];
		}
		ArrayList<String> queryIndexList  = lsh.calculateHashFunctionsForQuery(queryArr);
		Set<String> gesturesSet = lsh.findNearestGestures(queryIndexList);
		ArrayList<String> gestures = new ArrayList<>();
		
		Iterator<String> it  = gesturesSet.iterator();
		while(it.hasNext()){
			String gesture = it.next();
			if(gesture!=null){
			gestures.add(gesture);
			}
		}
		System.out.println("Similar gestures before finding simnilarity");
		for (int j = 0; j < gestures.size(); j++) {
			System.out.println(gestures.get(j));
		}
		//List<String> gestures  = new ArrayList<String>();
		Map<String, Double> simCoefMap = calculateAllComponentSimilarity(components, gestures, queryList);
		simCoefMap = sortByValuesFromMap(simCoefMap);
		return simCoefMap;
	}

	public static Map<String, Double> calculateAllComponentSimilarity(
			List<AllComponents> components, List<String> gestures, List<String> queryList) {
		Map<String, Double> simCoefMap = new HashMap<String, Double>();
		int k = 0;
		for (AllComponents component : components) {
			
			
			/*for(Map.Entry<String, Map<Integer, Map<Integer, Double>>> entry : component.precomputedTopicMap.entrySet()){
				if(!gestures.contains(entry.getKey())){
					component.precomputedTopicMap.remove(entry.getKey());
				}
				
			}*/
		Map<String, Map<Integer, Map<Integer, Double>>> precomputedDocMap = new HashMap<String, Map<Integer,Map<Integer,Double>>>();
		Map<String, Map<Integer, Map<Integer, Double>>> precomputedMap = component.precomputedTopicMap;
		for(String key: precomputedMap.keySet()){
			if(gestures.contains(key)){
				precomputedDocMap.put(key, precomputedMap.get(key));
			}
		}
		Map<String, Double> simCoefTFIDF2Map = calculateSimilarityTopics(
							precomputedDocMap,
							component.precomputedTopicQueryMap, queryList.get(k));
					// simCoefTFIDF2Map = sortByValuesFromMap(simCoefTFIDF2Map);
			for(String gestString : simCoefTFIDF2Map.keySet()){
				if(!simCoefMap.containsKey(gestString)){
					simCoefMap.put(gestString, simCoefTFIDF2Map.get(gestString));
				}
				else{
					double v = simCoefMap.get(gestString);
					v+=simCoefTFIDF2Map.get(gestString);
					simCoefMap.put(gestString, v);
				}
			}
			k++;
		}
		for(String str : simCoefMap.keySet()){
			double v = simCoefMap.get(str)/4;
			simCoefMap.put(str, v);
		}
		return simCoefMap;
	}

	/*public static List<AllComponents> createPrecomputedComponent()
			throws MatlabInvocationException {
		int size = 0;
		String folder = "X,Y,Z,W";
		String[] folders = folder.split(",");
		for (int i = 0; i < folders.length; i++) {
			Map<String, Double[][]> normalizedMap = phaseOne
					.getNormalizedValues(folders[i]);
			size = normalizedMap.size();
			NUMBER_OF_OBJECTS = normalizedMap.size() * 20;
			phaseOne.setBandMap(phaseOne.getGaussianBands(r));
			AllComponents component = new AllComponents(phaseOne,
					WINDOW_LENGTH, SHIFT_LENGTH, NUMBER_OF_OBJECTS, r);
			component.precomputeandCreateIndexMap(normalizedMap);
			components.add(component);
		}
		//int topicCol = 0;
		for (AllComponents component : components) {
			PrecomputeUtil util = new PrecomputeUtil();
			util.precomputeSVDMap(component.getPrecomputedMap(), size);
			component.setPrecomputeUtil(util);
			SVD svdDocSet = new SVD(util);
			Map<Integer, Double[][]> svdMap = svdDocSet.getMatrixForSVD();
			Map<Integer, Map<Integer, Map<String, Double>>> finalSVDMap = new HashMap<Integer, Map<Integer, Map<String, Double>>>();
			for (Integer sensor : svdMap.keySet()) {
				Double[][] sensorMatrix = svdMap.get(sensor);
				double[][] sensorTempMatrix = new double[sensorMatrix.length][sensorMatrix[0].length];
				for (int i = 0; i < sensorMatrix.length; i++) {
					for (int j = 0; j < sensorMatrix[0].length; j++) {
						sensorTempMatrix[i][j] = sensorMatrix[i][j];
					}
				}
				finalSVDMap.putAll(svdDocSet.getLatentSemanticsFromSVD(
						sensorTempMatrix, sensor));
			}
			Map<String, Map<Integer, Map<Integer, Double>>> precomputedTopicMap;
			precomputedTopicMap = createPrecomputedSVDTopicsMap(finalSVDMap,
					util);
			component.precomputedTopicMap = precomputedTopicMap;
			component.finalSVDMap = finalSVDMap;
		}
		return components;
	}*/

	public static double[][] getTestGestureFeatureMatrix(){
		double testData[][] = new double[featureMatrix.length - labelMap.size()][];
		int testCount = 0;
		for(int i=0; i<(featureMatrix.length);i++){
			if(!labelMap.containsKey(labelHelperMap.get(i))){
				testData[testCount] = featureMatrix[i];
				testMap.put(testCount, labelHelperMap.get(i));
				testCount++;
			}
		}
		
		return testData;
	}
	public static void getIrrelevantLabelsOfGestures(DecisionTreeCalculator dCalculator) throws MatlabInvocationException{
		System.out.println("Enter irrelevant gestures");
		String irr = sc.nextLine();
		String[] gestureArray = irr.split(",");
		List<Integer> gestures = new ArrayList<Integer>();
		for (int i = 0; i < gestureArray.length; i++) {
			gestures.add(Integer.parseInt(gestureArray[i]));
		}
		Map<Integer, Integer> gestureIndexMap = dCalculator.getFinalClassified();
		Set<Integer> indexes = new HashSet<Integer>();
		for(Integer gesture : gestureIndexMap.keySet()){
			if(gestures.contains(gesture))
			{
				indexes.add(gestureIndexMap.get(gesture));
			}
		}
		double[][] matrix = new double[featureMatrix.length][240];
		int k = 0, l = 0;
		int flag = 0;
		for (int i = 0; i < featureMatrix.length; i++) {
			l = 0;
			for (int j = 0; j < featureMatrix[0].length; j++) {
				if(indexes.contains(i)){
					flag = 1;
					break;
				}
				else{
					matrix[k][l] = featureMatrix[i][j];
					l++;
				}
			}
			if(flag == 0){
				k++;	
			}
			else
				flag =0;
		}
		getKNNOutput(matrix);
	}
	public static double[][] getGestureFeatureMatrixForDecisionTree()
			throws MatlabInvocationException {
		if(featureMatrix!=null){
			return featureMatrix;
		}
		int size = 0;
		String folder = "X,Y,Z,W";
		String[] folders = folder.split(",");
		for (int i = 0; i < folders.length; i++) {
			Map<String, Double[][]> normalizedMap = phaseOne
					.getNormalizedValues(folders[i]);
			size = normalizedMap.size();
			NUMBER_OF_OBJECTS = normalizedMap.size() * 20;
			phaseOne.setBandMap(phaseOne.getGaussianBands(r));
			AllComponents component = new AllComponents(phaseOne,
					WINDOW_LENGTH, SHIFT_LENGTH, NUMBER_OF_OBJECTS, r);
			component.precomputeandCreateIndexMap(normalizedMap);
			components.add(component);
		}
		int topicCol = 0;
		featureMatrix= new double[size][240];
		
		for (AllComponents component : components) {
			PrecomputeUtil util = new PrecomputeUtil();
			util.precomputeSVDMap(component.getPrecomputedMap(), size);
			component.setPrecomputeUtil(util);
			SVD svdDocSet = new SVD(util);
			Map<Integer, Double[][]> svdMap = svdDocSet.getMatrixForSVD();
			Map<Integer, Map<Integer, Map<String, Double>>> finalSVDMap = new HashMap<Integer, Map<Integer, Map<String, Double>>>();
			for (Integer sensor : svdMap.keySet()) {
				Double[][] sensorMatrix = svdMap.get(sensor);
				double[][] sensorTempMatrix = new double[sensorMatrix.length][sensorMatrix[0].length];
				for (int i = 0; i < sensorMatrix.length; i++) {
					for (int j = 0; j < sensorMatrix[0].length; j++) {
						sensorTempMatrix[i][j] = sensorMatrix[i][j];
					}
				}
				double[][] outputFromCoVariance = svdDocSet
						.calculateCoVariance(sensorTempMatrix);
				finalSVDMap.putAll(svdDocSet.getLatentSemanticsFromPCA(
						outputFromCoVariance, sensor));
			}
			Map<String, Map<Integer, Map<Integer, Double>>> precomputedTopicMap;
			precomputedTopicMap = createPrecomputedSVDTopicsMap(finalSVDMap,
					util);
			component.precomputedTopicMap = precomputedTopicMap;
			component.finalSVDMap = finalSVDMap;
			int gestureCount = 0;
			featureNameMap = new HashMap<>();
			int gestureRow = 0;
			for (String gesture : precomputedTopicMap.keySet()) {
				featureNameMap.put(gestureCount, gesture);
				gestureCount++;
				topicCol = 0;
				int gestNum = Integer.parseInt(gesture.substring(0, gesture.indexOf('.')));
				labelHelperMap.put(gestureRow, gestNum);
				for (Integer sensor : precomputedTopicMap.get(gesture).keySet()) {
					for (Integer topic : precomputedTopicMap.get(gesture)
							.get(sensor).keySet()) {
						double value = precomputedTopicMap.get(gesture)
								.get(sensor).get(topic);
						featureMatrix[gestureRow][topicCol] = value;
						topicCol++;
					}
				}
				gestureRow++;
			}
		}
		return featureMatrix;
	}

	public static double[][] getMatrixForSVDFromTopLatentSemantics(int choice) {
		int i = 0, j = 0;
		double[][] gestureSimMatrix = new double[precomputedTopicMap.size()][precomputedTopicMap
				.size()];
		int componentCount = 0;
		for (AllComponents component : components) {
			Map<String, Map<String, Double>> gestureSimMap = new HashMap<String, Map<String, Double>>();
			i = 0;
			j = 0;
			Map<String, Map<Integer, Map<Integer, Double>>> tempMap = null;
			if (choice == 1) {
				tempMap = component.precomputedTopicMap;
			} else if (choice == 2) {
				tempMap = component.precomputedPCATopicMap;
			}
			if (choice == 3) {
				tempMap = component.precomputedLDATopicMap;
			}
			for (String gesture : tempMap.keySet()) {
				// gesturesMap.put(gesture, i);
				Map<String, Double> simCoefTFIDF2Map = calculateSimilarityTopics(
						tempMap, tempMap, gesture);
				// simCoefTFIDF2Map = sortByValuesFromMap(simCoefTFIDF2Map);
				gestureSimMap.put(gesture, simCoefTFIDF2Map);
				i++;
			}

			i = 0;
			componentCount++;
			for (String gesture : gestureSimMap.keySet()) {
				j = 0;
				for (String gestureFile : gestureSimMap.get(gesture).keySet()) {
					gestureSimMatrix[i][j] += gestureSimMap.get(gesture).get(
							gestureFile);

					if (componentCount == components.size()) {
						gestureSimMatrix[i][j] = gestureSimMatrix[i][j]
								/ (double) components.size();
					}
					j++;
				}
				i++;
			}
		}

		return gestureSimMatrix;
	}

	private static double[][] getMatrixForSVDFromDocMapForTFIDFOneAndTwo(
			Map<String, Map<Integer, Map<String, Word>>> precomputedMap, int ch) {
		double[][] gestureSimMatrix;
		int i = 0, j = 0;

		gestureSimMatrix = new double[precomputedMap.size()][precomputedMap
				.size()];
		int componentCount = 0;
		for (AllComponents component : components) {
			Map<String, Map<String, Double>> gestureSimMap = new HashMap<String, Map<String, Double>>();
			if (ch == 1) {
				for (String gesture : component.precomputedMap.keySet()) {
					Map<String, Double> simCoefTFIDF2Map = calculateSimilarityTFIDF(
							component.precomputedMap, component.precomputedMap,
							gesture);
					// simCoefTFIDF2Map = sortByValuesFromMap(simCoefTFIDF2Map);
					gestureSimMap.put(gesture, simCoefTFIDF2Map);
					i++;
				}
			} else {
				for (String gesture : component.precomputedMap.keySet()) {
					Map<String, Double> simCoefTFIDF2Map = calculateSimilarityTFIDF2(
							component.precomputedMap, component.precomputedMap,
							gesture);
					// simCoefTFIDF2Map = sortByValuesFromMap(simCoefTFIDF2Map);
					gestureSimMap.put(gesture, simCoefTFIDF2Map);
					i++;
				}
			}

			i = 0;
			componentCount++;
			for (String gesture : gestureSimMap.keySet()) {
				j = 0;
				for (String gestureFile : gestureSimMap.get(gesture).keySet()) {
					gestureSimMatrix[i][j] += gestureSimMap.get(gesture).get(
							gestureFile);
					if (componentCount == components.size()) {
						gestureSimMatrix[i][j] = gestureSimMatrix[i][j]
								/ (double) components.size();
					}
					j++;

				}
				i++;
			}
		}

		return gestureSimMatrix;
	}

	public static void task2bMenu(double[][] gestureSimMatrix,
			Map<String, Integer> gesturesMap) {
		// TODO Auto-generated method stub

		/*
		 * Map<String, Map<String, Double>> gestureSimMap = new HashMap<String,
		 * Map<String, Double>>(); double[][] gestureSimMatrix = new
		 * double[precomputedTopicMap.size()][precomputedTopicMap .size()];
		 * Map<String, Integer> gesturesMap = new LinkedHashMap<String,
		 * Integer>(); int i = 0, j = 0; for (String gesture :
		 * precomputedTopicMap.keySet()) { gesturesMap.put(gesture, i); //
		 * Calculate the similarity between One gesture with all other gestures
		 * and Maintain that in this Map
		 * 
		 * Map<String, Double> simCoefTFIDF2Map = calculateSimilarityTopics(
		 * precomputedTopicMap, precomputedTopicMap, gesture); //
		 * simCoefTFIDF2Map = sortByValuesFromMap(simCoefTFIDF2Map);
		 * gestureSimMap.put(gesture, simCoefTFIDF2Map); i++; } i = 0; //
		 * Convert this similarity Map to Matrix for (String gesture :
		 * gestureSimMap.keySet()) { j = 0; for (String gestureFile :
		 * gestureSimMap.get(gesture).keySet()) { gestureSimMatrix[i][j] =
		 * gestureSimMap.get(gesture).get( gestureFile); j++; } i++;X }
		 */

		SVD svdSim = new SVD(pUtil);
		double[][] outputFromCoVariance = svdSim
				.calculateCoVariance(gestureSimMatrix);

		Map<Integer, Map<String, Double>> finalGestureTopicMap = svdSim
				.getLatentSemanticsForGesturesFromPCA(outputFromCoVariance,
						gesturesMap);
		for (String gestureName : finalGestureTopicMap.get(1).keySet()) {
			System.out.print("gesture " + gestureName);
			for (Integer topic : finalGestureTopicMap.keySet()) {
				System.out.print("\t"
						+ finalGestureTopicMap.get(topic).get(gestureName));
			}
			System.out.println();
		}
		/*
		 * for (Integer topic : finalGestureTopicMap.keySet()) {
		 * System.out.println("For topic " + topic); Map<String, Double> tempMap
		 * = finalGestureTopicMap.get(topic); tempMap =
		 * sortByValuesFromMap(tempMap);
		 * 
		 * for (String gesture : tempMap.keySet()) {
		 * System.out.println("Gesture:" + gesture + " value:" +
		 * tempMap.get(gesture)); } }
		 */
		task3(finalGestureTopicMap);
	}

	public static void task3(
			Map<Integer, Map<String, Double>> finalGestureTopicMap) {
		Map<String, Map<Integer, Double>> categoryMap = new HashMap<String, Map<Integer, Double>>();
		for (Integer topic : finalGestureTopicMap.keySet()) {
			for (String gesture : finalGestureTopicMap.get(topic).keySet()) {
				if (!categoryMap.containsKey(gesture)) {
					categoryMap.put(gesture, new HashMap<Integer, Double>());
					categoryMap.get(gesture).put(topic,
							finalGestureTopicMap.get(topic).get(gesture));
				} else {
					Integer topic2 = categoryMap.get(gesture).keySet()
							.iterator().next();
					double temp = categoryMap.get(gesture).get(topic2);

					if (temp < finalGestureTopicMap.get(topic).get(gesture)) {
						categoryMap.get(gesture).clear();
						categoryMap.get(gesture).put(topic,
								finalGestureTopicMap.get(topic).get(gesture));
					}
				}
				/*
				 * if(categoryMap.get(gesture).size()==0){
				 * categoryMap.get(gesture).put(topic,
				 * finalGestureTopicMap.get(topic).get(gesture)); }
				 * if(finalGestureTopicMap.get(topic).get(gesture) >
				 * categoryMap.get(gesture).get(topic)){
				 * categoryMap.put(gesture, new HashMap<Integer, Double>());
				 * categoryMap.get(gesture).put(topic,
				 * finalGestureTopicMap.get(topic).get(gesture)); }
				 */
			}
		}
		Map<Integer, Integer> categoryCount = new HashMap<Integer, Integer>();
		for (String str : categoryMap.keySet()) {
			Integer topicName = 0;
			for (Integer val2 : categoryMap.get(str).keySet()) {
				topicName = val2;
			}
			if (!categoryCount.containsKey(topicName)) {
				categoryCount.put(topicName, 1);
			} else {
				int prevValue = categoryCount.get(topicName);
				categoryCount.put(topicName, ++prevValue);
			}

		}
		for (Integer topic : categoryCount.keySet()) {
			System.out.println("Topic " + topic + " has "
					+ categoryCount.get(topic) + " documents");
		}

	}

	public static void task2cMenu(double[][] gestureSimMatrix,
			Map<String, Integer> gesturesMap) {
		SVD svdSim = new SVD(pUtil);
		Map<Integer, Map<String, Double>> finalGestureTopicMap = svdSim
				.getLatentSemanticsForGesturesFromSVD(gestureSimMatrix,
						gesturesMap);

		for (String gestureName : finalGestureTopicMap.get(1).keySet()) {
			System.out.print("gesture " + gestureName);
			for (Integer topic : finalGestureTopicMap.keySet()) {
				System.out.print("\t"
						+ finalGestureTopicMap.get(topic).get(gestureName));
			}
			System.out.println();
		}
		/*
		 * for (Integer topic : finalGestureTopicMap.keySet()) { Map<String,
		 * Double> tempMap = finalGestureTopicMap.get(topic); tempMap =
		 * sortByValuesFromMap(tempMap);
		 * 
		 * System.out.println("For topic " + topic); for (String gesture :
		 * tempMap.keySet()) { System.out.println("Gesture:" + gesture +
		 * " value:" + tempMap.get(gesture)); } }
		 */
		task3(finalGestureTopicMap);

	}

	/**
	 * @throws MatlabInvocationException
	 * 
	 */
	public static void oneBMenu() throws MatlabInvocationException {
		System.out.println("Task 1 - b");
		String gestureFileName;
		System.out.print("Enter the file Name:");

		gestureFileName = sc.next();

		Map<String, Double[][]> normalizeQueryMap = phaseOne
				.getNormalizedValues(gestureFileName);

		getQueryComputedMap(normalizeQueryMap);
		while (true) {
			System.out
					.println("1. TF-IDF Sensor Vectors\t2. TF-IDF2 Sensor Vectors\t3. top-3 latent sensor semantics\t4. Exit");
			System.out.println("Enter the choice:");
			int c = sc.nextInt();
			sc.nextLine();
			if (c == 1) {
				System.out.println("Using TF-IDF");
				System.out
						.println("Top 10 similar gesture files for TF-IDF given gesture file"
								+ gestureFileName);
				Map<String, Double> simCoefTFIDFMap = calculateSimilarityTFIDF(
						queryMap, precomputedMap, gestureFileName);

				simCoefTFIDFMap = sortByValuesFromMap(simCoefTFIDFMap);
				Set<String> simTFKeys = simCoefTFIDFMap.keySet();
				int i = 0;
				for (String string : simTFKeys) {
					if (i == 5)
						break;
					System.out.println(string + ":"
							+ simCoefTFIDFMap.get(string));
					i++;
				}

			} else if (c == 2) {
				System.out
						.println("Top 10 similar gesture files for TF-IDF2 given gesture file"
								+ gestureFileName);
				Map<String, Double> simCoefTFIDF2Map = calculateSimilarityTFIDF2(
						queryMap, precomputedMap, gestureFileName);

				simCoefTFIDF2Map = sortByValuesFromMap(simCoefTFIDF2Map);
				Set<String> simTFIDF2Keys = simCoefTFIDF2Map.keySet();
				int i = 0;
				for (String string : simTFIDF2Keys) {
					if (i == 5)
						break;
					System.out.println(string + ":"
							+ simCoefTFIDF2Map.get(string));
					i++;
				}

			} else if (c == 3) {

				// putilQuery = new PrecomputeUtil();
				pUtil.precomputeQuerySVDMap(queryMap, queryMap.size());

				while (true) {
					System.out.println("Menu");
					System.out.println("1. SVD");
					System.out.println("2. PCA");
					System.out.println("3. LDA");
					System.out.println("4. Break");
					System.out.println("Enter the choice:");
					int choice = sc.nextInt();
					sc.nextLine();
					if (choice == 1) {

						createPrecomputedSVDTopicsForQueryMap(finalSVDMap,
								pUtil);
						System.out
								.println("Top 10 similar gesture files for Top three semantics given gesture file"
										+ gestureFileName);
						Map<String, Double> simCoefTFIDF2Map = calculateSimilarityTopics(
								precomputedTopicMap, precomputedTopicQueryMap,
								gestureFileName);

						simCoefTFIDF2Map = sortByValuesFromMap(simCoefTFIDF2Map);
						Set<String> simTFIDF2Keys = simCoefTFIDF2Map.keySet();
						int i = 0;
						for (String string : simTFIDF2Keys) {
							if (i == 5)
								break;
							System.out.println(string + ":"
									+ simCoefTFIDF2Map.get(string));
							i++;
						}

					} else if (choice == 2) {

						precomputedPCATopicMap = createPrecomputedSVDTopicsMap(
								finalPCAMap, pUtil); // MULTIPLYING dt * tf

						// System.out.print("Enter the file Name:");

						// gestureFileName = sc.next();

						createPrecomputedSVDTopicsForQueryMap(finalPCAMap,
								pUtil);
						System.out
								.println("Top 5 gesture files for PCA---- Task 1b");

						Map<String, Double> simCoefTFIDF2Map = calculateSimilarityTopics(
								precomputedPCATopicMap,
								precomputedTopicQueryMap, gestureFileName);

						simCoefTFIDF2Map = sortByValuesFromMap(simCoefTFIDF2Map);
						Set<String> simTFIDF2Keys = simCoefTFIDF2Map.keySet();
						int i = 0;
						for (String string : simTFIDF2Keys) {
							if (i == 5)
								break;
							System.out.println(string + ":"
									+ simCoefTFIDF2Map.get(string));
							i++;
						}

					} else if (choice == 3) {
						Map<String, Map<Integer, Map<Integer, Double>>> precomputedTopicQueryMap = allocater
								.projectQueryInLatentSpace(queryMap);
						Map<String, Double> simCoefTFIDF2Map = calculateSimilarityTopics(
								precomputedLDATopicMap,
								precomputedTopicQueryMap, gestureFileName);

						simCoefTFIDF2Map = sortByValuesFromMap(simCoefTFIDF2Map);
						Set<String> simTFIDF2Keys = simCoefTFIDF2Map.keySet();
						int i = 0;
						for (String string : simTFIDF2Keys) {
							if (i == 5)
								break;
							System.out.println(string + ":"
									+ simCoefTFIDF2Map.get(string));
							i++;
						}
					} else {
						break;
					}
				}
			} else {
				break;
			}
		}
	}

	public static void oneCMenu() throws MatlabInvocationException {
		List<String> gestures = new ArrayList<String>();
		int size = 0;
		System.out.println("Enter folder names:");
		String folder = sc.next();
		String[] folders = folder.split(",");
		for (int i = 0; i < folders.length; i++) {
			Map<String, Double[][]> normalizedMap = phaseOne
					.getNormalizedValues(folders[i]);
			NUMBER_OF_OBJECTS = normalizedMap.size() * 20;
			phaseOne.setBandMap(phaseOne.getGaussianBands(r));
			AllComponents component = new AllComponents(phaseOne,
					WINDOW_LENGTH, SHIFT_LENGTH, NUMBER_OF_OBJECTS, r);
			component.precomputeandCreateIndexMap(normalizedMap);
			components.add(component);
		}
		System.out.println("Enter the query");
		String query = sc.next();
		int i = 0;
		for (AllComponents component : components) {
			String gesture = folders[i] + "\\" + query;
			Map<String, Double[][]> normalizedMap = phaseOne
					.getNormalizedValues(gesture);
			component.getQueryComputedMap(normalizedMap);
			size = normalizedMap.size();
			gestures.add(gesture);
			i++;
		}

		while (true) {
			System.out
					.println("1. TF-IDF Sensor Vectors\t2. TF-IDF2 Sensor Vectors\t3. top-3 latent sensor semantics\t4. Exit");
			System.out.println("Enter the choice:");
			int c = sc.nextInt();
			sc.nextLine();
			if (c == 1) {
				int j = 0;
				Map<String, Double> simCoefFinalTFIDFMap = new HashMap<String, Double>();
				System.out.println("Using TF-IDF");
				for (AllComponents component : components) {
					Map<String, Double> simCoefTFIDFMap = calculateSimilarityTFIDF(
							component.getQueryMap(),
							component.getPrecomputedMap(), gestures.get(j));
					for (String gestureFile : simCoefTFIDFMap.keySet()) {
						if (!simCoefFinalTFIDFMap.containsKey(gestureFile)) {
							double value = simCoefTFIDFMap.get(gestureFile);
							simCoefFinalTFIDFMap.put(gestureFile, value);
						} else {
							double value = simCoefFinalTFIDFMap
									.get(gestureFile);
							value += simCoefTFIDFMap.get(gestureFile);
							simCoefFinalTFIDFMap.put(gestureFile, value);
						}
					}
					j++;

				}
				for (String gestureFileName : simCoefFinalTFIDFMap.keySet()) {
					simCoefFinalTFIDFMap.put(gestureFileName,
							simCoefFinalTFIDFMap.get(gestureFileName) / j);
				}
				simCoefFinalTFIDFMap = sortByValuesFromMap(simCoefFinalTFIDFMap);
				int top = 0;
				for (String gest : simCoefFinalTFIDFMap.keySet()) {
					if (top == 5)
						break;
					System.out.println("Gesture: " + gest + " : "
							+ simCoefFinalTFIDFMap.get(gest));
					top++;
				}

			} else if (c == 2) {
				System.out.println("Using TF-IDF2");

				int j = 0;
				Map<String, Double> simCoefFinalTFIDF2Map = new HashMap<String, Double>();
				System.out.println("Using TF-IDF2");
				for (AllComponents component : components) {
					Map<String, Double> simCoefTFIDFMap = calculateSimilarityTFIDF2(
							component.getQueryMap(),
							component.getPrecomputedMap(), gestures.get(j));
					for (String gestureFile : simCoefTFIDFMap.keySet()) {
						if (!simCoefFinalTFIDF2Map.containsKey(gestureFile)) {
							double value = simCoefTFIDFMap.get(gestureFile);
							simCoefFinalTFIDF2Map.put(gestureFile, value);
						} else {
							double value = simCoefFinalTFIDF2Map
									.get(gestureFile);
							value += simCoefTFIDFMap.get(gestureFile);
							simCoefFinalTFIDF2Map.put(gestureFile, value);
						}
					}
					j++;

				}
				for (String gestureFileName : simCoefFinalTFIDF2Map.keySet()) {
					simCoefFinalTFIDF2Map.put(gestureFileName,
							simCoefFinalTFIDF2Map.get(gestureFileName) / j);
				}
				simCoefFinalTFIDF2Map = sortByValuesFromMap(simCoefFinalTFIDF2Map);
				int top = 0;
				for (String gest : simCoefFinalTFIDF2Map.keySet()) {
					if (top == 5)
						break;
					System.out.println("Gesture: " + gest + " : "
							+ simCoefFinalTFIDF2Map.get(gest));
					top++;
				}
			} else if (c == 3) {
				while (true) {
					System.out.println("Menu");
					System.out.println("1. SVD");
					System.out.println("2. PCA");
					System.out.println("3. LDA");
					System.out.println("4. Break");
					System.out.println("Enter the choice:");
					int choice = sc.nextInt();
					sc.nextLine();
					if (choice == 1) {
						Map<String, Double> simCoefFinalTopicsMap = new HashMap<String, Double>();
						int k = 0;
						for (AllComponents component : components) {
							PrecomputeUtil util = new PrecomputeUtil();
							util.precomputeSVDMap(
									component.getPrecomputedMap(), size);
							component.setPrecomputeUtil(util);
							SVD svdDocSet = new SVD(util);
							Map<Integer, Double[][]> svdMap = svdDocSet
									.getMatrixForSVD();
							Map<Integer, Map<Integer, Map<String, Double>>> finalSVDMap = new HashMap<Integer, Map<Integer, Map<String, Double>>>();
							for (Integer sensor : svdMap.keySet()) {
								Double[][] sensorMatrix = svdMap.get(sensor);
								double[][] sensorTempMatrix = new double[sensorMatrix.length][sensorMatrix[0].length];
								for (i = 0; i < sensorMatrix.length; i++) {
									for (int j = 0; j < sensorMatrix[0].length; j++) {
										sensorTempMatrix[i][j] = sensorMatrix[i][j];
									}
								}
								finalSVDMap.putAll(svdDocSet
										.getLatentSemanticsFromSVD(
												sensorTempMatrix, sensor));
							}
							Map<String, Map<Integer, Map<Integer, Double>>> precomputedTopicMap;
							precomputedTopicQueryMap = new HashMap<String, Map<Integer, Map<Integer, Double>>>();
							util.precomputeQuerySVDMap(component.getQueryMap(),
									component.getQueryMap().size());
							precomputedTopicMap = createPrecomputedSVDTopicsMap(
									finalSVDMap, util);
							component.precomputedTopicMap = precomputedTopicMap;
							createPrecomputedSVDTopicsForQueryMap(finalSVDMap,
									util);
							Map<String, Double> simCoefTopicsMap = calculateSimilarityTopics(
									precomputedTopicMap,
									precomputedTopicQueryMap, gestures.get(k));
							for (String gestureFile : simCoefTopicsMap.keySet()) {
								if (!simCoefFinalTopicsMap
										.containsKey(gestureFile)) {
									double value = simCoefTopicsMap
											.get(gestureFile);
									simCoefFinalTopicsMap.put(gestureFile,
											value);
								} else {
									double value = simCoefFinalTopicsMap
											.get(gestureFile);
									value += simCoefTopicsMap.get(gestureFile);
									simCoefFinalTopicsMap.put(gestureFile,
											value);
								}
							}
							k++;
						}
						for (String gestureFileName : simCoefFinalTopicsMap
								.keySet()) {
							simCoefFinalTopicsMap.put(gestureFileName,
									simCoefFinalTopicsMap.get(gestureFileName)
											/ k);
						}
						simCoefFinalTopicsMap = sortByValuesFromMap(simCoefFinalTopicsMap);
						int top = 0;
						for (String gest : simCoefFinalTopicsMap.keySet()) {
							if (top == 5)
								break;
							System.out.println("Gesture: " + gest + " : "
									+ simCoefFinalTopicsMap.get(gest));
							top++;
						}
					} else if (choice == 2) {
						Map<String, Double> simCoefFinalTopicsMap = new HashMap<String, Double>();
						int k = 0;
						for (AllComponents component : components) {
							PrecomputeUtil util = new PrecomputeUtil();
							util.precomputeSVDMap(
									component.getPrecomputedMap(), size);
							component.setPrecomputeUtil(util);
							SVD svdDocSet = new SVD(util);
							Map<Integer, Double[][]> pcaMap = svdDocSet
									.getMatrixForSVD(); // NEW PCA MAP for PCA
							Map<Integer, Map<Integer, Map<String, Double>>> finalPCAMap = new HashMap<Integer, Map<Integer, Map<String, Double>>>();
							for (Integer sensor : pcaMap.keySet()) {
								Double[][] sensorMatrix = pcaMap.get(sensor);
								double[][] sensorTempMatrix = new double[sensorMatrix.length][sensorMatrix[0].length];
								for (i = 0; i < sensorMatrix.length; i++) {
									for (int j = 0; j < sensorMatrix[0].length; j++) {
										sensorTempMatrix[i][j] = sensorMatrix[i][j];
									}
								}
								double[][] outputFromCoVariance = svdDocSet
										.calculateCoVariance(sensorTempMatrix);
								finalPCAMap.putAll(svdDocSet
										.getLatentSemanticsFromPCA(
												outputFromCoVariance, sensor));
							}
							Map<String, Map<Integer, Map<Integer, Double>>> precomputedTopicMap;
							precomputedTopicQueryMap = new HashMap<String, Map<Integer, Map<Integer, Double>>>();
							util.precomputeQuerySVDMap(component.getQueryMap(),
									component.getQueryMap().size());
							precomputedTopicMap = createPrecomputedSVDTopicsMap(
									finalPCAMap, util);
							component.precomputedPCATopicMap = precomputedTopicMap;
							createPrecomputedSVDTopicsForQueryMap(finalPCAMap,
									util);
							Map<String, Double> simCoefTopicsMap = calculateSimilarityTopics(
									precomputedTopicMap,
									precomputedTopicQueryMap, gestures.get(k));
							for (String gestureFile : simCoefTopicsMap.keySet()) {
								if (!simCoefFinalTopicsMap
										.containsKey(gestureFile)) {
									double value = simCoefTopicsMap
											.get(gestureFile);
									simCoefFinalTopicsMap.put(gestureFile,
											value);
								} else {
									double value = simCoefFinalTopicsMap
											.get(gestureFile);
									value += simCoefTopicsMap.get(gestureFile);
									simCoefFinalTopicsMap.put(gestureFile,
											value);
								}
							}
							k++;
						}
						for (String gestureFileName : simCoefFinalTopicsMap
								.keySet()) {
							simCoefFinalTopicsMap.put(gestureFileName,
									simCoefFinalTopicsMap.get(gestureFileName)
											/ k);
						}
						simCoefFinalTopicsMap = sortByValuesFromMap(simCoefFinalTopicsMap);
						int top = 0;
						for (String gest : simCoefFinalTopicsMap.keySet()) {
							if (top == 5)
								break;
							System.out.println("Gesture: " + gest + " : "
									+ simCoefFinalTopicsMap.get(gest));
							top++;
						}

					} else if (choice == 3) {
						Map<String, Double> simCoefFinalTopicsMap = new HashMap<String, Double>();
						int k = 0;
						for (AllComponents component : components) {
							LatentDirichletAllocater dirichletAllocater = new LatentDirichletAllocater(
									component.getPrecomputeUtil());
							component.precomputedLDATopicMap = dirichletAllocater
									.allFiles(phaseOne.getProxy());
							Map<String, Map<Integer, Map<Integer, Double>>> precomputedTopicQueryMap = dirichletAllocater
									.projectQueryInLatentSpace(component
											.getQueryMap());
							Map<String, Double> simCoefTopicsMap = calculateSimilarityTopics(
									component.precomputedLDATopicMap,
									precomputedTopicQueryMap, gestures.get(k++));
							for (String gestureFile : simCoefTopicsMap.keySet()) {
								if (!simCoefFinalTopicsMap
										.containsKey(gestureFile)) {
									double value = simCoefTopicsMap
											.get(gestureFile);
									simCoefFinalTopicsMap.put(gestureFile,
											value);
								} else {
									double value = simCoefFinalTopicsMap
											.get(gestureFile);
									value += simCoefTopicsMap.get(gestureFile);
									simCoefFinalTopicsMap.put(gestureFile,
											value);
								}
							}
						}
						for (String gestureFileName : simCoefFinalTopicsMap
								.keySet()) {
							simCoefFinalTopicsMap.put(gestureFileName,
									simCoefFinalTopicsMap.get(gestureFileName)
											/ k);
						}
						simCoefFinalTopicsMap = sortByValuesFromMap(simCoefFinalTopicsMap);
						int top = 0;
						System.out
								.println("*******************LDA most similar files for 1c*****************");
						for (String gest : simCoefFinalTopicsMap.keySet()) {
							if (top == 5)
								break;
							System.out.println("Gesture: " + gest + " : "
									+ simCoefFinalTopicsMap.get(gest));
							top++;
						}
					} else
						break;
				}
				System.out.println("Using top-3 latent sensor semantics");

			} else
				break;

		}
	}

	public static void twoBCMenu() {
		System.out.println("Task 2");
		double[][] gestureSimMatrix;
		Map<String, Integer> gesturesMap = new LinkedHashMap<String, Integer>();
		int i = 0;

		while (true) {
			System.out.println("Menu");
			System.out
					.println("1. TF-IDF Sensor Vectors\t2. TF-IDF2 Sensor Vectors\t3. top-3 latent sensor semantics SVD\t4. LDA\t5. PCA\t6. Exit");
			System.out.println("Enter the choice:");
			int c = sc.nextInt();
			sc.nextLine();
			if (c == 1) {
				System.out.println("TF-IDF");
				gestureSimMatrix = getMatrixForSVDFromDocMapForTFIDFOneAndTwo(
						precomputedMap, 1);
				i = 0;
				for (String gesture : precomputedMap.keySet()) {
					gesturesMap.put(gesture, i);
					i++;
				}
				// task2cMenu(gestureSimMatrix, gesturesMap);
				taskTwoBOrC(gestureSimMatrix, gesturesMap);
			} else if (c == 2) {
				System.out.println("TF-IDF2");
				gestureSimMatrix = getMatrixForSVDFromDocMapForTFIDFOneAndTwo(
						precomputedMap, 2);
				i = 0;
				for (String gesture : precomputedMap.keySet()) {
					gesturesMap.put(gesture, i);
					i++;
				}
				taskTwoBOrC(gestureSimMatrix, gesturesMap);
			} else if (c == 3) {

				System.out.println("Top 3 latent semantics with SVD");
				gestureSimMatrix = getMatrixForSVDFromTopLatentSemantics(1);
				i = 0;
				for (String gesture : precomputedTopicMap.keySet()) {
					gesturesMap.put(gesture, i);
					i++;
				}

				taskTwoBOrC(gestureSimMatrix, gesturesMap);

			} else if (c == 4) {
				i = 0;
				System.out.println("Top 3 latent semantics with LDA");
				gestureSimMatrix = getMatrixForSVDFromTopLatentSemantics(3);
				for (String gesture : precomputedLDATopicMap.keySet()) {
					gesturesMap.put(gesture, i);
					i++;
				}

				taskTwoBOrC(gestureSimMatrix, gesturesMap);
			} else if (c == 5) {
				i = 0;
				gestureSimMatrix = getMatrixForSVDFromTopLatentSemantics(2);
				for (String gesture : precomputedPCATopicMap.keySet()) {
					gesturesMap.put(gesture, i);
					i++;
				}
				taskTwoBOrC(gestureSimMatrix, gesturesMap);

			} else {
				break;
			}
		}
	}

	public static void taskTwoBOrC(double[][] gestureSimMatrix,
			Map<String, Integer> gesturesMap) {
		System.out.println("Menu");
		System.out.println("1-2 b: top 3 Gesture semantics using PCA");
		System.out.println("2-2 c: top 3 Gesture semantics using SVD");
		System.out.println("3-break");
		System.out.println("Enter the choice:");
		int choice = sc.nextInt();
		sc.nextLine();
		if (choice == 1) {
			task2bMenu(gestureSimMatrix, gesturesMap);
		} else if (choice == 2) {
			task2cMenu(gestureSimMatrix, gesturesMap);
		}
	}

	public static void createPrecomputedSVDTopicsForQueryMap(
			Map<Integer, Map<Integer, Map<String, Double>>> finalMap,
			PrecomputeUtil pUtil) {
		precomputedTopicQueryMap = new HashMap<String, Map<Integer, Map<Integer, Double>>>();
		Map<Integer, Map<String, Map<Integer, Double>>> docFeatureMap = pUtil
				.generateDocumentFeatureMatrixForSVDPCA(finalMap, 1);
		// Map<String, Map<Integer, Map<String, Word>>>
		for (Integer sensor : docFeatureMap.keySet()) {
			for (String gestureSensor : docFeatureMap.get(sensor).keySet()) {
				String gesture = gestureSensor.substring(gestureSensor
						.indexOf("_") + 1);
				for (Integer topic : docFeatureMap.get(sensor)
						.get(gestureSensor).keySet()) {
					if (!precomputedTopicQueryMap.containsKey(gesture)) {
						Map<Integer, Double> topicMap = new HashMap<Integer, Double>();
						Map<Integer, Map<Integer, Double>> sensorMap = new HashMap<Integer, Map<Integer, Double>>();

						topicMap.put(topic,
								docFeatureMap.get(sensor).get(gestureSensor)
										.get(topic));
						sensorMap.put(sensor, topicMap);
						precomputedTopicQueryMap.put(gesture, sensorMap);
					} else {
						if (!precomputedTopicQueryMap.get(gesture).containsKey(
								sensor)) {
							Map<Integer, Double> topicMap = new HashMap<Integer, Double>();
							Map<Integer, Map<Integer, Double>> sensorMap = precomputedTopicQueryMap
									.get(gesture);

							topicMap.put(topic,
									docFeatureMap.get(sensor)
											.get(gestureSensor).get(topic));
							sensorMap.put(sensor, topicMap);
							precomputedTopicQueryMap.put(gesture, sensorMap);
						} else {
							if (!precomputedTopicQueryMap.get(gesture)
									.get(sensor).containsKey(topic)) {
								Map<Integer, Double> topicMap = precomputedTopicQueryMap
										.get(gesture).get(sensor);
								topicMap.put(topic, docFeatureMap.get(sensor)
										.get(gestureSensor).get(topic));
							}

						}
					}
				}

			}
		}
	}

	/**
	 * 
	 */
	public static Map<String, Map<Integer, Map<Integer, Double>>> createPrecomputedSVDTopicsMap(
			Map<Integer, Map<Integer, Map<String, Double>>> finalSVDMap,
			PrecomputeUtil pUtil) {
		System.out.println("SVD");
		Map<String, Map<Integer, Map<Integer, Double>>> precomputedTopicMap = new HashMap<String, Map<Integer, Map<Integer, Double>>>();
		Map<Integer, Map<String, Map<Integer, Double>>> docFeatureMap = pUtil
				.generateDocumentFeatureMatrixForSVDPCA(finalSVDMap, 0);
		// Map<String, Map<Integer, Map<String, Word>>>
		for (Integer sensor : docFeatureMap.keySet()) {
			for (String gestureSensor : docFeatureMap.get(sensor).keySet()) {
				String gesture = gestureSensor.substring(gestureSensor
						.indexOf("_") + 1);
				for (Integer topic : docFeatureMap.get(sensor)
						.get(gestureSensor).keySet()) {
					if (!precomputedTopicMap.containsKey(gesture)) {
						Map<Integer, Double> topicMap = new HashMap<Integer, Double>();
						Map<Integer, Map<Integer, Double>> sensorMap = new HashMap<Integer, Map<Integer, Double>>();

						topicMap.put(topic,
								docFeatureMap.get(sensor).get(gestureSensor)
										.get(topic));
						sensorMap.put(sensor, topicMap);
						precomputedTopicMap.put(gesture, sensorMap);
					} else {
						if (!precomputedTopicMap.get(gesture).containsKey(
								sensor)) {
							Map<Integer, Double> topicMap = new HashMap<Integer, Double>();
							Map<Integer, Map<Integer, Double>> sensorMap = precomputedTopicMap
									.get(gesture);

							topicMap.put(topic,
									docFeatureMap.get(sensor)
											.get(gestureSensor).get(topic));
							sensorMap.put(sensor, topicMap);
							precomputedTopicMap.put(gesture, sensorMap);
						} else {
							if (!precomputedTopicMap.get(gesture).get(sensor)
									.containsKey(topic)) {
								Map<Integer, Double> topicMap = precomputedTopicMap
										.get(gesture).get(sensor);
								topicMap.put(topic, docFeatureMap.get(sensor)
										.get(gestureSensor).get(topic));
							}

						}
					}
				}

			}
		}
		return precomputedTopicMap;
	}

	/**
	 * @param svdMap
	 */
	public static void oneAMenu(Map<Integer, Double[][]> svdMap, SVD svd) {
		System.out.println("Task 1 - a");
		// Map<Integer, Map<Integer, Map<String, Double>>> tempMap ;
		while (true) {
			System.out.println("1. SVD\t2. PCA\t3. LDA\t4. Exit");
			System.out.println("Enter the choice:");
			int c = sc.nextInt();
			sc.nextLine();
			if (c == 1) {
				for (Integer sensor : svdMap.keySet()) {
					System.out.println("Top three topics for Sensor number:"
							+ sensor);
					Double[][] sensorMatrix = svdMap.get(sensor);
					double[][] sensorTempMatrix = new double[sensorMatrix.length][sensorMatrix[0].length];
					for (int i = 0; i < sensorMatrix.length; i++) {
						for (int j = 0; j < sensorMatrix[0].length; j++) {
							sensorTempMatrix[i][j] = sensorMatrix[i][j];
						}
					}
					finalSVDMap.putAll(svd.getLatentSemanticsFromSVD(
							sensorTempMatrix, sensor));

					for (Integer feature : finalSVDMap.get(sensor).keySet()) {

						System.out.println("Topic:" + feature);
						Map<String, Double> wordsMap = finalSVDMap.get(sensor)
								.get(feature);
						System.out.println(" Total bag of words:"
								+ wordsMap.size());
						wordsMap = sortByValuesFromMap(wordsMap);
						for (String word : wordsMap.keySet()) {
							System.out.print(word + ":" + wordsMap.get(word)
									+ ", ");
						}
						System.out.println();
					}
				}
			} else if (c == 2) {
				for (Integer sensor : svdMap.keySet()) {
					System.out.println("Top 3 topics for sensor number :"
							+ sensor);

					Double[][] inputMatrixCoVariance = svdMap.get(sensor);
					double[][] inputTempMatrix = new double[inputMatrixCoVariance.length][inputMatrixCoVariance[0].length]; // Convert
																															// Double
																															// to
																															// double
					for (int i = 0; i < inputTempMatrix.length; i++) {
						for (int j = 0; j < inputTempMatrix[0].length; j++) {

							inputTempMatrix[i][j] = inputMatrixCoVariance[i][j];
						}

					}
					double[][] outputFromCoVariance = svd
							.calculateCoVariance(inputTempMatrix);

					finalPCAMap.putAll(svd.getLatentSemanticsFromPCA(
							outputFromCoVariance, sensor));

					for (Integer feature : finalPCAMap.get(sensor).keySet()) {

						System.out.println("Topic:" + feature);
						Map<String, Double> wordsMap = finalPCAMap.get(sensor)
								.get(feature);
						System.out.println(" Total bag of words:"
								+ wordsMap.size());
						wordsMap = sortByValuesFromMap(wordsMap);
						for (String word : wordsMap.keySet()) {
							System.out.print(word + ":" + wordsMap.get(word)
									+ ", ");
						}
						System.out.println();
					}

				}

			} else if (c == 3) {
				System.out.println("LDA");
				precomputedLDATopicMap = allocater
						.allFiles(phaseOne.getProxy());

			} else {
				break;
			}
		}
	}

	/**
	 * @param folderName
	 * @throws MatlabInvocationException
	 * @throws IOException
	 */
	public static void project1Module(String folderName)
			throws MatlabInvocationException, IOException {
		String gestureFileName;
		task2Menu(folderName);

		System.out.print("Enter the file Name for task 3:");

		gestureFileName = sc.next();

		Map<String, Double[][]> normalizeQueryMap = phaseOne
				.getNormalizedValues(gestureFileName);

		getQueryComputedMap(normalizeQueryMap);
		int i = 0;
		i = task3Menu(gestureFileName, i);
	}

	/**
	 * 
	 * 
	 * Calls the similarity calculator function and displays result
	 * 
	 * @param gestureFileName
	 * @param i
	 * @return
	 * 
	 */
	public static int task3Menu(String gestureFileName, int i) {

		while (true) {
			System.out.println("Menu for getting Similarity");
			System.out.println("1. Using TF");
			System.out.println("2. Using TF-IDF");
			System.out.println("3. Using TF-IDF2");
			System.out.println("4. Exit");
			int ch = sc.nextInt();

			if (ch == 1) {
				i = 0;
				System.out.println("Using TF");
				System.out
						.println("Top 10 similar gesture files for TF given gesture file"
								+ gestureFileName);
				Map<String, Double> simCoefTFMap = calculateSimilarityTF(gestureFileName);
				simCoefTFMap = sortByValuesFromMap(simCoefTFMap);
				Set<String> simKeys = simCoefTFMap.keySet();

				for (String string : simKeys) {
					if (i == 10)
						break;
					System.out.println(string + ":" + simCoefTFMap.get(string));
					i++;
				}
			} else if (ch == 2) {
				System.out.println("Using TF-IDF");
				System.out
						.println("Top 10 similar gesture files for TF-IDF given gesture file"
								+ gestureFileName);
				Map<String, Double> simCoefTFIDFMap = calculateSimilarityTFIDF(
						queryMap, precomputedMap, gestureFileName);

				simCoefTFIDFMap = sortByValuesFromMap(simCoefTFIDFMap);
				Set<String> simTFKeys = simCoefTFIDFMap.keySet();
				i = 0;
				for (String string : simTFKeys) {
					if (i == 10)
						break;
					System.out.println(string + ":"
							+ simCoefTFIDFMap.get(string));
					i++;
				}

			} else if (ch == 3) {
				System.out
						.println("Top 10 similar gesture files for TF-IDF2 given gesture file"
								+ gestureFileName);
				Map<String, Double> simCoefTFIDF2Map = calculateSimilarityTFIDF2(
						queryMap, precomputedMap, gestureFileName);

				simCoefTFIDF2Map = sortByValuesFromMap(simCoefTFIDF2Map);
				Set<String> simTFIDF2Keys = simCoefTFIDF2Map.keySet();
				i = 0;
				for (String string : simTFIDF2Keys) {
					if (i == 10)
						break;
					System.out.println(string + ":"
							+ simCoefTFIDF2Map.get(string));
					i++;
				}

			} else if (ch == 4)
				break;
		}
		return i;
	}

	/**
	 * Creates the pre-computed query map for term frequency and Idf2
	 * 
	 * @param normalizeQueryMap
	 */
	public static void getQueryComputedMap(
			Map<String, Double[][]> normalizeQueryMap) {
		// Set<String> normalizedSet = normalizeQueryMap.keySet();
		Iterator<String> itr = normalizeQueryMap.keySet().iterator();
		while (itr.hasNext()) {
			String gesture = itr.next();
			Double[][] normalizedValues = normalizeQueryMap.get(gesture);
			double maxTf = 0;
			for (int i = 0; i < normalizedValues.length; i++) {
				maxTf = 0;
				Set<String> wordsSet = new HashSet<String>();
				List<String> str = new ArrayList<String>();
				for (int j = 0; j < normalizedValues[0].length; j++) {
					String band = phaseOne
							.getGaussianBand(normalizedValues[i][j]);
					str.add(band);
				}

				if (!queryMap.containsKey(gesture)) {
					queryMap.put(gesture,
							new HashMap<Integer, Map<String, Word>>());
				}
				if (!queryMap.get(gesture).containsKey(i)) {
					queryMap.get(gesture).put(i, new HashMap<String, Word>());
				}

				for (int k = 0; k < (str.size() - WINDOW_LENGTH); k = k
						+ SHIFT_LENGTH) {
					String wordName = "";
					for (int window = 0; window < WINDOW_LENGTH; window++) {
						wordName += str.get(k + window);
					}
					Map<String, Word> sensorMap = queryMap.get(gesture).get(i);
					if (sensorMap.containsKey(wordName)) {
						double newTf = sensorMap.get(wordName).getTf() + 1;
						sensorMap.get(wordName).setTf(newTf);
						if (newTf > maxTf)
							maxTf = newTf;
					} else {
						Word query = new Word();
						query.setTf(1);
						sensorMap.put(wordName, query);
						if (maxTf == 0) {
							maxTf = 1;
						}
					}

					if (!queryVocabulary1.containsKey(wordName)) {
						queryVocabulary1.put(wordName, 1.0);

					} else {
						if (!wordsSet.contains(wordName))
							queryVocabulary1.put(wordName,
									queryVocabulary1.get(wordName) + 1.0);
					}

					wordsSet.add(wordName);
				}

				Map<String, Word> docMap = queryMap.get(gesture).get(i);
				for (String word : docMap.keySet()) {
					Word wordObj = docMap.get(word);
					wordObj.setRawTF(wordObj.getTf());
					double termfreq = wordObj.getTf() / maxTf;
					wordObj.setTf(termfreq);
					docMap.put(word, wordObj);
				}
				queryMap.get(gesture).get(i).putAll(docMap);
			}
		} // end of while

		Iterator<String> docIterator = queryVocabulary1.keySet().iterator();
		while (docIterator.hasNext()) {
			String wordName = docIterator.next();
			double df = queryVocabulary1.get(wordName);
			df = Math.log(20.0 / df);
			/*
			 * if (df == 0.0) { df = 0.05; }
			 */
			queryVocabulary2.put(wordName, df);
		}

		Iterator<String> iterator = queryVocabulary1.keySet().iterator();
		while (iterator.hasNext()) {
			String wordName = iterator.next();
			// double df = queryVocabulary1.get(wordName);
			queryVocabulary1.put(
					wordName,
					Math.log((NUMBER_OF_OBJECTS/* +20.0 */)
							/ (dfMap.get(wordName) /* +df */)));

		}

		/*
		 * iterator= queryVocabulary1.keySet().iterator();
		 * while(iterator.hasNext()){ String docName = iterator.next();
		 */

		Iterator<String> gestureIterator = queryMap.keySet().iterator();
		while (gestureIterator.hasNext()) {
			String gestureName = gestureIterator.next();
			Iterator<Integer> sensorIterator = queryMap.get(gestureName)
					.keySet().iterator();
			while (sensorIterator.hasNext()) {
				int sensorName = sensorIterator.next();
				Iterator<String> wordIterator = queryMap.get(gestureName)
						.get(sensorName).keySet().iterator();
				while (wordIterator.hasNext()) {
					String wordName = wordIterator.next();
					Word query = queryMap.get(gestureName).get(sensorName)
							.get(wordName);
					query.setTFIdf(query.getTf()
							* queryVocabulary1.get(wordName));
					query.setTFIdf2(query.getTf()
							* queryVocabulary2.get(wordName));
				}
			}
		}
	}

	/**
	 * 
	 * Display all the results in map
	 * 
	 * @param gestureFileName
	 * @param scanner
	 * @throws MatlabInvocationException
	 * @throws IOException
	 */
	public static void task2Menu(String folder)
			throws MatlabInvocationException, IOException {
		String gestureFileName;
		while (true) {
			System.out.println("Enter the file name for task 2");
			gestureFileName = sc.next();
			System.out.println("----Menu----");
			System.out.println("1. Top 10 TF");
			System.out.println("2. Top 10 IDF");
			System.out.println("3. Top 10 IDF2");
			System.out.println("4. Top 10 TF-IDF");
			System.out.println("5. Top 10 TF-IDF2");
			System.out.println("6. Exit");
			System.out.println("Enter the choice:");
			int choice = sc.nextInt();
			if (choice == 1) {
				System.out.println("Top 10 TF");
				Map<Integer, Map<String, Word>> sensorMap = precomputedMap
						.get(gestureFileName);
				displayResultsOnGrayScale(sensorMap, gestureFileName, choice,
						folder);
			} else if (choice == 2) {
				System.out.println("Top 10 IDF");
				Map<Integer, Map<String, Word>> sensorMap = precomputedMap
						.get(gestureFileName);
				displayResultsOnGrayScale(sensorMap, gestureFileName, choice,
						folder);
			} else if (choice == 3) {
				System.out.println("Top 10 IDF2");
				Map<Integer, Map<String, Word>> sensorMap = precomputedMap
						.get(gestureFileName);
				displayResultsOnGrayScale(sensorMap, gestureFileName, choice,
						folder);
			} else if (choice == 4) {
				System.out.println("Top 10 TF-IDF");
				Map<Integer, Map<String, Word>> sensorMap = precomputedMap
						.get(gestureFileName);
				displayResultsOnGrayScale(sensorMap, gestureFileName, choice,
						folder);
			} else if (choice == 5) {
				System.out.println("Top 10 TF-IDF2");
				Map<Integer, Map<String, Word>> sensorMap = precomputedMap
						.get(gestureFileName);
				displayResultsOnGrayScale(sensorMap, gestureFileName, choice,
						folder);
			} else if (choice == 6)
				break;
		}
	}

	/**
	 * Module to generate the dictionary of words
	 * 
	 * @param normalizedMap
	 */

	public static void precomputeandCreateIndexMap(
			Map<String, Double[][]> normalizedMap) {
		Iterator<String> iterator = normalizedMap.keySet().iterator();
		double maxTf = 0;
		while (iterator.hasNext()) {
			String fileName = iterator.next();
			if (fileName.equals("48.csv")) {
				System.out.println();
			}
			Double[][] normalizedArray = normalizedMap.get(fileName);
			for (int i = 0; i < normalizedArray.length; i++) {
				maxTf = 0;
				// StringBuilder str = new StringBuilder();
				Set<String> wordsSet = new HashSet<String>();
				List<String> str = new ArrayList<String>();
				for (int j = 0; j < normalizedArray[0].length; j++) {
					String band = phaseOne
							.getGaussianBand(normalizedArray[i][j]);
					str.add(band);
				}

				if (!precomputedMap.containsKey(fileName)) {
					precomputedMap.put(fileName,
							new HashMap<Integer, Map<String, Word>>());
				}

				if (!precomputedMap.get(fileName).containsKey(i)) {
					precomputedMap.get(fileName).put(i,
							new HashMap<String, Word>());
				}

				for (int k = 0; k < (str.size() - WINDOW_LENGTH); k = k
						+ SHIFT_LENGTH) {
					String wordName = "";
					for (int window = 0; window < WINDOW_LENGTH; window++) {
						wordName += str.get(k + window);
					}
					Map<String, Word> doc = precomputedMap.get(fileName).get(i);
					if (doc.containsKey(wordName)) {
						double newTf = doc.get(wordName).getTf() + 1;
						doc.get(wordName).setTf(newTf);
						if (newTf > maxTf)
							maxTf = newTf;
					} else {
						Word word = new Word();
						word.setTf(1.0);
						word.setX(k);
						word.setY(i);
						doc.put(wordName, word);
						if (maxTf == 0)
							maxTf = 1;
					}
					if (!vocabulary.containsKey(wordName)) {
						vocabulary.put(wordName, 1.0);

					} else {
						if (!wordsSet.contains(wordName))
							vocabulary.put(wordName,
									vocabulary.get(wordName) + 1.0);
					}

					if (!dfMap.containsKey(wordName)) {
						dfMap.put(wordName, 1.0);

					} else {
						if (!wordsSet.contains(wordName))
							dfMap.put(wordName, dfMap.get(wordName) + 1.0);
					}

					if (!vocabulary2.containsKey(fileName)) {
						vocabulary2
								.put(fileName, new HashMap<String, Double>());
						vocabulary2.get(fileName).put(wordName, 1.0);
					} else {
						if (vocabulary2.get(fileName).containsKey(wordName)) {
							if (!wordsSet.contains(wordName))
								vocabulary2.get(fileName)
										.put(wordName,
												vocabulary2.get(fileName).get(
														wordName) + 1.0);
						} else
							vocabulary2.get(fileName).put(wordName, 1.0);
					}
					wordsSet.add(wordName);
				}

				// new code for tf here
				Map<String, Word> docMap = precomputedMap.get(fileName).get(i);
				for (String word : docMap.keySet()) {
					Word wordObj = docMap.get(word);
					wordObj.setRawTF(wordObj.getTf());
					double termfreq = wordObj.getTf() / maxTf;
					wordObj.setTf(termfreq);
					docMap.put(word, wordObj);
				}
				precomputedMap.get(fileName).get(i).putAll(docMap);
			}
		}// end while
		Iterator<String> lexiconIterator = vocabulary.keySet().iterator();
		while (lexiconIterator.hasNext()) {
			String wordName = lexiconIterator.next();
			double df = vocabulary.get(wordName);
			vocabulary.put(wordName, Math.log(NUMBER_OF_OBJECTS / df));

		}

		lexiconIterator = vocabulary2.keySet().iterator();
		while (lexiconIterator.hasNext()) {
			String docName = lexiconIterator.next();
			Iterator<String> docIterator = vocabulary2.get(docName).keySet()
					.iterator();
			while (docIterator.hasNext()) {
				String wordName = docIterator.next();
				double df = vocabulary2.get(docName).get(wordName);
				df = Math.log(20.0 / df);
				/*
				 * if (df == 0.0) df = df + 0.05;
				 */
				vocabulary2.get(docName).put(wordName, df);
			}

		}

		Iterator<String> gestureIterator = precomputedMap.keySet().iterator();
		while (gestureIterator.hasNext()) {
			String gestureName = gestureIterator.next();
			Iterator<Integer> sensorIterator = precomputedMap.get(gestureName)
					.keySet().iterator();
			while (sensorIterator.hasNext()) {
				int sensorName = sensorIterator.next();
				Iterator<String> wordIterator = precomputedMap.get(gestureName)
						.get(sensorName).keySet().iterator();
				while (wordIterator.hasNext()) {
					String wordName = wordIterator.next();
					Word word = precomputedMap.get(gestureName).get(sensorName)
							.get(wordName);
					word.setTFIdf(word.getTf() * vocabulary.get(wordName));
					word.setTFIdf2(word.getTf()
							* vocabulary2.get(gestureName).get(wordName));
				}
			}
		}
	}

	public static Map<String, Double> getWordsFromSensorsForTF(
			Map<Integer, Map<String, Word>> sensorMap) {
		Map<String, Double> tfMap = new HashMap<String, Double>();
		Set<Integer> sensors = sensorMap.keySet();
		for (Integer sensor : sensors) {
			Set<String> wordsSet = sensorMap.get(sensor).keySet();
			for (String word : wordsSet) {
				String wordSensor = word + "_" + "sensor" + sensor;
				tfMap.put(wordSensor, sensorMap.get(sensor).get(word).getTf());
			}
		}
		return tfMap;
	}

	public static Map<String, Double> getWordsFromSensorsForTFIDF2(
			Map<Integer, Map<String, Word>> sensorMap) {
		Map<String, Double> tfIDF2Map = new HashMap<String, Double>();
		Set<Integer> sensors = sensorMap.keySet();
		for (Integer sensor : sensors) {
			Set<String> wordsSet = sensorMap.get(sensor).keySet();
			for (String word : wordsSet) {
				String wordSensor = word + "_" + "sensor" + sensor;
				tfIDF2Map.put(wordSensor, sensorMap.get(sensor).get(word)
						.getTFIdf2());
			}
		}
		return tfIDF2Map;
	}

	public static Map<String, Double> getWordsFromSensorsForTopics(
			Map<Integer, Map<Integer, Double>> map) {
		Map<String, Double> topicsMap = new HashMap<String, Double>();
		Set<Integer> sensors = map.keySet();
		for (Integer sensor : sensors) {
			Set<Integer> wordsSet = map.get(sensor).keySet();
			for (Integer word : wordsSet) {
				String wordSensor = word + "_" + "sensor" + sensor;
				topicsMap.put(wordSensor, map.get(sensor).get(word));
			}
		}

		return topicsMap;
	}

	// Function to calculate similarity
	/**
	 * @param gestureFileName
	 *            represents the gesture file for which we have to find the 5
	 *            most similar documents
	 * @return A map which contains the similarity values of all the other
	 *         gesture documents with the @param
	 */
	public static Map<String, Double> calculateSimilarityTopics(
			Map<String, Map<Integer, Map<Integer, Double>>> topicMap,
			Map<String, Map<Integer, Map<Integer, Double>>> topicQueryMap,
			String gestureFileName) {
		Map<String, Double> topicsQueryMap = getWordsFromSensorsForTopics(topicQueryMap
				.get(gestureFileName));
		Map<String, Double> simCoefMap = new HashMap<String, Double>();
		double queryNorm = 0, docNorm = 0, simCoefTFIDF = 0;
		Set<String> queryStrings = topicsQueryMap.keySet();
		for (String string : queryStrings) {
			double value = topicsQueryMap.get(string);
			queryNorm = queryNorm + Math.pow(value, 2);
		}
		queryNorm = Math.pow(queryNorm, 0.5);

		Set<String> gestureFiles = topicMap.keySet();
		for (String gestureFile : gestureFiles) {
			Map<String, Double> topicsMap = getWordsFromSensorsForTopics(topicMap
					.get(gestureFile));
			Set<String> docStrings = topicsMap.keySet();
			docNorm = 0;
			simCoefTFIDF = 0;
			for (String string : docStrings) {
				double value = topicsMap.get(string);
				docNorm = docNorm + Math.pow(value, 2);
			}
			docNorm = Math.pow(docNorm, 0.5);
			Set<String> queries = topicsQueryMap.keySet();
			for (String string : queries) {
				if (topicsMap.containsKey(string)) {
					double val1 = topicsQueryMap.get(string);
					double val2 = topicsMap.get(string);
					simCoefTFIDF = simCoefTFIDF + (val1 * val2);
				}
			}
			if (queryNorm != 0.0 && docNorm != 0.0) {
				simCoefTFIDF = simCoefTFIDF / (queryNorm * docNorm);
			} else
				simCoefTFIDF = 0.0;
			simCoefMap.put(gestureFile, simCoefTFIDF);

		}
		return simCoefMap;
	}

	public static Map<String, Double> getWordsFromSensorsForTFIDF(
			Map<Integer, Map<String, Word>> sensorMap) {
		Map<String, Double> tfIDFMap = new HashMap<String, Double>();
		Set<Integer> sensors = sensorMap.keySet();
		for (Integer sensor : sensors) {
			Set<String> wordsSet = sensorMap.get(sensor).keySet();
			for (String word : wordsSet) {
				String wordSensor = word + "_" + "sensor" + sensor;
				tfIDFMap.put(wordSensor, sensorMap.get(sensor).get(word)
						.getTFIdf());
			}
		}
		return tfIDFMap;
	}

	// Calculates cosine similarity between query and all documents using TF-
	// IDF2

	public static Map<String, Double> calculateSimilarityTFIDF2(
			Map<String, Map<Integer, Map<String, Word>>> queryMap,
			Map<String, Map<Integer, Map<String, Word>>> precomputedMap,
			String gestureFileName) {
		Map<String, Double> tfInputMap = getWordsFromSensorsForTFIDF2(queryMap
				.get(gestureFileName));
		Map<String, Double> simCoefMap = new HashMap<String, Double>();
		double queryNorm = 0, docNorm = 0, simCoefTFIDF2 = 0;
		Set<String> queryStrings = tfInputMap.keySet();
		for (String string : queryStrings) {
			double value = tfInputMap.get(string);
			queryNorm = queryNorm + Math.pow(value, 2);
		}
		queryNorm = Math.pow(queryNorm, 0.5);

		Set<String> gestureFiles = precomputedMap.keySet();
		for (String gestureFile : gestureFiles) {
			// System.out.println("Gesture File Names:" + gestureFile);
			Map<String, Double> tfMap = getWordsFromSensorsForTFIDF2(precomputedMap
					.get(gestureFile));
			Set<String> docStrings = tfMap.keySet();
			docNorm = 0;
			simCoefTFIDF2 = 0;
			for (String string : docStrings) {
				double value = tfMap.get(string);
				docNorm = docNorm + Math.pow(value, 2);
			}
			docNorm = Math.pow(docNorm, 0.5);
			Set<String> queries = tfInputMap.keySet();
			for (String string : queries) {
				if (tfMap.containsKey(string)) {
					simCoefTFIDF2 = simCoefTFIDF2
							+ (tfInputMap.get(string) * tfMap.get(string));
				}
			}
			if ((queryNorm * docNorm) != 0) {
				simCoefTFIDF2 = simCoefTFIDF2 / (queryNorm * docNorm);
			}
			simCoefMap.put(gestureFile, simCoefTFIDF2);

		}
		return simCoefMap;
	}

	// Calculates cosine similarity between query and all documents using TF

	public static Map<String, Double> calculateSimilarityTF(
			String gestureFileName) {
		Map<String, Double> tfInputMap = getWordsFromSensorsForTF(queryMap
				.get(gestureFileName));
		Map<String, Double> simCoefMap = new HashMap<String, Double>();
		double queryNorm = 0, docNorm = 0, simCoefTF = 0;
		Set<String> queryStrings = tfInputMap.keySet();
		for (String string : queryStrings) {
			double value = tfInputMap.get(string);
			queryNorm = queryNorm + Math.pow(value, 2);
		}
		queryNorm = Math.pow(queryNorm, 0.5);

		Set<String> gestureFiles = precomputedMap.keySet();
		for (String gestureFile : gestureFiles) {
			// System.out.println("Gesture File Names:" + gestureFile);
			Map<String, Double> tfMap = getWordsFromSensorsForTF(precomputedMap
					.get(gestureFile));
			Set<String> docStrings = tfMap.keySet();
			docNorm = 0;
			simCoefTF = 0;
			for (String string : docStrings) {
				double value = tfMap.get(string);
				docNorm = docNorm + Math.pow(value, 2);
			}
			docNorm = Math.pow(docNorm, 0.5);
			Set<String> queries = tfInputMap.keySet();
			for (String string : queries) {
				if (tfMap.containsKey(string)) {
					simCoefTF = simCoefTF
							+ (tfInputMap.get(string) * tfMap.get(string));
				}
			}
			if ((queryNorm * docNorm) != 0) {
				simCoefTF = simCoefTF / (queryNorm * docNorm);
			}
			simCoefMap.put(gestureFile, simCoefTF);

		}
		return simCoefMap;
	}

	// Calculates cosine similarity between query and all documents using TF-IDF
	public static Map<String, Double> calculateSimilarityTFIDF(
			Map<String, Map<Integer, Map<String, Word>>> queryMap,
			Map<String, Map<Integer, Map<String, Word>>> precomputedMap,
			String gestureFileName) {
		Map<String, Double> tfIDFInputMap = getWordsFromSensorsForTFIDF(queryMap
				.get(gestureFileName));
		Map<String, Double> simCoefMap = new HashMap<String, Double>();
		double queryNorm = 0, docNorm = 0, simCoefTFIDF = 0;
		Set<String> queryStrings = tfIDFInputMap.keySet();
		for (String string : queryStrings) {
			double value = tfIDFInputMap.get(string);
			queryNorm = queryNorm + Math.pow(value, 2);
		}
		queryNorm = Math.pow(queryNorm, 0.5);

		Set<String> gestureFiles = precomputedMap.keySet();
		for (String gestureFile : gestureFiles) {
			Map<String, Double> tfIDFMap = getWordsFromSensorsForTFIDF(precomputedMap
					.get(gestureFile));
			Set<String> docStrings = tfIDFMap.keySet();
			docNorm = 0;
			simCoefTFIDF = 0;
			for (String string : docStrings) {
				double value = tfIDFMap.get(string);
				docNorm = docNorm + Math.pow(value, 2);
			}
			docNorm = Math.pow(docNorm, 0.5);
			Set<String> queries = tfIDFInputMap.keySet();
			for (String string : queries) {
				if (tfIDFMap.containsKey(string)) {
					simCoefTFIDF = simCoefTFIDF
							+ (tfIDFInputMap.get(string) * tfIDFMap.get(string));
				}
			}
			if ((queryNorm * docNorm) != 0) {
				simCoefTFIDF = simCoefTFIDF / (queryNorm * docNorm);
			}
			simCoefMap.put(gestureFile, simCoefTFIDF);
		}
		return simCoefMap;
	}

	// This module generates top 10 results for tf, idf, tfidf, tfidf2 and
	// generates gray scale heat map to display these results.
	public static void displayResultsOnGrayScale(
			Map<Integer, Map<String, Word>> sensorMap, String gestureName,
			int choice, String folderName) throws MatlabInvocationException,
			IOException {
		System.out.println("Results for gesture file:" + gestureName);
		Integer[][] arrayTFwords = new Integer[10][2];
		Map<String, Double> tfMap = new LinkedHashMap<String, Double>();
		Map<String, Double> tfidfMap = new LinkedHashMap<String, Double>();
		Map<String, Double> tfidf2Map = new LinkedHashMap<String, Double>();

		Set<Integer> sensors = sensorMap.keySet();
		for (Integer sensor : sensors) {
			Set<String> wordsSet = sensorMap.get(sensor).keySet();
			for (String word : wordsSet) {
				String wordSensor = word + "_" + sensor;
				tfMap.put(wordSensor, sensorMap.get(sensor).get(word).getTf());
				tfidfMap.put(wordSensor, sensorMap.get(sensor).get(word)
						.getTFIdf());
				tfidf2Map.put(wordSensor, sensorMap.get(sensor).get(word)
						.getTFIdf2());
			}
		}
		int i = 0;
		if (choice == 1) {
			i = 0;

			tfMap = sortByValuesFromMap(tfMap);
			Set<String> tfWords = tfMap.keySet();

			System.out.println("Top 10 tf words");
			for (String string : tfWords) {
				/*
				 * if (i == 10) break;
				 */

				String number = string.substring(string.indexOf('_') + 1,
						string.length());
				int n = Integer.parseInt(number);
				String sensorStr = string.substring(0, string.indexOf('_'));
				arrayTFwords[i][0] = sensorMap.get(n).get(sensorStr).getX();
				arrayTFwords[i][1] = sensorMap.get(n).get(sensorStr).getY();
				System.out.println("Word:" + string + " " + "Value:"
						+ tfMap.get(string));
				i++;
			}
		} else if (choice == 2) {
			i = 0;
			vocabulary = sortByValuesFromMap(vocabulary);

			System.out.println("Printing top 10 IDF values for " + gestureName);
			for (String word : vocabulary.keySet()) {
				int n;
				int flag = 0;
				/*
				 * if (i == 10) break;
				 */
				Set<Integer> sensorsSet = sensorMap.keySet();
				for (Integer integer : sensorsSet) {
					Set<String> wordsSet = sensorMap.get(integer).keySet();
					for (String wordString : wordsSet) {
						if (word.equals(wordString)) {
							n = integer;
							flag = 1;
							arrayTFwords[i][0] = sensorMap.get(n).get(word)
									.getX();
							arrayTFwords[i][1] = sensorMap.get(n).get(word)
									.getY();
							System.out.println(word + " :"
									+ vocabulary.get(word) + " Sensor:" + n);
							i++;
							break;
						}
					}
					if (flag == 1)
						break;
				}
				/*
				 * if(vocabulary2.get(gestureName).containsKey(word)){
				 * 
				 * }
				 */
			}

		} else if (choice == 3) {
			i = 0;

			Map<String, Double> gestureIDFMap = vocabulary2.get(gestureName);
			gestureIDFMap = sortByValuesFromMap(gestureIDFMap);
			System.out
					.println("Printing top 10 IDF2 values for " + gestureName);
			for (String word : gestureIDFMap.keySet()) {
				/*
				 * if (i >= 10) break;
				 */
				int n = 0;
				int flag = 0;
				Set<Integer> sensorsSet = sensorMap.keySet();
				for (Integer integer : sensorsSet) {
					Set<String> wordsSet = sensorMap.get(integer).keySet();
					for (String wordString : wordsSet) {
						if (word == wordString) {
							n = integer;
							flag = 1;
							arrayTFwords[i][0] = sensorMap.get(n).get(word)
									.getX();
							arrayTFwords[i][1] = sensorMap.get(n).get(word)
									.getY();
							System.out.println(word + " :"
									+ gestureIDFMap.get(word));

							i++;
							break;
						}
					}
					if (flag == 1)
						break;
				}
			}
		}

		else if (choice == 4) {
			i = 0;
			tfidfMap = sortByValuesFromMap(tfidfMap);
			System.out.println("Top 10 tf-idf words");
			Set<String> tfIdfWords = tfidfMap.keySet();
			i = 0;
			for (String string : tfIdfWords) {
				/*
				 * if (i == 10) break;
				 */
				String number = string.substring(string.indexOf('_') + 1,
						string.length());
				int n = Integer.parseInt(number);
				String sensorStr = string.substring(0, string.indexOf('_'));
				arrayTFwords[i][0] = sensorMap.get(n).get(sensorStr).getX();
				arrayTFwords[i][1] = sensorMap.get(n).get(sensorStr).getY();

				System.out.println("Word:" + string + " " + "Value:"
						+ tfidfMap.get(string));
				i++;
			}
		}

		else if (choice == 5) {
			i = 0;
			tfidf2Map = sortByValuesFromMap(tfidf2Map);
			System.out.println("Top 10 tf-idf2 words");

			Set<String> tfIdf2Words = tfidf2Map.keySet();
			i = 0;
			for (String string : tfIdf2Words) {
				/*
				 * if (i == 10) break;
				 */
				String number = string.substring(string.indexOf('_') + 1,
						string.length());
				int n = Integer.parseInt(number);
				String sensorStr = string.substring(0, string.indexOf('_'));
				arrayTFwords[i][0] = sensorMap.get(n).get(sensorStr).getX();
				arrayTFwords[i][1] = sensorMap.get(n).get(sensorStr).getY();
				System.out.println("Word:" + string + " " + "Value:"
						+ tfidf2Map.get(string));
				i++;
			}
		}

		// Using the matlab function to generate grayscale map
		FileWriter writer = new FileWriter("temp.csv");

		for (int m = 0; m < arrayTFwords.length; m++) {
			for (int n = 0; n < arrayTFwords[m].length; n++) {
				Integer tt = arrayTFwords[m][n];
				writer.append(tt + "");
				writer.append(',');
			}
			writer.append('\n');
		}

		Object[] a = new Object[2];

		a[0] = folderName + "\\" + gestureName;
		System.out.println("Folder:" + a[0]);
		a[1] = "temp.csv";
		writer.flush();
		writer.close();
		MatlabProxy proxy = phaseOne.getProxyConnection();

		proxy.feval("visualize", a);

	}
}

