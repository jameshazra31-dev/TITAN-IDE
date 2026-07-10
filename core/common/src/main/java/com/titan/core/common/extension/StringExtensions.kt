package com.titan.core.common.extension

import java.util.Locale

fun String.capitalizeFirst(): String = replaceFirstChar {
    if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
}

fun String.toTitleCase(): String = split(" ").joinToString(" ") { it.capitalizeFirst() }

fun String.stripIndent(): String = lines().map { it.trimStart() }.joinToString("\n")

fun String.containsAny(vararg patterns: String, ignoreCase: Boolean = false): Boolean {
    return patterns.any { contains(it, ignoreCase) }
}

fun String.lineCount(): Int = count { it == '\n' } + if (isNotEmpty() && last() != '\n') 1 else 0

fun String.wordCount(): Int = split(Regex("\\s+")).filter { it.isNotEmpty() }.size

fun String.toSnakeCase(): String = replace(Regex("([a-z])([A-Z])")) { "${it.groupValues[1]}_${it.groupValues[2]}" }.lowercase()

fun String.toCamelCase(): String = split("_").mapIndexed { index, part ->
    if (index == 0) part.lowercase() else part.capitalizeFirst()
}.joinToString("")

fun String.toPascalCase(): String = split("_").joinToString("") { it.capitalizeFirst() }

fun String.abbreviate(maxLength: Int): String {
    if (length <= maxLength) return this
    return take(maxLength - 3) + "..."
}

fun String.isNumeric(): Boolean = all { it.isDigit() }

fun String.isAlphanumeric(): Boolean = all { it.isLetterOrDigit() || it == '_' }

fun String?.orEmpty(): String = this ?: ""

fun String?.isNotNullOrEmpty(): Boolean = !isNullOrBlank()