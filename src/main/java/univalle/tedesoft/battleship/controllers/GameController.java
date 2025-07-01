package univalle.tedesoft.battleship.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import univalle.tedesoft.battleship.Exceptions.InvalidShipPlacementException;
import univalle.tedesoft.battleship.Exceptions.OutOfBoundsException;
import univalle.tedesoft.battleship.Exceptions.OverlapException;
import univalle.tedesoft.battleship.models.Enums.Orientation;
import univalle.tedesoft.battleship.models.Enums.ShipType;
import univalle.tedesoft.battleship.models.State.IGameState;
import univalle.tedesoft.battleship.views.GameView;

public class GameController {

    // --- Componentes FXML ---
    @FXML public Button finalizePlacementButton;
    @FXML public GridPane humanPlayerBoardGrid;
    @FXML public GridPane machinePlayerBoardGrid;
    @FXML public Label messageLabel;
    @FXML public VBox shipPlacementPane;
    @FXML public Button toggleOpponentBoardButton;

    // --- Referencias principales ---
    private IGameState gameState;
    private GameView gameView;

    // --- Estado interno del controlador ---
    private ShipType selectedShipToPlace;

    /**
     * Inicialización de JavaFX.
     * Se llama automáticamente después de que los campos @FXML han sido inyectados.
     */
    @FXML
    public void initialize() {}

    /**
     * Inicializa la UI a través de la GameView. Se llama desde la vista
     * una vez que todo está conectado.
     * @param gameView La instancia de la vista que hará el trabajo.
     */
    public void initializeUI(GameView gameView) {
        // El controlador le pide a la vista que configure los listeners de los tableros.
        gameView.initializeUI(this);
    }

    // --------- Setters y getters

    /**
     * Establece la referencia a la GameView y arranca la configuración inicial de la UI.
     * Es el puente que une al controlador con su vista.
     * @param gameView La instancia de GameView que maneja la ventana.
     */
    public void setGameView(GameView gameView) {
        this.gameView = gameView;

        // Ahora que la vista está lista, podemos pedirle que configure su estado inicial.
        if (this.gameView != null) {
            this.gameView.initializeUI(this);
        }
    }


    /**
     * Establece la referencia al modelo del juego.
     * @param gameState La instancia del estado del juego.
     */
    public void setGameState(IGameState gameState) {
        this.gameState = gameState;
    }


    // --------- Event handlers con FXML

    /**
     * Se activa cuando el jugador hace clic en el botón "Finalizar Colocación".
     * Notifica al modelo y actualiza la vista para pasar a la fase de disparos.
     * @param event El evento de la acción.
     */
    @FXML
    void onFinalizePlacementClick(ActionEvent event) {
        if (this.gameState != null && this.gameView != null) {
            // Verificar si aún quedan barcos por colocar
            if (!this.gameState.getPendingShipsToPlace().isEmpty()) {
                this.gameView.displayMessage("Aún debes colocar todos tus barcos.", true);
                return;
            }
            this.gameState.finalizeShipPlacement();
            this.gameView.showFiringPhase(
                    this.gameState.getHumanPlayerPositionBoard(),
                    this.gameState.getMachinePlayerTerritoryBoard()
            );
        }
    }

    @FXML
    void onToggleOpponentBoardClick(ActionEvent event) {
        // TODO:  Lógica para el botón de
    }

    // ------------ Métodos auxiliares
    /**
     * Maneja el clic en una celda del tablero de posición del jugador humano.
     * Se usa durante la fase de colocación de barcos.
     * @param row La fila de la celda clickeada.
     * @param col La columna de la celda clickeada.
     */
    public void handlePlacementCellClick(int row, int col) {
        if (this.selectedShipToPlace == null) {
            this.gameView.displayMessage("Por favor, selecciona un barco para colocar.", true);
            return;
        }

        try {
            // TODO: Crear un mecanismo en la UI (ej. clic derecho, un botón 'Rotar')
            // para que el jugador elija la orientación. Por ahora, usamos HORIZONTAL por defecto.
            Orientation chosenOrientation = Orientation.HORIZONTAL;

            // La llamada ahora coincide con la firma de la interfaz IGameState
            this.gameState.placeHumanPlayerShip(this.selectedShipToPlace, row, col, chosenOrientation);

            // Actualizar la vista del tablero
            this.gameView.drawBoard(this.humanPlayerBoardGrid, this.gameState.getHumanPlayerPositionBoard(), true);
            this.gameView.displayMessage("Barco " + this.selectedShipToPlace + " colocado.", false);

            this.selectedShipToPlace = null; // Deseleccionar para la siguiente colocación

            // Actualizar la lista de barcos a colocar en el panel izquierdo
            this.gameView.showShipPlacementPhase(
                    this.gameState.getHumanPlayerPositionBoard(),
                    this.gameState.getPendingShipsToPlace()
            );

        } catch (InvalidShipPlacementException | OverlapException | OutOfBoundsException e) {
            // Capturamos las excepciones específicas y mostramos un mensaje de error claro.
            this.gameView.displayMessage("Error: " + e.getMessage(), true);
        } catch (Exception e) {
            // Captura de cualquier otro error inesperado.
            this.gameView.displayMessage("Ocurrió un error inesperado.", true);
            e.printStackTrace();
        }
    }

    /**
     * Maneja el clic en una celda del tablero principal (territorio enemigo).
     * Se usa durante la fase de disparos.
     * @param row La fila de la celda clickeada.
     * @param col La columna de la celda clickeada.
     */
    public void handleFiringCellClick(int row, int col) {
        // Lógica de disparo
    }

    /**
     * Registra el tipo de barco que el jugador ha seleccionado del panel de colocación.
     * @param shipType El tipo de barco seleccionado.
     */
    public void handleShipSelection(ShipType shipType) {
        this.selectedShipToPlace = shipType;
        this.gameView.displayMessage("Seleccionado: " + shipType + ". Haz clic en tu tablero para colocarlo.", false);
    }
}
