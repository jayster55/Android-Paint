package jsketch;


import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RadioGroup;


public class PrimaryView extends LinearLayout implements Observer {
    // -----------------------------------------------------------
    // ---------------   Variable Declarations   -----------------
    // -----------------------------------------------------------
    private Model model;
    PrimaryCanvas canvas;

    int startX, startY;

    RadioGroup colors;
    RadioGroup tools;

    private boolean selectionComplete = true; // allows for initial changes to be made

    // -----------------------------------------------------------------
    // ---------------   Constructor for PrimaryView   -----------------
    // -----------------------------------------------------------------
    public PrimaryView(Context context, Model model) {
        super(context);

        // Model Initialization
        this.model = model;
        this.model.addObserver(this);

        // View Initialization
        View.inflate(context, R.layout.view, this);
        canvas = new PrimaryCanvas(context);
        ViewGroup v1 = findViewById(R.id.canvasContainer);
        v1.addView(canvas);

        // Assigning the groups by UI IDs
        colors = findViewById(R.id.colors);
        tools = findViewById(R.id.tools);

        tools.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int selection) {

                boolean toolbarChange = selectionComplete && selection != -1;

                if (toolbarChange) {

                    selectionComplete = false; // preventing other changes

                    switch (selection) {
                        case R.id.selectButton:
                            PrimaryView.this.model.setActiveTool(SelectedTool.SELECT);
                            PrimaryView.this.model.wipeRecentShape();
                            break;
                        case R.id.eraseButton:
                            PrimaryView.this.model.setActiveTool(SelectedTool.ERASE);
                            PrimaryView.this.model.wipeRecentShape();
                            break;
                        case R.id.circleButton:
                            PrimaryView.this.model.setActiveTool(SelectedTool.CIRCLE);
                            PrimaryView.this.model.wipeRecentShape();
                            break;
                        case R.id.rectangleButton:
                            PrimaryView.this.model.setActiveTool(SelectedTool.RECTANGLE);
                            PrimaryView.this.model.wipeRecentShape();
                            break;
                        case R.id.lineButton:
                            PrimaryView.this.model.setActiveTool(SelectedTool.LINE);
                            PrimaryView.this.model.wipeRecentShape();
                            break;
                    }
                }

                selectionComplete = true; // allowing changes again
            }
        });

        colors.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int selection) {

                boolean toolbarChange = selectionComplete && selection != -1;

                if (toolbarChange) {

                    selectionComplete = false; // preventing other changes

                    boolean selectingShape = PrimaryView.this.model.getActiveTool() == SelectedTool.SELECT;

                    switch (selection) {
                        case R.id.blackColorButton:
                            PrimaryView.this.model.setActiveColor(Color.BLACK);
                            if(selectingShape) { PrimaryView.this.model.setSelectedShapeColor(Color.BLACK); }
                            break;
                        case R.id.redColorButton:
                            PrimaryView.this.model.setActiveColor(Color.RED);
                            if(selectingShape) { PrimaryView.this.model.setSelectedShapeColor(Color.RED); }
                            break;
                        case R.id.greenColorButton:
                            PrimaryView.this.model.setActiveColor(Color.GREEN);
                            if(selectingShape) { PrimaryView.this.model.setSelectedShapeColor(Color.GREEN); }
                            break;
                        case R.id.blueColorButton:
                            PrimaryView.this.model.setActiveColor(Color.BLUE);
                            if(selectingShape) { PrimaryView.this.model.setSelectedShapeColor(Color.BLUE); }
                            break;
                    }
                }

                selectionComplete = true; // allowing changes again

            }
        });
    }


    public class PrimaryCanvas extends View {

        // ----------------------------------------------------
        // ---------------   Variable Declarations   -----------------
        // ----------------------------------------------------
        private Paint paintBrush;
        private ScaleGestureDetector detectScaling;
        final Handler handler = new Handler();
        Runnable hardPress = new Runnable() { public void run() { model.wipeShapes(); } };

        // ----------------------------------------------------
        // ---------------   Constructor   -----------------
        // ----------------------------------------------------
        public PrimaryCanvas(Context context) {
            super(context);
            setFocusableInTouchMode(true);
            setFocusable(true);
            setLayerType(View.LAYER_TYPE_SOFTWARE, null);

            initialize();
            detectScaling = new ScaleGestureDetector(context, new listenForScale());
        }

        // ------------------------------------------------------------------
        // ---------------  Listens for a Scale Op To Occur -----------------
        // ------------------------------------------------------------------
        private class listenForScale extends ScaleGestureDetector.SimpleOnScaleGestureListener {
            @Override
            public boolean onScale(ScaleGestureDetector detector) {
                model.scaleShape(detector.getScaleFactor());
                invalidate();
                return true;
            }
        }

        private void initialize() {
            paintBrush = new Paint();
            paintBrush.setStrokeCap(Paint.Cap.ROUND);
            paintBrush.setStyle(Paint.Style.STROKE);
            paintBrush.setStrokeJoin(Paint.Join.ROUND);
            paintBrush.setAntiAlias(true);
        }

        // ----------------------------------------------------
        // ---------------   View Overrides   -----------------
        // ----------------------------------------------------
        @Override
        protected void onDraw(Canvas canvas) {

            // Primary View Updating

            ArrayList<Shape> shapeList = model.getShapes();
            boolean recentShapeCreated = model.getRecentShapeCreation() != null;

            for (Shape shape: shapeList) {

                SelectedTool thisShapeType = shape.typeOfShape;

                // Setting Paintbrush style for shapes
                paintBrush.setStyle(Paint.Style.FILL);
                paintBrush.setColor(shape.strokeColor);

                switch (thisShapeType){
                    case CIRCLE:
                        paintBrush.setStrokeWidth(0); // allows for shape to be filled
                        canvas.drawOval(shape.xCoord1, shape.yCoord1, shape.xCoord2, shape.yCoord2, paintBrush);
                        break;
                    case RECTANGLE:
                        paintBrush.setStrokeWidth(0); // allows for shape to be filled
                        canvas.drawRect(shape.xCoord1, shape.yCoord1, shape.xCoord2, shape.yCoord2, paintBrush);
                        break;
                    case LINE:
                        paintBrush.setStrokeWidth(20); // makes line larger on canvas
                        canvas.drawLine(shape.xCoord1, shape.yCoord1, shape.xCoord2, shape.yCoord2, paintBrush);
                        break;
                }
            }


            if(recentShapeCreated) {

                paintBrush.setStrokeWidth(0);
                paintBrush.setColor(model.getActiveColor());

                Shape recentShape = model.getRecentShapeCreation();
                SelectedTool activeTool = model.getActiveTool();

                switch (activeTool){
                    case CIRCLE:
                        canvas.drawOval(recentShape.xCoord1, recentShape.yCoord1, recentShape.xCoord2, recentShape.yCoord2, paintBrush);
                        break;
                    case RECTANGLE:
                        canvas.drawRect(recentShape.xCoord1, recentShape.yCoord1, recentShape.xCoord2, recentShape.yCoord2, paintBrush);
                        break;
                    case LINE:
                        canvas.drawLine(recentShape.xCoord1, recentShape.yCoord1, recentShape.xCoord2, recentShape.yCoord2, paintBrush);
                        break;
                }
            }
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {

            detectScaling.onTouchEvent(event); // handles scaling while drawing

            int touchpointX = (int) event.getX();
            int touchpointY = (int) event.getY();

            switch (event.getActionMasked()) {

                // User pressed down with finger
                case MotionEvent.ACTION_DOWN: {

                    startX = touchpointX;
                    startY = touchpointY;

                    SelectedTool tool = model.getActiveTool();

                    switch(tool) {
                        case SELECT:
                            model.selectShape(touchpointX, touchpointY);
                            break;
                        case ERASE:
                            handler.postDelayed(hardPress, 1000);
                            model.eraseShape(touchpointX, touchpointY);
                            break;
                        default: model.drawShape(0,0,0,0);
                            break;
                    }
                    break;
                }

                // User released finger from screen
                case MotionEvent.ACTION_UP: {
                    SelectedTool tool = model.getActiveTool();

                    switch(tool) {
                        case SELECT:
                            break;
                        case ERASE:
                            handler.removeCallbacks(hardPress);
                            break;
                        default:
                            model.addShapeToList();
                            break;
                    }
                    break;
                }

                // User is dragging finger
                case MotionEvent.ACTION_MOVE: {
                    SelectedTool tool = model.getActiveTool();

                    switch(tool) {
                        case SELECT:
                            boolean selectedShape = event.getPointerCount() == 1;
                            if (selectedShape) { model.moveShape(touchpointX, touchpointY); }
                            break;
                        case ERASE:
                            handler.removeCallbacks(hardPress);
                            break;
                        default:
                            model.drawShape(startX, startY, touchpointX, touchpointY);
                        break;
                    }
                    break;
                }

            }
            postInvalidate();
            return true;
        }
    }

    @Override
    public void update(Observable observable, Object data) {
        // updates the toolbar to the active tool

        int activeColor = model.getActiveColor(); // getting color to set radio button

        switch (activeColor) { // setting color in UI

            case Color.BLACK:
                colors.check(R.id.blackColorButton); break;
            case Color.RED:
                colors.check(R.id.redColorButton); break;
            case Color.GREEN:
                colors.check(R.id.greenColorButton); break;
            case Color.BLUE:
                colors.check(R.id.blueColorButton); break;
        }

        SelectedTool tool = model.getActiveTool();

        switch (tool) {
            // Customizing
            case SELECT:
                tools.check(R.id.selectButton); break;
            case ERASE:
                tools.check(R.id.eraseButton); break;
            // Drawing
            case CIRCLE:
                tools.check(R.id.circleButton); break;
            case RECTANGLE:
                tools.check(R.id.rectangleButton); break;
            case LINE:
                tools.check(R.id.lineButton); break;
        }
        canvas.postInvalidate(); // invalidates previous canvas and redraws
    }
}
