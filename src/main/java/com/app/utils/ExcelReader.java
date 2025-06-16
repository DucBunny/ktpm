package com.app.utils;

import com.app.models.CollectionItems;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ExcelReader {
    public static List<CollectionItems> readElectricityData(String filePath, int revenueItemId) throws IOException {
        System.out.println("Đọc file điện: " + filePath);
        return readExcel(filePath, revenueItemId, "kWh", true, "Tiền điện");
    }

    public static List<CollectionItems> readWaterData(String filePath, int revenueItemId) throws IOException {
        System.out.println("Đọc file nước: " + filePath);
        return readExcel(filePath, revenueItemId, "m3", true, "Tiền nước");
    }

    public static List<CollectionItems> readInternetData(String filePath, int revenueItemId) throws IOException {
        System.out.println("Đọc file internet: " + filePath);
        return readExcel(filePath, revenueItemId, "gói", false, "Tiền internet");
    }

    private static List<CollectionItems> readExcel(String filePath, int revenueItemId, String quantityUnit, boolean hasQuantity, String revenueName) throws IOException {
        List<CollectionItems> items = new ArrayList<>();
        try (FileInputStream fis = new FileInputStream(filePath);
             Workbook workbook = new XSSFWorkbook(fis)) {
            Sheet sheet = workbook.getSheetAt(0);
            if (sheet == null) {
                System.err.println("Không tìm thấy sheet trong file: " + filePath);
                return items;
            }
            System.out.println("Tìm thấy sheet: " + sheet.getSheetName());
            boolean skipHeader = true;
            int rowIndex = 0;
            for (Row row : sheet) {
                rowIndex++;
                if (skipHeader) {
                    skipHeader = false;
                    System.out.println("Bỏ qua dòng tiêu đề: " + rowIndex);
                    continue;
                }
                try {
                    String roomNumber = getCellStringValue(row.getCell(0));
                    if (roomNumber.isEmpty()) {
                        System.err.println("Dòng " + rowIndex + ": roomNumber trống, bỏ qua.");
                        continue;
                    }
                    double quantity = hasQuantity ? getCellNumericValue(row.getCell(1)) : 1.0;
                    double unitPrice = getCellNumericValue(row.getCell(2));
                    double totalAmount = hasQuantity ? getCellNumericValue(row.getCell(3)) : getCellNumericValue(row.getCell(2));
                    CollectionItems item = new CollectionItems(revenueItemId, revenueName, roomNumber, quantityUnit, unitPrice, quantity, totalAmount, "mandatory");
                    items.add(item);
                    System.out.println("Dòng " + rowIndex + ": roomNumber=" + roomNumber + ", quantity=" + quantity + ", unitPrice=" + unitPrice + ", totalAmount=" + totalAmount);
                } catch (Exception e) {
                    System.err.println("Lỗi khi đọc dòng " + rowIndex + ": " + e.getMessage());
                }
            }
            System.out.println("Tổng số dòng dữ liệu hợp lệ: " + items.size());
        } catch (IOException e) {
            System.err.println("Lỗi khi đọc file " + filePath + ": " + e.getMessage());
            throw e;
        }
        return items;
    }

    private static String getCellStringValue(Cell cell) {
        if (cell == null)
            return "";
        try {
            return switch (cell.getCellType()) {
                case STRING -> cell.getStringCellValue().trim();
                case NUMERIC ->
                        String.valueOf((int) cell.getNumericCellValue()).trim();
                default -> "";
            };
        } catch (Exception e) {
            System.err.println("Lỗi đọc ô chuỗi: " + e.getMessage());
            return "";
        }
    }

    private static double getCellNumericValue(Cell cell) {
        if (cell == null)
            return 0.0;
        try {
            return switch (cell.getCellType()) {
                case NUMERIC -> cell.getNumericCellValue();
                case STRING -> {
                    String value = cell.getStringCellValue().trim();
                    yield value.isEmpty() ? 0.0 : Double.parseDouble(value);
                }
                default -> 0.0;
            };
        } catch (NumberFormatException e) {
            System.err.println("Lỗi đọc ô số: Giá trị không phải số - " + (cell.getCellType() == CellType.STRING ? cell.getStringCellValue() : cell.toString()));
            return 0.0;
        } catch (Exception e) {
            System.err.println("Lỗi đọc ô số: " + e.getMessage());
            return 0.0;
        }
    }
}
