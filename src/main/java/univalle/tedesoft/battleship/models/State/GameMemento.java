package univalle.tedesoft.battleship.models.State;

import univalle.tedesoft.battleship.models.Enums.GamePhase;
import univalle.tedesoft.battleship.models.Players.Player;

import java.time.LocalDateTime;

/**
 * Clase Memento que almacena el estado del juego para poder restaurarlo posteriormente.
 * Implementa el patrón de diseño Memento.
 * 
 * @author Juan Pablo Escamilla
 * @author David Valencia
 * @author Santiago Guerrero
 */
public class GameMemento {
    private final String humanPlayerNickname;
    private final int humanPlayerSunkShips;
    private final int computerPlayerSunkShips;
    private final GamePhase currentPhase;
    private final LocalDateTime saveDateTime;
    
    /**
     * Constructor del Memento
     * @param humanPlayerNickname Nickname del jugador humano
     * @param humanPlayerSunkShips Cantidad de barcos hundidos por el humano
     * @param computerPlayerSunkShips Cantidad de barcos hundidos por la computadora
     * @param currentPhase Fase actual del juego
     */
    public GameMemento(String humanPlayerNickname, int humanPlayerSunkShips, 
                      int computerPlayerSunkShips, GamePhase currentPhase) {
        this.humanPlayerNickname = humanPlayerNickname;
        this.humanPlayerSunkShips = humanPlayerSunkShips;
        this.computerPlayerSunkShips = computerPlayerSunkShips;
        this.currentPhase = currentPhase;
        this.saveDateTime = LocalDateTime.now();
    }
    
    // Getters
    public String getHumanPlayerNickname() {
        return humanPlayerNickname;
    }
    
    public int getHumanPlayerSunkShips() {
        return humanPlayerSunkShips;
    }
    
    public int getComputerPlayerSunkShips() {
        return computerPlayerSunkShips;
    }
    
    public GamePhase getCurrentPhase() {
        return currentPhase;
    }
    
    public LocalDateTime getSaveDateTime() {
        return saveDateTime;
    }
    
    @Override
    public String toString() {
        return String.format("GameMemento{player='%s', humanSunk=%d, computerSunk=%d, phase=%s, saved=%s}",
                humanPlayerNickname, humanPlayerSunkShips, computerPlayerSunkShips, 
                currentPhase, saveDateTime);
    }
} 