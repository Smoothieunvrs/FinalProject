package com.example.demo1;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.io.IOException;

public class DashboardAccountController {

    @FXML
    private Label studentIDLabel, firstNameLabel, lastNameLabel;

    @FXML
    private PasswordField passwordField;

    @FXML
    private DatePicker dobDatePicker;

    @FXML
    private ComboBox<String> gender;

    @FXML
    private TextField emailTextField, addressTextField, contactNumberTextField;

    @FXML
    protected void initialize() {
        // Load student data when the scene initializes
        loadStudentData();
    }

    private int getCurrentStudentID() {
        // Retrieve the current student's ID from the session manager
        return LoginController.SessionManager.getCurrentStudentID();
    }

    private void loadStudentData() {
        int studentID = getCurrentStudentID();
        String query = "SELECT StudentID, FirstName, LastName, Password, DateOfBirth, Gender, Email, Address, ContactNumber " +
                "FROM students WHERE StudentID = ?";

        try (Connection connection = DatabaseConnector.connect()) {
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, studentID);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                // Populate labels and fields with data from the result set
                studentIDLabel.setText(String.valueOf(resultSet.getInt("StudentID")));
                firstNameLabel.setText(resultSet.getString("FirstName"));
                lastNameLabel.setText(resultSet.getString("LastName"));

                passwordField.setText(resultSet.getString("Password"));
                dobDatePicker.setValue(resultSet.getDate("DateOfBirth").toLocalDate());
                gender.setValue(resultSet.getString("Gender"));
                emailTextField.setText(resultSet.getString("Email"));
                addressTextField.setText(resultSet.getString("Address"));
                contactNumberTextField.setText(resultSet.getString("ContactNumber"));

                // Lock uneditable fields
                emailTextField.setEditable(false);
                dobDatePicker.setDisable(true);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleSaveChanges() {
        int studentID = getCurrentStudentID();

        // Validate input fields before updating
        if (!validateInputs()) {
            showAlert("Validation Error", "Please fill in all fields correctly!", AlertType.ERROR);
            return;
        }

        String updateQuery = "UPDATE students SET Password = ?, Gender = ?, Address = ?, ContactNumber = ? WHERE StudentID = ?";

        try (Connection connection = DatabaseConnector.connect()) {
            PreparedStatement preparedStatement = connection.prepareStatement(updateQuery);
            preparedStatement.setString(1, passwordField.getText());
            preparedStatement.setString(2, gender.getValue());
            preparedStatement.setString(3, addressTextField.getText());
            preparedStatement.setString(4, contactNumberTextField.getText());
            preparedStatement.setInt(5, studentID);

            int rowsUpdated = preparedStatement.executeUpdate();

            if (rowsUpdated > 0) {
                showAlert("Success", "Account updated successfully!", AlertType.INFORMATION);
            } else {
                showAlert("Update Failed", "Failed to update the account. Please try again.", AlertType.ERROR);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Database Error", "An error occurred while updating the account. Please try again later.", AlertType.ERROR);
        }
    }

    private boolean validateInputs() {
        // Check if password, gender, address, and contact number are non-empty
        return !passwordField.getText().isEmpty() &&
                gender.getValue() != null &&
                !addressTextField.getText().isEmpty() &&
                !contactNumberTextField.getText().isEmpty();
    }

    private void showAlert(String title, String message, AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

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
}
