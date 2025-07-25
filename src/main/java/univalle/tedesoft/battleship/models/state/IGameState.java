package univalle.tedesoft.battleship.models.state;

import univalle.tedesoft.battleship.exceptions.InvalidShipPlacementException;
import univalle.tedesoft.battleship.exceptions.OutOfBoundsException;
import univalle.tedesoft.battleship.exceptions.OverlapException;
import univalle.tedesoft.battleship.models.enums.Orientation;
import univalle.tedesoft.battleship.models.enums.ShipType;
import univalle.tedesoft.battleship.models.enums.GamePhase;
import univalle.tedesoft.battleship.models.players.Player; // Necesitará ser definida
import univalle.tedesoft.battleship.models.board.Board;
import univalle.tedesoft.battleship.models.ships.Ship;
import univalle.tedesoft.battleship.models.board.ShotOutcome;


import java.util.List;

/**
 * Define el contrato para el estado y la lógica del juego Batalla Naval.
 * El GameController utiliza esta interfaz para interactuar con el modelo del juego,
 * gestionando la colocación de barcos, los disparos, los turnos y el estado general de la partida.
 */
public interface IGameState {

    /**
     * Inicia una nueva partida.
     * Prepara los tableros para el jugador humano y la máquina.
     * El jugador humano deberá colocar sus barcos. La máquina los colocará automáticamente.
     * @param humanPlayer El nombre del jugador humano.
     */
    void startNewGame(Player humanPlayer);

    /**
     * Intenta colocar un barco para el jugador humano en su tablero de posición.
     * Esta es la firma corregida que acepta los datos directamente desde la UI.
     *
     * @param shipType El tipo de barco a colocar (ej. AIRCRAFT_CARRIER).
     * @param row La fila (0-9) de la casilla de inicio del barco.
     * @param col La columna (0-9) de la casilla de inicio del barco.
     * @param orientation La orientación del barco (HORIZONTAL o VERTICAL).
     * @throws InvalidShipPlacementException si la colocación es inválida.
     * @throws OverlapException si el barco se superpone con otro.
     * @throws OutOfBoundsException si el barco se sale del tablero.
     */
    void placeHumanPlayerShip(ShipType shipType, int row, int col, Orientation orientation) throws InvalidShipPlacementException, OverlapException, OutOfBoundsException;

    /**
     * Indica que el jugador humano ha terminado de colocar todos sus barcos.
     * La máquina también debe tener sus barcos colocados.
     * El juego pasa a la fase de disparos.
     */
    void finalizeShipPlacement();

    /**
     * Procesa un disparo realizado por el jugador humano en el tablero principal (de la máquina).
     * @return Un objeto ShotResult que indica las coordenadas del disparo y su resultado (AGUA, TOCADO, HUNDIDO).
     * @throws OutOfBoundsException si el disparo es fuera del tablero.
     */
    ShotOutcome handleHumanPlayerShot(int row, int col) throws OutOfBoundsException, OverlapException;

    /**
     * Ejecuta el turno de la máquina. La máquina elige una casilla para disparar
     * en el tablero del jugador humano.
     * @return Un objeto ShotResult que indica las coordenadas del disparo y su resultado.
     */
    ShotOutcome handleMachinePlayerTurn();

    /**
     * Obtiene el tablero de posición del jugador humano.
     * Utilizado por la vista para mostrar los barcos del jugador y los disparos de la máquina.
     * @return El objeto Board del jugador humano.
     */
    Board getHumanPlayerPositionBoard();

    /**
     * Obtiene el tablero principal, que representa la vista del jugador humano
     * sobre el territorio de la máquina. Muestra los resultados de los disparos del humano.
     * @return El objeto Board del territorio de la máquina (vista del jugador).
     */
    Board getMachinePlayerTerritoryBoard();

    /**
     * Obtiene el tablero de posición real de la máquina.
     * No debe ser accesible para la lógica normal del juego del jugador.
     * @return El objeto Board con la disposición real de los barcos de la máquina.
     */
    Board getMachinePlayerActualPositionBoard();

    /**
     * Verifica si el juego ha terminado (toda la flota de un jugador ha sido hundida).
     * @return true si el juego ha terminado, false en caso contrario.
     */
    boolean isGameOver();

    /**
     * Obtiene el ganador del juego.
     * @return El PlayerType del ganador (HUMAN o COMPUTER), o null si el juego no ha terminado.
     */
    Player getWinner();

    /**
     * Obtiene el jugador cuyo turno es actualmente.
     * @return El PlayerType del jugador actual.
     */
    Player getCurrentTurnPlayer();

    /**
     * Guarda el estado actual del juego (tableros, turno, etc.) para poder reanudarlo.
     * Incluye la serialización del tablero y la información del jugador en archivos planos.
     */
    void saveGame();

    /**
     * Obtiene la lista de barcos que el jugador humano aún necesita colocar.
     * @return Una lista de ShipType.
     */
    List<ShipType> getPendingShipsToPlace();

    /**
     * Devuelve el nickname del jugador humano.
     * @return El nickname.
     */
    String getHumanPlayerNickname();

    /**
     * Cambia el turno al siguiente jugador.
     */
    void switchTurn();

    /**
     * Obtiene la fase actual del juego
     * @return La fase actual del juego
     */
    GamePhase getCurrentPhase();

    /**
     * Coloca todos los barcos del jugador humano de forma aleatoria en el tablero.
     */
    void placeHumanPlayerShipsRandomly();

    /**
     * Mueve un barco ya colocado a una nueva posición en el tablero del jugador humano.
     * Si la nueva posición es inválida, el barco se restaura a su ubicación original.
     *
     * @param shipToMove El objeto Ship que se desea mover.
     * @param newRow La nueva fila de inicio para el barco.
     * @param newCol La nueva columna de inicio para el barco.
     * @throws InvalidShipPlacementException si el movimiento es inválido.
     */
    void moveHumanPlayerShip(Ship shipToMove, int newRow, int newCol) throws InvalidShipPlacementException, OverlapException, OutOfBoundsException;

    /**
     * Carga una partida específica por nickname.
     * @param nickname El nombre del jugador cuya partida se quiere cargar
     * @return true si se cargó exitosamente, false en caso contrario
     */
    boolean loadGame(String nickname);

    /**
     * Crea un memento con el estado actual del juego
     * @return El memento creado
     */
    GameMemento createMemento();

    /**
     * Restaura el estado del juego desde un memento
     * @param memento El memento a restaurar
     */
    void restoreFromMemento(GameMemento memento);
}