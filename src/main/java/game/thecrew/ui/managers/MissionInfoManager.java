package game.thecrew.ui.managers;

import game.thecrew.GameApplication;
import game.thecrew.GameSession;
import game.thecrew.model.GamePhase;
import game.thecrew.model.Mission;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;

public class MissionInfoManager {

    private final Label missionLabel;
    private final Label difficultyLabel;
    private final Label missionDescriptionLabel;
    private final Label currentPlayerLabel;
    private final Pane taskPane;
    private final Pane trickPane;
    private final Button passTaskSelectionButton;

    public MissionInfoManager(Label missionLabel, Label difficultyLabel, Label missionDescriptionLabel,
                              Label currentPlayerLabel, Pane taskPane, Pane trickPane,
                              Button passTaskSelectionButton) {
        this.missionLabel = missionLabel;
        this.difficultyLabel = difficultyLabel;
        this.missionDescriptionLabel = missionDescriptionLabel;
        this.currentPlayerLabel = currentPlayerLabel;
        this.taskPane = taskPane;
        this.trickPane = trickPane;
        this.passTaskSelectionButton = passTaskSelectionButton;
    }

    public void updateMissionLabels(GameSession session) {
        if (session == null || session.getEngine() == null) return;

        Mission mission = session.getEngine().getCurrentMission();
        if (mission == null) {
            missionLabel.setText("Mission: ?");
            difficultyLabel.setText("Difficulty: ?");
            missionDescriptionLabel.setText("Waiting for all players...");
            return;
        }

        missionLabel.setText("Mission " + session.getEngine().getCurrentMissionNumber());
        difficultyLabel.setText("Difficulty: " + mission.getDifficulty());
        missionDescriptionLabel.setText(mission.getDescription());
    }

    public void updateCurrentPlayerLabel(GameSession session) {
        if (session == null || session.getEngine() == null
            || session.getEngine().getPlayerManager() == null) return;

        int idx = session.getEngine().getPlayerManager().getCurrentPlayerIndex();
        currentPlayerLabel.setText("Current Turn: Player " + (idx + 1));
    }

    public void updatePhasePanels(GameSession session) {
        boolean taskPhase = session != null && session.getEngine() != null
            && session.getEngine().getPhase() == GamePhase.TASK_SELECTION;

        taskPane.setVisible(taskPhase);
        taskPane.setManaged(taskPhase);
        passTaskSelectionButton.setVisible(taskPhase);
        passTaskSelectionButton.setManaged(taskPhase);

        if (taskPhase) {
            boolean myTurn = GameApplication.getPlayerInfo() != null
                && session.getEngine().getPlayerManager().getCurrentPlayerIndex()
                    == GameApplication.getPlayerInfo().getIndex();
            passTaskSelectionButton.setDisable(!myTurn);
        }

        trickPane.setVisible(!taskPhase);
        trickPane.setManaged(!taskPhase);
    }

    public void showTrickWinner(int winnerId) {
        currentPlayerLabel.setText("Player " + (winnerId + 1) + " won the trick!");
    }
}
