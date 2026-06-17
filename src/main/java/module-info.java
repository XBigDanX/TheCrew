module game.thecrew {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.naming;
    requires java.rmi;
    requires java.logging;

    uses javax.naming.spi.InitialContextFactory;

    opens game.thecrew to javafx.fxml;
    exports game.thecrew;
    exports game.thecrew.engine;
    opens game.thecrew.engine to javafx.fxml;
    exports game.thecrew.controllers;
    opens game.thecrew.controllers to javafx.fxml;
    exports game.thecrew.model;
    exports game.thecrew.model.taskrules;
    exports game.thecrew.network;
    exports game.thecrew.network.rmi;
}