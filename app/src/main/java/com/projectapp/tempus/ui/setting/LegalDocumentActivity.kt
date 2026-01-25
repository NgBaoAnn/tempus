package com.projectapp.tempus.ui.setting

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.projectapp.tempus.ui.theme.TempusTheme

/**
 * Activity hiển thị các tài liệu pháp lý (Privacy Policy, Terms of Service)
 */
class LegalDocumentActivity : ComponentActivity() {

    companion object {
        const val EXTRA_DOCUMENT_TYPE = "document_type"
        const val TYPE_PRIVACY_POLICY = "privacy_policy"
        const val TYPE_TERMS_OF_SERVICE = "terms_of_service"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        val documentType = intent.getStringExtra(EXTRA_DOCUMENT_TYPE) ?: TYPE_PRIVACY_POLICY
        
        setContent {
            TempusTheme {
                Scaffold { paddingValues ->
                    when (documentType) {
                        TYPE_PRIVACY_POLICY -> PrivacyPolicyScreen(
                            onBackClick = { finish() },
                            modifier = Modifier.padding(paddingValues)
                        )
                        TYPE_TERMS_OF_SERVICE -> TermsOfServiceScreen(
                            onBackClick = { finish() },
                            modifier = Modifier.padding(paddingValues)
                        )
                    }
                }
            }
        }
    }
}

// ======================== COLORS ========================

private object LegalColors {
    val Background = Color(0xFFF2F2F7)
    val Surface = Color.White
    val TextPrimary = Color(0xFF000000)
    val TextSecondary = Color(0xFF6B7280)
    val Blue = Color(0xFF007AFF)
    val Divider = Color(0xFFE5E5EA)
}

// ======================== PRIVACY POLICY SCREEN ========================

@Composable
private fun PrivacyPolicyScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(LegalColors.Background)
    ) {
        // Header
        LegalHeader(
            title = "Chính sách quyền riêng tư",
            onBackClick = onBackClick
        )
        
        // Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            LegalCard {
                LegalSectionTitle("1. Giới thiệu")
                LegalParagraph(
                    """Chào mừng bạn đến với Tempus - Ứng dụng Quản lý Thời gian Thông minh. Chúng tôi cam kết bảo vệ quyền riêng tư và dữ liệu cá nhân của bạn. Chính sách này mô tả cách chúng tôi thu thập, sử dụng và bảo vệ thông tin của bạn."""
                )
            }
            
            LegalCard {
                LegalSectionTitle("2. Thông tin chúng tôi thu thập")
                LegalSubsection("2.1. Thông tin tài khoản")
                LegalParagraph(
                    """• Email và tên người dùng khi đăng ký
• Mật khẩu đã được mã hóa
• Thông tin hồ sơ (tùy chọn)"""
                )
                
                LegalSubsection("2.2. Dữ liệu sử dụng")
                LegalParagraph(
                    """• Lịch trình và công việc bạn tạo
• Thời gian thức/ngủ cá nhân hóa
• Cài đặt ứng dụng"""
                )
                
                LegalSubsection("2.3. Thông tin thiết bị")
                LegalParagraph(
                    """• Loại thiết bị và hệ điều hành
• Múi giờ địa phương
• Token thông báo (nếu bật)"""
                )
            }
            
            LegalCard {
                LegalSectionTitle("3. Cách chúng tôi sử dụng thông tin")
                LegalParagraph(
                    """• Cung cấp và cải thiện dịch vụ
• Đồng bộ dữ liệu giữa các thiết bị
• Gửi thông báo nhắc nhở (nếu bạn cho phép)
• Phân tích để cải thiện trải nghiệm người dùng
• Hỗ trợ kỹ thuật khi cần thiết"""
                )
            }
            
            LegalCard {
                LegalSectionTitle("4. Bảo mật dữ liệu")
                LegalParagraph(
                    """Chúng tôi áp dụng các biện pháp bảo mật tiên tiến:

• Mã hóa dữ liệu khi truyền tải (TLS/SSL)
• Lưu trữ an toàn trên Supabase với mã hóa
• Xác thực sinh trắc học cho các thao tác nhạy cảm
• Không chia sẻ dữ liệu với bên thứ ba vì mục đích quảng cáo"""
                )
            }
            
            LegalCard {
                LegalSectionTitle("5. Quyền của bạn")
                LegalParagraph(
                    """Bạn có quyền:

• Truy cập và xem dữ liệu của mình
• Xuất dữ liệu (JSON/CSV)
• Chỉnh sửa hoặc xóa thông tin cá nhân
• Xóa tài khoản vĩnh viễn
• Từ chối nhận thông báo"""
                )
            }
            
            LegalCard {
                LegalSectionTitle("6. Lưu trữ và xóa dữ liệu")
                LegalParagraph(
                    """• Dữ liệu được lưu trữ trên máy chủ Supabase
• Bạn có thể xóa tất cả dữ liệu trong phần Cài đặt
• Sau khi xóa tài khoản, dữ liệu sẽ bị xóa trong 30 ngày
• Log hệ thống có thể được giữ lại tối đa 90 ngày"""
                )
            }
            
            LegalCard {
                LegalSectionTitle("7. Thay đổi chính sách")
                LegalParagraph(
                    """Chúng tôi có thể cập nhật chính sách này theo thời gian. Bạn sẽ được thông báo về các thay đổi quan trọng qua ứng dụng hoặc email."""
                )
            }
            
            LegalCard {
                LegalSectionTitle("8. Liên hệ")
                LegalParagraph(
                    """Nếu có câu hỏi về chính sách quyền riêng tư, vui lòng liên hệ:

📧 Email: support@tempus-app.com
🌐 Website: https://tempus-app.com"""
                )
            }
            
            LegalFooter("Cập nhật lần cuối: 25/01/2026")
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

