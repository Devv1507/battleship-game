package univalle.tedesoft.battleship.controllers;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import univalle.tedesoft.battleship.views.GameView;
import univalle.tedesoft.battleship.views.WelcomeView;

import java.io.IOException;

/**
 * Controlador para la pantalla de bienvenida del juego Battleship.
 * Gestiona la entrada del nombre del jugador y la transición a la vista principal del juego.
 */
public class WelcomeController {

    @FXML private TextField nameTextField;
    @FXML private Button startGameButton;
    @FXML private Button exitButton;

    private WelcomeView welcomeView;

    /**
     * Establece la referencia a la vista de bienvenida que este controlador maneja.
     * @param welcomeView La instancia de la vista de bienvenida.
     */
    public void setWelcomeView(WelcomeView welcomeView) {
        this.welcomeView = welcomeView;
    }

    /**
     * Maneja el clic en el botón "¡A la Batalla!".
     * Valida el nombre del jugador y transiciona a la vista del juego.
     * @param event El evento de la acción.
     */
    @FXML
    void onStartGameClick(ActionEvent event) {
        String playerName = this.nameTextField.getText().trim();

        if (playerName.isEmpty()) {
            // Mostrar una alerta si el nombre está vacío.
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Nombre Requerido");
            alert.setHeaderText(null);
            alert.setContentText("Por favor, ingrese un nombre de capitán para comenzar la batalla.");
            alert.showAndWait();
            return;
        }

        try {
            // Ocultar la ventana de bienvenida
            this.welcomeView.hide();

            // Obtener la instancia de la vista del juego
            GameView gameView = GameView.getInstance();

            // Personalizar el jugador con el nombre ingresado
            gameView.getController().getGameState().getHumanPlayerPositionBoard().resetBoard(); // Asegurar tablero limpio
            gameView.getController().getGameState().startNewGame(
                    new univalle.tedesoft.battleship.models.Players.HumanPlayer(playerName)
            );

            // Refrescar la UI del juego para mostrar todo desde el principio
            gameView.refreshUI();

            // Mostrar la ventana del juego
            gameView.show();

        } catch (IOException e) {
            e.printStackTrace();
            // Mostrar un error más grave si la vista del juego no puede cargar.
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error Crítico");
            alert.setHeaderText("No se pudo iniciar el juego.");
            alert.setContentText("Ocurrió un error al cargar la vista principal del juego. La aplicación se cerrará.");
            alert.showAndWait();
            Platform.exit();
        }
    }

    /**
     * Maneja el clic en el botón "Salir".
     * Cierra la aplicación.
     * @param event El evento de la acción.
     */
    @FXML
    void onExitClick(ActionEvent event) {
        Platform.exit();
    }
}
