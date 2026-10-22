package com.fkurevija.bioshot.core.database

import androidx.room.TypeConverter
import com.fkurevija.bioshot.core.model.DeviceType
import com.fkurevija.bioshot.core.model.InjectionSite
import com.fkurevija.bioshot.core.model.ScheduleType

class Converters {
    @TypeConverter
    fun fromInjectionSite(value: InjectionSite): String = value.name

    @TypeConverter
    fun toInjectionSite(value: String): InjectionSite = InjectionSite.valueOf(value)

    @TypeConverter
    fun fromDeviceType(value: DeviceType): String = value.name

    @TypeConverter
    fun toDeviceType(value: String): DeviceType = DeviceType.valueOf(value)

    @TypeConverter
    fun fromScheduleType(value: ScheduleType): String = value.name

    @TypeConverter
    fun toScheduleType(value: String): ScheduleType = ScheduleType.valueOf(value)
}
