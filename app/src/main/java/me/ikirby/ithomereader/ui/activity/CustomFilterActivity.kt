package me.ikirby.ithomereader.ui.activity

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import me.ikirby.ithomereader.BaseApplication
import me.ikirby.ithomereader.R
import me.ikirby.ithomereader.SETTINGS_KEY_CUSTOM_FILTER
import me.ikirby.ithomereader.databinding.DialogEdittextBinding
import me.ikirby.ithomereader.databinding.ListLayoutBinding
import me.ikirby.ithomereader.ui.adapter.CustomFilterListAdapter
import me.ikirby.ithomereader.ui.base.BaseActivity

class CustomFilterActivity : BaseActivity() {

    private lateinit var listLayout: ListLayoutBinding
    private lateinit var dialog: DialogEdittextBinding

    private val keywordsList = mutableListOf<String>()
    private lateinit var adapter: CustomFilterListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        listLayout = ListLayoutBinding.inflate(layoutInflater)
        // make sure binding is inflated before using in [initView()]
        super.onCreate(savedInstanceState)
        setTitleCustom(getString(R.string.pref_custom_filter))
        enableBackBtn()
        listLayout.swipeRefresh.isEnabled = false

        val keywords = BaseApplication.preferences.getString(SETTINGS_KEY_CUSTOM_FILTER, "")!!
        keywordsList.addAll(keywords.split(",").filter { it.isNotBlank() })

        adapter = CustomFilterListAdapter(keywordsList, layoutInflater) {
            val position = listLayout.listView.getChildAdapterPosition(it)
            confirmDelete(position)
        }
        listLayout.listView.layoutManager = LinearLayoutManager(this)
        listLayout.listView.adapter = adapter

        dialog = DialogEdittextBinding.inflate(layoutInflater)
    }

    override fun initView() {
        setContentView(listLayout.root)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.custom_filter_action, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_add_keyword -> addKeyword()
            android.R.id.home -> onBackPressedDispatcher.onBackPressed()
        }
        return true
    }

    private fun addKeyword() {
        // todo: use another more elegant way to handle custom view
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.add_keyword)
            .setView(dialog.root)
            .setPositiveButton(R.string.ok) { _, _ ->
                val keyword = dialog.editText.text.toString().trim()

                if (keyword.isNotBlank()) {
                    keywordsList.add(keyword)
                    saveToPreferences()
                    adapter.notifyDataSetChanged()
                }
            }
            .setNegativeButton(R.string.cancel, null)
            .setOnDismissListener {
                (dialog.root.parent as ViewGroup).removeView(dialog.root)
                // remove self, or crash next time.
            }
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
