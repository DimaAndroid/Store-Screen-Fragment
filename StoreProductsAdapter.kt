package com.app.sample.home.store

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.app.sample.R
import com.app.sample.base.BaseRecyclerAdapter
import com.app.sample.base.BaseViewHolder
import com.app.sample.data.api.response.StoreProduct
import com.app.sample.databinding.ItemProductBinding

class StoreProductsAdapter :
    BaseRecyclerAdapter<ItemProductBinding, StoreProduct, StoreProductsAdapter.StoreCategoryViewHolder>() {

    lateinit var listener: StoreProductsOnClick
    var cartStateVisibility: Int = View.INVISIBLE

    interface StoreProductsOnClick {
        fun onProductDetailsClick(product: StoreProduct)
        fun onProductAddToCartClick(product: StoreProduct, position: Int)
    }

    fun setOnProductClickListener(productOnClick: StoreProductsOnClick) {
        this.listener = productOnClick
    }

    fun changeCartStateVisibility(visibility: Int) {
        this.cartStateVisibility = visibility
    }

    fun changeCartState(position: Int) {
        items[position].isAddedToCart = true
    }

    override fun getBinding(inflater: LayoutInflater, parent: ViewGroup): ItemProductBinding {
        return DataBindingUtil.inflate(inflater, R.layout.item_product, parent, false)
    }

    override fun getViewHolder(binding: ItemProductBinding): StoreCategoryViewHolder {
        return StoreCategoryViewHolder(binding)
    }

    inner class StoreCategoryViewHolder(itemProductBinding: ItemProductBinding) :
        BaseViewHolder<ItemProductBinding, StoreProduct>(itemProductBinding) {
        override fun bind(data: StoreProduct, position: Int) {
            binding.product = data
            binding.imageProduct.clipToOutline = true

            binding.mainLayoutProduct.setOnClickListener {
                listener.onProductDetailsClick(data)
            }

            binding.viewCart.setOnClickListener {
                listener.onProductAddToCartClick(data, position)
            }

            val context = binding.mainLayoutProduct.context

            if (!data.isAddedToCart) {
                binding.addDoneMorph.background = ContextCompat.getDrawable(context, R.drawable.ic_add)
            } else {
                binding.addDoneMorph.background = ContextCompat.getDrawable(context, R.drawable.ic_shopping_cart_black_24dp)
            }

            binding.viewCart.visibility = cartStateVisibility
        }
    }
}