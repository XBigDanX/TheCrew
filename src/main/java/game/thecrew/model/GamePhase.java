package game.thecrew.model;

import java.io.Serializable;

public enum GamePhase implements Serializable {
    TASK_SELECTION,
    COMMUNICATION,
    TRICKING,
    MISSION_COMPLETE,
    GAME_OVER
}