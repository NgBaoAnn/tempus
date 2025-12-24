package com.projectapp.tempus.ui.timeline

import android.annotation.SuppressLint
import android.content.Context
import com.projectapp.tempus.R

// Hàm mở rộng cho Context: Tự động tìm ID ảnh theo tên label
@SuppressLint("DiscouragedApi")
fun Context.getIconResId(label: String?): Int {
    // 1. Nếu label null hoặc rỗng -> trả về icon mặc định
    if (label.isNullOrEmpty()) return R.drawable.ic_launcher_foreground

    // 2. Tạo tên file mong muốn: tên label viết thường
    // Ví dụ: label="Eat" -> tìm file "eat"
    val resourceName = "${label.trim().lowercase()}"

    // 3. Tìm ID trong thư mục res/drawable
    val resId = this.resources.getIdentifier(
        resourceName,
        "drawable",
        this.packageName
    )

    // 4. Trả về ID tìm được, nếu không thấy thì trả về mặc định
    return if (resId != 0) resId else R.drawable.ic_launcher_foreground
}