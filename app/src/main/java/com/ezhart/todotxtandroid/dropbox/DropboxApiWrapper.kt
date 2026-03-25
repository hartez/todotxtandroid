package com.ezhart.todotxtandroid.dropbox

import android.content.Context
import com.dropbox.core.DbxException
import com.dropbox.core.DbxRequestConfig
import com.dropbox.core.oauth.DbxCredential
import com.dropbox.core.v2.DbxClientV2
import com.dropbox.core.v2.files.FileMetadata
import com.dropbox.core.v2.files.WriteMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import kotlin.coroutines.CoroutineContext

class DropboxApiWrapper(
    dbxCredential: DbxCredential,
    clientIdentifier: String,
    private val ioDispatcher: CoroutineContext = Dispatchers.IO
) {
    val dropboxClient: DbxClientV2 = DbxClientV2(
        DbxRequestConfig(clientIdentifier),
        dbxCredential
    )

    suspend fun revokeDropboxAuthorization() = withContext(ioDispatcher) {
        dropboxClient.auth().tokenRevoke()
    }

    suspend fun getCurrentAccount(): GetCurrentAccountResult = withContext(ioDispatcher) {
        try {
            GetCurrentAccountResult.Success(dropboxClient.users().currentAccount)
        } catch (e: DbxException) {
            GetCurrentAccountResult.Error(e)
        }
    }

    suspend fun download(
        applicationContext: Context,
        metadata: FileMetadata
    ): DownloadFileTaskResult = withContext(ioDispatcher) {
        try {
            val file = File(applicationContext.filesDir, metadata.name)

            FileOutputStream(file).use { outputStream ->
                dropboxClient.files().download(metadata.pathLower, metadata.rev)
                    .download(outputStream)
            }

            DownloadFileTaskResult.Success(file)
        } catch (e: DbxException) {
            DownloadFileTaskResult.Error(e)
        } catch (e: IOException) {
            DownloadFileTaskResult.Error(e)
        }
    }

    suspend fun upload(
        applicationContext: Context,
        localFileName: String,
        remotePath: String,
        revision: String
    ): UploadFileTaskResult = withContext(ioDispatcher) {

        try {
            val file = File(applicationContext.filesDir, localFileName)

            FileInputStream(file).use { inputStream ->
                val fileMetadata = dropboxClient
                    .files()
                    .uploadBuilder(remotePath)

                    // These next three settings make the remote conflict resolution work; if the
                    // revision we pass in matches the one on Dropbox, then the file will be updated
                    // on Dropbox. If not, then Dropbox will upload our local file as a "conflicted"
                    // version (that's the auto rename setting). The "with strict conflict = false"
                    // setting allows uploading if the remote file has been deleted or has the exact
                    // content we're uploading.
                    .withMode(WriteMode.update(revision))
                    .withAutorename(true)
                    .withStrictConflict(false)

                    .uploadAndFinish(inputStream)
                UploadFileTaskResult.Success(fileMetadata)
            }


        } catch (e: DbxException) {
            UploadFileTaskResult.Error(e)
        } catch (e: IOException) {
            UploadFileTaskResult.Error(e)
        }
    }

    suspend fun getFileMetadata(path: String): GetFileMetadataTaskResult =
        withContext(ioDispatcher) {
            try {
                val metadata = dropboxClient.files().getMetadata(path) as FileMetadata
                GetFileMetadataTaskResult.Success(metadata)
            } catch (e: DbxException) {
                GetFileMetadataTaskResult.Error(e)
            } catch (e: IOException) {
                GetFileMetadataTaskResult.Error(e)
            }
        }

    private suspend fun getFilesForFolder(folderPath: String): GetFilesApiResponse =
        withContext(Dispatchers.IO) {
            try {
                val files = dropboxClient.files().listFolder(folderPath)
                GetFilesApiResponse.Success(files)
            } catch (exception: DbxException) {
                GetFilesApiResponse.Failure(exception)
            }
        }
}