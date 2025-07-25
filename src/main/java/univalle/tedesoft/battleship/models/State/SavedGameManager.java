package univalle.tedesoft.battleship.models.State;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * Clase para gestionar múltiples partidas guardadas organizadas por nickname.
 * Permite buscar, listar y obtener información de partidas guardadas específicas.
 * 
 * @author Juan Pablo Escamilla
 * @author David Valencia  
 * @author Santiago Guerrero
 */
public class SavedGameManager {
    private static final String SAVE_DIRECTORY = "src/main/resources/univalle/tedesoft/battleship/saves";
    private static final String GAME_INFO_FILE = "game_info.txt";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    /**
     * Clase interna para representar información básica de una partida guardada
     */
    public static class SavedGameInfo {
        private final String nickname;
        private final int humanSunkShips;
        private final int computerSunkShips;
        private final String gamePhase;
        private final LocalDateTime saveDate;
        private final String saveDirectory;
        
        public SavedGameInfo(String nickname, int humanSunkShips, int computerSunkShips, 
                           String gamePhase, LocalDateTime saveDate, String saveDirectory) {
            this.nickname = nickname;
            this.humanSunkShips = humanSunkShips;
            this.computerSunkShips = computerSunkShips;
            this.gamePhase = gamePhase;
            this.saveDate = saveDate;
            this.saveDirectory = saveDirectory;
        }
        
        public String getNickname() { return nickname; }
        public int getHumanSunkShips() { return humanSunkShips; }
        public int getComputerSunkShips() { return computerSunkShips; }
        public String getGamePhase() { return gamePhase; }
        public LocalDateTime getSaveDate() { return saveDate; }
        public String getSaveDirectory() { return saveDirectory; }
        
        @Override
        public String toString() {
            return String.format("Partida de %s - %s (Guardada: %s)", 
                nickname, gamePhase, saveDate.format(DATE_FORMATTER));
        }
    }
    
    /**
     * Busca todas las partidas guardadas para un nickname específico
     * @param nickname El nombre del jugador
     * @return Lista de partidas guardadas para ese jugador
     */
    public static List<SavedGameInfo> findSavedGamesByNickname(String nickname) {
        List<SavedGameInfo> savedGames = new ArrayList<>();
        Path savesPath = Paths.get(SAVE_DIRECTORY);
        
        if (!Files.exists(savesPath)) {
            return savedGames;
        }
        
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(savesPath)) {
            for (Path entry : stream) {
                if (Files.isDirectory(entry)) {
                    String dirName = entry.getFileName().toString();
                    if (dirName.equalsIgnoreCase(nickname)) {
                        SavedGameInfo gameInfo = loadGameInfoFromDirectory(entry.toString());
                        if (gameInfo != null) {
                            savedGames.add(gameInfo);
                        }
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error al buscar partidas guardadas: " + e.getMessage());
        }
        
        return savedGames;
    }
    
    /**
     * Obtiene la última partida guardada para un nickname específico
     * @param nickname El nombre del jugador
     * @return La información de la última partida guardada o null si no existe
     */
    public static SavedGameInfo getLastSavedGame(String nickname) {
        List<SavedGameInfo> savedGames = findSavedGamesByNickname(nickname);
        
        if (savedGames.isEmpty()) {
            return null;
        }
        
        // Encontrar la partida más reciente
        SavedGameInfo lastGame = savedGames.get(0);
        for (SavedGameInfo game : savedGames) {
            if (game.getSaveDate().isAfter(lastGame.getSaveDate())) {
                lastGame = game;
            }
        }
        
        return lastGame;
    }
    
    /**
     * Verifica si existe al menos una partida guardada para un nickname
     * @param nickname El nombre del jugador
     * @return true si tiene partidas guardadas, false en caso contrario
     */
    public static boolean hasAnyGameSaved(String nickname) {
        return !findSavedGamesByNickname(nickname).isEmpty();
    }
    
    /**
     * Obtiene todas las partidas guardadas de todos los jugadores
     * @return Lista de todas las partidas guardadas
     */
    public static List<SavedGameInfo> getAllSavedGames() {
        List<SavedGameInfo> allSavedGames = new ArrayList<>();
        Path savesPath = Paths.get(SAVE_DIRECTORY);
        
        if (!Files.exists(savesPath)) {
            return allSavedGames;
        }
        
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(savesPath)) {
            for (Path entry : stream) {
                if (Files.isDirectory(entry)) {
                    SavedGameInfo gameInfo = loadGameInfoFromDirectory(entry.toString());
                    if (gameInfo != null) {
                        allSavedGames.add(gameInfo);
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error al cargar todas las partidas guardadas: " + e.getMessage());
        }
        
        return allSavedGames;
    }
    
    /**
     * Carga la información de una partida desde un directorio específico
     * @param directoryPath La ruta del directorio que contiene la partida
     * @return SavedGameInfo con la información de la partida o null si hay error
     */
    private static SavedGameInfo loadGameInfoFromDirectory(String directoryPath) {
        Path gameInfoPath = Paths.get(directoryPath, GAME_INFO_FILE);
        
        if (!Files.exists(gameInfoPath)) {
            return null;
        }
        
        try (BufferedReader reader = Files.newBufferedReader(gameInfoPath)) {
            String nickname = null;
            int humanSunkShips = 0;
            int computerSunkShips = 0;
            String gamePhase = "UNKNOWN";
            LocalDateTime saveDate = LocalDateTime.now();
            
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
                            gamePhase = value;
                            break;
                        case "SAVE_DATE":
                            try {
                                saveDate = LocalDateTime.parse(value, DATE_FORMATTER);
                            } catch (DateTimeParseException e) {
                                System.err.println("Error al parsear fecha: " + value);
                            }
                            break;
                    }
                }
            }
            
            if (nickname != null) {
                return new SavedGameInfo(nickname, humanSunkShips, computerSunkShips, 
                                       gamePhase, saveDate, directoryPath);
            }
            
        } catch (IOException | NumberFormatException e) {
            System.err.println("Error al cargar información del juego desde " + directoryPath + ": " + e.getMessage());
        }
        
        return null;
    }
    
    /**
     * Crea el directorio de guardado si no existe
     */
    public static void createSaveDirectory() {
        Path savesPath = Paths.get(SAVE_DIRECTORY);
        try {
            if (!Files.exists(savesPath)) {
                Files.createDirectories(savesPath);
            }
        } catch (IOException e) {
            System.err.println("Error al crear directorio de guardado: " + e.getMessage());
        }
    }
    
    /**
     * Crea el directorio específico para un nickname si no existe
     * @param nickname El nombre del jugador
     * @return La ruta del directorio creado
     */
    public static String createPlayerSaveDirectory(String nickname) {
        String playerDir = Paths.get(SAVE_DIRECTORY, nickname).toString();
        Path playerPath = Paths.get(playerDir);
        
        try {
            if (!Files.exists(playerPath)) {
                Files.createDirectories(playerPath);
            }
        } catch (IOException e) {
            System.err.println("Error al crear directorio para jugador " + nickname + ": " + e.getMessage());
        }
        
        return playerDir;
    }
} 