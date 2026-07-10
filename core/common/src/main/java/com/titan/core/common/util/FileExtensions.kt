package com.titan.core.common.util

object FileExtensions {
    val KOTLIN = setOf("kt", "kts")
    val JAVA = setOf("java")
    val XML = setOf("xml")
    val GRADLE = setOf("gradle", "gradle.kts")
    val GROOVY = setOf("groovy")
    val C_CPP = setOf("c", "cpp", "h", "hpp", "cc", "cxx", "hxx")
    val PYTHON = setOf("py", "pyw")
    val JAVASCRIPT = setOf("js", "mjs", "cjs")
    val TYPESCRIPT = setOf("ts", "tsx", "d.ts")
    val HTML = setOf("html", "htm")
    val CSS = setOf("css", "scss", "sass", "less")
    val JSON = setOf("json")
    val YAML = setOf("yaml", "yml")
    val MARKDOWN = setOf("md", "mdx")
    val SHELL = setOf("sh", "bash", "zsh", "fish")
    val PROPERTIES = setOf("properties")
    val SQL = setOf("sql")
    val PROTO = setOf("proto")
    val DAGGER = setOf("kt") // Hilt/Dagger uses Kotlin

    private val EXTENSION_MAP = mapOf(
        "kotlin" to KOTLIN,
        "java" to JAVA,
        "xml" to XML,
        "gradle" to GRADLE,
        "groovy" to GROOVY,
        "c_cpp" to C_CPP,
        "python" to PYTHON,
        "javascript" to JAVASCRIPT,
        "typescript" to TYPESCRIPT,
        "html" to HTML,
        "css" to CSS,
        "json" to JSON,
        "yaml" to YAML,
        "markdown" to MARKDOWN,
        "shell" to SHELL,
    )

    fun getLanguageFromExtension(extension: String): String? {
        return EXTENSION_MAP.entries.firstOrNull { extension in it.value }?.key
    }

    fun isCodeFile(fileName: String): Boolean {
        val ext = fileName.substringAfterLast('.', "").lowercase()
        return EXTENSION_MAP.values.any { ext in it }
    }

    fun isImageFile(fileName: String): Boolean {
        val ext = fileName.substringAfterLast('.', "").lowercase()
        return ext in setOf("png", "jpg", "jpeg", "gif", "bmp", "webp", "svg", "ico")
    }

    fun isArchiveFile(fileName: String): Boolean {
        val ext = fileName.substringAfterLast('.', "").lowercase()
        return ext in setOf("zip", "tar", "gz", "7z", "rar", "jar", "aar")
    }
}