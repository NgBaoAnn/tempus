package com.projectapp.tempus.ui.garden.compose

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.projectapp.tempus.domain.model.TreeState
import com.projectapp.tempus.ui.garden.compose.drawing.ProceduralTreeSize


@Composable
fun TreeCard(
    tree: TreeUiModel,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(0.9f)
            .animateContentSize(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp,
            pressedElevation = 8.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        getTreeBackgroundColor(tree.state).copy(alpha = 0.08f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                ProceduralTree(
                    treeType = tree.type,
                    growthStage = tree.state,
                    treeId = tree.id,
                    size = ProceduralTreeSize.MEDIUM.dp
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            
            Text(
                text = tree.name,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                
                Text(
                    text = "${tree.state.emoji} ${tree.state.displayName}",
                    style = MaterialTheme.typography.labelSmall,
                    color = getTreeBackgroundColor(tree.state)
                )
                
                
                Text(
                    text = "${tree.progressPercent.toInt()}%",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            
            if (tree.isAlive && tree.daysUntilDeath <= 2 && tree.state != TreeState.TREE) {
                Text(
                    text = if (tree.daysUntilDeath == 0) "⚠️ Tưới ngay!" else "⚠️ ${tree.daysUntilDeath} ngày",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFFFF9500),
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun getTreeBackgroundColor(state: TreeState): Color {
    return when (state) {
        TreeState.SEED -> Color(0xFF8B4513)
        TreeState.SPROUT -> Color(0xFF32CD32)
        TreeState.SAPLING -> Color(0xFF228B22)
        TreeState.TREE -> Color(0xFF006400)
        TreeState.DEAD -> Color(0xFF696969)
    }
}
