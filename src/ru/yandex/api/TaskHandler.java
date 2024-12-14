package ru.yandex.api;

import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import ru.yandex.exceptions.TaskNotFoundException;
import ru.yandex.exceptions.TaskOverlapException;
import ru.yandex.model.Task;
import ru.yandex.service.TaskManager;

public class TaskHandler extends RequestHandler {

    TaskHandler(TaskManager taskManager) {
        super(taskManager);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        switch (RequestParser.getEndpoint(exchange)) {
            case GET_ALL -> {
                String body = gson.toJson(taskManager.getAllTasks());
                System.err.println(body);
                sendText(exchange, body, 200);
            }
            case GET_BY_ID -> {
                int id = Integer.parseInt(exchange.getRequestURI().getPath().split("/")[2]);
                try {
                    String body = gson.toJson(taskManager.getTask(id));
                    sendText(exchange, body, 200);
                } catch (TaskNotFoundException e) {
                    sendNotFound(exchange);
                }
            }
            case CREATE -> {
                try {
                    Task task = gson.fromJson(exchange.getRequestBody().toString(), Task.class);
                    if (task.getId() == -1) {
                        taskManager.addTask(task);
                    } else {
                        taskManager.updateTask(task);
                    }
                    sendText(exchange, "", 201);
                } catch (JsonSyntaxException e) {
                    sendBadRequest(exchange);
                } catch (TaskOverlapException e) {
                    sendHasIntersections(exchange);
                }
            }
            case REMOVE -> {
                int id = Integer.parseInt(exchange.getRequestURI().getPath().split("/")[2]);
                taskManager.removeTask(id);
                sendText(exchange, "", 200);
            }
            default -> sendBadRequest(exchange);
        }
    }
}
