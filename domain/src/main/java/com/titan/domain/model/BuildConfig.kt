package com.titan.domain.model

data class BuildConfiguration(
    val projectId: String = "",
    val buildType: BuildType = BuildType.DEBUG,
    val buildVariant: String = "debug",
    val flavor: String = "",
    val isClean: Boolean = false,
    val isIncremental: Boolean = true,
    val additionalArguments: String = "",
    val signingConfig: SigningConfig? = null,
)

data class SigningConfig(
    val keystorePath: String = "",
    val keystorePassword: String = "",
    val keyAlias: String = "",
    val keyPassword: String = "",
    val storeType: String = "JKS",
)

enum class BuildType(val displayName: String, val gradleTask: String) {
    DEBUG("Debug", "assembleDebug"),
    RELEASE("Release", "assembleRelease"),
    BUNDLE("App Bundle", "bundleRelease"),
}

data class BuildResult(
    val isSuccess: Boolean = false,
    val output: String = "",
    val errorOutput: String = "",
    val duration: Long = 0L,
    val apkPath: String = "",
    val buildType: BuildType = BuildType.DEBUG,
    val timestamp: Long = System.currentTimeMillis(),
)