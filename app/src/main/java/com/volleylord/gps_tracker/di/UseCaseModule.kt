package com.volleylord.gps_tracker.di

import com.volleylord.gps_tracker.domain.usecase.ObserveActivityHistoryUseCase
import com.volleylord.gps_tracker.domain.usecase.ObserveAuthStateUseCase
import com.volleylord.gps_tracker.domain.usecase.ObserveCurrentSessionUseCase
import com.volleylord.gps_tracker.domain.usecase.ObserveSessionDetailsUseCase
import com.volleylord.gps_tracker.domain.usecase.PauseActivityTrackingUseCase
import com.volleylord.gps_tracker.domain.usecase.ResumeActivityTrackingUseCase
import com.volleylord.gps_tracker.domain.usecase.SignInUseCase
import com.volleylord.gps_tracker.domain.usecase.SignInWithGoogleUseCase
import com.volleylord.gps_tracker.domain.usecase.SignOutUseCase
import com.volleylord.gps_tracker.domain.usecase.SignUpUseCase
import com.volleylord.gps_tracker.domain.usecase.StartActivityTrackingUseCase
import com.volleylord.gps_tracker.domain.usecase.StopActivityTrackingUseCase
import com.volleylord.gps_tracker.domain.usecase.UpdateSessionNotesUseCase
import com.volleylord.gps_tracker.domain.usecase.impl.ObserveActivityHistoryUseCaseImpl
import com.volleylord.gps_tracker.domain.usecase.impl.ObserveAuthStateUseCaseImpl
import com.volleylord.gps_tracker.domain.usecase.impl.ObserveCurrentSessionUseCaseImpl
import com.volleylord.gps_tracker.domain.usecase.impl.PauseActivityTrackingUseCaseImpl
import com.volleylord.gps_tracker.domain.usecase.impl.ResumeActivityTrackingUseCaseImpl
import com.volleylord.gps_tracker.domain.usecase.impl.UpdateSessionNotesUseCaseImpl
import com.volleylord.gps_tracker.domain.usecase.impl.ObserveSessionDetailsUseCaseImpl
import com.volleylord.gps_tracker.domain.usecase.impl.SignInUseCaseImpl
import com.volleylord.gps_tracker.domain.usecase.impl.SignInWithGoogleUseCaseImpl
import com.volleylord.gps_tracker.domain.usecase.impl.SignOutUseCaseImpl
import com.volleylord.gps_tracker.domain.usecase.impl.SignUpUseCaseImpl
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
    fun bindSignInWithGoogleUseCase(
        impl: SignInWithGoogleUseCaseImpl
    ): SignInWithGoogleUseCase

    @Binds
    fun bindSignUpUseCase(
        impl: SignUpUseCaseImpl
    ): SignUpUseCase

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

    @Binds
    fun bindPauseActivityTrackingUseCase(
        impl: PauseActivityTrackingUseCaseImpl
    ): PauseActivityTrackingUseCase

    @Binds
    fun bindResumeActivityTrackingUseCase(
        impl: ResumeActivityTrackingUseCaseImpl
    ): ResumeActivityTrackingUseCase

    @Binds
    fun bindObserveSessionDetailsUseCase(
        impl: ObserveSessionDetailsUseCaseImpl
    ): ObserveSessionDetailsUseCase

    @Binds
    fun bindUpdateSessionNotesUseCase(
        impl: UpdateSessionNotesUseCaseImpl
    ): UpdateSessionNotesUseCase
}

