package univalle.tedesoft.battleship.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import univalle.tedesoft.battleship.exceptions.OutOfBoundsException;
import univalle.tedesoft.battleship.exceptions.OverlapException;
import univalle.tedesoft.battleship.models.Enums.Orientation;
import univalle.tedesoft.battleship.models.Enums.ShipType;
import univalle.tedesoft.battleship.models.Players.HumanPlayer;
import univalle.tedesoft.battleship.models.Players.Player;
import univalle.tedesoft.battleship.models.ShotOutcome;
import univalle.tedesoft.battleship.models.State.IGameState;
import univalle.tedesoft.battleship.threads.MachineTurnRunnable;
import univalle.tedesoft.battleship.views.GameView;

public class GameController {

    // --- Componentes FXML ---
    @FXML public Button finalizePlacementButton;
    @FXML public GridPane humanPlayerBoardGrid;
    @FXML public GridPane machinePlayerBoardGrid;
    @FXML public VBox messageContainer;
    @FXML public VBox shipPlacementPane;
    @FXML public Button toggleOpponentBoardButton;
    @FXML public VBox orientationControlPane; // Contenedor de los botones
    @FXML public Button horizontalButton;
    @FXML public Button verticalButton;
    @FXML public Button saveGameButton;
    @FXML public Button loadGameButton;

    // --- Referencias principales ---
    private IGameState gameState;
    private GameView gameView;

    private Thread machineTurnThread;

    // --- Estado interno del controlador ---
    private ShipType selectedShipToPlace;
    private Orientation chosenOrientation = Orientation.HORIZONTAL;
    private boolean isOpponentBoardVisible = false;
    private static final long MACHINE_TURN_THINK_DELAY_MS = 1500;

    /**
     * Inicialización de JavaFX.
     * Se llama automáticamente después de que los campos @FXML han sido inyectados.
     */
    @FXML
    public void initialize() {
    }

    /**
     * Inicializa la UI a través de la GameView. Se llama desde la vista
     * una vez que todo está conectado.
     *
     * @param gameView La instancia de la vista que hará el trabajo.
     */
    public void initializeUI(GameView gameView) {
        // Limpiar el contenedor de mensajes al inicio
        if (this.messageContainer != null) {
            this.messageContainer.getChildren().clear();
        }
        // El controlador le pide a la vista que configure los listeners de los tableros.
        gameView.initializeUI(this);
    }

    // --------- Setters y getters

    /**
     * Establece la referencia a la GameView y arranca la configuración inicial de la UI.
     * Es el puente que une al controlador con su vista.
     *
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
     *
     * @param gameState La instancia del estado del juego.
     */
    public void setGameState(IGameState gameState) {
        this.gameState = gameState;
    }


    /**
     * Devuelve la instancia actual del estado del juego.
     * Permite a la vista acceder al modelo cuando sea necesario.
     *
     * @return la instancia de IGameState.
     */
    public IGameState getGameState() {
        return this.gameState;
    }


    // --------- Event handlers con FXML

    /**
     * Se activa cuando el jugador hace clic en el botón "Finalizar Colocación".
     * Notifica al modelo y actualiza la vista para pasar a la fase de disparos.
     *
     * @param event El evento de la acción.
     */
    @FXML
    void onFinalizePlacementClick(ActionEvent event) {
        if (this.gameState != null && this.gameView != null) {
            if (!this.gameState.getPendingShipsToPlace().isEmpty()) {
                this.gameView.displayMessage("Aún debes colocar todos tus barcos.", true);
                return;
            }
            // Notificar al modelo para que coloque los barcos de la máquina
            this.gameState.finalizeShipPlacement();

            // Actualizar la vista para reflejar el cambio a la fase de disparos
            this.gameView.showFiringPhase();
        }
    }


    /**
     * Se activa cuando el jugador hace clic en el botón "Horizontal".
     *
     * @param event El evento de la acción.
     */
    @FXML
    void onHorizontalClick(ActionEvent event) {
        this.chosenOrientation = Orientation.HORIZONTAL;
        this.gameView.updateOrientationButtons(this.chosenOrientation);
        this.gameView.displayMessage("Orientación seleccionada: Horizontal.", false);
    }

    /**
     * Se activa cuando el jugador hace clic en el botón "Vertical".
     *
     * @param event El evento de la acción.
     */
    @FXML
    void onVerticalClick(ActionEvent event) {
        this.chosenOrientation = Orientation.VERTICAL;
        this.gameView.updateOrientationButtons(this.chosenOrientation);
        this.gameView.displayMessage("Orientación seleccionada: Vertical.", false);
    }

