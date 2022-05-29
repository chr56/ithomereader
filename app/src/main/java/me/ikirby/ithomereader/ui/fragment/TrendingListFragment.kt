package me.ikirby.ithomereader.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.ikirby.ithomereader.KEY_LIST_STATE
import me.ikirby.ithomereader.KEY_SHOW_THUMB
import me.ikirby.ithomereader.KEY_TRENDING_LIST
import me.ikirby.ithomereader.R
import me.ikirby.ithomereader.api.impl.TrendingApiImpl
import me.ikirby.ithomereader.databinding.ListLayoutBinding
import me.ikirby.ithomereader.entity.Trending
import me.ikirby.ithomereader.ui.adapter.TrendingListAdapter
import me.ikirby.ithomereader.ui.base.BaseFragment
import me.ikirby.ithomereader.ui.util.ToastUtil
import me.ikirby.ithomereader.ui.util.UiUtil
import me.ikirby.ithomereader.util.shouldShowThumb

class TrendingListFragment : BaseFragment() {

    private var _binding: ListLayoutBinding? = null
    private val binding: ListLayoutBinding get() = _binding!!

    private lateinit var trendingList: ArrayList<Trending>
    private lateinit var adapter: TrendingListAdapter
    private lateinit var layoutManager: LinearLayoutManager
    private var isLoading = false
    private var showThumb: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = ListLayoutBinding.inflate(layoutInflater)

        layoutManager = LinearLayoutManager(activity)
        binding.listView.layoutManager = layoutManager

        binding.swipeRefresh.setOnRefreshListener { loadList() }
        binding.errorPlaceholder.setOnClickListener { loadList() }

        if (savedInstanceState != null) {
            trendingList = savedInstanceState.getParcelableArrayList(KEY_TRENDING_LIST) ?: ArrayList()
        }

        if (savedInstanceState == null || trendingList.isEmpty()) {
            trendingList = ArrayList()
            showThumb = shouldShowThumb()
            adapter = TrendingListAdapter(trendingList, requireContext(), showThumb)
            binding.listView.adapter = adapter
        } else {
            showThumb = savedInstanceState.getBoolean(KEY_SHOW_THUMB, true)
            adapter = TrendingListAdapter(trendingList, requireContext(), showThumb)
            binding.listView.adapter = adapter
            layoutManager.onRestoreInstanceState(savedInstanceState.getParcelable(KEY_LIST_STATE))
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (savedInstanceState == null) loadList()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelableArrayList(KEY_TRENDING_LIST, trendingList)
        outState.putParcelable(KEY_LIST_STATE, layoutManager.onSaveInstanceState())
        outState.putBoolean(KEY_SHOW_THUMB, showThumb)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_refresh -> {
                layoutManager.scrollToPosition(0)
                loadList()
            }
        }
        return true
    }

    private fun loadList() {
        if (!isLoading) {
            isLoading = true
            binding.swipeRefresh.isRefreshing = true
            launch {
                val trendings = withContext(Dispatchers.IO) { TrendingApiImpl.getTrendingList() }
                if (trendings != null) {
                    if (trendings.isNotEmpty()) {
                        showThumb = shouldShowThumb()
                        adapter.setShowThumb(showThumb)
                        trendingList.clear()
                        trendingList.addAll(trendings)
                        adapter.notifyDataSetChanged()
                    } else {
                        ToastUtil.showToast(R.string.no_more_content)
                    }
                } else {
                    ToastUtil.showToast(R.string.timeout_no_internet)
                }
                UiUtil.switchVisibility(binding.listView, binding.errorPlaceholder, trendingList.size)
                isLoading = false
                binding.swipeRefresh.isRefreshing = false
            }
        }
    }
}
