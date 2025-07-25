package univalle.tedesoft.battleship.models.state;

import univalle.tedesoft.battleship.models.Board;
import univalle.tedesoft.battleship.models.Coordinate;
import univalle.tedesoft.battleship.models.enums.CellState;
import univalle.tedesoft.battleship.models.enums.Orientation;
import univalle.tedesoft.battleship.models.enums.ShipType;
import univalle.tedesoft.battleship.models.ships.*;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Clase para serializar y deserializar el estado completo del juego de Batalla Naval.
 * Se encarga de la persistencia detallada de tableros, barcos y sus posiciones 
 * en archivos de texto plano.
 * 
 * Maneja la serialización de:
 * - Tableros: estado de cada celda (agua, barco, impacto, etc.)
 * - Barcos: tipo, orientación, daño recibido y coordenadas
 * 
 * Genera archivos separados para tableros (*_board_state.txt) 
 * y barcos (*_ships_state.txt).
 * 
 * @author Juan Pablo Escamilla
 * @author David Valencia
 * @author Santiago Guerrero
 */
public class GameSerializer {
    private static final String SAVE_DIRECTORY = "src/main/resources/univalle/tedesoft/battleship/saves";
    private static final String BOARD_FILE = "board_state.txt";
    private static final String SHIPS_FILE = "ships_state.txt";
    
