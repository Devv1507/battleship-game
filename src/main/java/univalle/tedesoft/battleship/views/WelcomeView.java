package univalle.tedesoft.battleship.views;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import univalle.tedesoft.battleship.Main;
import univalle.tedesoft.battleship.controllers.WelcomeController;

import java.io.IOException;

/**
 * Representa la vista de bienvenida del juego Battleship.
 * Es la primera ventana que ve el usuario, donde ingresa su nombre para comenzar.
 */
public class WelcomeView extends Stage {

    private final WelcomeController controller;

    private static class WelcomeViewHolder {
        private static WelcomeView INSTANCE;
    }

    /**
     * Devuelve la instancia única de WelcomeView, creándola si es necesario.
     * @return La instancia singleton de WelcomeView.
     * @throws IOException Si ocurre un error al cargar el archivo FXML.
     */
    public static WelcomeView getInstance() throws IOException {
        if (WelcomeViewHolder.INSTANCE == null) {
            WelcomeViewHolder.INSTANCE = new WelcomeView();
        }
        return WelcomeViewHolder.INSTANCE;
    }

    /**
     * Constructor privado para implementar el patrón Singleton.
     * Carga la vista desde el archivo FXML y establece la comunicación con su controlador.
     * @throws IOException Si falla la carga del FXML.
     */
    private WelcomeView() throws IOException {
        try {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("welcome-view.fxml"));
            Scene scene = new Scene(loader.load());
            this.controller = loader.getController();

            if (this.controller == null) {
                throw new IOException("No se pudo obtener el WelcomeController desde el FXML.");
            }

            this.controller.setWelcomeView(this);

            this.setTitle("Battleship - Puesto de Mando");
            this.setScene(scene);
            
        } catch (Exception e) {
            System.err.println("ERROR al inicializar WelcomeView: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
}
