package ru.yandex.model;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

class EpicTest {

    private static Epic epic;
    private static final Subtask subtask1 = new Subtask("_s1name_", "_s1desc_", 1, TaskStatus.NEW, 42);
    private static final Subtask subtask2 = new Subtask("_s2name_", "_s2desc_", 2, TaskStatus.NEW, 42);
    private static final Subtask subtask3 = new Subtask("_s3name_", "_s3desc_", 3, TaskStatus.IN_PROGRESS, 42);

    @BeforeEach
    void fillSubtasks() {
        epic = new Epic("_ename_", "_edesc_", 42);
        epic.addSubtask(subtask1);
        epic.addSubtask(subtask2);
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void shouldConvertToString() {
        String s = "Эпик #00000042 \"_ename_\": _edesc_ Статус: NEW; Подзадачи: #00000001, #00000002";
        assertEquals(s, epic.toString());
    }

    @Test
    void shouldBeIN_PROGRESSWhenSubtasksUpdate() {
        epic.addSubtask(subtask3);
        assertEquals(TaskStatus.IN_PROGRESS, epic.getStatus());
    }

    @Test
    void shouldAddSubtask() {
        epic.addSubtask(subtask3);
        assertTrue(epic.getSubtasks().containsKey(3) && epic.getSubtasks().get(3) == TaskStatus.IN_PROGRESS);
    }

    @Test
    void shouldRemoveSubtask() {
        epic.removeSubtask(subtask2);
        assertFalse(epic.getSubtasks().containsKey(2));
    }

    @Test
    void shouldClearSubtasks() {
        epic.clearSubtasks();
        assertEquals(epic.getSubtasks().values().size(), 0);
    }
}