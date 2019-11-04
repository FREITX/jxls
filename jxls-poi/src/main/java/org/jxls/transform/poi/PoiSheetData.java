package org.jxls.transform.poi;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.ConditionalFormatting;
import org.apache.poi.ss.usermodel.ConditionalFormattingRule;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.SheetConditionalFormatting;
import org.apache.poi.ss.util.CellRangeAddress;
import org.jxls.common.RowData;
import org.jxls.common.SheetData;

import java.util.ArrayList;
import java.util.List;

/**
 * Sheet data wrapper for POI sheet
 * 
 * @author Leonid Vysochyn
 */
public class PoiSheetData extends SheetData {
    private List<CellRangeAddress> mergedRegions = new ArrayList<>();
    private Sheet sheet;
    private List<PoiConditionalFormatting> poiConditionalFormattings = new ArrayList<>();

    public static PoiSheetData createSheetData(Sheet sheet, PoiTransformer transformer) {
        PoiSheetData sheetData = new PoiSheetData();
        sheetData.setTransformer(transformer);
        sheetData.sheet = sheet;
        sheetData.sheetName = sheet.getSheetName();
        int numberOfRows = sheet.getLastRowNum() + 1;
        int numberOfColumns = -1;
        for (int i = 0; i < numberOfRows; i++) {
            RowData rowData = PoiRowData.createRowData(sheetData, sheet.getRow(i), transformer);
            sheetData.rowDataList.add(rowData);
            if (rowData != null && rowData.getNumberOfCells() > numberOfColumns) {
                numberOfColumns = rowData.getNumberOfCells();
            }
        }
        for (int i = 0; i < sheet.getNumMergedRegions(); i++) {
            CellRangeAddress region = sheet.getMergedRegion(i);
            sheetData.mergedRegions.add(region);
        }
        if (numberOfColumns > 0) {
            sheetData.columnWidth = new int[numberOfColumns];
            for (int i = 0; i < numberOfColumns; i++) {
                sheetData.columnWidth[i] = sheet.getColumnWidth(i);
            }
        }
        SheetConditionalFormatting sheetConditionalFormatting = sheet.getSheetConditionalFormatting();
        for (int i = 0; i < sheetConditionalFormatting.getNumConditionalFormattings(); i++) {
            ConditionalFormatting conditionalFormatting = sheetConditionalFormatting.getConditionalFormattingAt(i);
            PoiConditionalFormatting poiConditionalFormatting = new PoiConditionalFormatting(conditionalFormatting);
            sheetData.poiConditionalFormattings.add(poiConditionalFormatting);
        }
        return sheetData;
    }

    public List<CellRangeAddress> getMergedRegions() {
        return mergedRegions;
    }

    public Sheet getSheet() {
        return sheet;
    }

    public void updateConditionalFormatting(PoiCellData srcCellData, Cell targetCell){
        if (!poiConditionalFormattings.isEmpty()){
            for (PoiConditionalFormatting conditionalFormatting : poiConditionalFormattings) {
                List<CellRangeAddress> ranges = conditionalFormatting.getRanges();
                for (CellRangeAddress range : ranges) {
                    if (range.isInRange(srcCellData.getRow(), srcCellData.getCol())){
                        CellRangeAddress newRange = new CellRangeAddress(targetCell.getRowIndex(), targetCell.getRowIndex(),
                                targetCell.getColumnIndex(), targetCell.getColumnIndex());
                        Sheet targetSheet = targetCell.getSheet();
                        SheetConditionalFormatting targetSheetConditionalFormatting = targetSheet.getSheetConditionalFormatting();
                        for (ConditionalFormattingRule rule : conditionalFormatting.getRules()){
                            targetSheetConditionalFormatting.addConditionalFormatting(new CellRangeAddress[]{newRange}, rule);
                        }
                    }
                }
            }
        }
    }
}
