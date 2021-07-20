package com.poatek.sample.ui.scenes.second

import android.view.LayoutInflater
import androidx.fragment.app.viewModels
import com.poatek.sample.databinding.FragmentSecondBinding
import com.poatek.sample.ui.base.BaseFragment


class SecondFragment : BaseFragment<FragmentSecondBinding>() {

    override val viewModel: SecondViewModel by viewModels()

    override fun onCreateBinding(inflater: LayoutInflater): FragmentSecondBinding {
        return FragmentSecondBinding.inflate(inflater)
    }

}