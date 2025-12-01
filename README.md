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
- **Minimum SDK**: Android 6.0 (API level 23)
- **Target SDK**: Android 14 (API level 36)
- **Required Permissions**: 
  - **Camera**: Required for taking photos directly from the app
  - **Storage (API 23-32)**: READ_EXTERNAL_STORAGE and WRITE_EXTERNAL_STORAGE for accessing gallery
  - **Storage (API 33+)**: READ_MEDIA_IMAGES for accessing photos on Android 13+
  - All permissions are requested at runtime with proper rationale dialogs

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

### Permission Handling

The app uses a robust permission system that gracefully handles all permission scenarios:

1. **First Launch**: When you first use the camera or gallery features, the app will show a dialog explaining why the permission is needed
2. **Permission Grant**: Tap "Grant Permission" to proceed with the system permission dialog
3. **Permission Denial**: If you deny the permission, you can still use other features of the app
4. **Permanent Denial**: If you permanently deny a permission, the app will show a dialog with an "Open Settings" button to help you enable it

### Capturing and Processing Images

1. **Take a Photo**:
   - Tap the "Take Picture" button
   - If first time, you'll see a permission rationale dialog
   - Grant camera permission when prompted
   - Capture an image with text
   - The app automatically uploads the image to the OCR API

2. **Select from Gallery**:
   - Tap the "Select Image" button
   - If first time, you'll see a permission rationale dialog
   - Grant storage permission when prompted (type varies by Android version)
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
├── PermissionHandler.kt         # Comprehensive permission handling utility
├── AssetUtils.kt                # Utility functions for assets
└── ui/
    └── theme/                   # Material Design 3 theme configuration
        ├── Color.kt
        ├── Theme.kt
        └── Type.kt
```

### Key Components

#### 1. PermissionHandler
- Comprehensive utility class for runtime permission management
- Supports Android 6.0+ (API 23+) permission model
- Handles both camera and storage permissions
- Automatically detects Android version and requests appropriate storage permissions:
  - API 23-32: READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE
  - API 33+: READ_MEDIA_IMAGES
- Provides methods to check permission status and get denied permissions
- Null-safe and follows Android best practices

#### 2. PreferencesManager
- Singleton pattern for managing app preferences
- Stores and retrieves API base URL configuration
- Manages theme mode preferences (Light, Dark, System)
- Validates IP address format
- Provides default configuration

#### 3. SettingsScreen
- Material Design 3 UI for app configuration
- **Theme Selection**: Beautiful card-based theme selector with radio buttons
- **API Configuration**: IP address input with real-time validation
- Visual feedback for save operations with success/error messages
- Displays current base URL and helpful information
- Supports both dark and light themes with appropriate color schemes

#### 4. RetrofitInstance
- Dynamic Retrofit configuration based on user settings
- HTTP logging interceptor for debugging
- Gson converter for JSON parsing
- Automatically updates when settings change

#### 5. OCRViewModel
- Manages upload state (Idle, Loading, Success, Error)
- Handles API communication using Kotlin coroutines
- Provides clean state management for UI
- Factory pattern for dependency injection

#### 6. CameraCaptureScreen
- Camera integration using Android's ActivityResultContracts
- Gallery selection support
- **Comprehensive Permission Handling**:
  - Permission rationale dialogs before requesting permissions
  - Separate launchers for camera and storage permissions
  - Handles permission denial gracefully with helpful dialogs
  - "Open Settings" option for permanently denied permissions
- FileProvider integration for secure file sharing
- Coil for image loading and display
- Network connectivity check before upload

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

The application uses a comprehensive permission system that supports Android 6.0+ (API 23+):

### Declared Permissions in `AndroidManifest.xml`:

```xml
<!-- Camera Permission -->
<uses-permission android:name="android.permission.CAMERA" />
<uses-feature android:name="android.hardware.camera.any" android:required="true" />

<!-- Storage Permissions for SDK < 33 (Android 12 and below) -->
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE"
    android:maxSdkVersion="32" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"
    android:maxSdkVersion="32" />

<!-- Storage Permissions for SDK >= 33 (Android 13 and above) -->
<uses-permission android:name="android.permission.READ_MEDIA_IMAGES" />

