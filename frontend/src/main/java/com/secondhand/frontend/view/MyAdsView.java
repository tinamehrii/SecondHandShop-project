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
 * List of the advertisements of the logged-in user.
 * The user can edit, delete or mark them as sold, and can see
 * the reject reason when the admin rejected the ad.
 */
public class MyAdsView {

    public static Parent build() {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(15));

        Label title = new Label("آگهی‌های من");
        title.getStyleClass().add("page-title");

        ListView<JsonObject> listView = new ListView<>();
        listView.setPlaceholder(new Label("هنوز آگهی‌ای ثبت نکرده‌اید"));
        listView.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(JsonObject ad, boolean empty) {
                super.updateItem(ad, empty);
                if (empty || ad == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    Label titleLabel = new Label(ad.get("title").getAsString());
                    titleLabel.getStyleClass().add("item-title");

                    String sub = Ui.formatPrice(ad.get("price").getAsLong())
                            + "  •  وضعیت: " + Ui.statusFa(ad.get("status").getAsString());
                    if (ad.has("rejectReason") && !ad.get("rejectReason").isJsonNull()
                            && !ad.get("rejectReason").getAsString().isEmpty()) {
                        sub += "  •  دلیل رد: " + ad.get("rejectReason").getAsString();
                    }
                    Label subLabel = new Label(sub);
                    subLabel.getStyleClass().add("item-sub");

                    setText(null);
                    setGraphic(new VBox(3, titleLabel, subLabel));
                }
            }
        });
        loadMyAds(listView);

        Button editButton = new Button("ویرایش");
        editButton.setOnAction(e -> {
            JsonObject selected = listView.getSelectionModel().getSelectedItem();
            if (selected == null) {
                Ui.showError("ابتدا یک آگهی را انتخاب کنید");
                return;
            }
            MainApplication.switchTo(CreateAdView.build(selected), 720, 720);
        });

        Button soldButton = new Button("اعلام فروخته شدن");
        soldButton.setOnAction(e -> {
            JsonObject selected = listView.getSelectionModel().getSelectedItem();
            if (selected == null) {
                Ui.showError("ابتدا یک آگهی را انتخاب کنید");
                return;
            }
            try {
                ApiClient.put("/api/advertisements/" + selected.get("id").getAsLong() + "/sold", null);
                Ui.showInfo("آگهی به عنوان فروخته‌شده علامت خورد");
                loadMyAds(listView);
            } catch (ApiException ex) {
                Ui.showError(ex.getMessage());
            }
        });

        Button deleteButton = new Button("حذف آگهی");
        deleteButton.getStyleClass().add("button-danger");
        deleteButton.setOnAction(e -> {
            JsonObject selected = listView.getSelectionModel().getSelectedItem();
            if (selected == null) {
                Ui.showError("ابتدا یک آگهی را انتخاب کنید");
                return;
            }
            try {
                ApiClient.delete("/api/advertisements/" + selected.get("id").getAsLong());
                Ui.showInfo("آگهی حذف شد");
                loadMyAds(listView);
            } catch (ApiException ex) {
                Ui.showError(ex.getMessage());
            }
        });

        Button backButton = new Button("بازگشت");
        backButton.setOnAction(e -> MainApplication.switchTo(MainView.build(), 1050, 700));

        HBox actions = new HBox(10, editButton, soldButton, deleteButton, backButton);
        actions.setAlignment(Pos.CENTER);
        actions.setPadding(new Insets(10, 0, 0, 0));

        VBox center = new VBox(10, title, listView);
        VBox.setVgrow(listView, Priority.ALWAYS);
        root.setCenter(center);
        root.setBottom(actions);
        return root;
    }

    private static void loadMyAds(ListView<JsonObject> listView) {
        try {
            JsonArray ads = ApiClient.get("/api/advertisements/mine").getAsJsonArray();
            listView.getItems().clear();
            for (JsonElement element : ads) {
                listView.getItems().add(element.getAsJsonObject());
            }
        } catch (ApiException e) {
            Ui.showError(e.getMessage());
        }
    }
}
