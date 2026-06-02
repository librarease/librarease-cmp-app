package com.example.feature.books.presentation.books

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarHalf
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.ArrowForward
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
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
import coil3.request.crossfade
import com.example.core.R
import com.example.core.model.book.BookSummary
import com.example.core.model.book.HslColor
import androidx.compose.foundation.isSystemInDarkTheme
import java.util.Locale
import kotlin.math.floor
import androidx.compose.ui.draw.shadow

@Composable
fun BooksScreen(
    uiState: BooksUiState,
    onLoadMore: () -> Unit,
    onRetry: () -> Unit,
    onBookClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var query by rememberSaveable { mutableStateOf("") }
    val listState = rememberLazyListState()

    val shouldLoadMore by remember {
        derivedStateOf {
            val totalItems = listState.layoutInfo.totalItemsCount
            val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            totalItems > 0 && lastVisible >= totalItems - 2
        }
    }

    LaunchedEffect(shouldLoadMore, uiState.isAppending, uiState.canLoadMore) {
        if (shouldLoadMore && uiState.canLoadMore && !uiState.isAppending && !uiState.isLoading) {
            onLoadMore()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colorResource(id = R.color.background))
            .statusBarsPadding()
            .padding(horizontal = 20.dp)
            .padding(top = 16.dp, bottom = 24.dp)
    ) {
        SearchBar(
            value = query,
            onValueChange = { query = it },
            onClear = { query = "" }
        )

        Spacer(modifier = Modifier.height(20.dp))

        when {
            uiState.isLoading && uiState.books.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = colorResource(id = R.color.accent))
                }
            }
            uiState.errorMessage != null && uiState.books.isEmpty() -> {
                ErrorState(message = uiState.errorMessage, onRetry = onRetry)
            }
            else -> {
                LazyColumn(
                    state = listState,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(uiState.books, key = { it.id }) { book ->
                        BookRow(
                            book = book,
                            onBookClick = onBookClick
                        )
                    }

                    if (uiState.isAppending) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = colorResource(id = R.color.accent))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchBar(
    value: String,
    onValueChange: (String) -> Unit,
    onClear: () -> Unit
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp)),
        placeholder = {
            Text(
                text = "Search your books...",
                color = colorResource(id = R.color.secondary),
                fontSize = 14.sp
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Outlined.Search,
                contentDescription = "Search",
                tint = colorResource(id = R.color.secondary)
            )
        },
        trailingIcon = {
            if (value.isNotBlank()) {
                IconButton(onClick = onClear) {
                    Icon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = "Clear search",
                        tint = colorResource(id = R.color.secondary)
                    )
                }
            }
        },
        singleLine = true,
        shape = RoundedCornerShape(20.dp),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = colorResource(id = R.color.surface_light),
            unfocusedContainerColor = colorResource(id = R.color.surface_light),
            disabledContainerColor = colorResource(id = R.color.surface_light),
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent
        )
    )
}

@Composable
private fun ErrorState(
    message: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(colorResource(id = R.color.surface_light))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = message,
            color = colorResource(id = R.color.primary),
            fontSize = 14.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Tap to retry",
            color = colorResource(id = R.color.accent),
            fontSize = 12.sp,
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(colorResource(id = R.color.surface))
                .padding(horizontal = 12.dp, vertical = 6.dp)
                .clickable(onClick = onRetry)
        )
    }
}

@Composable
private fun BookRow(
    book: BookSummary,
    onBookClick: (String) -> Unit
) {
    val cardGradient = Brush.horizontalGradient(
        colors = listOf(
            colorResource(id = R.color.surface_light),
            colorResource(id = R.color.background)
        )
    )
    val coverBackdrop = book.coverBackdropColor()
    val ratingValue = book.rating?.coerceIn(0.0, 5.0)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(cardGradient)
            .clickable { onBookClick(book.id) }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(96.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(coverBackdrop),
            contentAlignment = Alignment.Center
        ) {
            BookCover(
                coverUrl = book.coverUrl,
                width = 64.dp,
                height = 92.dp,
                cornerRadius = 12.dp,
                addShadow = true
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = book.title.ifBlank { "Untitled" },
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = colorResource(id = R.color.primary),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = book.author.ifBlank { "Unknown author" },
                fontSize = 14.sp,
                color = colorResource(id = R.color.secondary),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(8.dp))
            RatingRow(rating = ratingValue)
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "VIEW DETAILS",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF3B82F6)
                )
                Icon(
                    imageVector = Icons.Outlined.ArrowForward,
                    contentDescription = null,
                    tint = Color(0xFF3B82F6),
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}

