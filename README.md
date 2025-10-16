# TestOCRProject - Android OCR Application

An Android application for Optical Character Recognition (OCR) that captures images from the camera or gallery and extracts text using a remote API server.

## Features

- 📷 **Camera Capture**: Take photos directly from the app using the device camera
- 🖼️ **Gallery Selection**: Choose existing images from the device gallery
- 🔤 **Text Extraction**: Extract text from images using an OCR API server
- ⚙️ **Configurable API**: Easy-to-use settings screen to configure the API server IP address
- 🎨 **Modern UI**: Material Design 3 with dynamic color support
- 🌙 **Dark Mode**: Full dark mode support with Light, Dark, and System theme options
- 🌐 **Network API Integration**: RESTful API communication using Retrofit

## Requirements

### Android Device/Emulator
- **Minimum SDK**: Android 10 (API level 29)
- **Target SDK**: Android 14 (API level 36)
- **Required Permissions**: 
  - Camera permission for taking photos
  - Storage access for gallery selection

### Backend API Server
- Must be running and accessible on the configured network
- Expected endpoint: `POST /extract_text`
- Must accept multipart/form-data with an image file
- Must respond with JSON: `{"text": "extracted text content"}`

## Installation

1. Clone the repository:
```bash
git clone <repository-url>
cd TestOCRProject
```

2. Open the project in Android Studio

3. Sync Gradle dependencies

4. Build and run the application on a device or emulator

## Configuration

### Setting Up App Appearance (Theme)

The application supports three theme modes for optimal viewing comfort:

1. Launch the application
2. Tap the **Settings** icon (⚙️) in the top right corner
3. Under the **Appearance** section, choose your preferred theme:
   - **Light**: Always use light theme regardless of system settings
   - **Dark**: Always use dark theme regardless of system settings
   - **System Default**: Automatically follow your device's system theme setting
4. The theme changes immediately upon selection

### Setting Up the API Base URL

The application communicates with a backend OCR API server. By default, it's configured to use `http://192.168.103.82:5000/`, but you can easily change this:

1. Launch the application
2. Tap the **Settings** icon (⚙️) in the top right corner
3. Scroll down to the **API Configuration** section
4. Enter the IP address of your API server (e.g., `192.168.1.100`)
5. The format is automatically constructed as: `http://[YOUR_IP]:5000/`
6. Tap **Save** to apply the changes

**Note**: The HTTP protocol and port 5000 are fixed. Only the IP address is configurable.

### Network Requirements

- Ensure your Android device and API server are on the same network
- For emulators, use appropriate IP addresses (e.g., `10.0.2.2` for localhost on Android emulator)
- Make sure the API server is running on port 5000
- Clear text traffic is enabled in the AndroidManifest.xml for development purposes

## Usage

### Capturing and Processing Images

1. **Take a Photo**:
   - Tap the "Take Picture" button
   - Grant camera permission if prompted
   - Capture an image with text
   - The app automatically uploads the image to the OCR API

2. **Select from Gallery**:
   - Tap the "Select Image" button
   - Choose an image from your device
   - The app automatically uploads the image to the OCR API

3. **View Results**:
   - Wait for the upload and processing to complete
   - Extracted text will be displayed on the screen
   - Tap "Start Over" to process another image

4. **Handle Errors**:
   - If an error occurs, an error message will be displayed
   - Check your network connection and API server status
   - Tap "Try Again" to return to the capture screen

## Architecture

The application follows the **SOLID principles** and modern Android development best practices:

### Project Structure

```
app/src/main/java/com/example/testocrproject/
├── MainActivity.kt              # Main entry point with navigation logic
├── CameraCaptureScreen.kt       # Camera capture and gallery selection UI
├── SettingsScreen.kt            # API configuration UI
├── OCRViewModel.kt              # ViewModel for managing OCR upload state
├── ImageExtractorAPI.kt         # Retrofit API interface and configuration
├── PreferencesManager.kt        # SharedPreferences management for settings
├── AssetUtils.kt                # Utility functions for assets
└── ui/
    └── theme/                   # Material Design 3 theme configuration
        ├── Color.kt
        ├── Theme.kt
        └── Type.kt
```

