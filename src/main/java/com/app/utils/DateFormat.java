package com.app.utils;

import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class DateFormat {

    private static final DateTimeFormatter DEFAULT_DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // Hàm tạo cell factory định dạng LocalDate
    public static <T> Callback<TableColumn<T, LocalDate>, TableCell<T, LocalDate>> forLocalDate(String pattern) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        return column -> new TableCell<T, LocalDate>() {
            @Override
            protected void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.format(formatter));
                }
            }
        };
    }

    // Hàm sử dụng format mặc định dd/MM/yyyy
    public static <T> Callback<TableColumn<T, LocalDate>, TableCell<T, LocalDate>> forLocalDate() {
        return forLocalDate("dd/MM/yyyy");
    }
}