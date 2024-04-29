package com.kamesh.weatherapplication.Database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.provider.BaseColumns

class WeatherDatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_VERSION = 1
        private const val DATABASE_NAME = "weather_database.db"
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(WeatherContract.SQL_CREATE_ENTRIES)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL(WeatherContract.SQL_DELETE_ENTRIES)
        onCreate(db)
    }
    fun addWeather(weatherData: WeatherData): Long {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(WeatherContract.WeatherEntry.COLUMN_NAME_CITY, weatherData.city)
            put(WeatherContract.WeatherEntry.COLUMN_NAME_LATITUDE, weatherData.latitude)
            put(WeatherContract.WeatherEntry.COLUMN_NAME_LONGITUDE, weatherData.longitude)
            put(WeatherContract.WeatherEntry.COLUMN_NAME_CURRENT_TEMP, weatherData.currentTemperature)
            put(WeatherContract.WeatherEntry.COLUMN_NAME_MAX_TEMP, weatherData.maxTemperature)
            put(WeatherContract.WeatherEntry.COLUMN_NAME_MIN_TEMP, weatherData.minTemperature)
            put(WeatherContract.WeatherEntry.COLUMN_NAME_PRESSURE, weatherData.pressure)
            put(WeatherContract.WeatherEntry.COLUMN_NAME_HUMIDITY, weatherData.humidity)
            put(WeatherContract.WeatherEntry.COLUMN_NAME_WIND_SPEED, weatherData.windSpeed)
        }
        return db.insert(WeatherContract.WeatherEntry.TABLE_NAME, null, values)
    }

    fun getWeatherDataForCity(cityName: String): WeatherData? {
        val db = this.readableDatabase
        val projection = arrayOf(
            WeatherContract.WeatherEntry.COLUMN_NAME_CITY,
            WeatherContract.WeatherEntry.COLUMN_NAME_LATITUDE,
            WeatherContract.WeatherEntry.COLUMN_NAME_LONGITUDE,
            WeatherContract.WeatherEntry.COLUMN_NAME_CURRENT_TEMP,
            WeatherContract.WeatherEntry.COLUMN_NAME_MAX_TEMP,
            WeatherContract.WeatherEntry.COLUMN_NAME_MIN_TEMP,
            WeatherContract.WeatherEntry.COLUMN_NAME_PRESSURE,
            WeatherContract.WeatherEntry.COLUMN_NAME_HUMIDITY,
            WeatherContract.WeatherEntry.COLUMN_NAME_WIND_SPEED
        )

        val selection = "${WeatherContract.WeatherEntry.COLUMN_NAME_CITY} = ?"
        val selectionArgs = arrayOf(cityName)

        val cursor = db.query(
            WeatherContract.WeatherEntry.TABLE_NAME,  // The table to query
            projection,                                 // The array of columns to return (pass null to get all)
            selection,                                  // The columns for the WHERE clause
            selectionArgs,                              // The values for the WHERE clause
            null,                                       // don't group the rows
            null,                                       // don't filter by row groups
            null                                        // The sort order
        )

        var weatherData: WeatherData? = null
        cursor.use { c ->
            if (c != null && c.moveToFirst()) {
                weatherData = WeatherData(
                    id = null, // Setting id to null since it's not being retrieved from the database query
                    city = c.getString(c.getColumnIndexOrThrow(WeatherContract.WeatherEntry.COLUMN_NAME_CITY)),
                    latitude = c.getDouble(c.getColumnIndexOrThrow(WeatherContract.WeatherEntry.COLUMN_NAME_LATITUDE)),
                    longitude = c.getDouble(c.getColumnIndexOrThrow(WeatherContract.WeatherEntry.COLUMN_NAME_LONGITUDE)),
                    currentTemperature = c.getDouble(c.getColumnIndexOrThrow(WeatherContract.WeatherEntry.COLUMN_NAME_CURRENT_TEMP)),
                    maxTemperature = c.getDouble(c.getColumnIndexOrThrow(WeatherContract.WeatherEntry.COLUMN_NAME_MAX_TEMP)),
                    minTemperature = c.getDouble(c.getColumnIndexOrThrow(WeatherContract.WeatherEntry.COLUMN_NAME_MIN_TEMP)),
                    pressure = c.getInt(c.getColumnIndexOrThrow(WeatherContract.WeatherEntry.COLUMN_NAME_PRESSURE)),
                    humidity = c.getInt(c.getColumnIndexOrThrow(WeatherContract.WeatherEntry.COLUMN_NAME_HUMIDITY)),
                    windSpeed = c.getDouble(c.getColumnIndexOrThrow(WeatherContract.WeatherEntry.COLUMN_NAME_WIND_SPEED))
                )
            }
        }
        return weatherData
    }
    fun printAllWeatherData() {
        val db = this.readableDatabase

        val projection = arrayOf(
            WeatherContract.WeatherEntry.COLUMN_NAME_CITY,
            WeatherContract.WeatherEntry.COLUMN_NAME_LATITUDE,
            WeatherContract.WeatherEntry.COLUMN_NAME_LONGITUDE,
            WeatherContract.WeatherEntry.COLUMN_NAME_CURRENT_TEMP,
            WeatherContract.WeatherEntry.COLUMN_NAME_MAX_TEMP,
            WeatherContract.WeatherEntry.COLUMN_NAME_MIN_TEMP,
            WeatherContract.WeatherEntry.COLUMN_NAME_PRESSURE,
            WeatherContract.WeatherEntry.COLUMN_NAME_HUMIDITY,
            WeatherContract.WeatherEntry.COLUMN_NAME_WIND_SPEED
        )

        val cursor = db.query(
            WeatherContract.WeatherEntry.TABLE_NAME,
            projection,
            null,
            null,
            null,
            null,
            null
        )

        cursor.use { c ->
            while (c.moveToNext()) {
                val city = c.getString(c.getColumnIndexOrThrow(WeatherContract.WeatherEntry.COLUMN_NAME_CITY))
                val latitude = c.getDouble(c.getColumnIndexOrThrow(WeatherContract.WeatherEntry.COLUMN_NAME_LATITUDE))
                val longitude = c.getDouble(c.getColumnIndexOrThrow(WeatherContract.WeatherEntry.COLUMN_NAME_LONGITUDE))
                val currentTemp = c.getDouble(c.getColumnIndexOrThrow(WeatherContract.WeatherEntry.COLUMN_NAME_CURRENT_TEMP))
                val maxTemp = c.getDouble(c.getColumnIndexOrThrow(WeatherContract.WeatherEntry.COLUMN_NAME_MAX_TEMP))
                val minTemp = c.getDouble(c.getColumnIndexOrThrow(WeatherContract.WeatherEntry.COLUMN_NAME_MIN_TEMP))
                val pressure = c.getInt(c.getColumnIndexOrThrow(WeatherContract.WeatherEntry.COLUMN_NAME_PRESSURE))
                val humidity = c.getInt(c.getColumnIndexOrThrow(WeatherContract.WeatherEntry.COLUMN_NAME_HUMIDITY))
                val windSpeed = c.getDouble(c.getColumnIndexOrThrow(WeatherContract.WeatherEntry.COLUMN_NAME_WIND_SPEED))

                println("City: $city, Latitude: $latitude, Longitude: $longitude, " +
                        "Current Temp: $currentTemp, Max Temp: $maxTemp, Min Temp: $minTemp, " +
                        "Pressure: $pressure, Humidity: $humidity, Wind Speed: $windSpeed")
            }
        }
    }



}

