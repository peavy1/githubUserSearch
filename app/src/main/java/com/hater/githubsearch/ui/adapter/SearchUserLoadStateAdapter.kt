package com.hater.githubsearch.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.LoadState
import androidx.paging.LoadStateAdapter
import com.hater.githubsearch.databinding.ItemLoadStateBinding
import com.hater.githubsearch.ui.viewholder.SearchUserLoadStateViewHolder


class SearchUserLoadStateAdapter(
    private val retry: () -> Unit
) : LoadStateAdapter<SearchUserLoadStateViewHolder>() {

    override fun onBindViewHolder(holder: SearchUserLoadStateViewHolder, loadState: LoadState) {
        holder.bind(loadState)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        loadState: LoadState
    ): SearchUserLoadStateViewHolder {
        return SearchUserLoadStateViewHolder(
            ItemLoadStateBinding.inflate(LayoutInflater.from(parent.context), parent, false),
            retry
        )
    }

}