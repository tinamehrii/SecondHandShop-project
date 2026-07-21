package com.secondhand.frontend;

import javafx.scene.control.Alert;
import javafx.scene.control.TextInputDialog;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.Optional;

/**
 * Small helper methods for alerts and formatting.
 */
public class Ui {

    private Ui() {
    }

    public static void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("خطا");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.getDialogPane().setNodeOrientation(javafx.geometry.NodeOrientation.RIGHT_TO_LEFT);
        alert.showAndWait();
    }

    public static void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("پیام");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.getDialogPane().setNodeOrientation(javafx.geometry.NodeOrientation.RIGHT_TO_LEFT);
        alert.showAndWait();
    }

    /** Shows a simple text input dialog and returns the entered text. */
    public static Optional<String> askText(String title, String prompt) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle(title);
        dialog.setHeaderText(null);
        dialog.setContentText(prompt);
        dialog.getDialogPane().setNodeOrientation(javafx.geometry.NodeOrientation.RIGHT_TO_LEFT);
        return dialog.showAndWait();
    }

    /** Persian label of an advertisement status. */
    public static String statusFa(String status) {
        switch (status) {
            case "PENDING":
                return "در انتظار تایید";
            case "ACTIVE":
                return "فعال";
            case "REJECTED":
                return "رد شده";
            case "SOLD":
                return "فروخته شده";
            case "DELETED":
                return "حذف شده";
            default:
                return status;
        }
    }

    /** Formats 1500000 as "1,500,000 تومان". */
    public static String formatPrice(long price) {
        return NumberFormat.getNumberInstance(Locale.US).format(price) + " تومان";
    }
}
