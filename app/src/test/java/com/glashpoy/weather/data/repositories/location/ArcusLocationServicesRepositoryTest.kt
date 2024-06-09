package com.glashpoy.weather.data.repositories.location

import com.glashpoy.weather.di.NetworkModule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ArcusLocationServicesRepositoryTest {

    private val repository = ArcusLocationServicesRepository(
        locationClient = NetworkModule.provideLocationClient()
    )

    @Test
    fun `A valid query to fetch suggested places should successfully fetch list of suggestions`() =
        runTest {
            val result = repository.fetchSuggestedPlacesForQuery(query = "GooglePlex")
            advanceUntilIdle()
            assert(result.isSuccess)
        }
}
