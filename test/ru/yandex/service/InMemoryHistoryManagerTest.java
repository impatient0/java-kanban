package ru.yandex.service;

class InMemoryHistoryManagerTest extends HistoryManagerTest<InMemoryHistoryManager> {

    InMemoryHistoryManagerTest() {
        super(InMemoryHistoryManager::new);
    }
}