package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.data.local.AppDatabase
import com.example.data.local.MovieSelectionCountProjection
import com.example.data.local.SavedDecisionDao
import com.example.data.local.toEntity
import com.example.data.model.HistoryDashboardSummary
import com.example.data.model.Movie
import com.example.data.model.Participant
import com.example.data.model.Room
import com.example.data.model.RoomStatus
import com.example.data.model.analyzeDecisionHistory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlin.random.Random

data class SavedDecision(
    val id: String,
    val movie: Movie,
    val dateText: String,
    val minutesSaved: Int = 45,
    val mode: String, // "Karar Çarkı" (eski: "Tek Başına"), "Özel Çark (...)", "Film Karşılaştır (VS)"
    val selectedAtMillis: Long = System.currentTimeMillis()
)

object RoomRepository {

    private const val PREF_NAME = "ne_izlesek_saved_decisions_prefs"

    // Yazımlar sırayla: art arda yapılan işlemler (ör. izlendi + not) birbirini ezmesin
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    private val ioScope = CoroutineScope(SupervisorJob() + Dispatchers.IO.limitedParallelism(1))

    @Volatile
    private var dao: SavedDecisionDao? = null

    @Volatile
    private var prefs: SharedPreferences? = null

    private val _currentRoom = MutableStateFlow<Room?>(null)
    val currentRoom: StateFlow<Room?> = _currentRoom.asStateFlow()

    private val _savedDecisions = MutableStateFlow<List<SavedDecision>>(emptyList())
    val savedDecisions: StateFlow<List<SavedDecision>> = _savedDecisions.asStateFlow()

    private val _historyDashboardStats = MutableStateFlow(analyzeDecisionHistory(_savedDecisions.value))
    val historyDashboardStats: StateFlow<HistoryDashboardSummary> = _historyDashboardStats.asStateFlow()

    private fun updateSavedDecisionsState(updatedList: List<SavedDecision>) {
        _savedDecisions.value = updatedList
        _historyDashboardStats.value = analyzeDecisionHistory(updatedList)
    }

    fun observeDatabaseMostSelectedMovies(): Flow<List<MovieSelectionCountProjection>> {
        return dao?.observeMostSelectedMovies() ?: flowOf(emptyList())
    }

    fun observeDatabaseDashboardSummary(): Flow<HistoryDashboardSummary> {
        val localDao = dao ?: return historyDashboardStats
        return combine(
            localDao.getAllDecisions(),
            localDao.observeMostSelectedMovies()
        ) { entities, _ ->
            analyzeDecisionHistory(entities.map { it.toDomain() })
        }
    }

    fun init(context: Context) {
        if (prefs == null) {
            prefs = context.applicationContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        }
        if (dao != null) return
        val localDao = AppDatabase.getInstance(context).savedDecisionDao()
        dao = localDao

        ioScope.launch {
            // Eski sürümler ilk açılışta örnek (gerçek olmayan) iki karar ekliyordu; onları temizle
            val persisted = localDao.getAllDecisionsOnce().filter { it.id !in LEGACY_SAMPLE_IDS }
            LEGACY_SAMPLE_IDS.forEach { runCatching { localDao.deleteDecisionById(it) } }
            val decoded = persisted.map { it.toDomain() }
            val inMemoryPending = _savedDecisions.value.filter { mem -> decoded.none { it.id == mem.id } }
            val merged = (inMemoryPending + decoded)
                .distinctBy { it.id }
                .sortedByDescending { it.selectedAtMillis }
            updateSavedDecisionsState(merged)
            if (inMemoryPending.isNotEmpty()) {
                localDao.upsertDecisions(inMemoryPending.map { it.toEntity() })
            }
        }
    }

    private val LEGACY_SAMPLE_IDS = listOf("s1", "s2")





