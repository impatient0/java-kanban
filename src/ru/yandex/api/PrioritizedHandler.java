package ru.yandex.api;

import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import ru.yandex.service.TaskManager;

public class PrioritizedHandler extends RequestHandler {

    PrioritizedHandler(TaskManager taskManager) {
        super(taskManager);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        switch (RequestParser.getEndpoint(exchange)) {
            case GET_ALL -> {
                String body = gson.toJson(taskManager.getPrioritizedTasks());
                sendText(exchange, body, 200);
            }
            case null, default -> sendBadRequest(exchange);
        }
    }
}
