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
import me.ikirby.ithomereader.ui.widget.OnBottomReachedListener

class AllCommentFragment : Fragment() {

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
        binding.listView.setOnBottomReachedListener(object : OnBottomReachedListener {
            override fun onBottomReached() {
                if (viewModel.allLoading.value == true) {
                    return
                }
                if (viewModel.allList.value!!.isNotEmpty()) {
                    viewModel.loadComment(hot = false, all = true, refresh = false)
                }
            }
        })
        binding.swipeRefresh.setOnRefreshListener {
            viewModel.loadComment(hot = false, all = true, refresh = true)
        }

        viewModel.allLoading.observe(viewLifecycleOwner) {
            binding.swipeRefresh.isRefreshing = it
        }
        viewModel.allList.observe(viewLifecycleOwner) {
            adapter.list = it
            UiUtil.switchVisibility(binding.listView, binding.errorPlaceholder, it.size)
        }
        viewModel.allCommentsLoaded.observe(viewLifecycleOwner) {
            binding.listView.setAllContentLoaded(it)
        }

        return binding.root
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_refresh) {
            viewModel.loadComment(hot = false, all = true, refresh = true)
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
