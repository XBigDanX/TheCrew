package game.thecrew.model;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class GameState implements Serializable {

    public int missionId;
    public MissionStatus status;
    public int currentMissionNumber;
    public GamePhase phase;
    public int currentPlayerIndex;
    public int captainIndex;
    public int playersProcessed;

    public Map<Integer, List<Card>> playerHands;

    public List<TaskSnapshot> tasks;

    public boolean[] communicationUsed;
    public int communicationPlayerIndex = -1;
    public boolean[] communicationRequested;
    public List<CommunicationToken> activeTokens;

    public List<Trick> completedTricks;
    public Trick currentTrick;

    public GameState() {
    }
}
