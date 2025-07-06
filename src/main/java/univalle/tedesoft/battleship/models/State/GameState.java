package univalle.tedesoft.battleship.models.State;

import univalle.tedesoft.battleship.Exceptions.InvalidShipPlacementException;
import univalle.tedesoft.battleship.Exceptions.OutOfBoundsException;
import univalle.tedesoft.battleship.Exceptions.OverlapException;
import univalle.tedesoft.battleship.models.Board;
import univalle.tedesoft.battleship.models.Coordinate;
import univalle.tedesoft.battleship.models.Enums.*;
import univalle.tedesoft.battleship.models.Players.HumanPlayer;
import univalle.tedesoft.battleship.models.Players.MachinePlayer;
import univalle.tedesoft.battleship.models.Players.Player;
import univalle.tedesoft.battleship.models.Ships.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Clase que representa la instancia del juego.
 * @author David Esteban Valencia
 * @author Santiago David Guerrero
 * @author Juan Pablo Escamilla
 */
public abstract class GameState implements IGameState {
    /**Tableros de juego*/
    private Board humanPlayerBoard;
    private Board machinePlayerBoard;
    private Board machinePlayerTerritoryBoard;
    /**Jugadores*/
    private Player humanPlayer;
    private Player machinePlayer;
    private Player currentPlayer;
    /**Contadores para indicar cuantas naves quedan en el tablero de cada jugador*/
    private int humanPlayerSunkShipCount;
    private int computerPlayerSunkShipCount;
    /**Fase actual del juego*/
    private GamePhase currentPhase;
    /**Cantidad de Barcos que el humano tiene a su disposicion para colocar en la tabla*/
    private List<ShipType> pendingShipsToPlaceForHuman;

    /** Constructor de la Clase*/
    public GameState() {
        //Tableros de juego necesarios.
        this.humanPlayerBoard = new Board();
        this.machinePlayerBoard = new Board();
        this.machinePlayerTerritoryBoard = new Board();
        //Cantidad de Barcos hundidos de cada jugador al inicio del juego.
        this.humanPlayerSunkShipCount = 0;
        this.computerPlayerSunkShipCount = 0;
        //Fase inicial del juego.
        this.currentPhase = GamePhase.INITIAL;
        this.pendingShipsToPlaceForHuman = new ArrayList<>();
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
        //Se inicializa las tablas.
        this.humanPlayerBoard.resetBoard();
        this.machinePlayerBoard.resetBoard();
        this.machinePlayerTerritoryBoard.resetBoard();
        //El juego empieza en su fase inicial.
        this.currentPhase = GamePhase.PLACEMENT;
        //Barcos que el humano ha colocado.
        this.pendingShipsToPlaceForHuman.clear();
        //Barcos que el humano debe movilizar en la tabla.
        this.pendingShipsToPlaceForHuman.addAll(createFleetShipTypes());
        this.humanPlayerSunkShipCount = 0;
        this.computerPlayerSunkShipCount = 0;

    }
    /**
     * Intenta colocar un barco para el jugador humano en su tablero de posición, la mayor parte
     * de esta tarea se realiza en Board.
     * @param shipType El barco a colocar (ej. PORTAAVIONES, SUBMARINO).
     * @param row coordenada en fila donde se piensa ubicar el ship.
     * @param col coordenada en columna
     * @throws InvalidShipPlacementException si la colocación es inválida por superposición,
     *         salirse del tablero o tipo de barco ya colocado.
     */
    @Override
    public void placeHumanPlayerShip(ShipType shipType, int row, int col, Orientation orientation) throws InvalidShipPlacementException, OverlapException, OutOfBoundsException {

        // 1. Validar que el tipo de barco está pendiente de ser colocado.
        if (!this.pendingShipsToPlaceForHuman.contains(shipType)) {
            throw new InvalidShipPlacementException("Ya has colocado todos los barcos de tipo: " + shipType);
        }

        // 2. Crear los objetos de dominio necesarios.
        Ship newShip = createShipFromType(shipType);
        newShip.setOrientation(orientation);
        Coordinate coordinate = new Coordinate(col, row); // Recordar que Coordinate(x, y) -> (col, row)

        // 3. Delegar la colocación al tablero.
        if (this.humanPlayerBoard.placeShip(newShip, coordinate)) {
            // 4. Si la colocación fue exitosa, remover el tipo de barco de la lista de pendientes.
            this.pendingShipsToPlaceForHuman.remove(shipType);
        } else {
            // Esta línea es teóricamente inalcanzable si placeShip lanza excepciones, pero es una buena práctica.
            throw new InvalidShipPlacementException("No fue posible agregar esta embarcacion!!");
        }
    };

