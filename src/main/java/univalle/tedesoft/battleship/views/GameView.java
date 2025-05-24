package univalle.tedesoft.battleship.views;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import univalle.tedesoft.battleship.Main;
import univalle.tedesoft.battleship.controllers.GameController;

import java.io.IOException;

public class GameView extends Stage {

    private GameController controller;

    public GameView() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(
                Main.class.getResource("game-view.fxml")
        );
        Scene scene = new Scene(fxmlLoader.load());
        this.controller = fxmlLoader.getController();
        this.setTitle("Craps Game");
        this.setScene(scene);
    }

    public GameController getController() {
        return controller;
    }

    public static GameView getInstance() throws IOException {
        if (GameViewHolder.INSTANCE == null) {
            GameViewHolder.INSTANCE = new GameView();
            return GameViewHolder.INSTANCE;
        } else {
            return GameViewHolder.INSTANCE;
        }
    }

    private static class GameViewHolder {
        private static GameView INSTANCE;
    }

}