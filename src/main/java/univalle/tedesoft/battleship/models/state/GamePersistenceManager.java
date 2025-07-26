package univalle.tedesoft.battleship.models.state;

import univalle.tedesoft.battleship.models.enums.GamePhase;

import java.io.BufferedReader;
import java.io.BufferedWriter;
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
 * Gestor único para la persistencia del juego.
 * Esta clase es la única autoridad responsable de guardar, cargar,
 * y descubrir partidas guardadas en el sistema de archivos.
 */
public final class GamePersistenceManager {

    private static final String SAVE_DIRECTORY = "src/main/resources/univalle/tedesoft/battleship/saves";
    private static final String GAME_INFO_FILE = "game_info.txt";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /** Constructor privado para prevenir la instanciación. */
    private GamePersistenceManager() {}

    /**
     * Clase interna para representar información básica de una partida guardada.
     * Es un DTO (Data Transfer Object) para comunicar los datos de las partidas guardadas.
     */
    public static class SavedGameInfo {
        private final String nickname;
        private final String gamePhase;
        private final LocalDateTime saveDate;
        private final String saveDirectory;

        public SavedGameInfo(String nickname, String gamePhase, LocalDateTime saveDate, String saveDirectory) {
            this.nickname = nickname;
            this.gamePhase = gamePhase;
            this.saveDate = saveDate;
            this.saveDirectory = saveDirectory;
        }

        public String getNickname() { return this.nickname; }
        public String getGamePhase() { return this.gamePhase; }
        public LocalDateTime getSaveDate() { return this.saveDate; }
        public String getSaveDirectory() { return this.saveDirectory; }
    }

    /**
     * Guarda el estado completo de una partida para un jugador específico.
     *
     * @param gameState El estado del juego a guardar.
     * @return true si se guardó exitosamente, false en caso contrario.
     */
    public static boolean saveGame(GameState gameState) {
        String nickname = gameState.getHumanPlayerNickname();
        if (nickname == null || nickname.trim().isEmpty()) {
            System.err.println("No se puede guardar el juego sin un nickname válido.");
            return false;
        }

        try {
            String playerSaveDir = createPlayerSaveDirectory(nickname);

            // 1. Crear el Memento con la metadata actual.
            GameMemento memento = gameState.createMemento();

            // 2. Guardar la metadata en game_info.txt.
            saveGameInfoToFile(memento, playerSaveDir);

            // 3. Serializar el estado completo del juego (tableros y barcos).
            boolean completeStateSaved = GameSerializer.serializeGameByNickname(gameState, playerSaveDir);

            if (completeStateSaved) {
                System.out.println("Juego guardado exitosamente para " + nickname + " en: " + playerSaveDir);
                return true;
            } else {
                System.err.println("Error al guardar el estado completo del juego para " + nickname);
                return false;
            }
        } catch (Exception e) {
            System.err.println("Error al guardar el juego completo para " + nickname + ": " + e.getMessage());
            return false;
        }
    }

    /**
     * Carga el estado completo de una partida para un jugador específico.
     *
     * @param gameState El objeto GameState donde se cargarán los datos.
     * @param nickname  El nickname del jugador cuya partida se cargará.
     * @return true si se cargó exitosamente, false en caso contrario.
     */
    public static boolean loadGame(GameState gameState, String nickname) {
        try {
            String playerSaveDir = getPlayerSaveDirectory(nickname);
            if (playerSaveDir == null) {
                System.err.println("No se encontró directorio de guardado para " + nickname);
                return false;
            }

            // 1. Cargar la metadata desde game_info.txt.
            GameMemento memento = loadGameInfoFromFile(playerSaveDir);
            if (memento == null) {
                return false;
            }
            gameState.restoreFromMemento(memento);

            // 2. Deserializar el estado completo del juego.
            boolean completeStateLoaded = GameSerializer.deserializeGameByNickname(gameState, playerSaveDir);
            if (completeStateLoaded) {
                System.out.println("Estado completo del juego cargado para " + nickname);
                return true;
            } else {
                System.err.println("Error al cargar el estado completo del juego para " + nickname);
                return false;
            }
        } catch (Exception e) {
            System.err.println("Error al cargar el juego completo para " + nickname + ": " + e.getMessage());
            return false;
        }
    }

    /**
     * Busca y devuelve información de todas las partidas guardadas para un nickname.
     *
     * @param nickname El nickname a buscar.
     * @return Una lista de objetos SavedGameInfo.
     */
    public static List<SavedGameInfo> findSavedGamesByNickname(String nickname) {
        List<SavedGameInfo> savedGames = new ArrayList<>();
        String playerSaveDir = getPlayerSaveDirectory(nickname);

        if (playerSaveDir != null) {
            GameMemento memento = loadGameInfoFromFile(playerSaveDir);
            if (memento != null) {
                savedGames.add(new SavedGameInfo(
                        memento.getHumanPlayerNickname(),
                        memento.getCurrentPhase().toString(),
                        memento.getSaveDateTime(),
                        playerSaveDir
                ));
            }
        }
        return savedGames;
    }

    // --- Métodos Privados Auxiliares ---

    private static void saveGameInfoToFile(GameMemento memento, String playerSaveDir) throws IOException {
        Path gameInfoPath = Paths.get(playerSaveDir, GAME_INFO_FILE);
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
    }

    private static GameMemento loadGameInfoFromFile(String playerSaveDir) {
        Path gameInfoPath = Paths.get(playerSaveDir, GAME_INFO_FILE);
        if (!Files.exists(gameInfoPath)) {
            return null;
        }

        try (BufferedReader reader = Files.newBufferedReader(gameInfoPath)) {
            String nickname = null;
            int humanSunkShips = 0;
            int computerSunkShips = 0;
            GamePhase gamePhase = GamePhase.INITIAL;

            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(":", 2);
                if (parts.length == 2) {
                    String key = parts[0].trim();
                    String value = parts[1].trim();
                    switch (key) {
                        case "NICKNAME": nickname = value; break;
                        case "HUMAN_SUNK_SHIPS": humanSunkShips = Integer.parseInt(value); break;
                        case "COMPUTER_SUNK_SHIPS": computerSunkShips = Integer.parseInt(value); break;
                        case "GAME_PHASE": gamePhase = GamePhase.valueOf(value); break;
                    }
                }
            }
            return new GameMemento(nickname, humanSunkShips, computerSunkShips, gamePhase);
        } catch (IOException | IllegalArgumentException | DateTimeParseException e) {
            System.err.println("Error al leer el archivo de información del juego: " + e.getMessage());
            return null;
        }
    }

    private static String getPlayerSaveDirectory(String nickname) {
        Path playerPath = Paths.get(SAVE_DIRECTORY, nickname);
        if (Files.isDirectory(playerPath)) {
            return playerPath.toString();
        }
        return null;
    }

    private static String createPlayerSaveDirectory(String nickname) throws IOException {
        Path playerPath = Paths.get(SAVE_DIRECTORY, nickname);
        if (!Files.exists(playerPath)) {
            Files.createDirectories(playerPath);
        }
        return playerPath.toString();
    }
}
