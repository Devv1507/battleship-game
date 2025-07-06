module univalle.tedesoft.battleship {
    requires javafx.controls;
    requires javafx.fxml;


    opens univalle.tedesoft.battleship to javafx.fxml;
    opens univalle.tedesoft.battleship.controllers to javafx.fxml;

    exports univalle.tedesoft.battleship;
}