package com.example.demo1;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;

public class DashboardEnrollController {
    @FXML
    private TableView<Course> tableView;
    @FXML
    private TableColumn<Course, String> courseCodeColumn;
    @FXML
    private TableColumn<Course, String> courseNameColumn;
    @FXML
    private TableColumn<Course, Integer> unitsColumn;
    @FXML
    private TableColumn<Course, String> scheduleColumn;
    @FXML
    private TableColumn<Course, String> roomNumberColumn;
    @FXML
    private TableColumn<Course, String> professorColumn;

    private ObservableList<Course> courses = FXCollections.observableArrayList();

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

    public class Course {
        private String courseCode;
        private String courseName;
        private int units;
        private String schedule;
        private String roomNumber;
        private String professor;

        public Course(String courseCode, String courseName, int units, String schedule, String roomNumber, String professor) {
            this.courseCode = courseCode;
            this.courseName = courseName;
            this.units = units;
            this.schedule = schedule;
            this.roomNumber = roomNumber;
            this.professor = professor;
        }

        public String getCourseCode() { return courseCode; }
        public void setCourseCode(String courseCode) { this.courseCode = courseCode; }

        public String getCourseName() { return courseName; }
        public void setCourseName(String courseName) { this.courseName = courseName; }

        public int getUnits() { return units; }
        public void setUnits(int units) { this.units = units; }

        public String getSchedule() { return schedule; }
        public void setSchedule(String schedule) { this.schedule = schedule; }

        public String getRoomNumber() { return roomNumber; }
        public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }

        public String getProfessor() { return professor; }
        public void setProfessor(String professor) { this.professor = professor; }
    }

    @FXML
    public void initialize() {
        courseCodeColumn.setCellValueFactory(new PropertyValueFactory<>("courseCode"));
        courseNameColumn.setCellValueFactory(new PropertyValueFactory<>("courseName"));
        unitsColumn.setCellValueFactory(new PropertyValueFactory<>("units"));
        scheduleColumn.setCellValueFactory(new PropertyValueFactory<>("schedule"));
        roomNumberColumn.setCellValueFactory(new PropertyValueFactory<>("roomNumber"));
        professorColumn.setCellValueFactory(new PropertyValueFactory<>("professor"));

        loadCoursesFromDatabase();
        addEnrollButtonToTable(tableView);
    }

    private void loadCoursesFromDatabase() {
        try (Connection connection = DatabaseConnector.connect()) {
            String query = "SELECT courses.CourseID, courses.CourseName, courses.Units, " +
                    "courses.Schedule, courses.RoomNumber, " +
                    "CONCAT(professors.FirstName, ' ', professors.LastName) AS professorName " +
                    "FROM courses " +
                    "JOIN professors ON courses.ProfessorID = professors.ProfessorID";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                String courseCode = resultSet.getString("CourseID");
                String courseName = resultSet.getString("CourseName");
                int units = resultSet.getInt("Units");
                String schedule = resultSet.getString("Schedule");
                String roomNumber = resultSet.getString("RoomNumber");
                String professor = resultSet.getString("professorName");

                courses.add(new Course(courseCode, courseName, units, schedule, roomNumber, professor));
            }

            tableView.setItems(courses);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addEnrollButtonToTable(TableView<Course> tableView) {
        TableColumn<Course, Void> actionColumn = new TableColumn<>("Action");

        actionColumn.setCellFactory(col -> new TableCell<>() {
            private final Button enrollButton = new Button("Enroll");

            {
                enrollButton.setOnAction(event -> {
                    Course course = getTableView().getItems().get(getIndex());
                    enrollStudentInCourse(course);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);

                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(enrollButton);
                }
            }
        });

        tableView.getColumns().add(actionColumn);
    }

    private void enrollStudentInCourse(Course course) {
        int studentID = getCurrentStudentID();
        LocalDate enrollmentDate = LocalDate.now();
        String semester = "1st";

        try (Connection connection = DatabaseConnector.connect()) {
            String totalUnitsQuery = "SELECT SUM(courses.Units) AS totalUnits " +
                    "FROM enrollments " +
                    "JOIN courses ON enrollments.CourseID = courses.CourseID " +
                    "WHERE enrollments.StudentID = ? AND enrollments.Semester = ?";
            PreparedStatement totalUnitsStmt = connection.prepareStatement(totalUnitsQuery);
            totalUnitsStmt.setInt(1, studentID);
            totalUnitsStmt.setString(2, semester);
            ResultSet totalUnitsResult = totalUnitsStmt.executeQuery();

            int currentTotalUnits = 0;
            if (totalUnitsResult.next()) {
                currentTotalUnits = totalUnitsResult.getInt("totalUnits");
            }

            String fetchCourseDetailsQuery = "SELECT Units, Schedule FROM courses WHERE CourseID = ?";
            PreparedStatement fetchCourseDetailsStmt = connection.prepareStatement(fetchCourseDetailsQuery);
            fetchCourseDetailsStmt.setString(1, course.getCourseCode());
            ResultSet courseDetailsResult = fetchCourseDetailsStmt.executeQuery();

            if (courseDetailsResult.next()) {
                int courseUnits = courseDetailsResult.getInt("Units");
                String courseSchedule = courseDetailsResult.getString("Schedule");

                if (currentTotalUnits + courseUnits > 24) {
                    showAlert("Enrollment Limit Exceeded",
                            "You cannot enroll in this course as it exceeds the 24-unit limit.",
                            Alert.AlertType.WARNING);
                    return;
                }

                String scheduleConflictQuery = "SELECT courses.CourseName, courses.Schedule " +
                        "FROM enrollments " +
                        "JOIN courses ON enrollments.CourseID = courses.CourseID " +
                        "WHERE enrollments.StudentID = ? AND enrollments.Semester = ? " +
                        "AND courses.Schedule = ?";
                PreparedStatement scheduleConflictStmt = connection.prepareStatement(scheduleConflictQuery);
                scheduleConflictStmt.setInt(1, studentID);
                scheduleConflictStmt.setString(2, semester);
                scheduleConflictStmt.setString(3, courseSchedule);

                ResultSet conflictResult = scheduleConflictStmt.executeQuery();
                if (conflictResult.next()) {
                    String conflictingCourseName = conflictResult.getString("CourseName");
                    String conflictingSchedule = conflictResult.getString("Schedule");
                    showAlert("Schedule Conflict",
                            "You cannot enroll in this course as it conflicts with " +
                                    conflictingCourseName + " scheduled at " + conflictingSchedule + ".",
                            Alert.AlertType.WARNING);
                    return;
                }

                int totalFee = courseUnits * 500;

                String enrollQuery = "INSERT INTO enrollments (StudentID, CourseID, EnrollmentDate, Semester, TotalFee) VALUES (?, ?, ?, ?, ?)";
                PreparedStatement enrollStmt = connection.prepareStatement(enrollQuery);
                enrollStmt.setInt(1, studentID);
                enrollStmt.setString(2, course.getCourseCode());
                enrollStmt.setDate(3, Date.valueOf(enrollmentDate));
                enrollStmt.setString(4, semester);
                enrollStmt.setInt(5, totalFee);

                int rowsAffected = enrollStmt.executeUpdate();

                if (rowsAffected > 0) {
                    showAlert("Success", "Enrolled successfully", Alert.AlertType.INFORMATION);
                } else {
                    showAlert("Error", "An error occurred. Please try again.", Alert.AlertType.ERROR);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "An unexpected error occurred.", Alert.AlertType.ERROR);
        }
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
