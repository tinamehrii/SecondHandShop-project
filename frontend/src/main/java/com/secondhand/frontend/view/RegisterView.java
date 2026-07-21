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
 * Register page, shown as a white card in the middle of the window.
 */
public class RegisterView {

    public static Parent build() {
        Label title = new Label("ثبت‌نام کاربر جدید");
        title.getStyleClass().add("page-title");

        Label subtitle = new Label("چند لحظه بیشتر طول نمی‌کشد");
        subtitle.getStyleClass().add("muted");

        TextField fullNameField = new TextField();
        fullNameField.setPromptText("نام و نام خانوادگی");

        TextField usernameField = new TextField();
        usernameField.setPromptText("نام کاربری (حداقل ۴ کاراکتر)");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("رمز عبور (حداقل ۶ کاراکتر)");

        TextField phoneField = new TextField();
        phoneField.setPromptText("شماره موبایل (مثال: 09123456789)");

        TextField emailField = new TextField();
        emailField.setPromptText("ایمیل");

        Button registerButton = new Button("ثبت‌نام");
        registerButton.getStyleClass().add("button-primary");
        registerButton.setMaxWidth(Double.MAX_VALUE);
        registerButton.setDefaultButton(true);
        registerButton.setOnAction(e -> {
            try {
                JsonObject body = new JsonObject();
                body.addProperty("fullName", fullNameField.getText().trim());
                body.addProperty("username", usernameField.getText().trim());
                body.addProperty("password", passwordField.getText());
                body.addProperty("phoneNumber", phoneField.getText().trim());
                body.addProperty("email", emailField.getText().trim());

                JsonObject response = ApiClient.post("/api/auth/register", body).getAsJsonObject();
                SessionManager.login(response.get("token").getAsString(),
                        response.getAsJsonObject("user"));

                Ui.showInfo("ثبت‌نام با موفقیت انجام شد");
                MainApplication.switchTo(MainView.build(), 1050, 700);
            } catch (ApiException ex) {
                Ui.showError(ex.getMessage());
            }
        });

        Hyperlink backLink = new Hyperlink("قبلاً ثبت‌نام کرده‌اید؟ ورود");
        backLink.setOnAction(e -> MainApplication.switchTo(LoginView.build(), 460, 600));

        VBox card = new VBox(12, title, subtitle, fullNameField, usernameField,
                passwordField, phoneField, emailField, registerButton, backLink);
        card.getStyleClass().add("card");
        card.setPadding(new Insets(30));
        card.setAlignment(Pos.CENTER);
        card.setMaxWidth(340);

        StackPane root = new StackPane(card);
        root.setPadding(new Insets(30));
        return root;
    }
}
