module com.example.battleship {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;
    requires javafx.graphics;

    opens com.example.battleship.controllers to javafx.fxml;
    exports com.example.battleship;
}
