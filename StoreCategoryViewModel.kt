package com.app.sample.home.store

import android.content.SharedPreferences
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.app.sample.data.api.ResponseWrap
import com.app.sample.data.api.response.OrderProduct
import com.app.sample.data.api.response.OrderResponse
import com.app.sample.data.api.response.OrderResponseData
import com.app.sample.data.api.response.StoreProduct
import com.app.sample.data.repository.Repository
import com.app.sample.util.Const
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken


class StoreCategoryViewModel(
    private val repository: Repository,
    private val sharedPreferences: SharedPreferences
) : ViewModel() {

    var token: String? = Const.EMPTY_STRING

    val productsData: MutableLiveData<List<StoreProduct>> = MutableLiveData()
    val orderData: MutableLiveData<OrderResponseData> = MutableLiveData()

    val orderResponse: MutableLiveData<ResponseWrap<OrderResponse>> = MutableLiveData()

    val additionResponse: MutableLiveData<ResponseWrap<OrderResponse>> = MutableLiveData()

    fun proceedProductsData(productsData: List<StoreProduct>) {
        this.productsData.postValue(productsData)
    }

    fun proceedOrderData(orderProductsData: OrderResponseData?) {
        this.orderData.postValue(orderProductsData)
    }

    init {
        token = sharedPreferences.getString(Const.S_PREF_TOKEN, Const.EMPTY_STRING)
        repository.getUserOrder(token, orderResponse)
    }

    fun addToCart(url: String) {
        repository.addToCart(token, url, additionResponse)
    }

    fun sortAndUpdate() {
        val productList = productsData.value
        val orderList = orderData.value?.products

        if (productList != null && orderList != null) {
            for (item in productList) {

                for (orderItem in orderList) {
                    if (item.id == orderItem.productId) {
                        item.isAddedToCart = true
                        break
                    }
                }

                if (orderList.isEmpty()) {
                    item.isAddedToCart = false
                }
            }
        }

        productsData.postValue(productList)
    }

    fun changeRemovedItemsState() {
        val json = sharedPreferences.getString(Const.SP_REMOVED_PRODUCTS_JSON, "")
        val type = object : TypeToken<List<OrderProduct>>() {}.type

        val list: List<OrderProduct> = Gson().fromJson(json, type)

        val productList = productsData.value

        if (productList != null) {
            for (product in productList) {
                for (removedItem in list) {
                    if (product.id == removedItem.productId) {
                        product.isAddedToCart = false
                    }

                }
            }
        }

        productsData.postValue(productList)
    }
}
