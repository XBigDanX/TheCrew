package game.thecrew.model;

import java.io.Serializable;

public class PlayerInfo implements Serializable {
    private final String name;
    private final int index;

    public PlayerInfo(String name, int index) {
        this.name = name;
        this.index = index;
    }

    public String getName() {
        return name;
    }

    public int getIndex() {
        return index;
    }
}
