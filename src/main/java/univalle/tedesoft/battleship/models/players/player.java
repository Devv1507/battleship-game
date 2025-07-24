package univalle.tedesoft.battleship.models.players;
/**
 * Clase que representa al jugador.
 * @author David Esteban Valencia
 * @author Santiago David Guerrero
 * @author Juan Pablo Escamilla
 */
public abstract class player {
    /**Nombre del jugador*/
    private String name;
    /**Constructor de la clase*/
    public player(String name) {
        this.name = name;
    }
    /**Metodo para obtener el nombre del jugador*/
    public String getName() {
        return name;
    }
    /**Metodo para obtener el nombre del jugador*/
    public void setName(String name) {
        this.name= name;
    }
}
