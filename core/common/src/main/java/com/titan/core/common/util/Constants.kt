package com.titan.core.common.util

object Constants {
    const val DATABASE_NAME = "titan_ide_db"
    const val DATASTORE_NAME = "titan_ide_preferences"
    const val ENCRYPTED_DATASTORE_NAME = "titan_ide_encrypted_prefs"

    const val KEYSTORE_ALIAS = "titan_ide_keystore"
    const val KEYSTORE_PROVIDER = "AndroidKeyStore"

    const val DEFAULT_TIMEOUT = 30_000L
    const val MAX_FILE_SIZE = 10 * 1024 * 1024L // 10MB
    const val MAX_PROJECTS = 100

    object Editor {
        const val TAB_SIZE = 4
        const val FONT_SIZE = 14
        const val MAX_UNDO_STACK = 1000
        const val AUTOSAVE_INTERVAL = 3000L
        const val MAX_LINE_LENGTH = 500
    }

    object AI {
        const val DEFAULT_TEMPERATURE = 0.7
        const val DEFAULT_TOP_P = 1.0
        const val DEFAULT_MAX_TOKENS = 4096
        const val DEFAULT_FREQUENCY_PENALTY = 0.0
        const val DEFAULT_PRESENCE_PENALTY = 0.0
        const val DEFAULT_TIMEOUT = 60_000L
        const val DEFAULT_RETRY_COUNT = 3
        const val MAX_CHAT_HISTORY = 100
    }

    object Git {
        const val DEFAULT_BRANCH = "main"
        const val DEFAULT_REMOTE = "origin"
        const val COMMIT_MESSAGE_MAX_LENGTH = 72
    }

    object Terminal {
        const val DEFAULT_SHELL = "/system/bin/sh"
        const val MAX_HISTORY = 1000
        const val BUFFER_SIZE = 8192
    }

    object Build {
        const val GRADLE_COMMAND = "./gradlew"
        const val DEFAULT_BUILD_VARIANT = "debug"
        const val BUILD_TIMEOUT = 600_000L
    }
}