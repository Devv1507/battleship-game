package univalle.tedesoft.battleship.controllers;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import univalle.tedesoft.battleship.models.state.SavedGameManager;
import univalle.tedesoft.battleship.views.GameView;
import univalle.tedesoft.battleship.views.InstructionsView;
import univalle.tedesoft.battleship.views.WelcomeView;

import java.io.IOException;

/**
 * Controlador para la pantalla de bienvenida del juego Battleship.
 * Gestiona la entrada del nombre del jugador, la búsqueda de partidas guardadas
 * y la transición a la vista principal del juego.
 */
public class WelcomeController {

    // Campos para nueva partida
    @FXML private TextField nameTextField;
    @FXML private Button startGameButton;

    // Campos para búsqueda de partidas guardadas
    @FXML private TextField searchTextField;
    @FXML private Button searchGameButton;
    @FXML private VBox gameResultArea;
    @FXML private Label gameResultLabel;
    @FXML private Button loadGameButton;

    // Botones de navegación
    @FXML private Button exitButton;
    @FXML private Button instructionsButton;

    private WelcomeView welcomeView;
    private SavedGameManager.SavedGameInfo currentFoundGame;

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
            this.welcomeView.hide();

            GameView gameView = GameView.getInstance();
            gameView.initializeNewGame(new univalle.tedesoft.battleship.models.players.HumanPlayer(playerName));

