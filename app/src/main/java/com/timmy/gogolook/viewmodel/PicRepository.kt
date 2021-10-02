package com.timmy.gogolook.viewmodel

import androidx.lifecycle.MutableLiveData
import com.timmy.gogolook.api.ApiService
import com.timmy.gogolook.api.model.Hit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import timber.log.Timber
import javax.inject.Inject

class PicRepository @Inject constructor() {

    @Inject
    lateinit var retrofit: Retrofit


    private val apiService by lazy {
        retrofit.create(ApiService::class.java)
    }


    private val result by lazy { MutableLiveData<MutableList<Hit>>() }

    fun getLiveDataByAPI() = result

    fun init() { // 初始化，用於偵測資料庫是否有變化，或直接回傳值
    }

    fun getDataFromAPI() {
        CoroutineScope(Dispatchers.IO).launch {
            val responseBody = apiService.getData()
//            Timber.i("getDataFromAPI結果是=>${responseBody} ")
            MainScope().launch {
                //處理畫面更新

                result.postValue(responseBody.hits)
            }
        }
    }


}