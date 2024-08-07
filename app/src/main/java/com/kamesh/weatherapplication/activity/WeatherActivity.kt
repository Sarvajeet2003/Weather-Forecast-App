@file:Suppress("DEPRECATION")

package com.kamesh.weatherapplication.activity

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.os.Looper.myLooper
import android.provider.Settings
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kamesh.weatherapplication.model.current.WeatherModel
import com.kamesh.weatherapplication.model.forecast.WeatherDataModel
import com.kamesh.weatherapplication.model.forecast.WeatherParentModel
import com.google.android.gms.location.*
import com.kamesh.weatherapplication.Database.WeatherData
import com.kamesh.weatherapplication.Database.WeatherDatabaseHelper
import com.kamesh.weatherapplication.Notifiaction.WeatherNotification
import com.kamesh.weatherapplication.R
import com.kamesh.weatherapplication.adapter.WeatherForecastAdapter
import com.kamesh.weatherapplication.api.WeatherApiServices
import com.kamesh.weatherapplication.databinding.ActivityWeatherBinding
import com.kamesh.weatherapplication.model.current.Main
import com.kamesh.weatherapplication.model.current.Sys
import com.kamesh.weatherapplication.model.current.Wind
import com.kamesh.weatherapplication.model.forecast.WeatherValueModel
import kotlinx.android.synthetic.main.activity_weather.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.math.RoundingMode
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*


class WeatherActivity : AppCompatActivity() {
    private lateinit var binding: ActivityWeatherBinding
    private lateinit var recyclerView: RecyclerView
    private lateinit var forecastList: ArrayList<WeatherDataModel>
    private lateinit var mFusedLocation: FusedLocationProviderClient
    private lateinit var tvLatitude: TextView
    private lateinit var tvLongitude:TextView
    private lateinit var date1: String
    private lateinit var date2: String
    private lateinit var date3: String
    private lateinit var date4: String
    private lateinit var date5: String
    private lateinit var date6: String
    private lateinit var date7: String


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWeatherBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()
        mFusedLocation = LocationServices.getFusedLocationProviderClient(this)
        binding.weatherPage.visibility = View.GONE
        tvLatitude = findViewById(R.id.tvLatitude)
        tvLongitude = findViewById(R.id.tvLongitude)

        val dbHelper = WeatherDatabaseHelper(this)
        dbHelper.printAllWeatherData()


