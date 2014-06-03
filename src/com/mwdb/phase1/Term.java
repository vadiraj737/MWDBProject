package com.mwdb.phase1;

public class Term {
	private double tf;
	private double df;
	private double tfidf;
	public double getDf() {
		return df;
	}
	public double getTf() {
		return tf;
	}
	public double getTfidf() {
		return tfidf;
	}
	public void setDf(double df) {
		this.df = df;
	}
	public void setTf(double tf) {
		this.tf = tf;
	}
	public void setTfidf(double tfidf) {
		this.tfidf = tfidf;
	}
}
