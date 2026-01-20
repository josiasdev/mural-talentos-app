package com.edu.muraldetalentosapp.di

import com.edu.muraldetalentosapp.data.repository.ApplicationRepository
import com.edu.muraldetalentosapp.data.repository.CandidatesRepository
import com.edu.muraldetalentosapp.data.repository.JobPostingRepository
import com.edu.muraldetalentosapp.data.repository.UserRepository
import com.edu.muraldetalentosapp.viewmodel.CandidateSearchViewModel
import com.edu.muraldetalentosapp.viewmodel.CandidatesViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import org.koin.dsl.module

val appModule = module {
    single { FirebaseAuth.getInstance() }
    single { FirebaseFirestore.getInstance() }

    single { JobPostingRepository() }
    single { ApplicationRepository() }
    single { UserRepository() }

    single { CandidatesRepository(db = get()) }

    viewModel {
        CandidateSearchViewModel(
            jobRepository = get(),
            applicationRepository = get(),
            userRepository = get(),
            auth = get()
        )
    }

    viewModel { CandidatesViewModel(repository = get()) }
}