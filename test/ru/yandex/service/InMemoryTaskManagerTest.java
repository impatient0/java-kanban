package ru.yandex.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.exceptions.TaskNotFoundException;
import ru.yandex.model.Epic;
import ru.yandex.model.Subtask;
import ru.yandex.model.Task;
import ru.yandex.model.TaskStatus;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryTaskManagerTest {

    private static TaskManager taskManager;
    private static Task task1;
    private static Task task2;
    private static Epic epic1;
    private static Epic epic2;
    private static Subtask subtask1;
    private static Subtask subtask2;
    private static int firstTaskId;
    private static int secondTaskId;
    private static int firstEpicId;
    private static int secondEpicId;
    private static int firstSubtaskId;
    private static int secondSubtaskId;

    @BeforeEach
    void setUp() {
        taskManager = new InMemoryTaskManager();
        task1 = new Task("_t1name_", "_t1desc_");
        task2 = new Task("_t2name_", "_t2desc_");
        epic1 = new Epic("_e1name_", "_e1desc_");
        epic2 = new Epic("_e2name_", "_e2desc_");
    }

    private void addTasks() {
        firstTaskId = taskManager.addTask(task1);
        secondTaskId = taskManager.addTask(task2);
    }

    private void addEpics() {
        firstEpicId = taskManager.addEpic(epic1);
        secondEpicId = taskManager.addEpic(epic2);
    }

    private void addSubtasks() {
        subtask1 = new Subtask("_s1name_", "_s1desc_", firstEpicId);
        subtask2 = new Subtask("_s2name_", "_s2desc_", firstEpicId);
        firstSubtaskId = taskManager.addSubtask(subtask1);
        secondSubtaskId = taskManager.addSubtask(subtask2);
    }

    @Test
    void shouldAddTaskAndAssignIncrementingIds() {
        addTasks();
        assertEquals(0, firstTaskId);
        assertEquals(1, secondTaskId);
        assertEquals("_t1name_", taskManager.getTask(firstTaskId).getName());
        assertEquals("_t2name_", taskManager.getTask(secondTaskId).getName());
    }

    @Test
    void shouldAddSubtaskAndAssignIncrementingIds() {
        addEpics();
        addSubtasks();
        assertEquals(2, firstSubtaskId);
        assertEquals(3, secondSubtaskId);
        assertEquals("_s1name_", taskManager.getSubtask(firstSubtaskId).getName());
        assertEquals("_s2name_", taskManager.getSubtask(secondSubtaskId).getName());
    }

    @Test
    void shouldAddEpicAndAssignIncrementingIds() {
        addEpics();
        assertEquals(0, firstEpicId);
        assertEquals(1, secondEpicId);
        assertEquals("_e1name_", taskManager.getEpic(firstEpicId).getName());
        assertEquals("_e2name_", taskManager.getEpic(secondEpicId).getName());
    }

    // в том числе проверяет, что подзадача не может быть собственным эпиком
    @Test
    void shouldThrowExceptionIfAddingSubtaskWithInvalidEpicId() {
        subtask1 = new Subtask("_s1name_", "_s1desc_", 42);
        assertThrows(TaskNotFoundException.class, () -> taskManager.addSubtask(subtask1));
    }

    @Test
    void shouldUpdateTaskData() {
        addTasks();
        Task anotherTask = new Task("_anothertname_", "_anothertdesc_", firstTaskId, TaskStatus.IN_PROGRESS);
        taskManager.updateTask(anotherTask);
        assertEquals("_anothertname_", taskManager.getTask(firstTaskId).getName());
        assertEquals("_anothertdesc_", taskManager.getTask(firstTaskId).getDescription());
        assertEquals(TaskStatus.IN_PROGRESS, taskManager.getTask(firstTaskId).getStatus());
    }

    @Test
    void shouldUpdateSubtaskData() {
        addEpics();
        addSubtasks();
        Subtask anotherSubtask = new Subtask("_anothersname_", "_anothersdesc_", firstSubtaskId, TaskStatus.IN_PROGRESS,
                firstEpicId);
        taskManager.updateSubtask(anotherSubtask);
        assertEquals("_anothersname_", taskManager.getSubtask(firstSubtaskId).getName());
        assertEquals("_anothersdesc_", taskManager.getSubtask(firstSubtaskId).getDescription());
        assertEquals(TaskStatus.IN_PROGRESS, taskManager.getSubtask(firstSubtaskId).getStatus());
    }

    @Test
    void shouldUpdateEpicData() {
        addEpics();
        Epic anotherEpic = new Epic("_anotherename_", "_anotheredesc_", firstEpicId);
        taskManager.updateEpic(anotherEpic);
        assertEquals("_anotherename_", taskManager.getEpic(firstEpicId).getName());
        assertEquals("_anotheredesc_", taskManager.getEpic(firstEpicId).getDescription());
    }

    @Test
    void shouldClearTasks() {
        addTasks();
        taskManager.clearTasks();
        assertTrue(taskManager.getAllTasks().isEmpty());
    }

    @Test
    void shouldClearSubtasksAndRemoveThemFromEpics() {
        addEpics();
        addSubtasks();
        taskManager.clearSubtasks();
        assertTrue(taskManager.getSubtasks(firstEpicId).isEmpty());
        assertTrue(taskManager.getAllSubtasks().isEmpty());
    }

    @Test
    void shouldClearEpicsAndRemoveAllSubtasks() {
        addEpics();
        addSubtasks();
        taskManager.clearEpics();
        assertTrue(taskManager.getAllEpics().isEmpty());
        assertTrue(taskManager.getAllSubtasks().isEmpty());
    }

    @Test
    void shouldReturnCorrectTaskById() {
        addTasks();
        assertEquals(task1.hashCode(), taskManager.getTask(firstTaskId).hashCode());
    }

    @Test
    void shouldReturnCorrectEpicById() {
        addEpics();
        assertEquals(epic1.hashCode(), taskManager.getEpic(firstEpicId).hashCode());
    }

    @Test
    void shouldReturnCorrectSubtaskById() {
        addEpics();
        addSubtasks();
        assertEquals(subtask1.hashCode(), taskManager.getSubtask(firstSubtaskId).hashCode());
    }

    @Test
    void shouldNotHaveTaskPresentAfterRemoval() {
        addTasks();
        int numberOfTasksSnapshot = taskManager.getAllTasks().size();
        taskManager.removeTask(secondTaskId);
        assertEquals(numberOfTasksSnapshot - 1, taskManager.getAllTasks().size());
        assertNull(taskManager.getTask(secondTaskId));
    }

    @Test
    void shouldNotHaveEpicPresentAfterRemoval() {
        addEpics();
        int numberOfEpicsSnapshot = taskManager.getAllEpics().size();
        taskManager.removeEpic(secondEpicId);
        assertEquals(numberOfEpicsSnapshot - 1, taskManager.getAllEpics().size());
        assertNull(taskManager.getEpic(secondEpicId));
    }

    @Test
    void shouldNotHaveSubtaskPresentAfterRemoval() {
        addEpics();
        addSubtasks();
        int numberOfSubtasksSnapshot = taskManager.getAllSubtasks().size();
        taskManager.removeSubtask(secondSubtaskId);
        assertEquals(numberOfSubtasksSnapshot - 1, taskManager.getAllSubtasks().size());
        assertNull(taskManager.getSubtask(secondSubtaskId));
    }

    @Test
    void shouldReturnSubtasksOfEpicById() {
        addEpics();
        addSubtasks();
        assertTrue(taskManager.getSubtasks(firstEpicId).contains(subtask1));
        assertTrue(taskManager.getSubtasks(firstEpicId).contains(subtask2));
    }

    @Test
    void shouldReturnHistoryForAccessedObjects() {
        addTasks();
        addEpics();
        addSubtasks();
        taskManager.getTask(firstTaskId);
        taskManager.getEpic(secondEpicId);
        taskManager.getSubtask(secondSubtaskId);
        ArrayList<Task> history = taskManager.getHistory();
        assertArrayEquals(new Task[]{task1, epic2, subtask2}, history.toArray());
    }

    @Test
    void shouldStoreUnmodifiedVersionsOfTasksInHistory() {
        addTasks();
        addEpics();
        addSubtasks();
        taskManager.getTask(firstTaskId);
        taskManager.getEpic(firstEpicId);
        taskManager.getSubtask(secondSubtaskId);
        Task anotherTask = new Task("_anothertname_", "_anothertdesc_", firstTaskId, TaskStatus.IN_PROGRESS);
        taskManager.updateTask(anotherTask);
        taskManager.updateTask(anotherTask);
        taskManager.removeEpic(firstEpicId);
        ArrayList<Task> history = taskManager.getHistory();
        assertEquals("_t1name_", history.get(0).getName());
        assertEquals("_e1name_", history.get(1).getName());
        assertEquals("_s2name_", history.get(2).getName());
    }

    @Test
    void shouldAddTaskUnchanged() {
        Task task = new Task("_tname_", "_tdesc_", -1, TaskStatus.IN_PROGRESS);
        int taskId = taskManager.addTask(task);
        assertEquals("_tname_", taskManager.getTask(taskId).getName());
        assertEquals("_tdesc_", taskManager.getTask(taskId).getDescription());
        assertEquals(TaskStatus.IN_PROGRESS, taskManager.getTask(taskId).getStatus());
    }
}