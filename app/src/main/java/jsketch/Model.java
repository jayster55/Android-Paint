package jsketch;

import android.graphics.Color;
import android.graphics.Rect;

import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

public class Model extends Observable {

    // ------------------------------------------------------
    // -------------- Variable Declarations -----------------
    // ------------------------------------------------------
    private ArrayList<Shape> shapes = new ArrayList<Shape>();
    private SelectedTool activeTool = SelectedTool.SELECT;
    private int activeColor = Color.BLACK;

    private Shape recentShapeCreation = null;
    private Shape recentShapeSelection = null;

    int previousX;
    int previousY;

    Model() { }

    // ----------------------------------------------------
    // ---------------   Get Methods   -----------------
    // ----------------------------------------------------
    public ArrayList<Shape> getShapes() { return shapes; }
    public SelectedTool getActiveTool() { return activeTool; }
    public int getActiveColor() { return activeColor; }
    public Shape getRecentShapeCreation() { return recentShapeCreation; }


    // ----------------------------------------------------
    // ---------------   Set Methods   -----------------
    // ----------------------------------------------------
    public void setActiveTool(SelectedTool tool) { activeTool = tool; update(); }
    public void setActiveColor(int color) { activeColor = color; update(); }

    // ------------------------------------------------------------
    // ---------------   Shape List Aggregators   -----------------
    // ------------------------------------------------------------
    public void wipeShapes() { shapes = new ArrayList<Shape>(); update(); }

    public void loadShapes(ArrayList<Shape> loadedShapeList) {
        shapes = loadedShapeList;
        update();
    }

    public void addShapeToList() {
        shapes.add(recentShapeCreation);
        recentShapeCreation = null;
        update();
    }

    // ----------------------------------------------------
    // ---------------   Shape Operators -----------------
    // ----------------------------------------------------

    private boolean collisionDetected(Shape shape, int x, int y) {

        int xStart = (int) Math.min(shape.xCoord1, shape.xCoord2);
        int yStart = (int ) Math.min(shape.yCoord1, shape.yCoord2);
        int xStop = (int) Math.max(shape.xCoord1, shape.xCoord2);
        int yStop = (int) Math.max(shape.yCoord1, shape.yCoord2);

        Rect bounds = new Rect(xStart, yStart, xStop, yStop);

        return bounds.contains(x, y); // returns true if in bounds
    }


    public void drawShape(int x1, int y1, int x2, int y2) {

        // getting start and stop coords
        int xStart = Math.min(x1, x2);
        int yStart = Math.min(y1, y2);
        int xStop = Math.max(x1, x2);
        int yStop = Math.max(y1, y2);

        // getting width and height of shape
        int width = Math.abs(xStart - xStop);
        int height = Math.abs(yStart - yStop);

        // get size of shape (only needed for circle)
        int size = Math.max(width, height);

        switch (activeTool) {
            // activate tool and make shape accordingly
            case RECTANGLE: { recentShapeCreation = makeRectangle(xStart, yStart, xStop, yStop); break; }
            case CIRCLE: { recentShapeCreation = makeCircle(xStart, yStart, size); break; }
            case LINE: { recentShapeCreation = makeLine(x1,y1,x2,y2); break; }
        }
        update();
    }

    public void setSelectedShapeColor(int color) {
        if(!recentShapeSelection.equals(null)) { recentShapeSelection.strokeColor = color; }
        update();
    }

    public void wipeRecentShape() { recentShapeSelection = null; update(); }

    public void eraseShape(int x, int y) {
        // counts in reverse order from most recently created shape to least recent
        int i = shapes.size() - 1;
        while (i >= 0) {
            boolean hit  = collisionDetected(shapes.get(i), x, y);
            if (hit) { shapes.remove(i); break; }
            i--;
        }
        update();
    }

    public void selectShape(int x, int y) {
        previousX = x;
        previousY = y;
        int i = shapes.size() - 1;

        while (i >= 0) {
            boolean hit = collisionDetected(shapes.get(i), x, y);
            if (hit) {
                wipeRecentShape();
                recentShapeSelection = shapes.get(i);
                activeColor = recentShapeSelection.strokeColor;
                break;
            }
            i--;
        }

        update();
    }

    public void moveShape(int x, int y) {

        boolean shapeExists = recentShapeSelection != null;

        if(shapeExists) {
            int deltaX = x - previousX;
            int deltaY = y - previousY;

            recentShapeSelection.xCoord1 += deltaX;
            recentShapeSelection.xCoord2 += deltaX;
            previousX = x;

            recentShapeSelection.yCoord1 += deltaY;
            recentShapeSelection.yCoord2 += deltaY;
            previousY = y;
        }
        update();
    }

    public void scaleShape(float factor) {

        boolean shapeExists = recentShapeSelection != null;

        if(shapeExists) {

            float width = Math.abs(recentShapeSelection.xCoord2 - recentShapeSelection.xCoord1);
            float height = Math.abs(recentShapeSelection.yCoord2 - recentShapeSelection.yCoord1);

            boolean tooSmall = (width <= 2 || height <= 2) && factor < 1;

            if(tooSmall) { return; } // prevents shapes from disappearing when shrunk smaller

            float dX = (width * factor) - width;
            float dY = (height * factor) - height;

            boolean firstXSmaller = recentShapeSelection.xCoord1 < recentShapeSelection.xCoord2;
            boolean firstYSmaller = recentShapeSelection.yCoord1 < recentShapeSelection.yCoord2;

            scaleX(firstXSmaller, dX);
            scaleY(firstYSmaller, dY);
        }
        update();
    }

    private void scaleX(boolean firstXSmaller, float dX) {
        if (firstXSmaller) {
            recentShapeSelection.xCoord1 -= dX / 2;
            recentShapeSelection.xCoord2 += dX / 2;
        } else {
            recentShapeSelection.xCoord1 += dX / 2;
            recentShapeSelection.xCoord2 -= dX / 2;
        }
    }

    private void scaleY(boolean firstYSmaller, float dY) {
        if (firstYSmaller) {
            recentShapeSelection.yCoord1 -= dY / 2;
            recentShapeSelection.yCoord2 += dY / 2;
        } else {
            recentShapeSelection.yCoord1 += dY / 2;
            recentShapeSelection.yCoord2 -= dY / 2;
        }
    }

    // ----------------------------------------------------
    // ---------------   Shape Creation   -----------------
    // ----------------------------------------------------

    private Shape makeCircle(int xStart, int yStart, int radius) {
        return new Shape(SelectedTool.CIRCLE, xStart, yStart, xStart+radius, yStart+radius, activeColor);
    }
    private Shape makeRectangle(int xStart, int yStart, int xStop, int yStop) {
        return new Shape(SelectedTool.RECTANGLE, xStart, yStart, xStop, yStop, activeColor);
    }
    private Shape makeLine(int xStart, int yStart, int xStop, int yStop) {
        return new Shape(SelectedTool.LINE, xStart, yStart, xStop, yStop, activeColor);
    }

    // ----------------------------------------------------
    // ---------------  Observer Methods  -----------------
    // ----------------------------------------------------
    public void update() { setChanged(); notifyObservers(); }
    @Override
    public void notifyObservers() { super.notifyObservers(); }
    @Override
    protected void setChanged() { super.setChanged(); }
    @Override
    protected void clearChanged() { super.clearChanged(); }
    @Override
    public void addObserver(Observer observer) { super.addObserver(observer); }
    @Override
    public synchronized void deleteObservers() { super.deleteObservers(); }

}