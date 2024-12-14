package ru.yandex.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.exceptions.TaskNotFoundException;
import ru.yandex.exceptions.TaskOverlapException;
import ru.yandex.model.Epic;
import ru.yandex.model.Subtask;
import ru.yandex.model.Task;
import ru.yandex.model.TaskStatus;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;

abstract class TaskManagerTest<T extends TaskManager> {

    private static TaskManager taskManager;
    private static LocalDateTime nowDateTime;
    protected final Supplier<? extends T> factory;

    TaskManagerTest(Supplier<? extends T> factory) {
        this.factory = factory;
    }

    @BeforeEach
    void setUp() {
        nowDateTime = LocalDateTime.now();
        taskManager = factory.get();
    }

    @Test
    void shouldAddTaskAndAssignIncrementingIds() {
        int firstTaskId = taskManager.addTask(
            new Task("_t1name_", "_t1desc_", Duration.ZERO, LocalDateTime.MIN));
        int secondTaskId = taskManager.addTask(
            new Task("_t2name_", "_t2desc_", Duration.ZERO, LocalDateTime.MIN.plusHours(1)));
        assertEquals(0, firstTaskId);
        assertEquals(1, secondTaskId);
        assertEquals("_t1name_", taskManager.getTask(firstTaskId).getName());
        assertEquals("_t2name_", taskManager.getTask(secondTaskId).getName());
    }

    @Test
    void shouldAddSubtaskAndAssignIncrementingIds() {
        int epicId = taskManager.addEpic(new Epic("_e1name_", "_e1desc_"));
        int firstSubtaskId = taskManager.addSubtask(
            new Subtask("_s1name_", "_s1desc_", epicId, Duration.ZERO, LocalDateTime.MIN));
        int secondSubtaskId = taskManager.addSubtask(
            new Subtask("_s2name_", "_s2desc_", epicId, Duration.ZERO,
                LocalDateTime.MIN.plusHours(1)));
        assertEquals(1, firstSubtaskId);
        assertEquals(2, secondSubtaskId);
        assertEquals("_s1name_", taskManager.getSubtask(firstSubtaskId).getName());
        assertEquals("_s2name_", taskManager.getSubtask(secondSubtaskId).getName());
    }

    @Test
    void shouldAddEpicAndAssignIncrementingIds() {
        int firstEpicId = taskManager.addEpic(new Epic("_e1name_", "_e1desc_"));
        int secondEpicId = taskManager.addEpic(new Epic("_e2name_", "_e2desc_"));
        assertEquals(0, firstEpicId);
        assertEquals(1, secondEpicId);
        assertEquals("_e1name_", taskManager.getEpic(firstEpicId).getName());
        assertEquals("_e2name_", taskManager.getEpic(secondEpicId).getName());
    }

    // в том числе проверяет, что подзадача не может быть собственным эпиком
    @Test
    void shouldThrowExceptionIfAddingSubtaskWithInvalidEpicId() {
        Subtask subtask = new Subtask("_s1name_", "_s1desc_", 42, Duration.ZERO, LocalDateTime.MIN);
        assertThrows(TaskNotFoundException.class, () -> taskManager.addSubtask(subtask));
    }

    @Test
    void shouldUpdateTaskData() {
        int taskId = taskManager.addTask(
            new Task("_t1name_", "_t1desc_", Duration.ZERO, nowDateTime));
        taskManager.updateTask(
            new Task("_anothertname_", "_anothertdesc_", taskId, TaskStatus.IN_PROGRESS,
                Duration.ofHours(14),
                nowDateTime.plusHours(14)));
        assertEquals("_anothertname_", taskManager.getTask(taskId).getName());
        assertEquals("_anothertdesc_", taskManager.getTask(taskId).getDescription());
        assertEquals(TaskStatus.IN_PROGRESS, taskManager.getTask(taskId).getStatus());
        assertEquals(Duration.ofHours(14), taskManager.getTask(taskId).getDuration());
        assertEquals(nowDateTime.plusHours(14), taskManager.getTask(taskId).getStartTime());
    }

