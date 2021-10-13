package io.agriledger.webserver.utils

import org.apache.poi.ss.usermodel.IndexedColors
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.json.JSONObject
import org.springframework.core.io.InputStreamResource
import org.apache.poi.hssf.usermodel.HeaderFooter.file
import java.io.ByteArrayOutputStream
import java.io.FileInputStream
import org.apache.poi.hssf.usermodel.HeaderFooter.file
import org.apache.poi.ss.usermodel.CellStyle
import org.apache.poi.ss.util.CellRangeAddress
import org.apache.poi.ss.util.CellUtil
import org.springframework.core.io.ByteArrayResource
import java.io.ByteArrayInputStream


class ExcelHelper {

    fun generateExcelFromData(json: JSONObject, excelTitle: String): ByteArrayInputStream {
        //Instantiate Excel workbook:
        val xlWb = XSSFWorkbook()
        //Instantiate Excel worksheet:
        val xlWs = xlWb.createSheet()

        //Row index specifies the row in the worksheet (starting at 0):
        var rowNumber = 0
        //Cell index specifies the column within the chosen row (starting at 0):
        var columnNumber = 0

        //Write text value to cell located at ROW_NUMBER / COLUMN_NUMBER:
        //xlWs.createRow(rowNumber).createCell(columnNumber).setCellValue("TEST")

        // setting bold style
        val style = xlWb.createCellStyle()
        val font = xlWb.createFont()
        font.bold = true
        style.setFont(font)

        //setting header
        var row = xlWs.createRow(rowNumber)
        row.createCell(columnNumber).setCellValue(excelTitle)
        row.getCell(columnNumber).cellStyle = style

        xlWs.addMergedRegion(CellRangeAddress(0, 0, 0, 2))
        val cell = CellUtil.createCell(xlWs.getRow(0), 0, excelTitle)

        CellUtil.setAlignment(cell, xlWb, CellStyle.ALIGN_CENTER)
        rowNumber++
        for (key in json.keys()) {
            var row = xlWs.createRow(rowNumber)
            row.createCell(columnNumber).setCellValue(key)
            row.getCell(columnNumber).cellStyle = style
            columnNumber++
            if (!isJSON(json, key)) {
                row.createCell(columnNumber).setCellValue(if (json.has(key)) json.get(key).toString() else "NA")
                rowNumber++
                columnNumber--
            } else {
                rowNumber++
                val innerJson = json.getJSONObject(key)
                for (innerKey in innerJson.keys()) {
                    row = xlWs.createRow(rowNumber)
                    row.createCell(columnNumber).setCellValue(innerKey)
                    row.getCell(columnNumber).cellStyle = style
                    columnNumber++
                    row.createCell(columnNumber).setCellValue(if (innerJson.has(innerKey)) innerJson.get(innerKey).toString() else "NA")
                    rowNumber++
                    columnNumber--
                }
                columnNumber = 0
            }
        }

        // setting sheet name
        val sheetTitle = when (excelTitle) {
            "Service Request: Vault Details" -> {
                "service_request"
            }
            "Batch: Vault Details" -> {
                "batch_details"
            }
            else -> {
                "sale_details"
            }
        }
        xlWb.setSheetName(xlWb.getSheetIndex(xlWs), sheetTitle)

        val noOfColumns = xlWs.getRow(0).physicalNumberOfCells + 1
        for (i in 0..noOfColumns) {
            xlWs.autoSizeColumn(i)
        }
        val outputStream = ByteArrayOutputStream()
        xlWb.write(outputStream)
        //xlWb.close()

        val resource = ByteArrayInputStream(outputStream.toByteArray())
//        //Write file:
//        val outputStream = FileOutputStream(filepath)
//        xlWb.write(outputStream)
//        xlWb.close()

        return resource
    }

    private fun isJSON(json: JSONObject, key: String): Boolean {
        try {
            json.getJSONObject(key)
        } catch (ex: Exception) {
            return false
        }
        return true
    }

}