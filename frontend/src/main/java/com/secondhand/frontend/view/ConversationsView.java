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
 * List of the user's conversations.
 * Every row shows the last message and the unread count (bonus).
 */
public class ConversationsView {

    public static Parent build() {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(15));

        Label title = new Label("گفتگوهای من");
        title.getStyleClass().add("page-title");

        ListView<JsonObject> listView = new ListView<>();
        listView.setPlaceholder(new Label("هنوز گفتگویی ندارید"));
        listView.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(JsonObject item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    return;
                }
                JsonObject conversation = item.getAsJsonObject("conversation");
                JsonObject ad = conversation.getAsJsonObject("advertisement");

                // show the name of the other side of the conversation
                JsonObject buyer = conversation.getAsJsonObject("buyer");
                JsonObject seller = conversation.getAsJsonObject("seller");
                String otherName = buyer.get("id").getAsLong() == SessionManager.getUserId()
                        ? seller.get("fullName").getAsString()
                        : buyer.get("fullName").getAsString();

                Label titleLabel = new Label(ad.get("title").getAsString() + " — با " + otherName);
                titleLabel.getStyleClass().add("item-title");

                String last = "هنوز پیامی رد و بدل نشده";
                if (item.has("lastMessage") && !item.get("lastMessage").isJsonNull()) {
                    last = item.getAsJsonObject("lastMessage").get("text").getAsString();
                    if (last.length() > 40) {
                        last = last.substring(0, 40) + "...";
                    }
                }
                Label lastLabel = new Label(last);
                lastLabel.getStyleClass().add("item-sub");

                HBox row = new HBox(10, new VBox(3, titleLabel, lastLabel));
                row.setAlignment(Pos.CENTER_LEFT);

                // red badge with the number of unread messages (bonus)
                int unread = item.get("unreadCount").getAsInt();
                if (unread > 0) {
                    Region rowSpacer = new Region();
                    HBox.setHgrow(rowSpacer, Priority.ALWAYS);
                    Label badge = new Label(String.valueOf(unread));
                    badge.getStyleClass().add("unread-badge");
                    row.getChildren().addAll(rowSpacer, badge);
                }
                setText(null);
                setGraphic(row);
            }
        });
        loadConversations(listView);

        Button openButton = new Button("باز کردن گفتگو");
        openButton.getStyleClass().add("button-primary");
        openButton.setOnAction(e -> {
            JsonObject selected = listView.getSelectionModel().getSelectedItem();
            if (selected == null) {
                Ui.showError("ابتدا یک گفتگو را انتخاب کنید");
                return;
            }
            JsonObject conversation = selected.getAsJsonObject("conversation");
            MainApplication.switchTo(ChatView.build(
                    conversation.get("id").getAsLong(),
                    conversation.getAsJsonObject("advertisement").get("title").getAsString()),
                    700, 650);
        });

        Button refreshButton = new Button("بروزرسانی");
        refreshButton.setOnAction(e -> loadConversations(listView));

        Button backButton = new Button("بازگشت");
        backButton.setOnAction(e -> MainApplication.switchTo(MainView.build(), 1050, 700));

        HBox actions = new HBox(10, openButton, refreshButton, backButton);
        actions.setAlignment(Pos.CENTER);
        actions.setPadding(new Insets(10, 0, 0, 0));

        VBox center = new VBox(10, title, listView);
        VBox.setVgrow(listView, Priority.ALWAYS);
        root.setCenter(center);
        root.setBottom(actions);
        return root;
    }

    private static void loadConversations(ListView<JsonObject> listView) {
        try {
            JsonArray conversations = ApiClient.get("/api/conversations").getAsJsonArray();
            listView.getItems().clear();
            for (JsonElement element : conversations) {
                listView.getItems().add(element.getAsJsonObject());
            }
        } catch (ApiException e) {
            Ui.showError(e.getMessage());
        }
    }
}
