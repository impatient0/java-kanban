import java.util.ArrayList;
import java.util.stream.Collectors;

public class Epic extends Task {
    private final ArrayList<SubTask> subtasks;

    public Epic(String name, String description, int id, ArrayList<SubTask> subtasks) {
        super(name, description, id, subtasks.stream()
                .allMatch(st -> TaskStatus.NEW.equals(st.getStatus())) ? TaskStatus.NEW : (subtasks.stream()
                .allMatch(st -> TaskStatus.DONE.equals(st.getStatus())) ? TaskStatus.DONE : TaskStatus.IN_PROGRESS));
        this.subtasks = subtasks;
    }

    @Override
    public String toString() {
        return super.toString().replace("Задача", "Эпик") + "\n Подзадачи:\n" + subtasks.stream()
                .map(st -> ("\t#" + st.getId())).collect(Collectors.joining("\n"));
    }
}
