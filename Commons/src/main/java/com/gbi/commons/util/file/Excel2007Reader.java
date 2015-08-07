package com.gbi.commons.util.file;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.SAXParserFactory;

import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackageAccess;
import org.apache.poi.ss.usermodel.BuiltinFormats;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.xssf.eventusermodel.ReadOnlySharedStringsTable;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.model.StylesTable;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.json.JSONObject;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

/**
 * 抽象Excel2007读取器，excel2007的底层数据结构是xml文件，采用SAX的事件驱动的方法解析
 * xml，需要继承DefaultHandler，在遇到文件内容时，事件会触发，这种做法可以大大降低 内存的耗费，特别使用于大数据量的文件。
 */
public class Excel2007Reader extends DefaultHandler {
	
	enum xssfDataType {
		BOOL, ERROR, FORMULA, INLINESTR, SSTINDEX, NUMBER,
	}
	
	private StylesTable stylesTable;
	private ReadOnlySharedStringsTable sharedStringsTable;
	
	// Set when V start element is seen
	private boolean vIsOpen;
	
	// restore a cell value
	private StringBuffer value = new StringBuffer();

	private int curRow = 1;
//	private int columnNum = -1;
	
	private xssfDataType dataType;

	// Used to format numeric cell values.
	private short formatIndex;
	private String formatString;
	
	private List<String> headers;
	private List<Object> cells = new ArrayList<>();

	private ExcelRowReader rowReader;

	public void setRowReader(ExcelRowReader rowReader) {
		this.rowReader = rowReader;
	}

	/**
	 * 只遍历一个电子表格，其中sheetId为要遍历的sheet索引，从1开始
	 * 
	 * @param filename
	 * @param sheetId
	 * @throws Exception
	 */
	public void processOneSheet(final String filename, final int sheetId) throws Exception {
		OPCPackage pkg = OPCPackage.open(filename, PackageAccess.READ);
		sharedStringsTable = new ReadOnlySharedStringsTable(pkg);
		XSSFReader reader = new XSSFReader(pkg);
		stylesTable = reader.getStylesTable();
		
		XMLReader sheetReader = SAXParserFactory.newInstance().newSAXParser().getXMLReader();
		sheetReader.setContentHandler(this);

		// 根据 rId# 或 rSheet# 查找sheet
		InputStream sheet2 = reader.getSheet("rId" + sheetId);
		InputSource sheetSource = new InputSource(sheet2);
		sheetReader.parse(sheetSource);
		sheet2.close();
	}

	
	public void processOneSheet(final String filename, final String sheetName) throws Exception {
		OPCPackage pkg = OPCPackage.open(filename, PackageAccess.READ);
		sharedStringsTable = new ReadOnlySharedStringsTable(pkg);
		XSSFReader reader = new XSSFReader(pkg);
		stylesTable = reader.getStylesTable();
		
		XMLReader sheetReader = SAXParserFactory.newInstance().newSAXParser().getXMLReader();
		sheetReader.setContentHandler(this);

		XSSFReader.SheetIterator sheets = (XSSFReader.SheetIterator) reader.getSheetsData();
		while (sheets.hasNext()) {
			InputStream sheet =  sheets.next();
			InputSource sheetSource = new InputSource(sheet);
			if (sheets.getSheetName().equals(sheetName)) {
				sheetReader.parse(sheetSource);
			}
			sheet.close();
		}
		
	}
	/**
	 * 遍历工作簿中所有的电子表格
	 * 
	 * @param filename
	 * @throws Exception
	 */
	public void process(String filename) throws Exception {
		OPCPackage pkg = OPCPackage.open(filename, PackageAccess.READ);
		sharedStringsTable = new ReadOnlySharedStringsTable(pkg);
		XSSFReader reader = new XSSFReader(pkg);
		stylesTable = reader.getStylesTable();
		
		XMLReader sheetReader = SAXParserFactory.newInstance().newSAXParser().getXMLReader();
		sheetReader.setContentHandler(this);

		Iterator<InputStream> sheets = reader.getSheetsData();
		while (sheets.hasNext()) {
			curRow = 1;
			InputStream sheet = sheets.next();
			InputSource sheetSource = new InputSource(sheet);
			sheetReader.parse(sheetSource);
			sheet.close();
		}
	}

