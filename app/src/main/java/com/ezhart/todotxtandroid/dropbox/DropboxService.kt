package com.ezhart.todotxtandroid.dropbox

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import com.dropbox.core.v2.files.FileMetadata
import com.ezhart.todotxtandroid.TAG
import com.ezhart.todotxtandroid.data.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.io.path.Path

sealed interface SyncResult {
    class NotConnected : SyncResult
    class NotAuthenticated : SyncResult
    class Success(val message: String) : SyncResult
    class Conflict(val message: String) : SyncResult
    class Error(val e: Exception) : SyncResult
}

class DropboxService(
    val applicationContext: Context,
    private val settings: SettingsRepository,
    private val syncData: SyncDataStorage
) {
    var awaitingSignInResponse = false

    val config = DropboxAppConfig()
    val credentials by lazy { DropboxCredentials(applicationContext) }
    val authHandler by lazy { DropboxAuth(credentials, config) }
    val api by lazy {
        DropboxApiWrapper(
            credentials.readCredentialLocally()!!,
            config.clientIdentifier
        )
    }

    private var signedInCallback: () -> Unit = {}

    fun isAuthenticated(): Boolean {
        return credentials.isAuthenticated()
    }

    fun signIn(activityContext: Context, onSignedIn: () -> Unit) {
        awaitingSignInResponse = true
        signedInCallback = onSignedIn
        authHandler.startDropboxAuthorization2PKCE(activityContext)
    }

    fun signOut() {
        if (credentials.isAuthenticated()) {
            CoroutineScope(Dispatchers.IO).launch {
                api.revokeDropboxAuthorization()
            }
            credentials.removeCredentialLocally()
        }
    }

    fun onResume() {
        authHandler.onResume()
        if (awaitingSignInResponse) {
            if (credentials.isAuthenticated()) {
                signedInCallback()
                signedInCallback = {}
            }
        }
    }

    suspend fun sync(): SyncResult = withContext(Dispatchers.IO) {

        if(!isOnline()){
            return@withContext SyncResult.NotConnected()
        }

        if(!isAuthenticated()){
            return@withContext SyncResult.NotAuthenticated()
        }

        val (lastSyncRemotePath, lastSyncRemoteRevision, currentLocalRevision, lastSyncLocalRevision) = syncData.lastestSyncState.first()
        val todoPath = settings.todoPath.first().lowercase()

        try {
            val remoteMetadata = getFileMetadata(todoPath)

            val lastSyncRemoteRevision = lastSyncRemoteRevision

            val remoteHasChanges = lastSyncRemotePath != todoPath
                    || lastSyncRemoteRevision.isEmpty()
                    || remoteMetadata.rev != lastSyncRemoteRevision

            val localHasChanges = lastSyncRemotePath == todoPath
                    && !lastSyncLocalRevision.isEmpty()
                    && !currentLocalRevision.isEmpty()
                    && lastSyncLocalRevision != currentLocalRevision

            if (localHasChanges) {
                val syncResult =
                    putLocalChanges(todoPath, lastSyncRemoteRevision, currentLocalRevision)

                if (syncResult is SyncResult.Conflict) {
                    // In a conflict, the server version should take precedent
                    getRemoteChanges(remoteMetadata, todoPath)
                }

                return@withContext syncResult
            }

            if (remoteHasChanges) {
                getRemoteChanges(remoteMetadata, todoPath)
                return@withContext SyncResult.Success("No local changes; downloaded remote changes.")
            }

            SyncResult.Success("No changes in local or remote file; nothing to sync.")

        } catch (e: Exception) {
            SyncResult.Error(e)
        }
    }

    private suspend fun getRemoteChanges(remoteMetadata: FileMetadata, todoPath: String) =
        withContext(Dispatchers.IO) {
            downloadFile(remoteMetadata)
            syncData.updateLastSyncRemoteRevision(remoteMetadata.rev)
            syncData.updateLastSyncRemotePath(todoPath)
            syncData.updateLastSyncLocalRevision(syncData.updateCurrentLocalRevision())
        }

    private suspend fun putLocalChanges(
        todoPath: String,
        lastSyncRemoteRevision: String,
        currentLocalRevision: String
    ): SyncResult = withContext(Dispatchers.IO) {

        val localFileName = Path(todoPath).fileName.toString()
        val result = uploadFile(localFileName, todoPath, lastSyncRemoteRevision)

        when (result.name) {
            localFileName -> {
                // No conflicts
                syncData.updateLastSyncRemoteRevision(result.rev)
                syncData.updateLastSyncRemotePath(todoPath)
                syncData.updateLastSyncLocalRevision(currentLocalRevision)
                SyncResult.Success("Remote file updated")
            }
            else -> {
                SyncResult.Conflict("Conflicts detected. Conflicted version is ${result.name}.")
            }
        }
    }

    private suspend fun getFileMetadata(path: String): FileMetadata {
        when (val metaDataResult = api.getFileMetadata(path)) {
            is GetFileMetadataTaskResult.Success -> return metaDataResult.result

            is GetFileMetadataTaskResult.Error -> {
                throw metaDataResult.e
            }
        }
    }

    private suspend fun downloadFile(fileMetadata: FileMetadata): File {
        when (val downloadResult = api.download(applicationContext, fileMetadata)) {
            is DownloadFileTaskResult.Success -> return downloadResult.result
            is DownloadFileTaskResult.Error -> {
                throw downloadResult.e
            }
        }
    }

    private suspend fun uploadFile(
        localFileName: String,
        remotePath: String,
        revision: String
    ): FileMetadata {
        when (val uploadResult =
            api.upload(applicationContext, localFileName, remotePath, revision)) {
            is UploadFileTaskResult.Success -> return uploadResult.result
            is UploadFileTaskResult.Error -> {
                throw uploadResult.e
            }
        }
    }

    fun isOnline(): Boolean {
        val connectivityManager =
            applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val capabilities =
            connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
        if (capabilities != null) {
            if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                Log.i(TAG, "NetworkCapabilities.TRANSPORT_CELLULAR")
                return true
            } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                Log.i(TAG, "NetworkCapabilities.TRANSPORT_WIFI")
                return true
            } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {
                Log.i(TAG, "NetworkCapabilities.TRANSPORT_ETHERNET")
                return true
            }
        }
        return false
    }
}