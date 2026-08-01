package com.shinytracker.core.sprites

import android.content.Context
import android.util.Log
import com.shinytracker.core.model.DexEntry
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.io.File
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "ShinyChecklistSource"
private const val BUNDLED_ASSET_PATH = "checklist.json"
private const val CACHE_FILENAME = "checklist.json"
private const val CHECKLIST_URL = "https://pogoapi.net/api/v1/shiny_pokemon.json"

@Serializable
private data class ChecklistEntryJson(
    val dexId: Int,
    val name: String,
)

/**
 * Eligibility checklist -- which species have a known shiny form -- separate
 * from SpriteMatcher's sprite-art catalog. Loads a bundled asset by default
 * (written by scripts/sync_checklist.py) so the app works fully offline on
 * first install; [refresh] re-fetches over the network and overwrites only
 * the on-disk cache, never the bundled asset.
 */
@Singleton
class ShinyChecklistSource
    @Inject
    constructor(
        @param:ApplicationContext private val context: Context,
        private val dexDataSource: PokemonDexDataSource,
    ) {
        private val json = Json { ignoreUnknownKeys = true }
        private val state: MutableStateFlow<List<DexEntry>> by lazy { MutableStateFlow(loadBundledOrCached()) }

        fun observeChecklist(): Flow<List<DexEntry>> = state

        /** Synchronous lookup against the currently loaded checklist; "Unknown" if not found. */
        fun nameFor(dexId: Int): String = state.value.firstOrNull { it.dexId == dexId }?.name ?: "Unknown #%03d".format(dexId)

        /** Species-level DexEntry (types/localizedNames/species/evolutions merged in), used by SpriteCatalog. */
        fun dexEntryFor(dexId: Int): DexEntry = dexEntry(dexId, nameFor(dexId))

        suspend fun refresh(): Result<Unit> = refreshFrom(CHECKLIST_URL)

        /** Visible for testing: lets a test point at an unreachable URL without touching the real endpoint. */
        internal suspend fun refreshFrom(url: String): Result<Unit> =
            withContext(Dispatchers.IO) {
                try {
                    val text = fetch(url)
                    val entries = parse(text)
                    cacheFile().writeText(text)
                    state.value = entries
                    Result.success(Unit)
                } catch (e: IOException) {
                    Log.e(TAG, "checklist refresh failed", e)
                    Result.failure(e)
                } catch (e: SerializationException) {
                    Log.e(TAG, "checklist refresh returned unparseable data", e)
                    Result.failure(e)
                }
            }

        private fun loadBundledOrCached(): List<DexEntry> {
            val cache = cacheFile()
            val text =
                if (cache.exists()) {
                    cache.readText()
                } else {
                    context.assets
                        .open(BUNDLED_ASSET_PATH)
                        .bufferedReader()
                        .use { it.readText() }
                }
            return parse(text)
        }

        private fun cacheFile() = File(context.filesDir, CACHE_FILENAME)

        private fun parse(text: String): List<DexEntry> =
            json
                .decodeFromString<List<ChecklistEntryJson>>(text)
                .map { dexEntry(it.dexId, it.name) }

        private fun dexEntry(
            dexId: Int,
            name: String,
        ): DexEntry {
            val data = dexDataSource.get(dexId)
            return DexEntry(
                dexId = dexId,
                formId = 0,
                costumeId = 0,
                name = name,
                types = data?.types.orEmpty(),
                localizedNames = data?.localizedNames.orEmpty(),
                species = data?.species,
                evolvesFrom = data?.evolvesFrom,
                evolvesTo = data?.evolvesTo.orEmpty(),
            )
        }

        private fun fetch(url: String): String {
            val connection = URL(url).openConnection() as HttpURLConnection
            return try {
                connection.inputStream.bufferedReader().use { it.readText() }
            } finally {
                connection.disconnect()
            }
        }
    }
