package com.gbi.commons.util.file;

public class Excel {

	// excel2003扩展名
	public static final String EXCEL03_EXTENSION = ".xls";
	// excel2007扩展名
	public static final String EXCEL07_EXTENSION = ".xlsx";

	public static void readExcel(ExcelRowReader reader, String fileName, String sheetName) throws Exception {
		// 处理excel2003文件
/*		if (fileName.endsWith(EXCEL03_EXTENSION)) {
			Excel2003Reader excel = new Excel2003Reader();
			excel.setRowReader(reader);
			excel.process(fileName);
			// 处理excel2007文件
		} */
		if (fileName.endsWith(EXCEL07_EXTENSION)) {
			Excel2007Reader excel = new Excel2007Reader();
			excel.setRowReader(reader);
			excel.processOneSheet(fileName, sheetName);
		} else {
			throw new Exception("文件格式错误，扩展名只能是xls或xlsx");
		}
	}
}
