package com.example.feature.libraries.presentation.libraries

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.SubcomposeAsyncImage
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import com.example.core.R
import com.example.core.model.book.LibraryInfo

@Composable
fun LibrariesScreen(
    uiState: LibrariesUiState,
    onCategorySelected: (LibraryCategory) -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    val libraries = when (uiState.selectedCategory) {
        LibraryCategory.MY -> uiState.myLibraries
        LibraryCategory.ALL -> uiState.allLibraries
    }
    val showSubscribedBadge = uiState.selectedCategory == LibraryCategory.ALL
    val subscribedIds = uiState.subscribedLibraryIds

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colorResource(id = R.color.background))
            .statusBarsPadding()
            .padding(horizontal = 20.dp)
            .padding(top = 16.dp, bottom = 24.dp)
    ) {
        Text(
            text = "Libraries",
            fontSize = 22.sp,
            fontWeight = FontWeight.SemiBold,
            color = colorResource(id = R.color.primary)
        )

        Spacer(modifier = Modifier.height(16.dp))

        LibraryFilterToggle(
            selectedCategory = uiState.selectedCategory,
            onCategorySelected = onCategorySelected
        )

        Spacer(modifier = Modifier.height(20.dp))

        when {
            uiState.isLoading && libraries.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = colorResource(id = R.color.accent))
                }
            }
            uiState.errorMessage != null && libraries.isEmpty() -> {
                ErrorCard(message = uiState.errorMessage, onRetry = onRetry)
            }
            libraries.isEmpty() -> {
                val message = if (uiState.selectedCategory == LibraryCategory.MY) {
                    "You do not have any subscribed libraries yet."
                } else {
                    "No libraries available right now."
                }
                EmptyCard(message = message)
            }
            else -> {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(libraries, key = { it.id }) { library ->
                        LibraryCard(
                            library = library,
                            showSubscribedBadge = showSubscribedBadge,
                            isSubscribed = subscribedIds.contains(library.id)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LibraryFilterToggle(
    selectedCategory: LibraryCategory,
    onCategorySelected: (LibraryCategory) -> Unit
) {
    val border = colorResource(id = R.color.border)
    val surface = colorResource(id = R.color.surface_light)
    val accent = colorResource(id = R.color.accent)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(surface)
            .border(1.dp, border, RoundedCornerShape(20.dp))
            .height(44.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        LibraryFilterChip(
            label = "My libraries",
            selected = selectedCategory == LibraryCategory.MY,
            accent = accent,
            modifier = Modifier.weight(1f),
            onClick = { onCategorySelected(LibraryCategory.MY) }
        )
        Box(
            modifier = Modifier
                .width(1.dp)
                .fillMaxHeight()
                .background(border.copy(alpha = 0.6f))
        )
        LibraryFilterChip(
            label = "All libraries",
            selected = selectedCategory == LibraryCategory.ALL,
            accent = accent,
            modifier = Modifier.weight(1f),
            onClick = { onCategorySelected(LibraryCategory.ALL) }
        )
    }
}

@Composable
private fun LibraryFilterChip(
    label: String,
    selected: Boolean,
    accent: Color,
    modifier: Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .clickable { onClick() }
            .background(if (selected) accent.copy(alpha = 0.2f) else Color.Transparent)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
            color = if (selected) accent else colorResource(id = R.color.secondary)
        )
    }
}

@Composable
private fun LibraryCard(
    library: LibraryInfo,
    showSubscribedBadge: Boolean,
    isSubscribed: Boolean
) {
    val context = LocalContext.current
    val surface = colorResource(id = R.color.surface_light)
    val border = colorResource(id = R.color.border)
    val accent = colorResource(id = R.color.accent)
    val logoUrl = library.logo

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(surface)
            .border(1.dp, border, RoundedCornerShape(14.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(border),
                contentAlignment = Alignment.Center
            ) {
                if (!logoUrl.isNullOrBlank()) {
                    val request = remember(logoUrl) {
                        ImageRequest.Builder(context)
                            .data(logoUrl)
                            .memoryCachePolicy(CachePolicy.ENABLED)
                            .diskCachePolicy(CachePolicy.ENABLED)
                            .build()
                    }
                    SubcomposeAsyncImage(
                        model = request,
                        contentDescription = library.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                        loading = {
                            CircularProgressIndicator(
                                color = accent,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        error = {
                            Icon(
                                imageVector = Icons.Outlined.AccountBalance,
                                contentDescription = null,
                                tint = colorResource(id = R.color.tertiary)
                            )
                        }
                    )
                } else {
                    Icon(
                        imageVector = Icons.Outlined.AccountBalance,
                        contentDescription = null,
                        tint = colorResource(id = R.color.tertiary)
                    )
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = library.name.ifBlank { "Library" },
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colorResource(id = R.color.primary),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = library.address?.ifBlank { "Address unavailable" } ?: "Address unavailable",
                    fontSize = 12.sp,
                    color = colorResource(id = R.color.secondary),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (showSubscribedBadge && isSubscribed) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(accent.copy(alpha = 0.14f))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Subscribed",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = accent,
                        modifier = Modifier.widthIn(min = 72.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ErrorCard(
    message: String,
    onRetry: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(colorResource(id = R.color.surface_light))
            .border(
                width = 1.dp,
                color = colorResource(id = R.color.border),
                shape = RoundedCornerShape(14.dp)
            )
            .padding(14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = message,
            modifier = Modifier.weight(1f),
            fontSize = 13.sp,
            color = colorResource(id = R.color.secondary)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Box(
            modifier = Modifier
                .clip(CircleShape)
                .clickable { onRetry() }
                .background(colorResource(id = R.color.background))
                .padding(8.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.Refresh,
                contentDescription = "Retry",
                tint = colorResource(id = R.color.primary),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun EmptyCard(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(colorResource(id = R.color.surface_light))
            .border(
                width = 1.dp,
                color = colorResource(id = R.color.border),
                shape = RoundedCornerShape(14.dp)
            )
            .padding(14.dp)
    ) {
        Text(
            text = message,
            fontSize = 13.sp,
            color = colorResource(id = R.color.secondary)
        )
    }
}
