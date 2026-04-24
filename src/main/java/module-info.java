module com.example.test {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires java.desktop;

    opens com.example.test to javafx.fxml;
    exports com.example.test;
}