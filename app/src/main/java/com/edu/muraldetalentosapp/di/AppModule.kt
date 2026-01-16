package com.edu.muraldetalentosapp.di

import com.edu.muraldetalentosapp.data.repository.ApplicationRepository
import com.edu.muraldetalentosapp.data.repository.JobPostingRepository
import com.edu.muraldetalentosapp.data.repository.UserRepository
import com.edu.muraldetalentosapp.viewmodel.CandidateSearchViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import com.google.firebase.auth.FirebaseAuth
import org.koin.dsl.module

val appModule = module {
    single { FirebaseAuth.getInstance() }

    single { JobPostingRepository() }
    single { ApplicationRepository() }
    single { UserRepository() }

    viewModel {
        CandidateSearchViewModel(
            jobRepository = get(),
            applicationRepository = get(),
            userRepository = get(),
            auth = get()
        )
    }
}