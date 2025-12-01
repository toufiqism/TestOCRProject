package com.example.testocrproject

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat

/**
 * Utility class for handling runtime permissions in the OCR application.
 * Supports SDK 23+ with proper permission handling for Camera and Storage.
 * 
 * @property context Application context for permission checks
 */
class PermissionHandler(private val context: Context) {
    
    companion object {
        /**
         * Required permissions for camera functionality
         */
        val CAMERA_PERMISSIONS = arrayOf(
            Manifest.permission.CAMERA
        )
        
        /**
         * Required permissions for reading images from storage
         * Returns appropriate permissions based on Android version
         */
        fun getStoragePermissions(): Array<String> {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                // Android 13+ (API 33+)
                arrayOf(Manifest.permission.READ_MEDIA_IMAGES)
            } else {
                // Android 12 and below (API 23-32)
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }
        
        /**
         * All required permissions for the app
         */
        fun getAllRequiredPermissions(): Array<String> {
            val storagePermissions = getStoragePermissions()
            return CAMERA_PERMISSIONS + storagePermissions
        }
    }
    
    /**
     * Checks if camera permission is granted
     * @return true if camera permission is granted, false otherwise
     */
    fun isCameraPermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }
    
    /**
     * Checks if storage read permission is granted
     * Automatically checks the appropriate permission based on Android version
     * @return true if storage permission is granted, false otherwise
     */
    fun isStoragePermissionGranted(): Boolean {
        val storagePermissions = getStoragePermissions()
        return storagePermissions.all { permission ->
            ContextCompat.checkSelfPermission(
                context,
                permission
            ) == PackageManager.PERMISSION_GRANTED
        }
    }
    
    /**
     * Checks if all required permissions are granted
     * @return true if all permissions are granted, false otherwise
     */
    fun areAllPermissionsGranted(): Boolean {
        return isCameraPermissionGranted() && isStoragePermissionGranted()
    }
    
    /**
     * Gets list of denied permissions
     * @return Array of permissions that are not granted
     */
    fun getDeniedPermissions(): Array<String> {
        val allPermissions = getAllRequiredPermissions()
        return allPermissions.filter { permission ->
            ContextCompat.checkSelfPermission(
                context,
                permission
            ) != PackageManager.PERMISSION_GRANTED
        }.toTypedArray()
    }
    
    /**
     * Checks if we should show rationale for camera permission
     * @param activity The activity to check against
     * @return true if rationale should be shown
     */
    fun shouldShowCameraPermissionRationale(activity: android.app.Activity): Boolean {
        return activity.shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)
    }
    
    /**
     * Checks if we should show rationale for storage permission
     * @param activity The activity to check against
     * @return true if rationale should be shown
     */
    fun shouldShowStoragePermissionRationale(activity: android.app.Activity): Boolean {
        val storagePermissions = getStoragePermissions()
        return storagePermissions.any { permission ->
            activity.shouldShowRequestPermissionRationale(permission)
        }
    }
}

/**
 * Enum representing permission request results
 */
enum class PermissionStatus {
    GRANTED,
    DENIED,
    PERMANENTLY_DENIED
}

/**
 * Data class for permission result
 */
data class PermissionResult(
    val permission: String,
    val status: PermissionStatus
)