object WeatherContract {
    // Define table contents
    class WeatherEntry : BaseColumns {
        companion object {
            const val TABLE_NAME = "weather"
            const val COLUMN_NAME_CITY = "city"
            const val COLUMN_NAME_LATITUDE = "latitude"
            const val COLUMN_NAME_LONGITUDE = "longitude"
            const val COLUMN_NAME_CURRENT_TEMP = "current_temperature"
            const val COLUMN_NAME_MAX_TEMP = "max_temperature"
            const val COLUMN_NAME_MIN_TEMP = "min_temperature"
            const val COLUMN_NAME_PRESSURE = "pressure"
            const val COLUMN_NAME_HUMIDITY = "humidity"
            const val COLUMN_NAME_WIND_SPEED = "wind_speed"
            // Add more columns as needed
        }

    }

    const val SQL_CREATE_ENTRIES =
        "CREATE TABLE ${WeatherEntry.TABLE_NAME} (" +
                "${BaseColumns._ID} INTEGER PRIMARY KEY," +
                "${WeatherEntry.COLUMN_NAME_CITY} TEXT," +
                "${WeatherEntry.COLUMN_NAME_LATITUDE} REAL," +
                "${WeatherEntry.COLUMN_NAME_LONGITUDE} REAL," +
                "${WeatherEntry.COLUMN_NAME_CURRENT_TEMP} REAL," +
                "${WeatherEntry.COLUMN_NAME_MAX_TEMP} REAL," +
                "${WeatherEntry.COLUMN_NAME_MIN_TEMP} REAL," +
                "${WeatherEntry.COLUMN_NAME_PRESSURE} REAL," +
                "${WeatherEntry.COLUMN_NAME_HUMIDITY} INTEGER," +
                "${WeatherEntry.COLUMN_NAME_WIND_SPEED} REAL)"


    const val SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS ${WeatherEntry.TABLE_NAME}"
}



