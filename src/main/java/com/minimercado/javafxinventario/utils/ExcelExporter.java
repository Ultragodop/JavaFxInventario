package com.minimercado.javafxinventario.utils;

import com.minimercado.javafxinventario.modules.PriceHistoryEntry;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Clase utilitaria para exportar datos a archivos Excel
 */
public class ExcelExporter {

    /**
     * Exporta una lista de entradas de historial de precios a un archivo Excel
     * @param entries Lista de entradas de historial de precios
     * @param filePath Ruta del archivo donde se guardará
     * @throws IOException Si ocurre un error al escribir el archivo
     */
    public static void exportPriceHistory(List<PriceHistoryEntry> entries, String filePath) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Historial de Precios");
            
            // Crear estilos
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dateStyle = createDateStyle(workbook);
            CellStyle priceStyle = createPriceStyle(workbook);
            
            // Crear encabezados
            Row headerRow = sheet.createRow(0);
            List<String> headers = PriceHistoryEntry.getExcelHeaders();
            
            for (int i = 0; i < headers.size(); i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers.get(i));
                cell.setCellStyle(headerStyle);
            }
            
            // Añadir datos
            int rowNum = 1;
            for (PriceHistoryEntry entry : entries) {
                Row row = sheet.createRow(rowNum++);
                Object[] data = entry.toExcelRow();
                
                for (int i = 0; i < data.length; i++) {
                    Cell cell = row.createCell(i);
                    if (data[i] instanceof Double) {
                        cell.setCellValue((Double) data[i]);
                        cell.setCellStyle(priceStyle);
                    } else if (data[i] instanceof LocalDateTime) {
                        cell.setCellValue(((LocalDateTime) data[i]).format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
                        cell.setCellStyle(dateStyle);
                    } else if (data[i] instanceof Integer) {
                        cell.setCellValue((Integer) data[i]);
                    } else {
                        cell.setCellValue(data[i].toString());
                    }
                }
            }
            
            // Auto-ajustar columnas
            for (int i = 0; i < headers.size(); i++) {
                sheet.autoSizeColumn(i);
            }
            
            // Guardar archivo
            try (FileOutputStream fileOut = new FileOutputStream(filePath)) {
                workbook.write(fileOut);
            }
        }
    }
    
    /**
     * Exporta un historial de precios con nombre de archivo generado automáticamente
     * @param entries Lista de entradas de historial de precios
     * @return Ruta del archivo generado
     * @throws IOException Si ocurre un error al escribir el archivo
     */
    public static Path exportPriceHistory(List<PriceHistoryEntry> entries) throws IOException {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String fileName = "historial_precios_" + timestamp + ".xlsx";
        Path filePath = Paths.get(System.getProperty("user.home"), "Downloads", fileName);
        
        exportPriceHistory(entries, filePath.toString());
        return filePath;
    }
    
    private static CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }
    
    private static CellStyle createDateStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setDataFormat(workbook.getCreationHelper().createDataFormat().getFormat("dd/mm/yyyy hh:mm"));
        return style;
    }
    
    private static CellStyle createPriceStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setDataFormat(workbook.getCreationHelper().createDataFormat().getFormat("#,##0.00"));
        return style;
    }
}
