package com.poatek.sample.ui.scenes.third

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.poatek.sample.R
import com.poatek.sample.databinding.FragmentFirstBinding
import com.poatek.sample.databinding.FragmentThirdBinding
import com.poatek.sample.ui.base.BaseFragment
import com.poatek.sample.ui.scenes.second.SecondViewModel


class ThirdFragment : BaseFragment<FragmentThirdBinding>() {

    override val viewModel: ThirdViewModel by viewModels()

    override fun onCreateBinding(inflater: LayoutInflater): FragmentThirdBinding {
        return FragmentThirdBinding.inflate(inflater)
    }

}