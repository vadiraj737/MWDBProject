package com.mwdb.phase1;

public class Word {
	
	private double tf;
	private double rawTF;
	private double tFIdf;
	private double tFIdf2;
	private int x;
	private int y;
	public double getRawTF() {
		return rawTF;
	}
	public void setRawTF(double rawTF) {
		this.rawTF = rawTF;
	}
	public int getX() {
		return x;
	}
	public int getY() {
		return y;
	}
	public void setY(int y) {
		this.y = y;
	}
	public void setX(int x) {
		this.x = x;
	}
	public double getTf() {
		return tf;
	}
	public void setTf(double tf) {
		this.tf = tf;
	}
	public double getTFIdf() {
		return tFIdf;
	}
	public void setTFIdf(double idf) {
		this.tFIdf = idf;
	}
	public double getTFIdf2() {
		return tFIdf2;
	}
	public void setTFIdf2(double idf2) {
		this.tFIdf2 = idf2;
	}
	
}