package com.example.demo1;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextInputDialog;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Optional;

public class DashboardBalanceController {

    @FXML
    private Label totalPayment;

    @FXML
    private Label totalAssessment;

    @FXML
    private Label currentBalance;

    @FXML
    protected void balanceButton() {
        try {
            App.changeScene("DashboardBalance");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    protected void enrollButton() {
        try {
            App.changeScene("DashboardEnroll");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    protected void classButton() {
        try {
            App.changeScene("DashboardClass");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    protected void accountButton() {
        try {
            App.changeScene("DashboardAccount");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void initialize() {
        loadBalanceData();
    }

    private void loadBalanceData() {
        int studentID = getCurrentStudentID();

        String query = """
        SELECT 
            (SELECT COALESCE(SUM(TotalFee), 0) FROM enrollments WHERE StudentID = ?) AS TotalAssessment,
            (SELECT COALESCE(SUM(AmountPaid), 0) FROM payments WHERE StudentID = ?) AS TotalPayments,
            (SELECT COALESCE(SUM(TotalFee), 0) FROM enrollments WHERE StudentID = ?) -
            (SELECT COALESCE(SUM(AmountPaid), 0) FROM payments WHERE StudentID = ?) AS CurrentBalance
    """;

        try (Connection connection = DatabaseConnector.connect()) {
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, studentID);
            preparedStatement.setInt(2, studentID);
            preparedStatement.setInt(3, studentID);
            preparedStatement.setInt(4, studentID);

            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                double totalAssessmentValue = resultSet.getDouble("TotalAssessment");
                double totalPaymentsValue = resultSet.getDouble("TotalPayments");
                double currentBalanceValue = resultSet.getDouble("CurrentBalance");

                // Update labels with the string format
                totalAssessment.setText(String.format("Total Assessment: PHP %.2f", totalAssessmentValue));
                totalPayment.setText(String.format("Total Payment: PHP %.2f", totalPaymentsValue));
                currentBalance.setText(String.format("Current Balance: PHP %.2f", currentBalanceValue));
            } else {
                // If no data is found, set default text with 0.00
                totalAssessment.setText("Total Assessment: PHP 0.00");
                totalPayment.setText("Total Payment: PHP 0.00");
                currentBalance.setText("Current Balance: PHP 0.00");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @FXML
    private void makePayment() {
        int studentID = getCurrentStudentID();

        // Prompt user for payment amount
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Make Payment");
        dialog.setHeaderText("Enter the payment amount:");
        dialog.setContentText("Amount:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(amountInput -> {
            try {
                double amount = Double.parseDouble(amountInput);

                // Insert payment into the database
                String insertPaymentQuery = """
                    INSERT INTO payments (StudentID, AmountPaid, PaymentDate)
                    VALUES (?, ?, CURRENT_DATE)
                """;

                try (Connection connection = DatabaseConnector.connect()) {
                    PreparedStatement preparedStatement = connection.prepareStatement(insertPaymentQuery);
                    preparedStatement.setInt(1, studentID);
                    preparedStatement.setDouble(2, amount);

                    int rowsAffected = preparedStatement.executeUpdate();
                    if (rowsAffected > 0) {
                        showAlert("Success", "Payment successfully recorded!", Alert.AlertType.INFORMATION);
                        loadBalanceData(); // Refresh balance
                    } else {
                        showAlert("Error", "Failed to record payment. Try again.", Alert.AlertType.ERROR);
                    }
                }
            } catch (NumberFormatException e) {
                showAlert("Error", "Invalid amount entered. Please enter a numeric value.", Alert.AlertType.ERROR);
            } catch (Exception e) {
                e.printStackTrace();
                showAlert("Error", "An unexpected error occurred. Try again.", Alert.AlertType.ERROR);
            }
        });
    }

    private int getCurrentStudentID() {
        return LoginController.SessionManager.getCurrentStudentID();
    }

    private void showAlert(String title, String message, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
