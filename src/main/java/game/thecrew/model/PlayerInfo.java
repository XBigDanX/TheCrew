package game.thecrew.model;

import java.io.Serializable;

public class PlayerInfo implements Serializable {
    private final String name;
    private final int index;
    private final int totalPlayers;

    public PlayerInfo(String name, int index) {
        this(name, index, 0);
    }

    public PlayerInfo(String name, int index, int totalPlayers) {
        this.name = name;
        this.index = index;
        this.totalPlayers = totalPlayers;
    }

    public String getName() {
        return name;
    }

    public int getIndex() {
        return index;
    }

    public int getTotalPlayers() {
        return totalPlayers;
    }
}
