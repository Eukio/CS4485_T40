module com.example.test {
    //requires javafx.controls;
    //requires javafx.fxml;


    //opens com.example.test to javafx.fxml;
    //exports com.example.test;



    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires java.desktop;

    opens com.example.test to javafx.fxml;
    opens com.example.test.db to javafx.fxml;
    //opens com.example.test.gui to javafx.fxml;
    //opens com.example.test.gui to javafx.fxml;

    //exports com.example.test.gui;
    exports com.example.test;
    exports com.example.test.db;
    exports com.example.test.model;
    exports com.example.test.service;
    exports com.example.test.util;
}