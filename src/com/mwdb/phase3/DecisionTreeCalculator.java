package com.mwdb.phase3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class DecisionTreeCalculator {
	Map<Integer, String> labelMap = new HashMap<Integer, String>();
	Map<Integer, Integer> labelHelperMap = new HashMap<Integer, Integer>();
	Map<Integer, String> finalLabel = new HashMap<Integer, String>();
	Map<Integer, Integer> finalClassified = new HashMap<Integer, Integer>();
    
	public Map<Integer, Integer> getFinalClassified() {
		return finalClassified;
	}
	public void setFinalClassified(Map<Integer, Integer> finalClassified) {
		this.finalClassified = finalClassified;
	}
	public Map<Integer, String> getFinalLabel() {
		return finalLabel;
	}

	public void setFinalLabel(Map<Integer, String> finalLabel) {
		this.finalLabel = finalLabel;
	}

	public Map<Integer, Integer> getLabelHelperMap() {
		return labelHelperMap;
	}

	public void setLabelHelperMap(Map<Integer, Integer> labelHelperMap) {
		this.labelHelperMap = labelHelperMap;
	}

	public Map<Integer, String> getLabelMap() {
		return labelMap;
	}

	public void setLabelMap(Map<Integer, String> labelMap) {
		this.labelMap = labelMap;
	}

	public DecisionTreeCalculator(Map<Integer, String> labelMap,
			Map<Integer, Integer> labelHelperMap) {
		this.labelMap = labelMap;
		this.labelHelperMap = labelHelperMap;
	}
	public Map<Integer, List<Double>> generateFeatureListFromInputTestMatrix(Map<Integer, Integer>labelHelper, double[][] inputVector){
		Map<Integer, List<Double>> gesturesMap = new HashMap<Integer,List<Double>>();
		for (int i = 0; i < inputVector.length; i++) {
			int gesture = labelHelper.get(i);
			
			for (int j = 0; j < inputVector[0].length; j++) {
				if(!gesturesMap.containsKey(gesture)){
					List<Double> list = new ArrayList<Double>();
					list.add(inputVector[i][i]);
					gesturesMap.put(gesture, list);
				}
				else{
					List<Double> list = gesturesMap.get(gesture);
					list.add(inputVector[i][j]);
				}
			}
		}
		return gesturesMap;
	}
	public Map<Integer, List<Double>> generateFeatureListFromInputMatrix(
			double[][] inputVector) {
		Map<Integer, List<Double>> featureListMap = new HashMap<Integer, List<Double>>();
		for (int i = 0; i < inputVector[0].length; i++) {
			List<Double> featuresList = new ArrayList<Double>();
			for (int j = 0; j < inputVector.length; j++) {
				featuresList.add(inputVector[j][i]);
			}
			featureListMap.put(i + 1, featuresList);
		}
		return featureListMap;
	}

	public Node generateAttributeValuesForRoot(
			Map<Integer, List<Double>> featuresMap) {
		double prevEntropy = 1;
		int gain = -1;
		double maxInfoGain = -Double.MAX_VALUE;
		Node n = null;
		Node finalNode = null;
		System.out.println("max gain");
		for (Integer feature : featuresMap.keySet()) {
			n = EntropyGain.calculateEntropy(featuresMap.get(feature),
					labelMap, labelHelperMap);
			double infoGain = prevEntropy - n.getEntropyTotal();
			if (maxInfoGain < infoGain) {
				maxInfoGain = infoGain;
				gain = feature;
				finalNode = n;
			}
		}
		Map<Integer, List<Double>> nodeFeatureMap = new HashMap<Integer, List<Double>>();
		nodeFeatureMap.put(gain, featuresMap.get(gain));
		finalNode.setIndex(gain);
		System.out.println("Feature selected :" + gain);
		finalNode.setFeatureValues(nodeFeatureMap);
		return finalNode;
	}
	public void printLabels(){
		for(Integer gesture : finalLabel.keySet()){
			System.out.println("Gesture "+gesture + ":" + finalLabel.get(gesture));
		}
	}
	public void traverseTree(Node root,
			Map<Integer, List<Double>> featuresRowMap) {
		for (Integer gesture : featuresRowMap.keySet()) {
			List<Double> attributeValues = featuresRowMap.get(gesture);
			traverseTreeRecursive(root, attributeValues, gesture);
		}
	}

	public void traverseTreeRecursive(Node root, List<Double> attributeValues,
			Integer gesture) {
		int index = root.getIndex();
		double value = attributeValues.get(index);
		if (value < root.getMid()) {
			if (root.getClassNameLeft() != null) {
				finalClassified.put(gesture, index);
				finalLabel.put(gesture, root.getClassNameLeft());
				root.setClassifiedIndex(new HashMap<Integer, Integer>(gesture , index));
			} else {
				traverseTreeRecursive(root.rangeLeft, attributeValues, gesture);
			}
		} else {
			if (root.getClassNameRight() != null) {
				finalClassified.put(gesture, index);

				finalLabel.put(gesture, root.getClassNameRight());
				root.setClassifiedIndex(new HashMap<Integer, Integer>(gesture , index));
			} else {
				traverseTreeRecursive(root.rangeRight, attributeValues, gesture);
			}
		}
	}

	public Node buildDecisionTree(Node root,
			Map<Integer, List<Double>> featuresMap) {
		// TODO: update the return condition to left and right class name
		System.out.println("Root feature Index"+ root.getIndex());
		if(featuresMap.isEmpty())
			return root;
		if (root.getClassNameLeft() != null && root.getClassNameRight() != null) {
			//System.out.println("Class left:"+root.getClassNameLeft()+" "+"Class right :"+root.getClassNameRight());
			return root;
		}
		Node finalNode = null;
		double bestGain = -Double.MAX_VALUE;
		int featureFinal = 0;
		Map<Integer, List<Double>> featureMap;
		if (root.getClassNameLeft() == null) {
			for (Integer feature : featuresMap.keySet()) {
				Node node = EntropyGain.calculateEntropyForChildLeft(root,
						featuresMap.get(feature), labelMap, labelHelperMap);

				double gain = root.getEntropyLeft() - node.getEntropyTotal();
				if (gain > bestGain) {
					bestGain = gain;
					finalNode = node;
					featureFinal = feature;
				}
			}
			finalNode.setIndex(featureFinal);
			featureMap = new HashMap<Integer, List<Double>>();
			featureMap.put(featureFinal, featuresMap.get(featureFinal));
			finalNode.setFeatureValues(featureMap);
			featuresMap.remove(featureFinal);
			root.rangeLeft = buildDecisionTree(finalNode, featuresMap);
		}
		else

			//System.out.println("Class left:"+root.getClassNameLeft());
		bestGain = -Double.MAX_VALUE;

		if (root.getClassNameRight() == null) {
			for (Integer feature : featuresMap.keySet()) {
				Node node = EntropyGain.calculateEntropyForChildRight(root,
						featuresMap.get(feature), labelMap, labelHelperMap);
				double gain = root.getEntropyRight() - node.getEntropyTotal();
				if (gain > bestGain) {
					bestGain = gain;
					finalNode = node;
					featureFinal = feature;
				}
			}
			finalNode.setIndex(featureFinal);
			featureMap = new HashMap<Integer, List<Double>>();
			featureMap.put(featureFinal, featuresMap.get(featureFinal));
			finalNode.setFeatureValues(featureMap);
			featuresMap.remove(featureFinal);
			root.rangeRight = buildDecisionTree(finalNode, featuresMap);
		}

		//	System.out.println("Class right :"+root.getClassNameRight());
		return root;
	}

}
