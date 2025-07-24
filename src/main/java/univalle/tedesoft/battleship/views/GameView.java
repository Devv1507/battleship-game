package univalle.tedesoft.battleship.views;


import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Scale;
import javafx.stage.Stage;
import univalle.tedesoft.battleship.Main;
import univalle.tedesoft.battleship.controllers.GameController;
import univalle.tedesoft.battleship.models.Board;
import univalle.tedesoft.battleship.models.Coordinate;
import univalle.tedesoft.battleship.models.Enums.CellState;
import univalle.tedesoft.battleship.models.Enums.Orientation;
import univalle.tedesoft.battleship.models.Enums.ShipType;
import univalle.tedesoft.battleship.models.Players.HumanPlayer;
import univalle.tedesoft.battleship.models.Ships.Ship;
import univalle.tedesoft.battleship.models.State.GameState;
import univalle.tedesoft.battleship.models.State.IGameState;
import univalle.tedesoft.battleship.views.shapes.*;

import java.io.IOException;
import java.util.*;

/**
 * Gestiona la ventana principal y todos los elementos de la interfaz de usuario del juego.
 * Implementa la interfaz IGameView para realizar todas las manipulaciones de la UI,
 * utilizando los componentes FXML que le proporciona el GameController.
 */
public class GameView extends Stage {

    private GameController controller;

    // ------------ Constantes
    private static final int CELL_SIZE = 40;
    /** Mapa para las figuras de los barcos */
    private final Map<ShipType, ShipShape> shipShapeFactory;
    /** Mapa para las figuras de los marcadores de disparo */
    private final Map<CellState, IMarkerShape> markerShapeFactory;
    private static final int MAX_MESSAGES = 2; // Mostrar los últimos 2 mensajes


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
        // Inicializar la fábrica de formas de barcos
        this.shipShapeFactory = new HashMap<>();
        this.shipShapeFactory.put(ShipType.AIR_CRAFT_CARRIER, new AircraftShape());
        this.shipShapeFactory.put(ShipType.SUBMARINE, new SubmarineShape());
        this.shipShapeFactory.put(ShipType.DESTROYER, new DestroyerShape());
        this.shipShapeFactory.put(ShipType.FRIGATE, new FrigateShape());

        // Inicializar la fábrica de formas de marcadores
        this.markerShapeFactory = new HashMap<>();
        this.markerShapeFactory.put(CellState.SHOT_LOST_IN_WATER, new WaterMarkerShape());
        this.markerShapeFactory.put(CellState.HIT_SHIP, new TouchedMarkerShape());
        this.markerShapeFactory.put(CellState.SUNK_SHIP_PART, new SunkenMarkerShape());

        IGameState gameState = new GameState();
        this.controller.setGameView(this);
        this.controller.setGameState(gameState);

        this.controller.initializeUI(this);
        gameState.startNewGame(new HumanPlayer("Capitán")); // Inicia el modelo con un jugador por defecto
        this.showShipPlacementPhase(
                gameState.getHumanPlayerPositionBoard(),
                gameState.getPendingShipsToPlace()
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

                // Las etiquetas de coordenadas se manejan dinámicamente en drawBoard()

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
        // El botón solo se habilita si no quedan barcos por colocar.
        this.controller.finalizePlacementButton.setDisable(!shipsToPlace.isEmpty());
        this.controller.machinePlayerBoardGrid.setDisable(true);
        this.controller.humanPlayerBoardGrid.setDisable(false);

        this.controller.shipPlacementPane.getChildren().remove(1, this.controller.shipPlacementPane.getChildren().size());

        // Definir los anchos deseados para cada tipo de barco en el panel de selección.
        final Map<ShipType, Double> targetWidths = Map.of(
                ShipType.FRIGATE, 50.0,           // La más pequeña (la mitad del destructor).
                ShipType.DESTROYER, 100.0,        // Nuestro tamaño base.
                ShipType.SUBMARINE, 125.0,        // Proporcionalmente más grande.
                ShipType.AIR_CRAFT_CARRIER, 200.0 // La más grande (el doble del destructor).
        );

        for (ShipType type : shipsToPlace) {
            // Obtener la fábrica de formas correcta desde nuestro mapa usando polimorfismo.
            ShipShape shapeFactory = this.shipShapeFactory.get(type);

            if (shapeFactory != null) {
                // Crear la forma del barco. Esto nos devuelve un Node (un Group con todas las partes).
                Node shipVisualNode = shapeFactory.createShape();
                // Obtener el ancho objetivo dinámicamente del mapa.
                double targetWidth = targetWidths.getOrDefault(type, 150.0);

                // Calcular el factor de escala para que el barco encaje en el ancho deseado.
                double originalWidth = shipVisualNode.getBoundsInLocal().getWidth();
                double scaleFactor = targetWidth / originalWidth;

                // Aplicar la transformación de escala.
                Scale scale = new Scale(scaleFactor, scaleFactor);
                shipVisualNode.getTransforms().add(scale);

                // Crear un contenedor para la forma, para centrarla y manejarla fácilmente.
                VBox container = new VBox(shipVisualNode);
                container.setAlignment(Pos.CENTER);
                container.setPadding(new Insets(5, 0, 5, 0)); // Espaciado vertical
                container.getStyleClass().add("ship-selector-item"); // Para futuro estilo con CSS

                container.setOnMouseClicked(event -> this.controller.handleShipSelection(type));

                // 7. Añadir el contenedor (con el barco escalado dentro) al panel de colocación.
                this.controller.shipPlacementPane.getChildren().add(container);
            }
        }
    }

