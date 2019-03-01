package info.anodsplace.framework.app

import android.os.Parcel
import android.os.Parcelable
import androidx.annotation.ColorRes

/**
 * @author Alex Gavrishev
 * @date 04/12/2017
 */
interface CustomThemeActivity {
    val themeRes: Int
    val themeColors: CustomThemeColors
}

class CustomThemeColors(
        val available: Boolean,
        val statusBarLight: Boolean,
        @ColorRes val statusBarColor: Int,
        @ColorRes val navigationBarColor: Int) : Parcelable {

    constructor(parcel: Parcel) : this(
            parcel.readByte() != 0.toByte(),
            parcel.readByte() != 0.toByte(),
            parcel.readInt(),
            parcel.readInt())

    constructor(statusBarLight: Boolean,
                @ColorRes statusBarColor: Int,
                @ColorRes navigationBarColor: Int) : this(true, statusBarLight, statusBarColor, navigationBarColor)

    companion object {
        val none = CustomThemeColors(false, false, 0, 0)
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
        parcel.writeInt(statusBarColor)
        parcel.writeInt(navigationBarColor)
    }

    override fun describeContents(): Int {
        return 0
    }

}