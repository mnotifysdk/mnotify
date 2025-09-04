package com.convex.mnotifysdk.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.Log
import androidx.appcompat.app.AppCompatActivity.NOTIFICATION_SERVICE
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.app.NotificationCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.convex.mnotifysdk.MNotifyApp
import com.convex.mnotifysdk.model.NotificationModel
import com.convex.mnotifysdk.receiver.NotificationDismissReceiver
import com.convex.mnotifysdk.socket.SocketManager
import com.convex.mnotifysdk.utils.NotifyConstants
import java.util.Random
import kotlin.apply
import kotlin.jvm.java
import kotlin.takeIf
import kotlin.text.endsWith
import kotlin.text.isNullOrBlank
import kotlin.text.isNullOrEmpty
import kotlin.text.startsWith
import kotlin.text.toIntOrNull

internal object NotificationsManager {
    private lateinit var notificationManager: NotificationManager
    private var isInitialized = false
    private const val CHANNEL_ID = "bnotify_channel"

    fun init(context: Context) {
        if (!isInitialized) {
            notificationManager = context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            createNotificationChannel(context)
            isInitialized = true
        }
    }

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val appName = context.getConsumerAppName()
            val channel = NotificationChannel(
                CHANNEL_ID,
                appName,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "App notifications"
                enableLights(true)
                lightColor = Color.GREEN
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 1000, 500, 1000)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun handleNow(model: NotificationModel, context: Context) {
        try {
            if (!isInitialized) init(context)

            val notificationId = model.notificationId?.toIntOrNull() ?: Random().nextInt(10000)
            val notificationBuilder = buildBaseNotification(model, context, notificationId)

            if (!model.imageUrl.isNullOrBlank()) {
                val isURLValid = isValidImageUrl(model.imageUrl)
                if(isURLValid){
                    loadImageForNotification(model, notificationBuilder, notificationId,model.isExpanded!!, context)
                }else{
                    showNotification(notificationBuilder.build(), notificationId,model)
                }
            } else {
                showNotification(notificationBuilder.build(), notificationId,model)
            }
        }catch (e: Exception){
            Log.e("Bnotify", "${e.printStackTrace()}")
        }
    }


    /*private fun buildBaseNotification(
        model: NotificationModel,
        context: Context,
        notificationId: Int
    ): NotificationCompat.Builder {

        val pendingIntentFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
//            Log.i("Notification_SocketIO","PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE ${PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE}")
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
//            Log.i("Notification_SocketIO","FLAG_ONE_SHOT ${PendingIntent.FLAG_ONE_SHOT}")
        }

//        val pendingIntentFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
//        } else {
//            PendingIntent.FLAG_ONE_SHOT
//        }

//        val pendingIntentFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
//        } else {
//            PendingIntent.FLAG_UPDATE_CURRENT
//        }
        // For the main click action (when notification is clicked)
        val clickIntent = Intent(context, BNotifyApp.getActivityToOpenOnClick(context)).apply {
            putExtra(NotifyConstants.From, "notification")
            putExtra(NotifyConstants.SCREEN, model.screen)
            putExtra(NotifyConstants.ACTION, NotifyConstants.ClickedEvent) // Action when clicked
            putExtra(NotifyConstants.NOTIFICATION_ID, model.notificationId ?: 0)
            putExtra(NotifyConstants.TYPE, model.type ?: null)
            putExtra(NotifyConstants.TOKEN, model.token)
            putExtra(NotifyConstants.CLICK, true)
//            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }

        val clickPendingIntent = PendingIntent.getActivity(
            context,
            model.notificationId?.toIntOrNull() ?: System.currentTimeMillis().toInt(),
            clickIntent,
            pendingIntentFlags
        )

// For the dismiss action (when notification is dismissed)
        val dismissIntent = Intent(context, NotificationDismissReceiver::class.java).apply {
            putExtra(NotifyConstants.NOTIFICATION_ID, model.notificationId ?: 0)
            putExtra(NotifyConstants.ACTION, NotifyConstants.DismissedEvent) // Action when dismissed
            putExtra(NotifyConstants.SCREEN, model.screen)
            putExtra(NotifyConstants.TYPE, model.type ?: null)
            putExtra(NotifyConstants.CLICK, false)
            putExtra(NotifyConstants.TOKEN, model.token)
        }

        val dismissPendingIntent = PendingIntent.getBroadcast(
            context,
            (model.notificationId?.toIntOrNull() ?: System.currentTimeMillis().toInt()) + 1, // Different request code
            dismissIntent,
            pendingIntentFlags
        )

        val smallIconRes = context.applicationInfo.icon // consumer app's launcher icon
        val appIcon = context.packageManager.getApplicationIcon(context.packageName)
        val bitmap = drawableToBitmap(appIcon)
        //==========================================================================================

        Log.d("Notification_SocketIO", "Notification ACTION: ${model.action}")
        return NotificationCompat.Builder(context, CHANNEL_ID).apply {
            setAutoCancel(true)
            setDefaults(Notification.DEFAULT_ALL)
            setWhen(System.currentTimeMillis())
            setSmallIcon(smallIconRes)
            setLargeIcon(bitmap)
            setContentTitle(model.title)
            setContentText(model.message)
            setContentIntent(clickPendingIntent)
            setDeleteIntent(dismissPendingIntent) // Set dismiss intent
            setPriority(NotificationCompat.PRIORITY_HIGH)
            setCategory(NotificationCompat.CATEGORY_MESSAGE)
            setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            setShowWhen(true)
        }
    }*/

    private fun buildBaseNotification(
        model: NotificationModel,
        context: Context,
        notificationId: Int
    ): NotificationCompat.Builder {

        val pendingIntentFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_ONE_SHOT
        }

