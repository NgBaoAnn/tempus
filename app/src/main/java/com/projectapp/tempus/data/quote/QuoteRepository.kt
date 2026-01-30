package com.projectapp.tempus.data.quote

import android.content.Context
import android.content.SharedPreferences
import com.projectapp.tempus.data.quote.dto.QuoteDto
import java.time.LocalDate

/**
 * Repository để quản lý Daily Quotes
 * Sử dụng local quotes với caching theo ngày
 */
class QuoteRepository(context: Context) {
    
    private val prefs: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME, 
        Context.MODE_PRIVATE
    )
    
    /**
     * Lấy quote cho ngày hôm nay
     * Quote sẽ được cache và giữ nguyên trong suốt ngày
     */
    fun getTodayQuote(): QuoteDto {
        val today = LocalDate.now()
        val todayString = today.toString()
        
        // Determine current language
        // Prioritize explicit setting -> then system default
        val explicitLang = com.projectapp.tempus.data.user.UserProfileCache.getLanguage()
        val currentLanguage = explicitLang ?: java.util.Locale.getDefault().language
        val effectiveLanguage = if (currentLanguage == "vi") "vi" else "en" // Normalize to supported
        
        // Kiểm tra xem đã có quote cho hôm nay và ĐÚNG NGÔN NGỮ chưa
        val cachedDate = prefs.getString(KEY_QUOTE_DATE, null)
        val cachedText = prefs.getString(KEY_QUOTE_TEXT, null)
        val cachedAuthor = prefs.getString(KEY_QUOTE_AUTHOR, null)
        val cachedLang = prefs.getString(KEY_QUOTE_LANG, null)
        
        // Nếu đã cache quote cho hôm nay VÀ đúng ngôn ngữ, trả về quote đó
        if (cachedDate == todayString && cachedText != null && cachedLang == effectiveLanguage) {
            return QuoteDto(
                text = cachedText,
                author = if (cachedAuthor.isNullOrBlank()) null else cachedAuthor
            )
        }
        
        // Nếu chưa có hoặc đã qua ngày hoặc SAI NGÔN NGỮ, lấy quote mới
        val daySeed = today.year * 1000 + today.dayOfYear
        val newQuote = LocalQuotesProvider.getQuoteForSeed(daySeed, effectiveLanguage)
        
        // Cache quote mới kèm ngôn ngữ
        prefs.edit()
            .putString(KEY_QUOTE_DATE, todayString)
            .putString(KEY_QUOTE_LANG, effectiveLanguage)
            .putString(KEY_QUOTE_TEXT, newQuote.text)
            .putString(KEY_QUOTE_AUTHOR, newQuote.author ?: "")
            .apply()
        
        return newQuote
    }
    
    /**
     * Force refresh quote (bỏ qua cache)
     */
    fun refreshQuote(): QuoteDto {
        prefs.edit().clear().apply()
        return getTodayQuote()
    }
    
    companion object {
        private const val PREFS_NAME = "daily_quote_prefs"
        private const val KEY_QUOTE_DATE = "quote_date"
        private const val KEY_QUOTE_LANG = "quote_lang"
        private const val KEY_QUOTE_TEXT = "quote_text"
        private const val KEY_QUOTE_AUTHOR = "quote_author"
    }
}