### Key Components

#### 1. PreferencesManager
- Singleton pattern for managing app preferences
- Stores and retrieves API base URL configuration
- Manages theme mode preferences (Light, Dark, System)
- Validates IP address format
- Provides default configuration

#### 2. SettingsScreen
- Material Design 3 UI for app configuration
- **Theme Selection**: Beautiful card-based theme selector with radio buttons
- **API Configuration**: IP address input with real-time validation
- Visual feedback for save operations with success/error messages
- Displays current base URL and helpful information
- Supports both dark and light themes with appropriate color schemes

#### 3. RetrofitInstance
- Dynamic Retrofit configuration based on user settings
- HTTP logging interceptor for debugging
- Gson converter for JSON parsing
- Automatically updates when settings change

#### 4. OCRViewModel
- Manages upload state (Idle, Loading, Success, Error)
- Handles API communication using Kotlin coroutines
- Provides clean state management for UI
- Factory pattern for dependency injection

#### 5. CameraCaptureScreen
- Camera integration using Android's ActivityResultContracts
- Gallery selection support
- Permission handling
- FileProvider integration for secure file sharing
- Coil for image loading and display

## Dependencies

### Core Libraries
- **Jetpack Compose**: Modern declarative UI toolkit
- **Material 3**: Latest Material Design components
- **Kotlin Coroutines**: Asynchronous programming
- **ViewModel & Lifecycle**: Android Architecture Components

### Networking
- **Retrofit**: Type-safe HTTP client
- **OkHttp**: HTTP client with logging interceptor
- **Gson**: JSON serialization/deserialization

### Image Handling
- **Coil**: Image loading library for Compose
- **CameraX**: Modern camera API
- **FileProvider**: Secure file sharing

### OCR (Optional - Tesseract)
- **Tess-Two**: On-device OCR (currently not in use, relies on API)

## Permissions

The following permissions are declared in `AndroidManifest.xml`:

```xml
<uses-permission android:name="android.permission.CAMERA" />
<uses-feature android:name="android.hardware.camera.any" android:required="true" />
```

Permissions are requested at runtime using the modern ActivityResultContracts API, ensuring graceful handling and user privacy.

## Error Handling

The application implements comprehensive error handling:

- **Network Errors**: Displays user-friendly error messages
- **API Errors**: Shows specific error information from the server
- **Permission Denials**: Gracefully handles camera permission denial
- **Invalid Configuration**: Validates IP addresses before saving
- **Null Safety**: All nullable values are properly checked

## Best Practices Implemented

✅ **SOLID Principles**: Single responsibility, dependency injection, interface segregation
✅ **Null Safety**: Proper null checking throughout the codebase
✅ **Error Handling**: Comprehensive try-catch blocks and error states
✅ **Permission Management**: Modern runtime permission handling
✅ **State Management**: Clean state management with StateFlow
✅ **UI/UX**: Responsive UI with loading states and user feedback
✅ **Documentation**: Well-documented code with KDoc comments
✅ **Material Design 3**: Modern, accessible UI components
✅ **Separation of Concerns**: Clear separation between UI, business logic, and data layers

## Development Notes

### For iOS Development
If you plan to port this to iOS (Flutter/Swift), consider:
- iOS camera permissions require `NSCameraUsageDescription` in Info.plist
- iOS photo library access requires `NSPhotoLibraryUsageDescription`
- Network security configuration for HTTP endpoints (App Transport Security)
- Equivalent preference storage using UserDefaults (iOS) or shared_preferences (Flutter)

