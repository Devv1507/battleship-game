package univalle.tedesoft.battleship.models.State;

import univalle.tedesoft.battleship.models.Enums.GamePhase;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;

/**
 * Clase Caretaker que gestiona los mementos del juego y la persistencia en archivos planos.
 * Implementa el patrón de diseño Memento como Caretaker.
 * 
 * @author Tu Nombre
 */
public class GameCaretaker {
    private static final String SAVE_DIRECTORY = "src/main/resources/univalle/tedesoft/battleship/saves";
    private static final String GAME_INFO_FILE = "game_info.txt";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    private GameMemento currentMemento;
    
    /**
     * Constructor del Caretaker
     */
    public GameCaretaker() {
        // Crear el directorio de guardado si no existe
        createSaveDirectory();
    }
    
    /**
     * Guarda un memento en memoria
     * @param memento El memento a guardar
     */
    public void saveMemento(GameMemento memento) {
        this.currentMemento = memento;
    }
    
    /**
     * Recupera el memento guardado en memoria
     * @return El memento guardado o null si no hay ninguno
     */
    public GameMemento getMemento() {
        return currentMemento;
    }
    
    /**
     * Guarda el estado del juego en archivos planos
     * @param memento El memento a persistir
     * @return true si se guardó exitosamente, false en caso contrario
     */
    public boolean saveGameToFiles(GameMemento memento) {
        try {
            // Guardar información del juego en archivo plano
            Path gameInfoPath = Paths.get(SAVE_DIRECTORY, GAME_INFO_FILE);
            
            try (BufferedWriter writer = Files.newBufferedWriter(gameInfoPath)) {
                writer.write("NICKNAME:" + memento.getHumanPlayerNickname());
                writer.newLine();
                writer.write("HUMAN_SUNK_SHIPS:" + memento.getHumanPlayerSunkShips());
                writer.newLine();
                writer.write("COMPUTER_SUNK_SHIPS:" + memento.getComputerPlayerSunkShips());
                writer.newLine();
                writer.write("GAME_PHASE:" + memento.getCurrentPhase());
                writer.newLine();
                writer.write("SAVE_DATE:" + memento.getSaveDateTime().format(DATE_FORMATTER));
                writer.newLine();
            }
            
            // Guardar el memento en memoria también
            saveMemento(memento);
            
            System.out.println("Juego guardado exitosamente en: " + gameInfoPath.toAbsolutePath());
            return true;
            
        } catch (IOException e) {
            System.err.println("Error al guardar el juego: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Guarda el estado completo del juego incluyendo barcos y tableros
     * @param gameState El estado del juego a guardar
     * @return true si se guardó exitosamente, false en caso contrario
     */
    public boolean saveCompleteGame(GameState gameState) {
        try {
            // Primero guardar la información básica del juego
            GameMemento memento = new GameMemento(
                gameState.getHumanPlayerNickname(),
                gameState.getHumanPlayerSunkShipCount(),
                gameState.getComputerPlayerSunkShipCount(),
                gameState.getCurrentPhase()
            );
            
            boolean basicInfoSaved = saveGameToFiles(memento);
            if (!basicInfoSaved) {
                return false;
            }
            
            // Luego serializar el estado completo del juego
            boolean completeStateSaved = GameSerializer.serializeGame(gameState);
            if (completeStateSaved) {
                System.out.println("Estado completo del juego guardado exitosamente");
                return true;
            } else {
                System.err.println("Error al guardar el estado completo del juego");
                return false;
            }
            
        } catch (Exception e) {
            System.err.println("Error al guardar el juego completo: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Carga el estado del juego desde archivos planos
     * @return El memento cargado o null si no se pudo cargar
     */
    public GameMemento loadGameFromFiles() {
        try {
            Path gameInfoPath = Paths.get(SAVE_DIRECTORY, GAME_INFO_FILE);
            
            if (!Files.exists(gameInfoPath)) {
                System.out.println("No se encontró archivo de juego guardado");
                return null;
            }
            
            String nickname = null;
            int humanSunkShips = 0;
            int computerSunkShips = 0;
            GamePhase gamePhase = GamePhase.INITIAL;
            
            try (BufferedReader reader = Files.newBufferedReader(gameInfoPath)) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] parts = line.split(":", 2);
                    if (parts.length == 2) {
                        String key = parts[0].trim();
                        String value = parts[1].trim();
                        
                        switch (key) {
                            case "NICKNAME":
                                nickname = value;
                                break;
                            case "HUMAN_SUNK_SHIPS":
                                humanSunkShips = Integer.parseInt(value);
                                break;
                            case "COMPUTER_SUNK_SHIPS":
                                computerSunkShips = Integer.parseInt(value);
                                break;
                            case "GAME_PHASE":
                                gamePhase = GamePhase.valueOf(value);
                                break;
                        }
                    }
                }
            }
            
            if (nickname != null) {
                GameMemento memento = new GameMemento(nickname, humanSunkShips, computerSunkShips, gamePhase);
                saveMemento(memento);
                System.out.println("Juego cargado exitosamente desde: " + gameInfoPath.toAbsolutePath());
                return memento;
            } else {
                System.err.println("Archivo de guardado corrupto: falta nickname");
                return null;
            }
            
        } catch (IOException e) {
            System.err.println("Error al cargar el juego: " + e.getMessage());
            return null;
        } catch (NumberFormatException e) {
            System.err.println("Error al parsear números en el archivo de guardado: " + e.getMessage());
            return null;
        } catch (IllegalArgumentException e) {
            System.err.println("Error al parsear la fase del juego: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Carga el estado completo del juego incluyendo barcos y tableros
     * @param gameState El estado del juego donde cargar los datos
     * @return true si se cargó exitosamente, false en caso contrario
     */
    public boolean loadCompleteGame(GameState gameState) {
        try {
            // Primero cargar la información básica del juego
            GameMemento memento = loadGameFromFiles();
            if (memento == null) {
                return false;
            }
            
            // Restaurar información básica
            gameState.restoreFromMemento(memento);
            
            // Luego deserializar el estado completo del juego
            boolean completeStateLoaded = GameSerializer.deserializeGame(gameState);
            if (completeStateLoaded) {
                System.out.println("Estado completo del juego cargado exitosamente");
                return true;
            } else {
                System.err.println("Error al cargar el estado completo del juego");
                return false;
            }
            
        } catch (Exception e) {
            System.err.println("Error al cargar el juego completo: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Verifica si existe un juego guardado
     * @return true si existe un juego guardado, false en caso contrario
     */
    public boolean isSavedGameAvailable() {
        // Verificar si hay información básica del juego
        Path gameInfoPath = Paths.get(SAVE_DIRECTORY, GAME_INFO_FILE);
        boolean hasBasicInfo = Files.exists(gameInfoPath);
        
        // Verificar si hay estado completo del juego
        boolean hasCompleteState = GameSerializer.hasSavedGame();
        
        return hasBasicInfo && hasCompleteState;
    }
    
    /**
     * Elimina el juego guardado
     * @return true si se eliminó exitosamente, false en caso contrario
     */
    public boolean deleteSavedGame() {
        try {
            Path gameInfoPath = Paths.get(SAVE_DIRECTORY, GAME_INFO_FILE);
            if (Files.exists(gameInfoPath)) {
                Files.delete(gameInfoPath);
                currentMemento = null;
                System.out.println("Juego guardado eliminado");
                return true;
            }
            return false;
        } catch (IOException e) {
            System.err.println("Error al eliminar el juego guardado: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Crea el directorio de guardado si no existe
     */
    private void createSaveDirectory() {
        try {
            Path saveDir = Paths.get(SAVE_DIRECTORY);
            if (!Files.exists(saveDir)) {
                Files.createDirectories(saveDir);
                System.out.println("Directorio de guardado creado: " + saveDir.toAbsolutePath());
            }
        } catch (IOException e) {
            System.err.println("Error al crear directorio de guardado: " + e.getMessage());
        }
    }
} 