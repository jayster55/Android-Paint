package jsketch;

import android.os.Bundle;
import android.view.ViewGroup;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;


public class MainActivity extends AppCompatActivity {

    // -----------------------------------------------------------
    // ---------------   Variable Declarations   -----------------
    // -----------------------------------------------------------
    Model model;

    // ----------------------------------------------------------------------------
    // ---------------   Override Methods for AppCompatActivity   -----------------
    // ----------------------------------------------------------------------------
    @Override
    protected void onCreate(Bundle savedState) {
        super.onCreate(savedState);
        model = new Model();
        setContentView(R.layout.mainactivity);
    }

    @Override
    protected void onPostCreate(Bundle savedState) {

        super.onPostCreate(savedState);

        PrimaryView view = new PrimaryView(this, model);
        ViewGroup viewgroup = findViewById(R.id.mainActivity);
        viewgroup.addView(view);

        model.update();
    }

    // --------------------------------------------------------------
    // ---------------   Saving Application State   -----------------
    // --------------------------------------------------------------
    @Override
    protected void onSaveInstanceState(Bundle savedState) {

        savedState.putParcelableArrayList("Shapes", model.getShapes());
        savedState.putInt("Color", model.getActiveColor());

        SelectedTool choice = model.getActiveTool();

        switch (choice) {
            case SELECT:
                savedState.putInt("SelectedTool", 0); break;
            case ERASE:
                savedState.putInt("SelectedTool", 1); break;
            case CIRCLE:
                savedState.putInt("SelectedTool", 2); break;
            case RECTANGLE:
                savedState.putInt("SelectedTool", 3); break;
            case LINE:
                savedState.putInt("SelectedTool", 4); break;
        }

        super.onSaveInstanceState(savedState);
    }

    // -----------------------------------------------------------------
    // ---------------   Restoring Application State   -----------------
    // -----------------------------------------------------------------
    @Override
    protected void onRestoreInstanceState(Bundle savedState) {
        super.onRestoreInstanceState(savedState);

        ArrayList<Shape> list = savedState.getParcelableArrayList("Shapes");
        model.loadShapes(list);
        model.setActiveColor(savedState.getInt("Color"));

        int selectedTool = savedState.getInt("SelectedTool");

        switch (selectedTool) {
            case 0:
                model.setActiveTool(SelectedTool.SELECT); break;
            case 1:
                model.setActiveTool(SelectedTool.ERASE); break;
            case 2:
                model.setActiveTool(SelectedTool.CIRCLE); break;
            case 3:
                model.setActiveTool(SelectedTool.RECTANGLE); break;
            case 4:
                model.setActiveTool(SelectedTool.LINE); break;
        }

    }
}
