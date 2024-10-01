import java.util.ArrayList;
import java.util.HashMap;
import java.util.stream.Collectors;

public class TaskManager {
    private final HashMap<Integer, Task> tasks;
    private int freeId;

    public TaskManager() {
        tasks = new HashMap<>();
        freeId = 0;
    }

    public int addTask(String name, String description) {
        int id = freeId++;
        tasks.put(id, new Task(name, description, id, TaskStatus.NEW));
        return id;
    }

    public int addEpic(String name, String description) {
        int id = freeId++;
        tasks.put(id, new Epic(name, description, id, new ArrayList<>()));
        return id;
    }

    public int addSubtask(String name, String description, int epicId)
            throws TaskNotFoundException, BadTaskTypeException {
        if (!tasks.containsKey(epicId)) {
            throw new TaskNotFoundException(
                    "Невозможно добавить подзадачу к эпику #" + epicId + ": такого эпика не существует!");
        }
        if (!tasks.get(epicId).getClass().equals(Epic.class)) {
            throw new BadTaskTypeException(
                    "Невозможно добавить подзадачу к эпику #" + epicId + ": не является эпиком!");
        }
        int id = freeId++;
        SubTask subTask = new SubTask(name, description, id, TaskStatus.NEW, (Epic) tasks.get(epicId));
        tasks.put(id, subTask);
        addSubtaskToEpic((Epic) tasks.get(epicId), subTask);
        return id;
    }

    public void updateTask(int id, String name, String description, TaskStatus status)
            throws TaskNotFoundException, BadTaskTypeException {
        if (!tasks.containsKey(id)) {
            throw new TaskNotFoundException("Не найдена задача #" + id + "!");
        }
        if (!tasks.get(id).getClass().equals(Task.class)) {
            throw new BadTaskTypeException(
                    "Задача #" + id + " является " + (tasks.get(id) instanceof Epic ? "эпиком" : "подзадачей")
                            + ", а не задачей!");
        }
        tasks.put(id, new Task(name, description, id, status));
    }

    public void updateSubtask(int id, String name, String description, TaskStatus status, int epicId)
            throws TaskNotFoundException, BadTaskTypeException {
        if (!tasks.containsKey(id)) {
            throw new TaskNotFoundException("Не найдена подзадача #" + id + "!");
        }
        if (!tasks.get(id).getClass().equals(SubTask.class)) {
            throw new BadTaskTypeException(
                    "Задача #" + id + " является " + (tasks.get(id) instanceof Epic ? "эпиком" : "задачей")
                            + ", а не подзадачей!");
        }
        if (!tasks.get(epicId).getClass().equals(Epic.class)) {
            throw new BadTaskTypeException(
                    "Не может быть подзадачей для #" + epicId + ", т.к. #" + epicId + "не является эпиком!");
        }
        SubTask subTask = new SubTask(name, description, id, status, (Epic) tasks.get(epicId));
        Epic oldEpic = ((SubTask) tasks.get(id)).getEpic();
        Epic newEpic = (Epic) tasks.get(epicId);
        if (!oldEpic.equals(newEpic)) {
            removeSubtaskFromEpic(oldEpic, subTask);
            addSubtaskToEpic(newEpic, subTask);
        }
        tasks.put(id, subTask);
    }

    public void updateEpic(int id, String name, String description) throws TaskNotFoundException, BadTaskTypeException {
        if (!tasks.containsKey(id)) {
            throw new TaskNotFoundException("Не найден эпик #" + id + "!");
        }
        if (!tasks.get(id).getClass().equals(Epic.class)) {
            throw new BadTaskTypeException(
                    "Задача #" + id + " является " + (tasks.get(id) instanceof SubTask ? "подзадачей" : "задачей")
                            + ", а не эпиком!");
        }
        tasks.put(id, new Epic(name, description, id, ((Epic) tasks.get(id)).getSubtasks()));
    }

    public ArrayList<Task> getAllTasks() {
        return tasks.values().stream().filter(t -> t.getClass().equals(Task.class))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private boolean addSubtaskToEpic(Epic epic, SubTask subTask) {
        ArrayList<SubTask> subTasks = epic.getSubtasks();
        subTasks.add(subTask);
        tasks.put(epic.getId(), new Epic(epic.getName(), epic.getDescription(), epic.getId(), subTasks));
        return true;
    }

    private boolean removeSubtaskFromEpic(Epic epic, SubTask subTask) {
        ArrayList<SubTask> subTasks = epic.getSubtasks();
        boolean result = subTasks.remove(subTask);
        tasks.put(epic.getId(), new Epic(epic.getName(), epic.getDescription(), epic.getId(), subTasks));
        return result;
    }
}
