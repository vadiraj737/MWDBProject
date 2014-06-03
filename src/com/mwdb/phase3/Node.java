package com.mwdb.phase3;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mwdb.util.EntropyUtil;

public class Node {
	//private Node parent;
	EntropyUtil util;
	public double entropyTotal;
	public double entropyLeft;
	public double entropyRight;
	public Map<Integer, Integer> classifiedIndex = new HashMap<Integer, Integer>();
	public String classNameLeft;
	public String classNameRight;
	
	public int index;
	public Map<Integer, List<Double>> featureValues;
	public double mid;
	public Node rangeLeft;
	public Node rangeRight;
	public Node(){
		//this.parent = null;
		this.util =null;
		this.setEntropyTotal(-1);
		this.setEntropyLeft(-1);
		this.setEntropyRight(-1);
		this.featureValues = null;
		this.mid = 0;
		this.rangeLeft = null;
		this.rangeRight = null;
		this.entropyLeft = 0.0;
		this.entropyRight = 0.0;
		this.classNameLeft = null;
		this.classNameRight = null;
	}
	
	public Map<Integer, Integer> getClassifiedIndex() {
		return classifiedIndex;
	}
	public void setClassifiedIndex(Map<Integer, Integer> classifiedIndex) {
		this.classifiedIndex = classifiedIndex;
	}
	
	public double getEntropyTotal() {
		return entropyTotal;
	}
	public void setEntropyTotal(double entropyTotal) {
		this.entropyTotal = entropyTotal;
	}
	
	public double getEntropyLeft() {
		return entropyLeft;
	}
	public double getEntropyRight() {
		return entropyRight;
	}
	public void setEntropyLeft(double entropyLeft) {
		this.entropyLeft = entropyLeft;
	}
	public void setEntropyRight(double entropyRight) {
		this.entropyRight = entropyRight;
	}
	public String getClassNameLeft() {
		return classNameLeft;
	}
	public String getClassNameRight() {
		return classNameRight;
	}
	public void setClassNameLeft(String classNameLeft) {
		this.classNameLeft = classNameLeft;
	}
	public void setClassNameRight(String classNameRight) {
		this.classNameRight = classNameRight;
	}
	
	
	public EntropyUtil getUtil() {
		return util;
	}
	public void setUtil(EntropyUtil util) {
		this.util = util;
	}
	
	public int getIndex() {
		return index;
	}
	
	public void setIndex(int index) {
		this.index = index;
	}
	
	/*public double getEntropy() {
		return entropy;
	}
	public void setEntropy(double entropy) {
		this.entropy = entropy;
	}*/
	public void setFeatureValues(Map<Integer, List<Double>> featureValues2) {
		this.featureValues = featureValues2;
	}
	public Map<Integer, List<Double>> getFeatureValues() {
		return featureValues;
	}
	
	public double getMid() {
		return mid;
	}
	public void setMid(double mid) {
		this.mid = mid;
	}
	public Node getRangeLeft() {
		return rangeLeft;
	}
	public void setRangeLeft(Node rangeLeft) {
		this.rangeLeft = rangeLeft;
	}
	public Node getRangeRight() {
		return rangeRight;
	}
	public void setRangeRight(Node rangeRight) {
		this.rangeRight = rangeRight;
	}
	

}
