package univalle.tedesoft.battleship.models.state;

import univalle.tedesoft.battleship.exceptions.InvalidShipPlacementException;
import univalle.tedesoft.battleship.exceptions.OutOfBoundsException;
import univalle.tedesoft.battleship.exceptions.OverlapException;
import univalle.tedesoft.battleship.models.board.Board;
import univalle.tedesoft.battleship.models.board.Coordinate;
import univalle.tedesoft.battleship.models.players.Player;
import univalle.tedesoft.battleship.models.enums.*;
import univalle.tedesoft.battleship.models.players.MachinePlayer;
import univalle.tedesoft.battleship.models.ships.*;
import univalle.tedesoft.battleship.models.board.ShotOutcome;
import univalle.tedesoft.battleship.models.ships.ShipFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Clase que representa la instancia del juego.
 * @author David Esteban Valencia
 * @author Santiago David Guerrero
 * @author Juan Pablo Escamilla
 */
public class GameState implements IGameState {
    /**Tableros de juego*/
    private final Board humanPlayerBoard;
    private final Board machinePlayerBoard;
    private final Board machinePlayerTerritoryBoard;
    /**Jugadores*/
    private Player humanPlayer;
    private Player machinePlayer;
    private Player currentPlayer;
    /**Fase actual del juego*/
    private GamePhase currentPhase;
    /**Cantidad de Barcos que el humano tiene a su disposicion para colocar en la tabla*/
    private final List<ShipType> pendingShipsToPlaceForHuman;

