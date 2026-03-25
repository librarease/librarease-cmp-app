package com.example.feature.home.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.R

object MainTabRoutes {
    const val HOME = "tab/home"
    const val BOOKS = "tab/books"
    const val LIBRARIES = "tab/libraries"
    const val SETTINGS = "tab/settings"
}

private data class TabItem(
    val route: String,
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

private val tabItems = listOf(
    TabItem(MainTabRoutes.HOME, "Home", Icons.Outlined.Home),
    TabItem(MainTabRoutes.BOOKS, "Books", Icons.Outlined.MenuBook),
    TabItem(MainTabRoutes.LIBRARIES, "Libraries", Icons.Outlined.LocationOn),
    TabItem(MainTabRoutes.SETTINGS, "Settings", Icons.Outlined.Settings)
)

@Composable
fun MainBottomNavigationBar(
    currentRoute: String,
    onSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val surface = colorResource(id = R.color.surface_light)
    val glassTop = surface.copy(alpha = 0.32f)
    val glassBottom = surface.copy(alpha = 0.62f)
    val glassBorder = colorResource(id = R.color.border).copy(alpha = 0.32f)
    val highlight = Color.White.copy(alpha = 0.08f)
    val shape = RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                this.shape = shape
                clip = true
            }
            .border(
                width = 1.dp,
                color = glassBorder,
                shape = shape
            )
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(shape)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(glassTop, glassBottom)
                    )
                )
                .blur(18.dp)
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(shape)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(highlight, Color.Transparent)
                    )
                )
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            tabItems.forEach { item ->
                val isSelected = currentRoute == item.route
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { onSelected(item.route) }
                        .padding(horizontal = 6.dp)
                ) {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label,
                        tint = if (isSelected) {
                            Color(0xFF2E7D32)
                        } else {
                            colorResource(id = R.color.secondary)
                        }
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = item.label,
                        fontSize = 10.sp,
                        color = if (isSelected) {
                            Color(0xFF2E7D32)
                        } else {
                            colorResource(id = R.color.secondary)
                        }
                    )
                }
            }
        }
    }
}