    /**
     * Dibuja el estado completo de un tablero.
     * En esta versión, se ha modificado para dibujar los segmentos de los barcos
     * con colores específicos según su tipo.
     *
     * @param gridPane  El GridPane de fondo sobre el cual dibujar.
     * @param board     El objeto Board del modelo que contiene el estado a dibujar.
     * @param showShips Un booleano que indica si los barcos deben ser visibles.
     */
    public void drawBoard(GridPane gridPane, Board board, boolean showShips) {
        // Determinar qué Pane de dibujo usar basándose en el GridPane proporcionado.
        Pane drawingPane;
        if (gridPane == this.controller.humanPlayerBoardGrid) {
            drawingPane = this.controller.humanPlayerDrawingPane;
        } else {
            drawingPane = this.controller.machinePlayerDrawingPane;
        }

        // 1. Limpieza del tablero
        // Limpiar el canvas de dibujo de barcos viejos.
        drawingPane.getChildren().clear();

        // Limpiar los marcadores de las celdas del GridPane.
        for (Node node : gridPane.getChildren()) {
            if (node instanceof Pane) {
                Pane cellPane = (Pane) node;
                cellPane.getChildren().clear();
                cellPane.setStyle(""); // Restaurar estilo por defecto.
            }
        }

        // Lógica de dibujo de barcos
        // Primero, dibujamos todos los barcos que deben ser visibles.
        Set<Ship> drawnShips = new HashSet<>();
        // Iteramos directamente sobre la lista de barcos
        for (Ship ship : board.getShips()) {
            if (drawnShips.contains(ship)) continue;

            // La condición para dibujar el barco es la clave:
            // 1. Siempre se dibuja en el tablero del jugador humano.
            // 2. O el modo "showShips" (profesor) está activo.
            // 3. O el barco está hundido.
            boolean isHumanBoard = (gridPane == this.controller.humanPlayerBoardGrid);
            if (isHumanBoard || showShips || ship.isSunk()) {

                Coordinate headCoordinate = ship.getOccupiedCoordinates().get(0);
                Node shipVisualNode = createAndPositionShipVisual(ship, headCoordinate.getX(), headCoordinate.getY());

                // Aplicar efecto visual si está hundido
                if (ship.isSunk()) {
                    // Desaturado y oscuro
                    shipVisualNode.setEffect(new ColorAdjust(0, -0.5, -0.2, 0));
                    shipVisualNode.setOpacity(0.8);
                }

                drawingPane.getChildren().add(shipVisualNode);
                drawnShips.add(ship);
            }
        }

        // Lógica de dibujo de marcadores de estado de celdas y manejo de etiquetas de coordenadas
        boolean isEnemyBoard = (gridPane == this.controller.machinePlayerBoardGrid);
        for (int row = 0; row < board.getSize(); row++) {
            for (int col = 0; col < board.getSize(); col++) {
                CellState state = board.getCellState(row, col);
                Pane cellPane = this.getCellPane(gridPane, row, col);
                
                if (cellPane != null) {
                    // En el tablero enemigo, manejar las etiquetas de coordenadas según el estado de la celda
                    if (isEnemyBoard) {
                        if (state == CellState.EMPTY || state == CellState.SHIP) {
                            // Celda no atacada - mostrar etiqueta de coordenada si no existe
                            boolean hasCoordinateLabel = cellPane.getChildren().stream()
                                    .anyMatch(child -> child instanceof Label);
                            
                            if (!hasCoordinateLabel) {
                                char columnLetter = (char) ('A' + col);
                                int rowNumber = row + 1;
                                String coordinateText = String.format("%c%d", columnLetter, rowNumber);
                                
                                Label coordinateLabel = new Label(coordinateText);
                                coordinateLabel.setFont(new Font("Arial Bold", 14));
                                coordinateLabel.setStyle("-fx-text-fill: black; -fx-background-color: transparent;");
                                coordinateLabel.setMouseTransparent(true);
                                coordinateLabel.setPrefSize(CELL_SIZE, CELL_SIZE);
                                coordinateLabel.setAlignment(Pos.CENTER);
                                
                                cellPane.getChildren().add(coordinateLabel);
                            }
                        } else {
                            // Celda atacada - remover etiqueta de coordenada si existe
                            cellPane.getChildren().removeIf(child -> child instanceof Label);
                        }
                    }
                }
                
                // Continuar con la lógica original de marcadores para celdas atacadas
                if (state == CellState.EMPTY || state == CellState.SHIP) continue;

                // Buscar en la fábrica si hay un marcador para el estado actual de la celda.
                IMarkerShape markerFactory = this.markerShapeFactory.get(state);
                if (markerFactory != null) {
                    // Crear el marcador visual
                    Node markerVisualNode = markerFactory.createMarker();

                    // Aplicar solo si el marcador es una instancia de FlameMarkerShape.
                    if (markerFactory instanceof SunkenMarkerShape) {
                        // Hacer la llama un 60% del tamaño de la celda
                        double scaleFactor = 0.6;
                        markerVisualNode.setScaleX(scaleFactor);
                        markerVisualNode.setScaleY(scaleFactor);

                        // La llama debe estar ENCIMA del barco y CENTRADA.
                        // Usar un StackPane para centrar la llama automáticamente.
                        StackPane centeringContainer = new StackPane(markerVisualNode);

                        // Posicionar el CONTENEDOR en la capa superior usando relocate().
                        double xPos = col * CELL_SIZE;
                        double yPos = row * CELL_SIZE;
                        centeringContainer.relocate(xPos, yPos);

                        // Añadir el contenedor a la capa de dibujo.
                        drawingPane.getChildren().add(centeringContainer);
                    } else {
                        // Los otros marcadores (WATER, TOUCHED) van en la capa inferior (cellPane)
                        if (cellPane != null) {
                            cellPane.getChildren().add(markerVisualNode);
                        }
                    }
                }
            }
        }
    }

