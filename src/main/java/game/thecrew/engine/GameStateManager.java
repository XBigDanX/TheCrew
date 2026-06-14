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
        state.missionId = mission.getId();
        state.currentMissionNumber = engine.getCurrentMissionNumber();
        state.status = mission.getStatus();
        state.phase = engine.getPhase();
        state.currentPlayerIndex = engine.getPlayerManager().getCurrentPlayerIndex();
        state.captainIndex = engine.getPlayerManager().getCaptainIndex();

        Map<Integer, List<Card>> playerHands = new HashMap<>();
        for (int i = 0; i < engine.getPlayerManager().getPlayers().size(); i++) {
            playerHands.put(i, new ArrayList<>(engine.getPlayerManager().getPlayers().get(i).getHand()));
        }
        state.playerHands = playerHands;

        List<Task> missionTasks = mission.getTasks();
        List<TaskSnapshot> taskSnapshots = new ArrayList<>();
        for (int i = 0; i < missionTasks.size(); i++) {
            Task task = missionTasks.get(i);
            taskSnapshots.add(new TaskSnapshot(i, task.getAssignedPlayer(), task.isCompleted()));
        }
        state.tasks = taskSnapshots;

        state.completedTricks = new ArrayList<>(mission.getCompletedTricks());

        state.currentTrick = engine.getTrickManager().getCurrentTrick();

        int playerCount = engine.getPlayerManager().getPlayers().size();
        state.communicationUsed = new boolean[playerCount];
        for (int i = 0; i < playerCount; i++) {
            state.communicationUsed[i] = mission.hasPlayerUsedToken(i);
        }

        state.activeTokens = new ArrayList<>(mission.getActiveTokens());

        return state;
    }

    public void restoreState(GameState state) {
        engine.missions.clear();
        int playerCount = engine.getPlayerManager().getPlayers().size();
        engine.playerManager.setCaptainIndex(state.captainIndex);
        for (int missionId = 1; missionId <= 32; missionId++) {
            Mission mission = MissionLibrary.forPlayerCount(missionId, playerCount);
            mission.setCaptainIndex(engine.playerManager.getCaptainIndex());
            engine.missions.add(mission);
        }

        engine.currentMissionIndex = state.missionId - 1;
        Mission freshMission = engine.missions.get(engine.currentMissionIndex);
        freshMission.setStatus(state.status);

        for (Mission mission : engine.missions) {
            mission.setCaptainIndex(engine.playerManager.getCaptainIndex());
        }

        for (int i = 0; i < engine.getPlayerManager().getPlayers().size(); i++) {
            engine.getPlayerManager().getPlayers().get(i).getTaskHand().clear();
        }

        List<Task> freshTasks = freshMission.getTasks();
        for (TaskSnapshot snapshot : state.tasks) {
            if (snapshot.taskIndex >= 0 && snapshot.taskIndex < freshTasks.size()) {
                Task task = freshTasks.get(snapshot.taskIndex);
                if (snapshot.assignedPlayer != null) {
                    task.assignPlayer(snapshot.assignedPlayer);
                    engine.getPlayerManager().getPlayers().get(snapshot.assignedPlayer).addTask(task);
                }
                if (snapshot.completed) {
                    task.markCompleted();
                }
            }
        }

        for (int i = 0; i < engine.getPlayerManager().getPlayers().size(); i++) {
            engine.getPlayerManager().getPlayers().get(i).getHand().clear();
            List<Card> savedHand = state.playerHands.get(i);
            if (savedHand != null) {
                for (Card card : savedHand) {
                    engine.getPlayerManager().getPlayers().get(i).addCardToHand(card);
                }
            }
        }

        engine.playerManager.setCaptainIndex(state.captainIndex);
        engine.playerManager.setCurrentPlayerIndex(state.currentPlayerIndex);
        engine.phase = state.phase;

        freshMission.getCompletedTricks().clear();
        freshMission.getCompletedTricks().addAll(state.completedTricks);

        engine.getTrickManager().setCurrentTrick(state.currentTrick);

        engine.cardsPlayedInMission = state.completedTricks.size() * engine.getPlayerManager().getPlayers().size()
                + state.currentTrick.getPlays().size();

        for (int i = 0; i < engine.getPlayerManager().getPlayers().size(); i++) {
            freshMission.setPlayerUsedToken(i, state.communicationUsed[i]);
        }

        freshMission.getActiveTokens().clear();
        freshMission.getActiveTokens().addAll(state.activeTokens);

        engine.getCommunicationManager().init(engine.getPlayerManager().getPlayers().size());

        if (engine.phase == GamePhase.TASK_SELECTION) {
            engine.taskManager = new TaskSelectionManager(engine.getPlayerManager().getPlayers(), freshMission.getTasks());
        }
    }
}
