package com.gbi.commons.util.file;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.hssf.eventusermodel.EventWorkbookBuilder.SheetRecordCollectingListener;
import org.apache.poi.hssf.eventusermodel.FormatTrackingHSSFListener;
import org.apache.poi.hssf.eventusermodel.HSSFEventFactory;
import org.apache.poi.hssf.eventusermodel.HSSFListener;
import org.apache.poi.hssf.eventusermodel.HSSFRequest;
import org.apache.poi.hssf.eventusermodel.MissingRecordAwareHSSFListener;
import org.apache.poi.hssf.eventusermodel.dummyrecord.LastCellOfRowDummyRecord;
import org.apache.poi.hssf.eventusermodel.dummyrecord.MissingCellDummyRecord;
import org.apache.poi.hssf.model.HSSFFormulaParser;
import org.apache.poi.hssf.record.BOFRecord;
import org.apache.poi.hssf.record.BlankRecord;
import org.apache.poi.hssf.record.BoolErrRecord;
import org.apache.poi.hssf.record.BoundSheetRecord;
import org.apache.poi.hssf.record.FormulaRecord;
import org.apache.poi.hssf.record.LabelRecord;
import org.apache.poi.hssf.record.LabelSSTRecord;
import org.apache.poi.hssf.record.NumberRecord;
import org.apache.poi.hssf.record.Record;
import org.apache.poi.hssf.record.SSTRecord;
import org.apache.poi.hssf.record.StringRecord;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.json.JSONObject;

/**
 * 抽象Excel2003读取器，通过实现HSSFListener监听器，采用事件驱动模式解析excel2003
 * 中的内容，遇到特定事件才会触发，大大减少了内存的使用。
 */
public class Excel2003Reader implements HSSFListener {

	private POIFSFileSystem fs;

	/* Should we output the formula, or the value it has? */
	private boolean outputFormulaValues = true;

	/* For parsing Formulas */
	private SheetRecordCollectingListener workbookBuildingListener;

	// excel2003工作薄
	private HSSFWorkbook stubWorkbook;

	// Records we pick up as we process
	private SSTRecord sstRecord;
	private FormatTrackingHSSFListener formatListener;

	// 表索引
	private BoundSheetRecord[] orderedBSRs;
	private ArrayList<BoundSheetRecord> boundSheetRecords = new ArrayList<>();

	private boolean outputNextStringRecord;
	// 当前行
	private int curRow = 1;
	
	// 存储行记录的容器
	private List<String> headers;
	private List<Object> cells = new ArrayList<>();

	private ExcelRowReader rowReader;

	public void setRowReader(ExcelRowReader rowReader) {
		this.rowReader = rowReader;
	}

	/**
	 * 遍历excel下所有的sheet
	 * 
	 * @throws IOException
	 */
	public void process(String fileName) throws IOException {
		this.fs = new POIFSFileSystem(new FileInputStream(fileName));
		MissingRecordAwareHSSFListener listener = new MissingRecordAwareHSSFListener(this);
		formatListener = new FormatTrackingHSSFListener(listener);
		HSSFEventFactory factory = new HSSFEventFactory();
		HSSFRequest request = new HSSFRequest();
		if (outputFormulaValues) {
			request.addListenerForAllRecords(formatListener);
		} else {
			workbookBuildingListener = new SheetRecordCollectingListener(formatListener);
			request.addListenerForAllRecords(workbookBuildingListener);
		}
		factory.processWorkbookEvents(request, fs);
	}

	/**
	 * HSSFListener 监听方法，处理 Record
	 */
	public void processRecord(Record record) {
		Object thisCell = null;
		String value = null;
		switch (record.getSid()) {
		case BoundSheetRecord.sid:
			boundSheetRecords.add((BoundSheetRecord)record);
			break;
		case BOFRecord.sid:
			BOFRecord br = (BOFRecord) record;
			if (br.getType() == BOFRecord.TYPE_WORKSHEET) {
				// 如果有需要，则建立子工作薄
				if (workbookBuildingListener != null && stubWorkbook == null) {
					stubWorkbook = workbookBuildingListener.getStubHSSFWorkbook();
				}
				if (orderedBSRs == null) {
					orderedBSRs = BoundSheetRecord.orderByBofPosition(boundSheetRecords);
				}
			//	sheetName = orderedBSRs[sheetIndex].getSheetname();
			}
			break;

		case SSTRecord.sid:
			sstRecord = (SSTRecord) record;
			break;
		case BlankRecord.sid:
			cells.add(null);
			break;
		case BoolErrRecord.sid: // 单元格为布尔类型
			BoolErrRecord berec = (BoolErrRecord) record;
			cells.add(berec.getBooleanValue());
			break;
		case FormulaRecord.sid: // 单元格为公式类型
			FormulaRecord frec = (FormulaRecord) record;
			if (outputFormulaValues) {
				if (Double.isNaN(frec.getValue())) {
					// Formula result is a string
					// This is stored in the next record
					outputNextStringRecord = true;
				} else {
					thisCell = formatListener.formatNumberDateCell(frec);
				}
			} else {
				thisCell = HSSFFormulaParser.toFormulaString(stubWorkbook, frec.getParsedExpression());
			}
			cells.add(thisCell);
			break;
		case StringRecord.sid:// 单元格中公式的字符串
			if (outputNextStringRecord) {
				// String for formula
				StringRecord srec = (StringRecord) record;
				thisCell = srec.getString();
				outputNextStringRecord = false;
			}
			break;
		case LabelRecord.sid:
			LabelRecord lrec = (LabelRecord) record;
			curRow = lrec.getRow();
			value = lrec.getValue().trim();
			cells.add(value);
			break;
		case LabelSSTRecord.sid: // 单元格为字符串类型
			LabelSSTRecord lsrec = (LabelSSTRecord) record;
			curRow = lsrec.getRow();
			if (sstRecord == null) {
				cells.add("");
			} else {
				value = sstRecord.getString(lsrec.getSSTIndex()).toString().trim();
				value = value.equals("") ? " " : value;
				cells.add(value);
			}
			break;
		case NumberRecord.sid: // 单元格为数字类型
			NumberRecord numrec = (NumberRecord) record;
			curRow = numrec.getRow();
			value = formatListener.formatNumberDateCell(numrec).trim();
			value = value.equals("") ? " " : value;
			// 向容器加入列值
			cells.add(value);
			break;
		default:
			break;
		}

		// 空值的操作
		if (record instanceof MissingCellDummyRecord) {
			MissingCellDummyRecord mc = (MissingCellDummyRecord) record;
			curRow = mc.getRow();
			cells.add("");
		}
		// 行结束时的操作
		if (record instanceof LastCellOfRowDummyRecord) {
			if (curRow == 1) {
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
			++curRow;
		}
	}
}