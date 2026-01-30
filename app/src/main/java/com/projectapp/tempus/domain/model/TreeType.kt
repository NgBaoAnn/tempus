package com.projectapp.tempus.domain.model


enum class TreeType(
    val displayName: String,
    val costToPlant: Int,
    val description: String,
    val emoji: String
) {
    OAK("Cây Sồi", 50, "Cây cơ bản, dễ chăm sóc", "🌳"),
    PINE("Cây Thông", 75, "Cây mùa đông, xanh quanh năm", "🌲"),
    SAKURA("Hoa Anh Đào", 100, "Cây hoa đẹp, nở hoa mùa xuân", "🌸"),
    BAMBOO("Cây Tre", 60, "Cây châu Á, mọc nhanh", "🎋"),
    PALM("Cây Cọ", 50, "Cây nhiệt đới, dễ trồng", "🌴"),
    COCONUT("Cây Dừa", 90, "Cây nhiệt đới, cho trái dừa", "🥥"),
    APPLE("Cây Táo", 120, "Cây ăn quả, thưởng bonus khi trưởng thành", "🍎");
    
    companion object {
        fun fromString(type: String): TreeType {
            return entries.firstOrNull { it.name == type } ?: OAK
        }
        
        
        fun getAffordableTrees(currentPoints: Int): List<TreeType> {
            return entries.filter { currentPoints >= it.costToPlant }
        }
    }
}
