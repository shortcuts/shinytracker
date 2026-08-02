/*
 * Offline tool: hill-climbs SpriteMatching.kt's 8 ensemble weights against the
 * confusable pairs ConfusablePairs.kt (`make confusable-pairs`) flags, to widen the
 * margin between a sprite and its nearest look-alike. Prints a recommendation; does
 * NOT edit SpriteMatching.kt. Run: `make tune-weights`
 * (`./gradlew :core:sprites:descriptors:weightSearch`).
 */
package com.shinytracker.core.sprites.descriptors.tool

import com.shinytracker.core.sprites.descriptors.DescriptorEntry
import com.shinytracker.core.sprites.descriptors.EnsembleWeights
import com.shinytracker.core.sprites.descriptors.similarity
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.io.File

private val DESCRIPTORS_FILE = File("core/sprites/src/main/assets/sprites/descriptors.json")
private val json = Json { ignoreUnknownKeys = true }
const val SHORTLIST_SIZE = 15
val STEP_SIZES = floatArrayOf(0.05f, 0.02f, 0.01f)
const val MAX_PASSES_PER_STEP = 20
val WEIGHT_NAMES =
    listOf(
        "WEIGHT_GRID_RGB",
        "WEIGHT_PHASH",
        "WEIGHT_DHASH",
        "WEIGHT_HSV_HISTOGRAM",
        "WEIGHT_LAB_HISTOGRAM",
        "WEIGHT_DOMINANT_COLORS",
        "WEIGHT_EDGE_SIGNATURE",
        "WEIGHT_ALPHA_MASK",
    )
val BASELINE = floatArrayOf(0.30f, 0.15f, 0.15f, 0.15f, 0.10f, 0.05f, 0.05f, 0.05f)

fun toWeights(a: FloatArray) = EnsembleWeights(a[0], a[1], a[2], a[3], a[4], a[5], a[6], a[7])

/** Top-15 non-self candidates per confusable entry, at default weights -- fixed opponent pool for the search. */
fun buildShortlists(
    entries: List<DescriptorEntry>,
    targets: Set<String>,
): Map<String, List<DescriptorEntry>> {
    val byPath = entries.associateBy { it.assetPath }
    return targets.associateWith { path ->
        val entry = byPath.getValue(path)
        entries
            .filter { it.assetPath != path }
            .map { it to similarity(entry.descriptor, it.descriptor) }
            .sortedByDescending { (_, score) -> score }
            .take(SHORTLIST_SIZE)
            .map { (other, _) -> other }
    }
}

fun score(
    weightsArray: FloatArray,
    entriesByPath: Map<String, DescriptorEntry>,
    shortlists: Map<String, List<DescriptorEntry>>,
): Float {
    val weights = toWeights(weightsArray)
    val margins =
        shortlists.map { (path, candidates) ->
            val entry = entriesByPath.getValue(path)
            val best = candidates.maxOf { similarity(entry.descriptor, it.descriptor, weights) }
            1f - best
        }
    return margins.average().toFloat()
}

fun hillClimb(
    entriesByPath: Map<String, DescriptorEntry>,
    shortlists: Map<String, List<DescriptorEntry>>,
): FloatArray {
    var current = BASELINE.copyOf()
    var currentScore = score(current, entriesByPath, shortlists)
    for (step in STEP_SIZES) {
        var passesLeft = MAX_PASSES_PER_STEP
        var improved = true
        while (improved && passesLeft > 0) {
            improved = false
            passesLeft--
            for (i in 0 until 8) {
                for (j in 0 until 8) {
                    if (i == j || current[j] - step < 0f) continue
                    val candidate = current.copyOf()
                    candidate[i] += step
                    candidate[j] -= step
                    val candidateScore = score(candidate, entriesByPath, shortlists)
                    if (candidateScore > currentScore) {
                        current = candidate
                        currentScore = candidateScore
                        improved = true
                    }
                }
            }
        }
    }
    return current
}

fun accuracy(
    entries: List<DescriptorEntry>,
    confusable: List<ConfusablePair>,
): Float {
    val confusedPaths = confusable.flatMap { listOf(it.assetPathA, it.assetPathB) }.toSet()
    return (entries.size - confusedPaths.size) / entries.size.toFloat()
}

fun main() {
    val entries: List<DescriptorEntry> = json.decodeFromString(DESCRIPTORS_FILE.readText())
    val entriesByPath = entries.associateBy { it.assetPath }

    val baselineConfusable = findConfusablePairs(entries)
    val targets = baselineConfusable.flatMap { listOf(it.assetPathA, it.assetPathB) }.toSet()
    println("${targets.size} confusable entries to target, from ${baselineConfusable.size} pairs.")

    val shortlists = buildShortlists(entries, targets)
    val tuned = hillClimb(entriesByPath, shortlists)
    val tunedWeights = toWeights(tuned)
    val tunedConfusable = findConfusablePairs(entries, weights = tunedWeights)

    val baselineAccuracy = accuracy(entries, baselineConfusable)
    val tunedAccuracy = accuracy(entries, tunedConfusable)

    println("Baseline: ${baselineConfusable.size} confusable pairs, accuracy=$baselineAccuracy")
    println("Tuned:    ${tunedConfusable.size} confusable pairs, accuracy=$tunedAccuracy")

    val usedTuned = tunedConfusable.size < baselineConfusable.size
    val recommended = if (usedTuned) tuned else BASELINE
    if (!usedTuned) {
        println("Search did not reduce catalog-wide confusable pairs -- recommending NO CHANGE.")
    }
    println("Recommended weights (paste into SpriteMatching.kt:108-115):")
    for (i in 0 until 8) {
        println("private const val ${WEIGHT_NAMES[i]} = ${recommended[i]}f")
    }
}
