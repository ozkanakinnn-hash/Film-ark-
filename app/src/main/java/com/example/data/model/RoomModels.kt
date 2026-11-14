package com.example.data.model

enum class RoomStatus {
    WAITING_FOR_PARTNER, // Waiting for second participant
    FILTERING,           // Both selecting moods and platforms
    READY_TO_SPIN,       // Common pool generated, ready for wheel
    SPINNING,            // Wheel currently rotating
    RESULT_REVEALED,     // Movie chosen, veto option available
    VETOED,              // Movie was vetoed, spinning again
    FINAL_DECISION       // Decision locked, no more vetoes allowed
}

data class Participant(
    val id: String,
    val name: String,
    val isHost: Boolean,
    val selectedMoods: Set<String> = emptySet(),
    val selectedPlatforms: Set<String> = emptySet(),
    val hasVetoed: Boolean = false,
    val isReady: Boolean = false
)

data class Room(
    val id: String,
    val roomCode: String,
    val hostId: String,
    val status: RoomStatus = RoomStatus.WAITING_FOR_PARTNER,
    val host: Participant,
    val guest: Participant? = null,
    val eligibleMovieIds: List<String> = emptyList(),
    val selectedMovie: Movie? = null,
    val vetoHistory: List<String> = emptyList(), // Ids of vetoed movies
    val targetRotationAngle: Float = 0f,
    val spinDurationMs: Long = 4200L,
    val createdAt: Long = System.currentTimeMillis()
)

sealed class RoomEvent {
    data class JoinRoom(val roomCode: String, val guestName: String) : RoomEvent()
    data class UpdateFilters(val participantId: String, val moods: Set<String>, val platforms: Set<String>) : RoomEvent()
    data class ToggleReady(val participantId: String) : RoomEvent()
    data class SpinWheel(val targetMovieId: String, val targetAngle: Float, val durationMs: Long) : RoomEvent()
    data class VetoMovie(val participantId: String) : RoomEvent()
    object ResetSession : RoomEvent()
}
