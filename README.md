# Haptix - Video Haptics Android App

Haptix is an Android application that plays videos with synchronized haptic feedback. The app displays a grid of video cards and allows users to play videos with custom haptic effects triggered at specific timestamps.

## Features

- **Video Grid**: Display videos as cards with thumbnails and titles
- **Video Playback**: Full-screen video player using ExoPlayer
- **Synchronized Haptics**: Custom haptic feedback triggered at precise video timestamps
- **Haptics Toggle**: Enable/disable haptic feedback during playback
- **Device Compatibility**: Graceful fallbacks for devices without haptic capabilities
- **Modern UI**: Material Design with ViewBinding

## Technical Requirements

- **Minimum SDK**: 26 (Android 8.0)
- **Target SDK**: 34 (Latest stable)
- **Language**: Kotlin
- **Architecture**: MVVM with LiveData/StateFlow
- **Video Player**: ExoPlayer
- **Image Loading**: Glide
- **Haptics**: VibratorManager (API 31+) with Vibrator fallback

## Project Structure

```
app/src/main/java/com/example/haptix/
├── model/                    # Data models
│   ├── VideoItem.kt         # Video data class
│   ├── HapticEvent.kt       # Individual haptic event
│   └── HapticsTimeline.kt   # Complete haptics timeline
├── data/                    # Repository layer
│   ├── VideoRepository.kt   # Video data management
│   └── HapticsRepository.kt # Haptics data parsing
├── ui/
│   ├── main/               # Main screen
│   │   ├── MainActivity.kt
│   │   ├── MainViewModel.kt
│   │   └── VideoListAdapter.kt
│   └── player/             # Player screen
│       ├── PlayerActivity.kt
│       └── PlayerViewModel.kt
├── haptics/                # Haptics engine
│   ├── HapticsEngine.kt    # Main haptics controller
│   ├── HapticsScheduler.kt # Timeline synchronization
│   ├── HapticsMapper.kt    # Event to effect mapping
│   └── HapticsCapabilities.kt # Device capability checks
└── util/                   # Utilities
    ├── JsonExtensions.kt   # JSON parsing helpers
    └── Logger.kt           # Logging utility
```

## Haptics Data Format

The app uses a JSON format to define haptic events:

```json
{
  "version": 1,
  "events": [
    {
      "t": 500,                       // ms from video start
      "type": "primitive",            // "primitive" | "waveform"
      "primitive": "CLICK",           // CLICK, TICK, THUD, HEAVY_CLICK, etc.
      "repeat": 1
    },
    {
      "t": 1200,
      "type": "primitive",
      "primitive": "HEAVY_CLICK"
    },
    {
      "t": 2500,
      "type": "waveform",             // Custom waveform
      "timings": [0, 30, 40, 30, 40], // ms
      "amplitudes": [0, 255, 0, 200, 0] // 0-255
    }
  ]
}
```

### Supported Primitive Types

- `CLICK`: Short, sharp feedback
- `TICK`: Very brief feedback
- `THUD`: Medium-duration feedback
- `HEAVY_CLICK`: Strong, longer feedback
- `SPIN`: Rotating feedback pattern
- `QUICK_RISE`: Rapid intensity increase
- `SLOW_RISE`: Gradual intensity increase
- `QUICK_FALL`: Rapid intensity decrease

## Building and Running

### Prerequisites

- Android Studio Arctic Fox or later
- Android SDK 26+
- Java 8 or later

### Build Steps

1. **Clone or download the project**
   ```bash
   cd haptix-app
   ```

2. **Open in Android Studio**
   - Open Android Studio
   - Select "Open an existing project"
   - Navigate to the project directory

3. **Sync Project**
   - Android Studio will automatically sync Gradle
   - Wait for sync to complete

4. **Run the App**
   - Connect an Android device or start an emulator
   - Click the "Run" button (green play icon)
   - Or use `./gradlew installDebug` from command line

### Sample Data

The app includes sample videos in `app/src/main/assets/videos.json`:
- Demo Trailer (Big Buck Bunny)
- Nature Clip (Elephant's Dream)
- Action Scene (For Bigger Blazes)

Each video has associated haptic events for demonstration.

## Usage

1. **Main Screen**: Browse available videos in a 2-column grid
2. **Video Selection**: Tap any video card to start playback
3. **Player Screen**: 
   - Use standard ExoPlayer controls for playback
   - Toggle haptics on/off with the switch
   - Haptic events trigger automatically at defined timestamps
4. **Navigation**: Use back button or toolbar arrow to return to main screen

## Device Compatibility

### Haptic Support

- **Full Support**: Devices with VibratorManager (API 31+) and primitive support
- **Partial Support**: Devices with basic vibrator (fallback to waveforms)
- **No Support**: Devices without vibrator (toggle disabled, user notified)

### API Level Fallbacks

- **API 31+**: Uses VibratorManager and VibrationEffect primitives
- **API 26-30**: Uses Vibrator with waveform fallbacks
- **API < 26**: Not supported (minSdk = 26)

## Known Limitations

1. **Do Not Disturb**: Haptics may be blocked in DND mode (handled gracefully)
2. **Battery Optimization**: Some devices may limit vibrations for battery saving
3. **Network Dependency**: Video URLs must be accessible
4. **Haptic Precision**: Actual timing may vary slightly (±15ms tolerance)
5. **Background Playback**: Haptics pause when app is backgrounded

## Troubleshooting

### Common Issues

1. **Videos not loading**
   - Check internet connection
   - Verify video URLs in `videos.json`
   - Check ExoPlayer logs

2. **Haptics not working**
   - Verify device has vibrator
   - Check if haptics are enabled in settings
   - Ensure app has VIBRATE permission

3. **Build errors**
   - Clean and rebuild project
   - Check Android SDK versions
   - Verify Gradle sync completed successfully

### Debug Logging

Enable debug logging by checking Logcat with tag "Haptix":
```bash
adb logcat -s Haptix
```

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make changes with proper testing
4. Submit a pull request

## License

This project is provided as-is for educational and demonstration purposes.

## Future Enhancements

- [ ] Custom haptic pattern editor
- [ ] Haptic intensity controls
- [ ] Offline video support
- [ ] Haptic timeline visualization
- [ ] Export/import haptic patterns
- [ ] Multiple haptic tracks per video
- [ ] Haptic pattern sharing# haptix-android-app
