package univalle.tedesoft.battleship.models.state;

import univalle.tedesoft.battleship.models.board.Board;
import univalle.tedesoft.battleship.models.board.Coordinate;
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
            // Limpiar solo los barcos existentes (SIN afectar las casillas)
            gameState.getHumanPlayerPositionBoard().clearShipsOnly();
            gameState.getMachinePlayerActualPositionBoard().clearShipsOnly();
            gameState.getMachinePlayerTerritoryBoard().clearShipsOnly();
            
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
        
        // Inicializar solo la grilla (barcos ya fueron limpiados antes)
        for (int i = 0; i < board.getSize(); i++) {
            for (int j = 0; j < board.getSize(); j++) {
                board.setCellState(i, j, CellState.EMPTY);
            }
        }
        
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

    // ========== MÉTODOS PARA MANEJO POR NICKNAME ==========

    /**
     * Serializa el estado completo del juego en archivos de texto plano en un directorio específico.
     * Guarda tableros (estado de celdas) y barcos (posición, orientación, daño) para un jugador específico.
     * 
     * @param gameState El estado del juego a serializar
     * @param playerSaveDir El directorio donde guardar los archivos del jugador
     * @return true si se serializó exitosamente, false en caso contrario
     */
    public static boolean serializeGameByNickname(GameState gameState, String playerSaveDir) {
        try {
            // Asegurar que el directorio del jugador existe
            Path playerPath = Paths.get(playerSaveDir);
            if (!Files.exists(playerPath)) {
                Files.createDirectories(playerPath);
            }
            
            // Serializar tableros en el directorio del jugador
            serializeBoardByNickname(gameState.getHumanPlayerPositionBoard(), "human_board", playerSaveDir);
            serializeBoardByNickname(gameState.getMachinePlayerActualPositionBoard(), "machine_board", playerSaveDir);
            serializeBoardByNickname(gameState.getMachinePlayerTerritoryBoard(), "machine_territory", playerSaveDir);
            
            // Serializar barcos en el directorio del jugador
            serializeShipsByNickname(gameState.getHumanPlayerPositionBoard().getShips(), "human_ships", playerSaveDir);
            serializeShipsByNickname(gameState.getMachinePlayerActualPositionBoard().getShips(), "machine_ships", playerSaveDir);
            
            return true;
        } catch (Exception e) {
            System.err.println("Error al serializar el juego para el jugador: " + e.getMessage());
            return false;
        }
    }

    /**
     * Deserializa el estado completo del juego desde archivos guardados en un directorio específico.
     * Restaura tableros, barcos y sus posiciones desde los archivos de texto del jugador.
     * 
     * @param gameState El estado del juego donde cargar los datos
     * @param playerSaveDir El directorio donde están los archivos del jugador
     * @return true si se deserializó exitosamente, false en caso contrario
     */
    public static boolean deserializeGameByNickname(GameState gameState, String playerSaveDir) {
        try {
            // Verificar que el directorio del jugador existe
            Path playerPath = Paths.get(playerSaveDir);
            if (!Files.exists(playerPath)) {
                System.err.println("No existe el directorio de guardado del jugador: " + playerSaveDir);
                return false;
            }
            
            // PASO 1: Limpiar solo los barcos existentes (SIN afectar las casillas)
            gameState.getHumanPlayerPositionBoard().clearShipsOnly();
            gameState.getMachinePlayerActualPositionBoard().clearShipsOnly();
            gameState.getMachinePlayerTerritoryBoard().clearShipsOnly();
            
            // PASO 2: Deserializar tableros desde el directorio del jugador
            // Esto restaura el estado correcto de las casillas (HIT, MISS, WATER, etc.)
            if (!deserializeBoardByNickname(gameState.getHumanPlayerPositionBoard(), "human_board", playerSaveDir)) {
                return false;
            }
            if (!deserializeBoardByNickname(gameState.getMachinePlayerActualPositionBoard(), "machine_board", playerSaveDir)) {
                return false;
            }
            if (!deserializeBoardByNickname(gameState.getMachinePlayerTerritoryBoard(), "machine_territory", playerSaveDir)) {
                return false;
            }
            
            // PASO 3: Deserializar barcos desde el directorio del jugador
            List<Ship> humanShips = deserializeShipsByNickname("human_ships", playerSaveDir);
            List<Ship> machineShips = deserializeShipsByNickname("machine_ships", playerSaveDir);
            
            if (humanShips != null && machineShips != null) {
                // PASO 4: Agregar los barcos cargados (las casillas ya están correctas del paso 2)
                for (Ship ship : humanShips) {
                    gameState.getHumanPlayerPositionBoard().addShipDirectly(ship);
                }
                for (Ship ship : machineShips) {
                    gameState.getMachinePlayerActualPositionBoard().addShipDirectly(ship);
                }
                return true;
            }
            
            return false;
        } catch (Exception e) {
            System.err.println("Error al deserializar el juego para el jugador: " + e.getMessage());
            return false;
        }
    }

    /**
     * Serializa un tablero en un archivo específico dentro del directorio del jugador.
     */
    private static void serializeBoardByNickname(Board board, String boardName, String playerSaveDir) throws IOException {
        Path boardFilePath = Paths.get(playerSaveDir, boardName + "_board_state.txt");
        
        try (BufferedWriter writer = Files.newBufferedWriter(boardFilePath)) {
            int boardSize = board.getSize();
            
            for (int row = 0; row < boardSize; row++) {
                for (int col = 0; col < boardSize; col++) {
                    try {
                        CellState state = board.getCellState(row, col);
                        writer.write(row + "," + col + "," + state.name());
                        writer.newLine();
                    } catch (Exception e) {
                        System.err.println("Error al obtener estado de celda (" + row + "," + col + "): " + e.getMessage());
                    }
                }
            }
        }
    }

    /**
     * Deserializa un tablero desde un archivo específico dentro del directorio del jugador.
     */
    private static boolean deserializeBoardByNickname(Board board, String boardName, String playerSaveDir) {
        Path boardFilePath = Paths.get(playerSaveDir, boardName + "_board_state.txt");
        
        if (!Files.exists(boardFilePath)) {
            System.err.println("No se encontró archivo de tablero: " + boardFilePath);
            return false;
        }
        
        try (BufferedReader reader = Files.newBufferedReader(boardFilePath)) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 3) {
                    int row = Integer.parseInt(parts[0]);
                    int col = Integer.parseInt(parts[1]);
                    CellState state = CellState.valueOf(parts[2]);
                    
                    try {
                        board.setCellState(row, col, state);
                    } catch (Exception e) {
                        System.err.println("Error al establecer estado de celda (" + row + "," + col + "): " + e.getMessage());
                    }
                }
            }
            return true;
        } catch (IOException e) {
            System.err.println("Error al deserializar tablero " + boardName + ": " + e.getMessage());
            return false;
        } catch (IllegalArgumentException e) {
            System.err.println("Error al parsear estado de celda en tablero " + boardName + ": " + e.getMessage());
            return false;
        }
    }

    /**
     * Serializa una lista de barcos en un archivo específico dentro del directorio del jugador.
     */
    private static void serializeShipsByNickname(List<Ship> ships, String shipsName, String playerSaveDir) throws IOException {
        Path shipsFilePath = Paths.get(playerSaveDir, shipsName + "_ships_state.txt");
        
        try (BufferedWriter writer = Files.newBufferedWriter(shipsFilePath)) {
            for (Ship ship : ships) {
                // Escribir información básica del barco
                writer.write("SHIP_TYPE:" + ship.getShipType().name());
                writer.newLine();
                writer.write("ORIENTATION:" + ship.getOrientation().name());
                writer.newLine();
                writer.write("IS_SUNK:" + ship.isSunk());
                writer.newLine();
                writer.write("HIT_COUNT:" + ship.getHitCount());
                writer.newLine();
                
                // Escribir coordenadas del barco
                writer.write("COORDINATES:");
                List<Coordinate> coordinates = ship.getOccupiedCoordinates();
                for (int i = 0; i < coordinates.size(); i++) {
                    Coordinate coord = coordinates.get(i);
                    writer.write(coord.getY() + "," + coord.getX());
                    if (i < coordinates.size() - 1) {
                        writer.write(";");
                    }
                }
                writer.newLine();
                
                writer.write("---"); // Separador entre barcos
                writer.newLine();
            }
        }
    }

    /**
     * Deserializa una lista de barcos desde un archivo específico dentro del directorio del jugador.
     */
    private static List<Ship> deserializeShipsByNickname(String shipsName, String playerSaveDir) {
        Path shipsFilePath = Paths.get(playerSaveDir, shipsName + "_ships_state.txt");
        
        if (!Files.exists(shipsFilePath)) {
            System.err.println("No se encontró archivo de barcos: " + shipsFilePath);
            return null;
        }
        
        List<Ship> ships = new ArrayList<>();
        
        try (BufferedReader reader = Files.newBufferedReader(shipsFilePath)) {
            String line;
            ShipType shipType = null;
            Orientation orientation = null;
            boolean isSunk = false;
            int hitCount = 0;
            List<Coordinate> coordinates = null;
            
            while ((line = reader.readLine()) != null) {
                if (line.equals("---")) {
                    // Fin de un barco, crear la instancia
                    if (shipType != null && coordinates != null) {
                        Ship ship = createShipByType(shipType);
                        if (ship != null) {
                            ship.setOrientation(orientation);
                            // Agregar coordenadas
                            for (Coordinate coord : coordinates) {
                                ship.addCoordinates(coord);
                            }
                            // Restaurar daño
                            for (int i = 0; i < hitCount; i++) {
                                ship.registerHit();
                            }
                            ships.add(ship);
                        }
                    }
                    
                    // Resetear variables para el siguiente barco
                    shipType = null;
                    orientation = null;
                    isSunk = false;
                    hitCount = 0;
                    coordinates = null;
                    
                } else if (line.startsWith("SHIP_TYPE:")) {
                    shipType = ShipType.valueOf(line.substring("SHIP_TYPE:".length()));
                } else if (line.startsWith("ORIENTATION:")) {
                    orientation = Orientation.valueOf(line.substring("ORIENTATION:".length()));
                } else if (line.startsWith("IS_SUNK:")) {
                    isSunk = Boolean.parseBoolean(line.substring("IS_SUNK:".length()));
                } else if (line.startsWith("HIT_COUNT:")) {
                    hitCount = Integer.parseInt(line.substring("HIT_COUNT:".length()));
                } else if (line.startsWith("COORDINATES:")) {
                    coordinates = parseCoordinatesString(line.substring("COORDINATES:".length()));
                }
            }
            
        } catch (IOException | IllegalArgumentException e) {
            System.err.println("Error al deserializar barcos " + shipsName + ": " + e.getMessage());
            return null;
        }
        
        return ships;
    }

    /**
     * Crea una instancia de barco según su tipo.
     */
    private static Ship createShipByType(ShipType shipType) {
        switch (shipType) {
            case FRIGATE:
                return new Frigate();
            case DESTROYER:
                return new Destroyer();
            case SUBMARINE:
                return new Submarine();
            case AIR_CRAFT_CARRIER:
                return new AirCraftCarrier();
            default:
                System.err.println("Tipo de barco desconocido: " + shipType);
                return null;
        }
    }

    /**
     * Parsea una cadena de coordenadas en formato "y1,x1;y2,x2;..." 
     */
    private static List<Coordinate> parseCoordinatesString(String coordinatesStr) {
        List<Coordinate> coordinates = new ArrayList<>();
        
        if (coordinatesStr.trim().isEmpty()) {
            return coordinates;
        }
        
        String[] coordPairs = coordinatesStr.split(";");
        for (String coordPair : coordPairs) {
            String[] parts = coordPair.split(",");
            if (parts.length == 2) {
                try {
                    int y = Integer.parseInt(parts[0].trim());
                    int x = Integer.parseInt(parts[1].trim());
                    coordinates.add(new Coordinate(x, y));
                } catch (NumberFormatException e) {
                    System.err.println("Error al parsear coordenada: " + coordPair);
                }
            }
        }
        
        return coordinates;
    }

    /**
     * Verifica si existe un juego guardado en un directorio específico del jugador.
     * 
     * @param playerSaveDir El directorio del jugador donde buscar
     * @return true si existe un juego guardado válido, false en caso contrario
     */
    public static boolean hasSavedGameByNickname(String playerSaveDir) {
        // Verificar si hay archivos de tableros
        Path humanBoardPath = Paths.get(playerSaveDir, "human_board_board_state.txt");
        Path machineBoardPath = Paths.get(playerSaveDir, "machine_board_board_state.txt");
        Path territoryBoardPath = Paths.get(playerSaveDir, "machine_territory_board_state.txt");
        
        // Verificar si hay archivos de barcos
        Path humanShipsPath = Paths.get(playerSaveDir, "human_ships_ships_state.txt");
        Path machineShipsPath = Paths.get(playerSaveDir, "machine_ships_ships_state.txt");
        
        return Files.exists(humanBoardPath) && Files.exists(machineBoardPath) && 
               Files.exists(territoryBoardPath) && Files.exists(humanShipsPath) && 
               Files.exists(machineShipsPath);
    }
} 