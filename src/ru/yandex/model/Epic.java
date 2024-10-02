package ru.yandex.model;

import java.util.HashMap;
import java.util.stream.Collectors;

public class Epic extends Task {
    private final HashMap<Integer, TaskStatus> subtasks = new HashMap<>();

    public Epic(String name, String description, int id) {
        super(name, description, id, TaskStatus.NEW);
    }

    public Epic(String name, String description) {
        this(name, description, -1);
    }

    @Override
    public String toString() {
        return super.toString().replace("Задача", "Эпик") + "; Подзадачи: " + (subtasks.isEmpty() ? "нет" :
                subtasks.keySet().stream().map(st -> "#" + String.format("%08d", st))
                        .collect(Collectors.joining(", ")));
    }

    private void updateStatus() {
        if (subtasks.isEmpty()) {
            this.status = TaskStatus.DONE;
            return;
        }
        if (subtasks.values().stream().allMatch(TaskStatus.NEW::equals)) {
            this.status = TaskStatus.NEW;
            return;
        }
        if (subtasks.values().stream().allMatch(TaskStatus.DONE::equals)) {
            this.status = TaskStatus.DONE;
            return;
        }
        this.status = TaskStatus.IN_PROGRESS;
    }

    public HashMap<Integer, TaskStatus> getSubtasks() {
        return subtasks;
    }

    public boolean addSubtask(Subtask subtask) {
        if (!subtasks.containsKey(subtask.getId())) {
            subtasks.put(subtask.getId(), subtask.getStatus());
            this.updateStatus();
            return true;
        }
        return false;
    }

    public boolean updateSubtask(Subtask subtask) {
        if (!this.subtasks.containsKey(subtask.getId())) {
            return false;
        }
        this.subtasks.put(subtask.getId(), subtask.getStatus());
        this.updateStatus();
        return true;
    }

    public boolean removeSubtask(Subtask subtask) {
        boolean result = subtasks.remove(subtask.getId()) != null;
        if (result) {
            this.updateStatus();
        }
        return result;
    }

    public void clearSubtasks() {
        this.status = TaskStatus.DONE;
        subtasks.clear();
    }
}
