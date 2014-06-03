package com.mwdb.phase1;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import matlabcontrol.MatlabConnectionException;
import matlabcontrol.MatlabInvocationException;
import matlabcontrol.MatlabProxy;
import matlabcontrol.MatlabProxyFactory;
import matlabcontrol.MatlabProxyFactoryOptions;

import org.apache.commons.math3.distribution.NormalDistribution;


public class PhaseOne 
{
	//ScannerUtil scannerUtil = new ScannerUtil();
	public Map<String, List<Double>> bandMap;
	MatlabProxyFactory factory = null;
	MatlabProxy proxy = null;

	
	public PhaseOne(){
				factory = new MatlabProxyFactory();
	}
	public Map<String, List<Double>> getBandMap() {
		return bandMap;
	}
	public void setBandMap(Map<String, List<Double>> bandMap) {
		this.bandMap = bandMap;
	}
	public MatlabProxyFactory getFactory() {
		return factory;
	}
	public void setFactory(MatlabProxyFactory factory) {
		this.factory = factory;
	}
	public MatlabProxy getProxy() {
		return proxy;
	}
	public void setProxy(MatlabProxy proxy) {
		this.proxy = proxy;
	}
	public MatlabProxy getProxyConnection(){
		try {
			proxy = factory.getProxy();
		} catch (MatlabConnectionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return proxy;
	}
	public void closeProxyConnection(){
		proxy.disconnect();
	}
	public List<String> recursiveListFilesFromFolder(final File folder) {
		List<String>  fileNames = new ArrayList<String>();

		for (final File fileEntry : folder.listFiles()) {
			if (fileEntry.isDirectory()) {
				recursiveListFilesFromFolder(fileEntry);
			} else {
				fileNames.add(fileEntry.getName());
				// System.out.println(fileEntry.getName());
			}
		}
		return fileNames;
	}
	
	// Get the normalized value from the matlab for given gesture files
	public Map<String, Double[][]> getNormalizedValues(String folderName) throws MatlabInvocationException{
		Map<String, Double[][]> normalizedMap = new HashMap<String, Double[][]>();
		List<String> fileNames;
		proxy = getProxyConnection();
		//proxy.eval("addpath(\'C:/Users/V/workspace/MWDBPhase1/')");
		
		if(folderName.endsWith(".csv")){
			fileNames = new ArrayList<String>();
			fileNames.add(folderName);
			for (String string : fileNames) {
				//System.out.println("File name:"+string);
				String fileName =  string;
				Object[] result;
				double[] test = null;
				int row = 0,col = 0;
				try {
					
					result = proxy.returningFeval("normalize", 2, fileName);
					test = (double[])result[0];
					row = (int)((double [])result[1])[0];
					col = (int)((double [])result[1])[1];

				} catch (MatlabInvocationException e) {
					e.printStackTrace();
				}
				//System.out.println("Rows:"+ row);
				//System.out.println("Columns:"+col);
				Double[][] normArray = new Double[row][col];
				int k = 0;
				for(int i = 0; i< col; i++ ){
					for(int j = 0; j < row; j++){
						normArray[j][i] = test[k++];
					}
				}
				normalizedMap.put(string, normArray);
				//System.out.println("Length:"+test.length);
			}

		}
		else{
			File folder = new File(folderName);
			fileNames = recursiveListFilesFromFolder(folder);
			for (String string : fileNames) {
				//System.out.println("File name:"+string);
				String fileName =  folderName+"\\" + string;
				Object[] result;
				double[] test = null;
				int row = 0,col = 0;
				try {
					result = proxy.returningFeval("normalize", 2, fileName);
					test = (double[])result[0];
					row = (int)((double [])result[1])[0];
					col = (int)((double [])result[1])[1];

				} catch (MatlabInvocationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				//System.out.println("Rows:"+ row);
				//System.out.println("Columns:"+col);
				Double[][] normArray = new Double[row][col];
				int k = 0;
				for(int i = 0; i< col; i++ ){
					for(int j = 0; j < row; j++){
						normArray[j][i] = test[k++];
					}
				}
				normalizedMap.put(string, normArray);
				//System.out.println("Length:"+test.length);
			}

		}
				return normalizedMap;
	}
	
	//Generate gaussian bands for given resolution
	public Map<String, List<Double>> getGaussianBands(int r){
		double mean = 0.0;
		Map<String, List<Double>>bandMap = new HashMap<String, List<Double>>();
		double stdDeviation = 0.25;
		NormalDistribution  ndf =  new  NormalDistribution(mean,stdDeviation);

		double interval = 1.0/r;
		double min = -1.0;
		double max = 1.0;
		int minCnt = 1;
		int totalBands = 2*r;
		double[] arr = new double[r];
		double totalWidth = 0.0;
		for (int i = 1; i <= r; i++) {
			double fband = -1.0 + ((double)i*(interval));
			double lband = -1.0+((double)(i-1)*(interval));
			double area =   ndf.cumulativeProbability(fband) - ndf.cumulativeProbability(lband);
			double width = area;
			arr[i-1] = width;
			totalWidth +=arr[i-1];	
		}

		for (int i = 0; i < arr.length; i++) {
			double normVal = arr[i]/totalWidth;
			List<Double> temp1 = new ArrayList<Double>();
			temp1.add(min);
			temp1.add(min+normVal);
			min = min+normVal;

			List<Double> temp2 = new ArrayList<Double>();
			temp2.add(max-normVal);
			temp2.add(max);
			max = max-normVal;
			bandMap.put("b"+minCnt, temp1);
			bandMap.put("b"+totalBands, temp2);
			minCnt++;
			totalBands--;
		}
		return bandMap;
	}

	public String getGaussianBand(double val){
		Iterator<String> iterator = bandMap.keySet().iterator();
		while(iterator.hasNext()){
			String key = iterator.next();
			List<Double> range = bandMap.get(key);
			int cnt = 0;
			if(val>= range.get(cnt) && val <= range.get(cnt+1)){
				return key;
			}
		}
		return "b"+(bandMap.size()/2);
		//return null;
	}
}
