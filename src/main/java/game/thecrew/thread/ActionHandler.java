package game.thecrew.thread;

import game.thecrew.GameSession;
import game.thecrew.model.Card;
import game.thecrew.model.GameAction;
import game.thecrew.model.GameState;
import game.thecrew.model.Mission;
import game.thecrew.model.MissionStatus;
import game.thecrew.model.Task;
import game.thecrew.model.TokenPosition;
import game.thecrew.network.rmi.MissionServiceImpl;

import java.rmi.RemoteException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ActionHandler {

    private static final Logger LOGGER = Logger.getLogger(ActionHandler.class.getName());

    private GameSession session;
    private final ClientManager clientManager;
    private final MissionServiceImpl missionService;
    private final int maxPlayers;

    public ActionHandler(GameSession session, ClientManager clientManager, MissionServiceImpl missionService, int maxPlayers) {
        this.session = session;
        this.clientManager = clientManager;
        this.missionService = missionService;
        this.maxPlayers = maxPlayers;
    }

    public void setSession(GameSession session) {
        this.session = session;
    }

    public synchronized void handleAction(GameAction action) {
        LOGGER.log(Level.INFO, "[DEBUG_LOG] Processing action: {0}", action);
        boolean success;
        switch (action.getType()) {
            case PLAY_CARD -> {
                success = handlePlayCard(action);
                if (success) return;
            }
            case SELECT_TASK ->
                success = session.getEngine().selectTask(action.getPlayerIndex(), (Task) action.getPayload());
            case PASS_TASK_SELECTION ->
                success = session.getEngine().passTaskSelection(action.getPlayerIndex());
            case REQUEST_COMMUNICATION, SELECT_COMMUNICATION_CARD, DISMISS_COMMUNICATION ->
                success = handleCommunicationAction(action);
            case LOAD_GAME, NEXT_MISSION, RETRY_MISSION ->
                success = handleAdminAction(action);
            default -> success = false;
        }
        if (success) {
            clientManager.broadcastState(session.getEngine().saveState());
        } else {
            LOGGER.log(Level.INFO, "[DEBUG_LOG] Action failed: {0}", action);
        }
    }

    private boolean handlePlayCard(GameAction action) {
        if (action.getPlayerIndex() != session.getEngine().getPlayerManager().getCurrentPlayerIndex()) {
            LOGGER.log(Level.WARNING, "Out of turn play attempted");
            return false;
        }
        boolean success = session.getEngine().playCard(action.getPlayerIndex(), (Card) action.getPayload());
        if (success) {
            clientManager.broadcastState(session.getEngine().saveState());
            processPostPlayState();
        }
        return success;
    }

    private void processPostPlayState() {
        Mission mission = session.getEngine().getCurrentMission();
        if ((mission.getStatus() == MissionStatus.SUCCESS || mission.getStatus() == MissionStatus.FAILED) && missionService != null) {
            try {
                missionService.logMissionCompletion(mission.getId(), mission.getStatus() == MissionStatus.SUCCESS, "Server");
            } catch (RemoteException e) {
                LOGGER.log(Level.WARNING, "[RMI] Failed to log mission completion: {0}", e.getMessage());
            }
        }
        if (session.getEngine().getTrickManager().isComplete(maxPlayers)) {
            int winnerIndex = session.getEngine().getTrickManager().getWinner();
            clientManager.broadcastMessage("TRICK_WINNER:" + winnerIndex);
            new Thread(() -> {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                session.getEngine().resetTrick();
                clientManager.broadcastState(session.getEngine().saveState());
            }).start();
        }
    }

    private boolean handleCommunicationAction(GameAction action) {
        return switch (action.getType()) {
            case REQUEST_COMMUNICATION -> {
                session.getEngine().requestCommunication(action.getPlayerIndex());
                yield true;
            }
            case SELECT_COMMUNICATION_CARD -> {
                Object[] p = (Object[]) action.getPayload();
                boolean success = session.getEngine().selectCommunicationCard(action.getPlayerIndex(), (Card) p[0], (TokenPosition) p[1]);
                if (success) {
                    session.getEngine().applyPendingTokens();
                }
                yield success;
            }
            case DISMISS_COMMUNICATION -> {
                session.getEngine().removeActiveToken(action.getPlayerIndex());
                yield true;
            }
            default -> throw new IllegalArgumentException("Unexpected communication action: " + action.getType());
        };
    }

    private boolean handleAdminAction(GameAction action) {
        return switch (action.getType()) {
            case LOAD_GAME -> {
                session.getEngine().restoreState((GameState) action.getPayload());
                yield true;
            }
            case NEXT_MISSION -> {
                session.getEngine().advanceToNextMission();
                yield true;
            }
            case RETRY_MISSION -> {
                session.getEngine().restartCurrentMission();
                yield true;
            }
            default -> throw new IllegalArgumentException("Unexpected admin action: " + action.getType());
        };
    }
}
