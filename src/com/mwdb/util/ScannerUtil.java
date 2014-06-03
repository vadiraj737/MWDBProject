package com.mwdb.util;

import java.util.Scanner;

public class ScannerUtil {
	public Scanner s = new Scanner(System.in);
	public int getInt(String str){
		System.out.println(str);
		int n = s.nextInt();
		s.nextLine();
		return n;
	}
	
	public String getString(String str){
		System.out.println(str);
		String string = s.nextLine();
		return string;
	}
	
	public void closeConnection(){
		s.close();
	}
}
