package com.projectapp.tempus.util

import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.appcompat.app.AlertDialog

object PermissionHelper {

    /**
     * Check if the app has permission to schedule exact alarms
     */
    fun canScheduleExactAlarms(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.canScheduleExactAlarms()
        } else {
            // Permission not required for Android 11 and below
            true
        }
    }

    /**
     * Show dialog explaining the need for SCHEDULE_EXACT_ALARM permission
     * and offer to open Settings
     */
    fun showAlarmPermissionDialog(context: Context, onPermissionGranted: () -> Unit) {
        AlertDialog.Builder(context)
            .setTitle("Cần cấp quyền thông báo")
            .setMessage(
                "Để nhận thông báo đúng giờ cho các công việc đã lên lịch, " +
                "ứng dụng cần quyền 'Báo thức và lời nhắc'.\n\n" +
                "Bạn có muốn mở Cài đặt để cấp quyền không?"
            )
            .setPositiveButton("Mở Cài đặt") { _, _ ->
                openAlarmSettings(context)
            }
            .setNegativeButton("Để sau") { dialog, _ ->
                dialog.dismiss()
                // Still call the callback to save the task
                onPermissionGranted()
            }
            .setCancelable(false)
            .show()
    }

    /**
     * Open the alarm & reminders settings page for this app
     */
    private fun openAlarmSettings(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                data = Uri.parse("package:${context.packageName}")
            }
            context.startActivity(intent)
        }
    }
}
