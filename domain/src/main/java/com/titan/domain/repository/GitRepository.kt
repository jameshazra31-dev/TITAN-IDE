package com.titan.domain.repository

import com.titan.core.common.base.Result
import com.titan.domain.model.GitBranch
import com.titan.domain.model.GitCommit
import com.titan.domain.model.GitDiff
import com.titan.domain.model.GitRepository as GitRepoModel

interface GitRepository {
    suspend fun clone(url: String, destination: String, branch: String? = null): Result<GitRepoModel>
    suspend fun init(path: String): Result<GitRepoModel>
    suspend fun getStatus(path: String): Result<String>
    suspend fun addAll(path: String): Result<Unit>
    suspend fun add(path: String, filePattern: String): Result<Unit>
    suspend fun commit(path: String, message: String): Result<GitCommit>
    suspend fun push(path: String, remote: String = "origin", branch: String = "main"): Result<Unit>
    suspend fun pull(path: String, remote: String = "origin", branch: String = "main"): Result<Unit>
    suspend fun getBranches(path: String): Result<List<GitBranch>>
    suspend fun checkout(path: String, branch: String): Result<Unit>
    suspend fun createBranch(path: String, name: String): Result<GitBranch>
    suspend fun deleteBranch(path: String, name: String): Result<Unit>
    suspend fun getLog(path: String, limit: Int = 50): Result<List<GitCommit>>
    suspend fun getDiff(path: String): Result<List<GitDiff>>
    suspend fun merge(path: String, branch: String): Result<Unit>
    suspend fun rebase(path: String, branch: String): Result<Unit>
    suspend fun stash(path: String): Result<Unit>
    suspend fun stashPop(path: String): Result<Unit>
    suspend fun getRemoteUrl(path: String): Result<String>
    suspend fun setRemoteUrl(path: String, url: String): Result<Unit>
    suspend fun isAuthenticated(): Boolean
    suspend fun authenticateWithToken(token: String): Result<Unit>
    suspend fun authenticateWithOAuth(token: String): Result<Unit>
}