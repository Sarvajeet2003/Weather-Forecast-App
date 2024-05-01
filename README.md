**Weather Application**

**Overview**

The Weather Application is a mobile app designed to provide users with current weather conditions and forecasts for various locations. Users can search for specific cities or use geolocation to get weather information for their current location. The application fetches data from an external weather API and stores it locally for offline access. It features a user-friendly interface with options to customize themes and view weather forecasts for up to seven days.

**Features**

- **Current Weather Display**: View current weather conditions, including temperature, humidity, wind speed, and weather description.
- **Forecast Display**: Access seven-day weather forecasts with temperature ranges and weather descriptions for each day.
- **Location-Based Weather**: Fetch weather data for the user's current location using geolocation.
- **Database Integration**: Store weather data locally in a SQLite database for offline access.
- **User Interface Customization**: Customize the app's theme and weather icons based on current weather conditions.
- **Notification System**: Receive notifications for severe weather conditions and daily weather summaries.

**Technologies Used**

- **Programming Language**: Kotlin
- **API Integration**: Retrofit
- **Database**: SQLite
- **User Interface**: Android XML, View Binding
- **Location Services**: FusedLocationProviderClient
- **Notification**: NotificationManager, PendingIntent

**Usage**

1. Launch the application on your Android device.
1. Search for a city or allow the app to use your current location.
1. View current weather conditions and forecasts for the selected location.
1. Customize themes and view weather forecasts for different dates.

**Credits**

- Weather data provided by[ OpenWeather](https://openweathermap.org/).
