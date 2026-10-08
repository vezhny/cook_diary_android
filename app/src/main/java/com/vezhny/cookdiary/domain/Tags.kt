package com.vezhny.cookdiary.domain

/** "Суп,  быстро , ,суп" -> "суп,быстро" */
fun normalizeTags(raw: String): String = normalizeTags(raw.split(','))

/** Trims, lowercases, drops empty and duplicate tags; returns them comma-separated. */
fun normalizeTags(tags: Iterable<String>): String =
    tags.map { it.trim().lowercase() }.filter { it.isNotEmpty() }.distinct().joinToString(",")