    @Test
    void shouldUpdateSubtaskData() {
        int epicId = taskManager.addEpic(new Epic("_e1name_", "_e1desc_"));
        int subtaskId = taskManager.addSubtask(
            new Subtask("_s1name_", "_s1desc_", epicId, Duration.ZERO, LocalDateTime.MIN));
        taskManager.updateSubtask(
            new Subtask("_anothersname_", "_anothersdesc_", subtaskId, TaskStatus.IN_PROGRESS,
                epicId,
                Duration.ofHours(14), nowDateTime.plusHours(14)));
        assertEquals("_anothersname_", taskManager.getSubtask(subtaskId).getName());
        assertEquals("_anothersdesc_", taskManager.getSubtask(subtaskId).getDescription());
        assertEquals(Duration.ofHours(14), taskManager.getSubtask(subtaskId).getDuration());
        assertEquals(nowDateTime.plusHours(14), taskManager.getSubtask(subtaskId).getStartTime());
        assertEquals(TaskStatus.IN_PROGRESS, taskManager.getSubtask(subtaskId).getStatus());
    }

    @Test
    void shouldUpdateEpicData() {
        int epicId = taskManager.addEpic(new Epic("_e1name_", "_e1desc_"));
        taskManager.updateEpic(new Epic("_anotherename_", "_anotheredesc_", epicId));
        assertEquals("_anotherename_", taskManager.getEpic(epicId).getName());
        assertEquals("_anotheredesc_", taskManager.getEpic(epicId).getDescription());
    }

    @Test
    void shouldProvideAllTasks() {
        Task task1 = new Task("_t1name_", "_t1desc_", Duration.ZERO, nowDateTime);
        Task task2 = new Task("_t2name_", "_t2desc_", Duration.ZERO, nowDateTime);
        Task task3 = new Task("_t3name_", "_t3desc_", Duration.ZERO, nowDateTime);
        taskManager.addTask(task1);
        taskManager.addTask(task2);
        taskManager.addTask(task3);
        List<Task> allTasks = taskManager.getAllTasks();
        assertEquals(3, allTasks.size());
        assertTrue(
            allTasks.contains(task1) && allTasks.contains(task2) && allTasks.contains(task3));
    }

    @Test
    void shouldProvideAllEpics() {
        Epic epic1 = new Epic("_e1name_", "_e1desc_");
        Epic epic2 = new Epic("_e2name_", "_e2desc_");
        Epic epic3 = new Epic("_e3name_", "_e3desc_");
        taskManager.addEpic(epic1);
        taskManager.addEpic(epic2);
        taskManager.addEpic(epic3);
        List<Epic> allEpics = taskManager.getAllEpics();
        assertEquals(3, allEpics.size());
        assertTrue(
            allEpics.contains(epic1) && allEpics.contains(epic2) && allEpics.contains(epic3));
    }

    @Test
    void shouldProvideAllSubtasks() {
        Epic epic1 = new Epic("_e1name_", "_e1desc_");
        int firstEpicId = taskManager.addEpic(epic1);
        Subtask subtask1 = new Subtask("_s1name_", "_s1desc_", firstEpicId, Duration.ZERO,
            LocalDateTime.MIN);
        Subtask subtask2 = new Subtask("_s2name_", "_s2desc_", firstEpicId, Duration.ZERO,
            LocalDateTime.MIN);
        Subtask subtask3 = new Subtask("_s3name_", "_s3desc_", firstEpicId, Duration.ZERO,
            LocalDateTime.MIN);
        taskManager.addSubtask(subtask1);
        taskManager.addSubtask(subtask2);
        taskManager.addSubtask(subtask3);
        List<Subtask> allSubtasks = taskManager.getAllSubtasks();
        assertEquals(3, allSubtasks.size());
        assertTrue(allSubtasks.contains(subtask1) && allSubtasks.contains(subtask2)
            && allSubtasks.contains(subtask3));
    }

