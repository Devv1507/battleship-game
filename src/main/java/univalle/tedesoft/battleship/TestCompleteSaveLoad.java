package univalle.tedesoft.battleship;

import univalle.tedesoft.battleship.models.Players.HumanPlayer;
import univalle.tedesoft.battleship.models.State.GameState;
import univalle.tedesoft.battleship.models.Enums.ShipType;
import univalle.tedesoft.battleship.models.Enums.Orientation;

/**
 * Clase de prueba para verificar la serialización completa del juego.
 * 
 * @author Tu Nombre
 */
public class TestCompleteSaveLoad {
    
    public static void main(String[] args) {
        System.out.println("=== Prueba de Serialización Completa ===\n");
        
        // Crear una instancia del juego
        GameState gameState = new GameState();
        
        // Crear un jugador humano
        HumanPlayer humanPlayer = new HumanPlayer("JugadorCompleto");
        
        // Iniciar un nuevo juego
        gameState.startNewGame(humanPlayer);
        
        System.out.println("1. Estado inicial del juego:");
        System.out.println("   - Jugador: " + gameState.getHumanPlayerNickname());
        System.out.println("   - Fase del juego: " + gameState.getCurrentPhase());
        System.out.println("   - Barcos por colocar: " + gameState.getPendingShipsToPlace().size());
        
        // Simular colocación de algunos barcos
        System.out.println("\n2. Simulando colocación de barcos...");
        try {
            // Colocar un portaaviones horizontalmente en (0,0)
            gameState.placeHumanPlayerShip(ShipType.AIR_CRAFT_CARRIER, 0, 0, Orientation.HORIZONTAL);
            System.out.println("   ✓ Portaaviones colocado en (0,0) horizontal");
            
            // Colocar un submarino verticalmente en (2,2)
            gameState.placeHumanPlayerShip(ShipType.SUBMARINE, 2, 2, Orientation.VERTICAL);
            System.out.println("   ✓ Submarino colocado en (2,2) vertical");
            
            // Colocar un destructor horizontalmente en (5,5)
            gameState.placeHumanPlayerShip(ShipType.DESTROYER, 5, 5, Orientation.HORIZONTAL);
            System.out.println("   ✓ Destructor colocado en (5,5) horizontal");
            
        } catch (Exception e) {
            System.out.println("   Error al colocar barcos: " + e.getMessage());
        }
        
        System.out.println("   - Barcos restantes por colocar: " + gameState.getPendingShipsToPlace().size());
        
        // Guardar el juego completo
        System.out.println("\n3. Guardando el juego completo...");
        gameState.saveGame();
        
        // Verificar si se guardó correctamente
        System.out.println("\n4. Verificando si hay juego guardado:");
        if (gameState.isSavedGameAvailable()) {
            System.out.println("   ✓ Hay un juego guardado disponible");
        } else {
            System.out.println("   ✗ No hay juego guardado");
        }
        
        // Crear un nuevo estado y cargar el anterior
        System.out.println("\n5. Creando nuevo estado y cargando el anterior...");
        GameState newGameState = new GameState();
        HumanPlayer newPlayer = new HumanPlayer("NuevoJugador");
        newGameState.startNewGame(newPlayer);
        
        System.out.println("   - Nuevo jugador: " + newGameState.getHumanPlayerNickname());
        System.out.println("   - Barcos por colocar en nuevo estado: " + newGameState.getPendingShipsToPlace().size());
        
        // Cargar el estado anterior
        if (newGameState.loadGame()) {
            System.out.println("   ✓ Juego cargado exitosamente");
            System.out.println("   - Jugador restaurado: " + newGameState.getHumanPlayerNickname());
            System.out.println("   - Fase restaurada: " + newGameState.getCurrentPhase());
            System.out.println("   - Barcos restantes por colocar: " + newGameState.getPendingShipsToPlace().size());
            
            // Verificar que los barcos se cargaron correctamente
            System.out.println("   - Barcos en tablero humano: " + newGameState.getHumanPlayerPositionBoard().getShips().size());
            System.out.println("   - Barcos en tablero máquina: " + newGameState.getMachinePlayerActualPositionBoard().getShips().size());
            
        } else {
            System.out.println("   ✗ No se pudo cargar el juego");
        }
        
        System.out.println("\n=== Fin de la prueba ===");
        System.out.println("Verifica que los archivos se guardaron en: src/main/resources/univalle/tedesoft/battleship/saves/");
    }
} 