package com.shinytracker.core.sprites

import android.content.Context
import android.util.Log
import com.shinytracker.core.model.PokemonType
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "PokemonDexDataSource"
private const val DEX_DATA_ASSET_PATH = "dexdata.json"

@Serializable
internal data class DexDataJson(
    val dexId: Int,
    val names: Map<String, String>,
    val types: List<String>,
    val species: String? = null,
    val evolvesFrom: Int? = null,
    val evolvesTo: List<Int> = emptyList(),
)

internal data class PokemonDexData(
    val localizedNames: Map<String, String>,
    val types: List<PokemonType>,
    val species: String?,
    val evolvesFrom: Int?,
    val evolvesTo: List<Int>,
)

/**
 * Species-level Pokemon metadata (names, types, species flavor text,
 * evolution dex-id links) bundled from scripts/sync_dex_data.py's
 * dexdata.json. Separate from ShinyChecklistSource's eligibility list --
 * this is "what is this species", not "does it have a shiny form".
 */
@Singleton
class PokemonDexDataSource
    @Inject
    constructor(
        @ApplicationContext context: Context,
    ) {
        private val byDexId: Map<Int, PokemonDexData> by lazy {
            val json = Json { ignoreUnknownKeys = true }
            val text =
                context.assets
                    .open(DEX_DATA_ASSET_PATH)
                    .bufferedReader()
                    .use { it.readText() }
            json.decodeFromString<List<DexDataJson>>(text).associate {
                it.dexId to
                    PokemonDexData(
                        localizedNames = it.names,
                        types =
                            it.types.mapNotNull { type ->
                                runCatching { PokemonType.valueOf(type) }.getOrElse { e ->
                                    Log.w(TAG, "unknown type '$type' for dexId ${it.dexId}", e)
                                    null
                                }
                            },
                        species = it.species,
                        evolvesFrom = it.evolvesFrom,
                        evolvesTo = it.evolvesTo,
                    )
            }
        }

        internal fun get(dexId: Int): PokemonDexData? = byDexId[dexId]
    }