    @Test
    void shouldClearTasks() {
        int firstTaskId = taskManager.addTask(
            new Task("_t1name_", "_t1desc_", Duration.ZERO, LocalDateTime.MIN));
        int secondTaskId = taskManager.addTask(
            new Task("_t2name_", "_t2desc_", Duration.ZERO, LocalDateTime.MIN.plusHours(1)));
        taskManager.getTask(firstTaskId);
        taskManager.getTask(secondTaskId);
        taskManager.clearTasks();
        assertTrue(taskManager.getAllTasks().isEmpty());
        assertTrue(taskManager.getHistory().isEmpty());
    }

    @Test
    void shouldClearSubtasksAndRemoveThemFromEpics() {
        int firstEpicId = taskManager.addEpic(new Epic("_e1name_", "_e1desc_"));
        int firstSubtaskId = taskManager.addSubtask(
            new Subtask("_s1name_", "_s1desc_", firstEpicId, Duration.ZERO, LocalDateTime.MIN));
        int secondSubtaskId = taskManager.addSubtask(
            new Subtask("_s2name_", "_s2desc_", firstEpicId, Duration.ZERO,
                LocalDateTime.MIN.plusHours(1)));
        taskManager.getSubtask(firstSubtaskId);
        taskManager.getSubtask(secondSubtaskId);
        taskManager.clearSubtasks();
        assertTrue(taskManager.getSubtasks(firstEpicId).isEmpty());
        assertTrue(taskManager.getAllSubtasks().isEmpty());
        assertTrue(taskManager.getHistory().isEmpty());
    }

    @Test
    void shouldClearEpicsAndRemoveAllSubtasks() {
        int firstEpicId = taskManager.addEpic(new Epic("_e1name_", "_e1desc_"));
        int firstSubtaskId = taskManager.addSubtask(
            new Subtask("_s1name_", "_s1desc_", firstEpicId, Duration.ZERO, LocalDateTime.MIN));
        int secondSubtaskId = taskManager.addSubtask(
            new Subtask("_s2name_", "_s2desc_", firstEpicId, Duration.ZERO,
                LocalDateTime.MIN.plusHours(1)));
        taskManager.getEpic(firstEpicId);
        taskManager.getSubtask(firstSubtaskId);
        taskManager.getSubtask(secondSubtaskId);
        taskManager.clearEpics();
        assertTrue(taskManager.getAllEpics().isEmpty());
        assertTrue(taskManager.getAllSubtasks().isEmpty());
        assertTrue(taskManager.getHistory().isEmpty());
    }

    @Test
    void shouldReturnCorrectTaskById() {
        Task task = new Task("_t1name_", "_t1desc_", Duration.ZERO, LocalDateTime.MIN);
        int taskId = taskManager.addTask(task);
        assertEquals(task.hashCode(), taskManager.getTask(taskId).hashCode());
    }

    @Test
    void shouldReturnCorrectEpicById() {
        Epic epic = new Epic("_e1name_", "_e1desc_");
        int epicId = taskManager.addEpic(epic);
        assertEquals(epic.hashCode(), taskManager.getEpic(epicId).hashCode());
    }

    @Test
    void shouldReturnCorrectSubtaskById() {
        int epicId = taskManager.addEpic(new Epic("_e1name_", "_e1desc_"));
        Subtask subtask = new Subtask("_s1name_", "_s1desc_", epicId, Duration.ZERO,
            LocalDateTime.MIN);
        int subtaskId = taskManager.addSubtask(subtask);
        assertEquals(subtask, taskManager.getSubtask(subtaskId));
    }

    @Test
    void shouldNotHaveTaskPresentAfterRemoval() {
        taskManager.addTask(new Task("_t1name_", "_t1desc_", Duration.ZERO, LocalDateTime.MIN));
        int secondTaskId = taskManager.addTask(
            new Task("_t2name_", "_t2desc_", Duration.ZERO, LocalDateTime.MIN.plusHours(1)));
        int numberOfTasksSnapshot = taskManager.getAllTasks().size();
        taskManager.removeTask(secondTaskId);
        assertEquals(numberOfTasksSnapshot - 1, taskManager.getAllTasks().size());
        assertThrows(TaskNotFoundException.class, () -> taskManager.getTask(secondTaskId));
    }

