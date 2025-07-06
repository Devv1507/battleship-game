package univalle.tedesoft.battleship.models;

import univalle.tedesoft.battleship.Exceptions.OutOfBoundsException;
import univalle.tedesoft.battleship.Exceptions.OverlapException;
import univalle.tedesoft.battleship.models.Enums.CellState;
import univalle.tedesoft.battleship.models.Enums.Orientation;
import univalle.tedesoft.battleship.models.Enums.ShotResult;
import univalle.tedesoft.battleship.models.Ships.Ship;

import java.util.ArrayList;
import java.util.List;

/**
 * Clase que representa un tablero de juego.
 * @author David Esteban Valencia
 * @author Santiago David Guerrero
 * @author Juan Pablo Escamilla
 */
public class Board {
    /** Tamaño estandar del tablero*/
    private static final int DEFAULT_SIZE = 10; // Tamaño estándar del tablero 10x10
    /** Matriz de celdas, para guardar los estados de cada celda de forma ordenada por coordenadas*/
    private CellState[][] grid;
    /** Lista de barcos alojados en el tablero*/
    private List<Ship> ships;
    /**
     * Constructor que inicializa un tablero vacío con un tamaño específico.
     */
    public Board() {
        this.grid = new CellState[DEFAULT_SIZE][DEFAULT_SIZE];
        this.ships = new ArrayList<>();
        this.initializeGrid();
    }
    /**
     * Inicializa todas las celdas del tablero a EMPTY.
     */
    private void initializeGrid() {
        for (int i = 0; i < DEFAULT_SIZE; i++) {
            for (int j = 0; j < DEFAULT_SIZE; j++) {
                grid[i][j] = CellState.EMPTY;
            }
        }
    }
    /**
     * Intenta colocar un barco en el tablero, si lo logra llena las casillas en la tabla
     * y en el arreglo del jugador, tambien agrega el barco al arreglo dentro de la clase Board.
     * @param ship El barco a colocar.
     * @param startCoordinate La coordenada de inicio del barco (esquina superior-izquierda).
     * @return true si el barco fue colocado exitosamente, false en caso contrario.
     * @throws OutOfBoundsException si alguna parte del barco queda fuera del tablero.
     * @throws OverlapException si el barco se superpone con otro ya existente.
     */
    public boolean placeShip(Ship ship, Coordinate startCoordinate) throws OutOfBoundsException, OverlapException {
        // Validación de límites
        if (ship.getOrientation() == Orientation.HORIZONTAL && startCoordinate.getX() + ship.getValueShip() > DEFAULT_SIZE) {
            throw new OutOfBoundsException("Esta embarcacion no cabe horizontalmente en el tablero!");
        }
        if (ship.getOrientation() == Orientation.VERTICAL && startCoordinate.getY() + ship.getValueShip() > DEFAULT_SIZE) {
            throw new OutOfBoundsException("Esta embarcacion no cabe verticalmente en el tablero!");
        }

        List<Coordinate> coordsToPlace = new ArrayList<>();

        // Primero, verificar todas las celdas antes de modificar nada.
        for (int i = 0; i < ship.getValueShip(); i++) {
            int current_row = startCoordinate.getY();
            int current_col = startCoordinate.getX();

            if (ship.getOrientation() == Orientation.VERTICAL) {
                current_row += i;
            } else { // Si es HORIZONTAL
                current_col += i;
            }

            if (grid[current_row][current_col] != CellState.EMPTY) {
                throw new OverlapException("Casilla (" + current_row + "," + current_col + ") Ocupada");
            }

            // Guardamos la coordenada que vamos a ocupar.
            coordsToPlace.add(new Coordinate(current_col, current_row));
        }

        // Si todas las verificaciones pasaron, ahora sí colocamos el barco.
        for (Coordinate coord : coordsToPlace) {
            // Accedemos a la grid con Y,X (fila, columna)
            grid[coord.getY()][coord.getX()] = CellState.SHIP;

            // Añadimos la coordenada al propio barco para que sepa dónde está.
            ship.addCoordinates(coord);
        }

        ships.add(ship);
        return true;
    }

