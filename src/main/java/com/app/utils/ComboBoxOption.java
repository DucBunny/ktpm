package com.app.utils;

public class ComboBoxOption {
    private final String label;   // Hiển thị trên giao diện
    private final String value;   // Dùng để lưu DB

    public ComboBoxOption(String label, String value) {
        this.label = label;
        this.value = value;
    }

    public String getLabel() {
        return label;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return label; // để ComboBox hiển thị đúng tên tiếng Việt
    }
}

