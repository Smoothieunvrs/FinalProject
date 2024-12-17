package com.example.demo1;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class DashboardClassController {

    @FXML
    private TableView<CourseEnrollment> tableView;
    @FXML
    private TableColumn<CourseEnrollment, String> courseCodeColumn;
    @FXML
    private TableColumn<CourseEnrollment, String> courseNameColumn;
    @FXML
    private TableColumn<CourseEnrollment, String> scheduleColumn;
    @FXML
    private TableColumn<CourseEnrollment, String> semesterColumn;
    @FXML
    private TableColumn<CourseEnrollment, String> roomNumberColumn;
    @FXML
    private TableColumn<CourseEnrollment, String> professorColumn;

    private ObservableList<CourseEnrollment> enrollments = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Bind columns to properties in the CourseEnrollment class
        courseCodeColumn.setCellValueFactory(new PropertyValueFactory<>("courseCode"));
        courseNameColumn.setCellValueFactory(new PropertyValueFactory<>("courseName"));
        scheduleColumn.setCellValueFactory(new PropertyValueFactory<>("schedule"));
        semesterColumn.setCellValueFactory(new PropertyValueFactory<>("semester"));
        roomNumberColumn.setCellValueFactory(new PropertyValueFactory<>("roomNumber"));
        professorColumn.setCellValueFactory(new PropertyValueFactory<>("professorName"));

        loadEnrollmentsFromDatabase();
        addDropButtonToTable();
    }

    private void loadEnrollmentsFromDatabase() {
        int studentID = getCurrentStudentID(); // Fetch the logged-in student's ID

        String query = "SELECT c.CourseID, c.CourseName, c.Schedule, e.Semester, c.RoomNumber, " +
                "CONCAT(p.FirstName, ' ', p.LastName) AS ProfessorName " +
                "FROM enrollments e " +
                "JOIN courses c ON e.CourseID = c.CourseID " +
                "JOIN professors p ON c.ProfessorID = p.ProfessorID " +
                "WHERE e.StudentID = ?";

        try (Connection connection = DatabaseConnector.connect();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, studentID);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                String courseCode = resultSet.getString("CourseID");
                String courseName = resultSet.getString("CourseName");
                String schedule = resultSet.getString("Schedule");
                String semester = resultSet.getString("Semester");
                String roomNumber = resultSet.getString("RoomNumber");
                String professorName = resultSet.getString("ProfessorName");

                enrollments.add(new CourseEnrollment(courseCode, courseName, schedule, semester, roomNumber, professorName));
            }

            tableView.setItems(enrollments);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addDropButtonToTable() {
        TableColumn<CourseEnrollment, Void> actionColumn = new TableColumn<>("Action");

        actionColumn.setCellFactory(col -> new TableCell<>() {
            private final Button dropButton = new Button("Drop");

            {
                dropButton.setOnAction(event -> {
                    CourseEnrollment enrollment = getTableView().getItems().get(getIndex());
                    dropCourse(enrollment);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);

                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(dropButton);
                }
            }
        });

        tableView.getColumns().add(actionColumn);
    }

    private void dropCourse(CourseEnrollment enrollment) {
        int studentID = getCurrentStudentID();

        String query = "DELETE FROM enrollments WHERE StudentID = ? AND CourseID = ?";

        try (Connection connection = DatabaseConnector.connect();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, studentID);
            preparedStatement.setString(2, enrollment.getCourseCode());

            int rowsAffected = preparedStatement.executeUpdate();

            if (rowsAffected >= 0) {
                enrollments.remove(enrollment); // Remove from the table view
                showAlert("Success", "Course dropped successfully.", Alert.AlertType.INFORMATION);
            } else {
                showAlert("Error", "Failed to drop the course. Please try again.", Alert.AlertType.ERROR);
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "An unexpected error occurred. Please try again.", Alert.AlertType.ERROR);
        }
    }

    private int getCurrentStudentID() {
        return LoginController.SessionManager.getCurrentStudentID(); // Retrieve student ID from the session
    }

    private void showAlert(String title, String message, Alert.AlertType alertType) {
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

    // Inner class to represent the course enrollment data
    public static class CourseEnrollment {
        private final String courseCode;
        private final String courseName;
        private final String schedule;
        private final String semester;
        private final String roomNumber;
        private final String professorName;

        public CourseEnrollment(String courseCode, String courseName, String schedule, String semester, String roomNumber, String professorName) {
            this.courseCode = courseCode;
            this.courseName = courseName;
            this.schedule = schedule;
            this.semester = semester;
            this.roomNumber = roomNumber;
            this.professorName = professorName;
        }

        public String getCourseCode() {
            return courseCode;
        }

        public String getCourseName() {
            return courseName;
        }

        public String getSchedule() {
            return schedule;
        }

        public String getSemester() {
            return semester;
        }

        public String getRoomNumber() {
            return roomNumber;
        }

        public String getProfessorName() {
            return professorName;
        }
    }
}
