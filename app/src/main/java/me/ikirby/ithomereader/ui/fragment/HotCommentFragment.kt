package me.ikirby.ithomereader.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import me.ikirby.ithomereader.R
import me.ikirby.ithomereader.databinding.ListLayoutBinding
import me.ikirby.ithomereader.ui.adapter.CommentListAdapter
import me.ikirby.ithomereader.ui.databinding.viewmodel.CommentsActivityViewModel
import me.ikirby.ithomereader.ui.util.UiUtil

class HotCommentFragment: Fragment() {

    private val viewModel by activityViewModels<CommentsActivityViewModel>()
    private lateinit var binding: ListLayoutBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        setHasOptionsMenu(true)
        binding = ListLayoutBinding.inflate(inflater, container, false)

        val adapter = CommentListAdapter()
        binding.listView.adapter = adapter
        binding.listView.layoutManager = LinearLayoutManager(requireContext())
        binding.swipeRefresh.setOnRefreshListener {
            viewModel.loadComment(hot = true, all = false, refresh = false)
        }

        viewModel.hotLoading.observe(viewLifecycleOwner) {
            binding.swipeRefresh.isRefreshing = it
        }
        viewModel.hotList.observe(viewLifecycleOwner) {
            adapter.list = it
            UiUtil.switchVisibility(binding.listView, binding.errorPlaceholder, it.size)
        }

        return binding.root
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_refresh) {
            viewModel.loadComment(hot = true, all = false, refresh = false)
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
