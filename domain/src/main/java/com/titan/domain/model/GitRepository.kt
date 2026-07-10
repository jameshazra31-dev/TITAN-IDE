package com.titan.domain.model

data class GitRepository(
    val remoteUrl: String = "",
    val localPath: String = "",
    val branch: String = "main",
    val currentCommit: String = "",
    val author: String = "",
    val email: String = "",
    val isAuthenticated: Boolean = false,
    val hasUncommittedChanges: Boolean = false,
    val ahead: Int = 0,
    val behind: Int = 0,
)

data class GitCommit(
    val hash: String,
    val shortHash: String,
    val message: String,
    val author: String,
    val email: String,
    val date: Long,
)

data class GitBranch(
    val name: String,
    val isCurrent: Boolean = false,
    val isRemote: Boolean = false,
    val lastCommitDate: Long = 0L,
)

data class GitDiff(
    val oldPath: String = "",
    val newPath: String = "",
    val changeType: ChangeType = ChangeType.MODIFIED,
    val additions: Int = 0,
    val deletions: Int = 0,
    val content: String = "",
)

enum class ChangeType {
    ADDED, MODIFIED, DELETED, RENAMED, COPIED
}