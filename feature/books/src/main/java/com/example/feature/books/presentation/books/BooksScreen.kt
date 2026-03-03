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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Search
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
import androidx.compose.ui.graphics.lerp
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

@Composable
fun BooksScreen(
    uiState: BooksUiState,
    onLoadMore: () -> Unit,
    onRetry: () -> Unit,
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
            .padding(horizontal = 20.dp)
            .padding(top = 24.dp, bottom = 24.dp)
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
                        BookRow(book = book)
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
private fun BookRow(book: BookSummary) {
    val baseColor = book.cardBaseColor()
    val gradient = Brush.horizontalGradient(
        colors = listOf(baseColor.soften(0.82f), baseColor.soften(0.62f))
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(gradient)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        BookCover(coverUrl = book.coverUrl)
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
            Spacer(modifier = Modifier.height(12.dp))
            AvailabilityChip(isAvailable = book.available != false)
        }
    }
}

@Composable
private fun BookCover(coverUrl: String?) {
    val context = LocalContext.current
    val request = remember(coverUrl) {
        ImageRequest.Builder(context)
            .data(coverUrl)
            .crossfade(true)
            .diskCachePolicy(CachePolicy.ENABLED)
            .memoryCachePolicy(CachePolicy.ENABLED)
            .build()
    }

    SubcomposeAsyncImage(
        model = request,
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = Modifier
            .size(width = 80.dp, height = 110.dp)
            .clip(RoundedCornerShape(14.dp)),
        loading = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFE6E2F0))
            )
        },
        error = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFDCD6EA))
            )
        }
    )
}

@Composable
private fun AvailabilityChip(isAvailable: Boolean) {
    val background = if (isAvailable) Color(0xFFE5F4ED) else Color(0xFFFBE7E4)
    val textColor = if (isAvailable) Color(0xFF1B7F4B) else Color(0xFFB3261E)
    val label = if (isAvailable) "Available" else "Unavailable"

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(background)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = label,
            color = textColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

private fun BookSummary.cardBaseColor(): Color {
    val tone = colors?.lightMuted
        ?: colors?.muted
        ?: colors?.lightVibrant
        ?: colors?.vibrant
        ?: colors?.darkMuted
        ?: colors?.darkVibrant

    return tone?.toComposeColor(
        saturationMultiplier = 0.65f,
        lightnessOffset = 0.18f
    ) ?: Color(0xFFE6E1F0)
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

private fun Color.soften(amount: Float): Color {
    return lerp(this, Color.White, amount.coerceIn(0f, 1f))
}
