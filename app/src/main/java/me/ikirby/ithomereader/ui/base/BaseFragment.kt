package me.ikirby.ithomereader.ui.base

import androidx.fragment.app.Fragment
import kotlinx.coroutines.experimental.Job

open class BaseFragment : Fragment() {
    protected val parentJob = Job()

    override fun onDestroyView() {
        parentJob.cancel()
        super.onDestroyView()
    }
}
