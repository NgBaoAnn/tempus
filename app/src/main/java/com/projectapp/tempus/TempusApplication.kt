package com.projectapp.tempus

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.memory.MemoryCache
import coil.request.CachePolicy
import com.projectapp.tempus.data.user.UserProfileCache

/**
 * Application class for Tempus
 * Configures Coil ImageLoader with disk caching for offline avatar support
 */
class TempusApplication : Application(), ImageLoaderFactory {
    
    override fun onCreate() {
        super.onCreate()
        // Initialize UserProfileCache
        UserProfileCache.init(this)
        // Initialize ThemeManager (loads saved theme preference)
        com.projectapp.tempus.ui.theme.ThemeManager.init(this)
        // Initialize LanguageManager
        com.projectapp.tempus.ui.language.LanguageManager.init(this)
    }
    
    /**
     * Create ImageLoader with disk cache enabled
     * Uses cache-first strategy for offline support
     */
    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .memoryCache {
                MemoryCache.Builder(this)
                    .maxSizePercent(0.25) // Use 25% of app's available memory
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir.resolve("image_cache"))
                    .maxSizeBytes(50 * 1024 * 1024) // 50 MB disk cache
                    .build()
            }
            // Use disk cache for offline support
            .diskCachePolicy(CachePolicy.ENABLED)
            .memoryCachePolicy(CachePolicy.ENABLED)
            // Read from cache first, then network
            .networkCachePolicy(CachePolicy.ENABLED)
            .respectCacheHeaders(false) // Ignore cache headers, always use our caching
            .crossfade(true)
            .build()
    }
}
