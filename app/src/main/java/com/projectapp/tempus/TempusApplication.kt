package com.projectapp.tempus

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.memory.MemoryCache
import coil.request.CachePolicy
import com.projectapp.tempus.data.user.UserProfileCache


class TempusApplication : Application(), ImageLoaderFactory {
    
    override fun onCreate() {
        super.onCreate()
        
        UserProfileCache.init(this)
        
        com.projectapp.tempus.ui.theme.ThemeManager.init(this)
        
        com.projectapp.tempus.ui.language.LanguageManager.init(this)
    }
    
    
    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .memoryCache {
                MemoryCache.Builder(this)
                    .maxSizePercent(0.25) 
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir.resolve("image_cache"))
                    .maxSizeBytes(50 * 1024 * 1024) 
                    .build()
            }
            
            .diskCachePolicy(CachePolicy.ENABLED)
            .memoryCachePolicy(CachePolicy.ENABLED)
            
            .networkCachePolicy(CachePolicy.ENABLED)
            .respectCacheHeaders(false) 
            .crossfade(true)
            .build()
    }
}
