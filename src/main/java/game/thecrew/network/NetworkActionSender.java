package game.thecrew.network;

import game.thecrew.model.Card;
import game.thecrew.model.GameAction;
import game.thecrew.model.GameState;
import game.thecrew.model.Task;
import game.thecrew.model.TokenPosition;

public class NetworkActionSender {

    private final GameNetworkClient client;

    public NetworkActionSender(GameNetworkClient client) {
        this.client = client;
    }

    public void playCard(int playerIndex, Card card) {
        client.sendAction(new GameAction(playerIndex, GameAction.ActionType.PLAY_CARD, card));
    }

    public void selectTask(int playerIndex, Task task) {
        client.sendAction(new GameAction(playerIndex, GameAction.ActionType.SELECT_TASK, task));
    }

    public void passTaskSelection(int playerIndex) {
        client.sendAction(new GameAction(playerIndex, GameAction.ActionType.PASS_TASK_SELECTION, null));
    }

    public void requestCommunication(int playerIndex) {
        client.sendAction(new GameAction(playerIndex, GameAction.ActionType.REQUEST_COMMUNICATION, null));
    }

    public void selectCommunicationCard(int playerIndex, Card card, TokenPosition tokenPosition) {
        client.sendAction(
            new GameAction(playerIndex, GameAction.ActionType.SELECT_COMMUNICATION_CARD, new Object[]{card, tokenPosition})
        );
    }

    public void dismissCommunication(int playerIndex) {
        client.sendAction(new GameAction(playerIndex, GameAction.ActionType.DISMISS_COMMUNICATION, null));
    }

    public void loadGame(int playerIndex, GameState gameState) {
        client.sendAction(new GameAction(playerIndex, GameAction.ActionType.LOAD_GAME, gameState));
    }

    public void nextMission(int playerIndex) {
        client.sendAction(new GameAction(playerIndex, GameAction.ActionType.NEXT_MISSION, null));
    }

    public void retryMission(int playerIndex) {
        client.sendAction(new GameAction(playerIndex, GameAction.ActionType.RETRY_MISSION, null));
    }
}