    /**
     * Serializa el estado completo del juego en archivos de texto plano.
     * Guarda tableros (estado de celdas) y barcos (posición, orientación, daño).
     * 
     * @param gameState El estado del juego a serializar
     * @return true si se serializó exitosamente, false en caso contrario
     */
    public static boolean serializeGame(GameState gameState) {
        try {
            createSaveDirectory();
            
            // Serializar tableros
            serializeBoard(gameState.getHumanPlayerPositionBoard(), "human_board");
            serializeBoard(gameState.getMachinePlayerActualPositionBoard(), "machine_board");
            serializeBoard(gameState.getMachinePlayerTerritoryBoard(), "machine_territory");
            
            // Serializar barcos
            serializeShips(gameState.getHumanPlayerPositionBoard().getShips(), "human_ships");
            serializeShips(gameState.getMachinePlayerActualPositionBoard().getShips(), "machine_ships");
            
            return true;
        } catch (Exception e) {
            System.err.println("Error al serializar el juego: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Deserializa el estado completo del juego desde archivos guardados.
     * Restaura tableros, barcos y sus posiciones desde los archivos de texto.
     * 
     * @param gameState El estado del juego donde cargar los datos
     * @return true si se deserializó exitosamente, false en caso contrario
     */
    public static boolean deserializeGame(GameState gameState) {
        try {
            // Deserializar tableros
            deserializeBoard(gameState.getHumanPlayerPositionBoard(), "human_board");
            deserializeBoard(gameState.getMachinePlayerActualPositionBoard(), "machine_board");
            deserializeBoard(gameState.getMachinePlayerTerritoryBoard(), "machine_territory");
            
            // Deserializar barcos
            deserializeShips(gameState.getHumanPlayerPositionBoard(), "human_ships");
            deserializeShips(gameState.getMachinePlayerActualPositionBoard(), "machine_ships");
            
            return true;
        } catch (Exception e) {
            System.err.println("Error al deserializar el juego: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Serializa un tablero
     * @param board El tablero a serializar
     * @param prefix Prefijo para el archivo
     */
    private static void serializeBoard(Board board, String prefix) throws IOException {
        Path boardPath = Paths.get(SAVE_DIRECTORY, prefix + "_" + BOARD_FILE);
        
        try (BufferedWriter writer = Files.newBufferedWriter(boardPath)) {
            // Escribir el estado de cada celda
            for (int row = 0; row < board.getSize(); row++) {
                for (int col = 0; col < board.getSize(); col++) {
                    CellState state = board.getCellState(row, col);
                    writer.write(row + "," + col + ":" + state.name());
                    writer.newLine();
                }
            }
        }
    }
    
    /**
     * Deserializa un tablero
     * @param board El tablero donde cargar los datos
     * @param prefix Prefijo para el archivo
     */
    private static void deserializeBoard(Board board, String prefix) throws IOException {
        Path boardPath = Paths.get(SAVE_DIRECTORY, prefix + "_" + BOARD_FILE);
        
        if (!Files.exists(boardPath)) {
            return; // No hay archivo de tablero guardado
        }
        
        // Limpiar el tablero
        board.resetBoard();
        
        try (BufferedReader reader = Files.newBufferedReader(boardPath)) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(":");
                if (parts.length == 2) {
                    String[] coords = parts[0].split(",");
                    int row = Integer.parseInt(coords[0]);
                    int col = Integer.parseInt(coords[1]);
                    CellState state = CellState.valueOf(parts[1]);
                    
                    board.setCellState(row, col, state);
                }
            }
        }
    }
    
    /**
     * Serializa una lista de barcos
     * @param ships La lista de barcos a serializar
     * @param prefix Prefijo para el archivo
     */
    private static void serializeShips(List<Ship> ships, String prefix) throws IOException {
        Path shipsPath = Paths.get(SAVE_DIRECTORY, prefix + "_" + SHIPS_FILE);
        
        try (BufferedWriter writer = Files.newBufferedWriter(shipsPath)) {
            for (Ship ship : ships) {
                // Escribir información del barco
                writer.write("SHIP:" + ship.getShipType().name() + ":" + 
                            ship.getOrientation().name() + ":" + 
                            ship.getHitCount() + ":" + 
                            ship.isSunk());
                writer.newLine();
                
                // Escribir coordenadas del barco
                for (Coordinate coord : ship.getOccupiedCoordinates()) {
                    writer.write("COORD:" + coord.getX() + "," + coord.getY());
                    writer.newLine();
                }
                
                writer.write("ENDSHIP");
                writer.newLine();
            }
        }
    }
    
    /**
     * Deserializa barcos y los coloca en el tablero
     * @param board El tablero donde colocar los barcos
     * @param prefix Prefijo para el archivo
     */
    private static void deserializeShips(Board board, String prefix) throws IOException {
        Path shipsPath = Paths.get(SAVE_DIRECTORY, prefix + "_" + SHIPS_FILE);
        
        if (!Files.exists(shipsPath)) {
            return; // No hay archivo de barcos guardado
        }
        
        try (BufferedReader reader = Files.newBufferedReader(shipsPath)) {
            String line;
            Ship currentShip = null;
            List<Coordinate> shipCoords = new ArrayList<>();
            
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("SHIP:")) {
                    // Crear nuevo barco
                    String[] shipData = line.split(":");
                    ShipType shipType = ShipType.valueOf(shipData[1]);
                    Orientation orientation = Orientation.valueOf(shipData[2]);
                    int hitCount = Integer.parseInt(shipData[3]);
                    boolean sunk = Boolean.parseBoolean(shipData[4]);
                    
                    currentShip = createShipFromType(shipType);
                    currentShip.setOrientation(orientation);
                    
                    // Restaurar estado del barco
                    for (int i = 0; i < hitCount; i++) {
                        currentShip.registerHit();
                    }
                    
                    shipCoords.clear();
                } else if (line.startsWith("COORD:")) {
                    // Agregar coordenada al barco actual
                    String[] coordData = line.split(":")[1].split(",");
                    int x = Integer.parseInt(coordData[0]);
                    int y = Integer.parseInt(coordData[1]);
                    shipCoords.add(new Coordinate(x, y));
                } else if (line.equals("ENDSHIP")) {
                    // Finalizar barco y agregarlo al tablero
                    if (currentShip != null) {
                        // Agregar coordenadas al barco
                        for (Coordinate coord : shipCoords) {
                            currentShip.addCoordinates(coord);
                        }
                        
                        // Agregar barco al tablero (sin verificar superposición)
                        board.addShipDirectly(currentShip);
                    }
                    currentShip = null;
                    shipCoords.clear();
                }
            }
        }
    }
    
    /**
     * Crea una instancia de barco del tipo especificado.
     * Factory method usado durante la deserialización para reconstruir barcos.
     * 
     * @param shipType El tipo de barco a crear (AIR_CRAFT_CARRIER, SUBMARINE, DESTROYER, FRIGATE)
     * @return Una nueva instancia del tipo de barco especificado
     * @throws IllegalArgumentException si el tipo de barco no es reconocido
     */
    private static Ship createShipFromType(ShipType shipType) {
        /*
        switch (shipType) {
            case AIR_CRAFT_CARRIER:
                return new AirCraftCarrier();
            case SUBMARINE:
                return new Submarine();
            case DESTROYER:
                return new Destroyer();
            case FRIGATE:
                return new Frigate();
            default:
                throw new IllegalArgumentException("Tipo de barco desconocido: " + shipType);
        }

         */
        return ShipFactory.createShip(shipType);
    }
    
    /**
     * Crea el directorio de guardado si no existe
     */
    private static void createSaveDirectory() throws IOException {
        Path saveDir = Paths.get(SAVE_DIRECTORY);
        if (!Files.exists(saveDir)) {
            Files.createDirectories(saveDir);
        }
    }
    
    /**
     * Verifica si existen archivos de guardado
     * @return true si existen archivos de guardado, false en caso contrario
     */
    public static boolean hasSavedGame() {
        Path humanBoardPath = Paths.get(SAVE_DIRECTORY, "human_board_" + BOARD_FILE);
        return Files.exists(humanBoardPath);
    }
    
    /**
     * Elimina todos los archivos de guardado
     * @return true si se eliminaron exitosamente, false en caso contrario
     */
    public static boolean deleteSavedGame() {
        try {
            Path saveDir = Paths.get(SAVE_DIRECTORY);
            if (Files.exists(saveDir)) {
                Files.walk(saveDir)
                    .filter(Files::isRegularFile)
                    .forEach(path -> {
                        try {
                            Files.delete(path);
                        } catch (IOException e) {
                            System.err.println("Error al eliminar archivo: " + path);
                        }
                    });
            }
            return true;
        } catch (IOException e) {
            System.err.println("Error al eliminar archivos de guardado: " + e.getMessage());
            return false;
        }
    }
} 