    /**
     * Procesa un disparo en una coordenada específica del tablero.
     * Actualiza el estado de la celda y del barco (si lo hay).
     * @param targetCoordinate La coordenada donde se realiza el disparo.
     * @return El resultado del disparo (WATER, TOUCHED, SUNKEN, ALREADY_HIT).
     * @throws OutOfBoundsException si la coordenada está fuera del tablero.
     */
    public ShotResult receiveShot(Coordinate targetCoordinate) throws OutOfBoundsException {
        if (!isValidCoordinate(targetCoordinate)) {
            throw new OutOfBoundsException("El coordinate debe ser entre 0 y 10");
        }
        if (grid[targetCoordinate.getY()][targetCoordinate.getX()] == CellState.EMPTY) {
            grid[targetCoordinate.getY()][targetCoordinate.getX()] = CellState.SHOT_LOST_IN_WATER;
            return ShotResult.WATER;
        } else if (grid[targetCoordinate.getY()][targetCoordinate.getX()] == CellState.SHIP) {
            grid[targetCoordinate.getY()][targetCoordinate.getX()] = CellState.HIT_SHIP;
            return ShotResult.TOUCHED;
        }
        /*
        else if (grid[targetCoordinate.getY()][targetCoordinate.getX()] == CellState.SHOT_LOST_IN_WATER) {
            return ShotResult.ALREADY_HIT;
        } else if (grid[targetCoordinate.getY()][targetCoordinate.getX()] == CellState.HIT_SHIP) {
            return ShotResult.ALREADY_HIT;
        }
        */
        return ShotResult.ALREADY_HIT;
    }
    /**
     * Obtiene el estado de una celda específica.
     * @param row La fila de la celda.
     * @param col La columna de la celda.
     * @return El estado de la celda.
     * @throws OutOfBoundsException si la coordenada esta fuera del tablero.
     */
    public CellState getCellState(int row, int col) throws OutOfBoundsException {
        if (row < 0 || row >= DEFAULT_SIZE || col < 0 || col >= DEFAULT_SIZE) {
            throw new OutOfBoundsException("Coordenada (" + row + "," + col + ") está fuera del tablero.");
        }
        return grid[row][col];
    }
    /**
     * Establece el estado de una celda especifica,
     * generalmente los estados se manejan por placeShip y receiveShot.
     * @param row La fila de la celda.
     * @param col La columna de la celda.
     * @param state El nuevo estado para la celda.
     * @throws OutOfBoundsException si la coordenada está fuera del tablero.
     */
    public void setCellState(int row, int col, CellState state) throws OutOfBoundsException {
        if (!isValidCoordinate(row, col)) {
            throw new OutOfBoundsException("Coordenada (" + row + "," + col + ") está fuera del tablero.");
        }
        this.grid[row][col] = state;
    }
    /**
     * Verifica si todos los barcos en este tablero han sido hundidos.
     * @return true si todos los barcos están hundidos, false en caso contrario.(REVISAR ESTO)
     */
    public boolean areAllShipsSunk() {
        if (ships.isEmpty()) {
            return false;
        }
        for (Ship ship : ships) {
            if (!ship.isSunk()) {
                return false;
            }
        }
        return true;
    }
    /**
     * Devuelve la lista de barcos colocados en este tablero.
     * @return Una lista de los barcos.
     */
    public List<Ship> getShips() {
        return new ArrayList<>(ships); // Devuelve una copia para evitar modificaciones externas
    }


    /**
     * Encuentra y devuelve el barco que ocupa una coordenada específica.
     * Esencial para saber qué barco colorear en la vista.
     *
     * @param row La fila a verificar.
     * @param col La columna a verificar.
     * @return El objeto Ship en esa coordenada, o null si no hay ninguno.
     */
    public Ship getShipAt(int row, int col) {
        // Itera sobre todos los barcos en el tablero
        for (Ship ship : this.ships) {
            // Para cada barco, itera sobre sus coordenadas ocupadas
            for (Coordinate coord : ship.getOccupiedCoordinates()) {
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
    public boolean isValidCoordinate(Coordinate coordinate) {
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
