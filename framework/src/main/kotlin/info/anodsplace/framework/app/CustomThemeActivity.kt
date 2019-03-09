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

class CustomThemeColor(val available: Boolean, @ColorInt val colorInt: Int, @ColorRes val colorRes: Int) {
    companion object {
        val none = CustomThemeColor(false, 0, 0)
    }

    fun get(activity: Activity): Int {
        if (colorRes > 0) {
            return ContextCompat.getColor(activity, colorRes)
        }
        return colorInt
    }

}

class CustomThemeColors(
        val available: Boolean,
        val statusBarLight: Boolean,
        val statusBarColor: CustomThemeColor,
        val navigationBarColor: CustomThemeColor) : Parcelable {

    constructor(parcel: Parcel) : this(
            parcel.readByte() != 0.toByte(),
            parcel.readByte() != 0.toByte(),
            CustomThemeColor(parcel.readByte() != 0.toByte(), parcel.readInt(), parcel.readInt()),
            CustomThemeColor(parcel.readByte() != 0.toByte(), parcel.readInt(), parcel.readInt())
    )

    constructor(statusBarLight: Boolean,
                @ColorRes statusBarColor: Int,
                @ColorRes navigationBarColor: Int) : this(true, statusBarLight, CustomThemeColor(true, 0, statusBarColor), CustomThemeColor(true, 0, navigationBarColor))

    constructor(@ColorInt statusBarColor: Int,
                navigationBarColor: CustomThemeColor) : this(true, isStatusBarLight(statusBarColor), CustomThemeColor(true, statusBarColor, 0), navigationBarColor)

    companion object {
        val none = CustomThemeColors(false, false, CustomThemeColor.none, CustomThemeColor.none)


        fun isStatusBarLight(@ColorInt statusBarColor: Int): Boolean {
            val r = Color.red(statusBarColor)
            val g = Color.green(statusBarColor)
            val b = Color.blue(statusBarColor)
            val brightness = r * 0.299 + g * 0.587 + b * 0.144;
            return brightness <= 186
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
        parcel.writeByte(if (statusBarLight) 1 else 0)
        parcel.writeInt(if (statusBarColor.available) 1 else 0)
        parcel.writeInt(statusBarColor.colorInt)
        parcel.writeInt(statusBarColor.colorRes)
        parcel.writeInt(if (navigationBarColor.available) 1 else 0)
        parcel.writeInt(navigationBarColor.colorInt)
        parcel.writeInt(navigationBarColor.colorRes)
    }

    override fun describeContents(): Int {
        return 0
    }

}