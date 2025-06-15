package com.app.controllers.Payments.PaymentDetail;

import com.app.models.Payments;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class PaymentDetailController {
    @FXML
    private TextField periodField;
    @FXML
    private TextField roomField;
    @FXML
    private TextField amountField;
    @FXML
    private TextField dayField;
    @FXML
    private TextField monthField;
    @FXML
    private TextField yearField;
    @FXML
    private TextArea noteArea;

    private static Payments paymentDetail;

    public static void setPaymentDetail(Payments payment) {
        paymentDetail = payment;
    }

    @FXML
    public void initialize() {
        if (paymentDetail == null) {
            return;
        }

        // Set dữ liệu
        periodField.setText(paymentDetail.getCollectionPeriod());
        roomField.setText(paymentDetail.getRoomNumber());
        amountField.setText(paymentDetail.getPaidAmount());
        noteArea.setText(paymentDetail.getNote());

        if (paymentDetail.getPaymentDate() != null) {
            int day = paymentDetail.getPaymentDate().getDayOfMonth();
            int month = paymentDetail.getPaymentDate().getMonthValue();
            int year = paymentDetail.getPaymentDate().getYear();
            dayField.setText(String.format("%02d", day));
            monthField.setText(String.format("%02d", month));
            yearField.setText(String.valueOf(year));
        }
    }
}
