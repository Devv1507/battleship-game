package univalle.tedesoft.battleship.controllers;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import univalle.tedesoft.battleship.models.State.SavedGameManager;
import univalle.tedesoft.battleship.views.GameView;
import univalle.tedesoft.battleship.views.InstructionsView;
import univalle.tedesoft.battleship.views.WelcomeView;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Controlador para la pantalla de bienvenida del juego Battleship.
 * Gestiona la entrada del nombre del jugador, la búsqueda de partidas guardadas 
 * y la transición a la vista principal del juego.
 */
public class WelcomeController {

    // Campos para nueva partida
    @FXML private TextField nameTextField;
    // Campos para búsqueda de partidas guardadas
    @FXML private ScrollPane savedGamesScrollPane;
    @FXML private VBox savedGamesContainer;

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
     */
    @FXML
    void onStartGameClick() {
        String playerName = this.nameTextField.getText().trim();

        if (playerName.isEmpty()) {
            this.welcomeView.showAlert(Alert.AlertType.WARNING, "Nombre Requerido", "Por favor, ingrese un nombre de capitán para comenzar la batalla.");
            return;
        }

        try {
            this.welcomeView.hide();
            GameView gameView = GameView.getInstance();
            gameView.initializeNewGame(new univalle.tedesoft.battleship.models.Players.HumanPlayer(playerName));
            gameView.show();
        } catch (IOException e) {
            System.err.println("ERROR IOException al cargar GameView: " + e.getMessage());
            e.printStackTrace();
            this.welcomeView.show();
            this.welcomeView.showAlert(Alert.AlertType.ERROR, "Error de Carga", "No se pudo cargar la vista del juego: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("ERROR general al iniciar juego: " + e.getMessage());
            e.printStackTrace();
            this.welcomeView.show();
            this.welcomeView.showAlert(Alert.AlertType.ERROR, "Error al Iniciar", "Ocurrió un error inesperado al iniciar el juego: " + e.getMessage());
        }
    }

    /**
     * Maneja el clic en el botón "Buscar Partidas".
     * Muestra u oculta el panel de partidas guardadas y lo puebla con la información encontrada,
     * filtrando por el nickname si este ha sido proporcionado.
     */
    @FXML
    void onShowSavedGamesClick() {
        String nicknameFilter = this.nameTextField.getText().trim();

        if (nicknameFilter.isEmpty()) {
            this.welcomeView.showAlert(Alert.AlertType.WARNING, "Nombre Requerido", "Por favor, ingrese un nombre de capitán para buscar sus partidas guardadas.");
            return;
        }

        if (this.welcomeView.toggleSavedGamesVisibility(this.savedGamesScrollPane)) {
            // El panel ahora está visible, así que buscamos y poblamos los datos.
            List<SavedGameManager.SavedGameInfo> savedGames = SavedGameManager.findSavedGamesByNickname(nicknameFilter);
            // Le pedimos a la vista que muestre los juegos, pasándole el contenedor y la lógica de carga.
            this.welcomeView.displaySavedGames(this.savedGamesContainer, savedGames, this::handleLoadGame);
        }
    }

    /**
     * Maneja el clic en el botón "Instrucciones".
     * Muestra la ventana de instrucciones del juego.
     */
    @FXML
    void onInstructionsClick() {
        try {
            InstructionsView.getInstance().show();
        } catch (IOException e) {
            System.err.println("ERROR IOException al cargar InstructionsView: " + e.getMessage());
            e.printStackTrace();
            this.welcomeView.showAlert(Alert.AlertType.ERROR, "Error de Carga", "No se pudieron cargar las instrucciones: " + e.getMessage());
        }
    }

    /**
     * Maneja el clic en el botón "Salir".
     * Cierra la aplicación.
     */
    @FXML
    void onExitClick() {
        Platform.exit();
    }

    /**
     * Lógica para cargar una partida seleccionada.
     * @param gameToLoad La información de la partida que se va a cargar.
     */
    public void handleLoadGame(SavedGameManager.SavedGameInfo gameToLoad) {
        if (gameToLoad == null) {
            this.welcomeView.showAlert(Alert.AlertType.ERROR, "Error", "No hay partida para cargar.");
            return;
        }

        try {
            this.welcomeView.hide();
            GameView gameView = GameView.getInstance();

            String nickname = gameToLoad.getNickname();
            boolean gameLoaded = gameView.getController().getGameState().loadGameByNickname(nickname);

            if (gameLoaded) {
                gameView.initializeLoadedGame();
                gameView.show();
                // Usamos Platform.runLater para asegurar que la alerta se muestre después de que la ventana del juego esté visible.
                Platform.runLater(() ->
                        this.welcomeView.showAlert(Alert.AlertType.INFORMATION, "Partida Cargada", "La partida de " + gameToLoad.getNickname() + " se ha cargado correctamente.")
                );
            } else {
                this.welcomeView.show();
                this.welcomeView.showAlert(Alert.AlertType.ERROR, "Error al Cargar", "No se pudo cargar la partida. Los archivos pueden estar corruptos.");
            }
        } catch (IOException e) {
            e.printStackTrace();
            this.welcomeView.showAlert(Alert.AlertType.ERROR, "Error Crítico", "No se pudo iniciar el juego: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            this.welcomeView.show();
            this.welcomeView.showAlert(Alert.AlertType.ERROR, "Error al Cargar", "Ocurrió un error inesperado al cargar la partida: " + e.getMessage());
        }
    }
}
