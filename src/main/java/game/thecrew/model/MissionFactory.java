package game.thecrew.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MissionFactory {

    private final List<Task> taskPool;

    public MissionFactory(List<Task> taskPool) {
        this.taskPool = new ArrayList<>(taskPool);
    }

    public Mission createMission(int id, int difficulty, int playerCount) {
        List<Task> shuffled = new ArrayList<>(taskPool);
        Collections.shuffle(shuffled);

        List<ActiveMissionTask> selectedTasks = new ArrayList<>();
        int sum = 0;
        for (Task task : shuffled) {
            if (sum + task.getValue(playerCount) <= difficulty) {
                selectedTasks.add(new ActiveMissionTask(task));
                sum += task.getValue(playerCount);
            }
        }

        return new Mission(id, difficulty, selectedTasks);
    }
}
