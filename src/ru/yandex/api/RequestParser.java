package ru.yandex.api;

import com.sun.net.httpserver.HttpExchange;

import static ru.yandex.api.Endpoint.*;

public class RequestParser {

    public static Endpoint getEndpoint(HttpExchange exchange) {
        String method = exchange.getRequestMethod();
        String[] path = exchange.getRequestURI().getPath().split("/");
        switch (method) {
            case "GET" -> {
                if (path.length == 2) {
                    return GET_ALL;
                }
                try {
                    Integer.parseInt(path[2]);
                    if (path.length == 3) {
                        return GET_BY_ID;
                    }
                    if (path.length == 4 && path[3].equals("subtasks")) {
                        return GET_SUBTASKS;
                    }
                    return UNKNOWN;
                } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
                    return UNKNOWN;
                }
            }
            case "POST" -> {
                return CREATE;
            }
            case "DELETE" -> {
                try {
                    Integer.parseInt(path[2]);
                    return REMOVE;
                } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
                    return UNKNOWN;
                }
            }
            default -> {
                return UNKNOWN;
            }
        }
    }
}
