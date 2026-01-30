package com.projectapp.tempus.data

import android.content.Context
import com.projectapp.tempus.data.gamification.GamificationDatabase
import com.projectapp.tempus.data.gamification.GamificationRepository
import com.projectapp.tempus.data.gamification.LocalGamificationRepository
import com.projectapp.tempus.data.gamification.OfflineFirstGamificationRepository
import com.projectapp.tempus.data.gamification.SupabaseGamificationRepository
import com.projectapp.tempus.data.local.LocalScheduleRepository
import com.projectapp.tempus.data.local.TempusDatabase
import com.projectapp.tempus.data.notes.NotesRepository
import com.projectapp.tempus.data.notes.SupabaseNotesRepository
import com.projectapp.tempus.data.schedule.OfflineFirstScheduleRepository
import com.projectapp.tempus.data.schedule.ScheduleRepository
import com.projectapp.tempus.data.schedule.SupabaseScheduleRepository
import com.projectapp.tempus.data.sync.GamificationSyncManager
import com.projectapp.tempus.data.sync.NotesSyncManager
import com.projectapp.tempus.data.sync.SyncManager


object RepositoryProvider {
    
    
    @Volatile
    private var scheduleRepository: OfflineFirstScheduleRepository? = null
    
    @Volatile
    private var localRepository: LocalScheduleRepository? = null
    
    @Volatile
    private var remoteRepository: SupabaseScheduleRepository? = null
    
    @Volatile
    private var syncManager: SyncManager? = null
    
    
    @Volatile
    private var gamificationRepository: OfflineFirstGamificationRepository? = null
    
    @Volatile
    private var localGamificationRepository: LocalGamificationRepository? = null
    
    @Volatile
    private var remoteGamificationRepository: SupabaseGamificationRepository? = null
    
    @Volatile
    private var gamificationSyncManager: GamificationSyncManager? = null
    
    
    @Volatile
    private var notesRepository: NotesRepository? = null
    
    @Volatile
    private var remoteNotesRepository: SupabaseNotesRepository? = null
    
    @Volatile
    private var notesSyncManager: NotesSyncManager? = null
    
    
    fun getScheduleRepository(context: Context): OfflineFirstScheduleRepository {
        return scheduleRepository ?: synchronized(this) {
            scheduleRepository ?: createScheduleRepository(context).also {
                scheduleRepository = it
            }
        }
    }
    
    
    fun getLocalRepository(context: Context): LocalScheduleRepository {
        return localRepository ?: synchronized(this) {
            localRepository ?: createLocalRepository(context).also {
                localRepository = it
            }
        }
    }
    
    
    fun getRemoteRepository(): SupabaseScheduleRepository {
        return remoteRepository ?: synchronized(this) {
            remoteRepository ?: SupabaseScheduleRepository().also {
                remoteRepository = it
            }
        }
    }
    
    
    fun getSyncManager(context: Context): SyncManager {
        return syncManager ?: synchronized(this) {
            syncManager ?: createSyncManager(context).also {
                syncManager = it
            }
        }
    }
    
    
    fun getGamificationRepository(context: Context): GamificationRepository {
        return gamificationRepository ?: synchronized(this) {
            gamificationRepository ?: createGamificationRepository(context).also {
                gamificationRepository = it
            }
        }
    }
    
    
    fun getLocalGamificationRepository(context: Context): LocalGamificationRepository {
        return localGamificationRepository ?: synchronized(this) {
            localGamificationRepository ?: createLocalGamificationRepository(context).also {
                localGamificationRepository = it
            }
        }
    }
    
    
    fun getRemoteGamificationRepository(): SupabaseGamificationRepository {
        return remoteGamificationRepository ?: synchronized(this) {
            remoteGamificationRepository ?: SupabaseGamificationRepository().also {
                remoteGamificationRepository = it
            }
        }
    }
    
    
    fun getGamificationSyncManager(context: Context): GamificationSyncManager {
        return gamificationSyncManager ?: synchronized(this) {
            gamificationSyncManager ?: createGamificationSyncManager(context).also {
                gamificationSyncManager = it
            }
        }
    }
    
    
    fun getNotesRepository(context: Context): NotesRepository {
        return notesRepository ?: synchronized(this) {
            notesRepository ?: NotesRepository(context).also {
                notesRepository = it
            }
        }
    }
    
    
    fun getRemoteNotesRepository(): SupabaseNotesRepository {
        return remoteNotesRepository ?: synchronized(this) {
            remoteNotesRepository ?: SupabaseNotesRepository().also {
                remoteNotesRepository = it
            }
        }
    }
    
    
    fun getNotesSyncManager(context: Context): NotesSyncManager {
        return notesSyncManager ?: synchronized(this) {
            notesSyncManager ?: createNotesSyncManager(context).also {
                notesSyncManager = it
            }
        }
    }
    
    
    fun clear() {
        scheduleRepository = null
        localRepository = null
        syncManager = null
        gamificationRepository = null
        localGamificationRepository = null
        gamificationSyncManager = null
        notesRepository = null
        notesSyncManager = null
        
    }
    
    
    private fun createScheduleRepository(context: Context): OfflineFirstScheduleRepository {
        val localRepo = getLocalRepository(context)
        return OfflineFirstScheduleRepository(context.applicationContext, localRepo)
    }
    
    private fun createLocalRepository(context: Context): LocalScheduleRepository {
        val database = TempusDatabase.getDatabase(context)
        return LocalScheduleRepository(database.scheduleDao())
    }
    
    private fun createSyncManager(context: Context): SyncManager {
        val localRepo = getLocalRepository(context)
        val remoteRepo = getRemoteRepository()
        return SyncManager(localRepo, remoteRepo)
    }
    
    private fun createGamificationRepository(context: Context): OfflineFirstGamificationRepository {
        val localRepo = getLocalGamificationRepository(context)
        return OfflineFirstGamificationRepository(localRepo)
    }
    
    private fun createLocalGamificationRepository(context: Context): LocalGamificationRepository {
        
        val database = TempusDatabase.getDatabase(context)
        return LocalGamificationRepository(database.gamificationDao())
    }
    
    private fun createGamificationSyncManager(context: Context): GamificationSyncManager {
        val localRepo = getLocalGamificationRepository(context)
        val remoteRepo = getRemoteGamificationRepository()
        return GamificationSyncManager(localRepo, remoteRepo)
    }
    
    private fun createNotesSyncManager(context: Context): NotesSyncManager {
        val localRepo = getNotesRepository(context)
        val remoteRepo = getRemoteNotesRepository()
        return NotesSyncManager(localRepo, remoteRepo)
    }
}