    /**
     * Metodo de fábrica privado para crear una instancia de Ship a partir de su tipo.
     * Esto centraliza la lógica de creación de barcos.
     * @param type El enum ShipType del barco a crear.
     * @return una nueva instancia del barco correspondiente.
     */
    private Ship createShipFromType(ShipType type) {
        switch (type) {
            case AIR_CRAFT_CARRIER:
                return new AirCraftCarrier();
            case SUBMARINE:
                return new Submarine();
            case DESTROYER:
                return new Destroyer();
            case FRIGATE:
                return new Frigate();
            default:
                // Esto no debería ocurrir si el enum está completo.
                throw new IllegalArgumentException("Tipo de barco desconocido: " + type);
        }
    }


    /**
     * Este metodo crea la lista de TIPOS de barcos que cada jugador debe poseer.
     * @return una lista de ShipType con la flota completa.
     */
    public List<ShipType> createFleetShipTypes() {
        List<ShipType> fleetTypes = new ArrayList<>();
        fleetTypes.add(ShipType.AIR_CRAFT_CARRIER); // 1
        fleetTypes.add(ShipType.SUBMARINE);        // 2
        fleetTypes.add(ShipType.SUBMARINE);
        fleetTypes.add(ShipType.DESTROYER);        // 3
        fleetTypes.add(ShipType.DESTROYER);
        fleetTypes.add(ShipType.DESTROYER);
        fleetTypes.add(ShipType.FRIGATE);          // 4
        fleetTypes.add(ShipType.FRIGATE);
        fleetTypes.add(ShipType.FRIGATE);
        fleetTypes.add(ShipType.FRIGATE);
        return fleetTypes;
    }

    /**
     * Indica que el jugador humano ha terminado de colocar todos sus barcos.
     * La máquina también debe tener sus barcos colocados.
     * El juego pasa a la fase de disparos.
     */
    @Override
    public void finalizeShipPlacement() {
        if (!this.pendingShipsToPlaceForHuman.isEmpty()) {
            // No se puede finalizar si aún faltan barcos por colocar.
            return;
        }

        // Si la colocación del jugador está completa, la máquina coloca su flota.
        this.placeMachinePlayerShips();

        this.currentPhase = GamePhase.FIRING;
    }

    /**
     * Procesa un disparo realizado por el jugador humano en el tablero principal (de la máquina).
     * @param coordinate Coordenada entre la fila (0-9) y la columna (0-9), donde se realiza un disparo.
     * @return Un objeto ShotResult que indica las coordenadas del disparo y su resultado (AGUA, TOCADO, HUNDIDO).
     * @throws OutOfBoundsException si el disparo es fuera del tablero.
     */
    @Override
    public ShotResult handleHumanPlayerShot(Coordinate coordinate) throws OutOfBoundsException {
        return machinePlayerBoard.receiveShot(coordinate);
    }

    /**
     * Ejecuta el turno de la máquina. La máquina elige una casilla para disparar
     * en el tablero del jugador humano.
     * @return Un objeto ShotResult que indica las coordenadas del disparo y su resultado.
     */
    @Override
    public ShotResult handleComputerPlayerTurn() {
        if (this.currentPhase != GamePhase.FIRING || this.currentPlayer != this.machinePlayer) {
            System.err.println("Advertencia: handleComputerPlayerTurn llamado fuera de turno/fase.");
            return null;
        }
        Random random = new Random();
        Coordinate shotCoordinate;
        ShotResult result;
        boolean validShotChosen = false;
        int attempts = 0;
        final int MAX_SHOT_ATTEMPTS = 100;

        CellState cellStateAtTarget;
        do {
            int row = random.nextInt(this.humanPlayerBoard.getSize());
            int col = random.nextInt(this.humanPlayerBoard.getSize());
            shotCoordinate = new Coordinate(col, row);
            try {
                cellStateAtTarget = this.humanPlayerBoard.getCellState(row, col);
                if (cellStateAtTarget == CellState.EMPTY || cellStateAtTarget == CellState.SHIP) {
                    validShotChosen = true;
                }
            } catch (OutOfBoundsException e) {
                validShotChosen = false;
            }
            attempts++;
        } while (!validShotChosen && attempts < MAX_SHOT_ATTEMPTS);
        if (!validShotChosen) {
            if (!isGameOver()) {
                System.err.println("Error crítico: La IA no pudo encontrar una celda válida para disparar, pero el juego no ha terminado.");
            }
            return ShotResult.ALREADY_HIT;
        }
        try {
            result = this.humanPlayerBoard.receiveShot(shotCoordinate);
        } catch( OutOfBoundsException e) {
            System.err.println("Error inesperado: Disparo de la IA fuera de límites después de validación.");
            result = ShotResult.WATER;
        }
        this.saveGame();
        return result;
    }

