package me.ikirby.ithomereader.ui.fragment

import android.app.Activity
import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreference
import me.ikirby.ithomereader.*
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
            val loginAccount = findPreference<Preference>(SETTINGS_KEY_LOGIN_ACCOUNT)
            if (preferences.contains(SETTINGS_KEY_USERNAME)) {
                loginAccount.title = preferences.getString(SETTINGS_KEY_USERNAME, getString(R.string.login_title))
            }
            loginAccount.onPreferenceClickListener = Preference.OnPreferenceClickListener { preference ->
                var loginInfo: String? = null
                if (preferences.contains(SETTINGS_KEY_USER_HASH)) {
                    loginInfo = "y"
                }
                val dialog = LoginDialog.newInstance(loginInfo)
                dialog.setCallbackPreference(preference)
                dialog.show(activity!!.supportFragmentManager, "loginDialog")
                true
            }

            val oStyleLightSwitch = findPreference<SwitchPreference>(SETTINGS_KEY_WHITE_THEME)
            val swipeBackSwitch = findPreference<SwitchPreference>(SETTINGS_KEY_SWIPE_GESTURE)
            val useBottomNavSwitch = findPreference<SwitchPreference>(SETTINGS_KEY_USE_BOTTOM_NAV)

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

            val goFullSite = findPreference<Preference>(SETTINGS_KEY_GO_FULL_SITE)
            val libAndroidX = findPreference<Preference>(SETTINGS_KEY_LIB_ANDROIDX)
            val libMdc = findPreference<Preference>(SETTINGS_KEY_LIB_MDC)
            val libJsoup = findPreference<Preference>(SETTINGS_KEY_LIB_JSOUP)
            val libPhotoView = findPreference<Preference>(SETTINGS_KEY_LIB_PHOTOVIEW)
            val libGlide = findPreference<Preference>(SETTINGS_KEY_LIB_GLIDE)
            val libBottomSheet = findPreference<Preference>(SETTINGS_KEY_LIB_BOTTOMSHEET)
            val libKotlinxCoroutines = findPreference<Preference>(SETTINGS_KEY_LIB_KOTLINCOROUTINES)

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

            val checkForUpdate = findPreference<Preference>(SETTINGS_KEY_CHECK_UPDATE)
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
