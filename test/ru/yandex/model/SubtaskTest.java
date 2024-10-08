package ru.yandex.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SubtaskTest {

    private static final Subtask subtask = new Subtask("_sname_", "_sdesc_", 1, TaskStatus.NEW, 42);

    @Test
    void shouldConvertSubtaskToString() {
        String s = "Подзадача #00000001 \"_sname_\": _sdesc_ Статус: NEW; Относится к эпику #00000042";
        assertEquals(s, subtask.toString());
    }

    @Test
    void shouldTreatSubtasksWithSameIdAsEqual() {
        Subtask anotherSubtask = new Subtask("_anothersname_", "_anothersdesc_", 1, TaskStatus.DONE, 69);
        assertEquals(subtask, anotherSubtask);
    }

}