module com.example.battleship {
    requires javafx.controls;
    requires javafx.fxml;

    opens com.example.battleship.controller to javafx.fxml;
    exports com.example.battleship;
}
