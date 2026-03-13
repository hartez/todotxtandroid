package com.ezhart.todotxtandroid.dropbox

import android.content.Context
import android.util.Log
import com.ezhart.todotxtandroid.TAG
import com.ezhart.todotxtandroid.data.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

class DropboxService(
    val applicationContext: Context,
    private val settings: SettingsRepository
) {
    val todoPath = settings.todoPath.stateIn(
        kotlinx.coroutines.MainScope(),
        SharingStarted.Eagerly,
        ""
    )

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

    // TODO almost certainly needs to be on the IO dispatcher
    suspend fun sync() {
        // TODO See comment below, the sync process needs to be fleshed out
        // TODO force lower path

        downloadTaskFile(todoPath.value)
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