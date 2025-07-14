package univalle.tedesoft.battleship;

import univalle.tedesoft.battleship.models.Players.HumanPlayer;
import univalle.tedesoft.battleship.models.State.GameState;

/**
 * Clase de prueba para verificar que los archivos se guardan correctamente en resources.
 * 
 * @author Tu Nombre
 */
public class TestSaveLoad {
    
    public static void main(String[] args) {
        System.out.println("=== Prueba de Guardado en Resources ===\n");
        
        // Crear una instancia del juego
        GameState gameState = new GameState();
        
        // Crear un jugador humano
        HumanPlayer humanPlayer = new HumanPlayer("JugadorTest");
        
        // Iniciar un nuevo juego
        gameState.startNewGame(humanPlayer);
        
        System.out.println("1. Estado inicial del juego:");
        System.out.println("   - Jugador: " + gameState.getHumanPlayerNickname());
        System.out.println("   - Fase del juego: " + gameState.getCurrentPhase());
        
        // Guardar el juego
        System.out.println("\n2. Guardando el juego en resources...");
        gameState.saveGame();
        
        // Verificar si se guardó correctamente
        System.out.println("\n3. Verificando si hay juego guardado:");
        if (gameState.isSavedGameAvailable()) {
            System.out.println("   ✓ Hay un juego guardado disponible");
        } else {
            System.out.println("   ✗ No hay juego guardado");
        }
        
        // Crear un nuevo estado y cargar el anterior
        System.out.println("\n4. Creando nuevo estado y cargando el anterior...");
        GameState newGameState = new GameState();
        HumanPlayer newPlayer = new HumanPlayer("NuevoJugador");
        newGameState.startNewGame(newPlayer);
        
        System.out.println("   - Nuevo jugador: " + newGameState.getHumanPlayerNickname());
        
        // Cargar el estado anterior
        if (newGameState.loadGame()) {
            System.out.println("   ✓ Juego cargado exitosamente");
            System.out.println("   - Jugador restaurado: " + newGameState.getHumanPlayerNickname());
            System.out.println("   - Fase restaurada: " + newGameState.getCurrentPhase());
        } else {
            System.out.println("   ✗ No se pudo cargar el juego");
        }
        
        System.out.println("\n=== Fin de la prueba ===");
        System.out.println("Verifica que el archivo se guardó en: src/main/resources/univalle/tedesoft/battleship/saves/game_info.txt");
    }
} 