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
    private Point2D dragOffset;
    private Point2D originalPos;

    final boolean[] dndActive = {false};

    private double savedDx;
    private double savedDy;

    private boolean dragged = false;

    @Override
    public void start(Stage primaryStage) {

        double width = 600;
        double height = 500;

        mainPane = new Pane();
        mainPane.setPrefSize(width, height);

        VBox menu = new VBox(12);
        menu.setPadding(new Insets(15));
        menu.setAlignment(Pos.CENTER);
        menu.setPrefWidth(180);

        menu.setStyle("-fx-background-color: linear-gradient(to bottom, #2c3e50, #34495e);");

        BorderPane root = new BorderPane(mainPane, null, null, null, menu);
        Scene scene = new Scene(root, width + 180, height);

        primaryStage.setTitle("Workspace");
        primaryStage.setScene(scene);
        primaryStage.setWidth(width + 180);
        primaryStage.setHeight(height);
        primaryStage.setX(100);
        primaryStage.setY(100);

        Stage secondStage = new Stage();
        secondPane = new Pane();
        secondPane.setPrefSize(width, height);
        Scene scene2 = new Scene(secondPane, width, height);

        secondStage.setTitle("Preview");
        secondStage.setScene(scene2);
        secondStage.setWidth(width);
        secondStage.setHeight(height);
        secondStage.setX(primaryStage.getX() + primaryStage.getWidth() + 20);
        secondStage.setY(primaryStage.getY());

        setupDrop(mainPane);
        setupDrop(secondPane);

        Button start = new Button("▶ Start");
        Button stop = new Button("⏸ Stop");
        Button mode = new Button("Blend Mode");
        Button clear = new Button("Clear All");

        for (Button b : List.of(start, stop, mode, clear)) {
            b.setPrefSize(160, 40);
            b.setStyle("-fx-background-color: #ecf0f1; -fx-background-radius: 10; -fx-font-weight: bold;");
        }

        Button circleBtn = createShapeButton("circle");
        Button rectBtn = createShapeButton("rect");
        Button triangleBtn = createShapeButton("triangle");

        HBox shapesBox = new HBox(10, circleBtn, rectBtn, triangleBtn);
        shapesBox.setAlignment(Pos.CENTER);
        shapesBox.setStyle("-fx-background-color: #ffffff22; -fx-padding: 10; -fx-background-radius: 10;");

        menu.getChildren().addAll(shapesBox, start, stop, mode, clear);

        circleBtn.setOnAction(e -> {
            addShape(createCircle(circleBtn));
            updateButton(circleBtn, "circle");
        });

        rectBtn.setOnAction(e -> {
            addShape(createRect(rectBtn));
            updateButton(rectBtn, "rect");
        });

        triangleBtn.setOnAction(e -> {
            addShape(createTriangle(triangleBtn));
            updateButton(triangleBtn, "triangle");
        });

        start.setOnAction(e -> timer.start());
        stop.setOnAction(e -> timer.stop());

        mode.setOnAction(e -> {
            modeIndex = (modeIndex + 1) % modes.length;
            for (MovingShape s : shapes) {
                s.shape.setBlendMode(modes[modeIndex]);
            }
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

    private void updateButton(Button btn, String type) {
        Color c = randomColor();
        btn.setUserData(c);
        btn.setGraphic(createIcon(type, c));
    }

    private Shape createIcon(String type, Color c) {
        if ("circle".equals(type)) return new Circle(10, c);

        if ("triangle".equals(type)) {
            Polygon t = new Polygon(10, 0, 20, 20, 0, 20);
            t.setFill(c);
            return t;
        }

        Rectangle r = new Rectangle(20, 20);
        r.setFill(c);
        return r;
    }

    private Circle createCircle(Button btn) {
        Color c = (Color) btn.getUserData();
        double r = 20 + Math.random() * 30;
        Circle shape = new Circle(r, c);
        shape.setBlendMode(modes[modeIndex]);

        shape.setLayoutX(r + Math.random() * (mainPane.getWidth() - 2 * r));
        shape.setLayoutY(r + Math.random() * (mainPane.getHeight() - 2 * r));
        return shape;
    }

    private Rectangle createRect(Button btn) {
        Color c = (Color) btn.getUserData();
        double w = 40 + Math.random() * 40;
        double h = 40 + Math.random() * 40;

        Rectangle r = new Rectangle(w, h);
        r.setFill(c);
        r.setBlendMode(modes[modeIndex]);

        r.setLayoutX(Math.random() * (mainPane.getWidth() - w));
        r.setLayoutY(Math.random() * (mainPane.getHeight() - h));
        return r;
    }

    private Polygon createTriangle(Button btn) {
        Color c = (Color) btn.getUserData();

        double size = 30 + Math.random() * 40;

        Polygon t = new Polygon(
                0.0, size,
                size / 2, 0.0,
                size, size
        );

        t.setFill(c);
        t.setBlendMode(modes[modeIndex]);

        t.setLayoutX(Math.random() * (mainPane.getWidth() - size));
        t.setLayoutY(Math.random() * (mainPane.getHeight() - size));

        return t;
    }

    private void addShape(Shape s) {
        shapes.add(new MovingShape(s));
        mainPane.getChildren().add(s);
        enableDrag(s);
        addMenu(s);
    }

    // ===== MOVEMENT =====

    private static class MovingShape {
        Shape shape;
        double dx = Math.random() * 4 - 2;
        double dy = Math.random() * 4 - 2;

        MovingShape(Shape s) {
            shape = s;
        }

        void move(Pane bounds) {
            double x = shape.getLayoutX();
            double y = shape.getLayoutY();

            if (shape instanceof Circle) {
                Circle c = (Circle) shape;
                double r = c.getRadius();

                if (x - r <= 0) dx = Math.abs(dx);
                if (x + r >= bounds.getWidth()) dx = -Math.abs(dx);

                if (y - r <= 0) dy = Math.abs(dy);
                if (y + r >= bounds.getHeight()) dy = -Math.abs(dy);

            } else if (shape instanceof Rectangle) {
                Rectangle r = (Rectangle) shape;
                if (x <= 0) dx = Math.abs(dx);
                if (x + r.getWidth() >= bounds.getWidth()) dx = -Math.abs(dx);

                if (y <= 0) dy = Math.abs(dy);
                if (y + r.getHeight() >= bounds.getHeight()) dy = -Math.abs(dy);

            } else if (shape instanceof Polygon) {
                Bounds b = shape.getBoundsInParent();

                if (b.getMinX() <= 0) dx = Math.abs(dx);
                if (b.getMaxX() >= bounds.getWidth()) dx = -Math.abs(dx);

                if (b.getMinY() <= 0) dy = Math.abs(dy);
                if (b.getMaxY() >= bounds.getHeight()) dy = -Math.abs(dy);
            }

            shape.setLayoutX(x + dx);
            shape.setLayoutY(y + dy);
        }
    }

    // ===== CONTEXT MENU =====

    private void addMenu(Shape s) {
        ContextMenu m = new ContextMenu();
        MenuItem del = new MenuItem("Удалить");

        del.setOnAction(e -> {
            ((Pane) s.getParent()).getChildren().remove(s);
            shapes.removeIf(sh -> sh.shape == s);
        });

        m.getItems().add(del);

        s.setOnContextMenuRequested(e ->
                m.show(s, e.getScreenX(), e.getScreenY()));
    }

    // ===== DRAG & DROP =====

    private void enableDrag(Shape s) {

        s.setOnDragDetected(e -> {
            Dragboard db = s.startDragAndDrop(TransferMode.MOVE);

            ClipboardContent content = new ClipboardContent();

            if (s instanceof Circle) content.putString("circle");
            else if (s instanceof Rectangle) content.putString("rect");
            else content.putString("triangle");

            db.setContent(content);
            db.setDragView(s.snapshot(null, null));

            draggingShape = s;
            e.consume();
        });
    }

    private void setupDrop(Pane targetPane) {

        targetPane.setOnDragOver(e -> {
            if (e.getGestureSource() != targetPane && e.getDragboard().hasString()) {
                e.acceptTransferModes(TransferMode.MOVE);
            }
            e.consume();
        });

        targetPane.setOnDragDropped(e -> {

            Dragboard db = e.getDragboard();
            if (!db.hasString()) return;

            Shape newShape;

            if ("circle".equals(db.getString())) {
                Circle c = (Circle) draggingShape;
                newShape = new Circle(c.getRadius(), c.getFill());
            } else if ("rect".equals(db.getString())) {
                Rectangle r = (Rectangle) draggingShape;
                Rectangle copy = new Rectangle(r.getWidth(), r.getHeight());
                copy.setFill(r.getFill());
                newShape = copy;
            } else {
                Polygon old = (Polygon) draggingShape;
                Polygon copy = new Polygon();
                copy.getPoints().addAll(old.getPoints());
                copy.setFill(old.getFill());
                newShape = copy;
            }

            newShape.setLayoutX(e.getX());
            newShape.setLayoutY(e.getY());

            targetPane.getChildren().add(newShape);

            if (targetPane == mainPane) {
                addShape(newShape);
            } else {
                enableSecondWindow(newShape);
            }

            ((Pane) draggingShape.getParent()).getChildren().remove(draggingShape);
            shapes.removeIf(ms -> ms.shape == draggingShape);

            e.setDropCompleted(true);
            e.consume();
        });
    }

    // ===== SECOND WINDOW =====

    private void enableSecondWindow(Shape s) {
        s.setOnMouseClicked(e -> {
            if (e.getButton() == MouseButton.PRIMARY) {
                secondPane.getChildren().remove(s);
            } else if (e.getButton() == MouseButton.SECONDARY) {
                s.setFill(randomColor());
            }
        });
    }

    private Color randomColor() {
        return Color.color(Math.random(), Math.random(), Math.random());
    }

    public static void main(String[] args) {
        launch();
    }
}