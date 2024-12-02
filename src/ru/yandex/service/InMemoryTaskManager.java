package ru.yandex.service;

import ru.yandex.exceptions.TaskNotFoundException;
import ru.yandex.exceptions.TaskOverlapException;
import ru.yandex.model.Epic;
import ru.yandex.model.Subtask;
import ru.yandex.model.Task;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class InMemoryTaskManager implements TaskManager {
    protected final HashMap<Integer, Task> tasks = new HashMap<>();
    protected final HashMap<Integer, Subtask> subtasks = new HashMap<>();
    protected final HashMap<Integer, Epic> epics = new HashMap<>();
    protected final TreeSet<Task> prioritizedTasks = new TreeSet<>(
            Comparator.comparing(Task::getStartTime, LocalDateTime::compareTo));
    protected int freeId = 0;
    private final HistoryManager historyManager = Managers.getDefaultHistory();

    // методы добавления возвращают id добавленного элемента

    @Override
    public int addTask(Task task) {
        Optional<Task> overlappingTask = prioritizedTasks.stream().filter(t -> checkOverlap(t, task)).findFirst();
        if (overlappingTask.isPresent()) {
            throw new TaskOverlapException(
                    String.format("Невозможно добавить задачу: пересечение по срокам выполнения с %s #%08d!",
                            overlappingTask.get().getClass() == Task.class ? "задачей" :
                                    (overlappingTask.get().getClass() == Epic.class ? "эпиком" : "подзадачей"),
                            overlappingTask.get().getId()));
        }
        int id = freeId++;
        task.setId(id);
        tasks.put(id, task);
        prioritizedTasks.add(task);
        return id;
    }

    @Override
    public int addSubtask(Subtask subtask) {
        Optional<Task> overlappingTask = prioritizedTasks.stream().filter(t -> checkOverlap(t, subtask)).findFirst();
        if (overlappingTask.isPresent()) {
            throw new TaskOverlapException(
                    String.format("Невозможно добавить подзадачу: пересечение по срокам выполнения с %s #%08d!",
                            overlappingTask.get().getClass() == Task.class ? "задачей" :
                                    (overlappingTask.get().getClass() == Epic.class ? "эпиком" : "подзадачей"),
                            overlappingTask.get().getId()));
        }
        if (!epics.containsKey(subtask.getEpicId())) {
            throw new TaskNotFoundException(
                    "Невозможно добавить подзадачу к эпику #" + String.format("%08d", subtask.getEpicId())
                            + ": такого эпика не существует!");
        }
        int id = freeId++;
        subtask.setId(id);
        subtasks.put(id, subtask);
        prioritizedTasks.add(subtask);
        epics.get(subtask.getEpicId()).addSubtask(subtask);
        return id;
    }

    @Override
    public int addEpic(Epic epic) {
        int id = freeId++;
        epic.setId(id);
        epics.put(id, epic);
        return id;
    }

    @Override
    public void updateTask(Task task) {
        if (!tasks.containsKey(task.getId())) {
            throw new TaskNotFoundException("Невозможно обновить задачу #" + String.format("%08d", task.getId())
                    + ": такой задачи не существует!");
        }
        prioritizedTasks.remove(tasks.get(task.getId()));
        tasks.put(task.getId(), task);
        prioritizedTasks.add(task);
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        if (!subtasks.containsKey(subtask.getId())) {
            throw new TaskNotFoundException("Невозможно обновить подзадачу #" + String.format("%08d", subtask.getId())
                    + ": такой подзадачи не существует!");
        }
        prioritizedTasks.remove(epics.get(subtask.getEpicId()));
        epics.get(subtask.getEpicId()).updateSubtask(subtask);
        prioritizedTasks.add(epics.get(subtask.getEpicId()));
        prioritizedTasks.remove(subtasks.get(subtask.getId()));
        subtasks.put(subtask.getId(), subtask);
        prioritizedTasks.add(subtask);
    }

    @Override
    public void updateEpic(Epic epic) {
        if (!epics.containsKey(epic.getId())) {
            throw new TaskNotFoundException("Невозможно обновить эпик #" + String.format("%08d", epic.getId())
                    + ": такого эпика не существует!");
        }
        Epic oldEpic = epics.get(epic.getId());
        oldEpic.setName(epic.getName());
        oldEpic.setDescription(epic.getDescription());
        prioritizedTasks.remove(epics.get(epic.getId()));
        epics.put(epic.getId(), oldEpic);
        prioritizedTasks.add(epic);
    }

    @Override
    public ArrayList<Task> getAllTasks() {
        return new ArrayList<>(tasks.values());
    }

    @Override
    public ArrayList<Epic> getAllEpics() {
        return new ArrayList<>(epics.values());
    }

    @Override
    public ArrayList<Subtask> getAllSubtasks() {
        return new ArrayList<>(subtasks.values());
    }

    @Override
    public void clearTasks() {
        for (int id : tasks.keySet()) {
            historyManager.remove(id);
        }
        prioritizedTasks.removeAll(tasks.values());
        tasks.clear();
    }

    @Override
    public void clearSubtasks() {
        for (Epic epic : epics.values()) {
            epic.clearSubtasks();
        }
        for (int id : subtasks.keySet()) {
            historyManager.remove(id);
        }
        prioritizedTasks.removeAll(subtasks.values());
        subtasks.clear();
    }

    @Override
    public void clearEpics() {
        // подзадачи не могут существовать без эпиков, поэтому также удаляются
        for (int id : subtasks.keySet()) {
            historyManager.remove(id);
        }
        for (int id : epics.keySet()) {
            historyManager.remove(id);
        }
        prioritizedTasks.removeAll(subtasks.values());
        subtasks.clear();
        prioritizedTasks.removeAll(epics.values());
        epics.clear();
    }

    // геттеры возвращают null, если объекта с искомым id не существует

    @Override
    public Task getTask(int id) {
        Task task = tasks.get(id);
        historyManager.add(task);
        return task;
    }

    @Override
    public Epic getEpic(int id) {
        Epic epic = epics.get(id);
        historyManager.add(epic);
        return epic;
    }

    @Override
    public Subtask getSubtask(int id) {
        Subtask subtask = subtasks.get(id);
        historyManager.add(subtask);
        return subtask;
    }

    // методы для удаления возвращают true, если элемент был удалён, и false, если элемента с таким id не было

    @Override
    public boolean removeTask(int id) {
        historyManager.remove(id);
        prioritizedTasks.remove(tasks.get(id));
        return tasks.remove(id) != null;
    }

    @Override
    public boolean removeEpic(int id) {
        if (!epics.containsKey(id)) {
            return false;
        }
        for (int s : epics.get(id).getSubtasks().keySet()) {
            historyManager.remove(s);
            subtasks.remove(s);
        }
        historyManager.remove(id);
        prioritizedTasks.remove(epics.get(id));
        epics.remove(id);
        return true;
    }

    @Override
    public boolean removeSubtask(int id) {
        Subtask subtask = subtasks.get(id);
        if (subtask == null) {
            return false;
        }
        epics.get(subtask.getEpicId()).removeSubtask(subtask);
        historyManager.remove(id);
        prioritizedTasks.remove(subtasks.get(id));
        subtasks.remove(id);
        return true;
    }

    @Override
    public ArrayList<Subtask> getSubtasks(int id) {
        Epic epic = epics.get(id);
        return epic == null ? new ArrayList<>() : epic.getSubtasks().keySet().stream().map(subtasks::get)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    @Override
    public ArrayList<Task> getPrioritizedTasks() {
        return new ArrayList<>(prioritizedTasks);
    }

    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }

    protected boolean checkOverlap(Task task1, Task task2) {
        LocalDateTime start1 = task1.getStartTime(), start2 = task2.getStartTime(), end1 = task1.getEndTime(), end2
                = task2.getEndTime();
        return ((!start1.isBefore(start2) && start1.isBefore(end2)) || (!start2.isBefore(start1) && start2.isBefore(end1)));
    }
}
