package com.projectapp.tempus.data.quote

import android.content.Context
import android.content.SharedPreferences
import com.projectapp.tempus.data.quote.dto.QuoteDto
import java.time.LocalDate


class QuoteRepository(context: Context) {
    
    private val prefs: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME, 
        Context.MODE_PRIVATE
    )
    
    
    fun getTodayQuote(): QuoteDto {
        val today = LocalDate.now()
        val todayString = today.toString()
        
        
        val explicitLang = com.projectapp.tempus.data.user.UserProfileCache.getLanguage()
        val currentLanguage = explicitLang ?: java.util.Locale.getDefault().language
        val effectiveLanguage = if (currentLanguage == "vi") "vi" else "en" 
        
        
        val cachedDate = prefs.getString(KEY_QUOTE_DATE, null)
        val cachedText = prefs.getString(KEY_QUOTE_TEXT, null)
        val cachedAuthor = prefs.getString(KEY_QUOTE_AUTHOR, null)
        val cachedLang = prefs.getString(KEY_QUOTE_LANG, null)
        
        
        if (cachedDate == todayString && cachedText != null && cachedLang == effectiveLanguage) {
            return QuoteDto(
                text = cachedText,
                author = if (cachedAuthor.isNullOrBlank()) null else cachedAuthor
            )
        }
        
        
        val daySeed = today.year * 1000 + today.dayOfYear
        val newQuote = LocalQuotesProvider.getQuoteForSeed(daySeed, effectiveLanguage)
        
        
        prefs.edit()
            .putString(KEY_QUOTE_DATE, todayString)
            .putString(KEY_QUOTE_LANG, effectiveLanguage)
            .putString(KEY_QUOTE_TEXT, newQuote.text)
            .putString(KEY_QUOTE_AUTHOR, newQuote.author ?: "")
            .apply()
        
        return newQuote
    }
    
    
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
