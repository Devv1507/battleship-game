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
    @FXML private Button startGameButton;
    @FXML private Button showSavedGamesButton;

    // Campos para búsqueda de partidas guardadas
    @FXML private ScrollPane savedGamesScrollPane;
    @FXML private VBox savedGamesContainer;

    // Botones de navegación
    @FXML private Button exitButton;
    @FXML private Button instructionsButton;

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
            this.showAlert(Alert.AlertType.WARNING, "Nombre Requerido", "Por favor, ingrese un nombre de capitán para comenzar la batalla.");
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
            this.showAlert(Alert.AlertType.ERROR, "Error de Carga", "No se pudo cargar la vista del juego: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("ERROR general al iniciar juego: " + e.getMessage());
            e.printStackTrace();
            this.welcomeView.show();
            this.showAlert(Alert.AlertType.ERROR, "Error al Iniciar", "Ocurrió un error inesperado al iniciar el juego: " + e.getMessage());
        }
    }

    /**
     * Maneja el clic en el botón "Buscar Partidas".
     * Muestra u oculta el panel de partidas guardadas y lo puebla con la información encontrada,
     * filtrando por el nickname si este ha sido proporcionado.
     */
    @FXML
    void onShowSavedGamesClick() {
        // Obtener el texto del campo de nombre para usarlo como filtro.
        String nicknameFilter = this.nameTextField.getText().trim();

        // Validar el campo de nombre.
        if (nicknameFilter.isEmpty()) {
            this.showAlert(Alert.AlertType.WARNING, "Nombre Requerido", "Por favor, ingrese un nombre de capitán para buscar sus partidas guardadas.");
            // Ocultar la lista si ya estaba visible y el campo ahora está vacío.
            if (this.savedGamesScrollPane.isVisible()) {
                this.savedGamesScrollPane.setVisible(false);
                this.savedGamesScrollPane.setManaged(false);
            }
            return; // Detener la ejecución si el nombre está vacío.
        }

        boolean isVisible = this.savedGamesScrollPane.isVisible();
        this.savedGamesScrollPane.setVisible(!isVisible);
        this.savedGamesScrollPane.setManaged(!isVisible);

        if (!isVisible) {
            this.populateSavedGamesList(nicknameFilter);
        }
    }

    /**
     * Obtiene la lista de partidas guardadas y las muestra en la interfaz.
     * Si se proporciona un `nicknameFilter`, solo muestra las partidas de ese jugador.
     * @param nicknameFilter El nombre del jugador por el cual filtrar, o una cadena vacía para mostrar todos.
     */
    private void populateSavedGamesList(String nicknameFilter) {
        this.savedGamesContainer.getChildren().clear();

        List<SavedGameManager.SavedGameInfo> savedGames = SavedGameManager.findSavedGamesByNickname(nicknameFilter);

        if (savedGames.isEmpty()) {
            String message = "No se encontraron partidas para el capitán '" + nicknameFilter + "'.";

            Label noGamesLabel = new Label(message);
            noGamesLabel.setFont(new Font("Arial Italic", 14));
            noGamesLabel.setStyle("-fx-text-fill: #cccccc;");
            this.savedGamesContainer.getChildren().add(noGamesLabel);
        } else {
            for (SavedGameManager.SavedGameInfo gameInfo : savedGames) {
                Node gameCard = this.createSavedGameCard(gameInfo);
                this.savedGamesContainer.getChildren().add(gameCard);
            }
        }
    }

    /**
     * Crea un componente de UI (una "tarjeta") para mostrar la información de una partida guardada.
     * @param gameInfo El objeto con los datos de la partida guardada.
     * @return Un nodo de JavaFX que representa la tarjeta.
     */
    private Node createSavedGameCard(SavedGameManager.SavedGameInfo gameInfo) {
        // Contenedor principal de la tarjeta
        HBox card = new HBox(15);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setStyle("-fx-background-color: rgba(255, 255, 255, 0.1); -fx-background-radius: 10; -fx-border-color: #f39c12; -fx-border-radius: 10;");
        card.setPadding(new Insets(10));

        // Contenedor para la información textual
        VBox infoContainer = new VBox(5);
        infoContainer.setAlignment(Pos.CENTER_LEFT);

        Label nameLabel = new Label("Capitán: " + gameInfo.getNickname());
        nameLabel.setFont(new Font("Arial Bold", 16));
        nameLabel.setStyle("-fx-text-fill: white;");

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        Label dateLabel = new Label("Guardada: " + gameInfo.getSaveDate().format(formatter));
        dateLabel.setFont(new Font("Arial", 12));
        dateLabel.setStyle("-fx-text-fill: #e0e0e0;");

        Label phaseLabel = new Label("Fase: " + this.translateGamePhase(gameInfo.getGamePhase()));
        phaseLabel.setFont(new Font("Arial", 12));
        phaseLabel.setStyle("-fx-text-fill: #e0e0e0;");

        infoContainer.getChildren().addAll(nameLabel, phaseLabel, dateLabel);

        // Botón para cargar la partida
        Button loadButton = new Button("Cargar");
        loadButton.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8;");
        loadButton.setOnAction(event -> this.handleLoadGame(gameInfo));

        // Espaciador para empujar el botón a la derecha
        HBox spacer = new HBox();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        card.getChildren().addAll(infoContainer, spacer, loadButton);
        return card;
    }

    /**
     * Lógica para cargar una partida seleccionada.
     * @param gameToLoad La información de la partida que se va a cargar.
     */
    private void handleLoadGame(SavedGameManager.SavedGameInfo gameToLoad) {
        if (gameToLoad == null) {
            this.showAlert(Alert.AlertType.ERROR, "Error", "No hay partida para cargar.");
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
                this.showAlert(Alert.AlertType.INFORMATION, "Partida Cargada", "La partida de " + gameToLoad.getNickname() + " se ha cargado correctamente.");
            } else {
                this.welcomeView.show();
                this.showAlert(Alert.AlertType.ERROR, "Error al Cargar", "No se pudo cargar la partida. Los archivos pueden estar corruptos.");
            }
        } catch (IOException e) {
            e.printStackTrace();
            this.showAlert(Alert.AlertType.ERROR, "Error Crítico", "No se pudo iniciar el juego: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            this.welcomeView.show();
            this.showAlert(Alert.AlertType.ERROR, "Error al Cargar", "Ocurrió un error inesperado al cargar la partida: " + e.getMessage());
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
            InstructionsView.getInstance().show();
        } catch (IOException e) {
            System.err.println("ERROR IOException al cargar InstructionsView: " + e.getMessage());
            e.printStackTrace();
            this.showAlert(Alert.AlertType.ERROR, "Error de Carga", "No se pudieron cargar las instrucciones: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("ERROR general al abrir instrucciones: " + e.getMessage());
            e.printStackTrace();
            this.showAlert(Alert.AlertType.ERROR, "Error", "Ocurrió un error inesperado al abrir las instrucciones: " + e.getMessage());
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


    /**
     * Muestra alertas de forma centralizada.
     * @param alertType El tipo de alerta (ERROR, WARNING, INFORMATION).
     * @param title El título de la ventana de alerta.
     * @param content El mensaje principal de la alerta.
     */
    private void showAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
