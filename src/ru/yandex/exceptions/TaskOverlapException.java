package ru.yandex.exceptions;

public class TaskOverlapException extends RuntimeException {
    public TaskOverlapException(String message) {
        super(message);
    }
}
