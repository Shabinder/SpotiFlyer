/*
 * Copyright (C)  2020  Shabinder Singh
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.shabinder.spotiflyer.ui.base

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.viewbinding.ViewBinding
import com.shabinder.spotiflyer.SharedViewModel

abstract class BaseFragment<VB:ViewBinding,VM : ViewModel> : Fragment() {

    protected val sharedViewModel: SharedViewModel by activityViewModels()
    protected abstract val binding: VB
    protected abstract val viewModel: VM
    protected val viewModelScope by lazy{viewModel.viewModelScope}

    open fun applicationContext(): Context = requireActivity().applicationContext
}