package ru.yandex.model;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.stream.Collectors;

public class Epic extends Task {
    private HashMap<Integer, TaskStatus> subtasks = new HashMap<>();
    private HashMap<Integer, Duration> subtaskDurations = new HashMap<>();
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
            subtaskDurations.put(subtask.getId(), subtask.getDuration());
            updateStatus();
            updateTimeAndDuration(subtask);
            return true;
        }
        return false;
    }

    public boolean updateSubtask(Subtask subtask) {
        if (!subtasks.containsKey(subtask.getId())) {
            return false;
        }
        subtasks.put(subtask.getId(), subtask.getStatus());
        subtaskDurations.put(subtask.getId(), subtask.getDuration());
        updateStatus();
        updateTimeAndDuration(subtask);
        return true;
    }

    public boolean removeSubtask(Subtask subtask) {
        boolean result = subtasks.remove(subtask.getId()) != null;
        if (result) {
            subtaskDurations.remove(subtask.getId());
            updateStatus();
            updateTimeAndDuration(subtask);
        }
        return result;
    }

    public void clearSubtasks() {
        this.status = TaskStatus.NEW;
        subtasks.clear();
        subtaskDurations.clear();
    }

    @Override
    public LocalDateTime getEndTime() {
        return endTime;
    }

    @Override
    public Duration getDuration() {
        return subtaskDurations.values().stream().reduce(Duration.ZERO, Duration::plus);
    }

    private void updateTimeAndDuration(Subtask subtask) {
        if (this.startTime.isEqual(LocalDateTime.MIN) || subtask.getStartTime().isBefore(this.startTime)) {
            this.startTime = subtask.getStartTime();
        }
        if (this.endTime.isBefore(subtask.getEndTime())) {
            this.endTime = subtask.getEndTime();
        }
    }

    @Override
    public String getCSV() {
        return super.getCSV().replace(TaskType.TASK.toString(), TaskType.EPIC.toString());
    }

    @Override
    public Epic clone() throws CloneNotSupportedException {
        Epic clone = (Epic) super.clone();
        HashMap<Integer, TaskStatus> clonedSubtasks = new HashMap<>();
        HashMap<Integer, Duration> clonedSubtaskDurations = new HashMap<>();
        for (Integer id : this.subtasks.keySet()) {
            clonedSubtasks.put(id, this.subtasks.get(id));
            clonedSubtaskDurations.put(id, this.subtaskDurations.get(id));
        }
        clone.subtasks = clonedSubtasks;
        clone.subtaskDurations = clonedSubtaskDurations;
        return clone;
    }
}
