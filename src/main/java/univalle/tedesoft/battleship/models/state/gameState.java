package univalle.tedesoft.battleship.models.state;

import univalle.tedesoft.battleship.exceptions.invalidShipPlacementException;
import univalle.tedesoft.battleship.exceptions.outOfBoundsException;
import univalle.tedesoft.battleship.exceptions.overlapException;
import univalle.tedesoft.battleship.models.board;
import univalle.tedesoft.battleship.models.coordinate;
import univalle.tedesoft.battleship.models.enums.*;
import univalle.tedesoft.battleship.models.players.machinePlayer;
import univalle.tedesoft.battleship.models.players.player;
import univalle.tedesoft.battleship.models.ships.*;
import univalle.tedesoft.battleship.models.shotOutcome;
import univalle.tedesoft.battleship.models.ships.shipFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

// Imports para el patrón Memento


/**
 * Clase que representa la instancia del juego.
 * @author David Esteban Valencia
 * @author Santiago David Guerrero
 * @author Juan Pablo Escamilla
 */
public class gameState implements iGameState {
    /**Tableros de juego*/
    private board humanPlayerBoard;
    private board machinePlayerBoard;
    private board machinePlayerTerritoryBoard;
    /**Jugadores*/
    private player humanPlayer;
    private player machinePlayer;
    private player currentPlayer;
    /**Fase actual del juego*/
    private gamePhase currentPhase;
    /**Cantidad de Barcos que el humano tiene a su disposicion para colocar en la tabla*/
    private List<shipType> pendingShipsToPlaceForHuman;
    
    /**Caretaker para gestionar los mementos del juego*/
    private gameCaretaker gameCaretaker;

