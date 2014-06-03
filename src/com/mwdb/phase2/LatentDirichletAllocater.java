package com.mwdb.phase2;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import matlabcontrol.MatlabInvocationException;
import matlabcontrol.MatlabProxy;

import com.mwdb.phase1.Word;
import com.mwdb.util.PrecomputeUtil;


public class LatentDirichletAllocater {
	public  Map<Integer, Map<String, Map<String, Word>>> precomputedSVDMap;
	private Map<Integer, Map<String, Map<Integer, Double>>> vMatrixForAllSensors;
	public LatentDirichletAllocater(PrecomputeUtil pUtil){
		this.precomputedSVDMap = pUtil.getPrecomputedSVDMap();
		vMatrixForAllSensors = new HashMap<Integer, Map<String, Map<Integer, Double>>>();
	}
	public Map<Integer, Map<String, Map<Integer, Double>>> getvMatrixForAllSensors() {
		return vMatrixForAllSensors;
	}

	public void setvMatrixForAllSensors(
			Map<Integer, Map<String, Map<Integer, Double>>> vMatrixForAllSensors) {
		this.vMatrixForAllSensors = vMatrixForAllSensors;
	}

	public Map<String, Map<Integer, Double>> getWSAndDS(int sensorNumber, MatlabProxy matlabProxy){
		Map<String, Map<String, Word>> sensorMap = precomputedSVDMap.get(sensorNumber);
		Map<String, Integer> termIndex = new HashMap<String, Integer>();
		Map<String, Integer> documentIndex = new HashMap<String, Integer>();
		Map<String, Map<Integer, Double>> vMatrix = new HashMap<String, Map<Integer, Double>>();
		int termCount = 1;
		int docCount = 1;
		try {
			File file = new File("importLDA"+sensorNumber+".txt");
			// if file doesn't exists, then create it
			if (!file.exists()) {
				file.createNewFile();
			}
			FileWriter fw = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			for(String gestureName : sensorMap.keySet()){
				Map<String, Word> termMap = sensorMap.get(gestureName);
				if(!documentIndex.containsKey(gestureName)){
					documentIndex.put(gestureName, docCount++);
				}
				for(String termName: termMap.keySet()){
					Word word = termMap.get(termName);
					if(!termIndex.containsKey(termName)){
						termIndex.put(termName, termCount++);
					}
					int tfVal = (int)word.getRawTF();
					bw.write(""+documentIndex.get(gestureName)+" "+termIndex.get(termName)+" "+tfVal+"\n");
				}
			}
			bw.close();
			try {
				int numberOfDimensions = 3;
				Object[] objArray = matlabProxy.returningFeval("importworddoccounts", 2, "importLDA"+sensorNumber+".txt");
				objArray = matlabProxy.returningFeval("GibbsSamplerLDA", numberOfDimensions, objArray[0], objArray[1], 3, 100, 1, 0.01, 3, 1);
				double[] wordLatent = (double[])objArray[0];
				System.out.println("WP size:"+termIndex.size());
				int numberOfUniqueWords = wordLatent.length/numberOfDimensions;
				//double vMatrix[][] = new double[numberOfUniqueWords][3];
				
				for(int i=0; i<numberOfUniqueWords;i++){
					for(int j=0; j<numberOfDimensions;j++){
						String wordName="";
						for(String word: termIndex.keySet()){
							if(termIndex.get(word)==i+1)
								wordName = word;
						}
						//System.out.print(wordName+":"+wordLatent[i+j*numberOfUniqueWords]+"\t");
						if(!vMatrix.containsKey(wordName)){
							vMatrix.put(wordName, new HashMap<Integer, Double>());
						}
						if(Double.isNaN(wordLatent[i+j*numberOfUniqueWords])){
							vMatrix.get(wordName).put(j, 0.005);
						}
						else
							vMatrix.get(wordName).put(j, wordLatent[i+j*numberOfUniqueWords]);
					}
					//System.out.println();
				}
				System.out.println("Printing values for sensor:"+sensorNumber);
				vMatrixForAllSensors.put(sensorNumber, normalizeDVMtrix(vMatrix));
			}catch (MatlabInvocationException e) {
				e.printStackTrace();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("Printing DV Matrix for sensor "+sensorNumber);
		Map<String, Map<Integer, Double>> dVMatrix = new HashMap<String, Map<Integer, Double>>();
		for(String gesture: sensorMap.keySet()){
			dVMatrix.put(gesture, new HashMap<Integer, Double>());
			for(int i=0; i<3;i++){
				Map<String, Word> gestureRow = sensorMap.get(gesture);
				double matrixValue = 0.0;
				for(String word: gestureRow.keySet()){
					if(vMatrix.containsKey(word)){
						matrixValue += (gestureRow.get(word).getRawTF()*vMatrix.get(word).get(i));
					}
				}
				//System.out.print(""+matrixValue+" ");
				dVMatrix.get(gesture).put(i, matrixValue);
			}
			//System.out.println();
		}
		return dVMatrix;
	}
	private Map<String, Map<Integer, Double>> normalizeDVMtrix(
			Map<String, Map<Integer, Double>> dVMatrix) {
		double columnTotal[] = new  double[3];
		for(String word: dVMatrix.keySet()){
			columnTotal[0] = columnTotal[0] + dVMatrix.get(word).get(0);
			columnTotal[1] = columnTotal[1] + dVMatrix.get(word).get(1);
			columnTotal[2] = columnTotal[2] + dVMatrix.get(word).get(2);
		}
		System.out.println("Printing Normalized Values:");
		for(String word: dVMatrix.keySet()){
			double k1 = dVMatrix.get(word).get(0);
			double k2 = dVMatrix.get(word).get(1);
			double k3 = dVMatrix.get(word).get(2);
			k1 = k1/columnTotal[0];
			k2 = k2/columnTotal[1];
			k3 = k3/columnTotal[2];
			dVMatrix.get(word).put(0, k1);
			dVMatrix.get(word).put(1, k1);
			dVMatrix.get(word).put(2, k1);
			System.out.println(word+"\t"+k1+"\t"+k2+"\t"+k3);
		}
		return dVMatrix;
	}
	public Map<String, Map<Integer, Map<Integer, Double>>> allFiles(MatlabProxy matlabProxy){
		Map<Integer, Map<String, Map<Integer, Double>>> allDVMatrix = new HashMap<Integer, Map<String, Map<Integer, Double>>>();
		for(int i=0;i<20;i++){
				allDVMatrix.put(i, getWSAndDS(i, matlabProxy));
			}
		return createGestureSensorMap(allDVMatrix);
	}
	
	private Map<String, Map<Integer, Map<Integer, Double>>> createGestureSensorMap(Map<Integer, Map<String, Map<Integer, Double>>> allDVMatrix){
		Map<String, Map<Integer, Map<Integer, Double>>> gestureSensorData = new HashMap<String, Map<Integer, Map<Integer, Double>>>();
		for(Integer sensor: allDVMatrix.keySet()){
			for(String gesture: allDVMatrix.get(sensor).keySet()){
				String newGestureName = gesture.substring(gesture.indexOf("_")+1);
				if(!gestureSensorData.containsKey(newGestureName)){
					gestureSensorData.put(newGestureName, new HashMap<Integer, Map<Integer, Double>>());
				}
				gestureSensorData.get(newGestureName).put(sensor, allDVMatrix.get(sensor).get(gesture));
			}
		}
		return gestureSensorData;
	}
	public Map<String, Map<Integer, Map<Integer, Double>>> projectQueryInLatentSpace(
			Map<String, Map<Integer, Map<String, Word>>> queryMap) {
		Map<String, Map<Integer, Map<Integer, Double>>> queryTopicMap = new HashMap<String, Map<Integer, Map<Integer, Double>>>();
		for(String gesture: queryMap.keySet()){
			if(!queryTopicMap.containsKey(gesture)){
				queryTopicMap.put(gesture, new HashMap<Integer, Map<Integer, Double>>());
			}
			for(Integer sensor: queryMap.get(gesture).keySet()){
				if(!queryTopicMap.get(gesture).containsKey(sensor)){
					queryTopicMap.get(gesture).put(sensor, new HashMap<Integer, Double>());
				}
				Map<String, Word> sensorMap = queryMap.get(gesture).get(sensor);
				Map<String, Map<Integer, Double>> vMap = vMatrixForAllSensors.get(sensor);
				double columnValue[] = new double[3];
				for(String word: sensorMap.keySet()){
					if(vMap.containsKey(word)){
						double rawTf = sensorMap.get(word).getRawTF();
						double k1 = vMap.get(word).get(0);
						double k2 = vMap.get(word).get(1);
						double k3 = vMap.get(word).get(2);
						columnValue[0] += rawTf*k1;
						columnValue[1] += rawTf*k2;
						columnValue[2] += rawTf*k3;
					}
				}
				queryTopicMap.get(gesture).get(sensor).put(0, columnValue[0]);
				queryTopicMap.get(gesture).get(sensor).put(1, columnValue[1]);
				queryTopicMap.get(gesture).get(sensor).put(2, columnValue[2]);
			}
		}
		return queryTopicMap;
	}
	
}
