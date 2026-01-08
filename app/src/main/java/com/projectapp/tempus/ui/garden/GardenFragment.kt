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
import com.projectapp.tempus.domain.model.TreeType
import com.projectapp.tempus.domain.usecase.PointsManager
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * Fragment hiển thị vườn cây của người dùng
 */
class GardenFragment : Fragment() {

    private var _binding: FragmentGardenBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var pointsManager: PointsManager
    private lateinit var treeAdapter: TreeAdapter

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
        setupFab()
        observeData()
        
        // Check for dead trees on startup
        lifecycleScope.launch {
            pointsManager.checkAndUpdateDeadTrees()
        }
    }

    private fun setupRecyclerView() {
        treeAdapter = TreeAdapter { tree ->
            showTreeDetails(tree)
        }
        
        binding.rvTrees.apply {
            layoutManager = GridLayoutManager(requireContext(), 2)
            adapter = treeAdapter
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
        
        // Observe trees
        viewLifecycleOwner.lifecycleScope.launch {
            pointsManager.getAliveTrees().collectLatest { trees ->
                treeAdapter.submitList(trees)
                
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
                        append("⬆️ Cần ${points} điểm để lên level\n")
                    }
                    
                    if (info.entity.isAlive && info.state != com.projectapp.tempus.domain.model.TreeState.TREE) {
                        append("⏰ Còn ${info.daysUntilDeath} ngày trước khi héo")
                    }
                }
                
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle(tree.name)
                    .setMessage(message)
                    .setPositiveButton("Tưới cây (10 điểm)") { _, _ ->
                        waterTree(tree.id)
                    }
                    .setNegativeButton("Đóng", null)
                    .show()
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
            } else {
                Toast.makeText(
                    requireContext(),
                    "Không đủ điểm để tưới cây!",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
