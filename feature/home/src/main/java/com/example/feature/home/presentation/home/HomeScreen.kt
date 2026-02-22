package com.example.feature.home.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Subscriptions
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.R

@Composable
@Suppress("UnusedParameter")
fun HomeScreen(
    onSignOutClick: () -> Unit = {}
) {
    val scrollState = rememberScrollState()
    var selectedExploreIndex by remember { mutableIntStateOf(0) }
    var selectedNavIndex by remember { mutableIntStateOf(0) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(id = com.example.core.R.color.background))
            .padding(top = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp)
                .padding(top = 28.dp, bottom = 110.dp)
        ) {
            GreetingHeader()

            Spacer(modifier = Modifier.height(16.dp))
            SearchBar()

            Spacer(modifier = Modifier.height(24.dp))
            SectionHeader(title = "Continue Reading", action = "VIEW ALL")
            Spacer(modifier = Modifier.height(12.dp))
            ContinueReadingCard()

            Spacer(modifier = Modifier.height(24.dp))
            SectionHeader(title = "Explore")
            Spacer(modifier = Modifier.height(12.dp))
            ExploreChips(
                selectedIndex = selectedExploreIndex,
                onSelected = { selectedExploreIndex = it }
            )

            Spacer(modifier = Modifier.height(24.dp))
            BooksRow()
        }

        BottomNavigationBar(
            selectedIndex = selectedNavIndex,
            onSelected = { selectedNavIndex = it },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(bottom = 12.dp)
        )
    }
}

@Composable
private fun GreetingHeader() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text = "Good Morning,",
                fontSize = 14.sp,
                color = colorResource(id = com.example.core.R.color.secondary)
            )
            Text(
                text = "Isabella",
                fontSize = 24.sp,
                fontWeight = FontWeight.SemiBold,
                color = colorResource(id = com.example.core.R.color.primary)
            )
        }

        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(colorResource(id = com.example.core.R.color.surface_light)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "I",
                fontWeight = FontWeight.Bold,
                color = colorResource(id = com.example.core.R.color.primary)
            )
        }
    }
}

@Composable
private fun SearchBar() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(colorResource(id = com.example.core.R.color.surface_light))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Outlined.Search,
            contentDescription = "Search",
            tint = colorResource(id = com.example.core.R.color.secondary)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "Search titles, authors, ISBN...",
            color = colorResource(id = com.example.core.R.color.secondary),
            fontSize = 14.sp
        )
    }
}

@Composable
private fun SectionHeader(
    title: String,
    action: String? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = colorResource(id = com.example.core.R.color.primary)
        )
        if (action != null) {
            Text(
                text = action,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = colorResource(id = com.example.core.R.color.secondary)
            )
        }
    }
}

@Composable
private fun ContinueReadingCard() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(colorResource(id = com.example.core.R.color.surface_light))
            .padding(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(colorResource(id = com.example.core.R.color.border)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "TL",
                    fontWeight = FontWeight.Bold,
                    color = colorResource(id = com.example.core.R.color.primary)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "The Midnight Library",
                    fontWeight = FontWeight.SemiBold,
                    color = colorResource(id = com.example.core.R.color.primary),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "Matt Haig",
                    fontSize = 12.sp,
                    color = colorResource(id = com.example.core.R.color.secondary)
                )
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = 0.65f,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = colorResource(id = com.example.core.R.color.accent),
                    trackColor = colorResource(id = com.example.core.R.color.border)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "12 days left",
                    fontSize = 11.sp,
                    color = colorResource(id = com.example.core.R.color.secondary)
                )
            }
        }
    }
}

@Composable
private fun ExploreChips(
    selectedIndex: Int,
    onSelected: (Int) -> Unit
) {
    val categories = listOf("All Books", "Classics", "Sci-Fi", "Biography")
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        categories.forEachIndexed { index, label ->
            val isSelected = index == selectedIndex
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        if (isSelected) {
                            colorResource(id = com.example.core.R.color.primary)
                        } else {
                            colorResource(id = com.example.core.R.color.background)
                        }
                    )
                    .border(
                        width = 1.dp,
                        color = colorResource(id = com.example.core.R.color.border),
                        shape = RoundedCornerShape(20.dp)
                    )
                    .clickable { onSelected(index) }
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Text(
                    text = label,
                    fontSize = 12.sp,
                    color = if (isSelected) {
                        colorResource(id = com.example.core.R.color.surface_light)
                    } else {
                        colorResource(id = com.example.core.R.color.primary)
                    }
                )
            }
        }
    }
}

@Composable
private fun BooksRow() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        BookCoverCard(
            color = 0xFFB28A6F.toInt(),
            modifier = Modifier.weight(1f)
        )
        BookCoverCard(
            color = 0xFFF2C94C.toInt(),
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun BookCoverCard(
    color: Int,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(210.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(androidx.compose.ui.graphics.Color(color))
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(10.dp)
                .size(28.dp)
                .clip(CircleShape)
                .background(colorResource(id = R.color.background)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.BookmarkBorder,
                contentDescription = "Bookmark",
                tint = colorResource(id = com.example.core.R.color.primary),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
private fun BottomNavigationBar(
    selectedIndex: Int,
    onSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val items = listOf(
        BottomNavItem("Home", Icons.Outlined.Home),
        BottomNavItem("Subscriptions", Icons.Outlined.Subscriptions),
        BottomNavItem("Libraries", Icons.Outlined.MenuBook),
        BottomNavItem("Settings", Icons.Outlined.Settings)
    )

    val glassColor = colorResource(id = com.example.core.R.color.surface_light).copy(alpha = 0.78f)
    val glassBorder = colorResource(id = com.example.core.R.color.border).copy(alpha = 0.7f)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp))
            .background(glassColor)
            .border(width = 1.dp, color = glassBorder, shape = RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp))
            .padding(vertical = 14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEachIndexed { index, item ->
                val isSelected = index == selectedIndex
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { onSelected(index) }
                        .padding(horizontal = 6.dp)
                ) {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label,
                        tint = if (isSelected) {
                            colorResource(R.color.accent)
                        } else {
                            colorResource(id = R.color.secondary)
                        }
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = item.label,
                        fontSize = 10.sp,
                        color = if (isSelected) {
                            colorResource(id = R.color.accent)
                        } else {
                            colorResource(id = R.color.secondary)
                        }
                    )
                }
            }
        }
    }
}

private data class BottomNavItem(
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun HomeScreenPreview() {
    HomeScreen()
}
