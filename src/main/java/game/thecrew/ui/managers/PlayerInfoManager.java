package game.thecrew.ui.managers;

import game.thecrew.GameSession;
import game.thecrew.model.Mission;
import game.thecrew.model.Player;
import javafx.scene.control.Label;

import java.util.List;

public class PlayerInfoManager {

    private final Label[] infoLabels;

    public PlayerInfoManager(Label[] infoLabels) {
        this.infoLabels = infoLabels;
    }

    public void updateInfoLabels(GameSession session, int playerCount) {
        if (session == null || session.getEngine() == null) return;

        Mission mission = session.getEngine().getCurrentMission();
        if (mission == null) {
            setAllLabels("Waiting for game start...", playerCount);
            return;
        }

        List<Player> players = session.getEngine().getPlayerManager().getPlayers();
        for (int i = 0; i < playerCount; i++) {
            setInfoLabel(i, players, mission);
        }
    }

    private void setAllLabels(String text, int playerCount) {
        for (int i = 0; i < Math.min(playerCount, infoLabels.length); i++) {
            if (infoLabels[i] != null) infoLabels[i].setText(text);
        }
    }

    private void setInfoLabel(int i, List<Player> players, Mission mission) {
        if (i >= infoLabels.length || infoLabels[i] == null) return;
        if (i >= players.size()) {
            infoLabels[i].setText("Waiting for player...");
            return;
        }

        Player player = players.get(i);
        String suffix = (i == mission.getCaptainIndex()) ? "  [Captain]" : "";
        infoLabels[i].setText("Player " + (i + 1)
            + " | Tricks: " + mission.getPlayerWinCount(i)
            + "  Cards: " + player.getHand().size()
            + suffix);
    }
    public void setupVisibility(int playerCount) {
        for (int i = 0; i < infoLabels.length; i++) {
            boolean active = i < playerCount;
            if (infoLabels[i] != null) {
                infoLabels[i].setVisible(active);
                infoLabels[i].setManaged(active);
            }
        }
    }
}
