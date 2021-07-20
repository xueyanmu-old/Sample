package com.poatek.sample.ui.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.CallSuper
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.viewbinding.ViewBinding
import com.poatek.sample.BR

abstract class BaseFragment<B : ViewBinding> : Fragment() {

    protected open val displayBottomBar = true

    // Inner mutable binding
    private var _binding: B? = null

    // Binding used by subclasses
    val binding: B
        get() = _binding!!

    protected open val viewModel: ViewModel? = null

    @CallSuper
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = bindView(inflater, container)
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        (parentFragment as? BottomBarManager)?.changeBottomBarState(displayBottomBar)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        (activity as? BottomBarManager)?.changeBottomBarState(displayBottomBar)
    }

    abstract fun onCreateBinding(inflater: LayoutInflater): B

    @CallSuper
    protected open fun bindView(inflater: LayoutInflater, container: ViewGroup?): B {
        return onCreateBinding(inflater).apply {
            if (this is ViewDataBinding) {
                lifecycleOwner = viewLifecycleOwner
                viewModel?.let { setVariable(BR.viewModel, it) }
            }
        }
    }

    @CallSuper
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
