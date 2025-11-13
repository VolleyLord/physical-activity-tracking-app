package com.volleylord.gps_tracker.di

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.android.gms.location.FusedLocationProviderClient
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import org.junit.Before
import org.junit.Assert.assertNotNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class HiltModulesSmokeTest {

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Inject lateinit var firebaseAuth: FirebaseAuth
    @Inject lateinit var firestore: FirebaseFirestore
    @Inject lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    @Inject @DefaultDispatcher lateinit var defaultDispatcher: CoroutineDispatcher
    @Inject @IoDispatcher lateinit var ioDispatcher: CoroutineDispatcher

    @Before
    fun setUp() {
        hiltRule.inject()
    }

    @Test
    fun verifyFirebaseDependenciesAreInjected() {
        assertNotNull(firebaseAuth)
        assertNotNull(firestore)
    }

    @Test
    fun verifyLocationDependenciesAreInjected() {
        assertNotNull(fusedLocationProviderClient)
    }

    @Test
    fun verifyCoroutineDispatchersAreInjected() {
        assertNotNull(defaultDispatcher)
        assertNotNull(ioDispatcher)
    }
}

