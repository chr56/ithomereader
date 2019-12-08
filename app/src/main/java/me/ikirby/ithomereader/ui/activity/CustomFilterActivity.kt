package me.ikirby.ithomereader.ui.activity

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.core.view.ViewCompat
import androidx.core.view.updatePadding
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.android.synthetic.main.dialog_edittext.*
import kotlinx.android.synthetic.main.list_layout.*
import me.ikirby.ithomereader.BaseApplication
import me.ikirby.ithomereader.R
import me.ikirby.ithomereader.SETTINGS_KEY_CUSTOM_FILTER
import me.ikirby.ithomereader.ui.adapter.CustomFilterListAdapter
import me.ikirby.ithomereader.ui.base.BaseActivity
import me.ikirby.ithomereader.ui.util.ToastUtil

class CustomFilterActivity : BaseActivity() {

    private val keywordsList = mutableListOf<String>()
    private lateinit var adapter: CustomFilterListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTitleCustom(getString(R.string.pref_custom_filter))
        enableBackBtn()
        swipe_refresh.isEnabled = false

        val keywords = BaseApplication.preferences.getString(SETTINGS_KEY_CUSTOM_FILTER, "")!!
        keywordsList.addAll(keywords.split(",").filter { it.isNotBlank() })

        adapter = CustomFilterListAdapter(keywordsList, layoutInflater, View.OnClickListener {
            val position = list_view.getChildAdapterPosition(it)
            confirmDelete(position)
        })
        list_view.layoutManager = LinearLayoutManager(this)
        list_view.adapter = adapter
    }

    override fun initView() {
        setContentView(R.layout.list_layout)
        ToastUtil.showToast("123")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.navigationBarColor = Color.TRANSPARENT
            swipe_refresh.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            ViewCompat.setOnApplyWindowInsetsListener(swipe_refresh) { v, insets ->
                v.updatePadding(top = insets.systemWindowInsets.top)
                list_view.updatePadding(bottom = insets.systemWindowInsets.bottom)
                insets
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.custom_filter_action, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_add_keyword -> addKeyword()
            android.R.id.home -> onBackPressed()
        }
        return true
    }

    private fun addKeyword() {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.add_keyword)
            .setView(R.layout.dialog_edittext)
            .setPositiveButton(R.string.ok) { dialogInterface, _ ->
                val editText = (dialogInterface as AlertDialog).edit_text
                val keyword = editText.text.toString().trim()

                if (keyword.isNotBlank()) {
                    keywordsList.add(keyword)
                    saveToPreferences()
                    adapter.notifyDataSetChanged()
                }
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun confirmDelete(position: Int) {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.delete_keyword)
            .setMessage(getString(R.string.confirm_delete_keyword, keywordsList[position]))
            .setPositiveButton(R.string.ok) { _, _ ->
                keywordsList.removeAt(position)
                saveToPreferences()
                adapter.notifyDataSetChanged()
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun saveToPreferences() {
        BaseApplication.preferences.edit()
            .putString(SETTINGS_KEY_CUSTOM_FILTER, keywordsList.joinToString(","))
            .apply()
    }
}