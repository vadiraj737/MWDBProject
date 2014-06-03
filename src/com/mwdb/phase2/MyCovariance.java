package com.mwdb.phase2;

import org.apache.commons.math3.exception.MathIllegalArgumentException;
import org.apache.commons.math3.exception.NotStrictlyPositiveException;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.stat.correlation.Covariance;

public class MyCovariance extends Covariance{

	private RealMatrix covar;
	public MyCovariance(double[][] data) throws MathIllegalArgumentException,
			NotStrictlyPositiveException {
		super(data);
		setCovar(computeCovarianceMatrix(data));
		// TODO Auto-generated constructor stub
	}
	public RealMatrix getCovar() {
		return covar;
	}
	public void setCovar(RealMatrix covar) {
		this.covar = covar;
	}
}
