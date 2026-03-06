package com.ezhart.todotxtandroid.dropbox

import android.content.Context
import android.util.Log
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ezhart.todotxtandroid.TAG
import com.ezhart.todotxtandroid.data.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.io.File
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

class DropboxService(val applicationContext: Context,
                     private val settings: SettingsRepository) {

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

    suspend fun sync() {
        // TODO See comment below, the sync process needs to be fleshed out
        // TODO This service also needs the settings repository
        // TODO force lower path

        // TODO Fix this like in TaskFileService if that ends up working
        settings.todoPath.collectLatest {
            downloadTaskFile(it)
        }
    }

    @OptIn(ExperimentalTime::class)
    suspend fun downloadTaskFile(path: String): File? {
        when (val metaDataResult = api.getFileMetaData(path)) {
            is GetFileMetaDataTaskResult.Success -> {
                // TODO this needs logic for determining whether a download is necessary
                // i.e. local stuff is newer, remote stuff is newer, etc.

                // With settings repo available, we can record the last server update time
                // Times are UTC, probably?
                metaDataResult.result.serverModified
                val x = Clock.System.now()
                // Make sure to log the ISO formats of all these so we can compare what it thinks is happening

                when (val downloadResult =
                    api.download(applicationContext, metaDataResult.result)) {
                    is DownloadFileTaskResult.Success -> return downloadResult.result
                    is DownloadFileTaskResult.Error -> {
                        Log.e(TAG, downloadResult.e.toString())
                    }
                }
            }

            is GetFileMetaDataTaskResult.Error -> {
                Log.e(TAG, metaDataResult.e.toString())

                // TODO Need handling for _no remote file_ (create empty local one)

            }
        }

        return null
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
}