### Building for Production
Before releasing to production:
1. Remove or disable HTTP logging interceptor
2. Add ProGuard/R8 rules for Retrofit and Gson
3. Consider using HTTPS instead of HTTP for API communication
4. Implement proper SSL certificate pinning
5. Add crash reporting (e.g., Firebase Crashlytics)
6. Implement analytics for usage tracking
7. Add input validation for all user inputs
8. Consider adding offline caching capabilities

### Screen Size Compatibility
The application uses:
- `Modifier.fillMaxSize()` for responsive layouts
- Scaffold with proper padding values
- Scrollable content with `verticalScroll()`
- Flexible Row/Column arrangements
These ensure compatibility across different screen sizes and orientations.

## Recent Changes (Latest Update)

### Added Features:
1. **Dark Mode Support**: Full dark mode implementation with three theme options
   - Light mode: Always use light theme
   - Dark mode: Always use dark theme
   - System mode: Follow device system settings
2. **Settings Screen**: New screen for configuring API base URL and theme
3. **Dynamic API Configuration**: API base URL now configurable via settings
4. **Navigation System**: Simple navigation between Camera and Settings screens
5. **Preferences Management**: SharedPreferences-based configuration storage
6. **IP Validation**: Real-time validation of IP address format
7. **Visual Feedback**: Success/error messages for user actions
8. **Top App Bar**: Added app bar with settings button on main screen
9. **Theme Persistence**: Theme preference persists across app restarts

### Modified Files:
- `MainActivity.kt`: Added navigation logic and theme management
- `PreferencesManager.kt`: Added theme mode storage and retrieval with ThemeMode enum
- `SettingsScreen.kt`: Added theme selection UI with beautiful card-based options
- `CameraCaptureScreen.kt`: Added settings button and proper ViewModel initialization
- `ImageExtractorAPI.kt`: Made base URL dynamic based on preferences
- `OCRViewModel.kt`: Added context parameter for API initialization
- `README.md`: Updated with dark mode documentation

### New Files:
- `PreferencesManager.kt`: Manages app configuration (API + Theme)
- `SettingsScreen.kt`: UI for API and theme configuration
- `README.md`: This comprehensive documentation file

## Troubleshooting

### Common Issues

**Problem**: "Upload failed" error
- **Solution**: Check that the API server is running and accessible on the network
- Verify the IP address in settings is correct
- Ensure your device/emulator can reach the server

**Problem**: Camera permission denied
- **Solution**: Go to device Settings > Apps > TestOCRProject > Permissions and enable Camera

**Problem**: "Invalid IP address" error in settings
- **Solution**: Enter a valid IPv4 address in the format: xxx.xxx.xxx.xxx (e.g., 192.168.1.100)

**Problem**: App crashes on image capture
- **Solution**: Check that external storage is available and FileProvider is properly configured

**Problem**: Theme doesn't change immediately
- **Solution**: The theme changes are instant when selected in settings. If issues persist, try restarting the app

**Problem**: Dark mode colors look incorrect
- **Solution**: The app uses Material Design 3 dynamic colors on Android 12+. On older versions, predefined color schemes are used

## Future Enhancements

Potential improvements for future versions:
- [ ] Add support for HTTPS connections
- [ ] Implement on-device OCR as fallback (using Tesseract)
- [ ] Support for multiple languages
- [ ] History of processed images and texts
- [ ] Export extracted text to file
- [x] ~~Dark mode support~~ ✅ **Implemented!**
- [ ] Batch image processing
- [ ] Text editing and formatting
- [ ] Cloud storage integration
- [ ] Custom theme colors
- [ ] Font size adjustment in settings

## License

[Your License Here]

## Contributors

[Your Name/Team]

## Contact

For issues, questions, or contributions, please [open an issue](your-repo-url/issues) or contact [your-email].

---

**Last Updated**: October 16, 2025
**Version**: 1.0.0
**Minimum Android Version**: Android 10 (API 29)

