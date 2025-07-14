package univalle.tedesoft.battleship;

import univalle.tedesoft.battleship.models.Players.HumanPlayer;
import univalle.tedesoft.battleship.models.State.GameState;
import univalle.tedesoft.battleship.models.State.GameMemento;

/**
 * Ejemplo de uso del patrón Memento implementado en el juego Battleship.
 * Esta clase demuestra cómo guardar y cargar el estado del juego.
 * 
 * @author Tu Nombre
 */
public class MementoExample {
    
    public static void main(String[] args) {
        System.out.println("=== Ejemplo del Patrón Memento en Battleship ===\n");
        
        // Crear una instancia del juego
        GameState gameState = new GameState();
        
        // Crear un jugador humano
        HumanPlayer humanPlayer = new HumanPlayer("Jugador1");
        
        // Iniciar un nuevo juego
        gameState.startNewGame(humanPlayer);
        
        System.out.println("1. Estado inicial del juego:");
        System.out.println("   - Jugador: " + gameState.getHumanPlayerNickname());
        System.out.println("   - Fase del juego: " + gameState.getCurrentPhase());
        System.out.println("   - Barcos hundidos por humano: " + gameState.getHumanPlayerSunkShipCount());
        System.out.println("   - Barcos hundidos por computadora: " + gameState.getComputerPlayerSunkShipCount());
        
        // Simular algunos disparos para cambiar el estado
        System.out.println("\n2. Simulando algunos disparos...");
        try {
            // Simular disparos del humano (esto cambiaría el estado)
            // En un juego real, estos disparos modificarían el tablero
            System.out.println("   - Disparo realizado en (0,0)");
            System.out.println("   - Disparo realizado en (1,1)");
        } catch (Exception e) {
            System.out.println("   Error en disparos simulados: " + e.getMessage());
        }
        
        // Guardar el estado del juego usando el patrón Memento
        System.out.println("\n3. Guardando el estado del juego...");
        gameState.saveGame();
        
        // Verificar si hay un juego guardado
        System.out.println("\n4. Verificando si hay juego guardado:");
        if (gameState.isSavedGameAvailable()) {
            System.out.println("   ✓ Hay un juego guardado disponible");
        } else {
            System.out.println("   ✗ No hay juego guardado");
        }
        
        // Crear un nuevo estado de juego
        System.out.println("\n5. Creando un nuevo estado de juego...");
        GameState newGameState = new GameState();
        HumanPlayer newPlayer = new HumanPlayer("Jugador2");
        newGameState.startNewGame(newPlayer);
        
        System.out.println("   - Nuevo jugador: " + newGameState.getHumanPlayerNickname());
        System.out.println("   - Nueva fase: " + newGameState.getCurrentPhase());
        
        // Cargar el estado anterior
        System.out.println("\n6. Cargando el estado anterior...");
        if (newGameState.loadGame()) {
            System.out.println("   ✓ Juego cargado exitosamente");
            System.out.println("   - Jugador restaurado: " + newGameState.getHumanPlayerNickname());
            System.out.println("   - Fase restaurada: " + newGameState.getCurrentPhase());
            System.out.println("   - Barcos hundidos por humano: " + newGameState.getHumanPlayerSunkShipCount());
            System.out.println("   - Barcos hundidos por computadora: " + newGameState.getComputerPlayerSunkShipCount());
        } else {
            System.out.println("   ✗ No se pudo cargar el juego");
        }
        
        // Demostrar la creación manual de un memento
        System.out.println("\n7. Creando un memento manualmente:");
        GameMemento memento = gameState.createMemento();
        System.out.println("   - Memento creado: " + memento);
        
        // Restaurar desde el memento
        System.out.println("\n8. Restaurando desde el memento:");
        gameState.restoreFromMemento(memento);
        System.out.println("   ✓ Estado restaurado desde memento");
        
        System.out.println("\n=== Fin del ejemplo ===");
    }
} 