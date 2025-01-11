package com.dicoding.picodiploma.loginwithanimation.view.main

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.paging.AsyncPagingDataDiffer
import androidx.paging.PagingData
import com.dicoding.picodiploma.loginwithanimation.*
import com.dicoding.picodiploma.loginwithanimation.data.UserRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModelTest {

    // Menjalankan LiveData di thread utama saat test
    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    // Mengganti Dispatchers.Main dengan TestDispatcher
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Mock
    private lateinit var userRepository: UserRepository

    private lateinit var mainViewModel: MainViewModel

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        mainViewModel = MainViewModel(userRepository)
    }

    @Test
    fun `when getAllStoriesPaged() Success and Not Null, should return correct data`() = runTest {

        val dummyData = DataDummy.generateDummyStoriesEntity()
        val dummyPagingData = makePagingData(dummyData)


        val expectedFlow = flow {
            emit(dummyPagingData)
        }


        Mockito.`when`(userRepository.getAllStoriesPaged()).thenReturn(expectedFlow)


        val actualFlow = mainViewModel.getAllStoriesPaged()


        val actualPagingData = actualFlow.first()
        assertNotNull(actualPagingData)


        val differ = AsyncPagingDataDiffer(
            diffCallback = StoryDiffCallback(),
            updateCallback = NoopListUpdateCallback(),
            mainDispatcher = mainDispatcherRule.dispatcher,
            workerDispatcher = mainDispatcherRule.dispatcher
        )
        differ.submitData(actualPagingData)


        advanceUntilIdle()


        assertEquals(dummyData.size, differ.itemCount)


        assertNotNull(differ.snapshot())


        assertEquals(dummyData[0], differ.snapshot()[0])
    }

    @Test
    fun `when getAllStoriesPaged() Returns Empty, should return 0 item`() = runTest {
        // 1) Siapkan data kosong
        val emptyData = DataDummy.generateEmptyStoriesEntity()
        val emptyPagingData = makePagingData(emptyData)

        // 2) Flow palsu yang emit paging data kosong
        val expectedFlow = flow {
            emit(emptyPagingData)
        }

        // 3) Mock repository
        Mockito.`when`(userRepository.getAllStoriesPaged()).thenReturn(expectedFlow)

        // 4) Panggil ViewModel => Flow
        val actualFlow = mainViewModel.getAllStoriesPaged()

        // 5) Ambil emission pertama
        val actualPagingData = actualFlow.first()
        assertNotNull(actualPagingData)

        // 6) Masukkan ke AsyncPagingDataDiffer
        val differ = AsyncPagingDataDiffer(
            diffCallback = StoryDiffCallback(),
            updateCallback = NoopListUpdateCallback(),
            mainDispatcher = mainDispatcherRule.dispatcher,
            workerDispatcher = mainDispatcherRule.dispatcher
        )
        differ.submitData(actualPagingData)

        // 7) Jalankan coroutines
        advanceUntilIdle()

        // 8) Pastikan itemCount = 0
        assertEquals(0, differ.itemCount)
    }

    fun <T : Any> makePagingData(data: List<T>): PagingData<T> {
        return PagingData.from(data)
    }

}
