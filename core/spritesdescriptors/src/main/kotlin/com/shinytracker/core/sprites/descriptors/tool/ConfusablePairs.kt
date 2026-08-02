/*
 * Diagnostic tool: for every entry in descriptors.json, finds the highest-scoring
 * OTHER entry in the catalog and flags cases where that score is dangerously close
 * to a perfect match. A high top non-self score means the descriptor set can't
 * reliably tell the two sprites apart (e.g. Nidoran M/F, alternate costumes).
 *
 * Prerequisite for tuning SpriteMatching.kt's ensemble weights -- run this first to
 * see what's actually confusable instead of blindly re-tuning against the catalog.
 *
 * Run: `make confusable-pairs` (`./gradlew :core:sprites:descriptors:confusablePairs`).
 */
package com.shinytracker.core.sprites.descriptors.tool

import com.shinytracker.core.sprites.descriptors.DescriptorEntry
import com.shinytracker.core.sprites.descriptors.similarity
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.io.File

private val DESCRIPTORS_FILE = File("core/sprites/src/main/assets/sprites/descriptors.json")
private val json = Json { ignoreUnknownKeys = true }

// ponytail: starting point for the confusable-pairs report, not a hard cutoff --
// tune once real output shows where the true confusable/distinct boundary sits.
private const val CONFUSABLE_THRESHOLD = 0.95f

data class ConfusablePair(
    val assetPathA: String,
    val assetPathB: String,
    val score: Float,
)

/** For each entry, its best-scoring match among every OTHER entry in [entries]. */
fun findTopNonSelfMatches(entries: List<DescriptorEntry>): List<ConfusablePair> =
    entries.map { entry ->
        val (best, score) =
            entries
                .filter { it.assetPath != entry.assetPath }
                .map { other -> other to similarity(entry.descriptor, other.descriptor) }
                .maxByOrNull { (_, score) -> score }!!
        ConfusablePair(entry.assetPath, best.assetPath, score)
    }

fun findConfusablePairs(
    entries: List<DescriptorEntry>,
    threshold: Float = CONFUSABLE_THRESHOLD,
): List<ConfusablePair> =
    findTopNonSelfMatches(entries)
        .filter { it.score >= threshold }
        .sortedByDescending { it.score }

fun main() {
    val entries: List<DescriptorEntry> = json.decodeFromString(DESCRIPTORS_FILE.readText())
    val confusable = findConfusablePairs(entries)
    if (confusable.isEmpty()) {
        println("No confusable pairs found at threshold $CONFUSABLE_THRESHOLD across ${entries.size} entries.")
        return
    }
    println("${confusable.size} confusable pair(s) at threshold $CONFUSABLE_THRESHOLD:")
    for (pair in confusable) {
        println("  ${pair.score}  ${pair.assetPathA}  <->  ${pair.assetPathB}")
    }
}