        getLocation()
        dateStamp()
        weatherSearch()
        refreshPage()
        setClickListeners()
    }
    fun onViewGraphButtonClicked(view: View) {
        val temperatureData = forecastList.map { it.main.temp }
        startGraphActivity(temperatureData)
    }


    private fun setClickListeners(){
        binding.Btn1.setOnClickListener { filterWeatherOnDate(date1, binding.Btn1) }
        binding.Btn2.setOnClickListener { filterWeatherOnDate(date2, binding.Btn2) }
        binding.Btn3.setOnClickListener { filterWeatherOnDate(date3, binding.Btn3) }
        binding.Btn4.setOnClickListener { filterWeatherOnDate(date4, binding.Btn4) }
        binding.Btn5.setOnClickListener { filterWeatherOnDate(date5, binding.Btn5) }
        binding.Btn6.setOnClickListener { filterWeatherOnDate(date6, binding.Btn6) }
        binding.Btn7.setOnClickListener { filterWeatherOnDate(date7, binding.Btn7) }
    }
    // In WeatherActivity
    private fun startGraphActivity(temperatureData: List<Double>) {
        val intent = Intent(this, GraphActivity::class.java)
        // Convert temperatures from Kelvin to Celsius
        val celsiusTemperatures = temperatureData.map { kelvinToCelsiusArray(it) }
        println("Temperature Data in Celsius:")
        println(celsiusTemperatures)
        intent.putExtra("temperatureData", celsiusTemperatures.toDoubleArray())
        startActivity(intent)
    }

    private fun kelvinToCelsiusArray(kelvinTemperature: Double): Double {
        return kelvinTemperature - 273.15
    }



    @SuppressLint("UseCompatLoadingForDrawables")
    private fun filterWeatherOnDate(date: String, dateLabel: TextView) {
        val filteredList = forecastList.filter { it.dt_txt.split(" ")[0] == date }
        recyclerView = findViewById(R.id.forecastRecyclerList)
        recyclerView.layoutManager = LinearLayoutManager(this@WeatherActivity,RecyclerView.HORIZONTAL,false)
        recyclerView.setHasFixedSize(true)
        recyclerView.adapter = WeatherForecastAdapter(filteredList as ArrayList<WeatherDataModel>,this@WeatherActivity)
        binding.Btn1.background = getDrawable(R.drawable.chart_round_view_gray1)
        binding.Btn2.background = getDrawable(R.drawable.chart_round_view_gray1)
        binding.Btn3.background = getDrawable(R.drawable.chart_round_view_gray1)
        binding.Btn4.background = getDrawable(R.drawable.chart_round_view_gray1)
        binding.Btn5.background = getDrawable(R.drawable.chart_round_view_gray1)
        binding.Btn6.background = getDrawable(R.drawable.chart_round_view_gray1)
        binding.Btn7.background = getDrawable(R.drawable.chart_round_view_gray1)
        dateLabel.background = getDrawable(R.drawable.chart_round_view)
    }

    private fun refreshPage() {
        swipe_refresh_layout.setOnRefreshListener{
            weatherPage.visibility = View.GONE
            getLocation()
            binding.errortext.visibility = View.GONE
            swipe_refresh_layout.isRefreshing = false
        }
    }

    private fun weatherSearch() {
        binding.weatherSearch.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {

                getCityWeather(binding.weatherSearch.text.toString())
                getForecastCityWeather(binding.weatherSearch.text.toString())
                val view = this.currentFocus
                if (view != null) {
                    val imm: InputMethodManager =
                        getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(view.windowToken, 0)
                    binding.weatherSearch.clearFocus()
                }
                true
            } else false
        }
    }



    @SuppressLint("WeekBasedYear")
    @RequiresApi(Build.VERSION_CODES.O)
    private fun dateStamp() {

        val day1 = LocalDateTime.now()
        val day2 = LocalDateTime.now().plusDays(1)
        val day3 = LocalDateTime.now().plusDays(2)
        val day4 = LocalDateTime.now().plusDays(3)
        val day5 = LocalDateTime.now().plusDays(4)
        val day6 = LocalDateTime.now().plusDays(5)
        val day7 = LocalDateTime.now().plusDays(6)

        val sdf = DateTimeFormatter.ofPattern("MMM d")
        val formatter = DateTimeFormatter.ofPattern("YYYY-MM-dd")

        val month1 = day1.format(sdf)
        val month2 = day2.format(sdf)
        val month3 = day3.format(sdf)
        val month4 = day4.format(sdf)
        val month5 = day5.format(sdf)
        val month6 = day6.format(sdf)
        val month7 = day7.format(sdf)

        date1 = day1.format(formatter)
        date2 = day2.format(formatter)
        date3 = day3.format(formatter)
        date4 = day4.format(formatter)
        date5 = day5.format(formatter)
        date6 = day6.format(formatter)
        date7 = day7.format(formatter)

        binding.Btn1.text = StringBuilder().append(month1)
        binding.Btn2.text = StringBuilder().append(month2)
        binding.Btn3.text = StringBuilder().append(month3)
        binding.Btn4.text = StringBuilder().append(month4)
        binding.Btn5.text = StringBuilder().append(month5)
        binding.Btn6.text = StringBuilder().append(month6)
        binding.Btn7.text = StringBuilder().append(month7)

    }

    private fun getForecastCityWeather(cityName: String){
        binding.progressBar.visibility = View.VISIBLE
        WeatherApiServices.getApiInterface()?.getForecastWeatherData(cityName, API_KEY)?.enqueue(object :Callback<WeatherParentModel>{
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onResponse(call: Call<WeatherParentModel>, response: Response<WeatherParentModel>) {
                if(response.isSuccessful){
                    forecastList = response.body()?.list as ArrayList<WeatherDataModel>
                    filterWeatherOnDate(date1, binding.Btn1)
                    binding.errortext.visibility = View.GONE
                }else{
                    binding.weatherPage.visibility = View.GONE
                    binding.progressBar.visibility = View.GONE
                    binding.errortext.visibility = View.VISIBLE
                }

            }

            override fun onFailure(call: Call<WeatherParentModel>, t: Throwable) {

                Toast.makeText(applicationContext,"Please Enter the Valid Location Name",Toast.LENGTH_LONG).show()
            }

        })
    }

    fun isInternetConnected(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork ?: return false
            val networkCapabilities = connectivityManager.getNetworkCapabilities(network) ?: return false

            return networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        } else {
            val networkInfo = connectivityManager.activeNetworkInfo
            return networkInfo != null && networkInfo.isConnected
        }
    }
    private fun getCityWeather(cityName: String) {
        if (isInternetConnected(this)) {
            binding.progressBar.visibility = View.VISIBLE
            WeatherApiServices.getApiInterface()?.getWeatherData(cityName, API_KEY)?.enqueue(object : Callback<WeatherModel> {
                @RequiresApi(Build.VERSION_CODES.O)
                override fun onResponse(call: Call<WeatherModel>, response: Response<WeatherModel>) {
                    if (response.isSuccessful) {
                        val weather = response.body()?.main?.temp
                        setDataOnViews(response.body())
                        binding.errortext.visibility = View.GONE
                        println(weather)
                        WeatherNotification.showNotification(this@WeatherActivity, weather)
                    } else {
                        binding.weatherPage.visibility = View.GONE
                        binding.progressBar.visibility = View.GONE
                        binding.errortext.visibility = View.VISIBLE
                    }
                }

                override fun onFailure(call: Call<WeatherModel>, t: Throwable) {
                    Toast.makeText(applicationContext, "Please Enter the Valid Location Name", Toast.LENGTH_LONG).show()
                }
            })
        } else {
            fetchWeatherDataFromDatabase(cityName)
        }
    }

    private fun fetchWeatherDataFromDatabase(cityName: String) {
        val dbHelper = WeatherDatabaseHelper(this)
        val weatherData = dbHelper.getWeatherDataForCity(cityName)

        if (weatherData != null) {
            // Convert WeatherData to WeatherModel
            val weatherModel = WeatherModel(
                name = weatherData.city,
                sys = Sys(country = null,id = null, sunrise = null, sunset = null, type = null), // Provide necessary fields as per WeatherModel
                main = Main(
                    temp = weatherData.currentTemperature,
                    tempMax = weatherData.maxTemperature,
                    tempMin = weatherData.minTemperature,
                    pressure = weatherData.pressure,
                    humidity = weatherData.humidity,
                    feelsLike = null
                ),
                wind = Wind(speed = weatherData.windSpeed,deg = null),
                weather = listOf(WeatherValueModel(description = null, icon = null,id = null,main = null)) ,
                clouds = null,
                cod = null,
                coord = null,
                dt = null,
                id = null,
                timezone = null,
                visibility = null,
                base = null

            )
            // Show notification with current temperature
            WeatherNotification.showNotification(this, weatherData.currentTemperature)

            // Display WeatherModel data on the UI
            setDataOnViews(weatherModel)
            Toast.makeText(applicationContext, "Showing cached data", Toast.LENGTH_SHORT).show()
        } else {
            // If no data exists in the database, show a message indicating no data
            Toast.makeText(applicationContext, "No cached data available", Toast.LENGTH_SHORT).show()
        }
    }


    private fun getLocation() {
        if (checkThePermission()){
            if (locationEnabled()){
                if (ActivityCompat.checkSelfPermission(
                        this,
                        android.Manifest.permission.ACCESS_FINE_LOCATION
                    )!=PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                        this,
                        android.Manifest.permission.ACCESS_COARSE_LOCATION
                    )!= PackageManager.PERMISSION_GRANTED
                ){
                    requestPermission()
                    return
                }
                binding.progressBar.visibility = View.VISIBLE
                mFusedLocation.lastLocation
                    .addOnCompleteListener{task->
                        val location: Location? =task.result
                        if (location == null){
                            newLocation()
                        }else{
                            fetchCurrentLocationWeather(location.latitude.toString(),location.longitude.toString())
                            fetchForecastWeather(location.latitude.toString(),location.longitude.toString())
                            binding.progressBar.visibility = View.GONE
                        }
                    }
            }else{
                binding.progressBar.visibility = View.GONE
                Toast.makeText(this,"Please Turn On Your Location",Toast.LENGTH_LONG).show()
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
            }
        }else{
            requestPermission()
        }
    }

    private fun fetchCurrentLocationWeather(latitude: String,longitude: String) {
        binding.progressBar.visibility = View.VISIBLE
        WeatherApiServices.getApiInterface()?.getCurrentWeatherData(latitude, longitude, API_KEY)?.enqueue(object :
            Callback<WeatherModel>{
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onResponse(
                call: Call<WeatherModel>,
                response: Response<WeatherModel>
            ) {
                if (response.isSuccessful){
                    setDataOnViews(response.body())


                }
            }
            override fun onFailure(call: Call<WeatherModel>, t: Throwable) {
                Toast.makeText(applicationContext,"Error Getting Current Weather", Toast.LENGTH_SHORT).show()
            }

        })
    }

    private fun fetchForecastWeather(latitude: String,longitude: String){
        WeatherApiServices.getApiInterface()?.getCurrentForecastWeatherData(latitude, longitude, API_KEY)?.enqueue(object :
            Callback<WeatherParentModel>{
            override fun onResponse(
                call: Call<WeatherParentModel>,
                response: Response<WeatherParentModel>
            ) {
                if (response.isSuccessful ){
                    forecastList = response.body()?.list as ArrayList<WeatherDataModel>
                    filterWeatherOnDate(date1, binding.Btn1)
                }
            }
            override fun onFailure(call: Call<WeatherParentModel>, t: Throwable) {
                Toast.makeText(applicationContext,"Error Getting Forecast Weather", Toast.LENGTH_SHORT).show()
            }
        })
    }
    @SuppressLint("SetTextI18n", "SimpleDateFormat")
    private fun setDataOnViews(body: WeatherModel?) {
        val sdf = SimpleDateFormat("dd/MM/yyyy hh:mm")
        val currentDate = sdf.format(Date())
        binding.weatherPage.visibility = View.VISIBLE
        binding.weatherDayTextInWeatherFragment.text = currentDate
        binding.tvLatitude.text = body?.coord?.lat.toString()
        binding.tvLongitude.text = body?.coord?.lon.toString()
        binding.weatherLocationCityText.text = body?.name.toString()
        binding.weatherLocationRegionText.text = body?.sys?.country.toString()
        binding.Day.text ="↑"+kelvinToCelsius(body!!.main.tempMax) + "°"
        binding.Night.text ="↓"+kelvinToCelsius(body.main.tempMax) + "°"
        binding.degreeText.text =""+kelvinToCelsius(body.main.temp) + "°c"
        binding.degreeDescription.text =body.weather[0].description
        binding.miniStatusFeelsLikeCount.text = ""+body.main.pressure.toString()+" hPa"
        binding.miniStatusHumidityCount.text =body.main.humidity.toString()+"%"
        binding.miniStatusWindCount.text = body.wind.speed.toString()+"m/s"
        body.weather[0].icon?.let { updateUI(it) }

        val weatherData = WeatherData(
            null,
            body?.name ?: "",
            body?.coord?.lat ?: 0.0,
            body?.coord?.lon ?: 0.0,
            body?.main?.temp ?: 0.0,
            body?.main?.tempMax ?: 0.0,
            body?.main?.tempMin ?: 0.0,
            body?.main?.pressure ?: 0,
            body?.main?.humidity ?: 0,
            body?.wind?.speed ?: 0.0
        )

        // Insert the weather data into the database
        val dbHelper = WeatherDatabaseHelper(this)
        val newRowId = dbHelper.addWeather(weatherData)
        if (newRowId != -1L) {
            Toast.makeText(applicationContext, "Weather data added to database", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(applicationContext, "Failed to add weather data to database", Toast.LENGTH_SHORT).show()
        }

    }

    private fun updateUI(icon: String) {
        when (icon) {
            "11d" -> {
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
                window.statusBarColor = resources.getColor(R.color.thunderstorm)
                binding.swipeRefreshLayout.background = ContextCompat.getDrawable(
                    this@WeatherActivity,
                    R.drawable.thunderstorm_bg
                )
                binding.weatherForecastImg.setImageResource(R.drawable.thunderstorm)

            }
            "11n" -> {
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
                window.statusBarColor = resources.getColor(R.color.thunderstorm)
                binding.swipeRefreshLayout.background = ContextCompat.getDrawable(
                    this@WeatherActivity,
                    R.drawable.thunderstorm_bg
                )
                binding.weatherForecastImg.setImageResource(R.drawable.thunderstorm)

            }
            "09d" -> {
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
                window.statusBarColor = resources.getColor(R.color.drizzle)
                binding.swipeRefreshLayout.background = ContextCompat.getDrawable(
                    this@WeatherActivity,
                    R.drawable.drizzle_bg
                )
                binding.weatherForecastImg.setImageResource(R.drawable.cloud_rain_night)

            }
            "09n" -> {
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
                window.statusBarColor = resources.getColor(R.color.drizzle)
                binding.swipeRefreshLayout.background = ContextCompat.getDrawable(
                    this@WeatherActivity,
                    R.drawable.drizzle_bg
                )
                binding.weatherForecastImg.setImageResource(R.drawable.cloud_rain_night)

            }
            "10d" -> {
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
                window.statusBarColor = resources.getColor(R.color.rain)
                binding.swipeRefreshLayout.background = ContextCompat.getDrawable(
                    this@WeatherActivity,
                    R.drawable.rainy_bg
                )
                binding.weatherForecastImg.setImageResource(R.drawable.sun_rain)

            }
            "10n" -> {
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
                window.statusBarColor = resources.getColor(R.color.rain)
                binding.swipeRefreshLayout.background = ContextCompat.getDrawable(
                    this@WeatherActivity,
                    R.drawable.rainy_bg
                )
                binding.weatherForecastImg.setImageResource(R.drawable.rainy_night)

            }
            "13d" -> {
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
                window.statusBarColor = resources.getColor(R.color.snow)
                binding.swipeRefreshLayout.background = ContextCompat.getDrawable(
                    this@WeatherActivity,
                    R.drawable.snow_bg
                )
                binding.weatherForecastImg.setImageResource(R.drawable.snow_cloud_day)

            }

            "13n" -> {
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
                window.statusBarColor = resources.getColor(R.color.snow)
                binding.swipeRefreshLayout.background = ContextCompat.getDrawable(
                    this@WeatherActivity,
                    R.drawable.snow_bg
                )
                binding.weatherForecastImg.setImageResource(R.drawable.snow_cloud_night)

            }
            "50d" -> {
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
                window.statusBarColor = resources.getColor(R.color.atmosphere)
                binding.swipeRefreshLayout.background = ContextCompat.getDrawable(
                    this@WeatherActivity,
                    R.drawable.mist_bg
                )
                binding.weatherForecastImg.setImageResource(R.drawable.fog_day)

            }
            "50n" -> {
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
                window.statusBarColor = resources.getColor(R.color.atmosphere)
                binding.swipeRefreshLayout.background = ContextCompat.getDrawable(
                    this@WeatherActivity,
                    R.drawable.mist_bg
                )
                binding.weatherForecastImg.setImageResource(R.drawable.fog_night)

            }
            "01d" -> {
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
                window.statusBarColor = resources.getColor(R.color.clear)
                binding.swipeRefreshLayout.background = ContextCompat.getDrawable(
                    this@WeatherActivity,
                    R.drawable.clear_bg
                )
                binding.weatherForecastImg.setImageResource(R.drawable.sun_)

            }
            "01n" -> {
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
                window.statusBarColor = resources.getColor(R.color.clearNight)
                binding.swipeRefreshLayout.background = ContextCompat.getDrawable(
                    this@WeatherActivity,
                    R.drawable.clear_bg_night
                )
                binding.weatherForecastImg.setImageResource(R.drawable.moon)

            }
            "02d" ->{
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
                window.statusBarColor = resources.getColor(R.color.cloudsTop)
                binding.swipeRefreshLayout.background = ContextCompat.getDrawable(
                    this@WeatherActivity,
                    R.drawable.cloud_bg
                )
                binding.weatherForecastImg.setImageResource(R.drawable.cloud_day)

            }
            "02n" ->{
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
                window.statusBarColor = resources.getColor(R.color.colorPrimaryNight)
                binding.swipeRefreshLayout.background = ContextCompat.getDrawable(
                    this@WeatherActivity,
                    R.drawable.cloud_bg_ni8
                )
                binding.weatherForecastImg.setImageResource(R.drawable.cloud_night)

            }
            "03d"->{
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
                window.statusBarColor = resources.getColor(R.color.cloudsTop)
                binding.swipeRefreshLayout.background = ContextCompat.getDrawable(
                    this@WeatherActivity,
                    R.drawable.cloud_bg
                )
                binding.weatherForecastImg.setImageResource(R.drawable.cloud_day)

            }
            "03n" ->{
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
                window.statusBarColor = resources.getColor(R.color.colorPrimaryNight)
                binding.swipeRefreshLayout.background = ContextCompat.getDrawable(
                    this@WeatherActivity,
                    R.drawable.cloud_bg_ni8
                )
                binding.weatherForecastImg.setImageResource(R.drawable.cloud_day)

            }
            "04d"->{
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
                window.statusBarColor = resources.getColor(R.color.cloudsTop)
                binding.swipeRefreshLayout.background = ContextCompat.getDrawable(
                    this@WeatherActivity,
                    R.drawable.cloud_bg
                )
                binding.weatherForecastImg.setImageResource(R.drawable.cloud_day)

            }
            "04n" ->{
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
                window.statusBarColor = resources.getColor(R.color.colorPrimaryNight)
                binding.swipeRefreshLayout.background = ContextCompat.getDrawable(
                    this@WeatherActivity,
                    R.drawable.cloud_bg_ni8
                )
                binding.weatherForecastImg.setImageResource(R.drawable.cloud_day)

            }
            else -> {
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
                window.statusBarColor = resources.getColor(R.color.cloudsTop)
                binding.swipeRefreshLayout.background = ContextCompat.getDrawable(
                    this@WeatherActivity,
                    R.drawable.cloud_bg
                )
                binding.weatherForecastImg.setImageResource(R.drawable.cloud_day)

            }
        }
        binding.progressBar.visibility = View.GONE
        binding.weatherPage.visibility = View.VISIBLE

    }
    companion object{
        private const val PERMISSION_REQUEST_ACCESS_LOCATION = 100
        const val API_KEY ="601dc0c859d578d9ad03b4fa414abd3b"
    }
        private fun kelvinToCelsius(temp: Double): Double {
        var intTemp = temp
        intTemp = intTemp.minus(273)
        return intTemp.toBigDecimal().setScale(1,RoundingMode.UP).toDouble()
    }
    @Suppress("MissingPermission")
    private fun newLocation() {
        val locationRequest = LocationRequest()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.interval = 0
        locationRequest.fastestInterval = 0
        locationRequest.numUpdates = 1
        mFusedLocation = LocationServices.getFusedLocationProviderClient(this)
        myLooper()
            ?.let { mFusedLocation.requestLocationUpdates(locationRequest,locationCallback, it) }

    }
    private val locationCallback=object : LocationCallback(){
        override fun onLocationResult(p0: LocationResult) {
            var lastLocation: Location? =p0.lastLocation
        }
    }
    private fun requestPermission() {
        ActivityCompat.requestPermissions(this, arrayOf(
            android.Manifest.permission.ACCESS_COARSE_LOCATION,
            android.Manifest.permission.ACCESS_FINE_LOCATION
        ),
            PERMISSION_REQUEST_ACCESS_LOCATION
        )
    }
    private fun locationEnabled(): Boolean {
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }
    private fun checkThePermission(): Boolean {
        if (ActivityCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED||
            ActivityCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            return true
        }
        return false
    }
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_ACCESS_LOCATION){
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                getLocation()
            }else{
                Toast.makeText(applicationContext,"Denied",Toast.LENGTH_SHORT).show()
            }
        }
    }
}