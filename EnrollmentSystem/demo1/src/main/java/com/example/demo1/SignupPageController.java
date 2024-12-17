package com.example.demo1;

import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

public class SignupPageController {

    @FXML
    private TextField firstname;

    @FXML
    private TextField lastname;

    @FXML
    private TextField email;

    @FXML
    private PasswordField password;

    @FXML
    private DatePicker dob;

    @FXML
    private ComboBox<String> gender;

    @FXML
    private TextField address;

    @FXML
    private TextField contactNumber;

    @FXML
    protected void submitButton() {
        // Validate user input before proceeding
        if (validateInputs()) {
            String firstName = firstname.getText();
            String lastName = lastname.getText();
            String userEmail = email.getText();
            String userPassword = password.getText();
            LocalDate dateOfBirth = dob.getValue();
            String selectedGender = gender.getValue();
            String userAddress = address.getText();
            String userContact = contactNumber.getText();

            // Insert data into the database
            saveUserData(firstName, lastName, userEmail, userPassword, dateOfBirth, selectedGender, userAddress, userContact);
        }
    }

    private boolean validateInputs() {
        // Check for empty fields
        if (firstname.getText().isEmpty() || lastname.getText().isEmpty() || email.getText().isEmpty() ||
                password.getText().isEmpty() || dob.getValue() == null || gender.getValue() == null ||
                address.getText().isEmpty() || contactNumber.getText().isEmpty()) {
            // Display a simple validation message or alert if needed
            showAlert("Validation Error", "Please fill in all fields.", AlertType.WARNING);
            return false;
        }
        return true;
    }

    private void saveUserData(String firstName, String lastName, String email, String password, LocalDate dob,
                              String gender, String address, String contactNumber) {
        // SQL query to insert new user into the database
        String insertQuery = "INSERT INTO students (FirstName, LastName, Email, Password, DateOfBirth, Gender, Address, ContactNumber) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection connection = DatabaseConnector.connect()) {
            PreparedStatement preparedStatement = connection.prepareStatement(insertQuery);
            preparedStatement.setString(1, firstName);
            preparedStatement.setString(2, lastName);
            preparedStatement.setString(3, email);
            preparedStatement.setString(4, password);
            preparedStatement.setDate(5, java.sql.Date.valueOf(dob));
            preparedStatement.setString(6, gender);
            preparedStatement.setString(7, address);
            preparedStatement.setString(8, contactNumber);

            // Execute the insert query
            int rowsAffected = preparedStatement.executeUpdate();
            if (rowsAffected > 0) {
                showAlert("Success", "Account created successfully!", AlertType.INFORMATION);
                // Redirect to the login page or other desired screen
                App.changeScene("LoginPage"); // Example: Change to login page after successful sign up
            } else {
                showAlert("Error", "Failed to create account. Please try again.", AlertType.ERROR);
            }
        } catch (SQLException | IOException e) {
            e.printStackTrace();
            showAlert("Error", "Database error: " + e.getMessage(), AlertType.ERROR);
        }
    }

    private void showAlert(String title, String message, AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null); // Optional header text
        alert.setContentText(message);
        alert.showAndWait();
    }
}
