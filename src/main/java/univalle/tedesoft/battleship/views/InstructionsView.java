// ./src/main/java/univalle/tedesoft/battleship/views/InstructionsView.java
package univalle.tedesoft.battleship.views;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import univalle.tedesoft.battleship.Main;

import java.io.IOException;

/**
 * Esta clase representa la vista de instrucciones del juego Battleship.
 * Muestra las reglas básicas del juego en un formato legible y estilizado.
 * Utiliza el patrón Singleton para asegurar que solo exista una instancia
 * de esta ventana en la aplicación.
 */
public class InstructionsView extends Stage {
    /**
     * Clase interna estática (Holder) para implementar el patrón Singleton de forma segura.
     */
    private static class InstructionsViewHolder {
        /** Instancia única de InstructionsView. */
        private static InstructionsView INSTANCE;
    }

    /**
     * Devuelve la instancia única de InstructionsView. Si no existe, la crea.
     *
     * @return la instancia singleton de InstructionsView.
     * @throws IOException si ocurre un error al cargar el archivo FXML durante la primera creación.
     */
    public static InstructionsView getInstance() throws IOException {
        if (InstructionsViewHolder.INSTANCE == null) {
            InstructionsViewHolder.INSTANCE = new InstructionsView();
        }
        return InstructionsViewHolder.INSTANCE;
    }

    /**
     * Constructor privado para prevenir la instanciación directa y forzar el uso de getInstance().
     * Carga la vista desde el archivo FXML, establece la escena y configura el título de la ventana.
     *
     * @throws IOException si falla la carga del archivo FXML.
     */
    private InstructionsView() throws IOException {
        FXMLLoader loader = new FXMLLoader(Main.class.getResource("instructions-view.fxml"));
        Scene scene = new Scene(loader.load());

        this.setTitle("Battleship - Instrucciones del Juego");
        this.setScene(scene);
    }
}
