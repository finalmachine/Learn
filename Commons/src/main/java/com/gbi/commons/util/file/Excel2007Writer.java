package com.gbi.commons.util.file;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

public class Excel2007Writer {
	public static void main(String[] args) throws IOException {
		SXSSFWorkbook book = new SXSSFWorkbook(100);
		SXSSFSheet sheet = (SXSSFSheet) book.createSheet("表单1");

		for (int rownum = 0; rownum < 60; ++rownum) {
			Row row = sheet.createRow(rownum);
				Cell cell = row.createCell(0);
				cell.setCellType(Cell.CELL_TYPE_NUMERIC);
				CellStyle dateStyle = book.createCellStyle();
				dateStyle.setDataFormat((short)rownum);
				cell.setCellStyle(dateStyle);
				cell.setCellValue(Calendar.getInstance().getTime());
		}
		FileOutputStream out = new FileOutputStream("D:\\test3.xlsx");
		book.write(out);
		out.close();
		book.close();
	}
}