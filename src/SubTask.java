public class SubTask extends Task {
    private final Epic epic;

    public SubTask(String name, String description, int id, TaskStatus status, Epic epic) {
        super(name, description, id, status);
        this.epic = epic;
    }

    @Override
    public String toString() {
        return super.toString().replace("Задача", "Подзадача") + " Относится к эпику #" + epic.getId();
    }

    public Epic getEpic() {
        return epic;
    }
}