        // For the main click action (when notification is clicked)
        val clickIntent = Intent(context, MNotifyApp.getActivityToOpenOnClick(context)).apply {
            putExtra(NotifyConstants.From, "notification")
            putExtra(NotifyConstants.SCREEN, model.screen)
            putExtra(NotifyConstants.ACTION, NotifyConstants.ClickedEvent)
            putExtra(NotifyConstants.NOTIFICATION_ID, model.notificationId ?: 0)
            putExtra(NotifyConstants.TYPE, model.type ?: null)
            putExtra(NotifyConstants.TOKEN, model.token)
            putExtra(NotifyConstants.CLICK, true)
            // Add these flags to handle the activity launch properly
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }

        val clickPendingIntent = PendingIntent.getActivity(
            context,
            // Use a unique request code for each notification
            (model.notificationId?.toIntOrNull() ?: Random().nextInt()) + 1000,
            clickIntent,
            pendingIntentFlags
        )

        // For the dismiss action (when notification is dismissed)
        val dismissIntent = Intent(context, NotificationDismissReceiver::class.java).apply {
            putExtra(NotifyConstants.NOTIFICATION_ID, model.notificationId ?: 0)
            putExtra(NotifyConstants.ACTION, NotifyConstants.DismissedEvent)
            putExtra(NotifyConstants.SCREEN, model.screen)
            putExtra(NotifyConstants.TYPE, model.type ?: null)
            putExtra(NotifyConstants.CLICK, false)
            putExtra(NotifyConstants.TOKEN, model.token)
        }

        val dismissPendingIntent = PendingIntent.getBroadcast(
            context,
            // Use a different unique request code
            (model.notificationId?.toIntOrNull() ?: Random().nextInt()) + 2000,
            dismissIntent,
            pendingIntentFlags
        )

        val smallIconRes = context.applicationInfo.icon
        val appIcon = context.packageManager.getApplicationIcon(context.packageName)
        val bitmap = drawableToBitmap(appIcon)

        return NotificationCompat.Builder(context, CHANNEL_ID).apply {
            setAutoCancel(true)
            setDefaults(Notification.DEFAULT_ALL)
            setWhen(System.currentTimeMillis())
            setSmallIcon(smallIconRes)
            setLargeIcon(bitmap)
            setContentTitle(model.title)
            setContentText(model.message)
            setContentIntent(clickPendingIntent)
            setDeleteIntent(dismissPendingIntent)
            setPriority(NotificationCompat.PRIORITY_HIGH)
            setCategory(NotificationCompat.CATEGORY_MESSAGE)
            setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            setShowWhen(true)
        }
    }

    private fun loadImageForNotification(
        model: NotificationModel,
        builder: NotificationCompat.Builder,
        notificationId: Int,
        isExpanded: Boolean,
        context: Context
    ) {
        val appIcon = context.packageManager.getApplicationIcon(context.packageName)
        val bitmap = drawableToBitmap(appIcon)
        Glide.with(context)
            .asBitmap()
            .load(model.imageUrl)
            .into(object : CustomTarget<Bitmap>() {
                override fun onResourceReady(
                    resource: Bitmap,
                    transition: Transition<in Bitmap>?
                ) {
                    if (isExpanded){
                        builder.setStyle(
                            NotificationCompat.BigPictureStyle()
                                .bigPicture(resource)
                                .setBigContentTitle(model.title)
                                .setSummaryText(model.message)
                                .bigLargeIcon(bitmap)
                        )
                    }else{
                        // Initial compact view (no expanded image)
                        builder.setStyle(NotificationCompat.DecoratedCustomViewStyle())
                            .setLargeIcon(resource)
                    }

                    showNotification(builder.build(), notificationId, model)
                }

                override fun onLoadCleared(placeholder: Drawable?) {}

                override fun onLoadFailed(errorDrawable: Drawable?) {
                    showNotification(builder.build(), notificationId, model)
                }
            })
    }

    private fun showNotification(notification: Notification, notificationId: Int, model: NotificationModel) {
        try {
            notificationManager.notify(notificationId, notification)
            SocketManager.handleNotificationReceived(model)
            Log.d("NotificationsManager", "Notification shown with ID: $notificationId")
        } catch (e: Exception) {
            Log.e("NotificationsManager", "Error showing notification", e)
        }
    }

    private fun getBitmapFromMipmap(context: Context, mipmapResId: Int): Bitmap? {
        val drawable = AppCompatResources.getDrawable(context, mipmapResId) as? BitmapDrawable
        return drawable?.bitmap
    }

    private fun drawableToBitmap(drawable: Drawable): Bitmap {
        return if (drawable is BitmapDrawable) {
            drawable.bitmap
        } else {
            val width = drawable.intrinsicWidth.takeIf { it > 0 } ?: 1
            val height = drawable.intrinsicHeight.takeIf { it > 0 } ?: 1
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)
            bitmap
        }
    }

    fun isValidImageUrl(url: String?): Boolean {
        return !url.isNullOrEmpty() &&
                (url.startsWith("http://") || url.startsWith("https://")) &&
                (url.endsWith(".png", true) ||
                        url.endsWith(".jpg", true) ||
                        url.endsWith(".jpeg", true) ||
                        url.endsWith(".webp", true) ||
                        url.endsWith(".gif", true))
    }

    fun Context.getConsumerAppName(): String {
        val appInfo = applicationInfo
        val stringId = appInfo.labelRes
        return if (stringId == 0) {
            appInfo.nonLocalizedLabel?.toString() ?: packageManager.getApplicationLabel(appInfo).toString()
        } else {
            getString(stringId)
        }
    }


}