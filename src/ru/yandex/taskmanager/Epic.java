package ru.yandex.taskmanager;

import java.util.ArrayList;
import java.util.stream.Collectors;

public class Epic extends Task {
    private final ArrayList<Subtask> subtasks;

    public Epic(String name, String description, int id, ArrayList<Subtask> subtasks) {
        super(name, description, id, TaskStatus.NEW);
        this.subtasks = subtasks;
        this.updateStatus();
    }

    public Epic(String name, String description) {
        this(name, description, -1, null);
    }

    @Override
    public String toString() {
        return super.toString().replace("Задача", "Эпик") + "; Подзадачи: " + (subtasks == null || subtasks.isEmpty() ?
                "нет" : subtasks.stream().map(st -> ("#" + String.format("%08d", st.getId())))
                .collect(Collectors.joining(", ")));
    }

    private void updateStatus() {
        if (subtasks == null || subtasks.isEmpty()) {
            this.status = TaskStatus.DONE;
            return;
        }
        if (subtasks.stream().allMatch(st -> TaskStatus.NEW.equals(st.getStatus()))) {
            this.status = TaskStatus.NEW;
            return;
        }
        if (subtasks.stream().allMatch(st -> TaskStatus.DONE.equals(st.getStatus()))) {
            this.status = TaskStatus.DONE;
            return;
        }
        this.status = TaskStatus.IN_PROGRESS;
    }

    public ArrayList<Subtask> getSubtasks() {
        return subtasks == null ? new ArrayList<>() : subtasks;
    }

    boolean addSubtask(Subtask subtask) {
        if (!subtasks.contains(subtask)) {
            subtasks.add(subtask);
            this.updateStatus();
            return true;
        }
        return false;
    }

    boolean removeSubtask(Subtask subtask) {
        boolean result = subtasks.remove(subtask);
        if (result) {
            this.updateStatus();
        }
        return result;
    }
}
