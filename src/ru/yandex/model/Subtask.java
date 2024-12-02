package ru.yandex.model;

import java.time.Duration;
import java.time.LocalDateTime;

public class Subtask extends Task {
    private final int epicId;

    @Override
    public int getClassRank() {
        return 2;
    }

    public Subtask(String name, String description, int id, TaskStatus status, int epicId, Duration duration,
                   LocalDateTime startTime) {
        super(name, description, id, status, duration, startTime);
        this.epicId = epicId;
    }

    public Subtask(String name, String description, int epicId, Duration duration, LocalDateTime startTime) {
        this(name, description, -1, TaskStatus.NEW, epicId, duration, startTime);
    }

    @Override
    public String toString() {
        return super.toString().replace("Задача", "Подзадача") + "; Относится к эпику #" + String.format("%08d",
                epicId);
    }

    @Override
    public String getCSV() {
        return super.getCSV().replace(TaskType.TASK.toString(), TaskType.SUBTASK.toString()) + epicId;
    }

    public int getEpicId() {
        return epicId;
    }
}
