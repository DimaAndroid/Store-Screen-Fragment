package com.app.sample.base


import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView

/**
 * Sets view to ViewHolder and binding object to local variable.
 *
 * @param binding - exemplar of TrainingData Binding class.
 */

/**
 * Provide access to binding class for child classes.
 *
 * @return - exemplar of TrainingData Binding.
 */

abstract class BaseViewHolder<T : ViewDataBinding, P>(protected val binding: T) : RecyclerView.ViewHolder(binding.root)
{

    /**
     * Method for binding data to the view.
     *
     * @param data - exemplar of data class.
     */
    abstract fun bind(data: P, position: Int)
}