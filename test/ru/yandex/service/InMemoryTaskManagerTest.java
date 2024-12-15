package ru.yandex.service;

public class InMemoryTaskManagerTest extends TaskManagerTest<InMemoryTaskManager> {

    InMemoryTaskManagerTest() {
        super(InMemoryTaskManager::new);
    }

}
