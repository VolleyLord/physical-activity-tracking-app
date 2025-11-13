package com.volleylord.gps_tracker.di

import com.volleylord.gps_tracker.domain.usecase.ObserveActivityHistoryUseCase
import com.volleylord.gps_tracker.domain.usecase.ObserveAuthStateUseCase
import com.volleylord.gps_tracker.domain.usecase.ObserveCurrentSessionUseCase
import com.volleylord.gps_tracker.domain.usecase.SignInUseCase
import com.volleylord.gps_tracker.domain.usecase.SignOutUseCase
import com.volleylord.gps_tracker.domain.usecase.StartActivityTrackingUseCase
import com.volleylord.gps_tracker.domain.usecase.StopActivityTrackingUseCase
import com.volleylord.gps_tracker.domain.usecase.impl.ObserveActivityHistoryUseCaseImpl
import com.volleylord.gps_tracker.domain.usecase.impl.ObserveAuthStateUseCaseImpl
import com.volleylord.gps_tracker.domain.usecase.impl.ObserveCurrentSessionUseCaseImpl
import com.volleylord.gps_tracker.domain.usecase.impl.SignInUseCaseImpl
import com.volleylord.gps_tracker.domain.usecase.impl.SignOutUseCaseImpl
import com.volleylord.gps_tracker.domain.usecase.impl.StartActivityTrackingUseCaseImpl
import com.volleylord.gps_tracker.domain.usecase.impl.StopActivityTrackingUseCaseImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
interface UseCaseModule {

    @Binds
    fun bindObserveAuthStateUseCase(
        impl: ObserveAuthStateUseCaseImpl
    ): ObserveAuthStateUseCase

    @Binds
    fun bindSignInUseCase(
        impl: SignInUseCaseImpl
    ): SignInUseCase

    @Binds
    fun bindSignOutUseCase(
        impl: SignOutUseCaseImpl
    ): SignOutUseCase

    @Binds
    fun bindStartActivityTrackingUseCase(
        impl: StartActivityTrackingUseCaseImpl
    ): StartActivityTrackingUseCase

    @Binds
    fun bindStopActivityTrackingUseCase(
        impl: StopActivityTrackingUseCaseImpl
    ): StopActivityTrackingUseCase

    @Binds
    fun bindObserveCurrentSessionUseCase(
        impl: ObserveCurrentSessionUseCaseImpl
    ): ObserveCurrentSessionUseCase

    @Binds
    fun bindObserveActivityHistoryUseCase(
        impl: ObserveActivityHistoryUseCaseImpl
    ): ObserveActivityHistoryUseCase
}

