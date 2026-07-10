package com.titan.feature.ai.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.titan.domain.model.AIChat
import com.titan.domain.model.AIChatMessage
import com.titan.domain.model.MessageRole

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AIChatScreen(
    onNavigateBack: () -> Unit,
    viewModel: AIChatViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var inputText by remember { mutableStateOf("") }
    var showChatList by remember { mutableStateOf(true) }
    val listState = rememberLazyListState()

    LaunchedEffect(state.messages.size) {
        if (state.messages.isNotEmpty()) {
            listState.animateScrollToItem(state.messages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { showChatList = !showChatList }) {
                            Icon(Icons.Default.Menu, contentDescription = "Chat list")
                        }
                        Column {
                            Text("AI Assistant", fontWeight = FontWeight.Bold)
                            if (state.activeProviderName.isNotBlank()) {
                                Text(state.activeProviderName + " - " + state.activeModel, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") }
                },
                actions = {
                    IconButton(onClick = { viewModel.createNewChat() }) { Icon(Icons.Default.Add, contentDescription = "New Chat") }
                    IconButton(onClick = { viewModel.toggleProviderSelector() }) { Icon(Icons.Default.SwapHoriz, contentDescription = "Switch Provider") }
                },
            )
        },
    ) { paddingValues ->
        Row(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            AnimatedVisibility(visible = showChatList) {
                ChatListSidebar(
                    chats = state.chats,
                    activeChatId = state.activeChatId,
                    onChatSelected = { viewModel.selectChat(it) },
                    onNewChat = { viewModel.createNewChat() },
                    onDeleteChat = { viewModel.deleteChat(it) },
                    onPinChat = { viewModel.togglePinChat(it) },
                    modifier = Modifier.width(280.dp),
                )
            }
            Divider(modifier = Modifier.fillMaxHeight().width(1.dp))
            Column(modifier = Modifier.weight(1f)) {
                if (state.showProviderSelector) {
                    ProviderSelectorBanner(
                        providers = state.providers,
                        activeProviderId = state.activeProviderId,
                        onSelectProvider = { viewModel.selectProvider(it) },
                        onDismiss = { viewModel.toggleProviderSelector() },
                    )
                }
                if (state.messages.isEmpty()) {
                    EmptyChatState(
                        suggestions = listOf(
                            "Generate a Jetpack Compose screen",
                            "Explain this code",
                            "Write unit tests",
                            "Fix build errors",
                            "Generate documentation",
                            "Refactor code",
                        ),
                        onSuggestionClick = { inputText = it },
                    )
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        items(state.messages, key = { it.id }) { message ->
                            ChatMessageBubble(message = message)
                        }
                        if (state.isLoading) {
                            item {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(8.dp)) {
                                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Generating response...", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
                Surface(
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                    tonalElevation = 2.dp,
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.Bottom,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            OutlinedTextField(
                                value = inputText,
                                onValueChange = { inputText = it },
                                modifier = Modifier.weight(1f).heightIn(min = 48.dp, max = 120.dp),
                                placeholder = { Text("Ask AI anything...") },
                                maxLines = 5,
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.primary),
                            )
                            Column {
                                FilledIconButton(
                                    onClick = {
                                        if (inputText.isNotBlank()) {
                                            viewModel.sendMessage(inputText)
                                            inputText = ""
                                        }
                                    },
                                    enabled = inputText.isNotBlank() && !state.isLoading,
                                    modifier = Modifier.size(48.dp),
                                ) {
                                    Icon(Icons.Default.Send, contentDescription = "Send")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ChatListSidebar(
    chats: List<AIChat>,
    activeChatId: String,
    onChatSelected: (String) -> Unit,
    onNewChat: () -> Unit,
    onDeleteChat: (String) -> Unit,
    onPinChat: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(color = MaterialTheme.colorScheme.surfaceContainerLow, modifier = modifier.fillMaxHeight()) {
        Column {
            TextButton(onClick = onNewChat, modifier = Modifier.fillMaxWidth().padding(8.dp)) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("New Chat", style = MaterialTheme.typography.labelLarge)
            }
            HorizontalDivider()
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(vertical = 4.dp),
            ) {
                items(chats, key = { it.id }) { chat ->
                    val isActive = chat.id == activeChatId
                    Surface(
                        modifier = Modifier.fillMaxWidth().clickable { onChatSelected(chat.id) },
                        color = if (isActive) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp).fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (chat.isPinned) {
                                        Icon(Icons.Default.PushPin, contentDescription = null, modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.primary)
                                        Spacer(modifier = Modifier.width(4.dp))
                                    }
                                    Text(
                                        chat.title,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                }
                                Text(
                                    formatRelativeTime(chat.updatedAt),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ChatMessageBubble(message: AIChatMessage) {
    val isUser = message.role == MessageRole.USER
    val isError = message.isError

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
    ) {
        if (!isUser) {
            Surface(
                modifier = Modifier.size(32.dp),
                shape = MaterialTheme.shapes.small,
                color = MaterialTheme.colorScheme.tertiaryContainer,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.SmartToy, contentDescription = null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.tertiary)
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
        }
        Surface(
            shape = MaterialTheme.shapes.medium,
            color = when {
                isError -> MaterialTheme.colorScheme.errorContainer
                isUser -> MaterialTheme.colorScheme.primaryContainer
                else -> MaterialTheme.colorScheme.surfaceVariant
            },
            modifier = Modifier.widthIn(max = 400.dp).fillMaxWidth(if (isUser) 0.85f else 1f),
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    message.content,
                    style = MaterialTheme.typography.bodyMedium,
                    color = when {
                        isError -> MaterialTheme.colorScheme.error
                        isUser -> MaterialTheme.colorScheme.onPrimaryContainer
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    },
                )
                if (message.tokensUsed > 0) {
                    Text(
                        "${message.tokensUsed} tokens",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.padding(top = 4.dp).align(Alignment.End),
                    )
                }
            }
        }
        if (isUser) Spacer(modifier = Modifier.width(8.dp))
    }
}

@Composable
fun EmptyChatState(suggestions: List<String>, onSuggestionClick: (String) -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Outlined.SmartToy, contentDescription = null, modifier = Modifier.size(80.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
            Spacer(modifier = Modifier.height(16.dp))
            Text("How can I help you?", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(24.dp))
            suggestions.chunked(2).forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    row.forEach { suggestion ->
                        SuggestionChip(
                            onClick = { onSuggestionClick(suggestion) },
                            label = { Text(suggestion, style = MaterialTheme.typography.labelSmall) },
                            modifier = Modifier.padding(4.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ProviderSelectorBanner(providers: List<com.titan.domain.model.AIProvider>, activeProviderId: String, onSelectProvider: (String) -> Unit, onDismiss: () -> Unit) {
    Surface(color = MaterialTheme.colorScheme.surfaceContainerHigh, tonalElevation = 4.dp) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Text("Select AI Provider", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, contentDescription = "Close") }
            }
            Spacer(modifier = Modifier.height(8.dp))
            LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                items(providers) { provider ->
                    val isActive = provider.id == activeProviderId
                    Card(
                        modifier = Modifier.fillMaxWidth().clickable { onSelectProvider(provider.id) },
                        colors = CardDefaults.cardColors(containerColor = if (isActive) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface),
                        border = if (isActive) androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary) else null,
                    ) {
                        Row(modifier = Modifier.padding(12.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(provider.name, fontWeight = FontWeight.Medium)
                                Text("${provider.providerType.displayName} - ${provider.model}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            if (isActive) Icon(Icons.Default.Check, contentDescription = "Active", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }
    }
}

data class AIChatUiState(
    val chats: List<AIChat> = emptyList(),
    val messages: List<AIChatMessage> = emptyList(),
    val activeChatId: String = "",
    val providers: List<com.titan.domain.model.AIProvider> = emptyList(),
    val activeProviderId: String = "",
    val activeProviderName: String = "",
    val activeModel: String = "",
    val isLoading: Boolean = false,
    val showProviderSelector: Boolean = false,
    val error: String? = null,
) : com.titan.core.common.base.UiState

@HiltViewModel
class AIChatViewModel @Inject constructor(
    private val aiRepository: com.titan.domain.repository.AIRepository,
) : com.titan.core.common.base.BaseViewModel<AIChatUiState, com.titan.core.common.base.UiEvent, com.titan.core.common.base.UiAction>() {

    init {
        viewModelScope.launch {
            aiRepository.getChatHistory().collect { chats -> setState { copy(chats = chats) } }
        }
        viewModelScope.launch {
            aiRepository.getActiveProvider().collect { provider ->
                if (provider != null) {
                    setState { copy(activeProviderId = provider.id, activeProviderName = provider.name, activeModel = provider.model) }
                }
            }
        }
        viewModelScope.launch {
            aiRepository.getAllProviders().collect { providers -> setState { copy(providers = providers) } }
        }
    }

    override fun initialState() = AIChatUiState()

    override fun onEvent(event: com.titan.core.common.base.UiEvent) {}

    fun createNewChat() {
        viewModelScope.launch {
            val result = aiRepository.createChat("New Chat")
            if (result.isSuccess) {
                val chat = result.getOrNull()!!
                setState { copy(activeChatId = chat.id) }
                aiRepository.getChatMessages(chat.id).collect { messages ->
                    setState { copy(messages = messages) }
                }
            }
        }
    }

    fun selectChat(chatId: String) {
        setState { copy(activeChatId = chatId, messages = emptyList()) }
        viewModelScope.launch {
            aiRepository.getChatMessages(chatId).collect { messages -> setState { copy(messages = messages) } }
        }
    }

    fun deleteChat(chatId: String) {
        viewModelScope.launch { aiRepository.deleteChat(chatId) }
    }

    fun togglePinChat(chatId: String) {
        viewModelScope.launch { aiRepository.togglePinChat(chatId) }
    }

    fun selectProvider(providerId: String) {
        viewModelScope.launch {
            aiRepository.setActiveProvider(providerId)
            setState { copy(showProviderSelector = false) }
        }
    }

    fun toggleProviderSelector() {
        setState { copy(showProviderSelector = !showProviderSelector) }
    }

    fun sendMessage(content: String) {
        val chatId = state.value.activeChatId
        if (chatId.isBlank()) {
            createNewChat()
            return
        }
        viewModelScope.launch {
            setState { copy(isLoading = true) }
            aiRepository.sendMessage(chatId, content).collect { result ->
                when {
                    result.isLoading -> { }
                    result.isSuccess -> setState { copy(isLoading = false) }
                    result.isError -> setState { copy(isLoading = false, error = result.exceptionOrNull()?.message) }
                }
            }
        }
    }
}

private fun formatRelativeTime(timestamp: Long): String {
    val diff = System.currentTimeMillis() - timestamp
    val minutes = diff / 60_000
    val hours = diff / 3_600_000
    val days = diff / 86_400_000
    return when {
        minutes < 1 -> "Just now"
        minutes < 60 -> "${minutes}m ago"
        hours < 24 -> "${hours}h ago"
        days < 7 -> "${days}d ago"
        else -> java.text.SimpleDateFormat("MMM dd", java.util.Locale.getDefault()).format(java.util.Date(timestamp))
    }
}