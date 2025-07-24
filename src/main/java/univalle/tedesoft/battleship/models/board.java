package univalle.tedesoft.battleship.models;

import univalle.tedesoft.battleship.exceptions.outOfBoundsException;
import univalle.tedesoft.battleship.exceptions.overlapException;
import univalle.tedesoft.battleship.models.enums.cellState;
import univalle.tedesoft.battleship.models.enums.orientation;
import univalle.tedesoft.battleship.models.enums.shotResult;
import univalle.tedesoft.battleship.models.ships.ship;

import java.util.ArrayList;
import java.util.List;

/**
 * Clase que representa un tablero de juego.
 * @author David Esteban Valencia
 * @author Santiago David Guerrero
 * @author Juan Pablo Escamilla
 */
public class board {
    /** Tamaño estandar del tablero*/
    private static final int DEFAULT_SIZE = 10; // Tamaño estándar del tablero 10x10
    /** Matriz de celdas, para guardar los estados de cada celda de forma ordenada por coordenadas*/
    private cellState[][] grid;
    /** Lista de barcos alojados en el tablero*/
    private List<ship> ships;
    /**
     * Constructor que inicializa un tablero vacío con un tamaño específico.
     */
    public board() {
        this.grid = new cellState[DEFAULT_SIZE][DEFAULT_SIZE];
        this.ships = new ArrayList<>();
        this.initializeGrid();
    }
    /**
     * Inicializa todas las celdas del tablero a EMPTY.
     */
    private void initializeGrid() {
        for (int i = 0; i < DEFAULT_SIZE; i++) {
            for (int j = 0; j < DEFAULT_SIZE; j++) {
                grid[i][j] = cellState.EMPTY;
            }
        }
    }
    /**
     * Intenta colocar un barco en el tablero, si lo logra llena las casillas en la tabla
     * y en el arreglo del jugador, tambien agrega el barco al arreglo dentro de la clase Board.
     * @param ship El barco a colocar.
     * @param startCoordinate La coordenada de inicio del barco (esquina superior-izquierda).
     * @return true si el barco fue colocado exitosamente, false en caso contrario.
     * @throws outOfBoundsException si alguna parte del barco queda fuera del tablero.
     * @throws overlapException si el barco se superpone con otro ya existente.
     */
    public boolean placeShip(ship ship, coordinate startCoordinate) throws outOfBoundsException, overlapException {
        // Validación de límites
        if (ship.getOrientation() == orientation.HORIZONTAL && startCoordinate.getX() + ship.getValueShip() > DEFAULT_SIZE) {
            throw new outOfBoundsException("Esta embarcacion no cabe horizontalmente en el tablero!");
        }
        if (ship.getOrientation() == orientation.VERTICAL && startCoordinate.getY() + ship.getValueShip() > DEFAULT_SIZE) {
            throw new outOfBoundsException("Esta embarcacion no cabe verticalmente en el tablero!");
        }

        List<coordinate> coordsToPlace = new ArrayList<>();

        // Primero, verificar todas las celdas antes de modificar nada.
        for (int i = 0; i < ship.getValueShip(); i++) {
            int current_row = startCoordinate.getY();
            int current_col = startCoordinate.getX();

            if (ship.getOrientation() == orientation.VERTICAL) {
                current_row += i;
            } else { // Si es HORIZONTAL
                current_col += i;
            }

            if (grid[current_row][current_col] != cellState.EMPTY) {
                throw new overlapException("Casilla (" + current_row + "," + current_col + ") Ocupada");
            }

            // Guardamos la coordenada que vamos a ocupar.
            coordsToPlace.add(new coordinate(current_col, current_row));
        }

        // Si todas las verificaciones pasaron, ahora sí colocamos el barco.
        for (coordinate coord : coordsToPlace) {
            // Accedemos a la grid con Y,X (fila, columna)
            grid[coord.getY()][coord.getX()] = cellState.SHIP;

            // Añadimos la coordenada al propio barco para que sepa dónde está.
            ship.addCoordinates(coord);
        }

        ships.add(ship);
        return true;
    }

    /**
     * Procesa un disparo en una coordenada específica del tablero.
     * @param targetCoordinate La coordenada donde se realiza el disparo.
     * @return Un objeto ShotOutcome con todos los detalles del resultado.
     * @throws outOfBoundsException si la coordenada está fuera del tablero.
     * @throws overlapException si se intenta disparar a una celda ya atacada.
     */
    public shotOutcome receiveShot(coordinate targetCoordinate) throws outOfBoundsException, overlapException {
        if (!isValidCoordinate(targetCoordinate.getY(), targetCoordinate.getX())) {
            throw new outOfBoundsException("Coordenada fuera de los límites del tablero.");
        }

        int row = targetCoordinate.getY();
        int col = targetCoordinate.getX();
        cellState currentState = this.grid[row][col];

        switch (currentState) {
            case EMPTY:
                this.grid[row][col] = cellState.SHOT_LOST_IN_WATER;
                return new shotOutcome(targetCoordinate, shotResult.WATER);
            case SHIP:
                ship hitShip = this.getShipAt(row, col);
                if (hitShip != null) {
                    hitShip.registerHit();
                    if (hitShip.isSunk()) {
                        for (coordinate coord : hitShip.getOccupiedCoordinates()) {
                            this.grid[coord.getY()][coord.getX()] = cellState.SUNK_SHIP_PART;
                        }
                        return new shotOutcome(targetCoordinate, shotResult.SUNKEN, hitShip);
                    } else {
                        this.grid[row][col] = cellState.HIT_SHIP;
                        return new shotOutcome(targetCoordinate, shotResult.TOUCHED);
                    }
                }
                this.grid[row][col] = cellState.HIT_SHIP;
                return new shotOutcome(targetCoordinate, shotResult.TOUCHED);

            case HIT_SHIP:
            case SHOT_LOST_IN_WATER:
            case SUNK_SHIP_PART:
                throw new overlapException("Ya has disparado en la casilla " + targetCoordinate.toAlgebraicNotation() + ".");

            default:
                throw new IllegalStateException("Estado de celda desconocido: " + currentState);
        }
    }

