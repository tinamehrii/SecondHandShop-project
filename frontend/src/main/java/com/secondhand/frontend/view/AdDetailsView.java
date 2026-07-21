package com.secondhand.frontend.view;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.secondhand.frontend.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;

import java.util.Optional;

/**
 * Details page of one advertisement with an image gallery (bonus),
 * seller info with average rating, favorite / chat / rate buttons.
 */
public class AdDetailsView {

    public static Parent build(long adId) {
        JsonObject ad;
        try {
            ad = ApiClient.get("/api/advertisements/" + adId).getAsJsonObject();
        } catch (ApiException e) {
            Ui.showError(e.getMessage());
            return MainView.build();
        }

        JsonObject owner = ad.getAsJsonObject("owner");
        long ownerId = owner.get("id").getAsLong();
        boolean isMyAd = ownerId == SessionManager.getUserId();

        VBox root = new VBox(10);
        root.setPadding(new Insets(15));

        // ---------- image gallery (bonus: more than one image) ----------
        JsonArray images = ad.getAsJsonArray("images");
        ImageView imageView = new ImageView();
        imageView.setFitWidth(400);
        imageView.setFitHeight(280);
        imageView.setPreserveRatio(true);

        Label imageCounter = new Label();
        int[] currentIndex = {0};

        Runnable showImage = () -> {
            if (images == null || images.size() == 0) {
                imageCounter.setText("بدون تصویر");
                return;
            }
            String fileName = images.get(currentIndex[0]).getAsJsonObject()
                    .get("fileName").getAsString();
            imageView.setImage(new Image(ApiConfig.BASE_URL + "/api/images/" + fileName, true));
            imageCounter.setText((currentIndex[0] + 1) + " از " + images.size());
        };
        showImage.run();

        Button previousButton = new Button("تصویر قبلی");
        Button nextButton = new Button("تصویر بعدی");
        previousButton.setOnAction(e -> {
            if (images != null && images.size() > 0) {
                currentIndex[0] = (currentIndex[0] - 1 + images.size()) % images.size();
                showImage.run();
            }
        });
        nextButton.setOnAction(e -> {
            if (images != null && images.size() > 0) {
                currentIndex[0] = (currentIndex[0] + 1) % images.size();
                showImage.run();
            }
        });

        HBox galleryControls = new HBox(10, previousButton, imageCounter, nextButton);
        galleryControls.setAlignment(Pos.CENTER);
        VBox gallery = new VBox(5, imageView, galleryControls);
        gallery.setAlignment(Pos.CENTER);

        // ---------- ad info ----------
        Label titleLabel = new Label(ad.get("title").getAsString());
        titleLabel.getStyleClass().add("page-title");

        Label priceLabel = new Label("قیمت: " + Ui.formatPrice(ad.get("price").getAsLong()));
        priceLabel.getStyleClass().add("price-label");
        Label cityLabel = new Label("شهر: " + ad.getAsJsonObject("city").get("name").getAsString());
        Label categoryLabel = new Label("دسته‌بندی: " + ad.getAsJsonObject("category").get("name").getAsString());
        Label conditionLabel = new Label("وضعیت کالا: " +
                (ad.has("itemCondition") && !ad.get("itemCondition").isJsonNull()
                        ? ad.get("itemCondition").getAsString() : "نامشخص"));
        Label statusLabel = new Label("وضعیت آگهی: " + Ui.statusFa(ad.get("status").getAsString()));

        TextArea descriptionArea = new TextArea(ad.get("description").getAsString());
        descriptionArea.setEditable(false);
        descriptionArea.setWrapText(true);
        descriptionArea.setPrefRowCount(4);

        // ---------- seller info with average rating ----------
        String sellerText = "فروشنده: " + owner.get("fullName").getAsString();
        try {
            JsonObject ratingInfo = ApiClient.get("/api/ratings/seller/" + ownerId).getAsJsonObject();
            sellerText += "  |  میانگین امتیاز: " + ratingInfo.get("average").getAsDouble()
                    + " (" + ratingInfo.get("count").getAsInt() + " رای)";
        } catch (ApiException ignored) {
            // rating info is optional, page still works without it
        }
        Label sellerLabel = new Label(sellerText);
        sellerLabel.getStyleClass().add("muted");

        // ---------- action buttons ----------
        HBox actions = new HBox(10);
        actions.setAlignment(Pos.CENTER);

        if (!isMyAd) {
            Button favoriteButton = new Button("نشان کردن");
            favoriteButton.setOnAction(e -> {
                try {
                    ApiClient.post("/api/favorites/" + adId, null);
                    Ui.showInfo("آگهی به لیست نشان‌شده‌ها اضافه شد");
                } catch (ApiException ex) {
                    Ui.showError(ex.getMessage());
                }
            });

            Button chatButton = new Button("گفتگو با فروشنده");
            chatButton.getStyleClass().add("button-primary");
            chatButton.setOnAction(e -> {
                try {
                    JsonObject conversation = ApiClient
                            .post("/api/conversations/start/" + adId, null).getAsJsonObject();
                    MainApplication.switchTo(ChatView.build(
                            conversation.get("id").getAsLong(),
                            ad.get("title").getAsString()), 700, 650);
                } catch (ApiException ex) {
                    Ui.showError(ex.getMessage());
                }
            });

            Button rateButton = new Button("ثبت امتیاز به فروشنده");
            rateButton.setOnAction(e -> rateSeller(adId));

            actions.getChildren().addAll(favoriteButton, chatButton, rateButton);
        }

        Button backButton = new Button("بازگشت");
        backButton.setOnAction(e -> MainApplication.switchTo(MainView.build(), 1050, 700));
        actions.getChildren().add(backButton);

        root.getChildren().addAll(gallery, titleLabel, priceLabel, cityLabel, categoryLabel,
                conditionLabel, statusLabel, sellerLabel, descriptionArea, actions);

        ScrollPane scrollPane = new ScrollPane(root);
        scrollPane.setFitToWidth(true);
        VBox wrapper = new VBox(scrollPane);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);
        return wrapper;
    }

    /** Opens two simple dialogs for score and comment, then sends the rating. */
    private static void rateSeller(long adId) {
        ChoiceDialog<Integer> scoreDialog = new ChoiceDialog<>(5, 1, 2, 3, 4, 5);
        scoreDialog.setTitle("ثبت امتیاز");
        scoreDialog.setHeaderText(null);
        scoreDialog.setContentText("امتیاز شما به فروشنده (۱ تا ۵):");
        Optional<Integer> score = scoreDialog.showAndWait();
        if (score.isEmpty()) {
            return;
        }

        Optional<String> comment = Ui.askText("ثبت امتیاز", "نظر شما (اختیاری):");

        try {
            JsonObject body = new JsonObject();
            body.addProperty("advertisementId", adId);
            body.addProperty("score", score.get());
            body.addProperty("comment", comment.orElse(""));
            ApiClient.post("/api/ratings", body);
            Ui.showInfo("امتیاز شما ثبت شد");
        } catch (ApiException e) {
            Ui.showError(e.getMessage());
        }
    }
}