    /**
     * Muestra un mensaje en el contenedor de mensajes de la UI.
     * Añade el nuevo mensaje en la parte superior y gestiona el historial.
     * @param message El texto del mensaje a mostrar.
     * @param isError Si el mensaje es un error (se mostrará en rojo).
     */
    public void displayMessage(String message, boolean isError) {
        Platform.runLater(() -> {
            if (controller.messageContainer == null) {
                return;
            }

            // Crear una nueva etiqueta para el mensaje
            Label newLabel = new Label(message);
            newLabel.setFont(new Font("Arial", 16.0));
            newLabel.setWrapText(true);

            // Añadir el nuevo mensaje al principio del VBox
            controller.messageContainer.getChildren().add(0, newLabel);

            // Limitar el número de mensajes visibles
            if (controller.messageContainer.getChildren().size() > MAX_MESSAGES) {
                controller.messageContainer.getChildren().remove(MAX_MESSAGES);
            }

            // Aplicar estilos a todos los mensajes en el contenedor
            for (int i = 0; i < controller.messageContainer.getChildren().size(); i++) {
                Node node = controller.messageContainer.getChildren().get(i);
                if (node instanceof Label label) {
                    // El mensaje más reciente (i=0) tiene opacidad completa
                    if (i == 0) {
                        label.setOpacity(1.0);
                        if (isError) {
                            label.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                        } else {
                            label.setStyle("-fx-text-fill: black; -fx-font-weight: normal;");
                        }
                    } else {
                        // Los mensajes más antiguos se desvanecen
                        label.setOpacity(0.6); // Opacidad para el mensaje anterior
                        label.setStyle("-fx-text-fill: dimgray; -fx-font-weight: normal;");
                    }
                }
            }
        });
    }


