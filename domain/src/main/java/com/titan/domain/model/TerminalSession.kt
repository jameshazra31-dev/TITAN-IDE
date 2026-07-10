package com.titan.domain.model

data class TerminalSession(
    val id: String = java.util.UUID.randomUUID().toString(),
    val title: String = "Terminal",
    val workingDirectory: String = "",
    val shellType: ShellType = ShellType.BASH,
    val isRunning: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val envVariables: Map<String, String> = emptyMap(),
)

enum class ShellType(val command: String, val displayName: String) {
    BASH("/system/bin/sh", "Bash"),
    SH("/system/bin/sh", "sh"),
    ZSH("zsh", "Zsh"),
    FISH("fish", "Fish"),
    ROOT("/system/bin/su", "Root Shell"),
}