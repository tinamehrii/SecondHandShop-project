package com.secondhand.frontend;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

/**
 * A small helper class that sends HTTP requests to the backend.
 * All methods return the parsed JSON of the response body.
 * When the server returns an error, an ApiException with the
 * Persian message of the server is thrown.
 */
public class ApiClient {

    private static final HttpClient client = HttpClient.newHttpClient();
    private static final Gson gson = new Gson();

    private ApiClient() {
    }

    public static JsonElement get(String path) {
        return send("GET", path, null);
    }

    public static JsonElement post(String path, JsonObject body) {
        return send("POST", path, body);
    }

    public static JsonElement put(String path, JsonObject body) {
        return send("PUT", path, body);
    }

    public static JsonElement delete(String path) {
        return send("DELETE", path, null);
    }

    /** Encodes one query parameter value (for Persian words in the search). */
    public static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private static JsonElement send(String method, String path, JsonObject body) {
        try {
            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(URI.create(ApiConfig.BASE_URL + path))
                    .header("Content-Type", "application/json");

            // attach the JWT token when the user is logged in
            if (SessionManager.getToken() != null) {
                builder.header("Authorization", "Bearer " + SessionManager.getToken());
            }

            HttpRequest.BodyPublisher publisher = (body == null)
                    ? HttpRequest.BodyPublishers.noBody()
                    : HttpRequest.BodyPublishers.ofString(gson.toJson(body), StandardCharsets.UTF_8);
            builder.method(method, publisher);

            HttpResponse<String> response = client.send(builder.build(),
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

            if (response.statusCode() >= 400) {
                String message = "خطای ناشناخته از سمت سرور";
                try {
                    JsonObject error = JsonParser.parseString(response.body()).getAsJsonObject();
                    if (error.has("message")) {
                        message = error.get("message").getAsString();
                    }
                } catch (Exception ignored) {
                    // body was not JSON, keep the default message
                }
                throw new ApiException(message);
            }

            if (response.body() == null || response.body().isEmpty()) {
                return null;
            }
            return JsonParser.parseString(response.body());

        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException("اتصال به سرور برقرار نشد. مطمئن شوید بک‌اند اجرا شده است.");
        }
    }
}
