package com.secondhand.frontend.view;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.secondhand.frontend.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

/**
 * Page for creating a new advertisement, or editing an existing one
 * when adToEdit is not null. Supports up to 5 images (bonus).
 */
public class CreateAdView {

    public static Parent build(JsonObject adToEdit) {
        boolean isEdit = adToEdit != null;

        Label title = new Label(isEdit ? "ویرایش آگهی" : "ثبت آگهی جدید");
        title.getStyleClass().add("page-title");

        TextField titleField = new TextField();
        titleField.setPromptText("عنوان آگهی");

        TextArea descriptionArea = new TextArea();
        descriptionArea.setPromptText("توضیحات");
        descriptionArea.setPrefRowCount(4);
        descriptionArea.setWrapText(true);

        TextField priceField = new TextField();
        priceField.setPromptText("قیمت (تومان)");

        ComboBox<String> conditionBox = new ComboBox<>();
        conditionBox.getItems().addAll("نو", "در حد نو", "کارکرده");
        conditionBox.setPromptText("وضعیت کالا");

        ComboBox<Option> categoryBox = new ComboBox<>();
        categoryBox.setPromptText("دسته‌بندی");
        loadCategories(categoryBox);

        ComboBox<Option> cityBox = new ComboBox<>();
        cityBox.setPromptText("شهر");
        loadCities(cityBox);

        // ---------- image selection ----------
        List<String> imagesBase64 = new ArrayList<>();
        Label imagesLabel = new Label("هیچ تصویری انتخاب نشده");
        Button chooseImagesButton = new Button("انتخاب تصاویر (حداکثر ۵)");
        chooseImagesButton.setOnAction(e -> {
            FileChooser chooser = new FileChooser();
            chooser.setTitle("انتخاب تصویر");
            chooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("تصاویر", "*.jpg", "*.jpeg", "*.png"));
            List<File> files = chooser.showOpenMultipleDialog(MainApplication.getStage());
            if (files == null) {
                return;
            }
            if (files.size() > 5) {
                Ui.showError("حداکثر ۵ تصویر می‌توانید انتخاب کنید");
                return;
            }
            imagesBase64.clear();
            try {
                for (File file : files) {
                    byte[] bytes = Files.readAllBytes(file.toPath());
                    imagesBase64.add(Base64.getEncoder().encodeToString(bytes));
                }
                imagesLabel.setText(files.size() + " تصویر انتخاب شد");
            } catch (Exception ex) {
                imagesBase64.clear();
                Ui.showError("خواندن فایل تصویر ممکن نشد");
            }
        });
        HBox imagesRow = new HBox(10, chooseImagesButton, imagesLabel);
        imagesRow.setAlignment(Pos.CENTER_LEFT);

        // ---------- pre-fill fields in edit mode ----------
        if (isEdit) {
            titleField.setText(adToEdit.get("title").getAsString());
            descriptionArea.setText(adToEdit.get("description").getAsString());
            priceField.setText(String.valueOf(adToEdit.get("price").getAsLong()));
            if (adToEdit.has("itemCondition") && !adToEdit.get("itemCondition").isJsonNull()) {
                conditionBox.setValue(adToEdit.get("itemCondition").getAsString());
            }
            selectById(categoryBox, adToEdit.getAsJsonObject("category").get("id").getAsLong());
            selectById(cityBox, adToEdit.getAsJsonObject("city").get("id").getAsLong());
            imagesLabel.setText("اگر تصویر جدید انتخاب نکنید، تصاویر قبلی می‌مانند");
        }

        // ---------- submit ----------
        Button submitButton = new Button(isEdit ? "ذخیره تغییرات" : "ثبت آگهی");
        submitButton.getStyleClass().add("button-primary");
        submitButton.setMaxWidth(Double.MAX_VALUE);
        submitButton.setOnAction(e -> {
            try {
                if (categoryBox.getValue() == null || cityBox.getValue() == null) {
                    Ui.showError("دسته‌بندی و شهر را انتخاب کنید");
                    return;
                }
                JsonObject body = new JsonObject();
                body.addProperty("title", titleField.getText().trim());
                body.addProperty("description", descriptionArea.getText().trim());
                body.addProperty("price", Long.parseLong(priceField.getText().trim()));
                if (conditionBox.getValue() != null) {
                    body.addProperty("itemCondition", conditionBox.getValue());
                }
                body.addProperty("categoryId", categoryBox.getValue().id);
                body.addProperty("cityId", cityBox.getValue().id);

                if (!imagesBase64.isEmpty()) {
                    JsonArray imagesJson = new JsonArray();
                    for (String image : imagesBase64) {
                        imagesJson.add(image);
                    }
                    body.add("imagesBase64", imagesJson);
                }

                if (isEdit) {
                    ApiClient.put("/api/advertisements/" + adToEdit.get("id").getAsLong(), body);
                    Ui.showInfo("تغییرات ذخیره شد و آگهی دوباره در انتظار تایید مدیر قرار گرفت");
                } else {
                    ApiClient.post("/api/advertisements", body);
                    Ui.showInfo("آگهی ثبت شد و پس از تایید مدیر نمایش داده می‌شود");
                }
                MainApplication.switchTo(MyAdsView.build(), 1050, 700);
            } catch (NumberFormatException ex) {
                Ui.showError("قیمت باید عدد باشد");
            } catch (ApiException ex) {
                Ui.showError(ex.getMessage());
            }
        });

        Button backButton = new Button("بازگشت");
        backButton.setMaxWidth(Double.MAX_VALUE);
        backButton.setOnAction(e -> MainApplication.switchTo(MainView.build(), 1050, 700));

        VBox root = new VBox(12, title, titleField, descriptionArea, priceField,
                conditionBox, categoryBox, cityBox, imagesRow, submitButton, backButton);
        root.setPadding(new Insets(25));
        return root;
    }

    /** Selects the item of the ComboBox that has the given id. */
    private static void selectById(ComboBox<Option> box, Long id) {
        for (Option option : box.getItems()) {
            if (id.equals(option.id)) {
                box.setValue(option);
                return;
            }
        }
    }

    private static void loadCategories(ComboBox<Option> box) {
        try {
            JsonArray categories = ApiClient.get("/api/categories").getAsJsonArray();
            for (JsonElement element : categories) {
                JsonObject category = element.getAsJsonObject();
                String name = category.get("name").getAsString();
                if (category.has("parent") && !category.get("parent").isJsonNull()) {
                    name = category.getAsJsonObject("parent").get("name").getAsString() + " > " + name;
                }
                box.getItems().add(new Option(category.get("id").getAsLong(), name));
            }
        } catch (ApiException e) {
            Ui.showError(e.getMessage());
        }
    }

    private static void loadCities(ComboBox<Option> box) {
        try {
            JsonArray cities = ApiClient.get("/api/cities").getAsJsonArray();
            for (JsonElement element : cities) {
                JsonObject city = element.getAsJsonObject();
                box.getItems().add(new Option(city.get("id").getAsLong(), city.get("name").getAsString()));
            }
        } catch (ApiException e) {
            Ui.showError(e.getMessage());
        }
    }
}
