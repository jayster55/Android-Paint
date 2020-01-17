package jsketch;

import android.os.Parcel;
import android.os.Parcelable;

class Shape implements Parcelable {

    // -----------------------------------------------------------
    // ---------------   Variable Declarations   -----------------
    // -----------------------------------------------------------
    SelectedTool typeOfShape;
    int strokeColor;
    float xCoord1, yCoord1, xCoord2, yCoord2;

    Shape(SelectedTool shapeSelected, int xStart, int yStart, int xStop, int yStop, int color) {

        typeOfShape = shapeSelected;
        strokeColor = color;

        xCoord1 = xStart;
        xCoord2 = xStop;

        yCoord1 = yStart;
        yCoord2 = yStop;

    }

    protected Shape(Parcel in) {

        typeOfShape = SelectedTool.valueOf(in.readString());
        strokeColor = in.readInt();

        xCoord1 = in.readInt();
        xCoord2 = in.readInt();

        yCoord1 = in.readInt();
        yCoord2 = in.readInt();
    }

    // ----------------------------------------------------
    // ---------------   Overrides   -----------------
    // ----------------------------------------------------
    @Override
    public void writeToParcel(Parcel out, int flags) {

        // outputs to cache before app closes
        out.writeString(typeOfShape.toString());
        out.writeInt(strokeColor);

        out.writeFloat(xCoord1);
        out.writeFloat(xCoord2);

        out.writeFloat(yCoord1);
        out.writeFloat(yCoord2);

    }


    public static final Creator<Shape> CREATOR = new Creator<Shape>() { // creates shapes on app launch

        // reads input from cache

        @Override
        public Shape createFromParcel(Parcel in) { return new Shape(in); } // creates shape from Parcel on return

        @Override
        public Shape[] newArray(int size) { return new Shape[size]; } // initialize size of cached shapes list
    };

    @Override
    public int describeContents() { return 0; } // required

}



