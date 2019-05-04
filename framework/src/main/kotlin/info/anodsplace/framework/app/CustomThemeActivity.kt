package info.anodsplace.framework.app

import android.app.Activity
import android.graphics.Color
import android.os.Parcel
import android.os.Parcelable
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat

/**
 * @author Alex Gavrishev
 * @date 04/12/2017
 */
interface CustomThemeActivity {
    val themeRes: Int
    val themeColors: CustomThemeColors
}

class CustomThemeColor(
        val available: Boolean,
        @ColorInt val colorInt: Int,
        @ColorRes val colorRes: Int,
        val isLight: Boolean){

    constructor(@ColorInt colorInt: Int,
                @ColorRes colorRes: Int,
                isLight: Boolean) : this(true, colorInt, colorRes, isLight)

    companion object {
        val none = CustomThemeColor(false, 0, 0, false)
        val white = CustomThemeColor(true, Color.WHITE, 0, true)
        val black = CustomThemeColor(true, Color.BLACK, 0, false)
    }

    fun get(activity: Activity): Int {
        if (colorRes != 0) {
            return ContextCompat.getColor(activity, colorRes)
        }
        return colorInt
    }
}

class CustomThemeColors(
        val available: Boolean,
        val statusBarColor: CustomThemeColor,
        val navigationBarColor: CustomThemeColor) : Parcelable {

    constructor(parcel: Parcel) : this(
            parcel.readByte() != 0.toByte(),
            CustomThemeColor(parcel.readByte() != 0.toByte(), parcel.readInt(), parcel.readInt(), parcel.readByte() != 0.toByte()),
            CustomThemeColor(parcel.readByte() != 0.toByte(), parcel.readInt(), parcel.readInt(), parcel.readByte() != 0.toByte())
    )

    constructor(statusBarLight: Boolean,
                @ColorRes statusBarColor: Int,
                isMavBarLight: Boolean,
                @ColorRes navigationBarColor: Int)
            : this(true, CustomThemeColor(true, 0, statusBarColor, statusBarLight), CustomThemeColor(true, 0, navigationBarColor, isMavBarLight))

    constructor(@ColorInt statusBarColor: Int,
                navigationBarColor: CustomThemeColor)
            : this(true, CustomThemeColor(true, statusBarColor, 0, isStatusBarLight(statusBarColor)), navigationBarColor)

    constructor(statusBarColor: CustomThemeColor,
                navigationBarColor: CustomThemeColor)
            : this(true, statusBarColor, navigationBarColor)


    companion object {
        val none = CustomThemeColors(false, CustomThemeColor.none, CustomThemeColor.none)

        fun isStatusBarLight(@ColorInt statusBarColor: Int): Boolean {
            val r = Color.red(statusBarColor)
            val g = Color.green(statusBarColor)
            val b = Color.blue(statusBarColor)
            val brightness = r * 0.299 + g * 0.587 + b * 0.144
            return brightness <= 125
        }

        @JvmField
        val CREATOR = object : Parcelable.Creator<CustomThemeColors> {
            override fun createFromParcel(parcel: Parcel): CustomThemeColors {
                return CustomThemeColors(parcel)
            }
            override fun newArray(size: Int): Array<CustomThemeColors?> {
                return arrayOfNulls(size)
            }
        }
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeByte(if (available) 1 else 0)
        parcel.writeInt(if (statusBarColor.available) 1 else 0)
        parcel.writeInt(statusBarColor.colorInt)
        parcel.writeInt(statusBarColor.colorRes)
        parcel.writeByte(if (statusBarColor.isLight) 1 else 0)
        parcel.writeInt(if (navigationBarColor.available) 1 else 0)
        parcel.writeInt(navigationBarColor.colorInt)
        parcel.writeInt(navigationBarColor.colorRes)
        parcel.writeByte(if (navigationBarColor.isLight) 1 else 0)
    }

    override fun describeContents(): Int {
        return 0
    }

}