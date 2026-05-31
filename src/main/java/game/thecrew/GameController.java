package game.thecrew;

import game.thecrew.model.*;
import game.thecrew.ui.CardView;
import javafx.fxml.FXML;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;

import java.util.ArrayList;
import java.util.List;

public class GameController {
    // =========================
    // CENTER UI
    // =========================
    @FXML private Pane centerPane;

    @FXML private HBox taskPane;
    @FXML private Pane trickPane;

    // =========================
    // HANDS (UI)
    // =========================
    @FXML private HBox hand0;
    @FXML private HBox hand1;
    @FXML private HBox hand2;
    @FXML private HBox hand3;
    @FXML private HBox hand4;

    private HBox[] hands;

    // =========================
    // SLOTS (CENTER TRICKS)
    // =========================
    @FXML private Pane slot0;
    @FXML private Pane slot1;
    @FXML private Pane slot2;
    @FXML private Pane slot3;
    @FXML private Pane slot4;

    private Pane[] slots;

    @FXML private HBox taskHand0;
    @FXML private HBox taskHand1;
    @FXML private HBox taskHand2;
    @FXML private HBox taskHand3;
    @FXML private HBox taskHand4;

    private HBox[] taskHands;

    // =========================
    // GAME DATA
    // =========================
    private final List<Player> players = new ArrayList<>();
    private final int playerCount = 2; // change 2–5
    private final List<Task> tasks = new ArrayList<>();

    private GamePhase phase = GamePhase.TASK_SELECTION;


    // =========================
    // INIT
    // =========================
    @FXML
    public void initialize() {

        hands = new HBox[]{hand0, hand1, hand2, hand3, hand4};
        slots = new Pane[]{slot0, slot1, slot2, slot3, slot4};
        taskHands = new HBox[]{taskHand0, taskHand1, taskHand2, taskHand3, taskHand4};
        setupPlayerViews();
        createPlayers();
        addTestCards();
        renderAllHands();

        showTaskPhase();
        generateTasks();

    }

    // =========================
    // UI SETUP
    // =========================
    private void setupPlayerViews() {

        for (int i = 0; i < hands.length; i++) {

            boolean active = i < playerCount;

            hands[i].setVisible(active);
            hands[i].setManaged(active);

            slots[i].setVisible(active);
            slots[i].setManaged(active);

        }
    }

    // =========================
    // PLAYERS
    // =========================
    private void createPlayers() {

        players.clear();

        for (int i = 0; i < playerCount; i++) {
            players.add(new Player("Player " + (i + 1)));
        }
    }

    private void addTestCards() {

        if (playerCount >= 1) {
            players.get(0).addCard(new Card(CardColor.BLUE, 5));
            players.get(0).addCard(new Card(CardColor.GREEN, 2));
        }

        if (playerCount >= 2) {
            players.get(1).addCard(new Card(CardColor.RED, 7));
            players.get(1).addCard(new Card(CardColor.YELLOW, 9));
        }

        if (playerCount >= 3) {
            players.get(2).addCard(new Card(CardColor.GREEN, 8));
            players.get(2).addCard(new Card(CardColor.BLUE, 1));
        }

        if (playerCount >= 4) {
            players.get(3).addCard(new Card(CardColor.YELLOW, 4));
            players.get(3).addCard(new Card(CardColor.RED, 3));
        }

        if (playerCount >= 5) {
            players.get(4).addCard(new Card(CardColor.SUBMARINE, 4));
            players.get(4).addCard(new Card(CardColor.BLUE, 9));
        }
    }

    // =========================
    // RENDER HANDS
    // =========================
    private void renderAllHands() {

        for (int i = 0; i < playerCount; i++) {
            renderPlayerHand(i);
        }
    }

    private void renderPlayerHand(int playerIndex) {

        Player player = players.get(playerIndex);
        HBox hand = hands[playerIndex];

        hand.getChildren().clear();

        for (Card card : player.getHand()) {

            CardView cardView = new CardView(card);

            cardView.setOnMouseClicked(e ->
                    playCard(playerIndex, card)
            );

            hand.getChildren().add(cardView);
        }
    }

    // =========================
    // GAME LOGIC
    // =========================
    private void playCard(int playerIndex, Card card) {

        Player player = players.get(playerIndex);

        player.getHand().remove(card);

        hands[playerIndex].getChildren().clear();
        renderPlayerHand(playerIndex);

        slots[playerIndex].getChildren().clear();
        slots[playerIndex].getChildren().add(new CardView(card));
    }

    // =========================
    // PHASE SYSTEM
    // =========================
    private void showTaskPhase() {

        phase = GamePhase.TASK_SELECTION;

        taskPane.setVisible(true);
        taskPane.setManaged(true);

        trickPane.setVisible(false);
        trickPane.setManaged(false);
    }

    private void showTrickPhase() {

        phase = GamePhase.TRICKING;

        taskPane.setVisible(false);
        taskPane.setManaged(false);

        trickPane.setVisible(true);
        trickPane.setManaged(true);
    }

    private void generateTasks() {

        tasks.clear();

        tasks.add(new Task("Win Yellow 1"));
        tasks.add(new Task("Win Blue 5"));

        renderTasks();
    }

    private void renderTasks() {

        taskPane.getChildren().clear();

        for (Task task : tasks) {

            Pane taskView = new Pane();

            taskView.setPrefSize(80, 100);
            taskView.setStyle("-fx-background-color: white;");

            javafx.scene.text.Text text =
                    new javafx.scene.text.Text(task.getDescription());

            text.setLayoutX(10);
            text.setLayoutY(50);

            taskView.getChildren().add(text);

            taskView.setOnMouseClicked(e ->
                    selectTask(0, task)
            );

            taskPane.getChildren().add(taskView);
        }
    }

    private void selectTask(int playerIndex, Task task) {

        tasks.remove(task);

        renderTasks();

        Pane taskView = new Pane();

        taskView.setPrefSize(80, 100);
        taskView.setStyle("-fx-background-color: lightgreen;");

        javafx.scene.text.Text text =
                new javafx.scene.text.Text(task.getDescription());

        text.setLayoutX(10);
        text.setLayoutY(50);

        taskView.getChildren().add(text);

        taskHands[playerIndex].getChildren().add(taskView);

        if (tasks.isEmpty()) {
            showTrickPhase();
        }
    }
}