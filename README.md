# WeatherSnap

WeatherSnap is a complete Kotlin Android app built with Jetpack Compose, Material 3, MVVM, Hilt, Retrofit, Room, CameraX, Coroutines, and Flow.

## Setup

1. Open this folder in Android Studio.
2. Let Gradle sync the project.
3. Use an Android device or emulator with camera support.
4. Run the `app` configuration.

## Flow

1. Search for a city with more than two letters.
2. Select an Open-Meteo geocoding suggestion.
3. Review live Open-Meteo weather.
4. Create a report.
5. Capture a photo with the custom CameraX screen.
6. Save the compressed photo, weather details, notes, and file sizes to Room.
7. View saved reports from local storage.

## Notes

- Weather data uses Open-Meteo only.
- Photos are saved under the app's internal `filesDir/photos` folder.
- Images are compressed with `BitmapFactory` and JPEG quality `60`.
- Saved report images are loaded manually with `BitmapFactory`; no Coil or Glide is used.
- Camera permission is requested at runtime on the camera screen.
