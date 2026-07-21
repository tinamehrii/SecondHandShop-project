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

import java.util.Map;
import java.util.Optional;

/**
 * Admin panel with four tabs:
 * pending ads, users (block/unblock), categories and statistics.
 */
public class AdminView {

    public static Parent build() {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10));

        Label title = new Label("پنل مدیریت");
        title.getStyleClass().add("page-title");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Button backButton = new Button("بازگشت به فروشگاه");
        backButton.setOnAction(e -> MainApplication.switchTo(MainView.build(), 1050, 700));
        HBox topBar = new HBox(10, title, spacer, backButton);
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setPadding(new Insets(0, 0, 10, 0));

        TabPane tabs = new TabPane();
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabs.getTabs().add(new Tab("آگهی‌های در انتظار تایید", buildPendingTab()));
        tabs.getTabs().add(new Tab("کاربران", buildUsersTab()));
        tabs.getTabs().add(new Tab("دسته‌بندی‌ها", buildCategoriesTab()));
        tabs.getTabs().add(new Tab("آمار سامانه", buildStatsTab()));

        root.setTop(topBar);
        root.setCenter(tabs);
        return root;
    }

    // ---------------- pending advertisements ----------------

    private static Parent buildPendingTab() {
        ListView<JsonObject> listView = new ListView<>();
        listView.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(JsonObject ad, boolean empty) {
                super.updateItem(ad, empty);
                if (empty || ad == null) {
                    setText(null);
                } else {
                    setText(ad.get("title").getAsString()
                            + "  |  " + Ui.formatPrice(ad.get("price").getAsLong())
                            + "  |  آگهی‌دهنده: "
                            + ad.getAsJsonObject("owner").get("fullName").getAsString());
                }
            }
        });
        loadPending(listView);

        Button detailsButton = new Button("مشاهده توضیحات");
        detailsButton.setOnAction(e -> {
            JsonObject selected = listView.getSelectionModel().getSelectedItem();
            if (selected == null) {
                Ui.showError("ابتدا یک آگهی را انتخاب کنید");
                return;
            }
            Ui.showInfo(selected.get("title").getAsString() + "\n\n"
                    + selected.get("description").getAsString());
        });

        Button approveButton = new Button("تایید");
        approveButton.getStyleClass().add("button-primary");
        approveButton.setOnAction(e -> {
            JsonObject selected = listView.getSelectionModel().getSelectedItem();
            if (selected == null) {
                Ui.showError("ابتدا یک آگهی را انتخاب کنید");
                return;
            }
            try {
                ApiClient.put("/api/admin/advertisements/"
                        + selected.get("id").getAsLong() + "/approve", null);
                loadPending(listView);
            } catch (ApiException ex) {
                Ui.showError(ex.getMessage());
            }
        });

        Button rejectButton = new Button("رد آگهی");
        rejectButton.getStyleClass().add("button-danger");
        rejectButton.setOnAction(e -> {
            JsonObject selected = listView.getSelectionModel().getSelectedItem();
            if (selected == null) {
                Ui.showError("ابتدا یک آگهی را انتخاب کنید");
                return;
            }
            Optional<String> reason = Ui.askText("رد آگهی", "دلیل رد:");
            if (reason.isEmpty()) {
                return;
            }
            try {
                JsonObject body = new JsonObject();
                body.addProperty("reason", reason.get());
                ApiClient.put("/api/admin/advertisements/"
                        + selected.get("id").getAsLong() + "/reject", body);
                loadPending(listView);
            } catch (ApiException ex) {
                Ui.showError(ex.getMessage());
            }
        });

        Button refreshButton = new Button("بروزرسانی");
        refreshButton.setOnAction(e -> loadPending(listView));

        HBox actions = new HBox(10, detailsButton, approveButton, rejectButton, refreshButton);
        actions.setAlignment(Pos.CENTER);
        actions.setPadding(new Insets(10));

        VBox root = new VBox(listView, actions);
        VBox.setVgrow(listView, Priority.ALWAYS);
        return root;
    }

    private static void loadPending(ListView<JsonObject> listView) {
        try {
            JsonArray ads = ApiClient.get("/api/admin/advertisements/pending").getAsJsonArray();
            listView.getItems().clear();
            for (JsonElement element : ads) {
                listView.getItems().add(element.getAsJsonObject());
            }
        } catch (ApiException e) {
            Ui.showError(e.getMessage());
        }
    }

    // ---------------- users (bonus: block / unblock) ----------------

    private static Parent buildUsersTab() {
        ListView<JsonObject> listView = new ListView<>();
        listView.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(JsonObject user, boolean empty) {
                super.updateItem(user, empty);
                if (empty || user == null) {
                    setText(null);
                } else {
                    String status = user.get("status").getAsString().equals("BLOCKED")
                            ? "مسدود" : "فعال";
                    setText(user.get("fullName").getAsString()
                            + " (" + user.get("username").getAsString() + ")"
                            + "  |  نقش: " + user.get("role").getAsString()
                            + "  |  وضعیت: " + status);
                }
            }
        });
        loadUsers(listView);

        Button blockButton = new Button("مسدود کردن");
        blockButton.getStyleClass().add("button-danger");
        blockButton.setOnAction(e -> {
            JsonObject selected = listView.getSelectionModel().getSelectedItem();
            if (selected == null) {
                Ui.showError("ابتدا یک کاربر را انتخاب کنید");
                return;
            }
            try {
                ApiClient.put("/api/admin/users/" + selected.get("id").getAsLong() + "/block", null);
                loadUsers(listView);
            } catch (ApiException ex) {
                Ui.showError(ex.getMessage());
            }
        });

        Button unblockButton = new Button("رفع مسدودی");
        unblockButton.setOnAction(e -> {
            JsonObject selected = listView.getSelectionModel().getSelectedItem();
            if (selected == null) {
                Ui.showError("ابتدا یک کاربر را انتخاب کنید");
                return;
            }
            try {
                ApiClient.put("/api/admin/users/" + selected.get("id").getAsLong() + "/unblock", null);
                loadUsers(listView);
            } catch (ApiException ex) {
                Ui.showError(ex.getMessage());
            }
        });

        Button refreshButton = new Button("بروزرسانی");
        refreshButton.setOnAction(e -> loadUsers(listView));

        HBox actions = new HBox(10, blockButton, unblockButton, refreshButton);
        actions.setAlignment(Pos.CENTER);
        actions.setPadding(new Insets(10));

        VBox root = new VBox(listView, actions);
        VBox.setVgrow(listView, Priority.ALWAYS);
        return root;
    }

    private static void loadUsers(ListView<JsonObject> listView) {
        try {
            JsonArray users = ApiClient.get("/api/admin/users").getAsJsonArray();
            listView.getItems().clear();
            for (JsonElement element : users) {
                listView.getItems().add(element.getAsJsonObject());
            }
        } catch (ApiException e) {
            Ui.showError(e.getMessage());
        }
    }

    // ---------------- categories ----------------

    private static Parent buildCategoriesTab() {
        ListView<JsonObject> listView = new ListView<>();
        listView.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(JsonObject category, boolean empty) {
                super.updateItem(category, empty);
                if (empty || category == null) {
                    setText(null);
                } else {
                    String name = category.get("name").getAsString();
                    if (category.has("parent") && !category.get("parent").isJsonNull()) {
                        name = category.getAsJsonObject("parent").get("name").getAsString()
                                + " > " + name;
                    }
                    setText(name);
                }
            }
        });
        loadCategories(listView);

        Button addButton = new Button("افزودن دسته");
        addButton.setOnAction(e -> {
            Optional<String> name = Ui.askText("افزودن دسته", "نام دسته جدید:");
            if (name.isEmpty() || name.get().trim().isEmpty()) {
                return;
            }
            // if a category is selected, the new one becomes its sub-category
            JsonObject selected = listView.getSelectionModel().getSelectedItem();
            Long parentId = null;
            if (selected != null) {
                Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                        "زیرمجموعه‌ای از «" + selected.get("name").getAsString() + "» باشد؟",
                        ButtonType.YES, ButtonType.NO);
                confirm.setHeaderText(null);
                Optional<ButtonType> answer = confirm.showAndWait();
                if (answer.isPresent() && answer.get() == ButtonType.YES) {
                    parentId = selected.get("id").getAsLong();
                }
            }
            try {
                JsonObject body = new JsonObject();
                body.addProperty("name", name.get().trim());
                if (parentId != null) {
                    body.addProperty("parentId", parentId);
                }
                ApiClient.post("/api/admin/categories", body);
                loadCategories(listView);
            } catch (ApiException ex) {
                Ui.showError(ex.getMessage());
            }
        });

        Button renameButton = new Button("تغییر نام");
        renameButton.setOnAction(e -> {
            JsonObject selected = listView.getSelectionModel().getSelectedItem();
            if (selected == null) {
                Ui.showError("ابتدا یک دسته را انتخاب کنید");
                return;
            }
            Optional<String> name = Ui.askText("تغییر نام", "نام جدید:");
            if (name.isEmpty() || name.get().trim().isEmpty()) {
                return;
            }
            try {
                JsonObject body = new JsonObject();
                body.addProperty("name", name.get().trim());
                ApiClient.put("/api/admin/categories/" + selected.get("id").getAsLong(), body);
                loadCategories(listView);
            } catch (ApiException ex) {
                Ui.showError(ex.getMessage());
            }
        });

        Button deleteButton = new Button("حذف");
        deleteButton.setOnAction(e -> {
            JsonObject selected = listView.getSelectionModel().getSelectedItem();
            if (selected == null) {
                Ui.showError("ابتدا یک دسته را انتخاب کنید");
                return;
            }
            try {
                ApiClient.delete("/api/admin/categories/" + selected.get("id").getAsLong());
                loadCategories(listView);
            } catch (ApiException ex) {
                Ui.showError(ex.getMessage());
            }
        });

        HBox actions = new HBox(10, addButton, renameButton, deleteButton);
        actions.setAlignment(Pos.CENTER);
        actions.setPadding(new Insets(10));

        VBox root = new VBox(listView, actions);
        VBox.setVgrow(listView, Priority.ALWAYS);
        return root;
    }

    private static void loadCategories(ListView<JsonObject> listView) {
        try {
            JsonArray categories = ApiClient.get("/api/categories").getAsJsonArray();
            listView.getItems().clear();
            for (JsonElement element : categories) {
                listView.getItems().add(element.getAsJsonObject());
            }
        } catch (ApiException e) {
            Ui.showError(e.getMessage());
        }
    }

    // ---------------- statistics dashboard (bonus) ----------------

    private static Parent buildStatsTab() {
        VBox statsBox = new VBox(8);
        statsBox.setPadding(new Insets(20));

        Button refreshButton = new Button("بروزرسانی آمار");
        refreshButton.setOnAction(e -> loadStats(statsBox));
        loadStats(statsBox);

        VBox root = new VBox(10, refreshButton, statsBox);
        root.setPadding(new Insets(15));
        return root;
    }

    /** Persian labels of the statistics keys sent by the backend. */
    private static final Map<String, String> STAT_LABELS = Map.ofEntries(
            Map.entry("totalUsers", "تعداد کل کاربران"),
            Map.entry("activeUsers", "کاربران فعال"),
            Map.entry("blockedUsers", "کاربران مسدود"),
            Map.entry("totalAds", "تعداد کل آگهی‌ها"),
            Map.entry("pendingAds", "آگهی‌های در انتظار تایید"),
            Map.entry("activeAds", "آگهی‌های فعال"),
            Map.entry("rejectedAds", "آگهی‌های ردشده"),
            Map.entry("soldAds", "آگهی‌های فروخته‌شده"),
            Map.entry("deletedAds", "آگهی‌های حذف‌شده"),
            Map.entry("totalConversations", "تعداد گفتگوها"),
            Map.entry("totalMessages", "تعداد پیام‌ها"),
            Map.entry("totalRatings", "تعداد امتیازهای ثبت‌شده")
    );

    private static void loadStats(VBox statsBox) {
        try {
            JsonObject stats = ApiClient.get("/api/admin/stats").getAsJsonObject();
            statsBox.getChildren().clear();
            for (String key : stats.keySet()) {
                String label = STAT_LABELS.getOrDefault(key, key);
                Label row = new Label(label + ": " + stats.get(key).getAsString());
                row.setStyle("-fx-font-size: 14px;");
                statsBox.getChildren().add(row);
            }
        } catch (ApiException e) {
            Ui.showError(e.getMessage());
        }
    }
}
