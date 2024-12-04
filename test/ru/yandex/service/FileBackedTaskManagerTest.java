package ru.yandex.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.exceptions.ManagerLoadException;
import ru.yandex.exceptions.ManagerSaveException;
import ru.yandex.model.Epic;
import ru.yandex.model.Subtask;
import ru.yandex.model.Task;
import ru.yandex.model.TaskStatus;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class FileBackedTaskManagerTest extends TaskManagerTest<FileBackedTaskManager> {

    private final Path testSaveFile = Paths.get("test/ru/yandex/service/resource/test_save_file.txt"), badSaveFile
            = Paths.get("test/ru/yandex/service/resource/bad_save_file.txt");
    private FileBackedTaskManager taskManager;
    private LocalDateTime nowDateTime;

    FileBackedTaskManagerTest() {
        super(() -> {
            try {
                return new FileBackedTaskManager(File.createTempFile("test_save_file", ".tmp").toPath());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @BeforeEach
    void setUpForFileBackedTaskManager() {
        taskManager = factory.get();
        nowDateTime = LocalDateTime.now();
    }

    @Test
    void shouldLoadFromEmptyFile() {
        assertTrue(taskManager.getAllTasks().isEmpty() && taskManager.getAllSubtasks().isEmpty()
                && taskManager.getAllEpics().isEmpty());
    }

    @Test
    void shouldCreateSaveFileWhenNoFileExistsUnderPath() throws IOException {
        Files.delete(taskManager.getSaveFile());
        assertFalse(Files.exists(taskManager.getSaveFile()));
        assertTrue(taskManager.getAllTasks().isEmpty() && taskManager.getAllSubtasks().isEmpty()
                && taskManager.getAllEpics().isEmpty());
        taskManager = new FileBackedTaskManager(taskManager.getSaveFile());
        assertTrue(Files.exists(taskManager.getSaveFile()));
    }

    @Test
    void shouldLoadTasksFromFile() {
        TaskManager tm_test = FileBackedTaskManager.loadFromFile(testSaveFile.toFile());
        Task task = tm_test.getTask(0);
        Epic epic = tm_test.getEpic(1);
        Subtask subtask = tm_test.getSubtask(2);
        assertEquals("Подготовка к экзамену", task.getName());
        assertEquals(TaskStatus.NEW, epic.getStatus());
        assertEquals(
                "Изучение и анализ существующих рабочих процессов компании для выявления узких мест и возможностей "
                        + "для оптимизации.", subtask.getDescription());
        assertEquals(1, subtask.getEpicId());
        assertTrue(epic.getSubtasks().containsKey(2));
    }

    @Test
    void shouldSaveToAndLoadTasksFromFile() {
        Task task = new Task("Подготовка к экзамену", "Составить план подготовки к экзамену.", Duration.ofHours(42),
                nowDateTime);
        taskManager.addTask(task);
        Epic epic = new Epic("Оптимизация рабочего процесса",
                "Оптимизация рабочего процесса компании для повышения эффективности и продуктивности сотрудников.");
        int e1 = taskManager.addEpic(epic);
        Subtask subtask = new Subtask("Анализ текущих процессов",
                "Изучение и анализ существующих рабочих процессов компании для выявления узких мест и возможностей "
                        + "для оптимизации.", e1, Duration.ofHours(69), nowDateTime.plusHours(100));
        taskManager.addSubtask(subtask);
        assertDoesNotThrow(() -> FileBackedTaskManager.loadFromFile(taskManager.getSaveFile().toFile()));
        TaskManager tm2 = FileBackedTaskManager.loadFromFile(taskManager.getSaveFile().toFile());
        assertEquals(taskManager.getTask(0).getName(), tm2.getTask(0).getName());
        assertEquals(taskManager.getTask(0).getDescription(), tm2.getTask(0).getDescription());
        assertEquals(taskManager.getTask(0).getStatus(), tm2.getTask(0).getStatus());
        assertEquals(taskManager.getTask(0).getStartTime(), tm2.getTask(0).getStartTime());
        assertEquals(taskManager.getTask(0).getDuration(), tm2.getTask(0).getDuration());
        assertEquals(taskManager.getEpic(1).getName(), tm2.getEpic(1).getName());
        assertEquals(taskManager.getEpic(1).getDescription(), tm2.getEpic(1).getDescription());
        assertEquals(taskManager.getEpic(1).getStatus(), tm2.getEpic(1).getStatus());
        assertEquals(taskManager.getEpic(1).getStartTime(), tm2.getEpic(1).getStartTime());
        assertEquals(taskManager.getEpic(1).getDuration(), tm2.getEpic(1).getDuration());
        assertEquals(taskManager.getEpic(1).getEndTime(), tm2.getEpic(1).getEndTime());
        assertEquals(taskManager.getSubtask(2).getName(), tm2.getSubtask(2).getName());
        assertEquals(taskManager.getSubtask(2).getDescription(), tm2.getSubtask(2).getDescription());
        assertEquals(taskManager.getSubtask(2).getStatus(), tm2.getSubtask(2).getStatus());
        assertEquals(taskManager.getSubtask(2).getStartTime(), tm2.getSubtask(2).getStartTime());
        assertEquals(taskManager.getSubtask(2).getDuration(), tm2.getSubtask(2).getDuration());
        assertEquals(taskManager.getSubtask(2).getEpicId(), tm2.getSubtask(2).getEpicId());
    }

    @Test
    void shouldSaveTasksToFile() throws IOException {
        Task task = new Task("Подготовка к экзамену", "Составить план подготовки к экзамену.", Duration.ofHours(42),
                nowDateTime);
        taskManager.addTask(task);
        Epic epic = new Epic("Оптимизация рабочего процесса",
                "Оптимизация рабочего процесса компании для повышения эффективности и продуктивности сотрудников.");
        int e1 = taskManager.addEpic(epic);
        Subtask subtask = new Subtask("Анализ текущих процессов",
                "Изучение и анализ существующих рабочих процессов компании для выявления узких мест и возможностей "
                        + "для оптимизации.", e1, Duration.ofHours(69), nowDateTime.plusHours(100));
        taskManager.addSubtask(subtask);
        try (BufferedReader input = new BufferedReader(new FileReader(taskManager.getSaveFile().toFile()))) {
            input.readLine();
            assertEquals(task.getCSV(), input.readLine());
            assertEquals(epic.getCSV(), input.readLine());
            assertEquals(subtask.getCSV(), input.readLine());
        }
    }

    @Test
    void shouldThrowExceptionOnInvalidFile() {
        assertThrows(ManagerLoadException.class, () -> FileBackedTaskManager.loadFromFile(badSaveFile.toFile()));
    }

    @Test
    void shouldPreserveIdsUponDeletion() {
        Task task = new Task("Подготовка к экзамену", "Составить план подготовки к экзамену.", Duration.ofHours(42),
                nowDateTime);
        taskManager.addTask(task);
        Epic epic = new Epic("Оптимизация рабочего процесса",
                "Оптимизация рабочего процесса компании для повышения эффективности и продуктивности сотрудников.");
        int e1 = taskManager.addEpic(epic);
        Subtask subtask = new Subtask("Анализ текущих процессов",
                "Изучение и анализ существующих рабочих процессов компании для выявления узких мест и возможностей "
                        + "для оптимизации.", e1, Duration.ofHours(69), nowDateTime.plusHours(100));
        taskManager.addSubtask(subtask);
        taskManager.removeTask(0);
        TaskManager tm2 = FileBackedTaskManager.loadFromFile(taskManager.getSaveFile().toFile());
        assertTrue(tm2.getAllTasks().isEmpty());
        assertTrue(tm2.getEpic(1).getSubtasks().containsKey(2));
    }

    @Test
    void shouldThrowExceptionOnIncorrectSaveFilePath() {
        assertThrows(ManagerSaveException.class, () -> new FileBackedTaskManager(Path.of("lol/kek/cheburek")));
    }
}