package pt.ulisboa.tecnico.guardianapp.model

import android.os.Parcel
import android.os.Parcelable

data class Fence(var latitude: Double = 0.0, var longitude: Double = 0.0, var radius: Double = 0.0) : Parcelable {

    constructor(parcel: Parcel) : this() {
        latitude = parcel.readDouble()
        longitude = parcel.readDouble()
        radius = parcel.readDouble()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeDouble(latitude)
        parcel.writeDouble(longitude)
        parcel.writeDouble(radius)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Fence> {
        override fun createFromParcel(parcel: Parcel): Fence {
            return Fence(parcel)
        }

        override fun newArray(size: Int): Array<Fence?> {
            return arrayOfNulls(size)
        }
    }
}