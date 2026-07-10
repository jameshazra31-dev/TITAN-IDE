package com.titan.core.common.base

data class Resource<out T>(
    val status: Status,
    val data: T? = null,
    val message: String? = null,
    val code: Int? = null
) {
    enum class Status { SUCCESS, ERROR, LOADING }

    val isSuccess: Boolean get() = status == Status.SUCCESS
    val isError: Boolean get() = status == Status.ERROR
    val isLoading: Boolean get() = status == Status.LOADING

    companion object {
        fun <T> success(data: T): Resource<T> = Resource(Status.SUCCESS, data)
        fun <T> error(message: String, code: Int? = null): Resource<T> = Resource(Status.ERROR, message = message, code = code)
        fun <T> loading(): Resource<T> = Resource(Status.LOADING)
    }
}

inline fun <T> Resource<T>.onSuccess(block: (T) -> Unit): Resource<T> {
    if (isSuccess && data != null) block(data)
    return this
}

inline fun <T> Resource<T>.onError(block: (String, Int?) -> Unit): Resource<T> {
    if (isError) block(message ?: "Unknown error", code)
    return this
}