package com.ezhart.todotxtandroid.dropbox

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

const val SYNC_INFO_STORE_NAME = "file_info"

data class SyncState(
    val lastSyncRemotePath: String = "",
    val lastSyncRemoteRevision: String = "",
    val currentLocalRevision: String = "",
    val lastSyncLocalRevision: String = ""
)

class SyncDataStorage(private val context: Context) {
    private val Context.dataStore by preferencesDataStore(name = SYNC_INFO_STORE_NAME)

    private object DataKeys {
        val LAST_SYNC_REMOTE_PATH = stringPreferencesKey("last_sync_remote_path")
        val LAST_SYNC_REMOTE_REV = stringPreferencesKey("last_sync_remote_rev")
        val CURRENT_LOCAL_REV = stringPreferencesKey("current_local_rev")
        val LAST_SYNC_LOCAL_REV = stringPreferencesKey("last_sync_local_rev")
    }

    val lastestSyncState: Flow<SyncState> = context.dataStore.data.map { data ->
        SyncState(
            data[DataKeys.LAST_SYNC_REMOTE_PATH] ?: "",
            data[DataKeys.LAST_SYNC_REMOTE_REV] ?: "",
            data[DataKeys.CURRENT_LOCAL_REV] ?: "",
            data[DataKeys.LAST_SYNC_LOCAL_REV] ?: ""
        )
    }

    suspend fun updateLastSyncRemotePath(lastSyncRemotePath: String) {
        context.dataStore.edit { values ->
            values[DataKeys.LAST_SYNC_REMOTE_PATH] = lastSyncRemotePath
        }
    }

    suspend fun updateLastSyncRemoteRevision(revision: String) {
        context.dataStore.edit { values ->
            values[DataKeys.LAST_SYNC_REMOTE_REV] = revision
        }
    }

    suspend fun updateCurrentLocalRevision() : String {
        val currentLocalRevision = UUID.randomUUID().toString()

        context.dataStore.edit { values ->
            values[DataKeys.CURRENT_LOCAL_REV] = currentLocalRevision
        }

        return currentLocalRevision
    }

    suspend fun updateLastSyncLocalRevision(lastSyncLocalRevision: String) {
        context.dataStore.edit { values ->
            values[DataKeys.LAST_SYNC_LOCAL_REV] = lastSyncLocalRevision
        }
    }
}