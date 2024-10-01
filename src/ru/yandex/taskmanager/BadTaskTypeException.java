package ru.yandex.taskmanager;

public class BadTaskTypeException extends RuntimeException {
    public BadTaskTypeException(String message) {
        super(message);
    }
}
