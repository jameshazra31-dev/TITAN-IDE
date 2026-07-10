package com.titan.domain.usecase.git

import com.titan.core.common.base.Result
import com.titan.domain.model.GitRepository as GitRepoModel
import com.titan.domain.repository.GitRepository
import javax.inject.Inject

class CloneRepositoryUseCase @Inject constructor(
    private val gitRepository: GitRepository,
) {
    suspend operator fun invoke(url: String, destination: String, branch: String? = null): Result<GitRepoModel> {
        return gitRepository.clone(url, destination, branch)
    }
}