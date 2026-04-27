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

        primaryStage.setTitle("Рабочее окно");
        primaryStage.setScene(scene);
        primaryStage.setX(100);
        primaryStage.setY(100);

        Stage secondStage = new Stage();
        secondPane = new Pane();
        secondPane.setPrefSize(width, height);
        Scene scene2 = new Scene(secondPane, width, height);

        secondStage.setTitle("Второе окно");
        secondStage.setScene(scene2);
        secondStage.setX(primaryStage.getX() + width + 180);
        secondStage.setY(primaryStage.getY());

        primaryStage.setOnCloseRequest(e -> secondStage.close());
        secondStage.setOnCloseRequest(e -> primaryStage.close());

        setupDrop(mainPane);
        setupDrop(secondPane);

        Button start = new Button("Начать движение");
        Button stop = new Button("Остановить");
        Button mode = new Button("Сменить режим");
        Button clear = new Button("Очистить");

        for (Button b : List.of(start, stop, mode, clear)) {
            b.setPrefSize(130, 35);
            b.setStyle("-fx-background-color: #ecf0f1; -fx-background-radius: 8;");
        }

        Button circleBtn = createShapeButton("circle");
        Button rectBtn = createShapeButton("rect");
        Button triangleBtn = createShapeButton("triangle");

        HBox shapesBox = new HBox(8, circleBtn, rectBtn, triangleBtn);
        shapesBox.setAlignment(Pos.CENTER);
        shapesBox.setStyle("-fx-background-color: #ffffff22; -fx-padding: 8; -fx-background-radius: 8;");

        menu.getChildren().addAll(shapesBox, start, stop, mode, clear);

        circleBtn.setOnAction(e -> addShape(createCircle()));
        rectBtn.setOnAction(e -> addShape(createRect()));
        triangleBtn.setOnAction(e -> addShape(createTriangle()));

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
        timer.start();

        primaryStage.show();
        secondStage.show();
    }

    // ===== SHAPES =====

    private Button createShapeButton(String type) {
        Button btn = new Button();
        btn.setGraphic(createIcon(type));
        return btn;
    }

    private Shape createIcon(String type) {
        if ("circle".equals(type)) return new Circle(8, randomColor());

        if ("triangle".equals(type)) {
            Polygon t = new Polygon(8, 0, 16, 16, 0, 16);
            t.setFill(randomColor());
            return t;
        }

        Rectangle r = new Rectangle(16, 16);
        r.setFill(randomColor());
        return r;
    }

    private Circle createCircle() {
        double r = 15 + Math.random() * 20;
        Circle c = new Circle(r, randomColor());

        c.setLayoutX(r + Math.random() * (mainPane.getWidth() - 2 * r));
        c.setLayoutY(r + Math.random() * (mainPane.getHeight() - 2 * r));

        return c;
    }

    private Rectangle createRect() {
        double w = 30 + Math.random() * 20;
        double h = 30 + Math.random() * 20;

        Rectangle r = new Rectangle(w, h);
        r.setFill(randomColor());

        r.setLayoutX(Math.random() * (mainPane.getWidth() - w));
        r.setLayoutY(Math.random() * (mainPane.getHeight() - h));

        return r;
    }

    private Polygon createTriangle() {
        double size = 30 + Math.random() * 20;

        Polygon t = new Polygon(
                0.0, size,
                size / 2, 0.0,
                size, size
        );

        t.setFill(randomColor());

        t.setLayoutX(Math.random() * (mainPane.getWidth() - size));
        t.setLayoutY(Math.random() * (mainPane.getHeight() - size));

        return t;
    }

    private void addShape(Shape s) {
        s.setBlendMode(modes[modeIndex]);
        shapes.add(new MovingShape(s));
        mainPane.getChildren().add(s);
        enableDrag(s);
        addContextMenu(s);
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

            Bounds b = shape.getBoundsInParent();

            if (b.getMinX() <= 0 || b.getMaxX() >= bounds.getWidth()) dx *= -1;
            if (b.getMinY() <= 0 || b.getMaxY() >= bounds.getHeight()) dy *= -1;

            shape.setLayoutX(x + dx);
            shape.setLayoutY(y + dy);
        }
    }

    // ===== CONTEXT MENU =====

    private void addContextMenu(Shape s) {
        ContextMenu menu = new ContextMenu();
        MenuItem delete = new MenuItem("Удалить");

        delete.setOnAction(e -> {
            ((Pane) s.getParent()).getChildren().remove(s);
            shapes.removeIf(ms -> ms.shape == s);
        });

        menu.getItems().add(delete);

        s.setOnContextMenuRequested(e ->
                menu.show(s, e.getScreenX(), e.getScreenY()));
    }

    // ===== DRAG & DROP =====

    private void enableDrag(Shape s) {

        s.setOnDragDetected(e -> {
            Dragboard db = s.startDragAndDrop(TransferMode.MOVE);

            ClipboardContent content = new ClipboardContent();
            content.putString("shape");
            db.setContent(content);

            db.setDragView(s.snapshot(null, null));

            draggingShape = s;
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

            if (sourcePane != targetPane) {
                sourcePane.getChildren().remove(draggingShape);
                targetPane.getChildren().add(draggingShape);
            }

            draggingShape.setLayoutX(e.getX());
            draggingShape.setLayoutY(e.getY());
            draggingShape.setVisible(true);

            if (targetPane == secondPane) {
                shapes.removeIf(ms -> ms.shape == draggingShape);
                draggingShape.setOnContextMenuRequested(null);
                enableSecondWindowBehavior(draggingShape);
            } else {
                if (shapes.stream().noneMatch(ms -> ms.shape == draggingShape)) {
                    shapes.add(new MovingShape(draggingShape));
                }
                enableDrag(draggingShape);
                addContextMenu(draggingShape);
            }

            e.setDropCompleted(true);
            draggingShape = null;
            e.consume();
        });
    }

    // ===== SECOND WINDOW =====

    private void enableSecondWindowBehavior(Shape s) {
        s.setOnMouseClicked(e -> {
            if (e.getButton() == MouseButton.PRIMARY) {
                secondPane.getChildren().remove(s);
            } else if (e.getButton() == MouseButton.SECONDARY) {
                s.setFill(randomColor());
            }
        });
    }

    private Color randomColor() {
        return Color.hsb(Math.random() * 360, 0.7, 0.9);
    }

    public static void main(String[] args) {
        launch();
    }
}