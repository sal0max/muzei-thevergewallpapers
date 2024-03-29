package de.salomax.muzei.thevergewallpapers

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.RemoteActionCompat
import androidx.core.graphics.drawable.IconCompat
import com.google.android.apps.muzei.api.provider.Artwork
import com.google.android.apps.muzei.api.provider.MuzeiArtProvider

class TheVergeArtProvider : MuzeiArtProvider() {

   companion object {
      private const val USER_COMMAND_ID_SHARE = 1
   }

   override fun onLoadRequested(initial: Boolean) {
      TheVergeWorker.enqueueLoad(context!!)
   }

   /* This is the new API for Muzei 3.4+ that works on all API levels */
   override fun getCommandActions(artwork: Artwork): List<RemoteActionCompat> {
      val context = context ?: return super.getCommandActions(artwork)
      return listOf(
         RemoteActionCompat(
            IconCompat.createWithResource(context, R.drawable.ic_share_24),
            context.getString(R.string.share_artwork_title),
            context.getString(R.string.share_artwork_title),
            PendingIntent.getActivity(
               context, artwork.id.toInt(),
               createShareIntent(context, artwork),
               if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                  PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
               else
                  PendingIntent.FLAG_UPDATE_CURRENT
            )
         )
      )
   }

   /* kept for backward compatibility with Muzei 3.3 */
   @Suppress("OVERRIDE_DEPRECATION", "DEPRECATION")
   override fun getCommands(artwork: Artwork) = listOf(
         com.google.android.apps.muzei.api.UserCommand(USER_COMMAND_ID_SHARE, context?.getString(R.string.share_artwork_title))
   )

   /* kept for backward compatibility with Muzei 3.3 */
   @Suppress("OVERRIDE_DEPRECATION")
   override fun onCommand(artwork: Artwork, id: Int) {
      val context = context ?: return
      when (id) {
         USER_COMMAND_ID_SHARE -> {
            createShareIntent(context, artwork).apply {
               addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }.takeIf { it.resolveActivity(context.packageManager) != null }?.run {
               context.startActivity(this)
            }
         }
      }
   }

   private fun createShareIntent(
         context: Context,
         artwork: Artwork
   ): Intent = Intent.createChooser(Intent(Intent.ACTION_SEND).apply {
      type = "text/plain"
      putExtra(Intent.EXTRA_TEXT, context.getString(R.string.share_artwork_message,
            artwork.title,
            artwork.webUri))
   }, context.getString(R.string.share_artwork_title))

}
