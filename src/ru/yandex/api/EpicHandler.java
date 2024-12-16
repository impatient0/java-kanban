package ru.yandex.api;

import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import ru.yandex.exceptions.TaskNotFoundException;
import ru.yandex.model.Epic;
import ru.yandex.service.TaskManager;

public class EpicHandler extends RequestHandler {

    EpicHandler(TaskManager taskManager) {
        super(taskManager);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        switch (RequestParser.getEndpoint(exchange)) {
            case GET_ALL -> {
                String body = gson.toJson(taskManager.getAllEpics());
                sendText(exchange, body, 200);
            }
            case GET_BY_ID -> {
                int id = Integer.parseInt(exchange.getRequestURI().getPath().split("/")[2]);
                try {
                    String body = gson.toJson(taskManager.getEpic(id));
                    sendText(exchange, body, 200);
                } catch (TaskNotFoundException e) {
                    sendNotFound(exchange);
                } catch (Exception e) {
                    sendInternalError(exchange);
                }
            }
            case GET_SUBTASKS -> {
                try {
                    int id = Integer.parseInt(exchange.getRequestURI().getPath().split("/")[2]);
                    String body = gson.toJson(taskManager.getSubtasks(id));
                    sendText(exchange, body, 200);
                } catch (Exception e) {
                    sendInternalError(exchange);
                }
            }
            case CREATE -> {
                try {
                    Epic epic = gson.fromJson(
                        new String(exchange.getRequestBody().readAllBytes(), DEFAULT_CHARSET),
                        Epic.class);
                    taskManager.addEpic(epic);
                    sendText(exchange, "", 201);
                } catch (JsonSyntaxException e) {
                    sendBadRequest(exchange);
                } catch (Exception e) {
                    sendInternalError(exchange);
                }
            }
            case REMOVE -> {
                try {
                    int id = Integer.parseInt(exchange.getRequestURI().getPath().split("/")[2]);
                    taskManager.removeEpic(id);
                    sendText(exchange, "", 200);
                } catch (Exception e) {
                    sendInternalError(exchange);
                }
            }
            case UNKNOWN -> sendBadRequest(exchange);
        }
    }
}
