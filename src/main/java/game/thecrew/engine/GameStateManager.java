package game.thecrew.engine;

import game.thecrew.mission.MissionLibrary;
import game.thecrew.model.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GameStateManager {

    private final CrewEngine engine;

    public GameStateManager(CrewEngine engine) {
        this.engine = engine;
    }

    public GameState saveState() {
        GameState state = new GameState();

        Mission mission = engine.getCurrentMission();
        state.setMissionId(mission.getId());
        state.setCurrentMissionNumber(engine.getCurrentMissionNumber());
        state.setStatus(mission.getStatus());
        state.setPhase(engine.getPhase());
        state.setCurrentPlayerIndex(engine.getPlayerManager().getCurrentPlayerIndex());
        state.setCaptainIndex(engine.getPlayerManager().getCaptainIndex());
        if (engine.taskManager != null) {
            state.setPlayersProcessed(engine.taskManager.getPlayersProcessed());
        }

        Map<Integer, List<Card>> playerHands = new HashMap<>();
        for (int i = 0; i < engine.getPlayerManager().getPlayers().size(); i++) {
            playerHands.put(i, new ArrayList<>(engine.getPlayerManager().getPlayers().get(i).getHand()));
        }
        state.setPlayerHands(playerHands);

        List<Task> missionTasks = mission.getTasks();
        List<TaskSnapshot> taskSnapshots = new ArrayList<>();
        for (int i = 0; i < missionTasks.size(); i++) {
            Task task = missionTasks.get(i);
            taskSnapshots.add(new TaskSnapshot(i, task.getAssignedPlayer(), task.isCompleted()));
        }
        state.setTasks(taskSnapshots);

        state.setCompletedTricks(new ArrayList<>(mission.getCompletedTricks()));

        state.setCurrentTrick(engine.getTrickManager().getCurrentTrick());

        int playerCount = engine.getPlayerManager().getPlayers().size();
        state.setCommunicationUsed(new boolean[playerCount]);
        for (int i = 0; i < playerCount; i++) {
            state.getCommunicationUsed()[i] = mission.hasPlayerUsedToken(i);
        }

        state.setActiveTokens(new ArrayList<>(mission.getActiveTokens()));

        state.setCommunicationPlayerIndex(engine.getCommunicationManager().getCommunicationPlayerIndex());
        state.setCommunicationRequested(new boolean[playerCount]);
        for (int i = 0; i < playerCount; i++) {
            state.getCommunicationRequested()[i] = engine.getCommunicationManager().isCommunicationRequested(i);
        }

        return state;
    }

    public void restoreState(GameState state) {
        int playerCount = engine.getPlayerManager().getPlayers().size();

        restoreMissions(state, playerCount);

        Mission freshMission = engine.missions.get(state.getMissionId() - 1);

        restoreTasks(state, freshMission);
        restorePlayerHands(state);
        restoreTrickAndPhase(state, freshMission);
        restoreCommunicationState(state, freshMission, playerCount);
    }

    private void restoreMissions(GameState state, int playerCount) {
        engine.missions.clear();
        engine.playerManager.setCaptainIndex(state.getCaptainIndex());
        for (int missionId = 1; missionId <= 32; missionId++) {
            Mission mission = MissionLibrary.forPlayerCount(missionId, playerCount);
            mission.setCaptainIndex(engine.playerManager.getCaptainIndex());
            engine.missions.add(mission);
        }

        engine.currentMissionIndex = state.getMissionId() - 1;
        Mission freshMission = engine.missions.get(engine.currentMissionIndex);
        freshMission.setStatus(state.getStatus());

        for (Mission mission : engine.missions) {
            mission.setCaptainIndex(engine.playerManager.getCaptainIndex());
        }
    }

    private void restoreTasks(GameState state, Mission freshMission) {
        for (int i = 0; i < engine.getPlayerManager().getPlayers().size(); i++) {
            engine.getPlayerManager().getPlayers().get(i).getTaskHand().clear();
        }

        List<Task> freshTasks = freshMission.getTasks();
        for (TaskSnapshot snapshot : state.getTasks()) {
            if (snapshot.getTaskIndex() >= 0 && snapshot.getTaskIndex() < freshTasks.size()) {
                Task task = freshTasks.get(snapshot.getTaskIndex());
                if (snapshot.getAssignedPlayer() != null) {
                    task.assignPlayer(snapshot.getAssignedPlayer());
                    engine.getPlayerManager().getPlayers().get(snapshot.getAssignedPlayer()).addTask(task);
                }
                if (snapshot.isCompleted()) {
                    task.markCompleted();
                }
            }
        }
    }

    private void restorePlayerHands(GameState state) {
        for (int i = 0; i < engine.getPlayerManager().getPlayers().size(); i++) {
            engine.getPlayerManager().getPlayers().get(i).getHand().clear();
            List<Card> savedHand = state.getPlayerHands().get(i);
            if (savedHand != null) {
                for (Card card : savedHand) {
                    engine.getPlayerManager().getPlayers().get(i).addCardToHand(card);
                }
            }
        }
    }

    private void restoreTrickAndPhase(GameState state, Mission freshMission) {
        engine.playerManager.setCaptainIndex(state.getCaptainIndex());
        engine.playerManager.setCurrentPlayerIndex(state.getCurrentPlayerIndex());
        engine.phase = state.getPhase();

        freshMission.getCompletedTricks().clear();
        freshMission.getCompletedTricks().addAll(state.getCompletedTricks());

        engine.getTrickManager().setCurrentTrick(state.getCurrentTrick());

        engine.cardsPlayedInMission = state.getCompletedTricks().size() * engine.getPlayerManager().getPlayers().size()
                + state.getCurrentTrick().getPlays().size();

        if (engine.phase == GamePhase.TASK_SELECTION) {
            engine.taskManager = new TaskSelectionManager(engine.getPlayerManager().getPlayers(), freshMission.getTasks());
            engine.taskManager.setPlayersProcessed(state.getPlayersProcessed());
        }
    }

    private void restoreCommunicationState(GameState state, Mission freshMission, int playerCount) {
        for (int i = 0; i < engine.getPlayerManager().getPlayers().size(); i++) {
            freshMission.setPlayerUsedToken(i, state.getCommunicationUsed()[i]);
        }

        freshMission.getActiveTokens().clear();
        freshMission.getActiveTokens().addAll(state.getActiveTokens());

        engine.getCommunicationManager().init(engine.getPlayerManager().getPlayers().size());
        if (state.getCommunicationRequested() != null) {
            for (int i = 0; i < playerCount && i < state.getCommunicationRequested().length; i++) {
                if (state.getCommunicationRequested()[i]) {
                    engine.getCommunicationManager().toggleRequest(i);
                }
            }
        }
        if (state.getCommunicationPlayerIndex() >= 0) {
            engine.getCommunicationManager().startCommunication(state.getCommunicationPlayerIndex());
        }
    }
}
