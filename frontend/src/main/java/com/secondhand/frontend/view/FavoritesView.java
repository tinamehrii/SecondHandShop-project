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
 * List of favorite (bookmarked) advertisements of the user.
 */
public class FavoritesView {

    public static Parent build() {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(15));

        Label title = new Label("آگهی‌های نشان‌شده");
        title.getStyleClass().add("page-title");

        ListView<JsonObject> listView = new ListView<>();
        listView.setPlaceholder(new Label("هنوز آگهی‌ای را نشان نکرده‌اید"));
        listView.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(JsonObject favorite, boolean empty) {
                super.updateItem(favorite, empty);
                if (empty || favorite == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    JsonObject ad = favorite.getAsJsonObject("advertisement");
                    Label titleLabel = new Label(ad.get("title").getAsString());
                    titleLabel.getStyleClass().add("item-title");

                    Label subLabel = new Label(Ui.formatPrice(ad.get("price").getAsLong())
                            + "  •  وضعیت: " + Ui.statusFa(ad.get("status").getAsString()));
                    subLabel.getStyleClass().add("item-sub");

                    setText(null);
                    setGraphic(new VBox(3, titleLabel, subLabel));
                }
            }
        });
        loadFavorites(listView);

        Button openButton = new Button("مشاهده آگهی");
        openButton.getStyleClass().add("button-primary");
        openButton.setOnAction(e -> {
            JsonObject selected = listView.getSelectionModel().getSelectedItem();
            if (selected == null) {
                Ui.showError("ابتدا یک آگهی را انتخاب کنید");
                return;
            }
            long adId = selected.getAsJsonObject("advertisement").get("id").getAsLong();
            MainApplication.switchTo(AdDetailsView.build(adId), 850, 720);
        });

        Button removeButton = new Button("حذف از نشان‌شده‌ها");
        removeButton.getStyleClass().add("button-danger");
        removeButton.setOnAction(e -> {
            JsonObject selected = listView.getSelectionModel().getSelectedItem();
            if (selected == null) {
                Ui.showError("ابتدا یک آگهی را انتخاب کنید");
                return;
            }
            try {
                long adId = selected.getAsJsonObject("advertisement").get("id").getAsLong();
                ApiClient.delete("/api/favorites/" + adId);
                loadFavorites(listView);
            } catch (ApiException ex) {
                Ui.showError(ex.getMessage());
            }
        });

        Button backButton = new Button("بازگشت");
        backButton.setOnAction(e -> MainApplication.switchTo(MainView.build(), 1050, 700));

        HBox actions = new HBox(10, openButton, removeButton, backButton);
        actions.setAlignment(Pos.CENTER);
        actions.setPadding(new Insets(10, 0, 0, 0));

        VBox center = new VBox(10, title, listView);
        VBox.setVgrow(listView, Priority.ALWAYS);
        root.setCenter(center);
        root.setBottom(actions);
        return root;
    }

    private static void loadFavorites(ListView<JsonObject> listView) {
        try {
            JsonArray favorites = ApiClient.get("/api/favorites").getAsJsonArray();
            listView.getItems().clear();
            for (JsonElement element : favorites) {
                listView.getItems().add(element.getAsJsonObject());
            }
        } catch (ApiException e) {
            Ui.showError(e.getMessage());
        }
    }
}
