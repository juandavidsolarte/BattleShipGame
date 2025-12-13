package com.example.battleship.controllers;

import com.example.battleship.models.GameState;
import com.example.battleship.persistence.GameFileManager;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Welcome View Controller.
 * Manages nickname entry, automatic loading of saved games,
 * and transition to the main game scene.
 */
public class WelcomeController implements Initializable {

    //region 1. Variables and Constants @FXML

    @FXML
    private TextField nicknameField;

    //endregion

    //region 2. Initialization
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Use runLater to ensure the window is fully loaded before checking saves
        Platform.runLater(() -> {
            if (GameFileManager.hasSavedGame()) {
                GameState save = GameFileManager.loadGame();

                // If there's an active, unfinished game, load it automatically
                if (save != null && !save.isGameOver()) {
                    System.out.println("Partida detectada. Cargando automaticamente...");
                    loadGameScene(save, null);
                } else {
                    // Remove completed or invalid save files for a clean start
                    GameFileManager.deleteSaveFile();
                }
            }
        });
    }
    //endregion

    //region 3. FXML Events
    @FXML
    void onPlayButtonClick(ActionEvent event) throws IOException {
        String nickname = nicknameField.getText();
        if (nickname.isEmpty()) {
            showAlert("Nombre requerido", "Por favor, ingresa un nombre para comandar tu flota.");
            return;
        }
        loadGameScene(null, nickname);
    }

    /**
     * Shows the game instructions in a popup window.
     * We explain the objectives, fleet composition, controls, and gameplay flow
     * to help new players understand the Battleship rules.
     */
    @FXML
    protected void onInstructionsClick(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Instrucciones de Batalla Naval");
        alert.setHeaderText("¿Cómo jugar?");
        String rules =
                "OBJETIVO:\n" +
                        "Hunde la flota del enemigo antes de que él hunda la tuya.\n\n" +

                        "TU FLOTA:\n" +
                        "• 1 Portaaviones (4 casillas)\n" +
                        "• 2 Submarinos (3 casillas)\n" +
                        "• 3 Destructores (2 casillas)\n" +
                        "• 4 Fragatas (1 casilla)\n\n" +

                        "CONTROLES DE POSICIONAMIENTO:\n" +
                        "• Arrastra los barcos desde el panel izquierdo a tu territorio.\n" +
                        "• IMPORTANTE: Presiona la tecla 'R' antes de arrastrar para rotar el barco.\n\n" +

                        "EL JUEGO:\n" +
                        "• Dispara en el radar enemigo (derecha) para buscar sus barcos.\n" +
                        "• Si aciertas (Tocado), puedes volver a disparar.\n" +
                        "• Si fallas (Agua), el turno pasa a la máquina.";

        alert.setContentText(rules);
        alert.getDialogPane().setMinWidth(500);
        alert.showAndWait();
    }
    //endregion

    //region 4. Navigation

    /**
     * Loads the main game scene, either resuming a saved game or starting a new one.
     * We handle both scenarios: loading a saved state or initializing with a new player,
     * then transition smoothly to the game interface.
     */
    private void loadGameScene(GameState stateToLoad, String newPlayerName) {
        try
        {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/battleship/views/BatallaNaval.fxml"));
            Parent root = loader.load();
            GameController gameController = loader.getController();

            if (stateToLoad != null)
            {
                // Resume existing game
                gameController.loadGameState(stateToLoad);
            }
            else
            {
                // Start new game with player name
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
    //endregion

    //region 5. Auxiliary Methods

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    //endregion
}