    /**
     * Obtiene el estado de una celda específica.
     * @param row La fila de la celda.
     * @param col La columna de la celda.
     * @return El estado de la celda.
     * @throws outOfBoundsException si la coordenada esta fuera del tablero.
     */
    public cellState getCellState(int row, int col) throws outOfBoundsException {
        if (row < 0 || row >= DEFAULT_SIZE || col < 0 || col >= DEFAULT_SIZE) {
            throw new outOfBoundsException("Coordenada (" + row + "," + col + ") está fuera del tablero.");
        }
        return grid[row][col];
    }
    /**
     * Establece el estado de una celda especifica,
     * generalmente los estados se manejan por placeShip y receiveShot.
     * @param row La fila de la celda.
     * @param col La columna de la celda.
     * @param state El nuevo estado para la celda.
     * @throws outOfBoundsException si la coordenada está fuera del tablero.
     */
    public void setCellState(int row, int col, cellState state) throws outOfBoundsException {
        if (!isValidCoordinate(row, col)) {
            throw new outOfBoundsException("Coordenada (" + row + "," + col + ") está fuera del tablero.");
        }
        this.grid[row][col] = state;
    }
    /**
     * Verifica si todos los barcos en este tablero han sido hundidos.
     * @return true si todos los barcos están hundidos, false en caso contrario.(REVISAR ESTO)
     */
    public boolean areAllShipsSunk() {
        if (ships.isEmpty()) {
            System.out.println("No hay barcos en el tablero.");
            return false;
        }
        
        int totalShips = ships.size();
        int sunkShips = 0;
        
        for (ship ship : ships) {
            if (ship.isSunk()) {
                sunkShips++;
            }
        }
        
        System.out.println("Barcos hundidos: " + sunkShips + " de " + totalShips);
        
        return sunkShips == totalShips;
    }
    /**
     * Devuelve la lista de barcos colocados en este tablero.
     * @return Una lista de los barcos.
     */
    public List<ship> getShips() {
        return new ArrayList<>(ships); // Devuelve una copia para evitar modificaciones externas
    }
    
    /**
     * Agrega un barco directamente al tablero (para cargar desde archivo).
     * @param ship El barco a agregar
     */
    public void addShipDirectly(ship ship) {
        this.ships.add(ship);
    }


    /**
     * Encuentra y devuelve el barco que ocupa una coordenada específica.
     * Esencial para saber qué barco colorear en la vista.
     *
     * @param row La fila a verificar.
     * @param col La columna a verificar.
     * @return El objeto Ship en esa coordenada, o null si no hay ninguno.
     */
    public ship getShipAt(int row, int col) {
        // Itera sobre todos los barcos en el tablero
        for (ship ship : this.ships) {
            // Para cada barco, itera sobre sus coordenadas ocupadas
            for (coordinate coord : ship.getOccupiedCoordinates()) {
                // Si la coordenada del barco coincide con la que buscamos...
                if (coord.getY() == row && coord.getX() == col) {
                    return ship;
                }
            }
        }
        // Si recorrimos todos los barcos y ninguno ocupa esa celda, devolvemos null.
        return null;
    }


    /**
     * Obtiene el tamaño del tablero.
     * @return El tamaño (número de filas/columnas).
     */
    public int getSize() {
        return DEFAULT_SIZE;
    }

    /**
     * Verifica si una coordenada dada (fila, columna) es válida dentro del tablero.
     * @param row Fila.
     * @param col Columna.
     * @return true si la coordenada es válida, false en caso contrario.
     */
    public boolean isValidCoordinate(int row, int col) {
        return row >= 0 && row < getSize() && col >= 0 && col < getSize();
    }

    /**
     * Verifica si una coordenada dada (objeto Coordinate) es válida dentro del tablero.
     * @param coordinate La coordenada a verificar.
     * @return true si la coordenada es válida, false en caso contrario.
     */
    public boolean isValidCoordinate(coordinate coordinate) {
        if (coordinate == null) {
            return false;
        }
        return isValidCoordinate(coordinate.getX(), coordinate.getY());
    }

    /**
     * Reinicia el tablero a su estado inicial (todas las celdas vacías, sin barcos).
     */
    public void resetBoard() {
        initializeGrid();
        this.ships.clear();
    }
}
