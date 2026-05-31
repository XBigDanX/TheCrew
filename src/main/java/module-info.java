module game.thecrew {
    requires javafx.controls;
    requires javafx.fxml;


    opens game.thecrew to javafx.fxml;
    exports game.thecrew;
}