package com.mwdb.phase3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EntropyGain {
	public static Node calculateEntropy(List<Double> featureValues,
			Map<Integer, String> labelMap, Map<Integer, Integer> labelHelperMap) {
		Node node = new Node();
		double min = Double.MAX_VALUE;
		double max = -Double.MAX_VALUE;
		double entropyTotal = 0;
		double entropyLeft = 0;
		double entropyRight = 0;
		int range1Count = 0;
		int range2count = 0;
		Map<Integer, Map<String, Integer>> rangeMap = new HashMap<Integer, Map<String, Integer>>();
		for (double value : featureValues) {
			if (min > value) {
				min = value;
			}
		}
		for (double value : featureValues) {
			if (max < value) {
				max = value;
			}
		}

		double mid = (min + max) / 2;
		for (double value : featureValues) {
			if (value >= min && value < mid) {
				if (!rangeMap.containsKey(1)) {
					Map<String, Integer> classMap = new HashMap<String, Integer>();
					int index = featureValues.indexOf(value);
					int gesture = labelHelperMap.get(index);
					String label = labelMap.get(gesture);
					classMap.put(label, 1);
					rangeMap.put(1, classMap);
				} else {
					Map<String, Integer> classMap = rangeMap.get(1);
					int index = featureValues.indexOf(value);
					int gesture = labelHelperMap.get(index);
					String label = labelMap.get(gesture);
					if (!classMap.containsKey(label)) {
						classMap.put(label, 1);
					} else {
						int count = classMap.get(label);
						count++;
						classMap.put(label, count);
					}

					rangeMap.put(1, classMap);
				}
				range1Count++;
			} else if (value < max && value >= mid) {
				if (!rangeMap.containsKey(2)) {
					Map<String, Integer> classMap = new HashMap<String, Integer>();
					int index = featureValues.indexOf(value);
					int gesture = labelHelperMap.get(index);
					String label = labelMap.get(gesture);
					classMap.put(label, 1);
					rangeMap.put(2, classMap);
				} else {
					Map<String, Integer> classMap = rangeMap.get(2);
					int index = featureValues.indexOf(value);

					int gesture = labelHelperMap.get(index);
					String label = labelMap.get(gesture);
					if (!classMap.containsKey(label)) {
						classMap.put(label, 1);
					} else {
						int count = classMap.get(label);
						count++;
						classMap.put(label, count);
					}
					rangeMap.put(2, classMap);
				}
				range2count++;
			}
		}
		if (range1Count == 0 && range2count != 0) {
			for (String classNum : rangeMap.get(2).keySet()) {
				double probability = 0;
				probability = rangeMap.get(2).get(classNum) / range2count;
				if (probability == 1.0) {
					entropyRight = 0;
					node.setClassNameRight(classNum);
					break;
				}
				probability = probability
						* (Math.log(probability) / Math.log(2));
				entropyRight += -probability;

			}
			node.setEntropyRight(entropyRight);
			entropyRight += (range2count / (range1Count + range2count))
					* entropyRight;
			entropyTotal += entropyRight;
			node.setEntropyTotal(entropyTotal);

		} else if (range1Count != 0 && range2count == 0) {
			for (String classNum : rangeMap.get(1).keySet()) {
				double probability = 0;
				probability = rangeMap.get(1).get(classNum) / range1Count;
				if (probability == 1.0) {
					entropyLeft = 0;
					node.setClassNameLeft(classNum);
					break;
				}

				probability = probability
						* (Math.log(probability) / Math.log(2));
				entropyLeft += -probability;
			}
			node.setEntropyLeft(entropyLeft);
			entropyLeft += (range1Count / (range1Count + range2count))
					* entropyLeft;
			entropyTotal += entropyLeft;
			node.setEntropyTotal(entropyTotal);

		} else if (range1Count == 0 && range2count == 0) {
			node.setEntropyLeft(0);
			node.setEntropyRight(0);
			node.setEntropyTotal(0);
			node.setMid(0);
			return node;
		} else {
			for (String classNum : rangeMap.get(1).keySet()) {
				double probability = 0;
				probability = (double) rangeMap.get(1).get(classNum)
						/ (double) range1Count;
				if (probability == 0.1) {
					entropyLeft = 0;
					node.setClassNameLeft(classNum);
					break;
				}

				probability = probability
						* (Math.log(probability) / Math.log(2));
				entropyLeft += -probability;
			}
			node.setEntropyLeft(entropyLeft);
			entropyLeft += ((double) range1Count / (double) (range1Count + range2count))
					* entropyLeft;
			entropyTotal += entropyLeft;
			for (String classNum : rangeMap.get(2).keySet()) {
				double probability = 0;
				probability = (double) rangeMap.get(2).get(classNum)
						/ (double) range2count;
				if (probability >= 0.65) {
					entropyRight = 0;
					node.setClassNameRight(classNum);
					break;
				}
				probability = probability
						* (Math.log(probability) / Math.log(2));
				entropyRight += -probability;

			}
			node.setEntropyRight(entropyRight);
			entropyRight += ((double) range2count / (double) (range1Count + range2count))
					* entropyRight;
			entropyTotal += entropyRight;
			node.setEntropyTotal(entropyTotal);
		}
		node.setMid(mid);
		return node;
	}

	// Left node of for range 1
	// TODO: change labelmap and new map
	public static Node calculateEntropyForChildLeft(Node root,
			List<Double> attributeValues, Map<Integer, String> labelMap,
			Map<Integer, Integer> labelHelperMap) {
		List<Double> prevAttributes = root.getFeatureValues().get(
				root.getIndex());
		double prevMiddle = root.getMid();

		Node node = new Node();
		double min = Double.MAX_VALUE;
		double max = -Double.MAX_VALUE;
		double entropyTotal = 0;
		double entropyLeft = 0;
		double entropyRight = 0;
		int range1Count = 0;
		int range2count = 0;
		Map<Integer, List<Integer>> rangeMap = new HashMap<Integer, List<Integer>>();
		Map<Integer, Map<String, Integer>> rangeClassMap = new HashMap<Integer, Map<String, Integer>>();
		for (double value : attributeValues) {
			if (min > value) {
				min = value;
			}
		}
		for (double value : attributeValues) {
			if (max < value) {
				max = value;
			}
		}

		double mid = (min + max) / 2;
		for (Double double1 : prevAttributes) {
			if (double1 < prevMiddle) {
				int index = prevAttributes.indexOf(double1);
				if (!rangeMap.containsKey(1)) {
					//range1Count++;
					List<Integer> values = new ArrayList<Integer>();
					values.add(index);
					rangeMap.put(1, values);
				} else {
					List<Integer> values = rangeMap.get(1);
					values.add(index);
					rangeMap.put(1, values);
				}
			}
		}
		//System.out.println("mid:" + mid);

		if (!rangeMap.isEmpty()) {
			// int[] range1Array = new int[2];
			for (Integer range : rangeMap.keySet()) {
				List<Integer> rangeList = rangeMap.get(range);
				for (Integer integer : rangeList) {
					double value = attributeValues.get(integer);
					if (value < mid) {
						range1Count++;
						if (!rangeClassMap.containsKey(1)) {
							Map<String, Integer> classMap = new HashMap<String, Integer>();
							int index = attributeValues.indexOf(value);
							int gesture = labelHelperMap.get(index);
							String label = labelMap.get(gesture);

							classMap.put(label, 1);
							rangeClassMap.put(1, classMap);
						} else {
							Map<String, Integer> classMap = rangeClassMap
									.get(1);

							int index = attributeValues.indexOf(value);
							int gesture = labelHelperMap.get(index);
							String label = labelMap.get(gesture);
							if (!classMap.containsKey(label)) {
								classMap.put(label, 1);
							} else {
								int count = classMap.get(label);
								count++;
								classMap.put(label, count);

							}
						}
					} else {
						range2count++;
						if (!rangeMap.containsKey(2)) {
							Map<String, Integer> classMap = new HashMap<String, Integer>();
							int index = attributeValues.indexOf(value);
							int gesture = labelHelperMap.get(index);
							String label = labelMap.get(gesture);
							classMap.put(label, 1);
							rangeClassMap.put(2, classMap);
						} else {
							Map<String, Integer> classMap = rangeClassMap
									.get(2);

							int index = attributeValues.indexOf(value);
							int gesture = labelHelperMap.get(index);
							String label = labelMap.get(gesture);
							if (!classMap.containsKey(label)) {
								classMap.put(label, 1);
							} else {
								int count = classMap.get(label);
								count++;
								classMap.put(label, count);
							}
						}
					}
				}
			}
		}
		if (range1Count == 0 && range2count != 0) {
			// use Only entropy right
			for (String classNum : rangeClassMap.get(2).keySet()) {
				double probability = 0;
				probability = (double)rangeClassMap.get(2).get(classNum) / (double)range2count;
				if (probability >= 0.75) {
					entropyRight = 0;
					node.setClassNameRight(classNum);
				}
				probability = probability
						* (Math.log(probability) / Math.log(2));
				entropyRight += -probability;
			}
			node.setEntropyRight(entropyRight);

			entropyRight += ((double)range2count / (double)(range1Count + range2count))
					* entropyRight;
			entropyTotal += entropyRight;

		} else if (range1Count != 0 && range2count == 0) {
			// use only left
			for (String classNum : rangeClassMap.get(1).keySet()) {
				double probability = 0;
				probability = (double)rangeClassMap.get(1).get(classNum) / (double)range1Count;
				if (probability >= 0.75) {
					entropyLeft = 0;
					node.setClassNameLeft(classNum);
				}
				probability = probability
						* (Math.log(probability) / Math.log(2));
				entropyLeft += -probability;
			}
			node.setEntropyLeft(entropyLeft);

			entropyLeft += ((double)range1Count / (double)(range1Count + range2count))
					* entropyLeft;
			entropyTotal += entropyLeft;

		} else if (range1Count != 0 && range2count != 0) {
			for (String classNum : rangeClassMap.get(1).keySet()) {
				double probability = 0;
				probability = rangeClassMap.get(1).get(classNum) / range1Count;
				if (probability >= 0.75) {
					entropyLeft = 0;
					node.setClassNameLeft(classNum);
				}
				probability = probability
						* (Math.log(probability) / Math.log(2));
				entropyLeft += -probability;
			}
			node.setEntropyLeft(entropyLeft);

			entropyLeft += ((double)range1Count / (double)(range1Count + range2count))
					* entropyLeft;
			entropyTotal += entropyLeft;
			for (String classNum : rangeClassMap.get(2).keySet()) {
				double probability = 0;
				probability = (double)rangeClassMap.get(2).get(classNum) / (double)range2count;
				if (probability >= 0.75) {
					entropyRight = 0;
					node.setClassNameRight(classNum);
				}
				probability = probability
						* (Math.log(probability) / Math.log(2));
				entropyRight += -probability;
			}
			node.setEntropyRight(entropyRight);

			entropyRight += ((double)range2count / (double)(range1Count + range2count))
					* entropyRight;
			entropyTotal += entropyRight;

		} else {
			node.setEntropyLeft(0);
			node.setEntropyRight(0);
			node.setEntropyTotal(0);

			node.setMid(mid);
			return node;

		}
		node.setEntropyTotal(entropyTotal);

		node.setMid(mid);
		return node;
	}

	public static Node calculateEntropyForChildRight(Node root,
			List<Double> attributeValues, Map<Integer, String> labelMap,
			Map<Integer, Integer> labelHelperMap) {
		List<Double> prevAttributes = root.getFeatureValues().get(
				root.getIndex());
		double prevMiddle = root.getMid();

		Node node = new Node();
		double min = Double.MAX_VALUE;
		double max = -Double.MAX_VALUE;
		double entropyTotal = 0;
		double entropyLeft = 0;
		double entropyRight = 0;
		int range1Count = 0;
		int range2count = 0;
		Map<Integer, List<Integer>> rangeMap = new HashMap<Integer, List<Integer>>();
		Map<Integer, Map<String, Integer>> rangeClassMap = new HashMap<Integer, Map<String, Integer>>();
		for (double value : attributeValues) {
			if (min > value) {
				min = value;
			}
		}
		for (double value : attributeValues) {
			if (max < value) {
				max = value;
			}
		}

		double mid = (min + max) / 2;
		for (Double double1 : prevAttributes) {
			if (double1 > prevMiddle) {
				int index = prevAttributes.indexOf(double1);
				if (!rangeMap.containsKey(2)) {
					//range1Count++;
					List<Integer> values = new ArrayList<Integer>();
					values.add(index);
					rangeMap.put(2, values);
				} else {
					List<Integer> values = rangeMap.get(2);
					values.add(index);
					rangeMap.put(2, values);
				}
			}
		}

		if (!rangeMap.isEmpty()) {
			for (Integer range : rangeMap.keySet()) {
				List<Integer> rangeList = rangeMap.get(range);
				for (Integer integer : rangeList) {
					double value = attributeValues.get(integer);
					if (value < mid) {
						range1Count++;
						if (!rangeClassMap.containsKey(1)) {
							Map<String, Integer> classMap = new HashMap<String, Integer>();
							int index = attributeValues.indexOf(value);
							int gesture = labelHelperMap.get(index);
							String label = labelMap.get(gesture);
							classMap.put(label, 1);
							rangeClassMap.put(1, classMap);
						} else {
							Map<String, Integer> classMap = rangeClassMap
									.get(1);

							int index = attributeValues.indexOf(value);
							int gesture = labelHelperMap.get(index);
							String label = labelMap.get(gesture);
							if (!classMap.containsKey(label)) {
								classMap.put(label, 1);
							} else {
								int count = classMap.get(label);
								count++;
								classMap.put(label, count);

							}
						}
					} else {
						range2count++;
						if (!rangeClassMap.containsKey(2)) {
							Map<String, Integer> classMap = new HashMap<String, Integer>();
							int index = attributeValues.indexOf(value);
							int gesture = labelHelperMap.get(index);
							String label = labelMap.get(gesture);
							classMap.put(label, 1);
							rangeClassMap.put(2, classMap);
						} else {
							Map<String, Integer> classMap = rangeClassMap
									.get(2);

							int index = attributeValues.indexOf(value);
							int gesture = labelHelperMap.get(index);
							String label = labelMap.get(gesture);
							if (!classMap.containsKey(label)) {
								classMap.put(label, 1);
							} else {
								int count = classMap.get(label);
								count++;
								classMap.put(label, count);

							}
						}
					}
				}
			}
		}
		if (range1Count == 0 && range2count != 0) {
			// use Only entropy right
			for (String classNum : rangeClassMap.get(2).keySet()) {
				double probability = 0;
				probability = (double)rangeClassMap.get(2).get(classNum) / (double)range2count;
				if (probability >= 0.75) {
					entropyRight = 0;
					node.setClassNameRight(classNum);
					break;
				}
				probability = probability
						* (Math.log(probability) / Math.log(2));
				entropyRight += -probability;

			}
			node.setEntropyRight(entropyRight);
			entropyRight += ((double)range2count / (double)(range1Count + range2count))
					* entropyRight;

		} else if (range1Count != 0 && range2count == 0) {
			// use only left
			for (String classNum : rangeClassMap.get(1).keySet()) {
				double probability = 0;
				probability = rangeClassMap.get(1).get(classNum) / range1Count;
				if (probability >= 0.75) {
					entropyLeft = 0;
					node.setClassNameLeft(classNum);
					break;
				}
				probability = probability
						* (Math.log(probability) / Math.log(2));
				entropyLeft += -probability;
			}
			node.setEntropyLeft(entropyLeft);
			entropyLeft += ((double)range1Count /(double) (range1Count + range2count))
					* entropyLeft;
			entropyTotal += entropyLeft;

		} 
		else if(range1Count == 0 && range2count == 0){
			node.setEntropyLeft(0);
			node.setEntropyRight(0);
			node.setMid(mid);
			return node;
		}
		else {
			for (String classNum : rangeClassMap.get(1).keySet()) {
				double probability = 0;
				probability = (double)rangeClassMap.get(1).get(classNum) / (double)range1Count;
				if (probability >= 0.75) {
					entropyLeft = 0;
					node.setClassNameLeft(classNum);
					break;
				}
				probability = probability
						* (Math.log(probability) / Math.log(2));
				entropyLeft += -probability;
			}
			node.setEntropyLeft(entropyLeft);
			entropyLeft += ((double)range1Count / (double)(range1Count + range2count))
					* entropyLeft;
			entropyTotal += entropyLeft;

			for (String classNum : rangeClassMap.get(2).keySet()) {
				double probability = 0;
				probability = (double)rangeClassMap.get(2).get(classNum) / (double)range2count;
				if (probability >= 0.75) {
					entropyRight = 0;
					node.setClassNameRight(classNum);
					break;
				}
				probability = probability
						* (Math.log(probability) / Math.log(2));
				entropyRight += -probability;

			}
			node.setEntropyRight(entropyRight);
			entropyRight += ((double)range2count / (double)(range1Count + range2count))
					* entropyRight;

		}
		node.setEntropyTotal(entropyTotal);
		node.setMid(mid);
		return node;
	}

	/*
	 * 
	 * public static Node calculateEntropyForChild(Node root, List<Double>
	 * attributeValues, Map<Integer, Integer> labelMap) { List<Double>
	 * prevAttributes = root.getFeatureValues().get( root.getIndex()); double
	 * prevMiddle = root.getMid();
	 * 
	 * Node node = new Node(); double min = Double.MAX_VALUE; double max =
	 * Double.MIN_VALUE; double entropy = 0; int range1Count = 0; int
	 * range2count = 0; Map<Integer, List<Integer>> rangeMap = new
	 * HashMap<Integer, List<Integer>>(); Map<Integer, Map<Integer, Integer>>
	 * rangeClassMap = new HashMap<Integer, Map<Integer, Integer>>(); for
	 * (double value : attributeValues) { if (min > value) { min = value; } }
	 * for (double value : attributeValues) { if (max < value) { max = value; }
	 * }
	 * 
	 * double mid = (min + max) / 2; for (Double double1 : prevAttributes) { if
	 * (double1 < prevMiddle) { int index = prevAttributes.indexOf(double1); if
	 * (!rangeMap.containsKey(1)) { List<Integer> values = new
	 * ArrayList<Integer>(); values.add(index); rangeMap.put(1, values); } else
	 * { List<Integer> values = rangeMap.get(1); values.add(index);
	 * rangeMap.put(1, values); } } else { int index =
	 * prevAttributes.indexOf(double1); if (!rangeMap.containsKey(2)) {
	 * List<Integer> values = new ArrayList<Integer>(); values.add(index);
	 * rangeMap.put(2, values); } else { List<Integer> values = rangeMap.get(2);
	 * values.add(index); rangeMap.put(2, values); } } } int[] range1Array = new
	 * int[2]; int[] range2Array = new int[2]; for (Integer range :
	 * rangeMap.keySet()) { List<Integer> rangeList = rangeMap.get(range); for
	 * (Integer integer : rangeList) { double value =
	 * attributeValues.get(integer); if (value < mid) { if (range == 1) {
	 * range1Array[0]++; } else { range2Array[0]++; } if
	 * (!rangeClassMap.containsKey(1)) { Map<Integer, Integer> classMap = new
	 * HashMap<Integer, Integer>(); int index = attributeValues.indexOf(value);
	 * int label = labelMap.get(index); classMap.put(label, 1);
	 * rangeClassMap.put(1, classMap); } else { Map<Integer, Integer> classMap =
	 * rangeClassMap.get(1); int index = attributeValues.indexOf(value); int
	 * label = labelMap.get(index); int count = classMap.get(label); count++;
	 * classMap.put(label, count); } } else { if (range == 1) {
	 * range1Array[1]++; } else { range2Array[1]++; }
	 * 
	 * if (!rangeMap.containsKey(2)) { Map<Integer, Integer> classMap = new
	 * HashMap<Integer, Integer>(); int index = attributeValues.indexOf(value);
	 * int label = labelMap.get(index); classMap.put(label, 1);
	 * rangeClassMap.put(2, classMap); } else { Map<Integer, Integer> classMap =
	 * rangeClassMap.get(2); int index = attributeValues.indexOf(value); int
	 * label = labelMap.get(index); int count = classMap.get(label); count++;
	 * classMap.put(label, count); } } }
	 * 
	 * } double[] entropyVal = new double[2]; for (int i = 0; i < 2; i++) { for
	 * (Integer classNum : rangeClassMap.get(1).keySet()) { double probability =
	 * 0; probability = rangeMap.get(1).get(classNum) / range1Array[i];
	 * probability = probability (Math.log(probability) / Math.log(2));
	 * entropyVal[i] += -probability; } entropyVal[i] += (range1Array[i] /
	 * (range1Array[i] + range2Array[i])) entropyVal[i]; for (Integer classNum :
	 * rangeClassMap.get(2).keySet()) { double probability = 0; probability =
	 * rangeMap.get(2).get(classNum) / range2Array[i]; probability = probability
	 * (Math.log(probability) / Math.log(2)); entropyVal[i] += -probability; }
	 * entropyVal[i] += (range2Array[i] / (range1Array[i] + range2Array[i]))
	 * entropyVal[i]; } EntropyUtil util = new EntropyUtil();
	 * util.setEntropyVal1(entropyVal[0]); util.setEntropyVal2(entropyVal[1]);
	 * node.setUtil(util); node.setMid(mid); return node; }
	 */
	public static double calculateInfoGain(
			Map<Integer, Integer> featureEntropyMap, double rootEntropy) {

		return 0;
	}
}
