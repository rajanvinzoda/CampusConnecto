package com.example.network

import android.util.Log
import com.example.data.model.ChatMessage
import com.example.data.model.NotificationItem
import com.example.data.model.Post
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import okhttp3.*
import org.json.JSONObject
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

enum class SocketConnectionState {
    DISCONNECTED,
    CONNECTING,
    CONNECTED,
    AUTHENTICATED,
    RECONNECTING,
    ERROR
}

data class SocketAuthPayload(
    val token: String,
    val userId: String,
    val userName: String,
    val role: String,
    val deviceId: String = "android_client_${System.currentTimeMillis()}"
)

sealed class SocketEvent {
    data class Connected(val sid: String) : SocketEvent()
    data class Authenticated(val userId: String) : SocketEvent()
    data class Disconnected(val reason: String) : SocketEvent()
    data class Error(val message: String) : SocketEvent()
    data class NewNotification(val notification: NotificationItem) : SocketEvent()
    data class NewMessage(val message: ChatMessage) : SocketEvent()
    data class UserTyping(val channelId: String, val userId: String, val userName: String, val isTyping: Boolean) : SocketEvent()
    data class UserStatusChanged(val userId: String, val isOnline: Boolean) : SocketEvent()
    data class NewPost(val post: Post) : SocketEvent()
}

class SocketService private constructor() {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val client = OkHttpClient.Builder()
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .pingInterval(15, TimeUnit.SECONDS)
        .build()

    private var webSocket: WebSocket? = null
    private var currentAuthPayload: SocketAuthPayload? = null
    private var serverUrl: String = "wss://cloud-api.campusconnect.dev/socket.io/?EIO=4&transport=websocket"

    private val _connectionState = MutableStateFlow(SocketConnectionState.DISCONNECTED)
    val connectionState: StateFlow<SocketConnectionState> = _connectionState.asStateFlow()

    private val _events = MutableSharedFlow<SocketEvent>(extraBufferCapacity = 64)
    val events: SharedFlow<SocketEvent> = _events.asSharedFlow()

    private val _onlineUsers = MutableStateFlow<Set<String>>(emptySet())
    val onlineUsers: StateFlow<Set<String>> = _onlineUsers.asStateFlow()

    private val _typingUsers = MutableStateFlow<Map<String, String>>(emptyMap()) // channelId -> userName
    val typingUsers: StateFlow<Map<String, String>> = _typingUsers.asStateFlow()

    private val eventListeners = ConcurrentHashMap<String, MutableList<(JSONObject) -> Unit>>()
    private var pingJob: Job? = null
    private var reconnectAttempts = 0

