package com.example.android_launcher.data.local

import androidx.room.TypeConverter

class CommaListConverter {
    @TypeConverter
    fun datesToString(list: List<String>?): String{
        return list?.joinToString(",")?:""
    }
    @TypeConverter
    fun toStringList(data: String?): List<String> {
        return data?.split(",")?.filter { it.isNotBlank() } ?: emptyList()
    }
}