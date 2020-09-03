package com.vittach.sample

import com.vittach.sample.utils.PermissionsHelper
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.dsl.module

object SystemModule {
    fun create() = module {
        single { PermissionsHelper() }
    }
}