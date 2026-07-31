package com.shinytracker.core.sprites

import android.content.Context
import android.graphics.Bitmap
import com.shinytracker.core.model.MatchResult
import com.shinytracker.core.model.ShinyRecord
import com.shinytracker.core.sprites.descriptors.SpriteDescriptor
import com.shinytracker.core.sprites.descriptors.computeDescriptor
import com.shinytracker.core.sprites.descriptors.pickBestMatch
import com.shinytracker.core.sprites.descriptors.pickTopMatches
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SpriteMatcher
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
        private val checklistSource: ShinyChecklistSource,
    ) {
        private val catalog: List<Pair<ShinyRecord, SpriteDescriptor>> by lazy { loadCatalog() }

        fun match(crop: Bitmap): MatchResult? {
            val descriptor = computeDescriptor(crop.toArgbPixels(), crop.width, crop.height)
            val (record, score) = pickBestMatch(descriptor, catalog) ?: return null
            return MatchResult(record.dexEntry, record.shiny, score)
        }

        /** Top [topN] candidates instead of just the single best match. */
        fun matchCandidates(
            crop: Bitmap,
            topN: Int = 5,
        ): List<MatchResult> {
            val descriptor = computeDescriptor(crop.toArgbPixels(), crop.width, crop.height)
            return pickTopMatches(descriptor, catalog, topN).map { (record, score) ->
                MatchResult(record.dexEntry, record.shiny, score)
            }
        }

        private fun loadCatalog(): List<Pair<ShinyRecord, SpriteDescriptor>> {
            val records = SpriteCatalog.load(context, checklistSource::dexEntryFor)
            val descriptors = DescriptorCatalog.load(context)
            return records.mapNotNull { record -> descriptors[record.assetPath]?.let { record to it } }
        }
    }

private fun Bitmap.toArgbPixels(): IntArray {
    val pixels = IntArray(width * height)
    getPixels(pixels, 0, width, 0, 0, width, height)
    return pixels
}
