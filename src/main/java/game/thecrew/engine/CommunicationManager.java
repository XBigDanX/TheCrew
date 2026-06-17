package game.thecrew.engine;

import game.thecrew.model.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class CommunicationManager {

    private final List<Player> players;
    private final TrickManager trickManager;
    private CommunicationToken[] pendingTokens;
    private int communicationPlayerIndex = -1;
    private boolean[] communicationRequested;

    public CommunicationManager(List<Player> players, TrickManager trickManager) {
        this.players = players;
        this.trickManager = trickManager;
    }

    public void init(int playerCount) {
        pendingTokens = new CommunicationToken[playerCount];
        communicationRequested = new boolean[playerCount];
        communicationPlayerIndex = -1;
    }

    public int getCommunicationPlayerIndex() {
        return communicationPlayerIndex;
    }

    public boolean isCommunicationRequested(int playerIndex) {
        return communicationRequested[playerIndex];
    }

    public int getNextRequestPlayerIndex() {
        for (int i = 0; i < communicationRequested.length; i++) {
            if (communicationRequested[i]) {
                return i;
            }
        }
        return -1;
    }

    public void startCommunication(int playerIndex) {
        this.communicationPlayerIndex = playerIndex;
    }

    public void cancelCommunication(int playerIndex) {
        this.communicationPlayerIndex = -1;
        this.communicationRequested[playerIndex] = false;
    }

    public void toggleRequest(int playerIndex) {
        this.communicationRequested[playerIndex] = !this.communicationRequested[playerIndex];
    }

    public List<Card> getValidCommunicationCards(int playerIndex, Mission mission) {
        if (mission.hasPlayerUsedToken(playerIndex)) {
            return new ArrayList<>();
        }
        if (trickManager.getCurrentTrick().getPlayerPlay(playerIndex) != null) {
            return new ArrayList<>();
        }

        List<Card> hand = players.get(playerIndex).getHand();
        List<Card> validCards = new ArrayList<>();

        for (Card card : hand) {
            if (card.isTrump()) continue;
            List<Card> sameColorCards = getCardsByColor(hand, card.getColor());
            if (isCommunicationEligible(card, sameColorCards)) {
                validCards.add(card);
            }
        }
        return validCards;
    }

    public List<TokenPosition> getValidPositionsForCard(int playerIndex, Card card) {
        List<TokenPosition> positions = new ArrayList<>();
        List<Card> hand = players.get(playerIndex).getHand();
        List<Card> sameColorCards = getCardsByColor(hand, card.getColor());

        if (sameColorCards.size() == 1) {
            positions.add(TokenPosition.MIDDLE);
        } else {
            int min = minValue(sameColorCards);
            int max = maxValue(sameColorCards);
            if (card.getValue() == min) positions.add(TokenPosition.BOTTOM);
            if (card.getValue() == max) positions.add(TokenPosition.TOP);
        }
        return positions;
    }

    private static List<Card> getCardsByColor(List<Card> hand, CardColor color) {
        List<Card> sameColorCards = new ArrayList<>();
        for (Card c : hand) {
            if (c.getColor() == color) {
                sameColorCards.add(c);
            }
        }
        return sameColorCards;
    }

    private static int minValue(List<Card> cards) {
        return Collections.min(cards, Comparator.comparingInt(Card::getValue)).getValue();
    }

    private static int maxValue(List<Card> cards) {
        return Collections.max(cards, Comparator.comparingInt(Card::getValue)).getValue();
    }

    private static boolean isCommunicationEligible(Card card, List<Card> sameColorCards) {
        if (sameColorCards.size() == 1) {
            return true;
        }
        int min = minValue(sameColorCards);
        int max = maxValue(sameColorCards);
        return card.getValue() == min || card.getValue() == max;
    }

    public TokenPosition resolveCommunicationPosition(int playerIndex, Card card) {
        List<TokenPosition> positions = getValidPositionsForCard(playerIndex, card);
        if (positions.isEmpty()) return null;
        if (positions.contains(TokenPosition.MIDDLE)) return TokenPosition.MIDDLE;
        return positions.get(0);
    }

    public boolean selectCommunicationCard(int playerIndex, Card card, TokenPosition position, Mission mission) {
        if (playerIndex != communicationPlayerIndex) {
            return false;
        }
        if (mission.hasPlayerUsedToken(playerIndex)) {
            return false;
        }
        if (trickManager.getCurrentTrick().getPlayerPlay(playerIndex) != null) {
            return false;
        }
        List<Card> validCards = getValidCommunicationCards(playerIndex, mission);
        if (!validCards.contains(card)) {
            return false;
        }
        List<TokenPosition> validPositions = getValidPositionsForCard(playerIndex, card);
        if (!validPositions.contains(position)) {
            return false;
        }

        pendingTokens[playerIndex] = new CommunicationToken(card, position, playerIndex);
        mission.setPlayerUsedToken(playerIndex, true);

        communicationRequested[playerIndex] = false;
        communicationPlayerIndex = -1;
        return true;
    }

    public void applyPendingTokens(Mission mission) {
        if (pendingTokens == null) return;
        for (int i = 0; i < pendingTokens.length; i++) {
            if (pendingTokens[i] != null) {
                mission.addActiveToken(pendingTokens[i]);
                pendingTokens[i] = null;
            }
        }
    }

    public void removeActiveToken(int playerIndex, Mission mission) {
        mission.removeActiveToken(playerIndex);
    }

    public CommunicationToken[] getPendingTokens() {
        return pendingTokens;
    }

    public void reset() {
        if (pendingTokens != null) {
            pendingTokens = new CommunicationToken[pendingTokens.length];
        }
        if (communicationRequested != null) {
            communicationRequested = new boolean[communicationRequested.length];
        }
        communicationPlayerIndex = -1;
    }
}
