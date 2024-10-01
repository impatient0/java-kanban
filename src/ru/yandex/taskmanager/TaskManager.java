package ru.yandex.taskmanager;

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

    public int add(Task task) {
        return addTask(task.getName(), task.getDescription());
    }

    public int add(Epic epic) {
        return addEpic(epic.getName(), epic.getDescription());
    }

    public int add(Subtask subtask) {
        return addSubtask(subtask.getName(), subtask.getDescription(), subtask.getEpic().getId());
    }

    public void update(Task task) throws TaskNotFoundException, BadTaskTypeException {
        updateTask(task.getId(), task.getName(), task.getDescription(), task.status);
    }

    public void update(Epic epic) throws TaskNotFoundException, BadTaskTypeException {
        updateEpic(epic.getId(), epic.getName(), epic.getDescription(), epic.getSubtasks());
    }

    public void update(Subtask subtask) throws TaskNotFoundException, BadTaskTypeException {
        updateSubtask(subtask.getId(), subtask.getName(), subtask.getDescription(), subtask.getStatus(),
                subtask.getEpic().getId());
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
        Subtask subtask = new Subtask(name, description, id, TaskStatus.NEW, (Epic) tasks.get(epicId));
        tasks.put(id, subtask);
        subtask.getEpic().addSubtask(subtask);
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
        if (!tasks.get(id).getClass().equals(Subtask.class)) {
            throw new BadTaskTypeException(
                    "Задача #" + id + " является " + (tasks.get(id) instanceof Epic ? "эпиком" : "задачей")
                            + ", а не подзадачей!");
        }
        if (!tasks.get(epicId).getClass().equals(Epic.class)) {
            throw new BadTaskTypeException(
                    "Не может быть подзадачей для #" + epicId + ", т.к. #" + epicId + "не является эпиком!");
        }
        Subtask subtask = new Subtask(name, description, id, status, (Epic) tasks.get(epicId));
        // oldEpic и newEpic создаются как отдельные переменные, т.к. при обновлении подзадачи мог измениться свой эпик
        Epic oldEpic = ((Subtask) tasks.get(id)).getEpic();
        Epic newEpic = (Epic) tasks.get(epicId);
        oldEpic.removeSubtask(subtask);
        newEpic.addSubtask(subtask);
        tasks.put(id, subtask);
    }

    public void updateEpic(int id, String name, String description, ArrayList<Subtask> subtasks)
            throws TaskNotFoundException, BadTaskTypeException {
        if (!tasks.containsKey(id)) {
            throw new TaskNotFoundException("Не найден эпик #" + id + "!");
        }
        if (!tasks.get(id).getClass().equals(Epic.class)) {
            throw new BadTaskTypeException(
                    "Задача #" + id + " является " + (tasks.get(id) instanceof Subtask ? "подзадачей" : "задачей")
                            + ", а не эпиком!");
        }
        tasks.put(id, new Epic(name, description, id, subtasks));
    }

    public ArrayList<Task> getAll() {
        return new ArrayList<>(tasks.values());
    }

    public ArrayList<Task> getAllTasks() {
        return tasks.values().stream().filter(t -> t.getClass().equals(Task.class))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public ArrayList<Epic> getAllEpics() {
        return tasks.values().stream().filter(t -> t.getClass().equals(Epic.class)).map(t -> (Epic) t)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public ArrayList<Subtask> getAllSubtasks() {
        return tasks.values().stream().filter(t -> t.getClass().equals(Subtask.class)).map(t -> (Subtask) t)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public void clear() {
        tasks.clear();
    }

    public void clearTasks() {
        tasks.entrySet().removeIf(t -> t.getValue().getClass().equals(Task.class));
    }

    public void clearSubtasks() {
        for (Task t : tasks.values()) {
            if (t instanceof Subtask) {
                ((Subtask) t).getEpic().removeSubtask((Subtask) t);
                tasks.remove(t.getId());
            }
        }
    }

    public void clearEpics() {
        // подзадачи не могут существовать без эпиков, поэтому также удаляются
        tasks.entrySet().removeIf(t -> (t instanceof Epic || t instanceof Subtask));
    }

    public Task get(int id) {
        return tasks.get(id);
    }

    private boolean remove(Task task) {
        return tasks.remove(task.getId()) != null;
    }

    private boolean remove(Subtask subtask) {
        subtask.getEpic().removeSubtask(subtask);
        return tasks.remove(subtask.getId()) != null;
    }

    private boolean remove(Epic epic) {
        if (!tasks.containsValue(epic)) {
            return false;
        }
        for (Subtask st : epic.getSubtasks()) {
            tasks.remove(st.getId());
        }
        tasks.remove(epic.getId());
        return true;
    }

    public boolean remove(int id) {
        Task task = tasks.get(id);
        if (task instanceof Subtask) {
            return remove((Subtask) task);
        }
        if (task instanceof Epic) {
            return remove((Epic) task);
        }
        return remove(tasks.get(id));
    }

    public ArrayList<Subtask> getSubtasks(Task task) {
        if (!(task instanceof Epic)) {
            return null;
        }
        return ((Epic) task).getSubtasks();
    }
}
