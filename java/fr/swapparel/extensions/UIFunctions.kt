package fr.swapparel.extensions

import android.graphics.Color
import info.hoang8f.widget.FButton

class UIFunctions {
    fun makeBlue(b: FButton) {
        b.buttonColor = Color.parseColor("#19B5FE")
    }

    fun makeGrey(b: FButton) {
        b.buttonColor = Color.parseColor("#ECECEC")
    }
}