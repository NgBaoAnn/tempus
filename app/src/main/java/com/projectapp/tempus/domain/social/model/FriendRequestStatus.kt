package com.projectapp.tempus.domain.social.model

/**
 * Trạng thái của lời mời kết bạn
 */
enum class FriendRequestStatus {
    PENDING,
    ACCEPTED,
    REJECTED;

    companion object {
        fun fromString(value: String): FriendRequestStatus {
            return when (value.lowercase()) {
                "pending" -> PENDING
                "accepted" -> ACCEPTED
                "rejected" -> REJECTED
                else -> PENDING
            }
        }
    }

    fun toDbValue(): String = name.lowercase()
}