// ======================== TERMS OF SERVICE SCREEN ========================

@Composable
private fun TermsOfServiceScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(LegalColors.Background)
    ) {
        // Header
        LegalHeader(
            title = "Điều khoản dịch vụ",
            onBackClick = onBackClick
        )
        
        // Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            LegalCard {
                LegalSectionTitle("1. Chấp nhận điều khoản")
                LegalParagraph(
                    """Bằng việc tải xuống, cài đặt hoặc sử dụng ứng dụng Tempus, bạn đồng ý tuân thủ các điều khoản và điều kiện được quy định trong tài liệu này."""
                )
            }
            
            LegalCard {
                LegalSectionTitle("2. Mô tả dịch vụ")
                LegalParagraph(
                    """Tempus là ứng dụng quản lý thời gian cung cấp các tính năng:

• Tạo và quản lý lịch trình cá nhân
• Cá nhân hóa thói quen hằng ngày
• Đồng bộ dữ liệu đám mây
• Nhắc nhở thông minh
• Phân tích và thống kê thời gian"""
                )
            }
            
            LegalCard {
                LegalSectionTitle("3. Tài khoản người dùng")
                LegalParagraph(
                    """• Bạn phải cung cấp thông tin chính xác khi đăng ký
• Bạn chịu trách nhiệm bảo mật tài khoản
• Mỗi tài khoản chỉ dành cho một người sử dụng
• Chúng tôi có quyền khóa tài khoản vi phạm"""
                )
            }
            
            LegalCard {
                LegalSectionTitle("4. Quy tắc sử dụng")
                LegalParagraph(
                    """Bạn cam kết KHÔNG:

• Sử dụng ứng dụng cho mục đích bất hợp pháp
• Cố gắng truy cập trái phép hệ thống
• Sao chép, phân phối hoặc sửa đổi ứng dụng
• Gây hại cho người dùng khác
• Vi phạm quyền sở hữu trí tuệ"""
                )
            }
            
            LegalCard {
                LegalSectionTitle("5. Quyền sở hữu trí tuệ")
                LegalParagraph(
                    """• Tempus và tất cả nội dung là tài sản của chúng tôi
• Bạn được cấp giấy phép sử dụng có giới hạn
• Không được sao chép mã nguồn hoặc thiết kế
• Logo, thương hiệu thuộc quyền sở hữu của Tempus"""
                )
            }
            
            LegalCard {
                LegalSectionTitle("6. Giới hạn trách nhiệm")
                LegalParagraph(
                    """• Ứng dụng được cung cấp "nguyên trạng"
• Chúng tôi không đảm bảo hoạt động không gián đoạn
• Không chịu trách nhiệm cho mất mát dữ liệu do lỗi người dùng
• Tổng trách nhiệm bồi thường không vượt quá phí dịch vụ (nếu có)"""
                )
            }
            
            LegalCard {
                LegalSectionTitle("7. Chấm dứt")
                LegalParagraph(
                    """• Bạn có thể ngừng sử dụng bất cứ lúc nào
• Chúng tôi có thể chấm dứt dịch vụ với thông báo 30 ngày
• Tài khoản vi phạm có thể bị khóa ngay lập tức
• Sau khi chấm dứt, bạn có 30 ngày để xuất dữ liệu"""
                )
            }
            
            LegalCard {
                LegalSectionTitle("8. Thay đổi điều khoản")
                LegalParagraph(
                    """Chúng tôi có quyền sửa đổi điều khoản này. Thay đổi quan trọng sẽ được thông báo trước 14 ngày. Tiếp tục sử dụng sau thay đổi đồng nghĩa với việc bạn chấp nhận điều khoản mới."""
                )
            }
            
            LegalCard {
                LegalSectionTitle("9. Luật áp dụng")
                LegalParagraph(
                    """Các điều khoản này được điều chỉnh bởi pháp luật Việt Nam. Mọi tranh chấp sẽ được giải quyết tại tòa án có thẩm quyền tại Việt Nam."""
                )
            }
            
            LegalCard {
                LegalSectionTitle("10. Liên hệ")
                LegalParagraph(
                    """Thắc mắc về điều khoản dịch vụ:

📧 Email: legal@tempus-app.com
🌐 Website: https://tempus-app.com/terms"""
                )
            }
            
            LegalFooter("Cập nhật lần cuối: 25/01/2026")
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

