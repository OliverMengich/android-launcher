package com.example.android_launcher.data.local

import androidx.room.TypeConverter

class ListTypeConverter {
    @TypeConverter
    fun datesToString(list: List<String>): String{
        return list.joinToString(",") { "[$it]" }
    }

    @TypeConverter
    fun stringToDates(datesString: String): List<String>{
        return Regex("\\[(.*?)]")
            .findAll(datesString)
            .map { it.groupValues[1] }
            .toList()
    }
}