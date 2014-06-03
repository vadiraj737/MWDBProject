package com.mwdb.phase3;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import matlabcontrol.MatlabInvocationException;
import matlabcontrol.MatlabProxy;

public class SupportVectorMachines {
	double[][] featureMatrix;
	Map<Integer, Integer> featureNames;
	Map<Integer, Integer> featureIndexes;
	double[][] testData;
	//private final int NUMBER_OF_DIMENSIONS;
	private MatlabProxy matlabProxy;
	Map<Integer, Integer> testMap = new HashMap<>();
	
	public SupportVectorMachines(double[][] featureMatrix, MatlabProxy matlabProxy, Map<Integer, Integer> labelHelperMap) {
		super();
		this.featureMatrix = featureMatrix;
		this.matlabProxy = matlabProxy;
		this.featureNames = labelHelperMap;
		featureIndexes = getFeatureIndexes(featureNames);
		//NUMBER_OF_DIMENSIONS = featureMatrix[0].length;
	}
	private Map<Integer, Integer> getFeatureIndexes(
			Map<Integer, Integer> featureNames2) {
		Map<Integer, Integer> featureIndexes = new HashMap<>();
		for(int tempVal: featureNames2.keySet()){
			featureIndexes.put(featureNames2.get(tempVal), tempVal);
		}
		return featureIndexes;
	}
	public void createModel(){
		Map<Integer, Set<Integer>> classifiedData = getClassifiedData();
		partitionSpace(classifiedData);
	}
	public Map<Integer, Set<Integer>> getClassifiedData(){
		Map<Integer, Set<Integer>> classifiedData= new LinkedHashMap<>();
		try {
			BufferedReader br = new BufferedReader(new FileReader("Label.csv"));
			String line = "";
			while ((line = br.readLine()) != null) {

				String[] country = line.split(",");
				if(!classifiedData.containsKey(Integer.parseInt(country[1]))){
					/*if(classifiedData.get(country[0]) == null){
						classifiedData.put(Integer.parseInt(country[1]), new HashSet<Integer>());
					}*/
					classifiedData.put(Integer.parseInt(country[1]), new HashSet<Integer>());
					classifiedData.get(Integer.parseInt(country[1])).add(Integer.parseInt(country[0]));
				}
				else{
					classifiedData.get(Integer.parseInt(country[1])).add(Integer.parseInt(country[0]));
				}
			}
			br.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return classifiedData;
	}
	private void partitionSpace(Map<Integer, Set<Integer>> classifiedData) {
		Set<Integer> classLabels = new LinkedHashSet<>();
		Map<Integer, Set<Integer>> outputMap = new HashMap<>();
		classLabels.addAll(classifiedData.keySet());
		int numberOfClassifiedData = 0;
		for(int classLabel: classifiedData.keySet()){
			numberOfClassifiedData += classifiedData.get(classLabel).size();
		}
		//double[][] trainingData = new double[numberOfClassifiedData][classifiedData.size()];
		/*for(int i=0; i<numberOfClassifiedData; i++){
			for(int val: featureNames.keySet()){
				if(featureNames.get(val) == classifiedData.get(val)){
					
				}
			}
			trainingData[i] = inputData[i];
		}*/
		
		testData = new double[featureMatrix.length-numberOfClassifiedData][];
		int testCount = 0;
		for(int i=0; i<(featureMatrix.length);i++){
			boolean isFound = false;
			for(Integer val: classifiedData.keySet()){
				int key = featureNames.get(i);
				if(classifiedData.get(val).contains(key)){
					isFound = true;
				}
			}
			
			if(!isFound){
				testData[testCount] = featureMatrix[i];
				testMap.put(testCount, featureNames.get(i));
				testCount++;
			}
		}
		
		for(int classLabel: classLabels){
			Set<Integer> set1 = classifiedData.get(classLabel);
			Set<Integer> set2 = new LinkedHashSet<>();
			for(int secondSetClassLabel: classifiedData.keySet()){
				if(secondSetClassLabel!=classLabel){
					set2.addAll(classifiedData.get(secondSetClassLabel));
				}
				else{
					continue;
				}
			}
			
			Set<Integer> classifiedSet = binarySVM(set1, set2);
			outputMap.put(classLabel, classifiedSet);
		}
		for(int i=0; i<featureMatrix.length; i++){
			boolean flag = false;
			for(int classLabel:classifiedData.keySet()){
				if(classifiedData.get(classLabel).contains(i)){
					flag = true;
				}
			}
			if(!flag){
				System.out.print("gesture name: "+ featureNames.get(i));
				System.out.print("\tClass name:");
				for(int classLabel: outputMap.keySet()){
					if(outputMap.get(classLabel).contains(featureNames.get(i))){
						System.out.print(""+classLabel);
					}
				}
				System.out.println();
			}
			
		}
		/*for(int classLabel: classifiedData.keySet()){
			docSet.addAll(classifiedData.get(classLabels));
		}
		int docIterator = 0;
		for(int document: docSet){
			for(int classLabel: outputMap.keySet()){
				if
			}
		}*/
		
	}
	
	private Set<Integer> binarySVM(Set<Integer> set1, Set<Integer> set2) {
		Set<Integer> documentSet = new LinkedHashSet<>();
		documentSet.addAll(set1);
		documentSet.addAll(set2);
		for(int i=0; i<testData.length; i++){
			documentSet.add(testMap.get(i));
		}
		double[][] hMatrix = new double[documentSet.size()][documentSet.size()];
		int set1DocIterator = 0;
		for(int set1Doc : documentSet){
			int set2DocIterator = 0;
			double[] set1X = featureMatrix[featureIndexes.get(set1Doc)];
			for(int set2Doc: documentSet){
				double[] set2X = featureMatrix[featureIndexes.get(set2Doc)];
				double dotProduct = getDotProduct(set1X, set2X);
				if(set2.contains(set2Doc)){
					dotProduct = dotProduct * -1;
				}
				else{
					// nothing
				}
				if(set2.contains(set1Doc)){
					dotProduct = dotProduct * -1;
				}
				else{
					// nothing
				}
				hMatrix[set1DocIterator][set2DocIterator] = dotProduct;
				set2DocIterator++;
			}
			set1DocIterator++;
		}
		double[] legrangianMultiples = new double[documentSet.size()];
		try {
			FileWriter writer = new FileWriter("SVM1.csv");

			for (int m = 0; m < hMatrix.length; m++) {
				for (int n = 0; n < hMatrix[m].length; n++) {
					System.out.print(hMatrix[m][n]+" ");
					double tt = hMatrix[m][n];
					
					writer.append(""+tt);
					if(n!=hMatrix[m].length-1)
						writer.append(',');
				}
				System.out.println();
				writer.append('\n');
			}
			writer.flush();
			writer.close();
			matlabProxy.feval("quadratic_mwdb","\\"+"SVM1.csv");
			
			String delim = ",";
	        
            String line = "";
            try {
            	BufferedReader bufferedReader = new BufferedReader(new FileReader("hmatrix.csv"));

            	int i=0;
            	while ((line = bufferedReader.readLine()) != null) 
            	{
            		String[] tokens = line.split(delim);
            		legrangianMultiples[i++]=Double.parseDouble(tokens[0]);
            	}
            	bufferedReader.close();
            } catch (NumberFormatException | IOException e) {
            	e.printStackTrace();
            }
            //legrangianMultiples = alphaValues[0];
		} catch (MatlabInvocationException e) {
			e.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		double[] wVector = getWVector(legrangianMultiples, set1, set2);
		Set<Integer> supportVectors = new HashSet<>();
		int set1Iterator = 0;
		for(int intValue : set1){
			if(legrangianMultiples[set1Iterator]>0){
				supportVectors.add(intValue);
			}
			else{
				continue;
			}
			set1Iterator++;
		}
		int intValueIterator = 0;
		for(int intValue : set2){
			if(legrangianMultiples[intValueIterator]>0){
				supportVectors.add(intValue);
			}
			else{
				continue;
			}
			intValueIterator++;
		}
		double bValue= getBValue(supportVectors, set1, set2, legrangianMultiples);
		/*int docIterator = 0;
		Map<Boolean, Set<Integer>> outputMap = new LinkedHashMap<>();
		for(int docNumber: documentSet){
			boolean flag = checkIfPresentInFirstClass(wVector, bValue, featureMatrix[docIterator]);
			if(flag){
				if(outputMap.containsKey(true)){
					outputMap.get(true).add(docNumber);
				}
				else{
					outputMap.put(true, new LinkedHashSet<Integer>());
				}
			}
			else{
				if(outputMap.containsKey(false)){
					outputMap.get(false).add(docNumber);
				}
				else{
					outputMap.put(false, new LinkedHashSet<Integer>());
					outputMap.get(false).add(docNumber);
				}
			}
		}*/
		
		Set<Integer> classifiedSet = new LinkedHashSet<>();
		for(int i=0; i<testData.length;i++){
			int gestureName = testMap.get(i);
			int gestureIndex = 0;
			for(int tempVal: featureNames.keySet()){
				if(featureNames.get(tempVal) == gestureName){
					gestureIndex = tempVal;
					break;
				}
			}
			boolean flag = checkIfPresentInFirstClass(wVector, bValue, featureMatrix[gestureIndex]);
			if(flag){
				classifiedSet.add(featureNames.get(gestureIndex));
			}
		}
		return classifiedSet;
	}
	
	private boolean checkIfPresentInFirstClass(double[] wVector, double bValue, double[] featureVector) {
		double value = 0.0;
		value = getDotProduct(wVector, featureVector);
		//value = value + bValue;
		if(value<0)
			return false;
		else
			return true;
	}
	private double getBValue(Set<Integer> supportVectors, Set<Integer> set1,
			Set<Integer> set2, double[] legrangianMultiples) {
		double bValue = 0.0;
		int sIterator = 0;
		for(int s: supportVectors){
			Set<Integer> tempKeySet = new HashSet<>();
			tempKeySet.addAll(supportVectors);
			double yS = 0;
			if(set1.contains(s)){
				yS = 1;
			}
			else{
				yS = -1;
			}
				
			int mIterator = 0;
			for(int m: tempKeySet){
				double dotProduct = getDotProduct(featureMatrix[mIterator], featureMatrix[sIterator]);
				if(!set1.contains(m)){
					dotProduct = dotProduct * -1;
				}
				double alphaM = legrangianMultiples[mIterator];
				yS = yS - (dotProduct*alphaM);
				mIterator++;
			}
			bValue += yS;
		}
		bValue = bValue/supportVectors.size();
		return bValue;
	}
	private double[] getWVector(double[] legrangianMultiples,
			Set<Integer> set1, Set<Integer> set2) {
		
		double[] wVector = new double[featureMatrix[0].length];
		Set<Integer> docSet = new LinkedHashSet<>();
		docSet.addAll(set1);
		docSet.addAll(set2);
		int docNumberIterator = 0;
		
		for(int docNumber: docSet){
			
			int index = 0;
			for(int tempVal: featureNames.keySet()){
				if(featureNames.get(tempVal) == docNumber){
					index = tempVal;
				}
			}
			double alpha = legrangianMultiples[index];
			int y = 0;
			if(set1.contains(docNumber)){
				y = 1;
			}
			else{
				y = -1;
			}
			for(int i=0; i<wVector.length; i++){
				
				wVector[i] += alpha*y*featureMatrix[index][i];
			}
			docNumberIterator++;
		}
		return wVector;
	}
	private double getDotProduct(double[] set1x, double[] set2x) {
		double dotProduct = 0.0;
		for(int i=0; i<set1x.length; i++){
			dotProduct += set1x[i]*set2x[i];
		}
		return dotProduct;
	}
	/*private void getMinMaxFromOrigin(Map<Integer, Set<Integer>> classifiedData,
			double[] min, double[] max) {
		for(int classLabel : classifiedData.keySet())
		{
			//calculate the distance between the origin(0,0) with every point of LABEL 1
			Set<Integer> documents = classifiedData.get(1);
			for(int document: documents){
				double distanceFromOrigin = getDistance(featureMatrix[document], new double[NUMBER_OF_DIMENSIONS]);
				if(min[classLabel-1]!=-1)
				if(min[classLabel-1]>distanceFromOrigin){
					min[classLabel-1] = distanceFromOrigin; 
				}
				if(max[classLabel-1]<distanceFromOrigin){
					max[classLabel-1] = distanceFromOrigin;
				}
			}
			// find the min and max among those points
		}
	}
	*/
	/*private double getDistance(double[] point1, double[] point2){
		double distance = 0.0;
		for(int i=0; i< point1.length; i++){
			distance += Math.pow(point1[i] - point2[i], 2);
		}
		return Math.pow(distance, 0.5);
	}*/
}
