package de.salomax.muzei.thevergewallpapers

import android.content.Intent
import com.google.android.apps.muzei.api.UserCommand
import com.google.android.apps.muzei.api.provider.Artwork
import com.google.android.apps.muzei.api.provider.MuzeiArtProvider

class TheVergeArtProvider : MuzeiArtProvider() {

   companion object {
      private const val USER_COMMAND_ID_SHARE = 1
   }

   override fun onLoadRequested(initial: Boolean) {
      TheVergeWorker.enqueueLoad()
   }

   override fun getCommands(artwork: Artwork) = listOf(
         UserCommand(USER_COMMAND_ID_SHARE, context?.getString(R.string.share_artwork_title))
   )

   /*
    * Not working properly in Android 10. See:
    * - https://github.com/romannurik/muzei/issues/644
    * - https://developer.android.com/guide/components/activities/background-starts
    */
   override fun onCommand(artwork: Artwork, id: Int) {
      val context = context ?: return
      when (id) {
         USER_COMMAND_ID_SHARE -> {
            Intent.createChooser(Intent(Intent.ACTION_SEND).apply {
               type = "text/plain"
               putExtra(Intent.EXTRA_TEXT, context.getString(
                     R.string.share_artwork_message,
                     artwork.title,
                     artwork.webUri))
            }, context.getString(R.string.share_artwork_title))?.apply {
               addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }?.takeIf { it.resolveActivity(context.packageManager) != null }?.run {
               context.startActivity(this)
            }
         }
      }
   }

}