    /**
     * Maneja el clic en el botón para ver/ocultar el tablero del oponente.
     */
    @FXML
    void onToggleOpponentBoardClick(ActionEvent event) {
        if (this.gameState == null || this.gameView == null) return;

        this.isOpponentBoardVisible = !this.isOpponentBoardVisible; // Invertir el estado

        if (this.isOpponentBoardVisible) {
            // Pedir a la vista que muestre el tablero real de la máquina
            this.gameView.drawBoard(
                    this.machinePlayerBoardGrid,
                    this.gameState.getMachinePlayerActualPositionBoard(),
                    true // true para mostrar los barcos
            );
            this.gameView.updateToggleButtonText("Ocultar Tablero Oponente");
        } else {
            // Pedir a la vista que muestre la vista normal del territorio enemigo (sin barcos visibles)
            this.gameView.drawBoard(
                    this.machinePlayerBoardGrid,
                    this.gameState.getMachinePlayerTerritoryBoard(),
                    false // false para ocultar los barcos
            );
            this.gameView.updateToggleButtonText("Ver Tablero Oponente (Profesor)");
        }
    }

    /**
     * Maneja el clic en el botón para guardar el juego.
     */
    @FXML
    void onSaveGameClick(ActionEvent event) {
        if (this.gameState == null) {
            this.gameView.displayMessage("Error: No hay juego activo para guardar.", true);
            return;
        }

        try {
            this.gameState.saveGame();
            this.gameView.displayMessage("¡Juego guardado exitosamente!", false);
        } catch (Exception e) {
            this.gameView.displayMessage("Error al guardar el juego: " + e.getMessage(), true);
        }
    }

    /**
     * Maneja el clic en el botón para cargar el juego.
     */
    @FXML
    void onLoadGameClick(ActionEvent event) {
        if (this.gameState == null) {
            this.gameView.displayMessage("Error: No hay juego activo para cargar.", true);
            return;
        }

        try {
            boolean loaded = this.gameState.loadGame();
            if (loaded) {
                this.gameView.displayMessage("¡Juego cargado exitosamente!", false);
                // Actualizar la vista con el estado cargado
                this.gameView.refreshUI();
            } else {
                this.gameView.displayMessage("No hay un juego guardado disponible.", true);
            }
        } catch (Exception e) {
            this.gameView.displayMessage("Error al cargar el juego: " + e.getMessage(), true);
        }
    }

    // ------------ Métodos con lógica central del juego


    private void scheduleMachineTurn() {
        if (this.gameState.isGameOver()) return;

        this.gameView.displayMessage("Turno de la máquina. Pensando...", false);
        this.gameView.setBoardInteraction(this.machinePlayerBoardGrid, false);

        if (this.machineTurnThread != null && this.machineTurnThread.isAlive()) {
            this.machineTurnThread.interrupt();
        }

        MachineTurnRunnable machineRunnable = new MachineTurnRunnable(this, MACHINE_TURN_THINK_DELAY_MS);
        this.machineTurnThread = new Thread(machineRunnable);
        this.machineTurnThread.setDaemon(true);
        this.machineTurnThread.start();
    }

    public void executeMachineTurnLogic() {
        if (this.gameState.isGameOver()) return;

        ShotOutcome outcome = this.gameState.handleMachinePlayerTurn();

        String message = this.buildShotMessage("Máquina disparó a " + outcome.getCoordinate().toAlgebraicNotation(), outcome);
        this.gameView.displayMessage(message, false);

        this.gameView.drawBoard(this.humanPlayerBoardGrid, this.gameState.getHumanPlayerPositionBoard(), true);

        if (this.checkAndHandleGameOver()) {
            return;
        }

        this.gameState.switchTurn();
        this.gameView.displayMessage("¡Es tu turno!", false);
        this.gameView.setBoardInteraction(this.machinePlayerBoardGrid, true);
    }

    private boolean checkAndHandleGameOver() {
        if (this.gameState.isGameOver()) {
            Player winner = this.gameState.getWinner();
            String winnerMessage;
            
            if (winner != null) {
                if (winner instanceof HumanPlayer) {
                    winnerMessage = "¡FELICITACIONES! ¡Has ganado la partida!";
                } else {
                    winnerMessage = "¡La máquina ha ganado! Mejor suerte la próxima vez.";
                }
                System.out.println("Ganador determinado: " + winner.getName());
            } else {
                // Esto no debería suceder, pero lo manejamos por seguridad
                winnerMessage = "¡Juego Terminado! No se pudo determinar el ganador.";
                System.err.println("ERROR: No se pudo determinar el ganador aunque el juego terminó.");
            }
            
            // Mostrar el mensaje del ganador
            this.gameView.displayMessage(winnerMessage, false);
            
            // Deshabilitar la interacción con ambos tableros
            this.gameView.setBoardInteraction(this.humanPlayerBoardGrid, false);
            this.gameView.setBoardInteraction(this.machinePlayerBoardGrid, false);
            
            return true;
        }
        return false;
    }


