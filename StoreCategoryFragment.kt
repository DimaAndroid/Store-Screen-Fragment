package com.app.sample.home.store

import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import com.app.sample.R
import com.app.sample.base.BaseFragment
import com.app.sample.data.api.response.StoreProduct
import com.app.sample.databinding.FragmentStoreCategoryBinding
import com.app.sample.home.store.cart.CartFragment
import com.app.sample.home.store.details.ProductDetailsFragment
import com.app.sample.util.Const
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class StoreCategoryFragment(private val products: List<StoreProduct>) :
    BaseFragment<FragmentStoreCategoryBinding>() {

    override fun getFragmentLayout(): Int {
        return R.layout.fragment_store_category
    }

    private lateinit var storeProductsAdapter: StoreProductsAdapter
    private lateinit var gridLayoutManager: GridLayoutManager
    private val storeViewModel: StoreViewModel by viewModel()
    private val storeCategoryViewModel: StoreCategoryViewModel by viewModel()
    private val sharedPreferences: SharedPreferences by inject()
    private lateinit var addedToCartAnalyticsInfo: Bundle
    private lateinit var analytics: FirebaseAnalytics
    private var isDataReady = false

    private var addedProductPosition = -1

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        gridLayoutManager = GridLayoutManager(context, 2)

        binding?.recyclerViewProducts?.layoutManager = gridLayoutManager
        binding?.recyclerViewProducts?.adapter = storeProductsAdapter

        val isProductDataUpdateRequired =
            sharedPreferences.getBoolean(Const.SP_IS_UPDATE_REQUIRED, false)

        if (isProductDataUpdateRequired) {
            storeCategoryViewModel.changeRemovedItemsState()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding?.lifecycleOwner = this
        binding?.viewModel = storeViewModel
        binding?.storeCategoryViewModel = storeCategoryViewModel

        analytics = Firebase.analytics

        storeCategoryViewModel.proceedProductsData(products)

        storeProductsAdapter = StoreProductsAdapter()

        storeProductsAdapter.setOnProductClickListener(object :
            StoreProductsAdapter.StoreProductsOnClick {
            override fun onProductDetailsClick(product: StoreProduct) {
                val productDetailsFragment = ProductDetailsFragment()
                val args = Bundle()
                args.putString(Const.PRODUCT_LINK, product.productLink)
                args.putString(Const.PRODUCT_CART_LINK, product.productCartLink)
                args.putBoolean(Const.PRODUCT_CART_STATE, product.isAddedToCart)
                productDetailsFragment.arguments = args
                addChildFragment(productDetailsFragment, R.id.container)
            }

            override fun onProductAddToCartClick(product: StoreProduct, position: Int) {
                if (!product.isAddedToCart) {
                    storeCategoryViewModel.addToCart(product.productCartLink)
                    parentActivity.setIsResponseExecuted(true)
                    addedProductPosition = position
                    addedToCartAnalyticsInfo = Bundle()
                    addedToCartAnalyticsInfo.putString(Const.ADDED_TO_CART_NAME, product.name)
                    addedToCartAnalyticsInfo.putInt(Const.ADDED_TO_CART_PRICE, product.price)
                } else {
                    addChildFragment(CartFragment(), R.id.container)
                }
            }
        })

        initSubscribers()
    }

    private fun initSubscribers() {
        storeCategoryViewModel.productsData.observe(this, Observer {
            if (it != null && isDataReady) {
                storeProductsAdapter.notifyDataSetChanged()
                storeProductsAdapter.setItems(it)
                parentActivity.setIsResponseExecuted(false)
            } else {
                storeCategoryViewModel.proceedProductsData(products)
            }

            storeProductsAdapter.changeCartStateVisibility(View.VISIBLE)
        })

        storeViewModel.itemsCountResponse.observe(this, Observer {
            if (it.status) {
                it.data?.count?.let { count ->
                    parentActivity.updateCartCounter(count)
                } ?: run {
                    handleResponseError(it)
                }
            } else {
                handleResponseError(it)
            }
        })

        storeCategoryViewModel.orderResponse.observe(this@StoreCategoryFragment, Observer {
            if (it.status) {
                it.data?.orderResponseData?.let { data ->
                    storeCategoryViewModel.proceedOrderData(data)
                } ?: run {
                    handleResponseError(it)
                }

                storeViewModel.getItemsCount()
                parentActivity.setIsResponseExecuted(false)
            } else {
                handleResponseError(it)
            }
        })

        storeCategoryViewModel.additionResponse.observe(this, Observer {
            if (it.status) {
                storeProductsAdapter.changeCartState(addedProductPosition)
                storeProductsAdapter.notifyItemChanged(addedProductPosition)
                parentActivity.updateCartCounter(parentActivity.getCartCounter() + 1)
                parentActivity.setIsResponseExecuted(false)
                analytics.logEvent(Const.ADDED_TO_CART_ANALYTICS_EVENT, addedToCartAnalyticsInfo)
            } else {
                handleResponseError(it)
            }
        })

        storeCategoryViewModel.orderData.observe(this, Observer {
            isDataReady = true
            storeCategoryViewModel.sortAndUpdate()
        })
    }
}