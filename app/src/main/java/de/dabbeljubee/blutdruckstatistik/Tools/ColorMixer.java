package de.dabbeljubee.blutdruckstatistik.Tools;

import android.graphics.Color;

public class ColorMixer {

    public static int determineColor(int value, int lowerLevel, int upperLevel, int upper, int lowerColor) {
        final float upperPart = (float) (value - lowerLevel) / (upperLevel - lowerLevel);
        final float lowerPart = 1 - upperPart;
        return Color.argb(0xFF,
                (int) (Color.red(upper) * upperPart + Color.red(lowerColor) * lowerPart),
                (int) (Color.green(upper) * upperPart + Color.green(lowerColor) * lowerPart),
                (int) (Color.blue(upper) * upperPart + Color.blue(lowerColor) * lowerPart));
    }
}