    /** Constructor de la Clase*/
    public GameState() {
        //Tableros de juego necesarios.
        this.humanPlayerBoard = new Board();
        this.machinePlayerBoard = new Board();
        this.machinePlayerTerritoryBoard = new Board();
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
        //Ship newShip = createShipFromType(shipType);
        Ship newShip = ShipFactory.createShip(shipType);
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
     * Este metodo crea la lista de TIPOS de barcos que cada jugador debe poseer.
     * @return una lista de ShipType con la flota completa.
     */
    private List<ShipType> createFleetShipTypes() {
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
        
        // IMPORTANTE: El jugador humano siempre inicia la fase de disparos
        this.currentPlayer = this.humanPlayer;
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
    @Override
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
        boolean humanShipsSunk = this.humanPlayerBoard.areAllShipsSunk();
        boolean machineShipsSunk = this.machinePlayerBoard.areAllShipsSunk();
        
        // Mensajes de depuración
        if (humanShipsSunk) {
            System.out.println("Todos los barcos del jugador humano han sido hundidos.");
        }
        if (machineShipsSunk) {
            System.out.println("Todos los barcos de la máquina han sido hundidos.");
        }
        
        boolean isGameOver = humanShipsSunk || machineShipsSunk;
        if (isGameOver) {
            this.currentPhase = GamePhase.GAME_OVER;
            System.out.println("El juego ha terminado. Fase actualizada a GAME_OVER.");
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
            System.out.println("El juego aún no ha terminado. No hay ganador.");
            return null;
        }
        
        // Si todos los barcos de la máquina están hundidos, gana el humano
        if (this.machinePlayerBoard.areAllShipsSunk()) {
            System.out.println("El ganador es: " + this.humanPlayer.getName());
            return this.humanPlayer;
        }
        
        // Si todos los barcos del humano están hundidos, gana la máquina
        if (this.humanPlayerBoard.areAllShipsSunk()) {
            System.out.println("El ganador es: " + this.machinePlayer.getName());
            return this.machinePlayer;
        }
        
        // Esto no debería ocurrir si isGameOver() es true
        System.err.println("Estado inesperado: isGameOver() es true pero no se puede determinar el ganador.");
        return null;
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

    /**
     * Obtiene la lista de barcos que el jugador aún necesita colocar.
     * Esta lista se utiliza para mostrar los barcos pendientes en la interfaz de usuario.
     * @return Una lista de ShipType que representa los barcos pendientes de colocar por el jugador humano.
     */
    @Override
    public List<ShipType> getPendingShipsToPlace() {
        return new ArrayList<>(this.pendingShipsToPlaceForHuman);
    }
    
    /**
     * Obtiene la fase actual del juego
     * @return La fase actual del juego
     */
    @Override
    public GamePhase getCurrentPhase() {
        return this.currentPhase;
    }

    // ----- Métodos de Guardado y Carga -----
    /**
     * Crea un memento con el estado actual del juego
     * @return El memento creado
     */
    @Override
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
    @Override
    public void restoreFromMemento(GameMemento memento) {
        if (memento != null) {
            // Asegurar que los jugadores estén inicializados
            ensurePlayersInitialized(memento.getHumanPlayerNickname());
            
            // Restaurar la fase del juego
            this.currentPhase = memento.getCurrentPhase();
            
            // IMPORTANTE: Establecer el turno correcto basado en la fase cargada
            restoreCurrentPlayerBasedOnPhase(this.currentPhase);
            
            System.out.println("Estado del juego restaurado desde memento: " + memento);
        }
    }

    /**
     * Asegura que los jugadores estén correctamente inicializados al cargar una partida.
     * Esto es crucial para el funcionamiento correcto del sistema de turnos.
     * 
     * @param humanPlayerNickname El nickname del jugador humano
     */
    private void ensurePlayersInitialized(String humanPlayerNickname) {
        // Inicializar jugador humano si no existe o si el nickname es diferente
        if (this.humanPlayer == null || !humanPlayerNickname.equals(this.humanPlayer.getName())) {
            this.humanPlayer = new univalle.tedesoft.battleship.models.players.HumanPlayer(humanPlayerNickname);
        }
        
        // Inicializar jugador máquina si no existe
        if (this.machinePlayer == null) {
            this.machinePlayer = new univalle.tedesoft.battleship.models.players.MachinePlayer();
        }
    }

    /**
     * Establece el turno actual de forma inteligente basándose en la fase del juego.
     * Esto es crucial para partidas cargadas donde el turno no se guarda explícitamente.
     * 
     * @param phase La fase del juego cargada
     */
    private void restoreCurrentPlayerBasedOnPhase(GamePhase phase) {
        switch (phase) {
            case INITIAL:
            case PLACEMENT:
                // En colocación, el jugador humano debe estar activo
                this.currentPlayer = this.humanPlayer;
                break;
                
            case FIRING:
                // En disparos, por regla general el jugador humano inicia
                // (Esto podría mejorarse guardando el turno actual en el futuro)
                this.currentPlayer = this.humanPlayer;
                break;
                
            case GAME_OVER:
                // En juego terminado, no importa el turno, pero lo dejamos en humano
                this.currentPlayer = this.humanPlayer;
                break;
                
            default:
                // Por seguridad, asignar al jugador humano
                this.currentPlayer = this.humanPlayer;
                break;
        }
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

    /**
     * Guarda el estado completo del juego en un archivo.
     * Delega la lógica de persistencia al GamePersistenceManager.
     * @see GamePersistenceManager
     */
    @Override
    public void saveGame() {
        // Lógica para guardar el estado del juego completo
        GamePersistenceManager.saveGame(this);
    }

    /**
     * Carga una partida guardada por el jugador humano.
     * Delega la operación de carga al GamePersistenceManager.
     * Si la carga es exitosa, el estado de la instancia actual de GameState será completamente sobrescrito
     * con los datos de la partida cargada, incluyendo el estado de los tableros, los barcos y la fase del juego.
     * @param nickname El nombre del jugador cuya partida se quiere cargar.
     * @return true si la carga fue exitosa, false en caso contrario.
     * @see GamePersistenceManager
     */
    @Override
    public boolean loadGame(String nickname) {
        return GamePersistenceManager.loadGame(this, nickname);
    }

    /**
     * Obtiene el nickname del jugador humano.
     * @return El nickname del jugador humano, o null si no está definido.
     */
    @Override
    public String getHumanPlayerNickname() {
        if (this.humanPlayer != null) {
            return this.humanPlayer.getName();
        }
        return null;
    }

    /**
     * Metodo que crea la cantidad de barcos que cada jugador debe poseer en su tablero.
     * @return fleet flota de barcos especificada en los requerimientos.
     */
    private List<Ship> createFleet() {
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
     * Metodo que crea y asigna en su sitio los barcos del jugador máquina.
     */
    private void placeMachinePlayerShips() {
        this.placeShipsRandomlyOnBoard(this.machinePlayerBoard);
    }

    /**
     * Coloca todos los barcos pendientes del jugador humano de forma aleatoria en su tablero.
     * Delega la lógica de colocación y luego actualiza el estado de los barcos pendientes.
     */
    @Override
    public void placeHumanPlayerShipsRandomly() {
        this.placeShipsRandomlyOnBoard(this.humanPlayerBoard);
        // Vaciar la lista de barcos pendientes para la UI.
        this.pendingShipsToPlaceForHuman.clear();
    }

    /**
     * Lógica centralizada y reutilizable para colocar una flota completa de barcos
     * de forma aleatoria en un tablero específico.
     * Este metodo es llamado tanto para la colocación de la máquina como para la
     * colocación aleatoria del jugador humano.
     *
     * @param board El tablero (del humano o de la máquina) en el que se colocarán los barcos.
     */
    private void placeShipsRandomlyOnBoard(Board board) {
        // Asegurarse que el tablero esté limpio antes de empezar.
        board.resetBoard();
        List<Ship> fleetToPlace = this.createFleet();
        Random random = new Random();

        for (Ship ship : fleetToPlace) {
            boolean placedSuccessfully = false;
            int attempts = 0;
            final int MAX_PLACEMENT_ATTEMPTS = 100; // Evitar bucles infinitos.

            while (!placedSuccessfully && attempts < MAX_PLACEMENT_ATTEMPTS) {
                int row = random.nextInt(board.getSize());
                int col = random.nextInt(board.getSize());

                Orientation orientation;
                if (ship.getShipType() == ShipType.FRIGATE) {
                    orientation = Orientation.HORIZONTAL;
                } else {
                    orientation = random.nextBoolean() ? Orientation.HORIZONTAL : Orientation.VERTICAL;
                }
                ship.setOrientation(orientation);

                try {
                    board.placeShip(ship, new Coordinate(col, row));
                    placedSuccessfully = true;
                } catch (OutOfBoundsException | OverlapException e) {
                    placedSuccessfully = false;
                }
                attempts++;
            }

            if (!placedSuccessfully) {
                System.err.println("Error crítico: No se pudo colocar el barco " + ship.getShipType() + " en el tablero. Reiniciando tablero.");
                board.resetBoard();
                // Salir para evitar un tablero a medio colocar.
                return;
            }
        }
    }

    /**
     * Mueve un barco ya colocado a una nueva posición en el tablero del jugador humano.
     * Si la nueva posición es inválida, el barco se restaura a su ubicación original.
     *
     * @param shipToMove El objeto Ship que se desea mover.
     * @param newRow La nueva fila de inicio para el barco.
     * @param newCol La nueva columna de inicio para el barco.
     * @throws InvalidShipPlacementException si el movimiento es inválido.
     */
    @Override
    public void moveHumanPlayerShip(Ship shipToMove, int newRow, int newCol) throws InvalidShipPlacementException, OverlapException, OutOfBoundsException {
        // Guardar la información original del barco por si el movimiento falla
        List<Coordinate> originalCoordinates = new ArrayList<>(shipToMove.getOccupiedCoordinates());

        // Eliminar el barco de su posición actual en el tablero.
        if (!this.humanPlayerBoard.removeShip(shipToMove)) {
            throw new InvalidShipPlacementException("El barco a mover no se encuentra en el tablero.");
        }
        shipToMove.getOccupiedCoordinates().clear();

        try {
            // Intentar colocar el barco en la nueva posición.
            this.humanPlayerBoard.placeShip(shipToMove, new Coordinate(newCol, newRow));
        } catch (OutOfBoundsException | OverlapException e) {
            // Si el movimiento falla, restaurar el barco a su estado original.
            // Limpiar cualquier coordenada parcial que se haya podido añadir.
            shipToMove.getOccupiedCoordinates().clear();

            // Reasignar las coordenadas originales al objeto Ship.
            for (Coordinate coord : originalCoordinates) {
                shipToMove.addCoordinates(coord);
            }

            // Volver a añadir el barco al tablero en su posición original.
            this.humanPlayerBoard.addShipDirectly(shipToMove);
            for(Coordinate coord : shipToMove.getOccupiedCoordinates()){
                // Restaurar el estado en la grilla y en la lista de barcos.
                this.humanPlayerBoard.setCellState(coord.getY(), coord.getX(), CellState.SHIP);
            }

            // Relanzar la excepción para que el controlador pueda notificar al usuario.
            throw e;
        }
    }
}