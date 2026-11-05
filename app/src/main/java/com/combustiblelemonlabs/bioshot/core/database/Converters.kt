package com.combustiblelemonlabs.bioshot.core.database

import androidx.room.TypeConverter
import com.combustiblelemonlabs.bioshot.core.model.DeviceType
import com.combustiblelemonlabs.bioshot.core.model.InjectionSite
import com.combustiblelemonlabs.bioshot.core.model.ScheduleType
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

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

    /**
     * Single converter pair shared by any `List<String>` Room field (injection_logs.side_effects
     * and symptom_entries.tags). Room matches converters by input/output type pair.
     */
    @TypeConverter
    fun fromSideEffects(list: List<String>): String = Json.encodeToString(list)

    @TypeConverter
    fun toSideEffects(json: String): List<String> = Json.decodeFromString(json)
}
