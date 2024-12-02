package ru.yandex.model;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.stream.Collectors;

public class Epic extends Task {
    private HashMap<Integer, Subtask> subtasks = new HashMap<>();
    private LocalDateTime endTime;

    public Epic(String name, String description, int id) {
        super(name, description, id, TaskStatus.NEW, Duration.ZERO, LocalDateTime.MIN);
        this.endTime = LocalDateTime.MIN;
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
            this.status = TaskStatus.NEW;
            return;
        }
        if (subtasks.values().stream().allMatch(s -> s.getStatus().equals(TaskStatus.NEW))) {
            this.status = TaskStatus.NEW;
            return;
        }
        if (subtasks.values().stream().allMatch(s -> s.getStatus().equals(TaskStatus.DONE))) {
            this.status = TaskStatus.DONE;
            return;
        }
        this.status = TaskStatus.IN_PROGRESS;
    }

    public HashMap<Integer, Subtask> getSubtasks() {
        return subtasks;
    }

    public boolean addSubtask(Subtask subtask) {
        if (!subtasks.containsKey(subtask.getId())) {
            subtasks.put(subtask.getId(), subtask);
            updateStatus();
            updateTimeAndDuration();
            return true;
        }
        return false;
    }

    public boolean updateSubtask(Subtask subtask) {
        if (!subtasks.containsKey(subtask.getId())) {
            return false;
        }
        subtasks.put(subtask.getId(), subtask);
        updateStatus();
        updateTimeAndDuration();
        return true;
    }

    public boolean removeSubtask(Subtask subtask) {
        boolean result = subtasks.remove(subtask.getId()) != null;
        if (result) {
            updateStatus();
            updateTimeAndDuration();
        }
        return result;
    }

    public void clearSubtasks() {
        this.status = TaskStatus.NEW;
        subtasks.clear();
    }

    @Override
    public LocalDateTime getEndTime() {
        return endTime;
    }

    private void updateTimeAndDuration() {
        duration = subtasks.values().stream().map(Task::getDuration).reduce(Duration.ZERO, Duration::plus);
        startTime = subtasks.values().stream().map(Task::getStartTime).min(LocalDateTime::compareTo)
                .orElse(LocalDateTime.MIN);
        endTime = subtasks.values().stream().map(Task::getEndTime).max(LocalDateTime::compareTo)
                .orElse(LocalDateTime.MAX);
    }

    @Override
    public String getCSV() {
        return super.getCSV().replace(TaskType.TASK.toString(), TaskType.EPIC.toString());
    }

    @Override
    public Epic clone() throws CloneNotSupportedException {
        Epic clone = (Epic) super.clone();
        HashMap<Integer, Subtask> clonedSubtasks = new HashMap<>();
        for (Integer id : this.subtasks.keySet()) {
            clonedSubtasks.put(id, this.subtasks.get(id));
        }
        clone.subtasks = clonedSubtasks;
        return clone;
    }
}
