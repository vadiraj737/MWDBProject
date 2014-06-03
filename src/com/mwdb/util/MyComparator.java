package com.mwdb.util;

import java.util.Comparator;

public class MyComparator implements Comparator<Integer>{
	
	@Override
	public int compare(Integer i1, Integer i2) {
		return i2.compareTo(i1);
	}
}
