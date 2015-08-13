package com.gbi.jsoup.common;

import java.io.IOException;

public class CharsetTest {
	public static void main(String[] args) throws IOException {
		String str = "abc+-*/123";
		System.out.println(new String(str.getBytes(), "US-ASCII"));
		str = "  \n\t\t\t  \t";
		System.out.println(str.trim().length());
	}
}
