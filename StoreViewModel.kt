package com.app.sample.home.store

import android.content.SharedPreferences
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.app.sample.data.api.ResponseWrap
import com.app.sample.data.api.response.ItemsCountResponse
import com.app.sample.data.api.response.ProductsData
import com.app.sample.data.api.response.ProductsResponse
import com.app.sample.data.repository.Repository
import com.app.sample.util.Const

class StoreViewModel(private val repository: Repository, preferences: SharedPreferences) :
    ViewModel() {

    var token: String? = Const.EMPTY_STRING

    val productsResponse: MutableLiveData<ResponseWrap<ProductsResponse>> = MutableLiveData()
    val productsResponseData: MutableLiveData<List<ProductsData>> = MutableLiveData()
    val itemsCountResponse: MutableLiveData<ResponseWrap<ItemsCountResponse>> = MutableLiveData()

    init {
        token = preferences.getString(Const.S_PREF_TOKEN, Const.EMPTY_STRING)
        repository.getProducts(token, productsResponse)
    }

    fun proceedProductsResponseData(productsData: List<ProductsData>) {
        this.productsResponseData.postValue(productsData)
    }

    fun getItemsCount() {
        repository.getItemsCount(token, itemsCountResponse)
    }
}