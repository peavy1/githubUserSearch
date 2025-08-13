package com.hater.githubsearch.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView.Adapter
import com.hater.githubsearch.R
import com.hater.githubsearch.databinding.ViewholderSearchUserBinding
import com.hater.githubsearch.model.GithubUser
import com.hater.githubsearch.ui.viewholder.SearchUserViewHolder

class SearchUserAdapter: PagingDataAdapter<GithubUser, SearchUserViewHolder>(SearchUserDiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchUserViewHolder {
        return SearchUserViewHolder(
            ViewholderSearchUserBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: SearchUserViewHolder, position: Int) {
        val user = getItem(position)
        user?.let {
            holder.bind(user)
        }
    }

    companion object {
        private val SearchUserDiffCallback = object : DiffUtil.ItemCallback<GithubUser>() {
            override fun areItemsTheSame(oldItem: GithubUser, newItem: GithubUser): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: GithubUser, newItem: GithubUser): Boolean {
                return oldItem == newItem
            }
        }
    }

}