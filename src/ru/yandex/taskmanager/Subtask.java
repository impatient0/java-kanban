package ru.yandex.taskmanager;

public class Subtask extends Task {
    private final Epic epic;

    public Subtask(String name, String description, int id, TaskStatus status, Epic epic) {
        super(name, description, id, status);
        this.epic = epic;
    }

    public Subtask(String name, String description, Epic epic) {
        this(name, description, -1, TaskStatus.NEW, epic);
    }

    @Override
    public String toString() {
        return super.toString().replace("Задача", "Подзадача") + "; Относится к эпику #" + epic.getId();
    }

    public Epic getEpic() {
        return epic;
    }
}
