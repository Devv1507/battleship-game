package univalle.tedesoft.battleship.models.State;

import univalle.tedesoft.battleship.exceptions.InvalidShipPlacementException;
import univalle.tedesoft.battleship.exceptions.OutOfBoundsException;
import univalle.tedesoft.battleship.exceptions.OverlapException;
import univalle.tedesoft.battleship.models.Board;
import univalle.tedesoft.battleship.models.Coordinate;
import univalle.tedesoft.battleship.models.Enums.*;
import univalle.tedesoft.battleship.models.Players.HumanPlayer;
import univalle.tedesoft.battleship.models.Players.MachinePlayer;
import univalle.tedesoft.battleship.models.Players.Player;
import univalle.tedesoft.battleship.models.Ships.*;
import univalle.tedesoft.battleship.models.ShotOutcome;


import java.util.ArrayList;
import java.util.List;
import java.util.Random;

// Imports para el patrón Memento
import univalle.tedesoft.battleship.models.State.GameCaretaker;
import univalle.tedesoft.battleship.models.State.GameMemento;

/**
 * Clase que representa la instancia del juego.
 * @author David Esteban Valencia
 * @author Santiago David Guerrero
 * @author Juan Pablo Escamilla
 */
public class GameState implements IGameState {
    /**Tableros de juego*/
    private Board humanPlayerBoard;
    private Board machinePlayerBoard;
    private Board machinePlayerTerritoryBoard;
    /**Jugadores*/
    private Player humanPlayer;
    private Player machinePlayer;
    private Player currentPlayer;
    /**Fase actual del juego*/
    private GamePhase currentPhase;
    /**Cantidad de Barcos que el humano tiene a su disposicion para colocar en la tabla*/
    private List<ShipType> pendingShipsToPlaceForHuman;
    
    /**Caretaker para gestionar los mementos del juego*/
    private GameCaretaker gameCaretaker;

