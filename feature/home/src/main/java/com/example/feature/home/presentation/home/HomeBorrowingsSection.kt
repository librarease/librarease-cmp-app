package com.example.feature.home.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.SubcomposeAsyncImage
import coil3.imageLoader
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import com.example.core.R
import com.example.core.model.book.BookSummary
import com.example.core.model.book.HslColor
import com.example.feature.home.domain.model.Borrowing
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.math.ceil

@Composable
internal fun BorrowingsSection(
    uiState: HomeUiState,
    searchQuery: String,
    onRetry: () -> Unit,
    onBorrowingClick: (String) -> Unit
) {
    if (uiState is HomeUiState.Idle) return

    val borrowings = (uiState as? HomeUiState.Content)?.borrowings.orEmpty()
        .filter { it.returning == null }
    val trimmedQuery = searchQuery.trim()
    val filteredBorrowings = if (trimmedQuery.isBlank()) {
        borrowings
    } else {
        borrowings.filter { it.matchesQuery(trimmedQuery) }
    }
    HeaderWithCount(
        title = "Current Borrowings",
        count = filteredBorrowings.size
    )
    Spacer(modifier = Modifier.height(12.dp))

    when (uiState) {
        is HomeUiState.Idle -> Unit
        is HomeUiState.Loading -> BorrowingsLoadingRow()
        is HomeUiState.Error -> ErrorCard(
            message = uiState.message,
            onRetry = onRetry
        )
        is HomeUiState.Content -> {
            if (filteredBorrowings.isEmpty()) {
                val emptyMessage = if (trimmedQuery.isBlank()) {
                    "No borrowed books yet"
                } else {
                    "No results for \"$trimmedQuery\""
                }
                EmptyCard(message = emptyMessage)
            } else {
                BorrowingsRow(
                    borrowings = filteredBorrowings,
                    onBorrowingClick = onBorrowingClick
                )
            }
        }
    }
}

