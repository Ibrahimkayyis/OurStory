package com.dicoding.picodiploma.loginwithanimation

import androidx.paging.PagingData
import androidx.paging.PagingSource
import androidx.paging.PagingState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

// Fungsi untuk membuat PagingData dari list
fun <T: Any> makePagingData(items: List<T>): PagingData<T> {
    return PagingData.from(items)
}

// Contoh jika Anda ingin membuat Flow<PagingData<T>>
fun <T: Any> makePagingDataFlow(items: List<T>): Flow<PagingData<T>> {
    val pagingData = PagingData.from(items)
    return flowOf(pagingData)
}
