package ru.yandex.service;

import ru.yandex.exceptions.ManagerLoadException;
import ru.yandex.exceptions.ManagerSaveException;
import ru.yandex.model.*;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.stream.Collectors;

public class FileBackedTaskManager extends InMemoryTaskManager {
    private final Path saveFile;
    private static final String HEADER = "id,type,name,status,description,epic";

    public FileBackedTaskManager(Path saveFile) {
        this.saveFile = saveFile;
        if (!Files.exists(saveFile)) {
            try {
                Files.createFile(saveFile);
            } catch (IOException e) {
                throw new ManagerSaveException("Не удалось создать файл сохранения.");
            }
        }
    }

    public static FileBackedTaskManager loadFromFile(File file) {
        FileBackedTaskManager manager = new FileBackedTaskManager(file.toPath());
        manager.load();
        return manager;
    }

    private void load() {
        try (BufferedReader input = new BufferedReader(new FileReader(saveFile.toFile()))) {
            if (!input.ready()) {
                return;
            }
            if (!input.readLine().equals(HEADER)) {
                throw new ManagerLoadException("Некорректный формат файла сохранения.");
            }
            tasks.clear();
            epics.clear();
            subtasks.clear();
            freeId = 0;
            while (input.ready()) {
                String[] split = input.readLine().split(",");
                int id = Integer.parseInt(split[0]);
                TaskType type = TaskType.valueOf(split[1]);
                String name = split[2];
                TaskStatus status = TaskStatus.valueOf(split[3]);
                String description = split[4];
                Duration duration = Duration.parse(split[5]);
                LocalDateTime startTime = LocalDateTime.parse(split[6]);
                switch (type) {
                    case TaskType.TASK:
                        Task task = new Task(name, description, id, status, duration, startTime);
                        tasks.put(id, task);
                        freeId = Integer.max(id + 1, freeId);
                        break;
                    case TaskType.EPIC:
                        Epic epic = new Epic(name, description, id);
                        epics.put(id, epic);
                        freeId = Integer.max(id + 1, freeId);
                        break;
                    case TaskType.SUBTASK:
                        int epicId = Integer.parseInt(split[7]);
                        Subtask subtask = new Subtask(name, description, id, status, epicId, duration, startTime);
                        epics.get(epicId).addSubtask(subtask);
                        subtasks.put(id, subtask);
                        freeId = Integer.max(id + 1, freeId);
                        break;
                    default:
                        throw new ManagerLoadException("Некорректный формат файла сохранения.");
                }
            }
        } catch (IOException e) {
            throw new ManagerLoadException("Ошибка при чтении данных из файла.");
        }
    }

    private void save() {
        try (PrintStream print = new PrintStream(saveFile.toFile())) {
            print.println(HEADER);
            int curId = 0;
            while (curId < freeId) {
                Task task = tasks.get(curId), epic = epics.get(curId), subtask = subtasks.get(curId);
                Task curTask = task != null ? task : (epic != null ? epic : subtask);
                if (curTask == null) {
                    curId++;
                    continue;
                }
                print.println(curTask.getCSV());
                curId++;
            }
        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка при сохранении данных в файл.");
        }
    }

    @Override
    public int addTask(Task task) {
        int id = super.addTask(task);
        save();
        return id;
    }

    @Override
    public int addEpic(Epic epic) {
        int id = super.addEpic(epic);
        save();
        return id;
    }

    @Override
    public int addSubtask(Subtask subtask) {
        int id = super.addSubtask(subtask);
        save();
        return id;
    }

    @Override
    public void updateTask(Task task) {
        super.updateTask(task);
        save();
    }

    @Override
    public void updateEpic(Epic epic) {
        super.updateEpic(epic);
        save();
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        super.updateSubtask(subtask);
        save();
    }

    @Override
    public void clearTasks() {
        super.clearTasks();
        save();
    }

    @Override
    public void clearEpics() {
        super.clearEpics();
        save();
    }

    @Override
    public void clearSubtasks() {
        super.clearSubtasks();
        save();
    }

    @Override
    public boolean removeTask(int id) {
        boolean result = super.removeTask(id);
        save();
        return result;
    }

    @Override
    public boolean removeEpic(int id) {
        boolean result = super.removeEpic(id);
        save();
        return result;
    }

    @Override
    public boolean removeSubtask(int id) {
        boolean result = super.removeSubtask(id);
        save();
        return result;
    }

    public static void main(String[] args) throws IOException {
        Path saveFile = File.createTempFile("save_file", ".tmp").toPath();
        TaskManager taskManager = new FileBackedTaskManager(saveFile);
        LocalDateTime nowDateTime = LocalDateTime.now();
        int t1 = taskManager.addTask(
                new Task("Подготовка к экзамену", "Составить план подготовки к экзамену.", Duration.ofHours(1),
                        nowDateTime.plusHours(2)));
        int t2 = taskManager.addTask(
                new Task("Ремонт в детской комнате", "Составить список необходимых материалов и инструментов.",
                        Duration.ofHours(72), nowDateTime.plusHours(4)));
        int e1 = taskManager.addEpic(new Epic("Оптимизация рабочего процесса",
                "Оптимизация рабочего процесса компании для повышения эффективности и продуктивности сотрудников."));
        int e2 = taskManager.addEpic(new Epic("Улучшение пользовательского интерфейса",
                "Улучшение пользовательского интерфейса приложения для повышения удобства использования и "
                        + "привлекательности."));
        int s1 = taskManager.addSubtask(new Subtask("Анализ текущих процессов",
                "Изучение и анализ существующих рабочих процессов компании для выявления узких мест и возможностей "
                        + "для оптимизации.", e1, Duration.ofHours(2), nowDateTime.plusHours(6)));
        int s2 = taskManager.addSubtask(
                new Subtask("Разработка рекомендаций", "Подготовка предложений по улучшению рабочих процессов.", e1,
                        Duration.ofHours(1), nowDateTime.plusHours(9)));
        int s3 = taskManager.addSubtask(new Subtask("Анализ текущего интерфейса",
                "Изучение и анализ текущего пользовательского интерфейса приложения для выявления недостатков и "
                        + "возможностей для улучшения.", e1, Duration.ofHours(3), nowDateTime.plusHours(12)));
        TaskManager tm2 = loadFromFile(saveFile.toFile());
        showTasks(tm2);
    }

    public static void showTasks(TaskManager taskManager) {
        System.out.println("Задачи:");
        System.out.println(
                taskManager.getAllTasks().stream().map(t -> "\t" + t.toString()).collect(Collectors.joining("\n")));
        System.out.println("Эпики:");
        System.out.println(
                taskManager.getAllEpics().stream().map(t -> "\t" + t.toString()).collect(Collectors.joining("\n")));
        System.out.println("Подзадачи:");
        System.out.println(
                taskManager.getAllSubtasks().stream().map(t -> "\t" + t.toString()).collect(Collectors.joining("\n")));
    }
}