    /**
     * Obtiene el tablero de posición del jugador humano.
     * Utilizado por la vista para mostrar los barcos del jugador y los disparos de la máquina.
     * @return El objeto Board del jugador humano.
     */
    @Override
    public Board getHumanPlayerPositionBoard() {
        return this.humanPlayerBoard;
    }

    /**
     * Obtiene el tablero principal, que representa la vista del jugador humano
     * sobre el territorio de la máquina. Muestra los resultados de los disparos del humano.
     * @return El objeto Board del territorio de la máquina (vista del jugador).
     */
    @Override
    public Board getMachinePlayerTerritoryBoard() {
        return this.machinePlayerTerritoryBoard;
    }

    /**
     * Obtiene el tablero de posición real de la máquina.
     * Este método es para la HU-3 (visualización del tablero del oponente por el profesor).
     * No debe ser accesible para la lógica normal del juego del jugador.
     * @return El objeto Board con la disposición real de los barcos de la máquina.
     */
    @Override
    public Board getMachinePlayerActualPositionBoard() {
        return this.machinePlayerBoard;
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
        return new ArrayList<>(this.pendingShipsToPlaceForHuman);
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

    /**
     * Metodo que crea la cantidad de barcos que cada jugador debe poseer en su tablero.
     * @return fleet flota de barcos especificada en los requerimientos.
     */
    public List<Ship> createFleet() {
        List<Ship> fleet = new ArrayList<>();
        fleet.add(new AirCraftCarrier());
        fleet.add(new Submarine());
        fleet.add(new Submarine());
        fleet.add(new Destroyer());
        fleet.add(new Destroyer());
        fleet.add(new Destroyer());
        fleet.add(new Frigate());
        fleet.add(new Frigate());
        fleet.add(new Frigate());
        fleet.add(new Frigate());
        return fleet;
    }

    /**
     * Metodo que crea y asigna en su sitio los barcos del jugador maquina.
     */
    private void placeMachinePlayerShips() {
        this.machinePlayerBoard.resetBoard(); // Asegurarse que el tablero esté limpio
        List<Ship> machineFleet = this.createFleet();
        Random random = new Random();

        for (Ship ship : machineFleet) {
            boolean placedSuccessfully = false;
            int attempts = 0;
            final int MAX_PLACEMENT_ATTEMPTS = 100; // Para evitar bucles infinitos

            while (!placedSuccessfully && attempts < MAX_PLACEMENT_ATTEMPTS) {
                int row = random.nextInt(this.machinePlayerBoard.getSize());
                int col = random.nextInt(this.machinePlayerBoard.getSize());
                Orientation orientation = random.nextBoolean() ? Orientation.HORIZONTAL : Orientation.VERTICAL;

                ship.setOrientation(orientation);

                try {
                    // Usamos una versión simplificada de placeShip que no necesita
                    // la lógica compleja de movimiento.
                    this.machinePlayerBoard.placeShip(ship, new Coordinate(col, row));
                    placedSuccessfully = true; // Si no lanza excepción, se colocó bien.
                } catch (OutOfBoundsException | OverlapException e) {
                    // Si falla, simplemente lo intentamos de nuevo en otra posición.
                    placedSuccessfully = false;
                }
                attempts++;
            }

            if (!placedSuccessfully) {
                System.err.println("Error crítico: No se pudo colocar el barco de la máquina: " + ship.getShipType());
                // En un juego real, aquí podríamos reiniciar el proceso o lanzar una excepción.
            }
        }
        System.out.println("--- Flota de la Máquina Colocada ---");
        System.out.println(this.machinePlayerBoard.toString());
        System.out.println("----------------------------------");
    }

}
