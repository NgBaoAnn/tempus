package com.projectapp.tempus.util

import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.appcompat.app.AlertDialog

object PermissionHelper {

    
    fun canScheduleExactAlarms(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.canScheduleExactAlarms()
        } else {
            
            true
        }
    }

    
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
                
                onPermissionGranted()
            }
            .setCancelable(false)
            .show()
    }

    
    private fun openAlarmSettings(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                data = Uri.parse("package:${context.packageName}")
            }
            context.startActivity(intent)
        }
    }
}
