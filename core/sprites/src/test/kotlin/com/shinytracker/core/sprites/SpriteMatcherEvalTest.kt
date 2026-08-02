package com.shinytracker.core.sprites

import android.content.Context
import android.graphics.BitmapFactory
import androidx.test.core.app.ApplicationProvider
import com.shinytracker.core.model.DexEntry
import com.shinytracker.core.model.MatchResult
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertTrue
import org.junit.Assume
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.File

/**
 * Measures [SpriteMatcher]'s real-world top-1 accuracy against a labeled set of real
 * on-device screenshot crops, separate from the catalog self-similarity numbers used
 * elsewhere. Normally skipped -- no real crop set is bundled with the repo.
 *
 * To run against real labeled crops:
 * ```
 * EVAL_CROPS_DIR=/path/to/crops \
 * EVAL_LABELS_FILE=/path/to/labels.json \
 * ./gradlew :core:sprites:test --tests "com.shinytracker.core.sprites.SpriteMatcherEvalTest" --info
 * ```
 * `--info` is required to see the printed accuracy report; a plain passing test run
 * otherwise swallows stdout.
 *
 * `EVAL_LABELS_FILE` is a JSON array of:
 * ```json
 * [ { "filename": "crop_0001.png", "dexId": 25, "shiny": true } ]
 * ```
 * `filename` resolves against `EVAL_CROPS_DIR`.
 */
@Serializable
data class EvalLabel(
    val filename: String,
    val dexId: Int,
    val shiny: Boolean,
)

data class EvalOutcome(
    val label: EvalLabel,
    val predicted: MatchResult?,
)

fun summarizeEval(outcomes: List<EvalOutcome>): String {
    val total = outcomes.size
    val correct =
        outcomes.count {
            it.predicted?.dexEntry?.dexId == it.label.dexId && it.predicted.shiny == it.label.shiny
        }
    val percentage = if (total == 0) 0.0 else correct * 100.0 / total
    val header = "SpriteMatcher eval: $correct/$total correct (${"%.1f".format(percentage)}%)"
    val mismatches =
        outcomes
            .filterNot { it.predicted?.dexEntry?.dexId == it.label.dexId && it.predicted.shiny == it.label.shiny }
            .joinToString("\n") { outcome ->
                val actual =
                    outcome.predicted?.let {
                        "dexId=${it.dexEntry.dexId} shiny=${it.shiny} confidence=${it.confidence}"
                    } ?: "no match"
                "  ${outcome.label.filename}: expected dexId=${outcome.label.dexId} shiny=${outcome.label.shiny}, got $actual"
            }
    return if (mismatches.isEmpty()) header else "$header\n$mismatches"
}

@RunWith(RobolectricTestRunner::class)
class SpriteMatcherEvalTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val matcher = SpriteMatcher(context, ShinyChecklistSource(context, PokemonDexDataSource(context)))

    @Test
    fun `reports SpriteMatcher top-1 accuracy against a real labeled crop set`() {
        val cropsDir = System.getenv("EVAL_CROPS_DIR")
        val labelsFile = System.getenv("EVAL_LABELS_FILE")
        Assume.assumeTrue(
            "Set EVAL_CROPS_DIR and EVAL_LABELS_FILE to run this against real labeled crops",
            !cropsDir.isNullOrBlank() && !labelsFile.isNullOrBlank(),
        )

        val json = Json { ignoreUnknownKeys = true }
        val labels = json.decodeFromString<List<EvalLabel>>(File(labelsFile!!).readText())

        val outcomes =
            labels.map { label ->
                val bitmap = BitmapFactory.decodeFile(File(cropsDir, label.filename).path)
                EvalOutcome(label, matcher.match(bitmap))
            }

        println(summarizeEval(outcomes))
    }

    @Test
    fun `summarizeEval reports accuracy counts and mismatch detail`() {
        val bulbasaur = DexEntry(dexId = 1, formId = 0, name = "Bulbasaur")
        val charmander = DexEntry(dexId = 4, formId = 0, name = "Charmander")

        val outcomes =
            listOf(
                EvalOutcome(
                    label = EvalLabel("a.png", dexId = 1, shiny = false),
                    predicted = MatchResult(bulbasaur, shiny = false, confidence = 0.9f),
                ),
                EvalOutcome(
                    label = EvalLabel("b.png", dexId = 4, shiny = true),
                    predicted = MatchResult(charmander, shiny = false, confidence = 0.8f),
                ),
                EvalOutcome(
                    label = EvalLabel("c.png", dexId = 25, shiny = false),
                    predicted = null,
                ),
            )

        val summary = summarizeEval(outcomes)

        assertTrue(summary.contains("1/3 correct"))
        assertTrue(summary.contains("33.3%"))
        assertTrue(summary.contains("b.png: expected dexId=4 shiny=true, got dexId=4 shiny=false confidence=0.8"))
        assertTrue(summary.contains("c.png: expected dexId=25 shiny=false, got no match"))
    }
}
