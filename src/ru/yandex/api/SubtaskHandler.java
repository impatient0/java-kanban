package ru.yandex.api;

import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import ru.yandex.exceptions.TaskNotFoundException;
import ru.yandex.exceptions.TaskOverlapException;
import ru.yandex.model.Subtask;
import ru.yandex.service.TaskManager;

public class SubtaskHandler extends RequestHandler {

    SubtaskHandler(TaskManager taskManager) {
        super(taskManager);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        switch (RequestParser.getEndpoint(exchange)) {
            case GET_ALL -> {
                String body = gson.toJson(taskManager.getAllSubtasks());
                sendText(exchange, body, 200);
            }
            case GET_BY_ID -> {
                int id = Integer.parseInt(exchange.getRequestURI().getPath().split("/")[2]);
                try {
                    String body = gson.toJson(taskManager.getSubtask(id));
                    sendText(exchange, body, 200);
                } catch (TaskNotFoundException e) {
                    sendNotFound(exchange);
                }
            }
            case CREATE -> {
                try {
                    Subtask subtask = gson.fromJson(
                        new String(exchange.getRequestBody().readAllBytes(), DEFAULT_CHARSET),
                        Subtask.class);
                    if (subtask.getId() == -1) {
                        taskManager.addSubtask(subtask);
                    } else {
                        taskManager.updateSubtask(subtask);
                    }
                } catch (JsonSyntaxException e) {
                    sendBadRequest(exchange);
                } catch (TaskOverlapException e) {
                    sendHasIntersections(exchange);
                } catch (TaskNotFoundException e) {
                    sendNotFound(exchange);
                }
                sendText(exchange, "", 201);
            }
            case REMOVE -> {
                int id = Integer.parseInt(exchange.getRequestURI().getPath().split("/")[2]);
                taskManager.removeSubtask(id);
                sendText(exchange, "", 200);
            }
            case null, default -> sendBadRequest(exchange);
        }
    }
}
