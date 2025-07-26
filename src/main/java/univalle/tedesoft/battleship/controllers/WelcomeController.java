package univalle.tedesoft.battleship.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import univalle.tedesoft.battleship.models.State.SavedGameManager;
import univalle.tedesoft.battleship.views.GameView;
import univalle.tedesoft.battleship.views.InstructionsView;
import univalle.tedesoft.battleship.views.ViewUtils;
import univalle.tedesoft.battleship.views.WelcomeView;

import java.io.IOException;
import java.util.List;

/**
 * Controlador para la pantalla de bienvenida del juego Battleship.
 * Gestiona la entrada del nombre del jugador, la búsqueda de partidas guardadas 
 * y la transición a la vista principal del juego.
 */
public class WelcomeController {
    // --- Componentes FXML ---
    @FXML private TextField nameTextField;
    @FXML private Button startGameButton;
    @FXML private Button showSavedGamesButton;
    @FXML private Button exitButton;
    @FXML private Button instructionsButton;
    // Campos para búsqueda de partidas guardadas
    @FXML private ScrollPane savedGamesScrollPane;
    @FXML private VBox savedGamesContainer;
    private WelcomeView welcomeView;

    /**
     * Establece la referencia a la vista de bienvenida que este controlador maneja.
     * Esta referencia se utiliza para interactuar con la vista, como mostrar alertas o aplicar efectos visuales a los botones.
     * @param welcomeView La instancia de la vista de bienvenida.
     */
    public void setWelcomeView(WelcomeView welcomeView) {
        this.welcomeView = welcomeView;
        this.initializeButtonEffects();
    }

    // --------- Event handlers con FXML

    /**
     * Maneja el clic en el botón "¡A la Batalla!".
     * Valida el nombre del jugador y transiciona a la vista del juego.
     */
    @FXML
    void onStartGameClick() {
        String playerName = this.nameTextField.getText().trim();

        if (playerName.isEmpty()) {
            ViewUtils.showAlert(AlertType.WARNING, "Nombre Requerido", "Por favor, ingrese un nombre de capitán para comenzar la batalla.");
            return;
        }

        try {
            this.welcomeView.hide();
            GameView gameView = GameView.getInstance();
            gameView.initializeNewGame(new univalle.tedesoft.battleship.models.Players.HumanPlayer(playerName));
            gameView.show();
        } catch (IOException e) {
            this.welcomeView.show();
            ViewUtils.showAlert(AlertType.ERROR, "Error de Carga", "No se pudo cargar la vista del juego: " + e.getMessage());
        } catch (Exception e) {
            this.welcomeView.show();
            ViewUtils.showAlert(AlertType.ERROR, "Error al Iniciar", "Ocurrió un error inesperado al iniciar el juego: " + e.getMessage());
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
            ViewUtils.showAlert(AlertType.WARNING, "Nombre Requerido", "Por favor, ingrese un nombre de capitán para buscar sus partidas guardadas.");
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
            ViewUtils.showAlert(AlertType.ERROR, "Error de Carga", "No se pudieron cargar las instrucciones: " + e.getMessage());
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

    // ----- Lógica principal: carga de partidas guardadas y establecimiento de efectos visuales -----

    /**
     * Lógica para cargar una partida seleccionada.
     * @param gameToLoad La información de la partida que se va a cargar.
     */
    public void handleLoadGame(SavedGameManager.SavedGameInfo gameToLoad) {
        if (gameToLoad == null) {
            ViewUtils.showAlert(AlertType.ERROR, "Error", "No hay partida para cargar.");
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
                        ViewUtils.showAlert(AlertType.INFORMATION, "Partida Cargada", "La partida de " + gameToLoad.getNickname() + " se ha cargado correctamente.")
                );
            } else {
                this.welcomeView.show();
                ViewUtils.showAlert(AlertType.ERROR, "Error al Cargar", "No se pudo cargar la partida. Los archivos pueden estar corruptos.");
            }
        } catch (IOException e) {
            ViewUtils.showAlert(AlertType.ERROR, "Error Crítico", "No se pudo iniciar el juego: " + e.getMessage());
        } catch (Exception e) {
            this.welcomeView.show();
            ViewUtils.showAlert(AlertType.ERROR, "Error al Cargar", "Ocurrió un error inesperado al cargar la partida: " + e.getMessage());
        }
    }

    /**
     * Orquesta la aplicación de efectos visuales a los botones, delegando la lógica a la vista.
     */
    private void initializeButtonEffects() {
        ViewUtils.applyHoverScaleEffect(this.startGameButton);
        ViewUtils.applyHoverScaleEffect(this.showSavedGamesButton);
        ViewUtils.applyHoverScaleEffect(this.instructionsButton);
        ViewUtils.applyHoverScaleEffect(this.exitButton);
    }
}
