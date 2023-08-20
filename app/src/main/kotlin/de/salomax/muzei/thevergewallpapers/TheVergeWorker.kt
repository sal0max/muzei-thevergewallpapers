package de.salomax.muzei.thevergewallpapers

import android.content.Context
import android.util.Log
import androidx.core.net.toUri
import androidx.work.*
import com.google.android.apps.muzei.api.provider.Artwork
import com.google.android.apps.muzei.api.provider.ProviderContract
import java.io.IOException

class TheVergeWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {

   companion object {
      internal fun enqueueLoad(context: Context) {
         WorkManager
               .getInstance(context)
               .enqueue(OneTimeWorkRequestBuilder<TheVergeWorker>()
                     .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
                     .build())
      }
   }

   override fun doWork(): Result {
      // fetch photos
      val photos = try {
         TheVergeService.getWallpapers()
      } catch (e: IOException) {
         Log.w(javaClass.simpleName, "Error reading API.", e)
         return Result.retry()
      }

      // check if successful
      if (photos.isNullOrEmpty()) {
         Log.w(javaClass.simpleName, "Failed to find any photos.")
         return Result.failure()
      }

      // success -> set Artwork
      val providerClient = ProviderContract.getProviderClient(applicationContext, TheVergeArtProvider::class.java)
      photos.asReversed()  // reverse: first in list is newest, so add last
            .map { photo ->
               Artwork(
                  token = photo.url,
                  title = photo.title,
                  byline = photo.dimensions,
                  persistentUri = photo.url.toUri(),
                  webUri = "https://www.theverge.com/pages/wallpapers".toUri()
               )
            }
            .forEach {
               providerClient.addArtwork(it)
            }
      return Result.success()
   }

}