<!-- Network Permissions -->
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
```

### Runtime Permission Handling:

- **Modern API**: Uses ActivityResultContracts for permission requests
- **User-Friendly Dialogs**: Shows rationale dialogs explaining why permissions are needed
- **Graceful Degradation**: App remains functional even if some permissions are denied
- **Settings Integration**: Provides direct link to app settings for permanently denied permissions
- **Version-Aware**: Automatically requests appropriate permissions based on Android version
- **Null-Safe**: All permission checks are null-safe and handle edge cases

### Permission Flow:

1. **User Action**: User taps "Take Picture" or "Select Image"
2. **Permission Check**: App checks if required permission is already granted
3. **Rationale Dialog**: If not granted, shows dialog explaining why permission is needed
4. **System Dialog**: User grants or denies permission in system dialog
5. **Fallback**: If denied, shows option to open app settings

## Error Handling

The application implements comprehensive error handling:

- **Network Errors**: Displays user-friendly error messages with connectivity checks
- **API Errors**: Shows specific error information from the server
- **Permission Denials**: Gracefully handles all permission denials with helpful dialogs
- **Permission Rationale**: Shows explanation dialogs before requesting permissions
- **Permanent Denial**: Provides "Open Settings" button for permanently denied permissions
- **Invalid Configuration**: Validates IP addresses before saving
- **Null Safety**: All nullable values are properly checked
- **File Operations**: Proper error handling for file creation and copying
- **Memory Management**: Automatic cleanup of temporary files

## Best Practices Implemented

✅ **SOLID Principles**: Single responsibility, dependency injection, interface segregation
✅ **Null Safety**: Proper null checking throughout the codebase
✅ **Error Handling**: Comprehensive try-catch blocks and error states
✅ **Permission Management**: Modern runtime permission handling with rationale dialogs (API 23+)
✅ **Version-Aware Permissions**: Automatically requests appropriate permissions based on Android version
✅ **State Management**: Clean state management with StateFlow
✅ **UI/UX**: Responsive UI with loading states and user feedback
✅ **User Privacy**: Clear permission rationale and respect for user choices
✅ **Graceful Degradation**: App remains functional even with denied permissions
✅ **Documentation**: Well-documented code with KDoc comments
✅ **Material Design 3**: Modern, accessible UI components
✅ **Separation of Concerns**: Clear separation between UI, business logic, and data layers
✅ **Memory Management**: Automatic cleanup of temporary files and resources

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
1. **Comprehensive Permission System** (Latest Update):
   - Runtime permission handling for Android 6.0+ (API 23+)
   - Version-aware storage permissions (different for API 23-32 vs 33+)
   - Permission rationale dialogs explaining why permissions are needed
   - Graceful handling of denied permissions
   - "Open Settings" integration for permanently denied permissions
   - PermissionHandler utility class for reusable permission logic
2. **Dark Mode Support**: Full dark mode implementation with three theme options
   - Light mode: Always use light theme
   - Dark mode: Always use dark theme
   - System mode: Follow device system settings
3. **Settings Screen**: New screen for configuring API base URL and theme
4. **Dynamic API Configuration**: API base URL and port now configurable via settings
5. **Navigation System**: Simple navigation between Camera and Settings screens
6. **Preferences Management**: SharedPreferences-based configuration storage
7. **IP Validation**: Real-time validation of IP address format
8. **Visual Feedback**: Success/error messages for user actions
9. **Top App Bar**: Added app bar with settings button on main screen
10. **Theme Persistence**: Theme preference persists across app restarts
11. **Network Check**: Validates network connectivity before uploads

### Modified Files:
- `AndroidManifest.xml`: Added comprehensive permission declarations with version-specific maxSdkVersion
- `MainActivity.kt`: Added navigation logic and theme management
- `PreferencesManager.kt`: Added theme mode storage, configurable port constant
- `SettingsScreen.kt`: Added theme selection UI with beautiful card-based options
- `CameraCaptureScreen.kt`: Complete permission system overhaul with dialogs and graceful handling
- `ImageExtractorAPI.kt`: Made base URL dynamic based on preferences
- `OCRViewModel.kt`: Added context parameter for API initialization
- `build.gradle.kts`: Updated minSdk to 23 for broader device support
- `README.md`: Comprehensive documentation with permission details

### New Files:
- `PermissionHandler.kt`: Comprehensive utility for runtime permission management
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
- **Solution**: The app will show a dialog with "Open Settings" button. Tap it to go directly to app permissions, or manually go to: Settings > Apps > TestOCRProject > Permissions > Enable Camera

**Problem**: Storage/Gallery permission denied
- **Solution**: Similar to camera, use the "Open Settings" button or manually enable READ_EXTERNAL_STORAGE (Android 12 and below) or READ_MEDIA_IMAGES (Android 13+) in app settings

**Problem**: Permission dialog doesn't appear
- **Solution**: You may have permanently denied the permission. Go to app settings and enable it manually, or clear app data and try again

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

**Last Updated**: December 1, 2024
**Version**: 1.0.0
**Minimum Android Version**: Android 6.0 (API 23)
**Target Android Version**: Android 14 (API 36)