    /** Constructor de la Clase*/
    public gameState() {
        //Tableros de juego necesarios.
        this.humanPlayerBoard = new board();
        this.machinePlayerBoard = new board();
        this.machinePlayerTerritoryBoard = new board();
        //Fase inicial del juego.
        this.currentPhase = gamePhase.INITIAL;
        this.pendingShipsToPlaceForHuman = new ArrayList<>();
        //Inicializar el caretaker para el patrón Memento
        this.gameCaretaker = new gameCaretaker();
    }
    /**
     * Inicia una nueva partida.
     * Prepara los tableros para el jugador humano y la máquina.
     * El jugador humano deberá colocar sus barcos. La máquina los colocará automáticamente.
     * @param humanPlayer jugador humano.
     */
    @Override
    public void startNewGame(player humanPlayer) {
        //Jugadores.
        this.humanPlayer = humanPlayer;
        this.machinePlayer = new machinePlayer();
        this.currentPlayer = this.humanPlayer;
        //Se inicializa las tablas.
        this.humanPlayerBoard.resetBoard();
        this.machinePlayerBoard.resetBoard();
        this.machinePlayerTerritoryBoard.resetBoard();
        //El juego empieza en su fase inicial.
        this.currentPhase = gamePhase.PLACEMENT;
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
     * @throws invalidShipPlacementException si la colocación es inválida por superposición,
     *         salirse del tablero o tipo de barco ya colocado.
     */
    @Override
    public void placeHumanPlayerShip(shipType shipType, int row, int col, orientation orientation) throws invalidShipPlacementException, overlapException, outOfBoundsException {

        // 1. Validar que el tipo de barco está pendiente de ser colocado.
        if (!this.pendingShipsToPlaceForHuman.contains(shipType)) {
            throw new invalidShipPlacementException("Ya has colocado todos los barcos de tipo: " + shipType);
        }

        // 2. Crear los objetos de dominio necesarios.
        //Ship newShip = createShipFromType(shipType);
        ship newShip = shipFactory.createShip(shipType);
        newShip.setOrientation(orientation);
        coordinate coordinate = new coordinate(col, row); // Recordar que Coordinate(x, y) -> (col, row)

        // 3. Delegar la colocación al tablero.
        if (this.humanPlayerBoard.placeShip(newShip, coordinate)) {
            // 4. Si la colocación fue exitosa, remover el tipo de barco de la lista de pendientes.
            this.pendingShipsToPlaceForHuman.remove(shipType);
        } else {
            // Esta línea es teóricamente inalcanzable si placeShip lanza excepciones, pero es una buena práctica.
            throw new invalidShipPlacementException("No fue posible agregar esta embarcacion!!");
        }
    };

    // Candidato a eliminar.
    /**
     * Metodo de fábrica privado para crear una instancia de Ship a partir de su tipo.
     * Esto centraliza la lógica de creación de barcos.
     * @param type El enum ShipType del barco a crear.
     * @return una nueva instancia del barco correspondiente.
     */
    private ship createShipFromType(shipType type) {
        switch (type) {
            case AIR_CRAFT_CARRIER:
                return new airCraftCarrier();
            case SUBMARINE:
                return new submarine();
            case DESTROYER:
                return new destroyer();
            case FRIGATE:
                return new frigate();
            default:
                // Esto no debería ocurrir si el enum está completo.
                throw new IllegalArgumentException("Tipo de barco desconocido: " + type);
        }
    }


    /**
     * Este metodo crea la lista de TIPOS de barcos que cada jugador debe poseer.
     * @return una lista de ShipType con la flota completa.
     */
    public List<shipType> createFleetShipTypes() {
        List<shipType> fleetTypes = new ArrayList<>();
        fleetTypes.add(shipType.AIR_CRAFT_CARRIER); // 1
        fleetTypes.add(shipType.SUBMARINE);        // 2
        fleetTypes.add(shipType.SUBMARINE);
        fleetTypes.add(shipType.DESTROYER);        // 3
        fleetTypes.add(shipType.DESTROYER);
        fleetTypes.add(shipType.DESTROYER);
        fleetTypes.add(shipType.FRIGATE);          // 4
        fleetTypes.add(shipType.FRIGATE);
        fleetTypes.add(shipType.FRIGATE);
        fleetTypes.add(shipType.FRIGATE);
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
        this.currentPhase = gamePhase.FIRING;
    }

    /**
     * Procesa un disparo realizado por el jugador humano en el tablero de la máquina.
     * @param row Fila del disparo.
     * @param col Columna del disparo.
     * @return Un objeto ShotResult que indica el resultado del disparo.
     * @throws outOfBoundsException si el disparo es fuera del tablero.
     */
    @Override
    public shotOutcome handleHumanPlayerShot(int row, int col) throws outOfBoundsException, overlapException {
        coordinate coordinate = new coordinate(col, row);
        try {
            shotOutcome outcome = this.machinePlayerBoard.receiveShot(coordinate);
            this.machinePlayerTerritoryBoard.setCellState(row, col, this.machinePlayerBoard.getCellState(row, col));
            return outcome;
        } catch (overlapException e) {
            // Relanzar la excepción para que el controlador la maneje.
            throw e;
        }
    }

    /**
     * Ejecuta el turno de la máquina. La máquina elige una casilla para disparar
     * en el tablero del jugador humano.
     * @return Un objeto ShotOutcome que indica las coordenadas del disparo y su resultado.
     */
    public shotOutcome handleMachinePlayerTurn() {
        Random random = new Random();
        coordinate shotCoordinate;
        int maxAttempts = 100; // Evita bucles infinitos

        // Bucle para encontrar una celda válida que no haya sido disparada
        do {
            int row = random.nextInt(this.humanPlayerBoard.getSize());
            int col = random.nextInt(this.humanPlayerBoard.getSize());
            shotCoordinate = new coordinate(col, row);
            maxAttempts--;
        } while (isCellAlreadyShotByMachine(shotCoordinate) && maxAttempts > 0);

        // Si después de 100 intentos no se encontró una celda (muy improbable),
        // se devuelve un resultado que el controlador pueda interpretar.
        if (isCellAlreadyShotByMachine(shotCoordinate)) {
            return new shotOutcome(shotCoordinate, shotResult.ALREADY_HIT);
        }

        try {
            // La IA no debe lanzar la excepción, sino obtener un resultado simple.
            // Por eso no llamamos a receiveShot directamente sino que manejamos el caso internamente.
            return this.humanPlayerBoard.receiveShot(shotCoordinate);

        } catch(outOfBoundsException | overlapException e) {
            System.err.println("Error inesperado en el turno de la IA: " + e.getMessage());
            return new shotOutcome(shotCoordinate, shotResult.WATER);
        }
    }

    /**
     * Verifica si una celda en el tablero del jugador humano ya ha sido objetivo de un disparo.
     * @param coordinate La coordenada a verificar.
     * @return true si la celda ya fue disparada, false en caso contrario.
     */
    private boolean isCellAlreadyShotByMachine(coordinate coordinate) {
        try {
            cellState state = this.humanPlayerBoard.getCellState(coordinate.getY(), coordinate.getX());
            return state == cellState.HIT_SHIP || state == cellState.SUNK_SHIP_PART || state == cellState.SHOT_LOST_IN_WATER;
        } catch (outOfBoundsException e) {
            return true; // Considerar fuera de límites como "ya disparado" para evitarlo.
        }
    }

    /**
     * Obtiene el tablero de posición del jugador humano.
     * Utilizado por la vista para mostrar los barcos del jugador y los disparos de la máquina.
     * @return El objeto Board del jugador humano.
     */
    @Override
    public board getHumanPlayerPositionBoard() {
        return this.humanPlayerBoard;
    }

    /**
     * Obtiene el tablero principal, que representa la vista del jugador humano
     * sobre el territorio de la máquina. Muestra los resultados de los disparos del humano.
     * @return El objeto Board del territorio de la máquina (vista del jugador).
     */
    @Override
    public board getMachinePlayerTerritoryBoard() {
        return this.machinePlayerTerritoryBoard;
    }

    /**
     * Obtiene el tablero de posición real de la máquina.
     * Este método es para la HU-3 (visualización del tablero del oponente por el profesor).
     * No debe ser accesible para la lógica normal del juego del jugador.
     * @return El objeto Board con la disposición real de los barcos de la máquina.
     */
    @Override
    public board getMachinePlayerActualPositionBoard() {
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
            this.currentPhase = gamePhase.GAME_OVER;
        }
        return isGameOver;
    }

