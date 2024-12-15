package ru.yandex.model;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.stream.Collectors;

public class Epic extends Task {

    private HashMap<Integer, Subtask> subtasks = new HashMap<>();
    private LocalDateTime endTime;
    private boolean isUpdatedStatus, isUpdatedTimeAndDuration;

    public Epic(String name, String description, int id) {
        super(name, description, id, TaskStatus.NEW, Duration.ZERO, LocalDateTime.MIN);
        this.endTime = LocalDateTime.MAX;
        isUpdatedStatus = true;
        isUpdatedTimeAndDuration = true;
    }

    public Epic(String name, String description) {
        this(name, description, -1);
    }

    @Override
    public String toString() {
        return super.toString().replace("Задача", "Эпик") + "; Подзадачи: " + (subtasks.isEmpty()
            ? "нет" :
            subtasks.keySet().stream().map(st -> "#" + String.format("%08d", st))
                .collect(Collectors.joining(", ")));
    }

    @Override
    public TaskStatus getStatus() {
        if (!isUpdatedStatus) {
            updateStatus();
        }
        return super.getStatus();
    }

    public HashMap<Integer, Subtask> getSubtasks() {
        return subtasks;
    }

    public boolean addSubtask(Subtask subtask) {
        if (!subtasks.containsKey(subtask.getId())) {
            subtasks.put(subtask.getId(), subtask);
            isUpdatedStatus = false;
            isUpdatedTimeAndDuration = false;
            return true;
        }
        return false;
    }

    public boolean updateSubtask(Subtask subtask) {
        if (!subtasks.containsKey(subtask.getId())) {
            return false;
        }
        subtasks.put(subtask.getId(), subtask);
        isUpdatedStatus = false;
        isUpdatedTimeAndDuration = false;
        return true;
    }

    public boolean removeSubtask(Subtask subtask) {
        boolean result = subtasks.remove(subtask.getId()) != null;
        if (result) {
            isUpdatedStatus = false;
            isUpdatedTimeAndDuration = false;
        }
        return result;
    }

    public void clearSubtasks() {
        this.status = TaskStatus.NEW;
        startTime = LocalDateTime.MIN;
        endTime = LocalDateTime.MAX;
        duration = Duration.ZERO;
        isUpdatedStatus = true;
        isUpdatedTimeAndDuration = true;
        subtasks.clear();
    }

    @Override
    public LocalDateTime getStartTime() {
        if (!isUpdatedTimeAndDuration) {
            updateTimeAndDuration();
        }
        return super.getStartTime();
    }

    @Override
    public Duration getDuration() {
        if (!isUpdatedTimeAndDuration) {
            updateTimeAndDuration();
        }
        return super.getDuration();
    }

    @Override
    public LocalDateTime getEndTime() {
        if (!isUpdatedTimeAndDuration) {
            updateTimeAndDuration();
        }
        return endTime;
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
        isUpdatedStatus = true;
    }

    private void updateTimeAndDuration() {
        duration = subtasks.values().stream().map(Task::getDuration)
            .reduce(Duration.ZERO, Duration::plus);
        startTime = subtasks.values().stream().map(Task::getStartTime).min(LocalDateTime::compareTo)
            .orElse(LocalDateTime.MIN);
        endTime = subtasks.values().stream().map(Task::getEndTime).max(LocalDateTime::compareTo)
            .orElse(LocalDateTime.MAX);
        isUpdatedTimeAndDuration = true;
    }

    @Override
    public String getCSV() {
        return super.getCSV().replace(TaskType.TASK.toString(), TaskType.EPIC.toString());
    }

    @Override
    public Epic clone() throws CloneNotSupportedException {
        Epic clone = (Epic) super.clone();
        clone.subtasks = new HashMap<>(this.subtasks);
        return clone;
    }
}
