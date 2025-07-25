package univalle.tedesoft.battleship.views;

import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import univalle.tedesoft.battleship.Main;
import univalle.tedesoft.battleship.controllers.WelcomeController;
import univalle.tedesoft.battleship.models.State.SavedGameManager;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.Consumer;

/**
 * Representa la vista de bienvenida del juego Battleship.
 * Es la primera ventana que ve el usuario y se encarga de la manipulación de la UI de bienvenida.
 */
public class WelcomeView extends Stage {
    private static class WelcomeViewHolder {
        private static WelcomeView INSTANCE;
    }

    /**
     * Devuelve la instancia única de WelcomeView, creándola si es necesario.
     *
     * @return La instancia singleton de WelcomeView.
     * @throws IOException Si ocurre un error al cargar el archivo FXML.
     */
    public static WelcomeView getInstance() throws IOException {
        if (WelcomeViewHolder.INSTANCE == null) {
            WelcomeViewHolder.INSTANCE = new WelcomeView();
        }
        return WelcomeViewHolder.INSTANCE;
    }

    /**
     * Constructor privado para implementar el patrón Singleton.
     * Carga la vista desde el archivo FXML, establece la comunicación con su controlador y obtiene referencias a los componentes de la UI.
     *
     * @throws IOException Si falla la carga del FXML.
     */
    private WelcomeView() throws IOException {
        FXMLLoader loader = new FXMLLoader(Main.class.getResource("welcome-view.fxml"));
        Scene scene = new Scene(loader.load());
        WelcomeController controller = loader.getController();

        if (controller == null) {
            throw new IOException("No se pudo obtener el WelcomeController desde el FXML.");
        }

        // Enlazar la vista con el controlador
        controller.setWelcomeView(this);
        this.setTitle("Battleship - Puesto de Mando");
        this.setScene(scene);
    }

    /**
     * Muestra una alerta en la pantalla.
     *
     * @param alertType El tipo de alerta (ERROR, WARNING, INFORMATION).
     * @param title     El título de la ventana de alerta.
     * @param content   El mensaje principal de la alerta.
     */
    public void showAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    /**
     * Alterna la visibilidad de un panel de partidas guardadas.
     * @param scrollPane El componente ScrollPane a mostrar u ocultar.
     * @return `true` si el panel es ahora visible, `false` en caso contrario.
     */
    public boolean toggleSavedGamesVisibility(ScrollPane scrollPane) {
        boolean isNowVisible = !scrollPane.isVisible();
        scrollPane.setVisible(isNowVisible);
        scrollPane.setManaged(isNowVisible);
        return isNowVisible;
    }

    /**
     * Rellena un contenedor de la UI con las partidas guardadas.
     *
     * @param container  El VBox donde se insertarán las tarjetas de las partidas.
     * @param games      La lista de información de partidas guardadas obtenida del modelo.
     * @param loadAction La acción (del controlador) a ejecutar cuando se presiona el botón "Cargar".
     */
    public void displaySavedGames(VBox container, List<SavedGameManager.SavedGameInfo> games, Consumer<SavedGameManager.SavedGameInfo> loadAction) {
        container.getChildren().clear();

        if (games.isEmpty()) {
            Label noGamesLabel = new Label("No se encontraron partidas para el capitán especificado.");
            noGamesLabel.setFont(new Font("Arial Italic", 14));
            noGamesLabel.setStyle("-fx-text-fill: #cccccc;");
            container.getChildren().add(noGamesLabel);
        } else {
            for (SavedGameManager.SavedGameInfo gameInfo : games) {
                Node gameCard = this.createSavedGameCard(gameInfo, loadAction);
                container.getChildren().add(gameCard);
            }
        }
    }

    /**
     * Crea un componente de UI (una "tarjeta") para mostrar la información de una partida guardada.
     * @param gameInfo El objeto con los datos de la partida guardada.
     * @param loadAction La acción a vincular al botón "Cargar" de esta tarjeta.
     * @return Un nodo de JavaFX que representa la tarjeta.
     */
    private Node createSavedGameCard(SavedGameManager.SavedGameInfo gameInfo, Consumer<SavedGameManager.SavedGameInfo> loadAction) {
        HBox card = new HBox(15);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setStyle("-fx-background-color: rgba(255, 255, 255, 0.1); -fx-background-radius: 10; -fx-border-color: #f39c12; -fx-border-radius: 10;");
        card.setPadding(new Insets(10));

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

        Button loadButton = new Button("Cargar");
        loadButton.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8;");
        loadButton.setOnAction(event -> loadAction.accept(gameInfo));

        HBox spacer = new HBox();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        card.getChildren().addAll(infoContainer, spacer, loadButton);
        return card;
    }

    /**
     * Traduce las fases del juego a texto más amigable para el usuario.
     * @param phase La fase del juego en inglés
     * @return La traducción en español
     */
    private String translateGamePhase(String phase) {
        if (phase == null) return "Desconocida";
        switch (phase.toUpperCase()) {
            case "INITIAL": return "Inicial";
            case "PLACEMENT": return "Colocación de Barcos";
            case "FIRING": return "En Batalla";
            case "GAME_OVER": return "Juego Terminado";
            default: return phase;
        }
    }
}