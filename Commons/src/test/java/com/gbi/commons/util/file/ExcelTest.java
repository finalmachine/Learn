package com.gbi.commons.util.file;

import org.json.JSONObject;

public class ExcelTest {
	
	private static class TypeTester implements ExcelRowReader {
		@Override
		public void getRows(int rowNumber, JSONObject json) {
			for (String key : json.keySet()) {
				System.out.print(json.get(key).getClass().getSimpleName() + ";");
			}
			System.out.println("\n-------------");
			for (String key : json.keySet()) {
				System.out.print(json.get(key) + ";");
			}
			System.out.println("\n-------------");
		}
	}
	
	private static void test1() throws Exception {
		Excel.readExcel(new TypeTester(), ResourceUtil.getTestFileAbstractName(ExcelTest.class, "test1.xlsx"), "Sheet0");
	}

	public static void main(String[] args) throws Exception {
		test1();
	}
}
