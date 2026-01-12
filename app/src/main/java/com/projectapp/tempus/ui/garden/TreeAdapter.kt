package com.projectapp.tempus.ui.garden

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.projectapp.tempus.data.gamification.entity.TreeEntity
import com.projectapp.tempus.databinding.ItemTreeCardBinding
import com.projectapp.tempus.domain.model.TreeGrowthCalculator
import com.projectapp.tempus.domain.model.TreeState
import com.projectapp.tempus.domain.model.TreeType

/**
 * Adapter cho RecyclerView hiển thị danh sách cây
 * Hỗ trợ click và long-click events
 */
class TreeAdapter(
    private val onClick: (TreeEntity) -> Unit,
    private val onLongClick: ((TreeEntity) -> Unit)? = null
) : ListAdapter<TreeEntity, TreeAdapter.TreeViewHolder>(TreeDiffCallback()) {

    private val treeCalculator = TreeGrowthCalculator()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TreeViewHolder {
        val binding = ItemTreeCardBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return TreeViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TreeViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class TreeViewHolder(
        private val binding: ItemTreeCardBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(tree: TreeEntity) {
            val state = TreeState.fromString(tree.state)
            val type = TreeType.fromString(tree.treeType)
            val progress = treeCalculator.getProgressPercent(tree.investedPoints)
            val daysUntilDeath = treeCalculator.getDaysUntilDeath(tree.lastWateredAt)

            // Tree View
            binding.treeView.setState(state, type, animate = false)

            // Name
            binding.tvTreeName.text = tree.name

            // State badge
            binding.tvTreeState.text = "${state.emoji} ${state.displayName}"
            binding.tvTreeState.setBackgroundColor(getStateColor(state))

            // Progress
            binding.progressBar.progress = progress.toInt()

            // Warning for dying trees
            if (tree.isAlive && daysUntilDeath <= 2 && state != TreeState.TREE) {
                binding.tvWarning.visibility = View.VISIBLE
                binding.tvWarning.text = when (daysUntilDeath) {
                    0 -> "⚠️ Tưới ngay hôm nay!"
                    1 -> "⚠️ Tưới trong 1 ngày!"
                    else -> "⚠️ Tưới trong $daysUntilDeath ngày!"
                }
            } else {
                binding.tvWarning.visibility = View.GONE
            }

            // Click listener
            itemView.setOnClickListener { onClick(tree) }
            
            // Long click listener for delete
            onLongClick?.let { callback ->
                itemView.setOnLongClickListener { 
                    callback(tree)
                    true 
                }
            }

            // Animation on bind
            itemView.alpha = 0f
            itemView.animate()
                .alpha(1f)
                .setDuration(300)
                .setStartDelay((bindingAdapterPosition * 50).toLong())
                .start()
        }

        private fun getStateColor(state: TreeState): Int {
            return when (state) {
                TreeState.SEED -> Color.parseColor("#8B4513")
                TreeState.SPROUT -> Color.parseColor("#32CD32")
                TreeState.SAPLING -> Color.parseColor("#228B22")
                TreeState.TREE -> Color.parseColor("#006400")
                TreeState.DEAD -> Color.parseColor("#696969")
            }
        }
    }

    class TreeDiffCallback : DiffUtil.ItemCallback<TreeEntity>() {
        override fun areItemsTheSame(oldItem: TreeEntity, newItem: TreeEntity): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: TreeEntity, newItem: TreeEntity): Boolean {
            return oldItem == newItem
        }
    }
}
