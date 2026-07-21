package com.secondhand.frontend.view;

import com.google.gson.JsonObject;
import com.secondhand.frontend.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/**
 * Login page, shown as a white card in the middle of the window.
 */
public class LoginView {

    public static Parent build() {
        Label logo = new Label("🛒");
        logo.setStyle("-fx-font-size: 42px;");

        Label title = new Label("بازار دست دوم");
        title.getStyleClass().add("page-title");

        Label subtitle = new Label("سامانه خرید و فروش کالای دست دوم");
        subtitle.getStyleClass().add("muted");

        TextField usernameField = new TextField();
        usernameField.setPromptText("نام کاربری");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("رمز عبور");

        Button loginButton = new Button("ورود");
        loginButton.getStyleClass().add("button-primary");
        loginButton.setMaxWidth(Double.MAX_VALUE);
        loginButton.setDefaultButton(true);
        loginButton.setOnAction(e -> {
            try {
                JsonObject body = new JsonObject();
                body.addProperty("username", usernameField.getText().trim());
                body.addProperty("password", passwordField.getText());

                JsonObject response = ApiClient.post("/api/auth/login", body).getAsJsonObject();
                SessionManager.login(response.get("token").getAsString(),
                        response.getAsJsonObject("user"));

                MainApplication.switchTo(MainView.build(), 1050, 700);
            } catch (ApiException ex) {
                Ui.showError(ex.getMessage());
            }
        });

        Hyperlink registerLink = new Hyperlink("حساب کاربری ندارید؟ ثبت‌نام کنید");
        registerLink.setOnAction(e -> MainApplication.switchTo(RegisterView.build(), 460, 680));

        VBox card = new VBox(12, logo, title, subtitle,
                usernameField, passwordField, loginButton, registerLink);
        card.getStyleClass().add("card");
        card.setPadding(new Insets(30));
        card.setAlignment(Pos.CENTER);
        card.setMaxWidth(340);

        StackPane root = new StackPane(card);
        root.getStyleClass().add("login-root");
        root.setPadding(new Insets(30));
        return root;
    }
}
