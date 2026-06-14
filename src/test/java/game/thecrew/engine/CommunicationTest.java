package game.thecrew.engine;

import game.thecrew.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class CommunicationTest {

    private CrewEngine engine;

    @BeforeEach
    void setUp() {
        engine = new CrewEngine();
        engine.createPlayers(3);
        engine.startGame();
        // Skip task selection to enter trick phase
        while (engine.getPhase() == GamePhase.TASK_SELECTION) {
            Mission mission = engine.getCurrentMission();
            Task nextTask = null;
            for (Task t : mission.getTasks()) {
                if (t.getAssignedPlayer() == null) {
                    nextTask = t;
                    break;
                }
            }
            if (nextTask != null) {
                engine.selectTask(engine.getCurrentPlayerIndex(), nextTask);
            } else {
                engine.passTaskSelection(engine.getCurrentPlayerIndex());
            }
        }
    }

    @Test
    void testCommunicationValidation() {
        int playerIndex = 0;
        Player player = engine.getPlayers().get(playerIndex);
        player.getHand().clear();
        
        // Give player some cards
        player.addCardToHand(new Card(CardColor.BLUE, 1));
        player.addCardToHand(new Card(CardColor.BLUE, 5));
        player.addCardToHand(new Card(CardColor.RED, 3)); // Only red card
        player.addCardToHand(new Card(CardColor.SUBMARINE, 1)); // Trump not allowed

        List<Card> validCards = engine.getValidCommunicationCards(playerIndex);
        
        // BLUE 1 (Lowest), BLUE 5 (Highest), RED 3 (Only) should be valid
        // SUBMARINE 1 should NOT be valid
        assertTrue(validCards.contains(new Card(CardColor.BLUE, 1)));
        assertTrue(validCards.contains(new Card(CardColor.BLUE, 5)));
        assertTrue(validCards.contains(new Card(CardColor.RED, 3)));
        assertFalse(validCards.contains(new Card(CardColor.SUBMARINE, 1)));
    }

    @Test
    void testCommunicationFlow() {
        int playerIndex = 0;
        Player player = engine.getPlayers().get(playerIndex);
        player.getHand().clear();
        Card blue1 = new Card(CardColor.BLUE, 1);
        player.addCardToHand(blue1);
        player.addCardToHand(new Card(CardColor.BLUE, 5));

        // Select communication
        engine.requestCommunication(playerIndex);
        assertTrue(engine.selectCommunicationCard(playerIndex, blue1, TokenPosition.BOTTOM));
        
        // Should be in pending
        assertNotNull(engine.getPendingTokens()[playerIndex]);
        assertEquals(blue1, engine.getPendingTokens()[playerIndex].getCard());
        
        // Cannot use token again
        assertFalse(engine.selectCommunicationCard(playerIndex, blue1, TokenPosition.BOTTOM));

        // Start a trick (already in trick phase)
        // Pending tokens are applied at start of next trick or when phase starts.
        // In our implementation, they are applied in startTrickPhase() and after each trick reset.
        // Since we are already in trick phase, we need to complete a trick to see it applied.
        
        // Actually, let's check if it was applied when we entered TRICKING phase.
        // Wait, in my setUp I finished TASK_SELECTION, which calls startTrickPhase().
        // If I communicated BEFORE that, it would have been applied.
        // But I communicated AFTER. So it stays pending until next trick.
    }
}
