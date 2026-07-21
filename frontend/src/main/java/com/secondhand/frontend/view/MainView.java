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
 * Main page: list of active advertisements with search,
 * combined filters and sorting (bonus features).
 */
public class MainView {

    private static final String[] SORT_LABELS = {
            "جدیدترین", "قدیمی‌ترین", "ارزان‌ترین", "گران‌ترین", "بهترین امتیاز فروشنده"};
    private static final String[] SORT_CODES = {
            "newest", "oldest", "cheapest", "expensive", "bestRated"};

    public static Parent build() {
        BorderPane root = new BorderPane();
        root.getStyleClass().add("main-root");
        root.setPadding(new Insets(10));

        // ---------- top bar ----------
        Label appName = new Label("🛒 بازار دست دوم");
        appName.getStyleClass().add("app-name");

        Label welcome = new Label("سلام، " +
                SessionManager.getCurrentUser().get("fullName").getAsString());
        welcome.getStyleClass().add("muted");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button createAdButton = new Button("+ ثبت آگهی جدید");
        createAdButton.getStyleClass().add("button-primary");
        createAdButton.setOnAction(e -> MainApplication.switchTo(CreateAdView.build(null), 720, 720));

        Button myAdsButton = new Button("آگهی‌های من");
        myAdsButton.setOnAction(e -> MainApplication.switchTo(MyAdsView.build(), 1050, 700));

        Button favoritesButton = new Button("نشان‌شده‌ها");
        favoritesButton.setOnAction(e -> MainApplication.switchTo(FavoritesView.build(), 1050, 700));

        Button messagesButton = new Button("گفتگوها");
        messagesButton.setOnAction(e -> MainApplication.switchTo(ConversationsView.build(), 1050, 700));

        HBox topBar = new HBox(8, appName, welcome, spacer,
                createAdButton, myAdsButton, favoritesButton, messagesButton);
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.getStyleClass().add("top-bar");
        topBar.setPadding(new Insets(10, 14, 10, 14));

        if (SessionManager.isAdmin()) {
            Button adminButton = new Button("پنل مدیریت");
            adminButton.setOnAction(e -> MainApplication.switchTo(AdminView.build(), 1050, 700));
            topBar.getChildren().add(adminButton);
        }

        Button logoutButton = new Button("خروج");
        logoutButton.getStyleClass().add("button-danger");
        logoutButton.setOnAction(e -> {
            SessionManager.logout();
            MainApplication.switchTo(LoginView.build(), 460, 600);
        });
        topBar.getChildren().add(logoutButton);

        // ---------- filter bar ----------
        TextField keywordField = new TextField();
        keywordField.setPromptText("جستجو در عنوان و توضیحات");
        keywordField.setPrefWidth(180);

        ComboBox<Option> categoryBox = new ComboBox<>();
        loadCategories(categoryBox);

        ComboBox<Option> cityBox = new ComboBox<>();
        loadCities(cityBox);

        TextField minPriceField = new TextField();
        minPriceField.setPromptText("حداقل قیمت");
        minPriceField.setPrefWidth(100);

        TextField maxPriceField = new TextField();
        maxPriceField.setPromptText("حداکثر قیمت");
        maxPriceField.setPrefWidth(100);

        ComboBox<String> conditionBox = new ComboBox<>();
        conditionBox.getItems().addAll("همه", "نو", "در حد نو", "کارکرده");
        conditionBox.setValue("همه");

        ComboBox<String> sortBox = new ComboBox<>();
        sortBox.getItems().addAll(SORT_LABELS);
        sortBox.setValue(SORT_LABELS[0]);

        ListView<JsonObject> listView = new ListView<>();
        listView.setPlaceholder(new Label("آگهی‌ای برای نمایش پیدا نشد"));
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

                    Label subLabel = new Label(Ui.formatPrice(ad.get("price").getAsLong())
                            + "  •  " + ad.getAsJsonObject("city").get("name").getAsString()
                            + "  •  " + ad.getAsJsonObject("category").get("name").getAsString());
                    subLabel.getStyleClass().add("item-sub");

                    setText(null);
                    setGraphic(new VBox(3, titleLabel, subLabel));
                }
            }
        });

        Button searchButton = new Button("جستجو");
        searchButton.getStyleClass().add("button-primary");
        searchButton.setDefaultButton(true);
        searchButton.setOnAction(e -> search(listView, keywordField, categoryBox, cityBox,
                minPriceField, maxPriceField, conditionBox, sortBox));

        HBox filterBar = new HBox(8, keywordField, categoryBox, cityBox,
                minPriceField, maxPriceField, conditionBox, sortBox, searchButton);
        filterBar.setAlignment(Pos.CENTER_LEFT);
        filterBar.getStyleClass().add("filter-bar");
        VBox.setMargin(filterBar, new Insets(8, 0, 8, 0));

        VBox topSection = new VBox(topBar, filterBar);
        root.setTop(topSection);

        // ---------- list of ads ----------
        Label hint = new Label("برای مشاهده جزئیات، روی آگهی دو بار کلیک کنید");
        hint.getStyleClass().add("on-image");
        listView.setOnMouseClicked(e -> {
            JsonObject selected = listView.getSelectionModel().getSelectedItem();
            if (e.getClickCount() == 2 && selected != null) {
                MainApplication.switchTo(
                        AdDetailsView.build(selected.get("id").getAsLong()), 850, 720);
            }
        });

        VBox center = new VBox(5, hint, listView);
        VBox.setVgrow(listView, Priority.ALWAYS);
        root.setCenter(center);

        // load everything at the beginning
        search(listView, keywordField, categoryBox, cityBox,
                minPriceField, maxPriceField, conditionBox, sortBox);

        return root;
    }

    /** Builds the query string from the filters and fills the list. */
    private static void search(ListView<JsonObject> listView, TextField keywordField,
                               ComboBox<Option> categoryBox, ComboBox<Option> cityBox,
                               TextField minPriceField, TextField maxPriceField,
                               ComboBox<String> conditionBox, ComboBox<String> sortBox) {
        try {
            StringBuilder query = new StringBuilder("/api/advertisements?");

            if (!keywordField.getText().trim().isEmpty()) {
                query.append("keyword=").append(ApiClient.encode(keywordField.getText().trim())).append("&");
            }
            if (categoryBox.getValue() != null && categoryBox.getValue().id != null) {
                query.append("categoryId=").append(categoryBox.getValue().id).append("&");
            }
            if (cityBox.getValue() != null && cityBox.getValue().id != null) {
                query.append("cityId=").append(cityBox.getValue().id).append("&");
            }
            if (!minPriceField.getText().trim().isEmpty()) {
                query.append("minPrice=").append(Long.parseLong(minPriceField.getText().trim())).append("&");
            }
            if (!maxPriceField.getText().trim().isEmpty()) {
                query.append("maxPrice=").append(Long.parseLong(maxPriceField.getText().trim())).append("&");
            }
            if (!conditionBox.getValue().equals("همه")) {
                query.append("itemCondition=").append(ApiClient.encode(conditionBox.getValue())).append("&");
            }
            int sortIndex = sortBox.getItems().indexOf(sortBox.getValue());
            query.append("sort=").append(SORT_CODES[sortIndex]);

            JsonArray ads = ApiClient.get(query.toString()).getAsJsonArray();
            listView.getItems().clear();
            for (JsonElement element : ads) {
                listView.getItems().add(element.getAsJsonObject());
            }
        } catch (NumberFormatException e) {
            Ui.showError("قیمت باید عدد باشد");
        } catch (ApiException e) {
            Ui.showError(e.getMessage());
        }
    }

    /** Fills the category ComboBox. Sub-categories are shown as "parent > child". */
    private static void loadCategories(ComboBox<Option> box) {
        box.getItems().add(new Option(null, "همه دسته‌ها"));
        box.setValue(box.getItems().get(0));
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

    /** Fills the city ComboBox. */
    private static void loadCities(ComboBox<Option> box) {
        box.getItems().add(new Option(null, "همه شهرها"));
        box.setValue(box.getItems().get(0));
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