@Composable
private fun BorrowingsLoadingRow() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        repeat(2) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(280.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(colorResource(id = R.color.surface_light))
                    .border(
                        width = 1.dp,
                        color = colorResource(id = R.color.border),
                        shape = RoundedCornerShape(18.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Outlined.MenuBook,
                        contentDescription = null,
                        tint = colorResource(id = R.color.secondary)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Loading books...",
                        color = colorResource(id = R.color.secondary),
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun BorrowingsRow(
    borrowings: List<Borrowing>,
    onBorrowingClick: (String) -> Unit
) {
    val context = LocalContext.current
    LaunchedEffect(borrowings) {
        val coverUrls = borrowings
            .mapNotNull { borrowing ->
                borrowing.book.coverUrl?.takeIf { it.isNotBlank() }
            }
            .distinct()

        coverUrls.forEach { url ->
            val request = ImageRequest.Builder(context)
                .data(url)
                .memoryCachePolicy(CachePolicy.ENABLED)
                .diskCachePolicy(CachePolicy.ENABLED)
                .build()
            context.imageLoader.enqueue(request)
        }
    }

    Row(
        modifier = Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        borrowings.forEach { borrowing ->
            BorrowingCard(
                borrowing = borrowing,
                onBorrowingClick = onBorrowingClick
            )
        }
    }
}

@Composable
private fun BorrowingCard(
    borrowing: Borrowing,
    onBorrowingClick: (String) -> Unit
) {
    val context = LocalContext.current
    val isDark = isSystemInDarkTheme()
    val baseBackground = colorResource(id = R.color.background)
    val primary = borrowing.book.primaryColor()
    val secondary = borrowing.book.secondaryColor(primary)
    val detailsBackground = if (isDark) {
        colorResource(id = R.color.surface_light)
    } else if (primary.luminance() < 0.5f) {
        Color(0xFFE7EDF2)
    } else {
        Color(0xFFF1ECE2)
    }
    val gradientColors = if (isDark) {
        listOf(
            lerp(primary, baseBackground, 0.55f),
            lerp(secondary, baseBackground, 0.45f)
        )
    } else {
        listOf(primary, secondary)
    }
    val imageRequest = remember(borrowing.book.coverUrl) {
        ImageRequest.Builder(context)
            .data(borrowing.book.coverUrl)
            .memoryCachePolicy(CachePolicy.ENABLED)
            .diskCachePolicy(CachePolicy.ENABLED)
            .build()
    }

    Column(
        modifier = Modifier
            .width(176.dp)
            .clickable { onBorrowingClick(borrowing.id) }
            .clip(RoundedCornerShape(18.dp))
            .background(detailsBackground)
            .border(
                width = 1.dp,
                color = colorResource(id = R.color.border),
                shape = RoundedCornerShape(18.dp)
            )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .background(
                    brush = Brush.linearGradient(
                        colors = gradientColors
                    )
                )
        ) {
            SubcomposeAsyncImage(
                model = imageRequest,
                contentDescription = borrowing.book.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp)),
                loading = {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(primary.copy(alpha = 0.45f)),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = Color.White,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                },
                error = {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                brush = Brush.linearGradient(
                                    colors = gradientColors.map { it.copy(alpha = 0.9f) }
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = borrowing.book.title.take(1).ifBlank { "B" },
                            color = Color.White,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            )

            StatusBadge(
                status = borrowing.statusInfo(),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(10.dp)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Text(
                text = borrowing.book.title,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = colorResource(id = R.color.border)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = borrowing.book.author,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontSize = 12.sp,
                color = colorResource(id = R.color.secondary)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.DateRange,
                    contentDescription = "Due date",
                    tint = colorResource(id = R.color.secondary),
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = formatDueLabel(borrowing.dueAt),
                    fontSize = 11.sp,
                    color = colorResource(id = R.color.secondary)
                )
            }
        }
    }
}

@Composable
private fun StatusBadge(
    status: StatusBadgeUi?,
    modifier: Modifier = Modifier
) {
    if (status == null || status.label.isBlank()) {
        return
    }

    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(status.color)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = status.label,
            color = Color.White,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

private data class StatusBadgeUi(
    val label: String,
    val color: Color
)

private fun BookSummary.primaryColor(): Color {
    val tone = colors?.vibrant
        ?: colors?.muted
        ?: colors?.darkVibrant
        ?: colors?.lightVibrant
        ?: colors?.darkMuted
        ?: colors?.lightMuted

    return tone?.toComposeColor() ?: Color(0xFF8A84A6)
}

private fun BookSummary.secondaryColor(primary: Color): Color {
    val tone = colors?.darkMuted ?: colors?.muted ?: colors?.darkVibrant
    return tone?.toComposeColor()?.copy(alpha = 0.85f) ?: primary.copy(alpha = 0.75f)
}

private fun HslColor.toComposeColor(): Color {
    return Color.hsl(
        hue = (h.coerceIn(0f, 1f) * 360f),
        saturation = s.coerceIn(0f, 1f),
        lightness = l.coerceIn(0f, 1f)
    )
}

private fun formatDueLabel(dueDate: String?): String {
    if (dueDate.isNullOrBlank()) {
        return "Due date unavailable"
    }

    val parsed = parseDate(dueDate)
    val output = SimpleDateFormat("MMM d", Locale.US)

    return if (parsed != null) {
        "Due ${output.format(parsed)}"
    } else {
        "Due ${dueDate.take(10)}"
    }
}

private fun Borrowing.statusInfo(): StatusBadgeUi? {
    if (returning != null) {
        return StatusBadgeUi(label = "Returned", color = Color(0xFF4CAF50))
    }

    val dueDate = parseDate(dueAt)
    if (dueDate == null) {
        return StatusBadgeUi(label = "Borrowed", color = Color(0xFF2E7D32))
    }

    val now = java.util.Date()
    val diffMillis = dueDate.time - now.time
    if (diffMillis < 0L) {
        return StatusBadgeUi(label = "Overdue", color = Color(0xFFE53935))
    }

    val daysLeft = ceil(diffMillis.toDouble() / (1000L * 60L * 60L * 24L)).toInt()
    return when {
        daysLeft <= 0 -> StatusBadgeUi(label = "Due today", color = Color(0xFFF57C00))
        daysLeft == 1 -> StatusBadgeUi(label = "Due tomorrow", color = Color(0xFFF57C00))
        else -> StatusBadgeUi(label = "$daysLeft days left", color = Color(0xFF2E7D32))
    }
}

private fun Borrowing.matchesQuery(query: String): Boolean {
    if (query.isBlank()) return true
    val needle = query.lowercase(Locale.US)
    val title = book.title.lowercase(Locale.US)
    val author = book.author.lowercase(Locale.US)
    val code = book.code?.lowercase(Locale.US).orEmpty()
    return title.contains(needle) || author.contains(needle) || code.contains(needle)
}
