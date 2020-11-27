package com.app.sample.home.store

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.app.sample.R
import com.app.sample.base.BaseRecyclerAdapter
import com.app.sample.base.BaseViewHolder
import com.app.sample.data.api.response.ProductsData
import com.app.sample.databinding.ItemStoreCategoryBinding
import com.app.sample.util.getColorFromAttr
import com.app.sample.util.setBackgroundTint
import com.app.sample.util.setCustomTextColor

class StoreTabsAdapter :
    BaseRecyclerAdapter<ItemStoreCategoryBinding, ProductsData, StoreTabsAdapter.StoreTabsViewHolder>() {

    private lateinit var listener: OnCategoryChanged
    private var curViewPagerPosition = 0

    fun setOnCategoryChangedListener(onCategoryChangedListener: OnCategoryChanged) {
        this.listener = onCategoryChangedListener
    }

    fun setViewPagerCurrentPosition(position: Int) {
        curViewPagerPosition = position
    }

    override fun getBinding(inflater: LayoutInflater, parent: ViewGroup): ItemStoreCategoryBinding {
        return DataBindingUtil.inflate(inflater, R.layout.item_store_category, parent, false)
    }

    override fun getViewHolder(binding: ItemStoreCategoryBinding): StoreTabsViewHolder {
        return StoreTabsViewHolder(binding)
    }

    interface OnCategoryChanged {
        fun onCategoryChanged(position: Int)
    }

    inner class StoreTabsViewHolder(binding: ItemStoreCategoryBinding) :
        BaseViewHolder<ItemStoreCategoryBinding, ProductsData>(binding) {

        override fun bind(data: ProductsData, position: Int) {
            binding.productsData = data

            binding.layoutTab.setOnClickListener {
                listener.onCategoryChanged(position)
            }

            val context = binding.layoutTab.context

            val backgroundColor: Int
            val textColor: Int

            if (position == curViewPagerPosition) {
                backgroundColor = context.getColorFromAttr(R.attr.attrProfilePrimaryColor)
                textColor = android.R.color.white
            } else {
                backgroundColor = android.R.color.white
                textColor = R.color.grey1
            }

            binding.layoutTab.setBackgroundTint(backgroundColor)
            binding.textViewCategoryName.setCustomTextColor(textColor, context)
        }

    }
}