    /** Constructor de la Clase*/
    public GameState() {
        //Tableros de juego necesarios.
        this.humanPlayerBoard = new Board();
        this.machinePlayerBoard = new Board();
        this.machinePlayerTerritoryBoard = new Board();
        //Fase inicial del juego.
        this.currentPhase = GamePhase.INITIAL;
        this.pendingShipsToPlaceForHuman = new ArrayList<>();
        //Inicializar el caretaker para el patrón Memento
        this.gameCaretaker = new GameCaretaker();
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
        this.currentPlayer = this.humanPlayer;
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
            return;
        }
        this.placeMachinePlayerShips();
        this.currentPhase = GamePhase.FIRING;
    }

    /**
     * Procesa un disparo realizado por el jugador humano en el tablero de la máquina.
     * @param row Fila del disparo.
     * @param col Columna del disparo.
     * @return Un objeto ShotResult que indica el resultado del disparo.
     * @throws OutOfBoundsException si el disparo es fuera del tablero.
     */
    @Override
    public ShotOutcome handleHumanPlayerShot(int row, int col) throws OutOfBoundsException, OverlapException {
        Coordinate coordinate = new Coordinate(col, row);
        try {
            ShotOutcome outcome = this.machinePlayerBoard.receiveShot(coordinate);
            this.machinePlayerTerritoryBoard.setCellState(row, col, this.machinePlayerBoard.getCellState(row, col));
            return outcome;
        } catch (OverlapException e) {
            // Relanzar la excepción para que el controlador la maneje.
            throw e;
        }
    }

    /**
     * Ejecuta el turno de la máquina. La máquina elige una casilla para disparar
     * en el tablero del jugador humano.
     * @return Un objeto ShotOutcome que indica las coordenadas del disparo y su resultado.
     */
    public ShotOutcome handleMachinePlayerTurn() {
        Random random = new Random();
        Coordinate shotCoordinate;
        int maxAttempts = 100; // Evita bucles infinitos

        // Bucle para encontrar una celda válida que no haya sido disparada
        do {
            int row = random.nextInt(this.humanPlayerBoard.getSize());
            int col = random.nextInt(this.humanPlayerBoard.getSize());
            shotCoordinate = new Coordinate(col, row);
            maxAttempts--;
        } while (isCellAlreadyShotByMachine(shotCoordinate) && maxAttempts > 0);

        // Si después de 100 intentos no se encontró una celda (muy improbable),
        // se devuelve un resultado que el controlador pueda interpretar.
        if (isCellAlreadyShotByMachine(shotCoordinate)) {
            return new ShotOutcome(shotCoordinate, ShotResult.ALREADY_HIT);
        }

        try {
            // La IA no debe lanzar la excepción, sino obtener un resultado simple.
            // Por eso no llamamos a receiveShot directamente sino que manejamos el caso internamente.
            return this.humanPlayerBoard.receiveShot(shotCoordinate);

        } catch(OutOfBoundsException | OverlapException e) {
            System.err.println("Error inesperado en el turno de la IA: " + e.getMessage());
            return new ShotOutcome(shotCoordinate, ShotResult.WATER);
        }
    }

    /**
     * Verifica si una celda en el tablero del jugador humano ya ha sido objetivo de un disparo.
     * @param coordinate La coordenada a verificar.
     * @return true si la celda ya fue disparada, false en caso contrario.
     */
    private boolean isCellAlreadyShotByMachine(Coordinate coordinate) {
        try {
            CellState state = this.humanPlayerBoard.getCellState(coordinate.getY(), coordinate.getX());
            return state == CellState.HIT_SHIP || state == CellState.SUNK_SHIP_PART || state == CellState.SHOT_LOST_IN_WATER;
        } catch (OutOfBoundsException e) {
            return true; // Considerar fuera de límites como "ya disparado" para evitarlo.
        }
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
        boolean isGameOver = this.humanPlayerBoard.areAllShipsSunk() || this.machinePlayerBoard.areAllShipsSunk();
        if (isGameOver) {
            this.currentPhase = GamePhase.GAME_OVER;
        }
        return isGameOver;
    }

    /**
     * Obtiene el ganador del juego.
     * @return El Player del ganador, o null si el juego no ha terminado.
     */
    @Override
    public Player getWinner() {
        if (!isGameOver()) {
            return null;
        }
        if (this.machinePlayerBoard.areAllShipsSunk()) {
            return this.humanPlayer;
        }
        if (this.humanPlayerBoard.areAllShipsSunk()) {
            return this.machinePlayer;
        }
        return null; // En caso de empate o estado inesperado.
    }
    /**
     * Obtiene el jugador cuyo turno es actualmente.
     * @return El Player del jugador actual.
     */
    @Override
    public Player getCurrentTurnPlayer() {
        return this.currentPlayer;
    }

    /**
     * Cambia el turno al siguiente jugador.
     */
    @Override
    public void switchTurn() {
        if (this.currentPlayer == this.humanPlayer) {
            this.currentPlayer = this.machinePlayer;
        } else {
            this.currentPlayer = this.humanPlayer;
        }
    }

    @Override
    public List<ShipType> getPendingShipsToPlace() {
        return new ArrayList<>(this.pendingShipsToPlaceForHuman);
    }
    
    /**
     * Obtiene la fase actual del juego
     * @return La fase actual del juego
     */
    public GamePhase getCurrentPhase() {
        return this.currentPhase;
    }

    // Métodos no implementados (guardar/cargar, contadores, etc.)
    /**
     * Crea un memento con el estado actual del juego
     * @return El memento creado
     */
    public GameMemento createMemento() {
        String nickname = (humanPlayer != null) ? humanPlayer.getName() : "Unknown";
        int humanSunkShips = countSunkShips(humanPlayerBoard);
        int computerSunkShips = countSunkShips(machinePlayerBoard);
        
        return new GameMemento(nickname, humanSunkShips, computerSunkShips, currentPhase);
    }
    
    /**
     * Restaura el estado del juego desde un memento
     * @param memento El memento a restaurar
     */
    public void restoreFromMemento(GameMemento memento) {
        if (memento != null) {
            // Restaurar el nickname del jugador
            if (humanPlayer != null) {
                humanPlayer.setName(memento.getHumanPlayerNickname());
            }
            
            // Restaurar la fase del juego
            this.currentPhase = memento.getCurrentPhase();
            
            System.out.println("Estado del juego restaurado desde memento: " + memento);
        }
    }
    
    /**
     * Recalcula la lista de barcos pendientes basándose en los barcos ya colocados
     * en el tablero del jugador humano.
     */
    private void recalculatePendingShips() {
        // Crear una lista completa de todos los barcos que deberían estar en el tablero
        List<ShipType> allShipTypes = createFleetShipTypes();
        
        // Obtener los tipos de barcos ya colocados en el tablero
        List<ShipType> placedShipTypes = new ArrayList<>();
        for (Ship ship : humanPlayerBoard.getShips()) {
            placedShipTypes.add(ship.getShipType());
        }
        
        // Limpiar la lista de pendientes y recalcular
        pendingShipsToPlaceForHuman.clear();
        
        // Para cada tipo de barco en la flota completa
        for (ShipType shipType : allShipTypes) {
            // Si no está en la lista de barcos colocados, agregarlo a pendientes
            if (!placedShipTypes.isEmpty() && placedShipTypes.contains(shipType)) {
                placedShipTypes.remove(shipType); // Remover una ocurrencia
            } else {
                pendingShipsToPlaceForHuman.add(shipType);
            }
        }
        
        System.out.println("Barcos pendientes recalculados: " + pendingShipsToPlaceForHuman.size() + " barcos por colocar");
    }
    
    /**
     * Cuenta los barcos hundidos en un tablero
     * @param board El tablero a revisar
     * @return El número de barcos hundidos
     */
    private int countSunkShips(Board board) {
        int sunkShips = 0;
        List<Ship> ships = board.getShips();
        for (Ship ship : ships) {
            if (ship.isSunk()) {
                sunkShips++;
            }
        }
        return sunkShips;
    }
    
    @Override
    public void saveGame() {
        // Guardar el estado completo del juego incluyendo barcos y tableros
        boolean saved = gameCaretaker.saveCompleteGame(this);
        if (saved) {
            System.out.println("Juego guardado exitosamente usando patrón Memento con serialización completa");
        } else {
            System.err.println("Error al guardar el juego");
        }
    }
    @Override
    public boolean loadGame() {
        // Cargar el estado completo del juego incluyendo barcos y tableros
        boolean loaded = gameCaretaker.loadCompleteGame(this);
        if (loaded) {
            // Recalcular los barcos pendientes basándose en los barcos ya colocados
            recalculatePendingShips();
            System.out.println("Juego cargado exitosamente con estado completo");
            return true;
        }
        return false;
    }

    @Override
    public boolean isSavedGameAvailable() {
        return gameCaretaker.isSavedGameAvailable();
    }
    @Override
    public String getHumanPlayerNickname() {
        return (humanPlayer != null) ? humanPlayer.getName() : null;
    }
    
    @Override
    public int getHumanPlayerSunkShipCount() {
        return countSunkShips(machinePlayerBoard);
    }
    
    @Override
    public int getComputerPlayerSunkShipCount() {
        return countSunkShips(humanPlayerBoard);
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

                Orientation orientation;
                // Si el barco es de tipo FRIGATE, su orientación será HORIZONTAL.
                if (ship.getShipType() == ShipType.FRIGATE) {
                    orientation = Orientation.HORIZONTAL;
                } else {
                    // Para los demás barcos, la orientación es aleatoria.
                    orientation = random.nextBoolean() ? Orientation.HORIZONTAL : Orientation.VERTICAL;
                }
                ship.setOrientation(orientation);

                try {
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
            }
        }
    }

}