package ru.yandex.model;

public class Subtask extends Task {
    private final int epicId;

    public Subtask(String name, String description, int id, TaskStatus status, int epicId) {
        super(name, description, id, status);
        this.epicId = epicId;
    }

    public Subtask(String name, String description, int epicId) {
        this(name, description, -1, TaskStatus.NEW, epicId);
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
