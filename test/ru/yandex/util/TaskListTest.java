package ru.yandex.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.model.Task;
import ru.yandex.model.TaskStatus;

import static org.junit.jupiter.api.Assertions.*;

class TaskListTest {

    private TaskList taskList;
    private Task task1;
    private Task task2;


    @BeforeEach
    void setUp() {
        taskList = new TaskList();
        task1 = new Task("_t1name_", "_t1desc_", 42, TaskStatus.NEW);
        task2 = new Task("_t2name_", "_t2desc_", 69, TaskStatus.NEW);
    }

    @Test
    void shouldAddTasksToList() {
        taskList.addLast(task1);
        taskList.addLast(task2);
        assertArrayEquals(new Task[]{task1, task2}, taskList.toList().toArray());
    }

    @Test
    void shouldRemoveTasksFromList() {
        taskList.addLast(task1);
        taskList.addLast(task2);
        taskList.remove(task1.getId());
        assertArrayEquals(new Task[]{task2}, taskList.toList().toArray());
    }
}