package com.shinytracker.core.sprites

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.shinytracker.core.model.MatchResult
import com.shinytracker.core.model.ShinyRecord
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

        private fun loadCatalog(): List<Pair<ShinyRecord, SpriteDescriptor>> =
            SpriteCatalog.load(context, checklistSource::nameFor).map { record ->
                context.assets.open("sprites/${record.assetPath}").use { stream ->
                    val bitmap = BitmapFactory.decodeStream(stream)
                    val descriptor = computeDescriptor(bitmap.toArgbPixels(), bitmap.width, bitmap.height)
                    bitmap.recycle()
                    record to descriptor
                }
            }
    }

private fun Bitmap.toArgbPixels(): IntArray {
    val pixels = IntArray(width * height)
    getPixels(pixels, 0, width, 0, 0, width, height)
    return pixels
}
