package com.projectapp.tempus.ui.setting

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.projectapp.tempus.ui.setting.compose.ContactScreen
import com.projectapp.tempus.ui.theme.TempusTheme

class ContactActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            TempusTheme {
                ContactScreen(
                    onFacebookClick = { openFacebook() },
                    onEmailClick = { sendEmail() },
                    onAddressClick = { openAddress() },
                    onHotlineClick = { callHotline() },
                    onBackClick = { finish() }
                )
            }
        }
    }

    private fun openFacebook() {
        try {
            val facebookUrl = "https://www.facebook.com/profile.php?id=61587403014224"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(facebookUrl))
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "Không thể mở trình duyệt", Toast.LENGTH_SHORT).show()
        }
    }

    private fun sendEmail() {
        try {
            val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:hvo6471@gmail.com")
                putExtra(Intent.EXTRA_SUBJECT, "Hỗ trợ Tempus")
            }
            startActivity(Intent.createChooser(emailIntent, "Gửi email..."))
        } catch (e: Exception) {
            Toast.makeText(this, "Không tìm thấy ứng dụng email", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openAddress() {
        try {
            val mapUrl = "https://www.google.com/maps/place/Tr%C6%B0%E1%BB%9Dng+%C4%90%E1%BA%A1i+h%E1%BB%8Dc+Khoa+h%E1%BB%8Dc+T%E1%BB%B1+nhi%C3%AAn,+%C4%90HQG-HCM,+C%C6%A1+s%E1%BB%9F+Linh+Trung./@10.8756514,106.796595,17z/data=!3m1!4b1!4m6!3m5!1s0x3174d8a1768e1d03:0x38d3ea53e0581ae0!8m2!3d10.8756461!4d106.7991699!16s%2Fg%2F1tj5hn2m?entry=ttu&g_ep=EgoyMDI2MDEyNy4wIKXMDSoASAFQAw%3D%3D"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(mapUrl))
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "Không thể mở bản đồ", Toast.LENGTH_SHORT).show()
        }
    }

    private fun callHotline() {
        try {
            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:19001234"))
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "Không thể thực hiện cuộc gọi", Toast.LENGTH_SHORT).show()
        }
    }
}