    fun prepareSoloSpin(
        moods: Set<String>,
        platforms: Set<String>,
        customMovies: List<Movie>? = null,
        excludedMovieIds: Set<String> = WatchedMovieRepository.excludedWheelMovieIds.value
    ): Room {
        val host = Participant(
            id = "solo_user",
            name = "Sen",
            isHost = true,
            selectedMoods = moods,
            selectedPlatforms = platforms,
            isReady = true
        )
        val filteredCustom = customMovies?.filterNot { excludedMovieIds.contains(it.id) }
        val eligible = (filteredCustom?.takeIf { it.isNotEmpty() }
            ?: MovieRepository.filterMovies(moods, platforms, excludedMovieIds)).map { it.id }
        val room = Room(
            id = "solo_${System.currentTimeMillis()}",
            roomCode = "SOLO",
            hostId = host.id,
            status = RoomStatus.READY_TO_SPIN,
            host = host,
            guest = null,
            eligibleMovieIds = eligible
        )
        _currentRoom.value = room
        return room
    }


    fun excludeMovieFromCurrentRoom(movieId: String) {
        val room = _currentRoom.value ?: return
        val excluded = room.vetoHistory + WatchedMovieRepository.excludedWheelMovieIds.value + movieId
        val remaining = room.eligibleMovieIds.filterNot { it == movieId }
            .ifEmpty {
                MovieRepository.allMovies
                    .filterNot { excluded.contains(it.id) }
                    .ifEmpty { MovieRepository.allMovies }
                    .take(8)
                    .map { it.id }
            }
        _currentRoom.value = room.copy(eligibleMovieIds = remaining)
    }

    /**
     * Kazananı seçer ve ibreyi o dilimin ortasına getirecek açıyı hesaplar.
     *
     * [wheelSlices] verilirse kazanan MUTLAKA bu listeden (ekranda çizilen dilimlerden, aynı sırayla) seçilir;
     * böylece ibrenin durduğu dilim ile açılan film her zaman aynıdır.
     */
    fun triggerSpin(
        excludedMovieIds: Set<String> = WatchedMovieRepository.excludedWheelMovieIds.value,
        wheelSlices: List<Movie>? = null
    ): Pair<Movie, Float>? {
        val room = _currentRoom.value ?: return null
        val excluded = room.vetoHistory + excludedMovieIds
        val eligibleMovies = wheelSlices?.takeIf { it.isNotEmpty() } ?: if (room.eligibleMovieIds.isNotEmpty()) {
            room.eligibleMovieIds.mapNotNull {
                MovieRepository.getMovieById(it) ?: CustomWheelListRepository.findCustomMovieById(it)
            }
        } else {
            MovieRepository.allMovies
        }.filterNot { excluded.contains(it.id) }
            .ifEmpty {
                MovieRepository.allMovies
                    .filterNot { excluded.contains(it.id) }
                    .ifEmpty { MovieRepository.allMovies }
                    .take(8)
            }

        val winnerIndex = Random.nextInt(eligibleMovies.size)
        val winner = eligibleMovies[winnerIndex]

        // Calculate angle: Slice size = 360f / eligibleMovies.size
        val sliceDegrees = 360f / eligibleMovies.size
        // We want the wheel to spin at least 5-7 full turns (1800 - 2520 deg) plus slice offset
        val extraTurns = Random.nextInt(5, 8) * 360f
        // Needle is at top (0 deg offset). Winner slice center must align with pointer:
        val winnerAngleOffset = (winnerIndex * sliceDegrees) + (sliceDegrees / 2f)
        val finalAngle = extraTurns + (360f - winnerAngleOffset)

        _currentRoom.value = room.copy(
            status = RoomStatus.SPINNING,
            selectedMovie = winner,
            targetRotationAngle = finalAngle
        )

        return Pair(winner, finalAngle)
    }

