module game.thecrew {
    requires javafx.controls;
    requires javafx.fxml;


    opens game.thecrew to javafx.fxml;
    exports game.thecrew;
    exports game.thecrew.engine;
    opens game.thecrew.engine to javafx.fxml;
    exports game.thecrew.controllers;
    opens game.thecrew.controllers to javafx.fxml;
    exports game.thecrew.model;
    exports game.thecrew.model.taskrules;
}