	public void startElement(String uri, String localName, String name, Attributes attributes) throws SAXException {
		if ("inlineStr".equals(name) || "v".equals(name)) {
			vIsOpen = true;
			// Clear contents cache
			value.setLength(0);
		}
		// c => cell
		else if ("c".equals(name)) {

			// Set up defaults.
			dataType = xssfDataType.NUMBER;
			formatIndex = -1;
			formatString = null;

			String cellType = attributes.getValue("t");
			String cellStyleStr = attributes.getValue("s");

			if ("b".equals(cellType))
				dataType = xssfDataType.BOOL;
			else if ("e".equals(cellType))
				dataType = xssfDataType.ERROR;
			else if ("inlineStr".equals(cellType))
				dataType = xssfDataType.INLINESTR;
			else if ("s".equals(cellType))
				dataType = xssfDataType.SSTINDEX;
			else if ("str".equals(cellType))
				dataType = xssfDataType.FORMULA;
			else if (cellStyleStr != null) {
				// It's a number, but almost certainly one
				// with a special style or format
				int styleIndex = Integer.parseInt(cellStyleStr);
				XSSFCellStyle style = stylesTable.getStyleAt(styleIndex);
				formatIndex = style.getDataFormat();
				formatString = style.getDataFormatString();
				if (formatString == null)
					formatString = BuiltinFormats.getBuiltinFormat(formatIndex);
			}
		}
	}

	public void endElement(String uri, String localName, String name) throws SAXException {
		Object thisCell = null;
		// v => contents of a cell
		if ("v".equals(name)) {
			// Process the value contents as required.
			// Do now, as characters() may be called more than once
			switch (dataType) {
			case BOOL:
				char first = value.charAt(0);
				thisCell = first == '0' ? false : true;
				break;
			case ERROR:
				thisCell = "ERROR:" + value.toString();
				break;
			case FORMULA:
				// A formula could result in a string value,
				// so always add double-quote characters.
				thisCell = value.toString();
				break;
			case INLINESTR:
				// TODO: have seen an example of this, so it's untested.
				XSSFRichTextString rtsi = new XSSFRichTextString(value.toString());
				thisCell = rtsi.toString();
				break;
			case SSTINDEX:
				String sstIndex = value.toString();
				try {
					int idx = Integer.parseInt(sstIndex);
					XSSFRichTextString rtss = new XSSFRichTextString(sharedStringsTable.getEntryAt(idx));
					thisCell = rtss.toString();
				} catch (NumberFormatException ex) {
					throw new RuntimeException(ex);
				}
				break;
			case NUMBER:
				String n = value.toString();
				// 判断是否是日期格式
				if (HSSFDateUtil.isADateFormat(formatIndex, n)) {
					Double d = Double.parseDouble(n);
					thisCell = HSSFDateUtil.getJavaDate(d);
				} else if (formatString != null) {
					thisCell = new DataFormatter().formatRawCellContents(Double.parseDouble(n), formatIndex, formatString);
				} else {
					thisCell = n;
				}
				break;
			default:
				thisCell = "(TODO: Unexpected type: " + dataType + ")";
				break;
			}
			if (thisCell == null || (thisCell instanceof String && "".equals(thisCell))) {
				cells.add(null);
			} else {
				cells.add(thisCell);
			}
		} else if ("row".equals(name)) {
			if (curRow == 1) {
				headers = new ArrayList<>();
				for (Object head : cells) {
					headers.add(head.toString());
				}
			} else {
				JSONObject json = new JSONObject();
				for (int  i = 0; i < headers.size(); ++i) {
					json.put(new String(headers.get(i)), cells.get(i));
				}
				rowReader.getRows(curRow, json);
			}
			cells.clear();
			curRow++;
		}
	}

	public void characters(char[] ch, int start, int length) throws SAXException {
		if (vIsOpen) {
			value.append(ch, start, length);
		}
	}
}