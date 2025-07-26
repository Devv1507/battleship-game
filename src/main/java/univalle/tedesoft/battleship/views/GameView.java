package univalle.tedesoft.battleship.views;


import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
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
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
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
import univalle.tedesoft.battleship.models.Enums.GamePhase;
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
    private static final int CELL_SIZE = 40;
    /** Mapa para las figuras de los barcos */
    private final Map<ShipType, ShipShape> shipShapeFactory;
    /** Mapa para las figuras de los marcadores de disparo */
    private final Map<CellState, IMarkerShape> markerShapeFactory;
    private static final int MAX_MESSAGES = 2; // Mostrar los últimos 2 mensajes
    // Panel para dibujar la previsualización
    private final Pane dragPreviewPane;
    // Mapa para asociar un barco del modelo con su figura en la vista.
    private final Map<Ship, Node> shipVisuals = new HashMap<>();

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

    /**
     * Constructor privado Singleton.
     * Carga el FXML, obtiene la referencia al controlador y se la pasa a sí mismo (el Stage).
     * @throws IOException si el archivo FXML no se puede cargar.
     */
    private GameView() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(
                Main.class.getResource("game-view.fxml")
        );
        Scene scene = new Scene(fxmlLoader.load());

        this.controller = fxmlLoader.getController();
        if (this.controller == null) {
            throw new IllegalStateException("El controlador no se pudo cargar desde el FXML. Revisa el campo fx:controller.");
        }
        // Inicializar GameState
        IGameState gameState = new GameState();
        this.controller.setGameView(this);
        this.controller.setGameState(gameState);
        // Inicializar el controlador con la vista
        this.controller.initializeUI(this);
        // Configurar la escena y el título de la ventana
        this.setTitle("Battleship Game");
        this.setScene(scene);
        // Crear el panel de previsualización (se agregará más tarde)
        this.dragPreviewPane = new Pane();
        this.dragPreviewPane.setMouseTransparent(true); // Para que no intercepte clics

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

        // Inicializar los efectos de los botones
        this.initializeButtonEffects();
    }


    /**
     * Inicializa la apariencia visual de la UI. Se llama desde el controlador
     * una vez que la vista ha sido enlazada.
     * @param controller La instancia del controlador para asignar listeners.
     */
    public void initializeUI(GameController controller) {
        // Inicializar los tableros de juego.
        this.initializeBoardGrid(controller.humanPlayerBoardGrid, true);
        this.initializeBoardGrid(controller.machinePlayerBoardGrid, false);

        // Definir el tamaño del tablero en función del tamaño de las celdas.
        double boardSize = 10 * CELL_SIZE;

        // Limitar el tamaño del canvas de dibujo para que no sea más grande que el tablero.
        controller.humanPlayerDrawingPane.setMaxSize(boardSize, boardSize);
        controller.machinePlayerDrawingPane.setMaxSize(boardSize, boardSize);
    }


    // ------------ Lógica principal

    private void initializeBoardGrid(GridPane boardGrid, boolean isHumanBoard) {
        boardGrid.getChildren().clear();

        if (isHumanBoard) {
            // Asignar los manejadores de ARRASTRE y SOLTAR al GridPane completo.
            // Esto asegura que los eventos se capturen en cualquier lugar del tablero.

            // Manejador para el inicio del arrastre de un barco.
            boardGrid.setOnMouseDragged(event -> {
                // Calcular la fila y columna basándose en la posición del ratón dentro del GridPane.
                int col = (int) (event.getX() / CELL_SIZE);
                int row = (int) (event.getY() / CELL_SIZE);

                // Asegurarse de que las coordenadas calculadas estén dentro de los límites.
                if (row >= 0 && row < 10 && col >= 0 && col < 10) {
                    this.controller.handleShipDrag(row, col);
                }
                event.consume();
            });
            // Manejador para el final del arrastre de un barco.
            boardGrid.setOnMouseReleased(event -> {
                int col = (int) (event.getX() / CELL_SIZE);
                int row = (int) (event.getY() / CELL_SIZE);

                if (row >= 0 && row < 10 && col >= 0 && col < 10) {
                    this.controller.handleShipDragEnd(row, col);
                }
                event.consume();
            });
        }


        // Se añaden las celdas al GridPane
        for (int row = 0; row < 10; row++) {
            for (int col = 0; col < 10; col++) {
                Pane cellPane = new Pane();
                // Cada celda tiene el fondo azul
                cellPane.setStyle("-fx-background-color: rgba(74, 144, 226, 0.3);");
                cellPane.setPrefSize(CELL_SIZE, CELL_SIZE);

                // Las etiquetas de coordenadas se manejan dinámicamente en drawBoard()
                final int finalRow = row;
                final int finalCol = col;

                if (isHumanBoard) {
                    // Los eventos de INICIO (clic y doble clic) permanecen en la celda individual.

                    // Manejador para COLOCAR un nuevo barco (un solo clic).
                    cellPane.setOnMouseClicked(event -> {
                        if (event.getClickCount() == 1) {
                            this.controller.handlePlacementCellClick(finalRow, finalCol);
                        }
                        event.consume();
                    });
                    // Manejador para INICIAR el ARRASTRE (doble clic y mantener).
                    cellPane.setOnMousePressed(event -> {
                        if (event.getClickCount() == 2) {
                            this.controller.handleShipDragStart(finalRow, finalCol);
                        }
                        event.consume();
                    });
                } else {
                    // El tablero enemigo mantiene el comportamiento de clic simple para disparar.
                    cellPane.setOnMouseClicked(event -> {
                        this.controller.handleFiringCellClick(finalRow, finalCol);
                    });
                }
                boardGrid.add(cellPane, col, row);
            }
        }
    }

    /**
     * Inicia la retroalimentación visual para el arrastre de un barco.
     * Busca la figura del barco en el mapa `shipVisuals` y la oculta.
     * @param ship El barco que se está empezando a arrastrar.
     */
    public void startShipDrag(Ship ship) {
        Node visualNode = this.shipVisuals.get(ship);
        if (visualNode != null) {
            visualNode.setVisible(false);
        }
    }

    /**
     * Dibuja una previsualización ("fantasma") de las celdas del barco en una nueva posición.
     * @param ship El barco que se arrastra.
     * @param previewRow La fila de la esquina superior izquierda de la previsualización.
     * @param previewCol La columna de la esquina superior izquierda de la previsualización.
     */
    public void updateDragPreview(Ship ship, int previewRow, int previewCol) {
        this.clearDragPreview();

        for (int i = 0; i < ship.getValueShip(); i++) {
            int r = previewRow;
            int c = previewCol;

            if (ship.getOrientation() == Orientation.HORIZONTAL) {
                c += i;
            } else {
                r += i;
            }

            Rectangle previewCell = new Rectangle(c * CELL_SIZE, r * CELL_SIZE, CELL_SIZE, CELL_SIZE);
            previewCell.setFill(Color.web("#4a90e2", 0.7)); // Azul destacable y semitransparente
            previewCell.setStroke(Color.WHITE);
            previewCell.setStrokeWidth(2);
            previewCell.setMouseTransparent(true);
            this.dragPreviewPane.getChildren().add(previewCell);
        }
    }

    /**
     * Limpia la previsualización de arrastre del panel.
     */
    public void clearDragPreview() {
        if (this.dragPreviewPane != null) {
            this.dragPreviewPane.getChildren().clear();
        }
    }

    public void showShipPlacementPhase(Board playerPositionBoard, List<ShipType> shipsToPlace) {
        this.controller.shipPlacementPane.setVisible(true);
        // El botón solo se habilita si no quedan barcos por colocar.
        this.controller.finalizePlacementButton.setDisable(!shipsToPlace.isEmpty());
        this.controller.machinePlayerBoardGrid.setDisable(true);
        this.controller.humanPlayerBoardGrid.setDisable(false);
        
        // IMPORTANTE: Dibujar los barcos que ya están colocados en el tablero
        // Esto es crucial para las partidas cargadas donde ya hay barcos colocados
        this.drawBoard(this.controller.humanPlayerBoardGrid, playerPositionBoard, true);
        
        // Limpiar el contenido anterior del panel (excepto el control de orientación)
        this.controller.shipPlacementPane.getChildren().remove(1, this.controller.shipPlacementPane.getChildren().size());

        // Obtener el recuento de barcos pendientes del controlador
        Map<ShipType, Long> pendingCounts = this.controller.getPendingShipCounts();
        //  Definir los anchos deseados para cada tipo de barco en el panel de selección.
        final Map<ShipType, Double> targetWidths = Map.of(
                ShipType.FRIGATE, 50.0,
                ShipType.DESTROYER, 100.0,
                ShipType.SUBMARINE, 125.0,
                ShipType.AIR_CRAFT_CARRIER, 200.0
        );
        // Definir el orden en que queremos mostrar los barcos
        List<ShipType> displayOrder = List.of(
                ShipType.AIR_CRAFT_CARRIER,
                ShipType.SUBMARINE,
                ShipType.DESTROYER,
                ShipType.FRIGATE
        );

        for (ShipType type : displayOrder) {
            long count = pendingCounts.getOrDefault(type, 0L);

            // Crear un contenedor para la forma, para centrarla y manejarla fácilmente.
            VBox shipContainer = new VBox(5);
            shipContainer.setAlignment(Pos.CENTER);
            shipContainer.getStyleClass().add("ship-selector-item");

            // Etiqueta con el nombre y contador
            String shipName = type.toString().replace("_", " ");
            Label countLabel = new Label(String.format("%s: %dx", shipName, count));
            countLabel.setFont(new Font("Arial Bold", 14));
            countLabel.setTextFill(javafx.scene.paint.Color.WHITE);

            // Forma Visual del Barco
            ShipShape shapeFactory = this.shipShapeFactory.get(type);
            Node shipVisualNode = new Group();
            if (shapeFactory != null) {
                // Crear la forma del barco.
                shipVisualNode = shapeFactory.createShape();
                // Obtener el ancho objetivo dinámicamente del mapa targetWidths.
                double targetWidth = targetWidths.getOrDefault(type, 150.0);
                double originalWidth = shipVisualNode.getBoundsInLocal().getWidth();
                if (originalWidth > 0) {
                    double scaleFactor = targetWidth / originalWidth;
                    Scale scale = new Scale(scaleFactor, scaleFactor);
                    shipVisualNode.getTransforms().add(scale);
                }
            }

            shipContainer.getChildren().addAll(countLabel, shipVisualNode);

            if (count == 0) {
                // Si ya no hay más barcos de este tipo por colocar,
                // deshabilitar la interacción y hacerlo más tenue.
                shipContainer.setOpacity(0.4);
                shipContainer.setDisable(true);
            } else {
                // Si aún hay barcos por colocar, habilitar la interacción.
                shipContainer.setOpacity(1.0);
                shipContainer.setDisable(false);
                shipContainer.setOnMouseClicked(event -> this.controller.handleShipSelection(type));
                shipContainer.setStyle("-fx-cursor: hand;");
            }
            // Añadir el contenedor (con el barco escalado dentro) al panel de colocación.
            this.controller.shipPlacementPane.getChildren().add(shipContainer);
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

        // Limpiar el canvas de dibujo de barcos viejos.
        drawingPane.getChildren().clear();
        if (gridPane == this.controller.humanPlayerBoardGrid) {
            this.shipVisuals.clear(); // Limpiar solo para el tablero del jugador.
        }

        // Limpiar los marcadores de las celdas del GridPane.
        for (Node node : gridPane.getChildren()) {
            if (node instanceof Pane) {
                // Se restaura el color base de la celda en cada redibujado.
                Pane cellPane = (Pane) node;
                cellPane.getChildren().clear();
                cellPane.setStyle("-fx-background-color: rgba(74, 144, 226, 0.3);");
            }
        }

        // Dibujo de barcos
        Set<Ship> drawnShips = new HashSet<>();
        // Iteramos directamente sobre la lista de barcos
        for (Ship ship : board.getShips()) {
            if (drawnShips.contains(ship)) continue;

            // La condición para dibujar el barco:
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

                if (isHumanBoard) {
                    this.shipVisuals.put(ship, shipVisualNode);
                    if (this.controller.getGameState().getCurrentPhase() == GamePhase.PLACEMENT) {
                        // Resaltar las celdas del barco solo si estamos en la fase de colocación.
                        this.highlightShipCells(gridPane, ship);
                    }
                }

                drawingPane.getChildren().add(shipVisualNode);
                drawnShips.add(ship);
            }
        }

        // Dibujo de marcadores de estado de celdas y manejo de etiquetas de coordenadas
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
                                coordinateLabel.setStyle("-fx-text-fill: rgba(255, 255, 255, 0.7);");
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
                
                // Dibujo de marcadores para celdas atacadas
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
                    if (i == 0) {
                        label.setOpacity(1.0);
                        if (isError) {
                            // Rojo claro para errores, visible sobre fondo oscuro
                            label.setStyle("-fx-text-fill: #ff8a80; -fx-font-weight: bold;");
                        } else {
                            // Blanco para mensajes normales
                            label.setStyle("-fx-text-fill: white; -fx-font-weight: normal;");
                        }
                    } else {
                        // Gris claro para mensajes antiguos
                        label.setOpacity(0.7);
                        label.setStyle("-fx-text-fill: #cccccc; -fx-font-weight: normal;");
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

        // Mostrar los botones de la fase de batalla
        this.controller.restartGameButton.setVisible(true);
        this.controller.toggleOpponentBoardButton.setVisible(true);

        // Deshabilitar clics en el tablero propio y habilitarlos en el del enemigo
        this.controller.humanPlayerBoardGrid.setDisable(true);
        this.controller.machinePlayerBoardGrid.setDisable(false);

        this.displayMessage("¡Comienza la batalla! Haz clic en el tablero enemigo para disparar.", false);

        // Redibujar el tablero del jugador para eliminar los resaltados de la fase de colocación.
        this.drawBoard(this.controller.humanPlayerBoardGrid, this.controller.getGameState().getHumanPlayerPositionBoard(), true);
        // Dibujar el tablero enemigo vacío inicialmente (la vista normal)
        this.drawBoard(this.controller.machinePlayerBoardGrid, this.controller.getGameState().getMachinePlayerTerritoryBoard(), false);
    }

    /**
     * Actualiza el estilo de los botones de orientación para resaltar el que está activo.
     * @param activeOrientation La orientación actualmente seleccionada.
     */
    public void updateOrientationButtons(Orientation activeOrientation) {
        // Estilo base para los botones
        String baseStyle = "-fx-background-color: #6c757d; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8; fx-border-width: 2;";
        // Estilo para el botón activo
        String activeStyle = "-fx-background-color: lightblue; -fx-text-fill: black; -fx-font-weight: bold; -fx-background-radius: 8; -fx-border-width: 2;";

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
        // Limpiar la previsualización de arrastre si está activa
        this.clearDragPreview();
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


    /**
     * Restaura la interfaz de usuario al estado inicial de la fase de colocación.
     * Es invocado por el controlador cuando se reinicia el juego.
     */
    public void resetToPlacementPhase() {
        // Limpiar ambos tableros visualmente
        this.drawBoard(this.controller.humanPlayerBoardGrid, this.controller.getGameState().getHumanPlayerPositionBoard(), true);
        this.drawBoard(this.controller.machinePlayerBoardGrid, this.controller.getGameState().getMachinePlayerTerritoryBoard(), false);

        // Restaurar la visibilidad de los componentes de la fase de colocación
        this.controller.shipPlacementPane.setVisible(true);
        this.controller.placeRandomlyButton.setVisible(true);
        this.controller.finalizePlacementButton.setVisible(true);
        this.controller.finalizePlacementButton.setDisable(true); // Deshabilitado hasta que se coloquen los barcos

        // Ocultar componentes de la fase de batalla
        this.controller.toggleOpponentBoardButton.setVisible(false);
        this.controller.restartGameButton.setVisible(false);

        // Habilitar y deshabilitar los tableros correspondientes
        this.controller.humanPlayerBoardGrid.setDisable(false);
        this.controller.machinePlayerBoardGrid.setDisable(true);

        // Actualizar la lista de barcos para colocar
        this.showShipPlacementPhase(
                this.controller.getGameState().getHumanPlayerPositionBoard(),
                this.controller.getGameState().getPendingShipsToPlace()
        );
    }

    // ------------ Métodos auxiliares

    /**
     * Resalta las celdas del GridPane que están ocupadas por un barco específico.
     *
     * @param gridPane El GridPane en el que se aplicará el resaltado.
     * @param ship     El barco cuyas celdas se deben resaltar.
     */
    private void highlightShipCells(GridPane gridPane, Ship ship) {
        // Estilo para la celda resaltada. Un azul ligeramente más claro y brillante.
        String highlightStyle = "-fx-background-color: rgba(137, 197, 255, 0.6);";

        // Iterar sobre cada coordenada que el barco ocupa.
        for (Coordinate coord : ship.getOccupiedCoordinates()) {
            // Encontrar el Pane correspondiente a esta coordenada.
            Pane cellPane = getCellPane(gridPane, coord.getY(), coord.getX());
            if (cellPane != null) {
                // Aplicar el estilo de resaltado.
                cellPane.setStyle(highlightStyle);
            }
        }
    }

    /**
     * Crea, escala, rota y posiciona el nodo visual de un barco, asegurando
     * que quede visualmente centrado dentro de su área de celdas.
     *
     * @param ship El objeto de barco del modelo.
     * @param col  La columna inicial del barco en la cuadrícula.
     * @param row  La fila inicial del barco en la cuadrícula.
     * @return El nodo JavaFX listo para ser añadido al canvas de dibujo.
     */
    private Node createAndPositionShipVisual(Ship ship, int col, int row) {
        ShipShape factory = this.shipShapeFactory.get(ship.getShipType());
        if (factory == null) {
            return new Group();
        }

        Node shipVisualNode = factory.createShape();

        // Aplicar escala primero para obtener las dimensiones finales
        double targetWidth = CELL_SIZE * ship.getValueShip();
        double originalWidth = shipVisualNode.getBoundsInLocal().getWidth();
        double scaleFactor = targetWidth / originalWidth;

        Scale scale = new Scale(scaleFactor, scaleFactor);
        shipVisualNode.getTransforms().add(scale);

        // Lógica de centrado
        double finalOffsetX = 0;
        double finalOffsetY = 0;

        if (ship.getOrientation() == Orientation.VERTICAL) {
            // Para orientación vertical
            double pivotX = CELL_SIZE / 2.0;
            double pivotY = CELL_SIZE / 2.0;
            Rotate rotation = new Rotate(90, pivotX, pivotY);
            shipVisualNode.getTransforms().add(rotation);

            // Obtener las dimensiones DESPUÉS de escalar y rotar
            double finalBoundsWidth = shipVisualNode.getBoundsInParent().getWidth();
            // Calcular el offset para centrarlo horizontalmente en la columna
            finalOffsetX = (CELL_SIZE - finalBoundsWidth) / 2.0;

        } else {
            // Para orientación horizontal
            // Obtener las dimensiones DESPUÉS de escalar
            double finalBoundsHeight = shipVisualNode.getBoundsInParent().getHeight();
            // Calcular el offset para centrarlo verticalmente en la fila
            finalOffsetY = (CELL_SIZE - finalBoundsHeight) / 2.0;
        }

        // Posicionar la esquina superior izquierda de la celda y luego aplicar el offset de centrado
        double xPos = col * CELL_SIZE;
        double yPos = row * CELL_SIZE;
        shipVisualNode.relocate(xPos + finalOffsetX, yPos + finalOffsetY);

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
     * Inicializa un nuevo juego con el jugador especificado.
     * Este método debe ser llamado desde WelcomeController después de obtener la instancia.
     * @param player El jugador humano para el nuevo juego
     */
    public void initializeNewGame(HumanPlayer player) {
        try {
            IGameState gameState = this.controller.getGameState();
            
            // Configurar el panel de previsualización si no está ya agregado
            setupDragPreviewPane();
            
            // Limpiar cualquier estado previo si es posible
            gameState.getHumanPlayerPositionBoard().resetBoard();
            
            // Iniciar nuevo juego
            gameState.startNewGame(player);
            
            // Mostrar la fase de colocación de barcos
            this.showShipPlacementPhase(
                    gameState.getHumanPlayerPositionBoard(),
                    gameState.getPendingShipsToPlace()
            );
            
        } catch (Exception e) {
            System.err.println("Error al inicializar nuevo juego: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error al inicializar nuevo juego", e);
        }
    }

    /**
     * Inicializa el juego con una partida cargada.
     * Este método debe ser llamado después de cargar una partida desde WelcomeController.
     */
    public void initializeLoadedGame() {
        try {
            IGameState gameState = this.controller.getGameState();
            
            // Configurar el panel de previsualización si no está ya agregado
            setupDragPreviewPane();
            
            // Determinar la fase del juego y mostrar la UI apropiada
            GamePhase currentPhase = gameState.getCurrentPhase();
            
            // PASO 1: Refrescar completamente la visualización
            refreshLoadedGameDisplay(gameState, currentPhase);
            
            // PASO 2: Configurar la UI específica para la fase
            switch (currentPhase) {
                case PLACEMENT:
                    // Configurar la UI de colocación (esto YA dibuja los barcos gracias al fix anterior)
                    this.showShipPlacementPhase(
                            gameState.getHumanPlayerPositionBoard(),
                            gameState.getPendingShipsToPlace()
                    );
                    break;
                    
                case FIRING:
                case GAME_OVER:
                    // Para estas fases, usar refreshUI que maneja todo
                    this.refreshUI();
                    break;
                    
                default:
                    // Para cualquier otra fase, tratar como colocación
                    this.showShipPlacementPhase(
                            gameState.getHumanPlayerPositionBoard(),
                            gameState.getPendingShipsToPlace()
                    );
                    break;
            }
            
        } catch (Exception e) {
            System.err.println("Error al inicializar juego cargado: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error al cargar el juego", e);
        }
    }

    /**
     * Obtiene el controlador del juego.
     * @return El controlador de la vista del juego
     */
    public GameController getController() {
        return this.controller;
    }

    /**
     * Resetea la instancia Singleton de GameView.
     * Útil para limpiar estado entre diferentes juegos.
     */
    public static void resetInstance() {
        if (GameViewHolder.INSTANCE != null) {
            GameViewHolder.INSTANCE.close();
            GameViewHolder.INSTANCE = null;
        }
    }

    /**
     * Configura el panel de previsualización de forma segura.
     * Solo lo agrega si no está ya presente y los componentes están listos.
     */
    private void setupDragPreviewPane() {
        try {
            if (this.controller != null && 
                this.controller.humanPlayerBoardContainer != null && 
                this.dragPreviewPane != null) {
                
                // Verificar si ya está agregado para evitar duplicados
                if (!this.controller.humanPlayerBoardContainer.getChildren().contains(this.dragPreviewPane)) {
                    this.controller.humanPlayerBoardContainer.getChildren().add(this.dragPreviewPane);
                }
            }
        } catch (Exception e) {
            System.err.println("ERROR al configurar panel de previsualización: " + e.getMessage());
            // No lanzar excepción aquí, es un componente opcional
        }
    }

    /**
     * Refresca completamente la visualización de una partida cargada.
     * Asegura que tanto el modelo como la vista estén sincronizados.
     */
    private void refreshLoadedGameDisplay(IGameState gameState, GamePhase currentPhase) {
        try {
            // Limpiar cualquier estado visual previo
            if (this.controller.humanPlayerDrawingPane != null) {
                this.controller.humanPlayerDrawingPane.getChildren().clear();
            }
            if (this.controller.machinePlayerDrawingPane != null) {
                this.controller.machinePlayerDrawingPane.getChildren().clear();
            }
            this.shipVisuals.clear();
            
            // Forzar redibujado completo de ambos tableros
            this.drawBoard(this.controller.humanPlayerBoardGrid, gameState.getHumanPlayerPositionBoard(), true);
            this.drawBoard(this.controller.machinePlayerBoardGrid, gameState.getMachinePlayerTerritoryBoard(), false);
            
        } catch (Exception e) {
            System.err.println("ERROR al refrescar visualización: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Orquesta la aplicación de efectos visuales a los botones de esta vista,
     * utilizando la clase de utilidad ViewUtils.
     */
    private void initializeButtonEffects() {
        ViewUtils.applyHoverScaleEffect(this.controller.saveGameButton);
        ViewUtils.applyHoverScaleEffect(this.controller.placeRandomlyButton);
        ViewUtils.applyHoverScaleEffect(this.controller.instructionsButton);
        ViewUtils.applyHoverScaleEffect(this.controller.finalizePlacementButton);
        ViewUtils.applyHoverScaleEffect(this.controller.toggleOpponentBoardButton);
        ViewUtils.applyHoverScaleEffect(this.controller.horizontalButton);
        ViewUtils.applyHoverScaleEffect(this.controller.verticalButton);
        ViewUtils.applyHoverScaleEffect(this.controller.restartGameButton);
    }
}