    @Test
    void shouldNotHaveEpicPresentAfterRemoval() {
        taskManager.addEpic(new Epic("_e1name_", "_e1desc_"));
        int secondEpicId = taskManager.addEpic(new Epic("_e2name_", "_e2desc_"));
        int numberOfEpicsSnapshot = taskManager.getAllEpics().size();
        taskManager.removeEpic(secondEpicId);
        assertEquals(numberOfEpicsSnapshot - 1, taskManager.getAllEpics().size());
        assertThrows(TaskNotFoundException.class, () -> taskManager.getEpic(secondEpicId));
    }

    @Test
    void shouldNotHaveSubtaskPresentAfterRemoval() {
        int epicId = taskManager.addEpic(new Epic("_e1name_", "_e1desc_"));
        taskManager.addSubtask(
            new Subtask("_s1name_", "_s1desc_", epicId, Duration.ZERO, LocalDateTime.MIN));
        int secondSubtaskId = taskManager.addSubtask(
            new Subtask("_s2name_", "_s2desc_", epicId, Duration.ZERO,
                LocalDateTime.MIN.plusHours(1)));
        int numberOfSubtasksSnapshot = taskManager.getAllSubtasks().size();
        taskManager.removeSubtask(secondSubtaskId);
        assertEquals(numberOfSubtasksSnapshot - 1, taskManager.getAllSubtasks().size());
        assertThrows(TaskNotFoundException.class, () -> taskManager.getSubtask(secondSubtaskId));
        assertFalse(taskManager.getEpic(epicId).getSubtasks().containsKey(secondSubtaskId));
    }

    @Test
    void shouldReturnSubtasksOfEpicById() {
        int epicId = taskManager.addEpic(new Epic("_e1name_", "_e1desc_"));
        Subtask subtask1 = new Subtask("_s1name_", "_s1desc_", epicId, Duration.ZERO,
            LocalDateTime.MIN);
        Subtask subtask2 = new Subtask("_s2name_", "_s2desc_", epicId, Duration.ZERO,
            LocalDateTime.MIN.plusHours(1));
        taskManager.addSubtask(subtask1);
        taskManager.addSubtask(subtask2);
        assertTrue(taskManager.getSubtasks(epicId).contains(subtask1));
        assertTrue(taskManager.getSubtasks(epicId).contains(subtask2));
    }

    @Test
    void shouldReturnTasksSortedByStartTime() {
        Task task = new Task("_t1name_", "_t1desc_", Duration.ZERO, LocalDateTime.MIN.plusHours(1));
        taskManager.addTask(task);
        Epic epic1 = new Epic("_e1name_", "_e1desc_");
        Epic epic2 = new Epic("_e2name_", "_e2desc_");
        int firstEpicId = taskManager.addEpic(epic1);
        taskManager.addEpic(epic2);
        Subtask subtask = new Subtask("_s2name_", "_s2desc_", firstEpicId, Duration.ZERO,
            LocalDateTime.MIN.plusHours(2));
        taskManager.addSubtask(subtask);
        assertArrayEquals(new Task[]{task, subtask}, taskManager.getPrioritizedTasks().toArray());
    }

    @Test
    void shouldReturnHistoryForAccessedObjects() {
        Task task = new Task("_t1name_", "_t1desc_", Duration.ZERO, LocalDateTime.MIN);
        int taskId = taskManager.addTask(task);
        Epic epic1 = new Epic("_e1name_", "_e1desc_");
        Epic epic2 = new Epic("_e2name_", "_e2desc_");
        int firstEpicId = taskManager.addEpic(epic1);
        int secondEpicId = taskManager.addEpic(epic2);
        Subtask subtask = new Subtask("_s2name_", "_s2desc_", firstEpicId, Duration.ZERO,
            LocalDateTime.MIN.plusHours(1));
        int subtaskId = taskManager.addSubtask(subtask);
        taskManager.getTask(taskId);
        taskManager.getEpic(secondEpicId);
        taskManager.getSubtask(subtaskId);
        List<Task> history = taskManager.getHistory();
        assertArrayEquals(new Task[]{task, epic2, subtask}, history.toArray());
    }

