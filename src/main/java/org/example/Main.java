package org.example;

import processing.core.PApplet;
import processing.core.PShape;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

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
    PShape pShape;
    float xMul;
    float yMul;
    float x;
    float y;
    final float initialX;
    final float initialY;
    float width;
    float height;
    List<FacePart> children = new ArrayList<>();

    public FacePart(PShape shape, float xMul, float yMul, RectCoord coord) {
        this.pShape = shape;
        this.xMul = xMul;
        this.yMul = yMul;
        this.x = coord.x;
        this.y = coord.y;
        this.initialX = coord.x;
        this.initialY = coord.y;
        this.width = coord.width;
        this.height = coord.height;
    }

    public void move(float xDiff, float yDiff, RectCoord parent) {
        float xMove = xDiff * xMul;
        float yMove = yDiff * yMul;

        // はみ出るのを防止
        if (xMove + x < parent.x) {
            xMove = parent.x - x;
        } else if (xMove + x > parent.x + parent.width) {
            xMove = parent.x + parent.width - x;
        }

        if (yMove + y < parent.y) {
            yMove = parent.y - y;
        } else if (yMove + y > parent.y + parent.width) {
            yMove = parent.y + parent.width - y;
        }

        pShape.translate(xMove, yMove);

        RectCoord rectCoord = new RectCoord(x + xMove, y + yMove, width, height);

        for (FacePart child : children) {
            child.move(xMove, yMove, rectCoord);
        }
    }

    public void draw(BiConsumer<PShape, RectCoord> shape) {
        shape.accept(pShape, new RectCoord(initialX, initialY, width, height));
        for (FacePart child : children) {
            child.draw(shape);
        }
    }

    public FacePart addChild(FacePart child) {
        RectCoord newCoord = new RectCoord(child.x + x, child. y + y, child. width, child.height);
        children.add(new FacePart(child.pShape, child.xMul, child.yMul, newCoord));
        return this;
    }
}

public class Main extends PApplet {
    FacePart face;
    FacePart body;
    int oldMouseX = 0;
    int oldMouseY = 0;
    boolean pressed = false;
    double xDiff = 0;
    double yDiff = 0;

    @Override
    public void settings() {
        size(1000, 1000);
    }

    @Override
    public void setup() {
        background(255);
        frameRate(60);
        smooth();

        FacePart leftEye = new FacePart(loadShape("eye.svg"), 1.2f, 1f, new RectCoord(150, 150, 60, 60));
        FacePart rightEye = new FacePart(loadShape("eye.svg"), 1.2f, 1f, new RectCoord(310, 150, 60, 60));
        FacePart highlight = new FacePart(loadShape("eye_highlight.svg"), 0.2f, 0.05f, new RectCoord(10, 10, 20, 20));
        FacePart mouth = new FacePart(loadShape("mouth.svg"), 1.1f, 0.9f, new RectCoord(200, 250, 130, 30));
        FacePart leftWhiskers = new FacePart(loadShape("left_whiskers.svg"), 1.1f, 0.6f, new RectCoord(20, 205, 80, 85));
        FacePart rightWhiskers = new FacePart(loadShape("right_whiskers.svg"), 1.1f, 0.6f, new RectCoord(400, 210, 80, 85));

        face = new FacePart(loadShape("face.svg"), 0.7f, 0.3f, new RectCoord(100, 100, 550, 460))
//                .addChild(leftEye.addChild(highlight))
//                .addChild(rightEye.addChild(highlight))
                .addChild(highlight)
                .addChild(mouth)
                .addChild(rightWhiskers)
                .addChild(leftWhiskers);
    }

    @Override
    public void draw() {
        background(255);

        face.move((float) xDiff * 0.1f, (float) yDiff * 0.1f, new RectCoord(0, 0, 1000, 1000));
        face.draw((pShape, coord) -> shape(pShape, coord.x, coord.y, coord.width, coord.height));
    }

    @Override
    public void mouseDragged() {
        if (pressed) {
            xDiff = mouseX - oldMouseX;
            yDiff = mouseY - oldMouseY;
        }

        pressed = true;
        oldMouseX = mouseX;
        oldMouseY = mouseY;
    }

    @Override
    public void mouseReleased() {
        pressed = false;
        xDiff = 0;
        yDiff = 0;
    }

    public static void main(String[] args){
        PApplet.main("org.example.Main");
    }
}