@Composable
fun RatingScreen(modifier: Modifier = Modifier) {
    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn() {
            this@Box
        }
    }
}

@Composable
private fun BookCover(
    coverUrl: String?,
    width: androidx.compose.ui.unit.Dp = 80.dp,
    height: androidx.compose.ui.unit.Dp = 110.dp,
    cornerRadius: androidx.compose.ui.unit.Dp = 14.dp,
    addShadow: Boolean = false
) {
    val context = LocalContext.current
    val isDark = isSystemInDarkTheme()
    val placeholderColor = if (isDark) {
        colorResource(id = R.color.surface_light)
    } else {
        Color(0xFFE6E2F0)
    }
    val request = remember(coverUrl) {
        ImageRequest.Builder(context)
            .data(coverUrl)
            .crossfade(true)
            .diskCachePolicy(CachePolicy.ENABLED)
            .memoryCachePolicy(CachePolicy.ENABLED)
            .build()
    }

    val coverModifier = if (addShadow) {
        Modifier
            .shadow(6.dp, RoundedCornerShape(cornerRadius))
            .clip(RoundedCornerShape(cornerRadius))
    } else {
        Modifier.clip(RoundedCornerShape(cornerRadius))
    }

    SubcomposeAsyncImage(
        model = request,
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = Modifier
            .size(width = width, height = height)
            .then(coverModifier),
        loading = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(placeholderColor)
            )
        },
        error = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(placeholderColor.copy(alpha = 0.9f))
            )
        }
    )
}

@Composable
private fun RatingRow(rating: Double?) {
    val normalized = (rating ?: 0.0).coerceIn(0.0, 5.0)
    val fullStars = floor(normalized).toInt()
    val hasHalf = normalized - fullStars >= 0.5
    val emptyStars = 5 - fullStars - if (hasHalf) 1 else 0

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        repeat(fullStars) {
            Icon(
                imageVector = Icons.Filled.Star,
                contentDescription = null,
                tint = Color(0xFFFBBF24),
                modifier = Modifier.size(16.dp)
            )
        }
        if (hasHalf) {
            Icon(
                imageVector = Icons.Filled.StarHalf,
                contentDescription = null,
                tint = Color(0xFFFBBF24),
                modifier = Modifier.size(16.dp)
            )
        }
        repeat(emptyStars) {
            Icon(
                imageVector = Icons.Outlined.StarOutline,
                contentDescription = null,
                tint = Color(0xFF94A3B8),
                modifier = Modifier.size(16.dp)
            )
        }
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = if (rating != null) String.format(Locale.US, "(%.1f)", normalized) else "(--)",
            fontSize = 12.sp,
            color = colorResource(id = R.color.secondary)
        )
    }
}

private fun BookSummary.coverBackdropColor(): Color {
    val tone = colors?.muted
        ?: colors?.lightMuted
        ?: colors?.vibrant
        ?: colors?.lightVibrant
        ?: colors?.darkMuted
        ?: colors?.darkVibrant

    return tone?.toComposeColor(
        saturationMultiplier = 0.7f,
        lightnessOffset = 0.08f
    ) ?: Color(0xFF5CA39F)
}

private fun HslColor.toComposeColor(
    saturationMultiplier: Float = 1f,
    lightnessOffset: Float = 0f
): Color {
    val adjustedSaturation = (s * saturationMultiplier).coerceIn(0f, 1f)
    val adjustedLightness = (l + lightnessOffset).coerceIn(0f, 1f)
    return Color.hsl(
        hue = (h.coerceIn(0f, 1f) * 360f),
        saturation = adjustedSaturation,
        lightness = adjustedLightness
    )
}
