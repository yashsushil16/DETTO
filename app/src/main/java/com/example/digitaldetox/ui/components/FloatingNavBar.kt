package com.example.digitaldetox.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.digitaldetox.ui.theme.GlassBackground
import com.example.digitaldetox.ui.theme.White

data class NavItem(val title: String, val route: String, val icon: ImageVector?)

@Composable
fun FloatingNavBar(
    items: List<NavItem>,
    currentRoute: String?,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .padding(24.dp)
            .fillMaxWidth()
            .clip(CircleShape)
            .background(GlassBackground)
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        items.forEach { item ->
            val isSelected = currentRoute == item.route
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable { onNavigate(item.route) }
                    .padding(8.dp)
            ) {
                // In a real app we'd use Icons.Filled.X, but for now we'll just use text labels
                // since we didn't add material-icons-extended to build.gradle to keep size small.
                Text(
                    text = item.title,
                    color = if (isSelected) White else Color.Gray
                )
            }
        }
    }
}
