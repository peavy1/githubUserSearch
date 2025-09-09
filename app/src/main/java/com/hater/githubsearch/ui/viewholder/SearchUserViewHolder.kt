package com.hater.githubsearch.ui.viewholder

import android.content.Intent
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.hater.githubsearch.MainActivity
import com.hater.githubsearch.R
import com.hater.githubsearch.UserDetailActivity
import com.hater.githubsearch.databinding.ViewholderSearchUserBinding
import com.hater.githubsearch.model.GithubUser
import com.hater.githubsearch.model.UserInfo
import com.hater.githubsearch.util.ImageLoader
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SearchUserViewHolder(
    private val binding:ViewholderSearchUserBinding,
    private val targetImageSizePx: Int
): RecyclerView.ViewHolder(binding.root) {

    var imageLoadJob: Job? = null

    fun bind(githubUser: UserInfo) {
        imageLoadJob?.cancel()
        binding.userProfile.setImageBitmap(null)
        imageLoadJob = CoroutineScope(Dispatchers.Main).launch {
            ImageLoader.loadImage(githubUser.avatarUrl, targetImageSizePx, targetImageSizePx)?.let {
                binding.userProfile.setImageBitmap(it)
            }
        }

        binding.userName.text = githubUser.login
        binding.repoCount.text = String.format(itemView.context.getString(R.string.repo_count, githubUser.publicRepoCount))
        binding.root.setOnClickListener {
            val intent = Intent(itemView.context, UserDetailActivity::class.java)
            intent.putExtra(MainActivity.KEY_USER_URL, githubUser.htmlUrl)
            itemView.context.startActivity(intent)
        }
    }
}