    @Test
    void shouldProperlyUpdateRepeatingHistoryEntries() {
        Task task1 = new Task("_t1name_", "_t1desc_", Duration.ZERO, LocalDateTime.MIN);
        Task task2 = new Task("_t2name_", "_t2desc_", Duration.ZERO,
            LocalDateTime.MIN.plusHours(1));
        taskManager.addTask(task1);
        taskManager.addTask(task2);
        taskManager.getTask(task1.getId());
        taskManager.getTask(task2.getId());
        taskManager.getTask(task1.getId());
        List<Task> history = taskManager.getHistory();
        assertArrayEquals(new Task[]{task2, task1}, history.toArray());
    }

    @Test
    void shouldStoreUnmodifiedVersionsOfTasksInHistory() {
        Task task = new Task("_t1name_", "_t1desc_", Duration.ZERO, LocalDateTime.MIN);
        int taskId = taskManager.addTask(task);
        Epic epic = new Epic("_e1name_", "_e1desc_");
        int epicId = taskManager.addEpic(epic);
        Subtask subtask = new Subtask("_s2name_", "_s2desc_", epicId, Duration.ZERO,
            LocalDateTime.MIN.plusHours(1));
        int subtaskId = taskManager.addSubtask(subtask);
        taskManager.getTask(taskId);
        taskManager.getEpic(epicId);
        taskManager.getSubtask(subtaskId);
        Task anotherTask = new Task("_anothertname_", "_anothertdesc_", taskId,
            TaskStatus.IN_PROGRESS, Duration.ZERO,
            LocalDateTime.MIN);
        taskManager.updateTask(anotherTask);
        taskManager.removeEpic(epicId);
        List<Task> history = taskManager.getHistory();
        assertEquals("_t1name_", history.getFirst().getName());
    }

    @Test
    void shouldAddTaskUnchanged() {
        Task task = new Task("_tname_", "_tdesc_", -1, TaskStatus.IN_PROGRESS, Duration.ofHours(42),
            nowDateTime);
        int taskId = taskManager.addTask(task);
        assertEquals("_tname_", taskManager.getTask(taskId).getName());
        assertEquals("_tdesc_", taskManager.getTask(taskId).getDescription());
        assertEquals(TaskStatus.IN_PROGRESS, taskManager.getTask(taskId).getStatus());
        assertEquals(Duration.ofHours(42), taskManager.getTask(taskId).getDuration());
        assertEquals(nowDateTime, taskManager.getTask(taskId).getStartTime());
    }

    @Test
    void shouldNowAllowOverlappingTasks() {
        Task task1 = new Task("_t1name_", "_t1desc_", -1, TaskStatus.IN_PROGRESS,
            Duration.ofHours(42), nowDateTime);
        Task task2 = new Task("_t2name_", "_t2desc_", -1, TaskStatus.IN_PROGRESS,
            Duration.ofHours(69),
            nowDateTime.plusHours(69));
        Task task3 = new Task("_t3name_", "_t3desc_", -1, TaskStatus.IN_PROGRESS,
            Duration.ofHours(69),
            nowDateTime.plusHours(14));
        taskManager.addTask(task1);
        int task2id = taskManager.addTask(task2);
        assertThrows(TaskOverlapException.class, () -> taskManager.addTask(task3));
        Task goodUpdate = new Task("_t2name_", "_t2desc_", task2id, TaskStatus.IN_PROGRESS,
            Duration.ofHours(69),
            nowDateTime.plusHours(42));
        Task badUpdate = new Task("_t2name_", "_t2desc_", task2id, TaskStatus.IN_PROGRESS,
            Duration.ofHours(69),
            nowDateTime.plusHours(14));
        assertDoesNotThrow(() -> taskManager.updateTask(goodUpdate));
        assertThrows(TaskOverlapException.class, () -> taskManager.updateTask(badUpdate));
    }
}