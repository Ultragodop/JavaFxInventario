package com.minimercado.javafxinventario.utils;

import java.io.FileOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.minimercado.javafxinventario.models.PriceHistoryEntry;

public class ExcelExporter {
    
    private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    
    public void exportPriceHistory(List<PriceHistoryEntry> data, String filePath) throws Exception {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Historial de Precios");
            
            // Create header row
            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("ID Producto");
            headerRow.createCell(1).setCellValue("Nombre del Producto");
            headerRow.createCell(2).setCellValue("Precio Anterior");
            headerRow.createCell(3).setCellValue("Precio Actual");
            headerRow.createCell(4).setCellValue("Fecha de Cambio");
            headerRow.createCell(5).setCellValue("% Cambio");
            headerRow.createCell(6).setCellValue("Usuario");
            
            // Style for header row
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            
            for (int i = 0; i < 7; i++) {
                headerRow.getCell(i).setCellStyle(headerStyle);
            }
            
            // Fill data rows
            int rowNum = 1;
            for (PriceHistoryEntry entry : data) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(entry.getProductId());
                row.createCell(1).setCellValue(entry.getProductName());
                row.createCell(2).setCellValue(entry.getPreviousPrice());
                row.createCell(3).setCellValue(entry.getCurrentPrice());
                row.createCell(4).setCellValue(entry.getChangeDate().format(dateFormatter));
                row.createCell(5).setCellValue(String.format("%.2f%%", entry.getPercentageChange()));
                row.createCell(6).setCellValue(entry.getUser());
            }
            
            // Auto size columns
            for (int i = 0; i < 7; i++) {
                sheet.autoSizeColumn(i);
            }
            
            // Write to file
            try (FileOutputStream outputStream = new FileOutputStream(filePath)) {
                workbook.write(outputStream);
            }
        }
    }
}