    /**
     * Obtiene el ganador del juego.
     * @return El Player del ganador, o null si el juego no ha terminado.
     */
    @Override
    public player getWinner() {
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
    public player getCurrentTurnPlayer() {
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
    public List<shipType> getPendingShipsToPlace() {
        return new ArrayList<>(this.pendingShipsToPlaceForHuman);
    }
    
    /**
     * Obtiene la fase actual del juego
     * @return La fase actual del juego
     */
    public gamePhase getCurrentPhase() {
        return this.currentPhase;
    }

    // Métodos no implementados (guardar/cargar, contadores, etc.)
    /**
     * Crea un memento con el estado actual del juego
     * @return El memento creado
     */
    public gameMemento createMemento() {
        String nickname = (humanPlayer != null) ? humanPlayer.getName() : "Unknown";
        int humanSunkShips = countSunkShips(humanPlayerBoard);
        int computerSunkShips = countSunkShips(machinePlayerBoard);
        
        return new gameMemento(nickname, humanSunkShips, computerSunkShips, currentPhase);
    }
    
    /**
     * Restaura el estado del juego desde un memento
     * @param memento El memento a restaurar
     */
    public void restoreFromMemento(gameMemento memento) {
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
        List<shipType> allShipTypes = createFleetShipTypes();
        
        // Obtener los tipos de barcos ya colocados en el tablero
        List<shipType> placedShipTypes = new ArrayList<>();
        for (ship ship : humanPlayerBoard.getShips()) {
            placedShipTypes.add(ship.getShipType());
        }
        
        // Limpiar la lista de pendientes y recalcular
        pendingShipsToPlaceForHuman.clear();
        
        // Para cada tipo de barco en la flota completa
        for (shipType shipType : allShipTypes) {
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
    private int countSunkShips(board board) {
        int sunkShips = 0;
        List<ship> ships = board.getShips();
        for (ship ship : ships) {
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
    public List<ship> createFleet() {
        List<ship> fleet = new ArrayList<>();
        fleet.add(new airCraftCarrier());
        fleet.add(new submarine());
        fleet.add(new submarine());
        fleet.add(new destroyer());
        fleet.add(new destroyer());
        fleet.add(new destroyer());
        fleet.add(new frigate());
        fleet.add(new frigate());
        fleet.add(new frigate());
        fleet.add(new frigate());
        return fleet;
    }

    /**
     * Metodo que crea y asigna en su sitio los barcos del jugador maquina.
     */
    private void placeMachinePlayerShips() {
        this.machinePlayerBoard.resetBoard(); // Asegurarse que el tablero esté limpio
        List<ship> machineFleet = this.createFleet();
        Random random = new Random();

        for (ship ship : machineFleet) {
            boolean placedSuccessfully = false;
            int attempts = 0;
            final int MAX_PLACEMENT_ATTEMPTS = 100; // Para evitar bucles infinitos

            while (!placedSuccessfully && attempts < MAX_PLACEMENT_ATTEMPTS) {
                int row = random.nextInt(this.machinePlayerBoard.getSize());
                int col = random.nextInt(this.machinePlayerBoard.getSize());
                orientation orientation = random.nextBoolean() ? univalle.tedesoft.battleship.models.enums.orientation.HORIZONTAL : univalle.tedesoft.battleship.models.enums.orientation.VERTICAL;

                ship.setOrientation(orientation);

                try {
                    this.machinePlayerBoard.placeShip(ship, new coordinate(col, row));
                    placedSuccessfully = true; // Si no lanza excepción, se colocó bien.
                } catch (outOfBoundsException | overlapException e) {
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