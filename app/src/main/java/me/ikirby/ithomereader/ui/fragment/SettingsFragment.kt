package me.ikirby.ithomereader.ui.fragment

import android.app.Activity
import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreference
import me.ikirby.ithomereader.BaseApplication
import me.ikirby.ithomereader.BuildConfig
import me.ikirby.ithomereader.R
import me.ikirby.ithomereader.task.UpdateCheckNotifyTask
import me.ikirby.ithomereader.ui.dialog.LoginDialog
import me.ikirby.ithomereader.ui.dialog.TimePreferenceDialog
import me.ikirby.ithomereader.ui.widget.TimePreference
import me.ikirby.ithomereader.util.openLink


class SettingsFragment : PreferenceFragmentCompat() {

    private lateinit var preferences: SharedPreferences

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        preferences = BaseApplication.preferences
        if (savedInstanceState == null) {

            setPreferencesFromResource(R.xml.preferences, rootKey)
            val loginAccount = findPreference<Preference>("login_account")
            if (preferences.contains("username")) {
                loginAccount.title = preferences.getString("username", getString(R.string.login_title))
            }
            loginAccount.onPreferenceClickListener = Preference.OnPreferenceClickListener { preference ->
                var loginInfo: String? = null
                if (preferences.contains("user_hash")) {
                    loginInfo = "y"
                }
                val dialog = LoginDialog.newInstance(loginInfo)
                dialog.setCallbackPreference(preference)
                dialog.show(activity!!.supportFragmentManager, "loginDialog")
                true
            }

            val oStyleLightSwitch = findPreference<SwitchPreference>("o_style_light")
            val swipeBackSwitch = findPreference<SwitchPreference>("swipe_back")
            val useBottomNavSwitch = findPreference<SwitchPreference>("use_bottom_nav")

            oStyleLightSwitch.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _, _ ->
                activity!!.setResult(Activity.RESULT_OK)
                true
            }

            swipeBackSwitch.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _, _ ->
                activity!!.setResult(Activity.RESULT_OK)
                true
            }

            useBottomNavSwitch.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _, _ ->
                activity!!.setResult(Activity.RESULT_OK)
                true
            }

            val goFullSite = findPreference<Preference>("go_full_site")
            val libAndroidX = findPreference<Preference>("lib_androidx")
            val libMdc = findPreference<Preference>("lib_mdc")
            val libJsoup = findPreference<Preference>("lib_jsoup")
            val libPhotoView = findPreference<Preference>("lib_photoview")
            val libGlide = findPreference<Preference>("lib_glide")
            val libBottomSheet = findPreference<Preference>("lib_bottom_sheet")
            val libKotlinxCoroutines = findPreference<Preference>("lib_kotlinx_coroutines")

            goFullSite.onPreferenceClickListener = Preference.OnPreferenceClickListener {
                openInCustomTabs("https://www.ithome.com")
                true
            }

            libAndroidX.onPreferenceClickListener = Preference.OnPreferenceClickListener {
                openInCustomTabs("https://developer.android.com/topic/libraries/support-library/androidx-rn")
                true
            }

            libMdc.onPreferenceClickListener = Preference.OnPreferenceClickListener {
                openInCustomTabs("https://github.com/material-components/material-components-android")
                true
            }

            libJsoup.onPreferenceClickListener = Preference.OnPreferenceClickListener {
                openInCustomTabs("https://jsoup.org")
                true
            }

            libPhotoView.onPreferenceClickListener = Preference.OnPreferenceClickListener {
                openInCustomTabs("https://github.com/chrisbanes/PhotoView")
                true
            }

            libGlide.onPreferenceClickListener = Preference.OnPreferenceClickListener {
                openInCustomTabs("https://github.com/bumptech/glide")
                true
            }

            libBottomSheet.onPreferenceClickListener = Preference.OnPreferenceClickListener {
                openInCustomTabs("https://github.com/krossovochkin/BottomSheetMenu")
                true
            }

            libKotlinxCoroutines.onPreferenceClickListener = Preference.OnPreferenceClickListener {
                openInCustomTabs("https://github.com/Kotlin/kotlinx.coroutines/tree/master/ui/kotlinx-coroutines-android")
                true
            }

            val checkForUpdate = findPreference<Preference>("check_for_update")
            checkForUpdate.summary = getString(R.string.current_ver, BuildConfig.VERSION_NAME)
            checkForUpdate.onPreferenceClickListener = Preference.OnPreferenceClickListener {
                UpdateCheckNotifyTask(true).execute()
                true
            }
        }
    }

    override fun onDisplayPreferenceDialog(preference: Preference?) {
        var dialogFragment: DialogFragment? = null
        if (preference is TimePreference) {
            dialogFragment = TimePreferenceDialog()
            val bundle = Bundle(1)
            bundle.putString("key", preference.getKey())
            dialogFragment.setArguments(bundle)
        }

        if (dialogFragment != null) {
            dialogFragment.setTargetFragment(this, 0)
            dialogFragment.show(this.fragmentManager!!, "androidx.preference.PreferenceFragment.DIALOG")
        } else {
            super.onDisplayPreferenceDialog(preference)
        }
    }

    private fun openInCustomTabs(url: String) {
        openLink(context!!, url)
    }
}
