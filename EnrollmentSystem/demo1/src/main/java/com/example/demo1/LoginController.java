package com.example.demo1;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

import java.io.IOException;
import javafx.scene.control.Alert;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class LoginController {
    @FXML
    private TextField usernameField; // This can hold either StudentID or Email
    @FXML
    private TextField passwordField;

    public static class SessionManager {
        private static int currentStudentID; // Holds the logged-in student's ID
        private static String currentStudentEmail; // Holds the logged-in student's email

        // Method to set the current student info
        public static void setCurrentStudentInfo(int studentID, String email) {
            currentStudentID = studentID;
            currentStudentEmail = email;
        }

        // Method to get the current student ID
        public static int getCurrentStudentID() {
            return currentStudentID;
        }

        // Method to get the current student email
        public static String getCurrentStudentEmail() {
            return currentStudentEmail;
        }

        // Method to clear the session (e.g., on logout)
        public static void clearSession() {
            currentStudentID = 0; // Reset to default value
            currentStudentEmail = null; // Clear the email
        }
    }

    @FXML
    protected void submitButton() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        try (Connection connection = DatabaseConnector.connect()) {
            // Query to check login credentials using either StudentID or Email
            String query = """
                SELECT StudentID, Email 
                FROM students 
                WHERE (StudentID = ? OR Email = ?) AND Password = ?
                """;
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, username); // Check if username matches StudentID
            preparedStatement.setString(2, username); // Or matches Email
            preparedStatement.setString(3, password);

            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                // Fetch the StudentID and Email from the database
                int studentID = resultSet.getInt("StudentID");
                String email = resultSet.getString("Email");

                // Set the student info in the session manager
                SessionManager.setCurrentStudentInfo(studentID, email);

                // Change the scene to Dashboard
                App.changeScene("DashboardBalance");
            } else {
                // Handle invalid login (e.g., show an error message)
                showAlert("Login Error", "Invalid username or password!", Alert.AlertType.ERROR);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String message, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    protected void SignupBtn() throws IOException {
        App.changeScene("SignupPage");
    }
}
