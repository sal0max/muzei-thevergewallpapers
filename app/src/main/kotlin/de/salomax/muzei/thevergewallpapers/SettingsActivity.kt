package de.salomax.muzei.thevergewallpapers

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import java.util.*

class SettingsActivity : AppCompatActivity() {

   override fun onCreate(savedInstanceState: Bundle?) {
      super.onCreate(savedInstanceState)

      // display the fragment as the main content.
      supportFragmentManager
            .beginTransaction()
            .replace(android.R.id.content, PrefsFragment())
            .commit()
   }

   class PrefsFragment : PreferenceFragmentCompat() {

      override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
         super.onViewCreated(view, savedInstanceState)
         view.fitsSystemWindows = true
      }

      override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
         addPreferencesFromResource(R.xml.preferences)

         // show correct version name & copyright year
         findPreference<Preference>(getText(R.string.pref_about_key))?.summary = getString(
               R.string.pref_about_summary,
               requireActivity().packageManager.getPackageInfo(requireActivity().packageName, 0).versionName,
               Calendar.getInstance().get(Calendar.YEAR))
         // open browser
         findPreference<Preference>(getText(R.string.pref_link_key))?.setOnPreferenceClickListener {
            startActivity(Intent(Intent.ACTION_VIEW).apply {
               data = getString(R.string.pref_link_link).toUri()
            })
            true
         }
      }
   }

   override fun onOptionsItemSelected(item: MenuItem): Boolean {
      if (item.itemId == android.R.id.home) {
         onBackPressed()
         return true
      }
      return super.onOptionsItemSelected(item)
   }

}
