package univalle.tedesoft.battleship.models.State;

import univalle.tedesoft.battleship.Exceptions.InvalidShipPlacementException;
import univalle.tedesoft.battleship.Exceptions.OutOfBoundsException;
import univalle.tedesoft.battleship.Exceptions.OverlapException;
import univalle.tedesoft.battleship.models.Board;
import univalle.tedesoft.battleship.models.Enums.GamePhase;
import univalle.tedesoft.battleship.models.Enums.Orientation;
import univalle.tedesoft.battleship.models.Enums.ShipType;
import univalle.tedesoft.battleship.models.Enums.ShotResult;
import univalle.tedesoft.battleship.models.Players.HumanPlayer;
import univalle.tedesoft.battleship.models.Players.MachinePlayer;
import univalle.tedesoft.battleship.models.Players.Player;

import java.util.List;

public abstract class GameState implements IGameState {
    //Tableros de juego.
    private Board humanPlayerBoard;
    private Board machineBoard;
    private Board machineTerritoryBoard;
    //Jugadores.
    private Player humanPlayer;
    private Player machinePlayer;
    private Player currentPlayer;
    //Contadores para indicar cuantas naves quedan en el tablero de cada jugador.
    private int humanPlayerSunkShipCount;
    private int computerPlayerSunkShipCount;
    //Fase actual del juego.
    private GamePhase currentPhase;

    /***/
    public GameState(String nameHumanPlayer) {
        //Tableros de juego necesarios.
        this.humanPlayerBoard = new Board();
        this.machineBoard = new Board();
        this.machineTerritoryBoard = new Board();
        //Cantidad de Barcos que posee cada jugador al inicio.
        this.humanPlayerSunkShipCount = 0;
        this.computerPlayerSunkShipCount = 0;
        this.currentPhase = GamePhase.INITIAL;
    }
    /**
     * Inicia una nueva partida.
     * Prepara los tableros para el jugador humano y la máquina.
     * El jugador humano deberá colocar sus barcos. La máquina los colocará automáticamente.
     * @param humanPlayer jugador humano.
     */
    @Override
    public void startNewGame(Player humanPlayer) {
        //Jugadores.
        this.humanPlayer = humanPlayer;
        this.machinePlayer = new MachinePlayer();
        this.currentPlayer = humanPlayer;
    }
    /**
     * Intenta colocar un barco para el jugador humano en su tablero de posición.
     * @param shipType El tipo de barco a colocar (ej. PORTAAVIONES, SUBMARINO).
     * @param row La fila (0-9) de la casilla de inicio del barco.
     * @param col La columna (0-9) de la casilla de inicio del barco.
     * @param orientation La orientación del barco (HORIZONTAL o VERTICAL).
     * @throws InvalidShipPlacementException si la colocación es inválida por superposición,
     *         salirse del tablero o tipo de barco ya colocado.
     */
    @Override
    public void placeHumanPlayerShip(ShipType shipType, int row, int col, Orientation orientation) throws InvalidShipPlacementException, OverlapException, OutOfBoundsException{

    };

    /**
     * Indica que el jugador humano ha terminado de colocar todos sus barcos.
     * La máquina también debe tener sus barcos colocados.
     * El juego pasa a la fase de disparos.
     */
    @Override
    public void finalizeShipPlacement() {
    }

    /**
     * Procesa un disparo realizado por el jugador humano en el tablero principal (de la máquina).
     * @param row La fila (0-9) del disparo.
     * @param col La columna (0-9) del disparo.
     * @return Un objeto ShotResult que indica las coordenadas del disparo y su resultado (AGUA, TOCADO, HUNDIDO).
     * @throws OutOfBoundsException si el disparo es fuera del tablero.
     */
    @Override
    public ShotResult handleHumanPlayerShot(int row, int col) throws OutOfBoundsException {
        return null;
    }

    /**
     * Ejecuta el turno de la máquina. La máquina elige una casilla para disparar
     * en el tablero del jugador humano.
     * @return Un objeto ShotResult que indica las coordenadas del disparo y su resultado.
     */
    @Override
    public ShotResult handleComputerPlayerTurn() {
        return null;
    }

    /**
     * Obtiene el tablero de posición del jugador humano.
     * Utilizado por la vista para mostrar los barcos del jugador y los disparos de la máquina.
     * @return El objeto Board del jugador humano.
     */
    @Override
    public Board getHumanPlayerPositionBoard() {
        return null;
    }

    /**
     * Obtiene el tablero principal, que representa la vista del jugador humano
     * sobre el territorio de la máquina. Muestra los resultados de los disparos del humano.
     * @return El objeto Board del territorio de la máquina (vista del jugador).
     */
    @Override
    public Board getMachinePlayerTerritoryBoard() {
        return null;
    }

    /**
     * Obtiene el tablero de posición real de la máquina.
     * Este método es para la HU-3 (visualización del tablero del oponente por el profesor).
     * No debe ser accesible para la lógica normal del juego del jugador.
     * @return El objeto Board con la disposición real de los barcos de la máquina.
     */
    @Override
    public Board getMachinePlayerActualPositionBoard() {
        return null;
    }

    /**
     * Verifica si el juego ha terminado (toda la flota de un jugador ha sido hundida).
     * @return true si el juego ha terminado, false en caso contrario.
     */
    @Override
    public boolean isGameOver() {
        return false;
    }

    /**
     * Obtiene el ganador del juego.
     * @return El PlayerType del ganador (HUMAN o COMPUTER), o null si el juego no ha terminado.
     */
    @Override
    public Player getWinner() {
        return null;
    }
    /**
     * Obtiene el jugador cuyo turno es actualmente.
     * @return El PlayerType del jugador actual.
     */
    @Override
    public Player getCurrentTurnPlayer() {
        return null;
    }

    /**
     * Guarda el estado actual del juego (tableros, turno, etc.) para poder reanudarlo.
     * Incluye la serialización del tablero y la información del jugador en archivos planos.
     */
    @Override
    public void saveGame() {

    }

    /**
     * Carga un estado de juego previamente guardado.
     * @return true si se cargó un juego exitosamente, false si no hay juego guardado o hay un error.
     */
    @Override
     public boolean loadGame() {
         return false;
     }

    /**
     * Verifica si existe un juego guardado que se pueda cargar.
     * @return true si hay un juego guardado, false en caso contrario.
     */
    @Override
    public boolean isSavedGameAvailable() {
        return false;
    }

    /**
     * Obtiene la lista de barcos que el jugador humano aún necesita colocar.
     * @return Una lista de ShipType.
     */
    @Override
    public List<ShipType> getPendingShipsToPlace() {
        return null;
    }

    /**
     * Devuelve el nickname del jugador humano.
     * @return El nickname.
     */
    @Override
    public String getHumanPlayerNickname() {
        return null;
    }

    /**
     * Devuelve la cantidad de barcos hundidos por el jugador humano.
     * @return Número de barcos de la máquina hundidos.
     */
    @Override
    public int getHumanPlayerSunkShipCount() {
        return 0;
    }

    /**
     * Devuelve la cantidad de barcos hundidos por la máquina.
     * @return Número de barcos del humano hundidos.
     */
    @Override
    public int getComputerPlayerSunkShipCount() {
        return 0;
    }


}
