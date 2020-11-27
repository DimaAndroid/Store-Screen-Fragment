package com.app.sample.home.store

import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager.widget.ViewPager
import com.app.sample.util.Const
import com.app.sample.R
import com.app.sample.base.BaseFragment
import com.app.sample.data.api.response.ProductsData
import com.app.sample.databinding.FragmentStoreViewpagerBinding
import com.app.sample.home.navigation.NavActivity
import com.app.sample.util.Const
import com.app.sample.util.ViewPagerAdapter
import kotlinx.android.synthetic.main.fragment_store_viewpager.*
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class StoreViewPager : BaseFragment<FragmentStoreViewpagerBinding>(),
    NavActivity.FragmentOnBackPressed {

    private val storeViewModel: StoreViewModel by viewModel()
    private val sharedPreferences: SharedPreferences by inject()

    private var lastTabsAdapterPosition = 0
    private lateinit var viewPagerAdapter: ViewPagerAdapter
    lateinit var storeTabsAdapter: StoreTabsAdapter

    override fun getFragmentLayout(): Int {
        return R.layout.fragment_store_viewpager
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        storeViewModel.productsResponseData.observe(this, Observer {
            if (it != null) {
                val items: ArrayList<ProductsData> = ArrayList()

                for (productsData in it) {
                    if (productsData.products.isNotEmpty()) {
                        viewPagerAdapter.addFragment(
                            StoreCategoryFragment(productsData.products),
                            productsData.name
                        )

                        items.add(productsData)
                    }
                }

                setupTitlesAdapter(items)
            }

            binding?.storeViewPager?.adapter = viewPagerAdapter
            parentActivity.setIsResponseExecuted(false)
        })

        storeViewModel.itemsCountResponse.observe(this, Observer {
            if (it.status) {
                it.data?.count?.let { count ->
                    parentActivity.updateCartCounter(count)

                    if (count > 0) {
                        sharedPreferences.edit().putBoolean(Const.SP_CART_IS_EMPTY, false).apply()
                    } else {
                        sharedPreferences.edit().putBoolean(Const.SP_CART_IS_EMPTY, true).apply()
                    }
                } ?: run {
                    handleResponseError(it)
                }


            } else {
                handleResponseError(it)
            }
        })
    }

    private fun scrollTabsToLastPosition() {
        binding?.recyclerViewTabs?.postDelayed({
            binding?.recyclerViewTabs?.smoothScrollToPosition(lastTabsAdapterPosition)
        }, 250)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding?.lifecycleOwner = this
        binding?.viewModel = storeViewModel

        viewPagerAdapter = ViewPagerAdapter(childFragmentManager)
        storeTabsAdapter = StoreTabsAdapter()

        storeViewModel.getItemsCount()
        parentActivity.setIsResponseExecuted(true)
        scrollTabsToLastPosition()


        storeViewModel.productsResponse.observe(viewLifecycleOwner, Observer {
            if (it.status) {
                storeViewModel.proceedProductsResponseData(it.data?.productsData!!)
            } else {
                handleResponseError(it)
            }
        })

        storeViewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {

            }

            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
            }

            override fun onPageSelected(position: Int) {
                storeTabsAdapter.setViewPagerCurrentPosition(position)
                storeTabsAdapter.notifyDataSetChanged()
                lastTabsAdapterPosition = position
                recyclerViewTabs.smoothScrollToPosition(position)
            }
        })
    }

    private fun setupTitlesAdapter(data: List<ProductsData>) {
        storeTabsAdapter = StoreTabsAdapter()
        val linearLayoutManager =
            LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)

        storeTabsAdapter.setOnCategoryChangedListener(object : StoreTabsAdapter.OnCategoryChanged {
            override fun onCategoryChanged(position: Int) {
                storeViewPager.currentItem = position
            }
        })

        binding?.recyclerViewTabs?.layoutManager = linearLayoutManager
        binding?.recyclerViewTabs?.adapter = storeTabsAdapter
        storeTabsAdapter.setItems(data)
    }

    override fun onBackPressed(): Boolean {
        return false
    }

    override fun onResume() {
        super.onResume()
        parentActivity.setupStoreUI()
        parentActivity.setIsStoreScreenVisible(true)
    }

    override fun onStop() {
        super.onStop()
        parentActivity.setIsStoreScreenVisible(false)
        sharedPreferences.edit().putBoolean(Const.SP_IS_UPDATE_REQUIRED, false).apply()
        sharedPreferences.edit().remove(Const.SP_REMOVED_PRODUCTS_JSON)
    }
}