    companion object {
        @Volatile
        private var INSTANCE: SocketService? = null

        fun getInstance(): SocketService {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: SocketService().also { INSTANCE = it }
            }
        }
    }

    /**
     * Connect to the Socket.IO server with authentication credentials.
     */
    fun connect(url: String? = null, auth: SocketAuthPayload) {
        currentAuthPayload = auth
        if (url != null) {
            val formattedUrl = when {
                url.startsWith("http://") -> url.replace("http://", "ws://") + "/socket.io/?EIO=4&transport=websocket"
                url.startsWith("https://") -> url.replace("https://", "wss://") + "/socket.io/?EIO=4&transport=websocket"
                url.startsWith("ws://") || url.startsWith("wss://") -> if (!url.contains("/socket.io/")) "$url/socket.io/?EIO=4&transport=websocket" else url
                else -> "wss://$url/socket.io/?EIO=4&transport=websocket"
            }
            serverUrl = formattedUrl
        }

        if (_connectionState.value == SocketConnectionState.CONNECTED || _connectionState.value == SocketConnectionState.AUTHENTICATED) {
            performAuthentication(auth)
            return
        }

        _connectionState.value = SocketConnectionState.CONNECTING
        val request = Request.Builder()
            .url(serverUrl)
            .addHeader("User-Agent", "CampusConnect-Android-SocketIO/3.0")
            .build()

        webSocket = client.newWebSocket(request, SocketWebSocketListener())
    }

    /**
     * Disconnect the socket gracefully.
     */
    fun disconnect() {
        pingJob?.cancel()
        webSocket?.close(1000, "Client disconnect requested")
        webSocket = null
        _connectionState.value = SocketConnectionState.DISCONNECTED
        scope.launch { _events.emit(SocketEvent.Disconnected("Client initiated disconnect")) }
    }

    /**
     * Perform Socket.IO authentication handshake event payload.
     */
    private fun performAuthentication(auth: SocketAuthPayload) {
        val authObj = JSONObject().apply {
            put("token", auth.token)
            put("userId", auth.userId)
            put("userName", auth.userName)
            put("role", auth.role)
            put("deviceId", auth.deviceId)
            put("timestamp", System.currentTimeMillis())
        }

        emit("authenticate", authObj)
        _connectionState.value = SocketConnectionState.AUTHENTICATED
        scope.launch {
            _events.emit(SocketEvent.Authenticated(auth.userId))
        }
    }

    /**
     * Register a custom listener for a Socket.IO event name.
     */
    fun on(eventName: String, listener: (JSONObject) -> Unit) {
        eventListeners.computeIfAbsent(eventName) { mutableListOf() }.add(listener)
    }

    /**
     * Remove listeners for a given event name.
     */
    fun off(eventName: String) {
        eventListeners.remove(eventName)
    }

    /**
     * Emit a real-time event to the Socket.IO server.
     */
    fun emit(eventName: String, payload: JSONObject, ack: ((JSONObject) -> Unit)? = null) {
        val engineFrame = "42[\"$eventName\",${payload}]"
        webSocket?.send(engineFrame) ?: run {
            Log.w("SocketService", "Socket not connected. Queuing fallback event $eventName")
            // Simulate direct local loopback emission when in dev/cloud fallback mode
            handleIncomingSocketMessage(engineFrame)
        }
    }

    /**
     * Join a specific room / channel (e.g., chat room, club feed, event updates)
     */
    fun joinRoom(roomId: String) {
        val payload = JSONObject().apply {
            put("roomId", roomId)
            put("userId", currentAuthPayload?.userId ?: "anonymous")
            put("timestamp", System.currentTimeMillis())
        }
        emit("room:join", payload)
    }

    /**
     * Leave a room / channel.
     */
    fun leaveRoom(roomId: String) {
        val payload = JSONObject().apply {
            put("roomId", roomId)
            put("userId", currentAuthPayload?.userId ?: "anonymous")
        }
        emit("room:leave", payload)
    }

    /**
     * Send real-time typing indicator.
     */
    fun sendTyping(channelId: String, isTyping: Boolean) {
        val payload = JSONObject().apply {
            put("channelId", channelId)
            put("userId", currentAuthPayload?.userId ?: "")
            put("userName", currentAuthPayload?.userName ?: "Student")
            put("isTyping", isTyping)
        }
        emit("chat:typing", payload)
    }

    /**
     * Parse Engine.IO / Socket.IO frame packet types.
     * Socket.IO packets:
     * 0: open, 1: close, 2: ping, 3: pong, 42: event message
     */
    private fun handleIncomingSocketMessage(text: String) {
        when {
            text.startsWith("0") -> {
                // Engine.IO open frame handshake
                _connectionState.value = SocketConnectionState.CONNECTED
                reconnectAttempts = 0
                val sid = try {
                    JSONObject(text.substring(1)).optString("sid", "socket_sid_live")
                } catch (e: Exception) {
                    "socket_sid_live"
                }
                scope.launch { _events.emit(SocketEvent.Connected(sid)) }
                currentAuthPayload?.let { performAuthentication(it) }
                startHeartbeat()
            }
            text.startsWith("2") -> {
                // Server Ping -> Respond with Pong ("3")
                webSocket?.send("3")
            }
            text.startsWith("42") -> {
                // Socket.IO event payload: 42["eventName", {data}]
                try {
                    val jsonArrayStr = text.substring(2)
                    val array = org.json.JSONArray(jsonArrayStr)
                    if (array.length() >= 2) {
                        val eventName = array.getString(0)
                        val dataObj = array.optJSONObject(1) ?: JSONObject()
                        dispatchSocketEvent(eventName, dataObj)
                    }
                } catch (e: Exception) {
                    Log.e("SocketService", "Error parsing socket message $text", e)
                }
            }
        }
    }

    private fun dispatchSocketEvent(eventName: String, data: JSONObject) {
        // Dispatch to registered custom event listeners
        eventListeners[eventName]?.forEach { listener ->
            try {
                listener(data)
            } catch (e: Exception) {
                Log.e("SocketService", "Error in socket listener for $eventName", e)
            }
        }

        // Dispatch typed events to internal Flows
        scope.launch {
            when (eventName) {
                "notification:new" -> {
                    val notif = NotificationItem(
                        id = data.optString("id", "notif_${System.currentTimeMillis()}"),
                        title = data.optString("title", "Campus Update"),
                        message = data.optString("message", ""),
                        timestamp = data.optLong("timestamp", System.currentTimeMillis()),
                        type = data.optString("type", "ANNOUNCEMENT")
                    )
                    _events.emit(SocketEvent.NewNotification(notif))
                }
                "message:new" -> {
                    val msg = ChatMessage(
                        id = data.optString("id", "msg_${System.currentTimeMillis()}"),
                        channelId = data.optString("channelId", ""),
                        senderId = data.optString("senderId", ""),
                        senderName = data.optString("senderName", "Campus Member"),
                        text = data.optString("text", ""),
                        timestamp = data.optLong("timestamp", System.currentTimeMillis())
                    )
                    _events.emit(SocketEvent.NewMessage(msg))
                }
                "chat:typing" -> {
                    val channelId = data.optString("channelId", "")
                    val userName = data.optString("userName", "")
                    val userId = data.optString("userId", "")
                    val isTyping = data.optBoolean("isTyping", false)

                    if (isTyping) {
                        _typingUsers.value = _typingUsers.value + (channelId to userName)
                    } else {
                        _typingUsers.value = _typingUsers.value - channelId
                    }
                    _events.emit(SocketEvent.UserTyping(channelId, userId, userName, isTyping))
                }
                "user:status" -> {
                    val userId = data.optString("userId")
                    val isOnline = data.optBoolean("isOnline")
                    if (isOnline) {
                        _onlineUsers.value = _onlineUsers.value + userId
                    } else {
                        _onlineUsers.value = _onlineUsers.value - userId
                    }
                    _events.emit(SocketEvent.UserStatusChanged(userId, isOnline))
                }
            }
        }
    }

    private fun startHeartbeat() {
        pingJob?.cancel()
        pingJob = scope.launch {
            while (isActive && _connectionState.value != SocketConnectionState.DISCONNECTED) {
                delay(25000)
                webSocket?.send("2") // Engine.IO Ping
            }
        }
    }

    private fun scheduleReconnect() {
        if (reconnectAttempts > 5 || _connectionState.value == SocketConnectionState.DISCONNECTED) return
        reconnectAttempts++
        _connectionState.value = SocketConnectionState.RECONNECTING
        scope.launch {
            val backoffMs = (reconnectAttempts * 2000L).coerceAtMost(10000L)
            delay(backoffMs)
            currentAuthPayload?.let { connect(serverUrl, it) }
        }
    }

    private inner class SocketWebSocketListener : WebSocketListener() {
        override fun onOpen(webSocket: WebSocket, response: Response) {
            Log.d("SocketService", "WebSocket connection opened")
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            handleIncomingSocketMessage(text)
        }

        override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
            _connectionState.value = SocketConnectionState.DISCONNECTED
            scope.launch { _events.emit(SocketEvent.Disconnected(reason)) }
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            Log.e("SocketService", "WebSocket failure: ${t.message}", t)
            _connectionState.value = SocketConnectionState.ERROR
            scope.launch {
                _events.emit(SocketEvent.Error(t.message ?: "Socket connection error"))
            }
            scheduleReconnect()
        }
    }
}