    /**
     * Muestra u oculta el panel de control de orientación.
     * @param show True para mostrar, false para ocultar.
     */
    public void showOrientationControls(boolean show) {
        this.controller.orientationControlPane.setVisible(show);
    }

    /**
     * Transiciona la UI a la fase de disparos.
     * Oculta los controles de colocación y habilita la interacción con el tablero enemigo.
     */
    public void showFiringPhase() {
        // Ocultar panel de colocación y botones de orientación
        this.controller.shipPlacementPane.setVisible(false);
        this.controller.orientationControlPane.setVisible(false);

        // Ocultar el botón de finalizar y el de colocar barcos aleatoriamente
        this.controller.finalizePlacementButton.setVisible(false);
        this.controller.placeRandomlyButton.setVisible(false);

        // Habilitar el botón para ver el tablero del oponente
        this.controller.toggleOpponentBoardButton.setDisable(false);

        // Deshabilitar clics en el tablero propio y habilitarlos en el del enemigo
        this.controller.humanPlayerBoardGrid.setDisable(true);
        this.controller.machinePlayerBoardGrid.setDisable(false);

        this.displayMessage("¡Comienza la batalla! Haz clic en el tablero enemigo para disparar.", false);

        // Dibujar el tablero enemigo vacío inicialmente (la vista normal)
        this.drawBoard(this.controller.machinePlayerBoardGrid, this.controller.getGameState().getMachinePlayerTerritoryBoard(), false);
    }

    /**
     * Actualiza el estilo de los botones de orientación para resaltar el que está activo.
     * @param activeOrientation La orientación actualmente seleccionada.
     */
    public void updateOrientationButtons(Orientation activeOrientation) {
        // Estilo base para los botones
        String baseStyle = "-fx-background-color: lightgray; -fx-border-color: gray;";
        // Estilo para el botón activo
        String activeStyle = "-fx-background-color: lightblue; -fx-border-color: darkblue; -fx-font-weight: bold;";

        if (activeOrientation == Orientation.HORIZONTAL) {
            this.controller.horizontalButton.setStyle(activeStyle);
            this.controller.verticalButton.setStyle(baseStyle);
        } else {
            this.controller.horizontalButton.setStyle(baseStyle);
            this.controller.verticalButton.setStyle(activeStyle);
        }
    }


    /**
     * Habilita o deshabilita la interacción con un tablero específico.
     * @param gridPane El tablero (GridPane) a modificar.
     * @param enabled  true para habilitar la interacción, false para deshabilitarla.
     */
    public void setBoardInteraction(GridPane gridPane, boolean enabled) {
        gridPane.setDisable(!enabled);
    }


    /**
     * Actualiza el texto del botón para ver/ocultar el tablero del oponente.
     * @param text El nuevo texto para el botón.
     */
    public void updateToggleButtonText(String text) {
        this.controller.toggleOpponentBoardButton.setText(text);
    }

