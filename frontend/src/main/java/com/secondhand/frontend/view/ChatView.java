package com.secondhand.frontend.view;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.secondhand.frontend.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;

/**
 * Messages of one conversation, shown as chat bubbles.
 * My messages and the other user's messages have different colors and sides.
 * A single check mark means "sent" and a double check mark
 * means the other user has seen the message (bonus).
 */
public class ChatView {

    public static Parent build(long conversationId, String adTitle) {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(15));

        Label title = new Label("گفتگو درباره: " + adTitle);
        title.getStyleClass().add("page-title");

        Button refreshButton = new Button("بروزرسانی");
        Button backButton = new Button("بازگشت");
        backButton.setOnAction(e -> MainApplication.switchTo(ConversationsView.build(), 1050, 700));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox topBar = new HBox(8, title, spacer, refreshButton, backButton);
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setPadding(new Insets(0, 0, 10, 0));

        ListView<JsonObject> messagesList = new ListView<>();
        messagesList.getStyleClass().add("chat-list");
        messagesList.setPlaceholder(new Label("هنوز پیامی وجود ندارد؛ اولین پیام را بنویسید"));
        messagesList.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(JsonObject message, boolean empty) {
                super.updateItem(message, empty);
                if (empty || message == null) {
                    setText(null);
                    setGraphic(null);
                    return;
                }
                JsonObject sender = message.getAsJsonObject("sender");
                boolean mine = sender.get("id").getAsLong() == SessionManager.getUserId();

                Label textLabel = new Label(message.get("text").getAsString());
                textLabel.setWrapText(true);
                textLabel.setMaxWidth(320);

                // small info line under the message: sender, time and check marks
                String info = "";
                if (!mine) {
                    info = sender.get("fullName").getAsString();
                }
                if (message.has("sentAt") && !message.get("sentAt").isJsonNull()) {
                    String sentAt = message.get("sentAt").getAsString();
                    if (sentAt.length() >= 16) {
                        info += "  " + sentAt.substring(0, 16).replace("T", " ");
                    }
                }
                if (mine) {
                    boolean seen = message.has("seen") && message.get("seen").getAsBoolean();
                    info += seen ? "  ✓✓" : "  ✓";
                }
                Label infoLabel = new Label(info.trim());
                infoLabel.getStyleClass().add("muted");

                VBox bubble = new VBox(4, textLabel, infoLabel);
                bubble.getStyleClass().addAll("bubble", mine ? "bubble-mine" : "bubble-theirs");

                HBox row = new HBox(bubble);
                row.setAlignment(mine ? Pos.CENTER_LEFT : Pos.CENTER_RIGHT);
                setText(null);
                setGraphic(row);
            }
        });
        loadMessages(messagesList, conversationId);
        refreshButton.setOnAction(e -> loadMessages(messagesList, conversationId));

        TextField messageField = new TextField();
        messageField.setPromptText("متن پیام...");
        HBox.setHgrow(messageField, Priority.ALWAYS);

        Button sendButton = new Button("ارسال");
        sendButton.getStyleClass().add("button-primary");
        sendButton.setDefaultButton(true);
        sendButton.setOnAction(e -> {
            String text = messageField.getText().trim();
            if (text.isEmpty()) {
                return;
            }
            try {
                JsonObject body = new JsonObject();
                body.addProperty("text", text);
                ApiClient.post("/api/conversations/" + conversationId + "/messages", body);
                messageField.clear();
                loadMessages(messagesList, conversationId);
            } catch (ApiException ex) {
                Ui.showError(ex.getMessage());
            }
        });

        HBox sendRow = new HBox(8, messageField, sendButton);
        sendRow.setAlignment(Pos.CENTER);
        sendRow.setPadding(new Insets(10, 0, 0, 0));

        root.setTop(topBar);
        root.setCenter(messagesList);
        root.setBottom(sendRow);
        return root;
    }

    /** Loads the messages. The backend also marks new messages as seen. */
    private static void loadMessages(ListView<JsonObject> messagesList, long conversationId) {
        try {
            JsonArray messages = ApiClient
                    .get("/api/conversations/" + conversationId + "/messages").getAsJsonArray();
            messagesList.getItems().clear();
            for (JsonElement element : messages) {
                messagesList.getItems().add(element.getAsJsonObject());
            }
            // scroll to the last message
            if (!messagesList.getItems().isEmpty()) {
                messagesList.scrollTo(messagesList.getItems().size() - 1);
            }
        } catch (ApiException e) {
            Ui.showError(e.getMessage());
        }
    }
}
