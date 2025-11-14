package com.volleylord.gps_tracker.di

import com.volleylord.gps_tracker.data.repository.ActivitySessionRepositoryImpl
import com.volleylord.gps_tracker.data.repository.FirebaseAuthRepositoryImpl
import com.volleylord.gps_tracker.domain.repository.ActivitySessionRepository
import com.volleylord.gps_tracker.domain.repository.AuthRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * DI module for binding repository implementations.
 */
@Module
@InstallIn(SingletonComponent::class)
interface RepositoryModule {

    @Binds
    @Singleton
    fun bindAuthRepository(
        impl: FirebaseAuthRepositoryImpl
    ): AuthRepository

    @Binds
    @Singleton
    fun bindActivitySessionRepository(
        impl: ActivitySessionRepositoryImpl
    ): ActivitySessionRepository
}

