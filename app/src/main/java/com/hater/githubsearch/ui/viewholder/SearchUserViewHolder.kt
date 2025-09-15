package com.hater.githubsearch.ui.viewholder

import android.content.Intent
import android.net.Uri
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.hater.githubsearch.MainActivity
import com.hater.githubsearch.R
import com.hater.githubsearch.UserDetailActivity
import com.hater.githubsearch.databinding.ViewholderSearchUserBinding
import com.hater.githubsearch.model.UserInfo
import com.hater.githubsearch.util.ImageLoader
import com.hater.githubsearch.util.loadUrl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch


class SearchUserViewHolder(
    private val binding:ViewholderSearchUserBinding
): RecyclerView.ViewHolder(binding.root) {

    fun bind(githubUser: UserInfo) {
        binding.userProfile.loadUrl(githubUser.avatarUrl)
        binding.userName.text = githubUser.login
        binding.repoCount.text = String.format(itemView.context.getString(R.string.repo_count, githubUser.publicRepoCount))
        binding.root.setOnClickListener {
            val intent = Intent(itemView.context, UserDetailActivity::class.java)
            intent.putExtra(MainActivity.KEY_USER_URL, githubUser.htmlUrl)
            itemView.context.startActivity(intent)
        }
    }
}