            gameView.show();

        } catch (IOException e) {
            System.err.println("ERROR IOException al cargar GameView: " + e.getMessage());
            e.printStackTrace();

            // Mostrar la ventana de bienvenida nuevamente
            this.welcomeView.show();

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error de Carga");
            alert.setHeaderText("No se pudo cargar la vista del juego.");
            alert.setContentText("Error al cargar archivos FXML del juego: " + e.getMessage());
            alert.showAndWait();

        } catch (Exception e) {
            System.err.println("ERROR general al iniciar juego: " + e.getMessage());
            e.printStackTrace();

            // Mostrar la ventana de bienvenida nuevamente
            this.welcomeView.show();

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error al Iniciar");
            alert.setHeaderText("No se pudo iniciar el juego.");
            alert.setContentText("Error inesperado: " + e.getMessage());
            alert.showAndWait();
        }
    }

    /**
     * Maneja el clic en el botón "Buscar Partidas".
     * Busca partidas guardadas para el nickname especificado.
     * @param event El evento de la acción.
     */
    @FXML
    void onSearchGameClick(ActionEvent event) {
        String searchNickname = this.searchTextField.getText().trim();

        if (searchNickname.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Nombre Requerido");
            alert.setHeaderText(null);
            alert.setContentText("Por favor, ingrese un nombre de jugador para buscar partidas guardadas.");
            alert.showAndWait();
            return;
        }

        // Buscar la última partida guardada para este nickname
        SavedGameManager.SavedGameInfo lastSavedGame = SavedGameManager.getLastSavedGame(searchNickname);

        // Mostrar el área de resultados
        gameResultArea.setVisible(true);

        if (lastSavedGame != null) {
            // Se encontró una partida guardada
            currentFoundGame = lastSavedGame;

            String resultText = String.format(
                    "✓ Última partida encontrada para '%s':\n" +
                            "Fase: %s\n" +
                            "Barcos hundidos (Jugador): %d\n" +
                            "Barcos hundidos (Computadora): %d\n" +
                            "Guardada: %s",
                    lastSavedGame.getNickname(),
                    translateGamePhase(lastSavedGame.getGamePhase()),
                    lastSavedGame.getHumanSunkShips(),
                    lastSavedGame.getComputerSunkShips(),
                    lastSavedGame.getSaveDate().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
            );

            gameResultLabel.setText(resultText);
            loadGameButton.setVisible(true);

        } else {
            // No se encontró ninguna partida guardada
            currentFoundGame = null;

            String resultText = String.format(
                    "✗ No se encontraron partidas guardadas para '%s'.\n" +
                            "Este jugador no tiene partidas guardadas disponibles.",
                    searchNickname
            );

            gameResultLabel.setText(resultText);
            loadGameButton.setVisible(false);
        }
    }

    /**
     * Maneja el clic en el botón "Cargar Partida".
     * Carga la partida encontrada en la búsqueda.
     * @param event El evento de la acción.
     */
    @FXML
    void onLoadGameClick(ActionEvent event) {
        if (currentFoundGame == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("No hay partida para cargar");
            alert.setContentText("No se ha seleccionado ninguna partida válida para cargar.");
            alert.showAndWait();
            return;
        }

        try {
            // Ocultar la ventana de bienvenida
            this.welcomeView.hide();

            // Obtener la instancia de la vista del juego
            GameView gameView = GameView.getInstance();

            // Cargar la partida específica usando el nuevo sistema por nickname
            String nickname = currentFoundGame.getNickname();
            boolean gameLoaded = gameView.getController().getGameState().loadGameByNickname(nickname);

            if (gameLoaded) {
                // Inicializar la vista con la partida cargada
                gameView.initializeLoadedGame();

                // Mostrar la ventana del juego
                gameView.show();

                // Mostrar mensaje de confirmación
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Partida Cargada");
                alert.setHeaderText("¡Éxito!");
                alert.setContentText("La partida de " + currentFoundGame.getNickname() + " se ha cargado correctamente.");
                alert.show();

            } else {
                // Error al cargar
                this.welcomeView.show(); // Volver a mostrar la ventana de bienvenida

                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error al Cargar");
                alert.setHeaderText("No se pudo cargar la partida");
                alert.setContentText("Ocurrió un error al intentar cargar la partida guardada. Los archivos pueden estar corruptos.");
                alert.showAndWait();
            }

        } catch (IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error Crítico");
            alert.setHeaderText("No se pudo iniciar el juego.");
            alert.setContentText("Ocurrió un error al cargar la vista principal del juego: " + e.getMessage());
            alert.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
            // Mostrar la ventana de bienvenida nuevamente si hay un error
            this.welcomeView.show();

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error al Cargar");
            alert.setHeaderText("No se pudo cargar la partida.");
            alert.setContentText("Ocurrió un error inesperado: " + e.getMessage());
            alert.showAndWait();
        }
    }

    /**
     * Maneja el clic en el botón "Instrucciones".
     * Muestra la ventana de instrucciones del juego.
     * @param event El evento de la acción.
     */
    @FXML
    void onInstructionsClick(ActionEvent event) {
        try {
            InstructionsView instructionsView = InstructionsView.getInstance();
            instructionsView.show();

        } catch (IOException e) {
            System.err.println("ERROR IOException al cargar InstructionsView: " + e.getMessage());
            e.printStackTrace();

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error de Carga");
            alert.setHeaderText("No se pudieron cargar las instrucciones.");
            alert.setContentText("Error al cargar archivo FXML de instrucciones: " + e.getMessage());
            alert.showAndWait();

        } catch (Exception e) {
            System.err.println("ERROR general al abrir instrucciones: " + e.getMessage());
            e.printStackTrace();

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error al Abrir Instrucciones");
            alert.setHeaderText("No se pudieron abrir las instrucciones.");
            alert.setContentText("Error inesperado: " + e.getMessage());
            alert.showAndWait();
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

    /**
     * Traduce las fases del juego a texto más amigable para el usuario.
     * @param phase La fase del juego en inglés
     * @return La traducción en español
     */
    private String translateGamePhase(String phase) {
        switch (phase.toUpperCase()) {
            case "INITIAL":
                return "Inicial";
            case "PLACEMENT":
                return "Colocación de Barcos";
            case "BATTLE":
                return "En Batalla";
            case "GAME_OVER":
                return "Juego Terminado";
            default:
                return phase;
        }
    }


}
