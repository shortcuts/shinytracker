package com.shinytracker.core.data

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.core.content.FileProvider
import com.shinytracker.core.model.CaughtRecord
import com.shinytracker.core.model.DexEntry
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.io.File
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "ProfileShareRepository"
private const val SCHEMA_VERSION = 1
private const val SHARE_DIR = "shared-profiles"
private const val SHARE_FILENAME = "shinytracker-profile.json"

@Serializable
private data class SharedEntryJson(
    val dexId: Int,
    val formId: Int,
    val costumeId: Int,
    val shiny: Boolean,
    val name: String,
    val caughtAt: Long,
)

@Serializable
private data class SharedProfileJson(
    val schemaVersion: Int = SCHEMA_VERSION,
    val entries: List<SharedEntryJson>,
)

/**
 * Exports the owner's caught-shiny list to a shareable file and imports
 * someone else's exported file for read-only viewing. Imported records are
 * never written to CaughtRepository -- that table is exclusively the device
 * owner's own catches.
 */
@Singleton
class ProfileShareRepository
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
        private val caughtRepository: CaughtRepository,
    ) {
        private val json = Json { ignoreUnknownKeys = true }

        suspend fun exportProfile(): Result<Uri> =
            withContext(Dispatchers.IO) {
                try {
                    val entries = caughtRepository.observeCaught().first()
                    val payload = SharedProfileJson(entries = entries.map { it.toJson() })
                    val shareDir = File(context.cacheDir, SHARE_DIR).apply { mkdirs() }
                    val file = File(shareDir, SHARE_FILENAME)
                    file.writeText(json.encodeToString(SharedProfileJson.serializer(), payload))
                    Result.success(FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file))
                } catch (e: IOException) {
                    Log.e(TAG, "profile export failed", e)
                    Result.failure(e)
                }
            }

        suspend fun importProfile(uri: Uri): Result<List<CaughtRecord>> =
            withContext(Dispatchers.IO) {
                try {
                    val text =
                        context.contentResolver
                            .openInputStream(uri)
                            ?.bufferedReader()
                            ?.use { it.readText() }
                            ?: return@withContext Result.failure(IOException("Could not open $uri"))
                    val payload = json.decodeFromString(SharedProfileJson.serializer(), text)
                    Result.success(payload.entries.map { it.toDomain() })
                } catch (e: IOException) {
                    Log.e(TAG, "profile import failed", e)
                    Result.failure(e)
                } catch (e: SerializationException) {
                    Log.e(TAG, "profile import returned unparseable data", e)
                    Result.failure(e)
                }
            }
    }

private fun CaughtRecord.toJson() = SharedEntryJson(dexEntry.dexId, dexEntry.formId, dexEntry.costumeId, shiny, dexEntry.name, caughtAt)

private fun SharedEntryJson.toDomain() = CaughtRecord(DexEntry(dexId, formId, costumeId, name), shiny, caughtAt)
