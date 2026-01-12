package com.projectapp.tempus.ui.garden

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.projectapp.tempus.data.gamification.SupabaseGamificationRepository
import com.projectapp.tempus.data.gamification.entity.TreeEntity
import com.projectapp.tempus.databinding.FragmentGardenBinding
import com.projectapp.tempus.domain.model.TreeState
import com.projectapp.tempus.domain.model.TreeType
import com.projectapp.tempus.domain.usecase.PointsManager
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * Fragment hiển thị vườn cây của người dùng
 * Enhanced với Stats Summary, Pull-to-Refresh, và Delete Tree
 */
class GardenFragment : Fragment() {

    private var _binding: FragmentGardenBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var pointsManager: PointsManager
    private lateinit var treeAdapter: TreeAdapter
    
    // Stats tracking
    private var totalTrees = 0
    private var matureTrees = 0
    private var totalInvested = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGardenBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Initialize repository and manager
        val repository = SupabaseGamificationRepository()
        pointsManager = PointsManager(repository)
        
        setupRecyclerView()
        setupSwipeRefresh()
        setupFab()
        observeData()
        
        // Check for dead trees on startup
        lifecycleScope.launch {
            pointsManager.checkAndUpdateDeadTrees()
        }
    }

    private fun setupRecyclerView() {
        treeAdapter = TreeAdapter(
            onClick = { tree -> showTreeDetails(tree) },
            onLongClick = { tree -> showDeleteDialog(tree) }
        )
        
        binding.rvTrees.apply {
            layoutManager = GridLayoutManager(requireContext(), 2)
            adapter = treeAdapter
        }
    }
    
    private fun setupSwipeRefresh() {
        binding.swipeRefresh.setColorSchemeColors(
            resources.getColor(android.R.color.holo_green_dark, null)
        )
        binding.swipeRefresh.setOnRefreshListener {
            refreshData()
        }
    }
    
    private fun refreshData() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                pointsManager.checkAndUpdateDeadTrees()
                // Data will auto-update via Flow observers
            } finally {
                binding.swipeRefresh.isRefreshing = false
            }
        }
    }
    
    private fun setupFab() {
        binding.fabPlantTree.setOnClickListener {
            showPlantTreeDialog()
        }
    }
    
    private fun observeData() {
        // Observe points
        viewLifecycleOwner.lifecycleScope.launch {
            pointsManager.getUserPoints().collectLatest { userPoints ->
                userPoints?.let {
                    binding.pointsDisplay.setPoints(it.totalPoints)
                    binding.pointsDisplay.setStreak(it.currentStreak)
                }
            }
        }
        
        // Observe trees and update stats
        viewLifecycleOwner.lifecycleScope.launch {
            pointsManager.getAliveTrees().collectLatest { trees ->
                treeAdapter.submitList(trees)
                updateStats(trees)
                
                // Show/hide empty state
                if (trees.isEmpty()) {
                    binding.emptyState.visibility = View.VISIBLE
                    binding.rvTrees.visibility = View.GONE
                } else {
                    binding.emptyState.visibility = View.GONE
                    binding.rvTrees.visibility = View.VISIBLE
                }
            }
        }
    }
    
    private fun updateStats(trees: List<TreeEntity>) {
        totalTrees = trees.size
        matureTrees = trees.count { 
            TreeState.fromString(it.state) == TreeState.TREE 
        }
        totalInvested = trees.sumOf { it.investedPoints }
        
        binding.tvTotalTrees.text = totalTrees.toString()
        binding.tvMatureTrees.text = matureTrees.toString()
        binding.tvInvestedPoints.text = totalInvested.toString()
    }
    
    private fun showPlantTreeDialog() {
        viewLifecycleOwner.lifecycleScope.launch {
            // Get current points
            val repository = SupabaseGamificationRepository()
            val currentPoints = repository.getUserPointsOnce()?.totalPoints ?: 0
            
            val affordableTrees = TreeType.getAffordableTrees(currentPoints)
            
            if (affordableTrees.isEmpty()) {
                Toast.makeText(
                    requireContext(),
                    "Cần ít nhất ${TreeType.OAK.costToPlant} điểm để trồng cây! (Hiện có: $currentPoints điểm)",
                    Toast.LENGTH_SHORT
                ).show()
                return@launch
            }
            
            val treeNames = affordableTrees.map { 
                "${it.emoji} ${it.displayName} (${it.costToPlant} điểm)" 
            }.toTypedArray()
            
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("🌱 Chọn loại cây (Bạn có $currentPoints điểm)")
                .setItems(treeNames) { _, which ->
                    val selectedType = affordableTrees[which]
                    plantTree(selectedType)
                }
                .setNegativeButton("Hủy", null)
                .show()
        }
    }
    
    private fun plantTree(type: TreeType) {
        viewLifecycleOwner.lifecycleScope.launch {
            val treeId = pointsManager.plantTree(type)
            
            if (treeId != null) {
                Toast.makeText(
                    requireContext(),
                    "🌱 Đã trồng ${type.displayName}!",
                    Toast.LENGTH_SHORT
                ).show()
                
                // Show floating -X points animation
                binding.pointsDisplay.showEarnedPoints(-type.costToPlant)
            } else {
                Toast.makeText(
                    requireContext(),
                    "Không đủ điểm để trồng cây!",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
    
    private fun showTreeDetails(tree: TreeEntity) {
        viewLifecycleOwner.lifecycleScope.launch {
            val treeInfo = pointsManager.getTreeInfo(tree.id)
            
            treeInfo?.let { info ->
                val message = buildString {
                    append("🌳 ${info.type.displayName}\n")
                    append("📊 Trạng thái: ${info.state.displayName}\n")
                    append("💰 Đã đầu tư: ${info.entity.investedPoints} điểm\n")
                    append("📈 Tiến độ: ${info.progressPercent.toInt()}%\n")
                    
                    info.pointsToNextLevel?.let { points ->
                        append("⬆️ Cần $points điểm để lên level\n")
                    }
                    
                    if (info.entity.isAlive && info.state != TreeState.TREE) {
                        append("⏰ Còn ${info.daysUntilDeath} ngày trước khi héo")
                    }
                }
                
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle(tree.name)
                    .setMessage(message)
                    .setPositiveButton("Tưới cây (10 điểm)") { _, _ ->
                        waterTree(tree.id)
                    }
                    .setNeutralButton("Xóa cây") { _, _ ->
                        showDeleteDialog(tree)
                    }
                    .setNegativeButton("Đóng", null)
                    .show()
            }
        }
    }
    
    private fun showDeleteDialog(tree: TreeEntity) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("🗑️ Xóa cây?")
            .setMessage("Bạn có chắc muốn xóa \"${tree.name}\"?\n\nHành động này không thể hoàn tác và bạn sẽ mất ${tree.investedPoints} điểm đã đầu tư.")
            .setPositiveButton("Xóa") { _, _ ->
                deleteTree(tree)
            }
            .setNegativeButton("Hủy", null)
            .show()
    }
    
    private fun deleteTree(tree: TreeEntity) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val repository = SupabaseGamificationRepository()
                repository.killTree(tree.id)
                
                Toast.makeText(
                    requireContext(),
                    "Đã xóa ${tree.name}",
                    Toast.LENGTH_SHORT
                ).show()
            } catch (e: Exception) {
                Toast.makeText(
                    requireContext(),
                    "Lỗi xóa cây: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
    
    private fun waterTree(treeId: Long) {
        viewLifecycleOwner.lifecycleScope.launch {
            val newState = pointsManager.waterTree(treeId)
            
            if (newState != null) {
                Toast.makeText(
                    requireContext(),
                    "💧 Đã tưới cây!",
                    Toast.LENGTH_SHORT
                ).show()
                binding.pointsDisplay.showEarnedPoints(-10)
                
                // Force refresh to update progress bar immediately
                forceRefreshTrees()
            } else {
                Toast.makeText(
                    requireContext(),
                    "Không đủ điểm để tưới cây!",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
    
    /**
     * Force refresh tree list from database
     */
    private fun forceRefreshTrees() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val repository = SupabaseGamificationRepository()
                val freshTrees = repository.getAliveTreesOnce()
                
                // Force adapter to update by clearing and resubmitting
                // Need to create a new list to trigger DiffUtil
                treeAdapter.submitList(null)
                treeAdapter.submitList(freshTrees.toList())
                
                updateStats(freshTrees)
            } catch (e: Exception) {
                android.util.Log.e("GardenFragment", "Error refreshing: ${e.message}")
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
