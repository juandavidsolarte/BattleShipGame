package com.example.battleship.controllers;

import com.example.battleship.models.GameState;
import com.example.battleship.persistence.GameFileManager;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class WelcomeController implements Initializable {

    @FXML
    private TextField nicknameField;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        // Usamos runLater para asegurar que la ventana ya existe antes de cambiarla
        Platform.runLater(() -> {
            if (GameFileManager.hasSavedGame()) {
                GameState save = GameFileManager.loadGame();

                // Si hay partida y NO ha terminado -> Cargar directo
                if (save != null && !save.isGameOver()) {
                    System.out.println("Partida detectada. Cargando automaticamente...");
                    loadGameScene(save, null);
                } else {
                    // Si la partida vieja ya acabo, borramos el archivo para empezar limpio
                    GameFileManager.deleteSaveFile();
                }
            }
        });
    }

    @FXML
    void onPlayButtonClick(ActionEvent event) throws IOException {
        String nickname = nicknameField.getText();

        if (nickname.isEmpty()) {
            showAlert("Nombre requerido", "Por favor, ingresa un nombre para comandar tu flota.");
            return;
        }
        loadGameScene(null, nickname);
    }

    private void loadGameScene(GameState stateToLoad, String newPlayerName) {
        try
        {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/battleship/views/BatallaNaval.fxml"));
            Parent root = loader.load();
            GameController gameController = loader.getController();

            if (stateToLoad != null)
            {
                gameController.loadGameState(stateToLoad);
            }
            else
            {
                gameController.setPlayerName(newPlayerName);
            }

            Stage stage = (Stage) nicknameField.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.centerOnScreen();
            stage.show();

        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }


}