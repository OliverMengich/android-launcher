package com.example.android_launcher.data.local

import androidx.room.TypeConverter

//@ProvidedTypeConverter
class ListTypeConverter {
    @TypeConverter
    fun datesToString(list: List<String>): String{
        return list.joinToString(",") { "[$it]" }
//        return dates.joinToString(separator = ",")
    }

    @TypeConverter
    fun stringToDates(datesString: String): List<String>{
        return Regex("\\[(.*?)]")
            .findAll(datesString)
            .map { it.groupValues[1] }
            .toList()
//        return if (datesString.isEmpty()) emptyList() else datesString.split(",").toList()
    }
}