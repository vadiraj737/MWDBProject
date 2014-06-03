package com.mwdb.phase3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.apache.commons.math3.distribution.NormalDistribution;

public class LSH {
	public static  int l = 0;
	public static  int k = 0 ;
	public static final double w = 0.5;
	public static final int NO_OF_BUCKETS = 2;
	public static double[] bucketsRange = new double[NO_OF_BUCKETS];
	public static Map<String, ArrayList<String>> indexMap = new HashMap<String, ArrayList<String>>();
	public static double mean  = 0.0;
	public static double standardDeviation = 0.0;
	
	
	public void calculateHashFunctions(double[][] inputVectors, Map<Integer,String> indexGestureMap,int inputk,int inputl){
		k = inputk;
		l = inputl;
		inputVectors = normalizeInputValues(inputVectors);
		double mean = inputVectors[0].length /2.0;
		double stdDeviation = inputVectors[0].length/6.0;
		String key = "";
		 for (int i = 0; i < inputVectors.length; i++) {
			//	for (int j = 0; j < inputVectors[i].length; j++) {
					
		for (int i1 = 0; i1 <l ; i1++) {
				key = "L"+i1+"_";
			for (int j1 = 0; j1 <k ; j1++) {
				//double x = inputVectors[i];
				NormalDistribution n = new NormalDistribution(mean,stdDeviation);
				double r = n.sample();
				r = Math.abs(r);
				int r1 = (int) Math.ceil(r);
				if(r1 > inputVectors[i].length -1 ){
					r1 = 10;
				}
				double b = generateRandomNum();
				double val = (inputVectors[i][r1] + b )/w;
				int bucketNo = findBucket(val);
				key+=bucketNo;
				}// j1 for 
			if(indexMap.containsKey(key)){
				indexMap.get(key).add(indexGestureMap.get(i));
			}
			else {
				ArrayList<String> list = new ArrayList<String>();
				list.add(indexGestureMap.get(i));
				indexMap.put(key, list);
			}
			}// i1 for
		//}// j for
		
	} // i for 
		 Iterator<String> itr  = indexMap.keySet().iterator();
		 while(itr.hasNext()){
			 String key1 = itr.next();
			 System.out.print("key :"+key1+": ");
			 ArrayList<String> list = indexMap.get(key1);
			 for (int i = 0; i < list.size(); i++) {
				System.out.print(list.get(i) +",");
			}
			 System.out.println();
		 }
		 System.out.println("End of calculateHashFunctions");
	}	
	
	public double generateRandomNum(){
		double min = 0;
		double max = w;
		Random r = new Random();
		double randomValue = min + (max - min) * r.nextDouble();
		//num  = min + (int)(Math.random() * ((max - min) + 1));
		return randomValue;
			
	}
	
	public void calculateBucketRange(){
		double split = 0.5 ;
		for (int i = 0; i < bucketsRange.length; i++) {
			if(i==0){
				bucketsRange[i] = split ;
			}else{
			bucketsRange[i] = bucketsRange[i-1] + split;
			}
		}
	}
	public int findBucket(double val){
		for (int i = 0; i < bucketsRange.length; i++) {
			if(val <= bucketsRange[i]){
				return i;
			}
		}
		return 0;
	}
	public ArrayList<String> calculateHashFunctionsForQuery(double[] queryArray){
		double[] queryArr = normalizeQueryValues(queryArray);
		double mean  = 120;
		double stdDeviation = 60;
		String key = "";
		ArrayList<String> list = new ArrayList<String>();
		for (int i1 = 0; i1 <l ; i1++) {
			key = "L"+i1+"_";
		for (int j1 = 0; j1 <k ; j1++) {
			
			NormalDistribution n = new NormalDistribution(mean,stdDeviation);
			double r = n.sample();
			r = Math.abs(r);
			int r1 =  (int) Math.ceil(r);
			if(r1 > queryArray.length -1 ){
				r1 = 10;
			}
			double b = generateRandomNum();
			double val = (queryArr[r1] + b )/w;
			int bucketNo = findBucket(val);
			key+=bucketNo;
			}// j1 for 
		list.add(key);
		}// i1 for
		System.out.println("end of calculateHashFunctionsForQuery");
		return list;
	
	}
	
	public Set<String> findNearestGestures(ArrayList<String> queryList){
		Set<String> gestureSet = new HashSet<String>();
		for (int i = 0; i < queryList.size(); i++) {
			if(indexMap.containsKey(queryList.get(i))){
				for (int j = 0; j < indexMap.get(queryList.get(i)).size(); j++) {
					gestureSet.add(indexMap.get(queryList.get(i)).get(j));
				}
			}
		}
		return gestureSet;
	}
	
	public double[][] normalizeInputValues(double[][] inputVectors){
		double min = inputVectors[0][0];
		double max = inputVectors[0][0];
		for (int i = 0; i < inputVectors.length; i++) {
			for (int j = 0; j < inputVectors[i].length; j++) {
				if(inputVectors[i][j] < min){
					min = inputVectors[i][j];
				}
				else if (inputVectors[i][j] > max) {
					max = inputVectors[i][j];
				}
			}
		}
		max-=min;
		for (int i = 0; i < inputVectors.length; i++) {
			for (int j = 0; j < inputVectors[i].length; j++) {
				inputVectors[i][j]-= min; 
				
				inputVectors[i][j]/= max;
				if(inputVectors[i][j] >1.0 && inputVectors[i][j] < -1.0){
					System.out.println(inputVectors[i][j]);
				}
			}
		}
		return inputVectors;
	}
	
	public double[] normalizeQueryValues(double[] queryArr){
		if(queryArr != null){
		double min = queryArr[0];
		double  max = queryArr[0];
		
		for (int i = 0; i < queryArr.length; i++) {
			if(queryArr[i] < min){
				min = queryArr[i];
			}else if (queryArr[i] > max) {
				max = queryArr[i];
			}
			
		}
		max-=min;
		for (int j = 0; j < queryArr.length; j++) {
			queryArr[j]-= min; 
			
			queryArr[j]/= max;
			if(queryArr[j] >1.0 && queryArr[j] < -1.0){
				System.out.println(queryArr[j]);
			}
		}
		}
		return queryArr;
	}
	
}