    fun finishSpin(modeOverride: String? = null) {
        val room = _currentRoom.value ?: return
        val winner = room.selectedMovie ?: return
        val isFinal = room.vetoHistory.isNotEmpty()
        _currentRoom.value = room.copy(
            status = if (isFinal) RoomStatus.FINAL_DECISION else RoomStatus.RESULT_REVEALED
        )

        val resolvedMode = modeOverride?.takeIf { it.isNotBlank() }
            ?: "Karar Çarkı"
        recordSavedDecision(movie = winner, mode = resolvedMode)
    }

    fun applyVeto(
        participantId: String,
        excludedMovieIds: Set<String> = WatchedMovieRepository.excludedWheelMovieIds.value
    ): Boolean {
        val room = _currentRoom.value ?: return false
        val currentWinner = room.selectedMovie ?: return false

        val updatedHost = if (room.host.id == participantId) room.host.copy(hasVetoed = true) else room.host
        val updatedGuest = if (room.guest?.id == participantId) room.guest.copy(hasVetoed = true) else room.guest

        val newVetoHistory = room.vetoHistory + currentWinner.id
        val excluded = newVetoHistory + excludedMovieIds
        val updatedEligible = room.eligibleMovieIds
            .filterNot { excluded.contains(it) }
            .ifEmpty {
                MovieRepository.allMovies
                    .filterNot { excluded.contains(it.id) }
                    .ifEmpty { MovieRepository.allMovies.filter { it.id != currentWinner.id } }
                    .take(6)
                    .map { it.id }
            }

        _currentRoom.value = room.copy(
            host = updatedHost,
            guest = updatedGuest,
            status = RoomStatus.VETOED,
            vetoHistory = newVetoHistory,
            eligibleMovieIds = updatedEligible
        )
        return true
    }

    fun selectAlternativeMovie(movie: Movie) {
        val room = _currentRoom.value ?: return
        _currentRoom.value = room.copy(
            selectedMovie = movie,
            status = if (room.vetoHistory.isNotEmpty()) RoomStatus.FINAL_DECISION else RoomStatus.RESULT_REVEALED
        )
    }

    fun recordSavedDecision(movie: Movie, mode: String, minutesSaved: Int = 45) {
        val now = System.currentTimeMillis()
        val timeText = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(now))
        val newSaved = SavedDecision(
            id = "sd_${now}_${UUID.randomUUID().toString().take(4)}",
            movie = movie,
            dateText = "Bugün • $timeText",
            minutesSaved = minutesSaved,
            mode = mode,
            selectedAtMillis = now
        )
        updateSavedDecisionsState(listOf(newSaved) + _savedDecisions.value)
        dao?.let { localDao ->
            ioScope.launch {
                localDao.upsertDecision(newSaved.toEntity())
            }
        }
    }

    fun deleteSavedDecision(decisionId: String) {
        updateSavedDecisionsState(_savedDecisions.value.filterNot { it.id == decisionId })
        dao?.let { localDao ->
            ioScope.launch {
                localDao.deleteDecisionById(decisionId)
            }
        }
    }

    fun clearSavedHistory() {
        updateSavedDecisionsState(emptyList())
        dao?.let { localDao ->
            ioScope.launch {
                localDao.clearAll()
            }
        }
    }

    /** Yedekten geri yüklemeden sonra karar geçmişini veritabanından yeniden okur. */
    suspend fun reloadAfterRestore() {
        val localDao = dao ?: return
        updateSavedDecisionsState(
            localDao.getAllDecisionsOnce().map { it.toDomain() }.sortedByDescending { it.selectedAtMillis }
        )
    }

    fun resetSavedDecisionsForTests() {
        updateSavedDecisionsState(emptyList())
        prefs?.edit()?.clear()?.apply()
        // Yalnızca testlerden çağrılır: sıfırlama bitmeden dönme ki testlerle yarış durumu oluşmasın
        dao?.let { localDao ->
            kotlinx.coroutines.runBlocking(kotlinx.coroutines.Dispatchers.IO) {
                localDao.clearAll()
            }
        }
    }

    fun clearRoom() {
        _currentRoom.value = null
    }
}
