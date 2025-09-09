package com.hater.githubsearch.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView.Adapter
import com.hater.githubsearch.R
import com.hater.githubsearch.databinding.ViewholderSearchUserBinding
import com.hater.githubsearch.model.GithubUser
import com.hater.githubsearch.model.UserInfo
import com.hater.githubsearch.ui.viewholder.SearchUserViewHolder
import com.hater.githubsearch.util.AppUtil
import com.hater.githubsearch.util.ImageLoader
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class SearchUserAdapter: PagingDataAdapter<UserInfo, SearchUserViewHolder>(SearchUserDiffCallback) {

    private var targetImageSizePx: Int = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchUserViewHolder {
        targetImageSizePx = AppUtil.dpToPx(parent.context, 90)

        return SearchUserViewHolder(
            ViewholderSearchUserBinding.inflate(LayoutInflater.from(parent.context), parent, false),
            targetImageSizePx
        )
    }

    override fun onBindViewHolder(holder: SearchUserViewHolder, position: Int) {
        val user = getItem(position)
        user?.let {
            holder.bind(user)
        }
    }

    override fun onViewRecycled(holder: SearchUserViewHolder) {
        super.onViewRecycled(holder)
        holder.imageLoadJob?.cancel()
    }

    companion object {
        private val SearchUserDiffCallback = object : DiffUtil.ItemCallback<UserInfo>() {
            override fun areItemsTheSame(oldItem: UserInfo, newItem: UserInfo): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: UserInfo, newItem: UserInfo): Boolean {
                return oldItem == newItem
            }
        }
    }

}