package game.thecrew.ui.managers;

import game.thecrew.GameSession;
import game.thecrew.model.GamePhase;
import game.thecrew.model.MissionStatus;
import javafx.animation.PauseTransition;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

public class MissionResultManager {

    private final StackPane missionResultOverlay;
    private final Label resultTitleLabel;
    private final Label resultMessageLabel;
    private final Button nextMissionButton;
    private final Button retryButton;

    private PauseTransition currentDelay;

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

        GamePhase phase = session.getEngine().getPhase();
        boolean finished = (phase == GamePhase.MISSION_COMPLETE || phase == GamePhase.GAME_OVER);

        if (!finished) {
            if (currentDelay != null) {
                currentDelay.stop();
                currentDelay = null;
            }
            missionResultOverlay.setVisible(false);
            missionResultOverlay.setManaged(false);
            return;
        }

        if (currentDelay != null) return; // Delay already in progress

        boolean success = session.getEngine().getCurrentMission().getStatus() == MissionStatus.SUCCESS;
        String title = success ? "Mission Complete!" : "Mission Failed";
        String message = success ? "All tasks were completed." : "Not all tasks were completed.";

        currentDelay = new PauseTransition(Duration.seconds(2));
        currentDelay.setOnFinished(e -> {
            currentDelay = null;
            // Final check before showing
            GamePhase currentPhase = session.getEngine().getPhase();
            if (currentPhase == GamePhase.MISSION_COMPLETE || currentPhase == GamePhase.GAME_OVER) {
                missionResultOverlay.setVisible(true);
                missionResultOverlay.setManaged(true);
                resultTitleLabel.setText(title);
                resultMessageLabel.setText(message);
                nextMissionButton.setVisible(success);
                nextMissionButton.setManaged(success);
                retryButton.setVisible(!success);
                retryButton.setManaged(!success);
            }
        });
        currentDelay.play();
    }
}
