package univalle.tedesoft.battleship.views;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import univalle.tedesoft.battleship.Main;
import univalle.tedesoft.battleship.controllers.GameController;
import univalle.tedesoft.battleship.models.Board;
import univalle.tedesoft.battleship.models.Enums.CellState;
import univalle.tedesoft.battleship.models.Enums.ShipType;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * Gestiona la ventana principal y todos los elementos de la interfaz de usuario del juego.
 * Implementa la interfaz IGameView para realizar todas las manipulaciones de la UI,
 * utilizando los componentes FXML que le proporciona el GameController.
 */
public class GameView extends Stage {

    private GameController controller;

    // ------------ Constantes
    private static final int CELL_SIZE = 40;
    private final Map<ShipType, Image> shipImages;

    /**
     * Constructor privado Singleton.
     * Carga el FXML, obtiene la referencia al controlador y se la pasa a sí mismo (el Stage).
     * @throws IOException si el archivo FXML no se puede cargar.
     */
    public GameView() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(
                Main.class.getResource("game-view.fxml")
        );
        Scene scene = new Scene(fxmlLoader.load());

        this.controller = fxmlLoader.getController();
        if (this.controller == null) {
            throw new IllegalStateException("El controlador no se pudo cargar desde el FXML. Revisa el campo fx:controller.");
        }
        this.controller.setGameView(this);

        this.shipImages = Map.of(
                ShipType.AIR_CRAFT_CARRIER, this.loadImage("images/aircraft_carrier.png"),
                ShipType.SUBMARINE, this.loadImage("images/submarine.png"),
                ShipType.DESTROYER, this.loadImage("images/destroyer.png"),
                ShipType.FRIGATE, this.loadImage("images/frigate.png")
        );
        this.setTitle("Battleship Game");
        this.setScene(scene);
    }


    /**
     * Inicializa la apariencia visual de la UI. Se llama desde el controlador
     * una vez que la vista ha sido enlazada.
     * @param controller La instancia del controlador para asignar listeners.
     */
    public void initializeUI(GameController controller) {
        this.initializeBoardGrid(controller.humanPlayerBoardGrid, true);
        this.initializeBoardGrid(controller.machinePlayerBoardGrid, false);
    }


    // ------------ Lógica principal

    private void initializeBoardGrid(GridPane boardGrid, boolean isHumanBoard) {
        for (int row = 0; row < 10; row++) {
            for (int col = 0; col < 10; col++) {
                Pane cellPane = new Pane();
                cellPane.getStyleClass().add("cell"); // Para CSS
                cellPane.setPrefSize(CELL_SIZE, CELL_SIZE);

                final int finalRow = row;
                final int finalCol = col;
                cellPane.setOnMouseClicked(event -> {
                    if (isHumanBoard) {
                        this.controller.handlePlacementCellClick(finalRow, finalCol);
                    } else {
                        this.controller.handleFiringCellClick(finalRow, finalCol);
                    }
                });
                boardGrid.add(cellPane, col, row);
            }
        }
    }

    public void showShipPlacementPhase(Board playerPositionBoard, List<ShipType> shipsToPlace) {
        this.controller.shipPlacementPane.setVisible(true);
        this.controller.finalizePlacementButton.setDisable(false);
        this.controller.machinePlayerBoardGrid.setDisable(true);

        this.controller.shipPlacementPane.getChildren().remove(1, this.controller.shipPlacementPane.getChildren().size());

        for (ShipType type : shipsToPlace) {
            ImageView shipImageView = new ImageView(this.shipImages.get(type));
            shipImageView.setPreserveRatio(true);
            shipImageView.setFitWidth(150);

            shipImageView.setOnMouseClicked(event -> {
                this.controller.handleShipSelection(type);
            });
            this.controller.shipPlacementPane.getChildren().add(shipImageView);
        }
    }

    public void showFiringPhase(Board playerPositionBoard, Board machineTerritoryBoard) {
        this.controller.shipPlacementPane.setVisible(false);
        this.controller.finalizePlacementButton.setDisable(true);
        this.controller.humanPlayerBoardGrid.setDisable(true);
        this.controller.machinePlayerBoardGrid.setDisable(false);

        displayMessage("¡Comienza la batalla! Haz clic en el tablero enemigo para disparar.", false);
        drawBoard(this.controller.humanPlayerBoardGrid, playerPositionBoard, true);
        drawBoard(this.controller.machinePlayerBoardGrid, machineTerritoryBoard, false);
    }

    public void drawBoard(GridPane gridPane, Board board, boolean showShips) {
        for (int row = 0; row < board.getSize(); row++) {
            for (int col = 0; col < board.getSize(); col++) {
                CellState state = board.getCellState(row, col);
                Pane cellPane = getCellPane(gridPane, row, col);
                if (cellPane == null) continue;

                cellPane.getChildren().clear();

                Rectangle marker = new Rectangle(CELL_SIZE - 2, CELL_SIZE - 2);
                marker.setArcWidth(10);
                marker.setArcHeight(10);

                switch (state) {
                    case SHIP:
                        if (showShips) {
                            marker.setFill(Color.DIMGRAY);
                            cellPane.getChildren().add(marker);
                        }
                        break;
                    case HIT_SHIP:
                        marker.setFill(Color.ORANGERED);
                        cellPane.getChildren().add(marker);
                        break;
                    case SHOT_LOST_IN_WATER:
                        marker.setFill(Color.ROYALBLUE);
                        cellPane.getChildren().add(marker);
                        break;
                    case SUNK_SHIP_PART:
                        marker.setFill(Color.DARKRED);
                        cellPane.getChildren().add(marker);
                        break;
                }
            }
        }
    }

    public void displayMessage(String message, boolean isError) {
        this.controller.messageLabel.setText(message);
        if (isError) {
            this.controller.messageLabel.setStyle("-fx-text-fill: red;");
        } else {
            this.controller.messageLabel.setStyle("-fx-text-fill: black;");
        }
    }

    // ------------ Métodos auxiliares

    private Pane getCellPane(GridPane gridPane, int row, int col) {
        for (Node node : gridPane.getChildren()) {
            if (GridPane.getRowIndex(node) == row && GridPane.getColumnIndex(node) == col) {
                return (Pane) node;
            }
        }
        return null;
    }

    private Image loadImage(String path) {
        InputStream stream = getClass().getResourceAsStream("/univalle/tedesoft/battleship/" + path);
        if (stream == null) {
            System.err.println("Error: No se pudo cargar el recurso de imagen: " + path);
            return null;
        }
        return new Image(stream);
    }


    /**
     * Permite obtener la instancia del controlador asociado a esta vista.
     * @return El GameController de la vista.
     */
    public GameController getController() {
        return controller;
    }

    // --- Singleton Holder Pattern ---
    private static class GameViewHolder {
        private static GameView INSTANCE;
    }

    public static GameView getInstance() throws IOException {
        if (GameViewHolder.INSTANCE == null) {
            GameViewHolder.INSTANCE = new GameView();
            return GameViewHolder.INSTANCE;
        } else {
            return GameViewHolder.INSTANCE;
        }
    }
}