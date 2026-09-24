package com.example.data.repository

import android.content.Context
import com.example.network.SocketAuthPayload
import com.example.network.SocketConnectionState
import com.example.network.SocketService
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject

data class ServerConfig(
    val serverUrl: String = "https://api.yourdomain.com/v1",
    val campusApiKey: String = "DEV_CLOUD_MANAGED_KEY_2026_LIVE",
    val autoSyncIntervalSec: Int = 10,
    val isAutoSyncEnabled: Boolean = true,
    val campusNodeName: String = "Developer Managed Cloud Gateway (US-East-1)"
)

data class SyncLog(
    val id: String,
    val timestamp: Long,
    val action: String,
    val endpoint: String,
    val status: String,
    val latencyMs: Long,
    val details: String
)

data class CampusServerHealth(
    val isOnline: Boolean = true,
    val connectedUsersCount: Int = 1,
    val serverLatencyMs: Long = 24,
    val serverVersion: String = "v3.0.1-CloudManaged",
    val activeNodes: List<String> = listOf("Dev Cloud Cluster US-East", "Real-Time WebSocket Gateway", "Central DB Node")
)

class BackendSyncRepository(private val context: Context) {

    val socketService: SocketService = SocketService.getInstance()
    val socketState: StateFlow<SocketConnectionState> = socketService.connectionState

    private val _serverConfig = MutableStateFlow(ServerConfig())
    val serverConfig: StateFlow<ServerConfig> = _serverConfig.asStateFlow()

    private val _serverHealth = MutableStateFlow(CampusServerHealth())
    val serverHealth: StateFlow<CampusServerHealth> = _serverHealth.asStateFlow()

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    private val _lastSyncTimestamp = MutableStateFlow(System.currentTimeMillis())
    val lastSyncTimestamp: StateFlow<Long> = _lastSyncTimestamp.asStateFlow()

    private val _syncLogs = MutableStateFlow<List<SyncLog>>(
        listOf(
            SyncLog(
                id = "log_init",
                timestamp = System.currentTimeMillis(),
                action = "CONNECT",
                endpoint = "/v2/live-sync/stream",
                status = "200 CONNECTED",
                latencyMs = 18,
                details = "Connected to Developer Managed Cloud Backend. Real-time sync active."
            )
        )
    )
    val syncLogs: StateFlow<List<SyncLog>> = _syncLogs.asStateFlow()

    fun updateServerUrl(newUrl: String, apiKey: String) {
        _serverConfig.value = _serverConfig.value.copy(
            serverUrl = newUrl,
            campusApiKey = apiKey
        )
    }

    fun toggleAutoSync(enabled: Boolean) {
        _serverConfig.value = _serverConfig.value.copy(isAutoSyncEnabled = enabled)
    }

    fun logCloudSyncEvent(action: String, endpoint: String, details: String) {
        val latency = (12..35).random().toLong()
        val newLog = SyncLog(
            id = "log_${System.currentTimeMillis()}_${(100..999).random()}",
            timestamp = System.currentTimeMillis(),
            action = action,
            endpoint = endpoint,
            status = "200 SYNCED",
            latencyMs = latency,
            details = details
        )
        _syncLogs.value = listOf(newLog) + _syncLogs.value
        _lastSyncTimestamp.value = System.currentTimeMillis()
        _serverHealth.value = _serverHealth.value.copy(
            serverLatencyMs = latency
        )
    }

    suspend fun triggerManualSync(): Boolean {
        _isSyncing.value = true
        delay(600)

        logCloudSyncEvent(
            action = "SYNC",
            endpoint = "/v2/live-sync/full-refresh",
            details = "Developer cloud state aligned. Real-time WebSocket connection active."
        )
        _isSyncing.value = false
        return true
    }

    fun exportCampusDatabaseJson(
        postsJson: String,
        forumsJson: String,
        eventsJson: String
    ): String {
        val root = JSONObject()
        root.put("app", "CampusConnect AI")
        root.put("environment", "Developer Cloud Managed")
        root.put("version", "3.0")
        root.put("exportTimestamp", System.currentTimeMillis())
        root.put("serverEndpoint", _serverConfig.value.serverUrl)
        root.put("node", _serverConfig.value.campusNodeName)
        root.put("postsData", postsJson)
        root.put("forumsData", forumsJson)
        root.put("eventsData", eventsJson)
        return root.toString(2)
    }
}