    /**
     * Actualiza la UI después de cargar un juego guardado.
     * Redibuja los tableros y actualiza el estado de la interfaz.
     */
    public void refreshUI() {
        if (this.controller.getGameState() == null) {
            return;
        }

        // Redibujar el tablero del jugador humano
        this.drawBoard(
            this.controller.humanPlayerBoardGrid,
            this.controller.getGameState().getHumanPlayerPositionBoard(),
            true
        );

        // Redibujar el tablero del territorio enemigo
        this.drawBoard(
            this.controller.machinePlayerBoardGrid,
            this.controller.getGameState().getMachinePlayerTerritoryBoard(),
            false
        );

        // Sincronizar la UI según la fase del juego
        switch (this.controller.getGameState().getCurrentPhase()) {
            case PLACEMENT:
                // Mostrar solo los barcos que faltan por colocar
                this.showShipPlacementPhase(
                    this.controller.getGameState().getHumanPlayerPositionBoard(),
                    this.controller.getGameState().getPendingShipsToPlace()
                );
                this.controller.finalizePlacementButton.setDisable(
                    !this.controller.getGameState().getPendingShipsToPlace().isEmpty()
                );
                this.controller.shipPlacementPane.setVisible(true);
                this.controller.finalizePlacementButton.setVisible(true);
                this.controller.placeRandomlyButton.setVisible(true);
                this.controller.machinePlayerBoardGrid.setDisable(true);
                this.controller.humanPlayerBoardGrid.setDisable(false);
                break;
            case FIRING:
                // Saltar la parte de colocación y mostrar la fase de disparos
                this.showFiringPhase();
                break;
            case GAME_OVER:
                // Mostrar mensaje de fin de juego
                this.displayMessage("¡La partida ha terminado!", false);
                this.controller.shipPlacementPane.setVisible(false);
                this.controller.finalizePlacementButton.setVisible(false);
                this.controller.placeRandomlyButton.setVisible(true);
                this.controller.machinePlayerBoardGrid.setDisable(true);
                this.controller.humanPlayerBoardGrid.setDisable(true);
                break;
            default:
                break;
        }
    }

    // ------------ Métodos auxiliares

    /**
     * Crea, escala, rota y posiciona el nodo visual de un barco.
     *
     * @param ship El objeto de barco del modelo.
     * @param col  La columna inicial del barco en la cuadrícula.
     * @param row  La fila inicial del barco en la cuadrícula.
     * @return El nodo JavaFX listo para ser añadido al canvas de dibujo.
     */
    private Node createAndPositionShipVisual(Ship ship, int col, int row) {
        ShipShape factory = this.shipShapeFactory.get(ship.getShipType());
        if (factory == null) {
            return new Group(); // Devuelve un grupo vacío si no hay fábrica.
        }

        Node shipVisualNode = factory.createShape();

        // Calcular escala
        double originalWidth = shipVisualNode.getBoundsInLocal().getWidth();
        double targetWidth = CELL_SIZE * ship.getValueShip();
        double scaleFactor = targetWidth / originalWidth;

        // Calcular posición en píxeles
        double xPos = col * CELL_SIZE;
        double yPos = row * CELL_SIZE;

        shipVisualNode.setLayoutX(xPos);
        shipVisualNode.setLayoutY(yPos);

        // Aplicar transformaciones
        if (ship.getOrientation() == Orientation.VERTICAL) {
            // Para rotación vertical, el pivote debe estar en el centro de la primera celda.
            double pivotX = CELL_SIZE / 2.0;
            double pivotY = CELL_SIZE / 2.0;
            Rotate rotation = new Rotate(90, pivotX, pivotY);
            Scale scale = new Scale(scaleFactor, scaleFactor, pivotX, pivotY);
            shipVisualNode.getTransforms().addAll(scale, rotation);
        } else {
            // Para horizontal, el pivote puede ser (0,0)
            Scale scale = new Scale(scaleFactor, scaleFactor, 0, 0);
            shipVisualNode.getTransforms().add(scale);
        }

        return shipVisualNode;
    }

    /**
     * Obtiene el Pane de una celda específica en un GridPane de forma segura.
     * Maneja el caso en que los índices de fila/columna son nulos (interpretándolos como 0).
     * @param gridPane El GridPane del cual obtener la celda.
     * @param row La fila deseada.
     * @param col La columna deseada.
     * @return El nodo Pane en esa posición, o null si no se encuentra.
     */
    private Pane getCellPane(GridPane gridPane, int row, int col) {
        for (Node node : gridPane.getChildren()) {
            // Obtener los índices. Pueden ser null.
            Integer rowIndex = GridPane.getRowIndex(node);
            Integer colIndex = GridPane.getColumnIndex(node);

            // Convertir los índices nulos a 0.
            int r = (rowIndex == null) ? 0 : rowIndex;
            int c = (colIndex == null) ? 0 : colIndex;

            // Comparar los índices calculados.
            if (r == row && c == col) {
                // Asegurarse de que el nodo es un Pane antes de hacer el cast.
                if (node instanceof Pane) {
                    return (Pane) node;
                }
            }
        }
        return null; // No se encontró la celda.
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