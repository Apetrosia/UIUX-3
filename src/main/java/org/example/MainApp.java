package org.example;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.geometry.*;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.BlendMode;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseButton;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.stage.Screen;
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

        Rectangle2D screen = Screen.getPrimary().getVisualBounds();

        mainPane = new Pane();
        mainPane.setPrefSize(800, 600);

        VBox menu = new VBox(10);
        menu.setPadding(new Insets(10));
        menu.setAlignment(Pos.CENTER);
        menu.setStyle("-fx-background-color: #cccccc;");
        menu.setPrefWidth(200);

        BorderPane root = new BorderPane(mainPane, null, null, null, menu);
        Scene scene = new Scene(root, 1000, 600);

        primaryStage.setTitle("Primary");
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(800);
        primaryStage.setMinHeight(500);
        primaryStage.setMaxWidth(screen.getWidth());
        primaryStage.setMaxHeight(screen.getHeight());

        Stage secondStage = new Stage();
        secondPane = new Pane();
        secondPane.setPrefSize(400, 400);
        Scene scene2 = new Scene(secondPane);

        setupDrop(mainPane);
        setupDrop(secondPane);

        secondStage.setTitle("Secondary");
        secondStage.setScene(scene2);
        secondStage.setMinWidth(300);
        secondStage.setMinHeight(300);
        secondStage.setMaxWidth(screen.getWidth());
        secondStage.setMaxHeight(screen.getHeight());

        Button start = new Button("Начать движение");
        Button stop = new Button("Остановить движение");
        Button mode = new Button("Сменить режим");
        Button clear = new Button("Очистить");

        for (Button b : List.of(start, stop, mode, clear)) {
            b.setPrefSize(180, 40);
        }

        Button circleBtn = createShapeButton("circle");
        Button rectBtn = createShapeButton("rect");

        circleBtn.setPrefSize(40, 40);
        rectBtn.setPrefSize(40, 40);

        HBox shapesBox = new HBox(10, circleBtn, rectBtn);
        shapesBox.setAlignment(Pos.CENTER);

        menu.getChildren().addAll(shapesBox, start, stop, mode, clear);

        circleBtn.setOnAction(e -> {
            addShape(createCircle(circleBtn));
            updateButton(circleBtn, "circle");
        });

        rectBtn.setOnAction(e -> {
            addShape(createRect(rectBtn));
            updateButton(rectBtn, "rect");
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

    // ===================== SHAPES =====================

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

    private void addShape(Shape s) {
        shapes.add(new MovingShape(s));
        mainPane.getChildren().add(s);
        enableDrag(s);
        addMenu(s);
    }

    // ===================== MOVEMENT =====================

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
            }

            shape.setLayoutX(x + dx);
            shape.setLayoutY(y + dy);
        }
    }

    // ===================== CONTEXT =====================

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

    // ===================== DRAG =====================

    private void enableDrag(Shape s) {

        final boolean[] outside = {false};

        s.setOnMousePressed(e -> {
            if (e.getButton() != MouseButton.PRIMARY) return;

            MovingShape msS = findMovingShape(s);
            savedDx = msS.dx;
            savedDy = msS.dy;

            draggingShape = s;

            originalPos = new Point2D(s.getLayoutX(), s.getLayoutY());

            dragOffset = new Point2D(
                    e.getSceneX() - s.getLayoutX(),
                    e.getSceneY() - s.getLayoutY()
            );

            outside[0] = false;
            dragged = false;

            shapes.stream()
                    .filter(ms -> ms.shape == s)
                    .forEach(ms -> {
                        ms.dx = 0;
                        ms.dy = 0;
                    });
        });

        s.setOnDragDetected(e -> {
            if (e.getButton() != MouseButton.PRIMARY) return;

            Dragboard db = s.startDragAndDrop(TransferMode.ANY);

            ClipboardContent content = new ClipboardContent();

            if (s instanceof Circle) {
                content.putString("circle");
            } else {
                content.putString("rect");
            }

            db.setContent(content);
            db.setDragView(s.snapshot(null, null));

            dndActive[0] = true;

            e.consume();
        });

        s.setOnMouseDragged(e -> {
            if (draggingShape == null || s != draggingShape) return;

            if (dndActive[0]) return;

            s.setLayoutX(e.getSceneX() - dragOffset.getX());
            s.setLayoutY(e.getSceneY() - dragOffset.getY());

            if (isCompletelyOutside(s, (Pane) s.getParent())) {
                outside[0] = true;
            }

            dragged = true;
        });

        s.setOnMouseReleased(e -> {
            if (draggingShape == null) return;

            if (!outside[0]) {
                if (isInside(e, mainPane)) {
                    clamp(s, mainPane);
                } else {
                    s.setLayoutX(originalPos.getX());
                    s.setLayoutY(originalPos.getY());
                }
            }

            if (!dragged) {
                MovingShape ms = findMovingShape(draggingShape);
                ms.dx = savedDx;
                ms.dy = savedDy;
            }

            draggingShape = null;
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

            if (!db.hasString()) {
                e.setDropCompleted(false);
                return;
            }

            String type = db.getString();

            Shape newShape;

            if ("circle".equals(type)) {
                Circle c = (Circle) draggingShape;
                newShape = new Circle(c.getRadius(), c.getFill());
            } else {
                Rectangle r = (Rectangle) draggingShape;
                Rectangle copy = new Rectangle(r.getWidth(), r.getHeight());
                copy.setFill(r.getFill());
                newShape = copy;
            }

            newShape.setLayoutX(e.getX());
            newShape.setLayoutY(e.getY());

            targetPane.getChildren().add(newShape);

            if (targetPane == mainPane) {
                MovingShape newMs = new MovingShape(newShape);
                newMs.dx = savedDx;
                newMs.dy = savedDy;
                shapes.add(newMs);

                enableDrag(newShape);
                addMenu(newShape);
            } else {
                enableSecondWindow(newShape);
            }

            mainPane.getChildren().remove(draggingShape);
            shapes.removeIf(ms -> ms.shape == draggingShape);

            e.setDropCompleted(true);
            e.consume();
        });

        targetPane.setOnDragDone(e -> {
            if (draggingShape == null) return;

            if (!e.isDropCompleted()) {
                if (draggingShape.getParent() == null) {
                    mainPane.getChildren().add(draggingShape);
                }

                MovingShape ms = findMovingShape(draggingShape);
                ms.dx = savedDx;
                ms.dy = savedDy;

                draggingShape.setLayoutX(originalPos.getX());
                draggingShape.setLayoutY(originalPos.getY());

                draggingShape.setVisible(true);

                if (shapes.stream().noneMatch(ms_ -> ms_.shape == draggingShape)) {
                    shapes.add(new MovingShape(draggingShape));
                    enableDrag(draggingShape);
                    addMenu(draggingShape);
                }
            }

            draggingShape = null;
            e.consume();
            dndActive[0] = false;
        });
    }

    private MovingShape findMovingShape(Shape s) {
        return shapes.stream()
                .filter(ms -> ms.shape == s)
                .findFirst()
                .orElse(null);
    }

    private boolean isCompletelyOutside(Shape s, Pane p) {

        double x = s.getLayoutX();
        double y = s.getLayoutY();

        if (s instanceof Circle) {
            Circle c = (Circle) s;
            double r = c.getRadius();

            return (x + r < 0) ||
                    (x - r > p.getWidth()) ||
                    (y + r < 0) ||
                    (y - r > p.getHeight());
        } else {
            Rectangle r = (Rectangle) s;

            return (x + r.getWidth() < 0) ||
                    (x > p.getWidth()) ||
                    (y + r.getHeight() < 0) ||
                    (y > p.getHeight());
        }
    }

    private void clamp(Shape s, Pane p) {
        double x = s.getLayoutX();
        double y = s.getLayoutY();

        if (s instanceof Circle) {
            Circle c = (Circle) s;
            double r = c.getRadius();

            x = Math.max(r, Math.min(x, p.getWidth() - r));
            y = Math.max(r, Math.min(y, p.getHeight() - r));

        } else {
            Rectangle r = (Rectangle) s;

            x = Math.max(0, Math.min(x, p.getWidth() - r.getWidth()));
            y = Math.max(0, Math.min(y, p.getHeight() - r.getHeight()));
        }

        s.setLayoutX(x);
        s.setLayoutY(y);
    }

    private boolean isInside(javafx.scene.input.MouseEvent e, Pane p) {
        Point2D pt = p.sceneToLocal(e.getSceneX(), e.getSceneY());
        return pt.getX() >= 0 && pt.getX() <= p.getWidth()
                && pt.getY() >= 0 && pt.getY() <= p.getHeight();
    }

    // ===================== SECOND WINDOW =====================

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