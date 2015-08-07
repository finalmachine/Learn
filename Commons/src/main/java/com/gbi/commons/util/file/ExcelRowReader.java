package com.gbi.commons.util.file;

import org.json.JSONObject;

public interface ExcelRowReader {
	/**
	 * 
	 * @param sheetIndex
	 * @param curRow
	 * @param rowlist
	 */
	public void getRows(int rowNumber, JSONObject json);  
}
