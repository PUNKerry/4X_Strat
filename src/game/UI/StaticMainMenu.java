package game.UI;

import game.app.MainApp;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

public class StaticMainMenu {

    public static StackPane mainMenuRoot;
    public static void createMainMenu(MainApp game) {
        mainMenuRoot = new StackPane();
        mainMenuRoot.setStyle("-fx-background-color: rgba(20,20,30,1);");
        mainMenuRoot.setAlignment(Pos.CENTER);

        VBox menuBox = new VBox(30);
        menuBox.setAlignment(Pos.CENTER);

        Label title = new Label("ГЛОБАЛЬНАЯ СТРАТЕГИЯ");
        title.setTextFill(Color.WHITE);
        title.setFont(Font.font(42));
        title.setStyle("-fx-font-weight: bold;");

        Button newGameBtn = new Button("Новая игра");
        newGameBtn.setStyle("-fx-font-size: 24px; -fx-background-color: #2a7a2a; -fx-text-fill: white; -fx-padding: 15 40; -fx-cursor: hand;");
        newGameBtn.setOnAction(e -> game.startNewGame());

        Button exitBtn = new Button("Выход");
        exitBtn.setStyle("-fx-font-size: 24px; -fx-background-color: #8b0000; -fx-text-fill: white; -fx-padding: 15 40; -fx-cursor: hand;");
        exitBtn.setOnAction(e -> System.exit(0));

        menuBox.getChildren().addAll(title, newGameBtn, exitBtn);
        mainMenuRoot.getChildren().add(menuBox);
    }
}
