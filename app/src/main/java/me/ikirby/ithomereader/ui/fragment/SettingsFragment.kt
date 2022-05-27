package me.ikirby.ithomereader.ui.fragment

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreference
import me.ikirby.ithomereader.*
import me.ikirby.ithomereader.ui.activity.CustomFilterActivity
import me.ikirby.ithomereader.ui.base.BaseActivity
import me.ikirby.ithomereader.ui.dialog.showListPreferenceDialog
import me.ikirby.ithomereader.ui.dialog.showLoginDialog
import me.ikirby.ithomereader.ui.dialog.showLogoutDialog
import me.ikirby.ithomereader.ui.dialog.showTimePreferenceDialog
import me.ikirby.ithomereader.ui.task.checkForUpdate
import me.ikirby.ithomereader.ui.widget.TimePreference
import me.ikirby.ithomereader.util.openLink


class SettingsFragment : PreferenceFragmentCompat() {

    private lateinit var preferences: SharedPreferences

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        preferences = BaseApplication.preferences
        if (savedInstanceState == null) {
            setPreferencesFromResource(R.xml.preferences, rootKey)

            val customFilterPreference = findPreference<Preference>(SETTINGS_KEY_CUSTOM_FILTER)
            customFilterPreference?.onPreferenceClickListener = Preference.OnPreferenceClickListener {
                startActivity(Intent(requireContext(), CustomFilterActivity::class.java))
                true
            }

            val showThumbPreference = findPreference<ListPreference>(SETTINGS_KEY_SHOW_THUMB_COND)
            val loadImagePreference = findPreference<ListPreference>(SETTINGS_KEY_LOAD_IMAGE_COND)
            val fontSizePreference = findPreference<ListPreference>(SETTINGS_KEY_FONT_SIZE)
            val nightModePreference = findPreference<ListPreference>(SETTINGS_KEY_APPCOMPAT_NIGHT_MODE)
            val nightModeStartTimePreference = findPreference<TimePreference>(SETTINGS_KEY_NIGHT_MODE_START_TIME)
            val nightModeEndTimePreference = findPreference<TimePreference>(SETTINGS_KEY_NIGHT_MODE_END_TIME)

            val summaryProvider = ListPreference.SimpleSummaryProvider.getInstance()
            showThumbPreference?.summaryProvider = summaryProvider
            loadImagePreference?.summaryProvider = summaryProvider
            fontSizePreference?.summaryProvider = summaryProvider
            nightModePreference?.summaryProvider = summaryProvider

            if (preferences.getString(SETTINGS_KEY_APPCOMPAT_NIGHT_MODE, "MODE_NIGHT_FOLLOW_SYSTEM") ==
                "MODE_NIGHT_BASED_ON_TIME"
            ) {
                nightModeStartTimePreference?.isEnabled = true
                nightModeEndTimePreference?.isEnabled = true
            }

            nightModePreference?.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _, newValue ->
                BaseApplication.hasSetNightModeManually = false
                if (newValue == "MODE_NIGHT_BASED_ON_TIME") {
                    nightModeStartTimePreference?.isEnabled = true
                    nightModeEndTimePreference?.isEnabled = true
                } else {
                    nightModeStartTimePreference?.isEnabled = false
                    nightModeEndTimePreference?.isEnabled = false
                }
                BaseApplication.instance.applyNightMode()
                true
            }

            val loginAccount = findPreference<Preference>(SETTINGS_KEY_LOGIN_ACCOUNT)
            if (preferences.contains(SETTINGS_KEY_USERNAME)) {
                loginAccount?.title = preferences.getString(SETTINGS_KEY_USERNAME, getString(R.string.login_title))
            }
            loginAccount?.onPreferenceClickListener = Preference.OnPreferenceClickListener { preference ->
                if (preferences.contains(SETTINGS_KEY_USER_HASH)) {
                    showLogoutDialog(requireActivity(), preference.title.toString(), preference)
                } else {
                    showLoginDialog(requireActivity()) { preference.title = it }
                }
                true
            }

            val swipeBackSwitch = findPreference<SwitchPreference>(SETTINGS_KEY_SWIPE_GESTURE)
            val useBottomNavSwitch = findPreference<SwitchPreference>(SETTINGS_KEY_USE_BOTTOM_NAV)

            swipeBackSwitch?.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _, _ ->
                activity?.setResult(Activity.RESULT_OK)
                true
            }

            useBottomNavSwitch?.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _, _ ->
                activity?.setResult(Activity.RESULT_OK)
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
            val libPanguJs = findPreference<Preference>(SETTINGS_KEY_LIB_PANGU_JS)

            goFullSite?.onPreferenceClickListener = Preference.OnPreferenceClickListener {
                openInCustomTabs("https://www.ithome.com")
                true
            }

            libAndroidX?.onPreferenceClickListener = Preference.OnPreferenceClickListener {
                openInCustomTabs("https://developer.android.com/topic/libraries/support-library/androidx-rn")
                true
            }

            libMdc?.onPreferenceClickListener = Preference.OnPreferenceClickListener {
                openInCustomTabs("https://github.com/material-components/material-components-android")
                true
            }

            libJsoup?.onPreferenceClickListener = Preference.OnPreferenceClickListener {
                openInCustomTabs("https://jsoup.org")
                true
            }

            libPhotoView?.onPreferenceClickListener = Preference.OnPreferenceClickListener {
                openInCustomTabs("https://github.com/chrisbanes/PhotoView")
                true
            }

            libGlide?.onPreferenceClickListener = Preference.OnPreferenceClickListener {
                openInCustomTabs("https://github.com/bumptech/glide")
                true
            }

            libBottomSheet?.onPreferenceClickListener = Preference.OnPreferenceClickListener {
                openInCustomTabs("https://github.com/krossovochkin/BottomSheetMenu")
                true
            }

            libKotlinxCoroutines?.onPreferenceClickListener = Preference.OnPreferenceClickListener {
                openInCustomTabs("https://github.com/Kotlin/kotlinx.coroutines/tree/master/ui/kotlinx-coroutines-android")
                true
            }

            libPanguJs?.onPreferenceClickListener = Preference.OnPreferenceClickListener {
                openInCustomTabs("https://github.com/vinta/pangu.js")
                true
            }

            val checkForUpdatePreference = findPreference<Preference>(SETTINGS_KEY_CHECK_UPDATE)
            checkForUpdatePreference?.summary = getString(R.string.current_ver, BuildConfig.VERSION_NAME)
            checkForUpdatePreference?.onPreferenceClickListener = Preference.OnPreferenceClickListener {
                checkForUpdate(requireActivity() as BaseActivity, true)
                true
            }
        }
    }

    override fun onDisplayPreferenceDialog(preference: Preference) {
        when (preference) {
            is ListPreference -> showListPreferenceDialog(requireContext(), preference)
            is TimePreference -> showTimePreferenceDialog(requireContext(), preference)
            else -> super.onDisplayPreferenceDialog(preference)
        }
    }

    private fun openInCustomTabs(url: String) {
        openLink(requireContext(), url)
    }
}
