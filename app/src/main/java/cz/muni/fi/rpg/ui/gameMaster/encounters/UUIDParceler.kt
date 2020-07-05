package cz.muni.fi.rpg.ui.gameMaster.encounters

import android.os.Parcel
import kotlinx.android.parcel.Parceler
import java.util.*

class UUIDParceler : Parceler<UUID> {
    override fun create(parcel: Parcel): UUID {
        val mostSigBits = parcel.readLong()
        val leastSigBits = parcel.readLong()
        return UUID(mostSigBits, leastSigBits)
    }

    override fun UUID.write(parcel: Parcel, flags: Int) {
        parcel.writeLong(mostSignificantBits)
        parcel.writeLong(leastSignificantBits)
    }
}