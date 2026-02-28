package com.CuriosityLabs.digibalance.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.Image
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.IBinder
import android.util.DisplayMetrics
import android.view.WindowManager
import androidx.core.app.NotificationCompat
import com.CuriosityLabs.digibalance.R
import com.CuriosityLabs.digibalance.data.PreferencesManager
import com.CuriosityLabs.digibalance.data.repository.ScreenshotRequestRepository
import kotlinx.coroutines.*
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer

class ScreenshotCaptureService : Service() {
    
    private val serviceScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private val repository = ScreenshotRequestRepository()
    private lateinit var prefs: PreferencesManager
    
    private var mediaProjection: MediaProjection? = null
    private var virtualDisplay: VirtualDisplay? = null
    private var imageReader: ImageReader? = null
    
    private var isMonitoring = false
    private var monitoringJob: Job? = null
    
    companion object {
        const val CHANNEL_ID = "screenshot_capture_channel"
        const val NOTIFICATION_ID = 1003
        const val ACTION_START_MONITORING = "START_MONITORING"
        const val ACTION_STOP_MONITORING = "STOP_MONITORING"
        const val EXTRA_RESULT_CODE = "result_code"
        const val EXTRA_DATA = "data"
        
        private const val CHECK_INTERVAL_MS = 10000L // Check every 10 seconds
    }
    
    override fun onCreate() {
        super.onCreate()
        prefs = PreferencesManager.getInstance(this)
        createNotificationChannel()
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_MONITORING -> {
                val resultCode = intent.getIntExtra(EXTRA_RESULT_CODE, Activity.RESULT_CANCELED)
                val data = intent.getParcelableExtra<Intent>(EXTRA_DATA)
                
                if (resultCode == Activity.RESULT_OK && data != null) {
                    startMonitoring(resultCode, data)
                }
            }
            ACTION_STOP_MONITORING -> {
                stopMonitoring()
            }
        }
        
        return START_STICKY
    }
    
    private fun startMonitoring(resultCode: Int, data: Intent) {
        if (isMonitoring) return
        
        isMonitoring = true
        
        // Setup MediaProjection
        val projectionManager = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        mediaProjection = projectionManager.getMediaProjection(resultCode, data)
        
        // Start foreground service
        val notification = createNotification("Monitoring for screenshot requests...")
        startForeground(NOTIFICATION_ID, notification)
        
        // Start monitoring job
        monitoringJob = serviceScope.launch {
            while (isActive && isMonitoring) {
                checkForScreenshotRequests()
                delay(CHECK_INTERVAL_MS)
            }
        }
    }
    
    private fun stopMonitoring() {
        isMonitoring = false
        monitoringJob?.cancel()
        
        releaseMediaProjection()
        stopForeground(true)
        stopSelf()
    }
    
    private suspend fun checkForScreenshotRequests() {
        val studentId = prefs.currentUserId ?: return
        
        val result = repository.getPendingRequests(studentId)
        result.onSuccess { requests ->
            if (requests.isNotEmpty()) {
                // Process the first pending request
                val request = requests.first()
                captureAndUploadScreenshot(request.id)
            }
        }.onFailure { error ->
            android.util.Log.e("ScreenshotService", "Failed to check requests: ${error.message}")
        }
    }
    
    private suspend fun captureAndUploadScreenshot(requestId: String) {
        withContext(Dispatchers.Main) {
            try {
                val screenshot = captureScreen()
                if (screenshot != null) {
                    // Save to file
                    val file = saveBitmapToFile(screenshot)
                    
                    // Get device info
                    val deviceInfo = getDeviceInfo()
                    
                    // Upload
                    val uploadResult = repository.uploadScreenshot(requestId, file, deviceInfo)
                    uploadResult.onSuccess {
                        android.util.Log.d("ScreenshotService", "Screenshot uploaded successfully")
                        
                        // Show notification to student
                        showScreenshotCapturedNotification()
                        
                        // Clean up file
                        file.delete()
                    }.onFailure { error ->
                        android.util.Log.e("ScreenshotService", "Upload failed: ${error.message}")
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("ScreenshotService", "Capture failed: ${e.message}", e)
            }
        }
    }
    
    private fun captureScreen(): Bitmap? {
        try {
            val windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val metrics = DisplayMetrics()
            windowManager.defaultDisplay.getMetrics(metrics)
            
            val width = metrics.widthPixels
            val height = metrics.heightPixels
            val density = metrics.densityDpi
            
            // Create ImageReader
            imageReader = ImageReader.newInstance(width, height, PixelFormat.RGBA_8888, 2)
            
            // Create VirtualDisplay
            virtualDisplay = mediaProjection?.createVirtualDisplay(
                "ScreenCapture",
                width, height, density,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                imageReader?.surface, null, null
            )
            
            // Wait a bit for the display to be ready
            Thread.sleep(300)
            
            // Get the image
            val image = imageReader?.acquireLatestImage()
            val bitmap = image?.let { imageToBitmap(it) }
            
            image?.close()
            
            return bitmap
        } catch (e: Exception) {
            android.util.Log.e("ScreenshotService", "Screen capture error: ${e.message}", e)
            return null
        } finally {
            virtualDisplay?.release()
            imageReader?.close()
        }
    }
    
    private fun imageToBitmap(image: Image): Bitmap {
        val planes = image.planes
        val buffer: ByteBuffer = planes[0].buffer
        val pixelStride = planes[0].pixelStride
        val rowStride = planes[0].rowStride
        val rowPadding = rowStride - pixelStride * image.width
        
        val bitmap = Bitmap.createBitmap(
            image.width + rowPadding / pixelStride,
            image.height,
            Bitmap.Config.ARGB_8888
        )
        bitmap.copyPixelsFromBuffer(buffer)
        
        return Bitmap.createBitmap(bitmap, 0, 0, image.width, image.height)
    }
    
    private fun saveBitmapToFile(bitmap: Bitmap): File {
        val file = File(cacheDir, "screenshot_${System.currentTimeMillis()}.jpg")
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, out)
        }
        return file
    }
    
    private fun getDeviceInfo(): String {
        return buildString {
            append("{")
            append("\"model\":\"${Build.MODEL}\",")
            append("\"manufacturer\":\"${Build.MANUFACTURER}\",")
            append("\"android_version\":\"${Build.VERSION.RELEASE}\",")
            append("\"sdk_int\":${Build.VERSION.SDK_INT}")
            append("}")
        }
    }
    
    private fun releaseMediaProjection() {
        virtualDisplay?.release()
        imageReader?.close()
        mediaProjection?.stop()
        
        virtualDisplay = null
        imageReader = null
        mediaProjection = null
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Screenshot Monitoring",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Monitors for parent screenshot requests"
                setShowBadge(false)
            }
            
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    private fun createNotification(message: String): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("DigiBalance Monitoring")
            .setContentText(message)
            .setSmallIcon(R.drawable.focus) // Using existing icon
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()
    }
    
    private fun showScreenshotCapturedNotification() {
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Screenshot Captured")
            .setContentText("A screenshot was taken and sent to your parent")
            .setSmallIcon(R.drawable.focus) // Using existing icon
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()
        
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.notify(NOTIFICATION_ID + 1, notification)
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
    
    override fun onDestroy() {
        super.onDestroy()
        stopMonitoring()
        serviceScope.cancel()
    }
}
