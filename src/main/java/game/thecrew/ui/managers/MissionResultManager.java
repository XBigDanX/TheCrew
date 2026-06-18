package game.thecrew.ui.managers;

import game.thecrew.GameSession;
import game.thecrew.model.GamePhase;
import game.thecrew.model.MissionStatus;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

public class MissionResultManager {

    private final StackPane missionResultOverlay;
    private final Label resultTitleLabel;
    private final Label resultMessageLabel;
    private final Button nextMissionButton;
    private final Button retryButton;

    public MissionResultManager(StackPane missionResultOverlay, Label resultTitleLabel,
                                 Label resultMessageLabel, Button nextMissionButton,
                                 Button retryButton) {
        this.missionResultOverlay = missionResultOverlay;
        this.resultTitleLabel = resultTitleLabel;
        this.resultMessageLabel = resultMessageLabel;
        this.nextMissionButton = nextMissionButton;
        this.retryButton = retryButton;
    }

    public void handleMissionEnd(GameSession session) {
        if (session == null || session.getEngine() == null) return;

        boolean complete = session.getEngine().getPhase() == GamePhase.MISSION_COMPLETE;

        missionResultOverlay.setVisible(complete);
        missionResultOverlay.setManaged(complete);

        if (!complete) return;

        boolean success = session.getEngine().getCurrentMission().getStatus() == MissionStatus.SUCCESS;

        resultTitleLabel.setText(success ? "Mission Complete!" : "Mission Failed");
        resultMessageLabel.setText(success ? "All tasks were completed." : "Not all tasks were completed.");

        nextMissionButton.setVisible(success);
        nextMissionButton.setManaged(success);
        retryButton.setVisible(!success);
        retryButton.setManaged(!success);
    }
}
