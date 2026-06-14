package game.thecrew.model;

import java.io.Serializable;

public enum TokenPosition implements Serializable {
    TOP,    // Highest card of that color
    MIDDLE, // Only card of that color
    BOTTOM  // Lowest card of that color
}
