package univalle.tedesoft.battleship.models.state;

import univalle.tedesoft.battleship.exceptions.InvalidShipPlacementException;
import univalle.tedesoft.battleship.exceptions.OutOfBoundsException;
import univalle.tedesoft.battleship.exceptions.OverlapException;
import univalle.tedesoft.battleship.models.enums.Orientation;
import univalle.tedesoft.battleship.models.enums.ShipType;
import univalle.tedesoft.battleship.models.enums.GamePhase;
import univalle.tedesoft.battleship.models.players.Player; // Necesitará ser definida
import univalle.tedesoft.battleship.models.Board;
import univalle.tedesoft.battleship.models.ShotOutcome;


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
    Board getMachinePlayerTerritoryBoard(); // Vista para el jugador humano

    /**
     * Obtiene el tablero de posición real de la máquina.
     * Este método es para la HU-3 (visualización del tablero del oponente por el profesor).
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
    Player getWinner(); // Debería devolver un tipo Player o un enum que identifique al ganador

    /**
     * Obtiene el jugador cuyo turno es actualmente.
     * @return El PlayerType del jugador actual.
     */
    Player getCurrentTurnPlayer(); // Debería devolver un tipo Player o un enum

    /**
     * Guarda el estado actual del juego (tableros, turno, etc.) para poder reanudarlo.
     * Incluye la serialización del tablero y la información del jugador en archivos planos.
     */
    void saveGame();

    /**
     * Carga un estado de juego previamente guardado.
     * @return true si se cargó un juego exitosamente, false si no hay juego guardado o hay un error.
     */
    boolean loadGame();

    /**
     * Verifica si existe un juego guardado que se pueda cargar.
     * @return true si hay un juego guardado, false en caso contrario.
     */
    boolean isSavedGameAvailable();

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
     * Devuelve la cantidad de barcos hundidos por el jugador humano.
     * @return Número de barcos de la máquina hundidos.
     */
    int getHumanPlayerSunkShipCount();

    /**
     * Devuelve la cantidad de barcos hundidos por la máquina.
     * @return Número de barcos del humano hundidos.
     */
    int getComputerPlayerSunkShipCount();

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
}