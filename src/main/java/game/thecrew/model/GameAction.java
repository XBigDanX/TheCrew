package game.thecrew.model;

import java.io.Serializable;

public class GameAction implements Serializable {
    public enum ActionType {
        PLAY_CARD,
        SELECT_TASK,
        PASS_TASK_SELECTION,
        REQUEST_COMMUNICATION,
        SELECT_COMMUNICATION_CARD,
        DISMISS_COMMUNICATION,
        LOAD_GAME,
        NEXT_MISSION,
        RETRY_MISSION
    }

    private final int playerIndex;
    private final ActionType type;
    private final Serializable payload;

    public GameAction(int playerIndex, ActionType type, Serializable payload) {
        this.playerIndex = playerIndex;
        this.type = type;
        this.payload = payload;
    }

    public int getPlayerIndex() { return playerIndex; }
    public ActionType getType() { return type; }
    public Serializable getPayload() { return payload; }

    @Override
    public String toString() {
        return "GameAction{" +
                "playerIndex=" + playerIndex +
                ", type=" + type +
                ", payload=" + payload +
                '}';
    }
}
