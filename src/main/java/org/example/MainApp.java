package org.example;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.geometry.*;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.BlendMode;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

public class MainApp extends Application {

    private Pane mainPane;
    private Pane secondPane;
    private final List<MovingShape> shapes = new ArrayList<>();

    private AnimationTimer timer;

    private final BlendMode[] modes = {
            BlendMode.SRC_OVER,
            BlendMode.MULTIPLY,
            BlendMode.SCREEN
    };
    private int modeIndex = 0;

    private Shape draggingShape;

    @Override
    public void start(Stage primaryStage) {

        double width = 450;
        double height = 350;

        mainPane = new Pane();
        mainPane.setPrefSize(width, height);

        VBox menu = new VBox(10);
        menu.setPadding(new Insets(10));
        menu.setAlignment(Pos.CENTER);
        menu.setPrefWidth(150);

        menu.setStyle("-fx-background-color: linear-gradient(to bottom, #2c3e50, #34495e);");

        BorderPane root = new BorderPane(mainPane, null, null, null, menu);
        Scene scene = new Scene(root, width + 150, height);

        primaryStage.setTitle("Workspace");
        primaryStage.setScene(scene);
        primaryStage.setX(100);
        primaryStage.setY(100);

        Stage secondStage = new Stage();
        secondPane = new Pane();
        secondPane.setPrefSize(width, height);
        Scene scene2 = new Scene(secondPane, width, height);

        secondStage.setTitle("Preview");
        secondStage.setScene(scene2);

        secondStage.setX(primaryStage.getX() + width + 180);
        secondStage.setY(primaryStage.getY());

        // 💀 Закрытие обоих окон
        primaryStage.setOnCloseRequest(e -> {
            secondStage.close();
        });

        secondStage.setOnCloseRequest(e -> {
            primaryStage.close();
        });

        setupDrop(mainPane);
        setupDrop(secondPane);

        Button start = new Button("▶");
        Button stop = new Button("⏸");
        Button mode = new Button("Mode");
        Button clear = new Button("Clear");

        for (Button b : List.of(start, stop, mode, clear)) {
            b.setPrefSize(120, 35);
            b.setStyle("-fx-background-color: #ecf0f1; -fx-background-radius: 8;");
        }

        Button circleBtn = createShapeButton("circle");
        Button rectBtn = createShapeButton("rect");
        Button triangleBtn = createShapeButton("triangle");

        HBox shapesBox = new HBox(8, circleBtn, rectBtn, triangleBtn);
        shapesBox.setAlignment(Pos.CENTER);
        shapesBox.setStyle("-fx-background-color: #ffffff22; -fx-padding: 8; -fx-background-radius: 8;");

        menu.getChildren().addAll(shapesBox, start, stop, mode, clear);

        circleBtn.setOnAction(e -> addShape(createCircle(circleBtn)));
        rectBtn.setOnAction(e -> addShape(createRect(rectBtn)));
        triangleBtn.setOnAction(e -> addShape(createTriangle(triangleBtn)));

        start.setOnAction(e -> timer.start());
        stop.setOnAction(e -> timer.stop());

        mode.setOnAction(e -> {
            modeIndex = (modeIndex + 1) % modes.length;
            shapes.forEach(s -> s.shape.setBlendMode(modes[modeIndex]));
        });

        clear.setOnAction(e -> {
            mainPane.getChildren().clear();
            secondPane.getChildren().clear();
            shapes.clear();
        });

        timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                for (MovingShape s : shapes) {
                    s.move(mainPane);
                }
            }
        };

        primaryStage.show();
        secondStage.show();
    }

    // ===== SHAPES =====

    private Button createShapeButton(String type) {
        Button btn = new Button();
        Color c = randomColor();
        btn.setUserData(c);
        btn.setGraphic(createIcon(type, c));
        return btn;
    }

    private Shape createIcon(String type, Color c) {
        if ("circle".equals(type)) return new Circle(8, c);

        if ("triangle".equals(type)) {
            Polygon t = new Polygon(8, 0, 16, 16, 0, 16);
            t.setFill(c);
            return t;
        }

        Rectangle r = new Rectangle(16, 16);
        r.setFill(c);
        return r;
    }

    private Circle createCircle(Button btn) {
        Circle c = new Circle(15 + Math.random() * 20, randomColor());
        c.setLayoutX(Math.random() * mainPane.getWidth());
        c.setLayoutY(Math.random() * mainPane.getHeight());
        return c;
    }

    private Rectangle createRect(Button btn) {
        Rectangle r = new Rectangle(30 + Math.random() * 20, 30 + Math.random() * 20);
        r.setFill(randomColor());
        r.setLayoutX(Math.random() * mainPane.getWidth());
        r.setLayoutY(Math.random() * mainPane.getHeight());
        return r;
    }

    private Polygon createTriangle(Button btn) {
        double size = 30 + Math.random() * 20;

        Polygon t = new Polygon(
                0.0, size,
                size / 2, 0.0,
                size, size
        );

        t.setFill(randomColor());
        t.setLayoutX(Math.random() * mainPane.getWidth());
        t.setLayoutY(Math.random() * mainPane.getHeight());
        return t;
    }

    private void addShape(Shape s) {
        shapes.add(new MovingShape(s));
        mainPane.getChildren().add(s);
        enableDrag(s);
    }

    // ===== MOVEMENT =====

    private static class MovingShape {
        Shape shape;
        double dx = Math.random() * 3 - 1.5;
        double dy = Math.random() * 3 - 1.5;

        MovingShape(Shape s) {
            shape = s;
        }

        void move(Pane bounds) {
            double x = shape.getLayoutX();
            double y = shape.getLayoutY();

            double nextX = x + dx;
            double nextY = y + dy;

            if (shape instanceof Circle) {
                Circle c = (Circle) shape;
                double r = c.getRadius();

                if (nextX - r <= 0 || nextX + r >= bounds.getWidth()) {
                    dx *= -1;
                }

                if (nextY - r <= 0 || nextY + r >= bounds.getHeight()) {
                    dy *= -1;
                }

            } else if (shape instanceof Rectangle) {
                Rectangle r = (Rectangle) shape;

                if (nextX <= 0 || nextX + r.getWidth() >= bounds.getWidth()) {
                    dx *= -1;
                }

                if (nextY <= 0 || nextY + r.getHeight() >= bounds.getHeight()) {
                    dy *= -1;
                }

            } else if (shape instanceof Polygon) {
                Bounds b = shape.getBoundsInParent();

                if (b.getMinX() <= 0 || b.getMaxX() >= bounds.getWidth()) {
                    dx *= -1;
                }

                if (b.getMinY() <= 0 || b.getMaxY() >= bounds.getHeight()) {
                    dy *= -1;
                }
            }

            shape.setLayoutX(x + dx);
            shape.setLayoutY(y + dy);
        }
    }

    // ===== DRAG =====

    private void enableDrag(Shape s) {

        s.setOnDragDetected(e -> {
            Dragboard db = s.startDragAndDrop(TransferMode.MOVE);

            ClipboardContent content = new ClipboardContent();
            content.putString("shape");
            db.setContent(content);

            db.setDragView(s.snapshot(null, null));

            draggingShape = s;

            // 💡 скрываем оригинал
            s.setVisible(false);

            e.consume();
        });
    }

    private void setupDrop(Pane targetPane) {

        targetPane.setOnDragOver(e -> {
            if (e.getDragboard().hasString()) {
                e.acceptTransferModes(TransferMode.MOVE);
            }
            e.consume();
        });

        targetPane.setOnDragDropped(e -> {

            if (draggingShape == null) return;

            Pane sourcePane = (Pane) draggingShape.getParent();

            // 👉 если в то же окно — просто перемещаем
            if (sourcePane == targetPane) {
                draggingShape.setLayoutX(e.getX());
                draggingShape.setLayoutY(e.getY());
                draggingShape.setVisible(true);
            } else {
                // 👉 перенос в другое окно
                sourcePane.getChildren().remove(draggingShape);
                targetPane.getChildren().add(draggingShape);

                draggingShape.setLayoutX(e.getX());
                draggingShape.setLayoutY(e.getY());
                draggingShape.setVisible(true);
            }

            e.setDropCompleted(true);
            draggingShape = null;
            e.consume();
        });
    }

    private Color randomColor() {
        return Color.color(Math.random(), Math.random(), Math.random());
    }

    public static void main(String[] args) {
        launch();
    }
}