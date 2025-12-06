package com.example.battleship.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

public class WelcomeController {

    @FXML
    private TextField nicknameField;

    @FXML
    void onPlayButtonClick(ActionEvent event) throws IOException {
        String nickname = nicknameField.getText();

        if (nickname.isEmpty()) {
            showAlert("Nombre requerido", "Por favor, ingresa un nombre para comandar tu flota.");
            return;
        }

        // --- CORRECCIÓN DE LA RUTA ---
        // El archivo está dentro de la carpeta 'views'.
        // IMPORTANTE: En tu imagen vi un archivo llamado "BatallaNaval.fxml".
        // Si ese es tu juego, cambia "game-view.fxml" por "BatallaNaval.fxml" en la línea de abajo.
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/battleship/views/BatallaNaval.fxml"));

        // Verificación de seguridad para que sepas si la ruta falla antes de crashear
        if (loader.getLocation() == null) {
            throw new IOException("Error fatal: No se encuentra el archivo de juego en /com/example/battleship/views/game-view.fxml. Revisa si el nombre del archivo es correcto.");
        }

        Parent root = loader.load();

        // 2. Obtener el controlador del juego y pasarle el nombre
        GameController gameController = loader.getController();
        gameController.setPlayerName(nickname);

        // 3. Cambiar la escena actual a la del juego
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.centerOnScreen();
        stage.show();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}