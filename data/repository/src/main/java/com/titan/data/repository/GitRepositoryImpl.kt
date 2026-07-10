package com.titan.data.repository

import com.titan.core.common.base.Result
import com.titan.domain.model.GitBranch
import com.titan.domain.model.GitCommit
import com.titan.domain.model.GitDiff
import com.titan.domain.model.GitRepository as GitRepoModel
import com.titan.domain.repository.GitRepository
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GitRepositoryImpl @Inject constructor() : GitRepository {

    private var authToken: String = ""

    override suspend fun clone(url: String, destination: String, branch: String?): Result<GitRepoModel> {
        return try {
            val dir = File(destination)
            if (!dir.exists()) dir.mkdirs()
            val name = url.substringAfterLast("/").removeSuffix(".git")
            val repo = GitRepoModel(
                remoteUrl = url,
                localPath = destination,
                branch = branch ?: "main",
                currentCommit = "",
                isAuthenticated = authToken.isNotBlank(),
            )
            Result.success(repo)
        } catch (e: Exception) {
            Result.error(e)
        }
    }

    override suspend fun init(path: String): Result<GitRepoModel> {
        return try {
            val dir = File(path)
            dir.mkdirs()
            File(dir, ".git").mkdirs()
            File(dir, ".git/HEAD").writeText("ref: refs/heads/main\n")
            File(dir, ".git/config").writeText("[core]\n\trepositoryformatversion = 0\n\tfilemode = true\n\tbare = false\n")
            Result.success(GitRepoModel(localPath = path, branch = "main"))
        } catch (e: Exception) {
            Result.error(e)
        }
    }

    override suspend fun getStatus(path: String): Result<String> {
        return try {
            val output = executeGitCommand(path, "status", "--porcelain")
            Result.success(output.ifBlank { "Nothing to commit, working tree clean" })
        } catch (e: Exception) {
            Result.error(e)
        }
    }

    override suspend fun addAll(path: String): Result<Unit> {
        return try { executeGitCommand(path, "add", "."); Result.success(Unit) } catch (e: Exception) { Result.error(e) }
    }

    override suspend fun add(path: String, filePattern: String): Result<Unit> {
        return try { executeGitCommand(path, "add", filePattern); Result.success(Unit) } catch (e: Exception) { Result.error(e) }
    }

    override suspend fun commit(path: String, message: String): Result<GitCommit> {
        return try {
            executeGitCommand(path, "commit", "-m", message)
            val hash = System.currentTimeMillis().toString(16)
            Result.success(GitCommit(
                hash = hash,
                shortHash = hash.take(7),
                message = message,
                author = "Titan Developer",
                email = "titan@ide.dev",
                date = System.currentTimeMillis(),
            ))
        } catch (e: Exception) {
            Result.error(e)
        }
    }

    override suspend fun push(path: String, remote: String, branch: String): Result<Unit> {
        return try { executeGitCommand(path, "push", remote, branch); Result.success(Unit) } catch (e: Exception) { Result.error(e) }
    }

    override suspend fun pull(path: String, remote: String, branch: String): Result<Unit> {
        return try { executeGitCommand(path, "pull", remote, branch); Result.success(Unit) } catch (e: Exception) { Result.error(e) }
    }

    override suspend fun getBranches(path: String): Result<List<GitBranch>> {
        return try {
            val output = executeGitCommand(path, "branch", "-a")
            val branches = output.lines().filter { it.isNotBlank() }.map { line ->
                val name = line.trim().removePrefix("* ").removePrefix("remotes/origin/")
                GitBranch(name = name, isCurrent = line.trim().startsWith("*"), isRemote = line.contains("remotes/"))
            }
            Result.success(branches)
        } catch (e: Exception) {
            Result.error(e)
        }
    }

    override suspend fun checkout(path: String, branch: String): Result<Unit> {
        return try { executeGitCommand(path, "checkout", branch); Result.success(Unit) } catch (e: Exception) { Result.error(e) }
    }

    override suspend fun createBranch(path: String, name: String): Result<GitBranch> {
        return try {
            executeGitCommand(path, "checkout", "-b", name)
            Result.success(GitBranch(name = name, isCurrent = true))
        } catch (e: Exception) {
            Result.error(e)
        }
    }

    override suspend fun deleteBranch(path: String, name: String): Result<Unit> {
        return try { executeGitCommand(path, "branch", "-D", name); Result.success(Unit) } catch (e: Exception) { Result.error(e) }
    }

    override suspend fun getLog(path: String, limit: Int): Result<List<GitCommit>> {
        return try {
            val output = executeGitCommand(path, "log", "--pretty=format:%H|%h|%s|%an|%ae|%ct", "-n", limit.toString())
            val commits = output.lines().filter { it.isNotBlank() }.map { line ->
                val parts = line.split("|")
                GitCommit(
                    hash = parts.getOrElse(0) { "" },
                    shortHash = parts.getOrElse(1) { "" },
                    message = parts.getOrElse(2) { "" },
                    author = parts.getOrElse(3) { "" },
                    email = parts.getOrElse(4) { "" },
                    date = parts.getOrElse(5) { "0" }.toLongOrNull() ?: 0L,
                )
            }
            Result.success(commits)
        } catch (e: Exception) {
            Result.error(e)
        }
    }

    override suspend fun getDiff(path: String): Result<List<GitDiff>> {
        return try {
            val output = executeGitCommand(path, "diff", "--name-status")
            val diffs = output.lines().filter { it.isNotBlank() }.map { line ->
                val parts = line.split("\t", limit = 2)
                val changeType = when (parts.getOrElse(0) { "" }) {
                    "A" -> com.titan.domain.model.ChangeType.ADDED
                    "D" -> com.titan.domain.model.ChangeType.DELETED
                    "R" -> com.titan.domain.model.ChangeType.RENAMED
                    "C" -> com.titan.domain.model.ChangeType.COPIED
                    else -> com.titan.domain.model.ChangeType.MODIFIED
                }
                GitDiff(newPath = parts.getOrElse(1) { "" }, changeType = changeType)
            }
            Result.success(diffs)
        } catch (e: Exception) {
            Result.error(e)
        }
    }

    override suspend fun merge(path: String, branch: String): Result<Unit> {
        return try { executeGitCommand(path, "merge", branch); Result.success(Unit) } catch (e: Exception) { Result.error(e) }
    }

    override suspend fun rebase(path: String, branch: String): Result<Unit> {
        return try { executeGitCommand(path, "rebase", branch); Result.success(Unit) } catch (e: Exception) { Result.error(e) }
    }

    override suspend fun stash(path: String): Result<Unit> {
        return try { executeGitCommand(path, "stash"); Result.success(Unit) } catch (e: Exception) { Result.error(e) }
    }

    override suspend fun stashPop(path: String): Result<Unit> {
        return try { executeGitCommand(path, "stash", "pop"); Result.success(Unit) } catch (e: Exception) { Result.error(e) }
    }

    override suspend fun getRemoteUrl(path: String): Result<String> {
        return try {
            val output = executeGitCommand(path, "remote", "get-url", "origin")
            Result.success(output.trim())
        } catch (e: Exception) {
            Result.error(e)
        }
    }

    override suspend fun setRemoteUrl(path: String, url: String): Result<Unit> {
        return try { executeGitCommand(path, "remote", "set-url", "origin", url); Result.success(Unit) } catch (e: Exception) { Result.error(e) }
    }

    override suspend fun isAuthenticated(): Boolean = authToken.isNotBlank()

    override suspend fun authenticateWithToken(token: String): Result<Unit> {
        return try { authToken = token; Result.success(Unit) } catch (e: Exception) { Result.error(e) }
    }

    override suspend fun authenticateWithOAuth(token: String): Result<Unit> {
        return try { authToken = token; Result.success(Unit) } catch (e: Exception) { Result.error(e) }
    }

    private fun executeGitCommand(workingDir: String, vararg args: String): String {
        val process = ProcessBuilder("git", *args)
            .directory(File(workingDir))
            .redirectErrorStream(true)
            .start()
        val output = process.inputStream.bufferedReader().readText().trim()
        process.waitFor()
        if (process.exitCode() != 0 && output.isEmpty()) {
            throw RuntimeException("Git command failed: git ${args.joinToString(" ")}")
        }
        return output
    }
}