    /**
     * Maneja el clic en una celda del tablero principal (territorio enemigo).
     * Se usa durante la fase de disparos.
     *
     * @param row La fila de la celda clickeada.
     * @param col La columna de la celda clickeada.
     */
    public void handleFiringCellClick(int row, int col) {
        if (this.gameState.isGameOver() || !(this.gameState.getCurrentTurnPlayer() instanceof HumanPlayer)) {
            this.gameView.displayMessage("Espera tu turno.", true);
            return;
        }

        try {
            ShotOutcome outcome = this.gameState.handleHumanPlayerShot(row, col);

            String message = this.buildShotMessage("Disparo a " + outcome.getCoordinate().toAlgebraicNotation(), outcome);
            this.gameView.displayMessage(message, false);

            this.gameView.drawBoard(this.machinePlayerBoardGrid, this.gameState.getMachinePlayerTerritoryBoard(), false);

            if (this.checkAndHandleGameOver()) {
                return;
            }

            this.gameState.switchTurn();
            this.scheduleMachineTurn();

        } catch (OverlapException e) {
            // El jugador disparó a una casilla repetida. Mostramos el error y le permitimos disparar de nuevo.
            this.gameView.displayMessage(e.getMessage() + " Por favor, selecciona otra casilla.", true);
            // IMPORTANTE: No cambiamos de turno.
        } catch (OutOfBoundsException e) {
            this.gameView.displayMessage("Error: " + e.getMessage(), true);
        }
    }


    // ------------ Métodos auxiliares

    /**
     * Maneja el clic en una celda del tablero de posición del jugador humano.
     * Se usa durante la fase de colocación de barcos.
     *
     * @param row La fila de la celda clickeada.
     * @param col La columna de la celda clickeada.
     */
    public void handlePlacementCellClick(int row, int col) {
        if (this.selectedShipToPlace == null) {
            this.gameView.displayMessage("Por favor, selecciona un barco para colocar.", true);
            return;
        }

        try {
            // Ya no usamos un valor por defecto, usamos la variable 'chosenOrientation'
            this.gameState.placeHumanPlayerShip(this.selectedShipToPlace, row, col, this.chosenOrientation);

            // Actualizar la vista del tablero
            this.gameView.drawBoard(this.humanPlayerBoardGrid, this.gameState.getHumanPlayerPositionBoard(), true);
            this.gameView.displayMessage("Barco " + this.selectedShipToPlace + " colocado.", false);

            this.selectedShipToPlace = null; // Deseleccionar
            this.gameView.showOrientationControls(false); // Ocultar controles de orientación

            // Actualizar la lista de barcos a colocar
            this.gameView.showShipPlacementPhase(
                    this.gameState.getHumanPlayerPositionBoard(),
                    this.gameState.getPendingShipsToPlace()
            );

        } catch (Exception e) {
            this.gameView.displayMessage("Error: " + e.getMessage(), true);
        }
    }

    /**
     * Registra el tipo de barco que el jugador ha seleccionado del panel de colocación.
     *
     * @param shipType El tipo de barco seleccionado.
     */
    public void handleShipSelection(ShipType shipType) {
        this.selectedShipToPlace = shipType;
        this.gameView.showOrientationControls(true); // Mostrar controles
        this.gameView.updateOrientationButtons(this.chosenOrientation); // Resaltar el botón actual
        this.gameView.displayMessage("Seleccionado: " + shipType + ". Haz clic en tu tablero para colocarlo.", false);
    }

    /**
     * Construye un mensaje detallado basado en el resultado de un disparo.
     * @param baseMessage El inicio del mensaje (ej. "Disparo a A1").
     * @param outcome El resultado del disparo.
     * @return El mensaje completo y formateado.
     */
    private String buildShotMessage(String baseMessage, ShotOutcome outcome) {
        String message = switch (outcome.getResult()) {
            case WATER -> baseMessage + ", ¡Falla!";
            case TOUCHED -> baseMessage + ", ¡Acierto!";
            case SUNKEN -> baseMessage + ", ¡Acierto! Hundiste un " + outcome.getSunkenShip().getShipType() + " del enemigo.";
            case ALREADY_HIT -> baseMessage + ", ¡disparo repetido!";
        };
        // Impresión en consola para depuración
        System.out.println("Mensaje generado: " + message);
        return message;
    }
}
