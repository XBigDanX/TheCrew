package game.thecrew.mission;

import game.thecrew.model.Card;
import game.thecrew.model.CardColor;
import game.thecrew.model.Task;
import game.thecrew.model.WinSpecificCardRule;

import java.util.List;

public class TaskLibrary {

    private TaskLibrary() {
    }

    public static List<Task> getAllTasks() {
        return List.of(
                new Task("Win Yellow 1", 2, 3, 3, new WinSpecificCardRule(new Card(CardColor.YELLOW, 1))),
                new Task("Win Red 5", 2, 2, 2, new WinSpecificCardRule(new Card(CardColor.RED, 5))),
                new Task("Win Blue 8", 1, 1, 1, new WinSpecificCardRule(new Card(CardColor.BLUE, 8)))
        );
    }
}
