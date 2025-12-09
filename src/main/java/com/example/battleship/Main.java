package com.example.battleship;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage stage) throws Exception {

        FXMLLoader loader = new FXMLLoader(
                Main.class.getResource("/com/example/battleship/views/welcome-view.fxml")
        );

        Scene scene = new Scene(loader.load());

        // Cargar CSS
        scene.getStylesheets().add(
                Main.class.getResource("/styles/naval.css").toExternalForm()
        );

        stage.setTitle("Batalla Naval - JavaFX");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
