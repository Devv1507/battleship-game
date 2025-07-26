package univalle.tedesoft.battleship.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import univalle.tedesoft.battleship.exceptions.OutOfBoundsException;
import univalle.tedesoft.battleship.exceptions.OverlapException;
import univalle.tedesoft.battleship.models.board.Coordinate;
import univalle.tedesoft.battleship.models.enums.GamePhase;
import univalle.tedesoft.battleship.models.enums.Orientation;
import univalle.tedesoft.battleship.models.enums.ShipType;
import univalle.tedesoft.battleship.models.players.HumanPlayer;
import univalle.tedesoft.battleship.models.players.MachinePlayer;
import univalle.tedesoft.battleship.models.players.Player;
import univalle.tedesoft.battleship.models.ships.Ship;
import univalle.tedesoft.battleship.models.board.ShotOutcome;
import univalle.tedesoft.battleship.models.state.IGameState;
import univalle.tedesoft.battleship.threads.MachineTurnRunnable;
import univalle.tedesoft.battleship.views.GameView;
import univalle.tedesoft.battleship.views.InstructionsView;
import univalle.tedesoft.battleship.views.ViewUtils;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public class GameController {
    // --- Componentes FXML ---
    /** Botón para confirmar la colocación de todos los barcos y pasar a la fase de disparos. */
    @FXML public Button finalizePlacementButton;
    /** Botón para mostrar/ocultar el tablero real del oponente. */
    @FXML public Button toggleOpponentBoardButton;
    /** Botón para seleccionar la orientación horizontal al colocar un barco. */
    @FXML public Button horizontalButton;
    /** Botón para seleccionar la orientación vertical al colocar un barco. */
    @FXML public Button verticalButton;
    /** Botón para guardar el estado actual de la partida. */
    @FXML public Button toggleAutoSaveButton;
    /** Botón para que el sistema coloque los barcos del jugador de forma aleatoria. */
    @FXML public Button placeRandomlyButton;
    /** Botón para mostrar la ventana de instrucciones del juego. */
    @FXML public Button instructionsButton;
    /** Botón para reiniciar la partida actual al estado inicial de colocación. */
    @FXML public Button restartGameButton;
    /** Tablero donde el jugador coloca sus barcos y recibe disparos. */
    @FXML public GridPane humanPlayerBoardGrid;
    /** Tablero de la máquina, donde el jugador dispara al oponente. */
    @FXML public GridPane machinePlayerBoardGrid;
    /** Panel lateral izquierdo que muestra los barcos disponibles para colocar. */
    @FXML public VBox shipPlacementPane;
    /** Contenedor para los botones de control de orientación (Horizontal/Vertical). */
    @FXML public VBox orientationControlPane;
    /** Capa de dibujo sobre el tablero humano, usada para renderizar las formas de los barcos. */
    @FXML public Pane humanPlayerDrawingPane;
    /** Capa de dibujo sobre el tablero de la máquina, usada para renderizar barcos hundidos y efectos. */
    @FXML public Pane machinePlayerDrawingPane;
    /** Contenedor que agrupa el tablero humano y su capa de dibujo. */
    @FXML public StackPane humanPlayerBoardContainer;
    /** Contenedor que agrupa el tablero de la máquina y su capa de dibujo. */
    @FXML public StackPane machinePlayerBoardContainer;
    /** Contenedor superior para mostrar mensajes de estado al jugador. */
    @FXML public VBox messageContainer;

    // --- Referencias principales ---
    /** Referencia al modelo del juego (GameState). El controlador delega toda la lógica de negocio y manipulación de datos a esta instancia. */
    private IGameState gameState;
    /** Referencia a la vista del juego (GameView). El controlador se comunica con la vista para actualizar la UI y recibir eventos. */
    private GameView gameView;
    /** Hilo que maneja el turno de la máquina. Se usa para introducir un retraso de "pensamiento" antes de que la máquina dispare. */
    private Thread machineTurnThread;

    // --- Estado interno del controlador ---
    /** Tipo de barco seleccionado por el jugador para colocar en el tablero. */
    private ShipType selectedShipToPlace;
    /** Orientación elegida para colocar el barco seleccionado (Horizontal o Vertical). */
    private Orientation chosenOrientation = Orientation.HORIZONTAL;
    /** Barco que se está arrastrando actualmente. Se usa para mover barcos ya colocados en el tablero. */
    private Ship shipBeingDragged = null;
    /** Desplazamiento del clic del ratón dentro del barco arrastrado. */
    private Coordinate mouseClickOffsetInShip = null;
    /** Indica si el tablero del oponente está visible. Se usa para alternar entre mostrar el tablero real de la máquina o su vista normal. */
    private boolean isOpponentBoardVisible = false;
    /** Tiempo de espera en milisegundos antes de que la máquina realice su turno. Este valor se usa para simular el "tiempo de pensamiento" de la máquina. */
    private static final long MACHINE_TURN_THINK_DELAY_MS = 1500;
    /** Flag que indica si el guardado automático está activado. Por defecto, está habilitado. */
    private boolean isAutoSaveEnabled = true;

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

    // ----- Setters y getters -----

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

    /**
     * Procesa la lista de barcos pendientes del modelo y devuelve un mapa
     * con el recuento de cada tipo de barco.
     * Este es el formato de datos que la vista necesita para renderizar el panel de colocación.
     *
     * @return Un Map donde la clave es ShipType y el valor es la cantidad pendiente.
     */
    public Map<ShipType, Long> getPendingShipCounts() {
        if (this.gameState == null) {
            // Devuelve un mapa vacío si el estado del juego no está listo
            return Collections.emptyMap();
        }

        // Obtener la lista de barcos pendientes del modelo y agruparlos por tipo
        List<ShipType> pendingShips = this.gameState.getPendingShipsToPlace();
        return pendingShips.stream().collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
    }

    // ----- Handlers o Manejadores de Eventos con FXML -----

    /**
     * Maneja el clic en el botón "Reiniciar Juego".
     * Muestra una alerta de confirmación y, si el usuario acepta, reinicia el estado del juego.
     */
    @FXML
    void onRestartGameClick() {
        if (this.gameState == null || this.gameView == null) {
            return;
        }
        // Llamar a ViewUtils para mostrar el diálogo de confirmación.
        Optional<ButtonType> result = ViewUtils.showConfirmationDialog(
                "Confirmar Reinicio",
                "¿Está seguro de reiniciar?",
                "Si no ha guardado, perderá todo el avance del juego."
        );

        // Si el usuario confirma, reiniciamos el juego.
        if (result.isPresent() && result.get() == ButtonType.OK) {
            this.restartGame();
        }
    }

    /**
     * Se activa cuando el jugador hace clic en el botón "Finalizar Colocación".
     * Notifica al modelo y actualiza la vista para pasar a la fase de disparos.
     */
    @FXML
    void onFinalizePlacementClick() {
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
     */
    @FXML
    void onHorizontalClick() {
        this.chosenOrientation = Orientation.HORIZONTAL;
        this.gameView.updateOrientationButtons(this.chosenOrientation);
        this.gameView.displayMessage("Orientación seleccionada: Horizontal.", false);
    }

    /**
     * Se activa cuando el jugador hace clic en el botón "Vertical".
     */
    @FXML
    void onVerticalClick() {
        this.chosenOrientation = Orientation.VERTICAL;
        this.gameView.updateOrientationButtons(this.chosenOrientation);
        this.gameView.displayMessage("Orientación seleccionada: Vertical.", false);
    }

    /**
     * Maneja el clic en el botón para ver/ocultar el tablero del oponente.
     */
    @FXML
    void onToggleOpponentBoardClick() {
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
                    this.gameState.getMachinePlayerActualPositionBoard(),
                    false // false para ocultar los barcos
            );
            this.gameView.updateToggleButtonText("Ver Tablero Oponente");
        }
    }

    /**
     * Maneja el clic en el botón para guardar el juego.
     */
    @FXML
    void onToggleAutoSaveClick() {
        if (this.gameState == null) {
            this.gameView.displayMessage("Error: No hay juego activo para guardar.", true);
            return;
        }
        // Invertir el estado actual del guardado automático.
        this.isAutoSaveEnabled = !this.isAutoSaveEnabled;
        // Pedir a la vista que actualice el estilo y texto del botón.
        this.gameView.updateAutoSaveButton(this.isAutoSaveEnabled);
        // Mostrar un mensaje al usuario sobre el estado del guardado automático.
        if (this.isAutoSaveEnabled) {
            this.gameView.displayMessage("Guardado automático ACTIVADO.", false);
            this.gameState.saveGame();
        } else {
            this.gameView.displayMessage("Guardado automático DESACTIVADO.", false);
        }
    }

    /**
     * Maneja el clic en el botón "Colocar Aleatoriamente".
     * Llama al modelo para que coloque los barcos del jugador humano al azar
     * y luego actualiza la vista para reflejar los cambios.
     */
    @FXML
    void onPlaceRandomlyClick() {
        if (this.gameState == null || this.gameView == null) {
            return;
        }

        // Pedir al modelo que coloque los barcos aleatoriamente.
        this.gameState.placeHumanPlayerShipsRandomly();
        this.gameView.drawBoard(this.humanPlayerBoardGrid, this.gameState.getHumanPlayerPositionBoard(), true);
        this.gameView.showShipPlacementPhase(
                this.gameState.getHumanPlayerPositionBoard(),
                this.gameState.getPendingShipsToPlace()
        );
        this.gameView.displayMessage("¡Tus barcos han sido colocados aleatoriamente! Presiona 'Finalizar Colocación'.", false);

        // Guardar el estado del juego si el guardado automático está habilitado.
        this.autoSaveIfEnabled();
    }

    /**
     * Maneja el clic en el botón "Instrucciones".
     * Muestra la ventana con las reglas del juego.
     */
    @FXML
    void onInstructionsClick() {
        try {
            InstructionsView.getInstance().show();
        } catch (IOException e) {
            e.printStackTrace();
            this.gameView.displayMessage("Error al abrir las instrucciones.", true);
        }
    }

    // ----- Métodos con lógica central del juego -----

    /**
     * Agenda e inicia el turno de la máquina en un hilo de ejecución separado.
     * Crea un hilo con MachineTurnRunnable para introducir un retraso de "pensamiento".
     * Maneja el caso de que el juego ya haya terminado y cancela cualquier turno de máquina
     *  previo que aún estuviera en ejecución para evitar condiciones de carrera.
     * @see MachineTurnRunnable
     */
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

    /**
     * Ejecuta la lógica central para un único disparo de la máquina.
     * Pide al modelo que realice un disparo, actualiza el tablero del jugador humano con el resultado
     *  y muestra un mensaje con el resultado del disparo.
     * @see MachineTurnRunnable
     */
    public void executeMachineTurnLogic() {
        if (this.gameState.isGameOver()) return;
        ShotOutcome outcome = this.gameState.handleMachinePlayerTurn();
        // Construir y mostrar el mensaje del resultado del disparo de la máquina.
        String message = this.buildShotMessage("Máquina disparó a " + outcome.getCoordinate().toAlgebraicNotation(), outcome);
        this.gameView.displayMessage(message, false);
        // Actualizar el tablero del jugador para mostrar el disparo de la máquina.
        this.gameView.drawBoard(this.humanPlayerBoardGrid, this.gameState.getHumanPlayerPositionBoard(), true);
        // Guardar el estado del juego si el guardado automático está habilitado.
        this.autoSaveIfEnabled();
        // Lógica de cambio de turno
        this.processTurnContinuation(outcome);
    }

    /**
     * Comprueba si la partida ha finalizado y, si es así, gestiona el estado de fin de juego.
     * Si el juego ha terminado, determina al ganador, muestra un mensaje de victoria/derrota
     *  y deshabilita la interacción con ambos tableros.
     * @return true si el juego ha terminado, false si la partida continúa.
     */
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
            // Construir y mostrar el mensaje del resultado del disparo.
            String message = this.buildShotMessage("Disparo a " + outcome.getCoordinate().toAlgebraicNotation(), outcome);
            this.gameView.displayMessage(message, false);
            // Actualizar el tablero del oponente para mostrar el resultado.
            this.gameView.drawBoard(this.machinePlayerBoardGrid, this.gameState.getMachinePlayerActualPositionBoard(), false);
            // Guardar el estado del juego si el guardado automático está habilitado.
            this.autoSaveIfEnabled();
            // Comprobar si el juego ha terminado después del disparo.
            if (this.checkAndHandleGameOver()) {
                return;
            }
            // Lógica de cambio de turno
            this.processTurnContinuation(outcome);

        } catch (OverlapException e) {
            // El jugador disparó a una casilla repetida. Mostrar el error y permitir disparar de nuevo.
            this.gameView.displayMessage(e.getMessage() + " Por favor, selecciona otra casilla.", true);
        } catch (OutOfBoundsException e) {
            this.gameView.displayMessage("Error: " + e.getMessage(), true);
        }
    }

    /**
     * Maneja el clic en una celda del tablero de posición del jugador humano.
     * Se usa durante la fase de colocación de barcos.
     *
     * @param row La fila de la celda clickeada.
     * @param col La columna de la celda clickeada.
     */
    public void handlePlacementCellClick(int row, int col) {
        if (this.shipBeingDragged != null) {
            // Si estamos en medio de un arrastre, un clic simple no debe hacer nada.
            return;
        }

        if (this.selectedShipToPlace == null) {
            this.gameView.displayMessage("Por favor, selecciona un barco para colocar.", true);
            return;
        }

        try {
            if (this.selectedShipToPlace == ShipType.FRIGATE) {
                // Si es un FRIGATE, por defecto la orientación es horizontal.
                this.chosenOrientation = Orientation.HORIZONTAL;
            }
            this.gameState.placeHumanPlayerShip(this.selectedShipToPlace, row, col, this.chosenOrientation);

            // Actualizar la vista del tablero
            this.gameView.drawBoard(this.humanPlayerBoardGrid, this.gameState.getHumanPlayerPositionBoard(), true);
            this.gameView.displayMessage("Barco " + this.selectedShipToPlace + " colocado.", false);

            // Ocultar controles de orientación
            this.selectedShipToPlace = null;
            this.gameView.showOrientationControls(false);

            // Actualizar la lista de barcos a colocar
            this.gameView.showShipPlacementPhase(
                    this.gameState.getHumanPlayerPositionBoard(),
                    this.gameState.getPendingShipsToPlace()
            );

            // Guardar el estado del juego si el guardado automático está habilitado.
            this.autoSaveIfEnabled();

        } catch (Exception e) {
            this.gameView.displayMessage("Error: " + e.getMessage(), true);
        }
    }

    /**
     * Inicia el proceso de arrastre de un barco existente.
     * Se activa con un doble clic presionado en una celda ocupada por un barco.
     *
     * @param row Fila donde se inició el arrastre.
     * @param col Columna donde se inició el arrastre.
     */
    public void handleShipDragStart(int row, int col) {
        if (this.gameState.getCurrentPhase() != GamePhase.PLACEMENT) {
            return;
        }

        Ship clickedShip = this.gameState.getHumanPlayerPositionBoard().getShipAt(row, col);

        if (clickedShip != null) {
            this.shipBeingDragged = clickedShip;

            Coordinate shipOrigin = clickedShip.getOccupiedCoordinates().get(0);
            int offsetX = col - shipOrigin.getX();
            int offsetY = row - shipOrigin.getY();
            this.mouseClickOffsetInShip = new Coordinate(offsetX, offsetY);

            // Notificar a la vista para que oculte el barco original.
            this.gameView.startShipDrag(this.shipBeingDragged);

            // Mostrar una previsualización inicial en la posición actual.
            this.handleShipDrag(row, col);
        }
    }

    /**
     * Actualiza la posición de la previsualización del barco que se está arrastrando.
     *
     * @param row La fila actual del cursor sobre el tablero.
     * @param col La columna actual del cursor sobre el tablero.
     */
    public void handleShipDrag(int row, int col) {
        if (this.shipBeingDragged == null) {
            return;
        }

        int newTopLeftCol = col - this.mouseClickOffsetInShip.getX();
        int newTopLeftRow = row - this.mouseClickOffsetInShip.getY();

        this.gameView.updateDragPreview(this.shipBeingDragged, newTopLeftRow, newTopLeftCol);
    }

    /**
     * Finaliza el proceso de arrastre, intentando mover el barco a la nueva posición.
     *
     * @param row La fila donde se soltó el ratón.
     * @param col La columna donde se soltó el ratón.
     */
    public void handleShipDragEnd(int row, int col) {
        if (this.shipBeingDragged == null) {
            return;
        }

        this.gameView.clearDragPreview();

        int finalTopLeftCol = col - this.mouseClickOffsetInShip.getX();
        int finalTopLeftRow = row - this.mouseClickOffsetInShip.getY();

        try {
            this.gameState.moveHumanPlayerShip(this.shipBeingDragged, finalTopLeftRow, finalTopLeftCol);
            this.gameView.displayMessage("Barco " + this.shipBeingDragged.getShipType()  + " movido exitosamente.", false);
            this.autoSaveIfEnabled();
        } catch (Exception e) {
            this.gameView.displayMessage("Movimiento inválido: " + e.getMessage(), true);
        } finally {
            this.shipBeingDragged = null;
            this.mouseClickOffsetInShip = null;

            // Es crucial refrescar toda la UI para que el barco aparezca de nuevo,
            // ya sea en su nueva posición o en la original si el movimiento falló.
            this.gameView.refreshUI();
        }
    }

    /**
     * Lógica centralizada para reiniciar el juego.
     * Restablece el modelo y actualiza la vista al estado inicial.
     */
    private void restartGame() {
        // Guardamos el jugador actual para no perder su nombre.
        Player currentPlayer = new HumanPlayer(this.getGameState().getHumanPlayerNickname());

        // Reiniciar el modelo.
        this.getGameState().startNewGame(currentPlayer);

        // Reiniciar el estado interno del controlador.
        this.isOpponentBoardVisible = false;

        // Actualizar la vista.
        this.gameView.resetToPlacementPhase();
        this.gameView.updateToggleButtonText("Ver Tablero Oponente");
        this.gameView.displayMessage("Juego reiniciado. Coloca tus barcos.", false);
    }

    // ----- Métodos auxiliares -----

    /**
     * Registra el tipo de barco que el jugador ha seleccionado del panel de colocación.
     *
     * @param shipType El tipo de barco seleccionado.
     */
    public void handleShipSelection(ShipType shipType) {
        this.selectedShipToPlace = shipType;
        // Si el barco es un FRIGATE, no necesita controles de orientación.
        if (shipType == ShipType.FRIGATE) {
            this.gameView.showOrientationControls(false);
            this.gameView.displayMessage("Seleccionado: " + shipType + ". Es un barco de 1 casilla, solo haz clic para colocarlo.", false);

        } else {
            this.gameView.showOrientationControls(true);
            this.gameView.updateOrientationButtons(this.chosenOrientation);
            this.gameView.displayMessage("Seleccionado: " + shipType + ". Elige una orientación y haz clic en tu tablero para colocarlo.", false);
        }
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
        return message;
    }

    /**
     * Procesa el resultado de un disparo y determina la continuación del turno.
     * Permite centralizar la lógica de turnos entre el turno del jugador humano y el de la máquina.
     *
     * @param outcome El resultado del disparo que acaba de ocurrir.
     */
    private void processTurnContinuation(ShotOutcome outcome) {
        // Verificar si el juego terminó después de este disparo.
        if (this.checkAndHandleGameOver()) {
            return;
        }
        // Identificar quién es el jugador actual para aplicar la lógica correcta.
        Player currentPlayer = this.gameState.getCurrentTurnPlayer();

        switch (outcome.getResult()) {
            case TOUCHED:
            case SUNKEN:
                // En caso de acierto, el turno continúa con el mismo jugador.
                if (currentPlayer instanceof HumanPlayer) {
                    this.gameView.displayMessage("¡Buen tiro! Vuelves a disparar.", false);
                    // No se hace nada más, el control permanece con el jugador humano.
                } else {
                    this.gameView.displayMessage("La máquina ha acertado. ¡Vuelve a disparar!", false);
                    // Se programa otro turno para la máquina.
                    this.scheduleMachineTurn();
                }
                break;

            case ALREADY_HIT:
                // En caso de disparo repetido, el jugador actual tiene otra oportunidad.
                // Para el jugador humano, este caso es manejado principalmente por la excepción OverlapException
                if (currentPlayer instanceof MachinePlayer) {
                    this.scheduleMachineTurn();
                }
                break;

            case WATER:
            default:
                // En caso de fallo o disparo repetido, se cambia el turno al otro jugador.
                this.gameState.switchTurn();

                if (currentPlayer instanceof HumanPlayer) {
                    // Si el humano acaba de fallar, es el turno de la máquina.
                    this.scheduleMachineTurn();
                } else {
                    // Si la máquina acaba de fallar, es el turno del humano.
                    this.gameView.displayMessage("¡Es tu turno!", false);
                    this.gameView.setBoardInteraction(this.machinePlayerBoardGrid, true);
                }
                break;
        }
    }

    /**
     * Guarda el estado del juego si la opción de autoguardado está habilitada.
     * Muestra un mensaje sutil al usuario para confirmar la acción.
     */
    private void autoSaveIfEnabled() {
        if (this.isAutoSaveEnabled) {
            if (this.gameState == null) {
                return;
            }
            try {
                this.gameState.saveGame();
            } catch (Exception e) {
                this.gameView.displayMessage("Error en el guardado automático: " + e.getMessage(), true);
            }
        }
    }
}