// ======================== COMMON COMPONENTS ========================

@Composable
private fun LegalHeader(
    title: String,
    onBackClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(LegalColors.Background)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextButton(
            onClick = onBackClick,
            contentPadding = PaddingValues(0.dp)
        ) {
            Text(
                text = "← Quay lại",
                color = LegalColors.Blue,
                fontSize = 17.sp
            )
        }

        Text(
            text = title,
            color = LegalColors.TextPrimary,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .weight(1f)
                .padding(end = 60.dp)
        )
    }
}

@Composable
private fun LegalCard(
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = LegalColors.Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            content = content
        )
    }
}

@Composable
private fun LegalSectionTitle(text: String) {
    Text(
        text = text,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        color = LegalColors.TextPrimary,
        modifier = Modifier.padding(bottom = 12.dp)
    )
}

@Composable
private fun LegalSubsection(text: String) {
    Text(
        text = text,
        fontSize = 15.sp,
        fontWeight = FontWeight.SemiBold,
        color = LegalColors.TextPrimary,
        modifier = Modifier.padding(top = 12.dp, bottom = 8.dp)
    )
}

@Composable
private fun LegalParagraph(text: String) {
    Text(
        text = text,
        fontSize = 15.sp,
        color = LegalColors.TextSecondary,
        lineHeight = 22.sp
    )
}

@Composable
private fun LegalFooter(text: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = LegalColors.Blue.copy(alpha = 0.1f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Text(
            text = text,
            fontSize = 13.sp,
            color = LegalColors.Blue,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        )
    }
}
