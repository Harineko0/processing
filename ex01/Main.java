package org.example;

import processing.core.PApplet;
import processing.core.PImage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

enum Emotion {
    NORMAL, SMILE
}

class RectCoord {
    float x;
    float y;
    float width;
    float height;

    public RectCoord(float x, float y, float width, float height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }
}

class FacePart {
    PImage currentImage;
    PImage defaultImage;
    float xMul;
    float yMul;
    float x;
    float y;
    float width;
    float height;
    List<FacePart> children = new ArrayList<>();
    Map<Emotion, PImage> imageVariant = new HashMap<>();

    public FacePart(PImage shape, float xMul, float yMul, RectCoord coord) {
        this.currentImage = shape;
        this.defaultImage = shape;
        this.xMul = xMul;
        this.yMul = yMul;
        this.x = coord.x;
        this.y = coord.y;
        this.width = coord.width;
        this.height = coord.height;
    }

    public void move(float xDiff, float yDiff, RectCoord parent) {
        float xMove = xDiff * xMul;
        float yMove = yDiff * yMul;

        // はみ出るのを防止
//        if (xMove + x < parent.x) {
//            xMove = 0;
//        } else if (xMove + x > parent.x + parent.width) {
//            xMove = 0;
//        }
//
//        if (yMove + y < parent.y) {
//            yMove = parent.y - y;
//        } else if (yMove + y > parent.y + parent.width) {
//            yMove = parent.y + parent.width - y;
//        }

        x += xMove;
        y += yMove;

        RectCoord rectCoord = new RectCoord(x, y, width, height);

        for (FacePart child : children) {
            child.move(xMove, yMove, rectCoord);
        }
    }

    public void draw(BiConsumer<PImage, RectCoord> shape) {
        shape.accept(currentImage, new RectCoord(x, y, width, height));
        for (FacePart child : children) {
            child.draw(shape);
        }
    }

    public FacePart addChild(FacePart child) {
        RectCoord newCoord = new RectCoord(child.x + x, child. y + y, child. width, child.height);
        child.setCoord(newCoord);
        children.add(child);
        return this;
    }

    public void setCoord(RectCoord coord) {
        this.x = coord.x;
        this.y = coord.y;
        this.width = coord.width;
        this.height = coord.height;
    }

    public void changeVariant(Emotion key) {
        if (key == Emotion.NORMAL) {
            currentImage = defaultImage;
        }

        if (imageVariant.containsKey(key)) {
            currentImage = imageVariant.get(key);
        }

        for (FacePart child : children) {
            child.changeVariant(key);
        }
    }

    public FacePart addVariant(Emotion key, PImage image) {
        imageVariant.put(key, image);
        return this;
    }
}

public class Main extends PApplet {
    FacePart face;
    FacePart body;
    PImage background;
    int oldMouseX = 0;
    int oldMouseY = 0;
    boolean pressed = false;
    double xMove = 0;
    double yMove = 0;
    double xDiff = 0;
    double yDiff = 0;
    final Emotion[] emotions = Emotion.values();
    int currentEmotionIndex = 0;

    @Override
    public void settings() {
        size(1280 , 720);
    }

    @Override
    public void setup() {
        background(255);
        frameRate(60);
        smooth();

        background = loadImage("background.png");
        FacePart leftEye = new FacePart(loadImage("eye.png"), 1.2f, 1f, new RectCoord(75, 75, 30, 30))
                .addVariant(Emotion.SMILE, loadImage("smile_eye_l.png"));
        FacePart rightEye = new FacePart(loadImage("eye.png"), 1.2f, 1f, new RectCoord(155, 75, 30, 30))
                .addVariant(Emotion.SMILE, loadImage("smile_eye_r.png"));
//        FacePart highlight = new FacePart(loadImage("eye_highlight.png"), 0.2f, 0.05f, new RectCoord(100, 100, 20, 20));
        FacePart mouth = new FacePart(loadImage("mouth.png"), 1.15f, 0.9f, new RectCoord(100, 125, 65, 15));
        FacePart leftWhiskers = new FacePart(loadImage("left_whiskers.png"), 1.1f, 0.6f, new RectCoord(10, 102, 40, 42));
        FacePart rightWhiskers = new FacePart(loadImage("right_whiskers.png"), 1.1f, 0.6f, new RectCoord(200, 105, 40, 42));

        face = new FacePart(loadImage("face.png"), 0.7f, 0.3f, new RectCoord(640 - (275f / 2), 260 - (230f / 2), 275, 230))
                .addChild(leftEye)
                .addChild(rightEye)
                .addChild(mouth)
                .addChild(rightWhiskers)
                .addChild(leftWhiskers);
        body = new FacePart(loadImage("body.png"), 0.5f, 0.3f, new RectCoord(face.x + 25, face.y + 200, 220, 200));
    }

    @Override
    public void draw() {
        background(255);

        image(background, 0, 0, 1280, 720);
        face.move((float) xMove, (float) yMove, new RectCoord(400, 0, 880 , 720));
        face.draw((pImage, coord) -> image(pImage, coord.x, coord.y, coord.width, coord.height));
        body.move((float) xMove, (float) yMove, new RectCoord(400, 0, 880 , 720));
        body.draw((pImage, coord) -> image(pImage, coord.x, coord.y, coord.width, coord.height));

        if (mouseX == oldMouseX) xMove = 0;
        if (mouseY == oldMouseY) yMove = 0;
    }

    @Override
    public void mouseDragged() {
        if (pressed) {
            int xMax = 200; /* 動かせる幅の最大値 */
            int yMax = 200;
            xMove = Math.cos(Math.abs(xDiff / xMax)) * (mouseX - oldMouseX);
            yMove = Math.cos(Math.abs(yDiff / yMax)) * (mouseY - oldMouseY);
            xDiff += xMove;
            yDiff += yMove;
        }

        pressed = true;
        oldMouseX = mouseX;
        oldMouseY = mouseY;
    }

    @Override
    public void mouseReleased() {
        if (oldMouseX == 0 && oldMouseY == 0) {
            currentEmotionIndex++;

            if (currentEmotionIndex >= emotions.length) {
                currentEmotionIndex = 0;
            }

            face.changeVariant(emotions[currentEmotionIndex]);
        }

        pressed = false;
        xMove = 0;
        yMove = 0;
        oldMouseX = 0;
        oldMouseY = 0;
    }

    public static void main(String[] args){
        PApplet.main("org.example.Main");
    }
}