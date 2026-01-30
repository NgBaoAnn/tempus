package com.projectapp.tempus.util

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

object TimerEventBus {
    
    enum class TimerEvent {
        PAUSE,
        RESUME,
        STOP
    }

    private val _events = MutableSharedFlow<TimerEvent>(extraBufferCapacity = 1)
    val events = _events.asSharedFlow()

    suspend fun emit(event: TimerEvent) {
        _events.emit(event)
    }
    
    
    fun tryEmit(event: TimerEvent) {
        _events.tryEmit(event)
    }
}
