package com.example.feature.home.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
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
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import com.example.core.R
import com.example.core.model.book.BookSummary
import com.example.core.model.book.HslColor
import com.example.feature.home.domain.model.Borrowing
import com.example.feature.home.domain.model.Subscription
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.ceil
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

@Composable
internal fun OverdueBorrowingsSection(
    overdueBorrowings: List<Borrowing>,
    subscriptions: List<Subscription>,
    onBorrowingClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (overdueBorrowings.isEmpty()) return

    val libraryNames = remember(subscriptions) {
        subscriptions.associate { subscription ->
            subscription.id to (subscription.libraryName?.ifBlank { "Library" } ?: "Library")
        }
    }

    OverdueBorrowingsPager(
        overdueBorrowings = overdueBorrowings,
        libraryNames = libraryNames,
        onBorrowingClick = onBorrowingClick,
        modifier = modifier
    )
}

@Composable
@OptIn(ExperimentalFoundationApi::class)
private fun OverdueBorrowingsPager(
    overdueBorrowings: List<Borrowing>,
    libraryNames: Map<String, String>,
    onBorrowingClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val pagerState = rememberPagerState(pageCount = { overdueBorrowings.size })
    val currentPage by remember { derivedStateOf { pagerState.currentPage } }

    LaunchedEffect(overdueBorrowings.size) {
        if (overdueBorrowings.size <= 1) return@LaunchedEffect
        while (isActive) {
            delay(4500)
            if (pagerState.isScrollInProgress) continue
            val nextPage = (pagerState.currentPage + 1) % overdueBorrowings.size
            pagerState.animateScrollToPage(nextPage)
        }
    }

    HorizontalPager(
        state = pagerState,
        contentPadding = PaddingValues(horizontal = 2.dp),
        pageSpacing = 12.dp,
        modifier = modifier.fillMaxWidth()
    ) { page ->
        val borrowing = overdueBorrowings[page]
        val libraryName = libraryNames[borrowing.subscriptionId] ?: "Library"
        OverdueBorrowingCard(
            borrowing = borrowing,
            libraryName = libraryName,
            onBorrowingClick = onBorrowingClick
        )
    }

    Spacer(modifier = Modifier.height(12.dp))
    OverduePagerDots(
        totalDots = overdueBorrowings.size,
        selectedIndex = currentPage,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun OverduePagerDots(
    totalDots: Int,
    selectedIndex: Int,
    modifier: Modifier = Modifier
) {
    if (totalDots <= 1) return

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(totalDots) { index ->
            val isSelected = index == selectedIndex
            val size = if (isSelected) 8.dp else 6.dp
            val color = if (isSelected) {
                colorResource(id = R.color.primary)
            } else {
                colorResource(id = R.color.secondary)
            }
            Box(
                modifier = Modifier
                    .size(size)
                    .clip(RoundedCornerShape(999.dp))
                    .background(color)
            )
            if (index != totalDots - 1) {
                Spacer(modifier = Modifier.width(6.dp))
            }
        }
    }
}

@Composable
private fun OverdueBorrowingCard(
    borrowing: Borrowing,
    libraryName: String,
    onBorrowingClick: (String) -> Unit
) {
    val cardShape = RoundedCornerShape(20.dp)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(cardShape)
            .background(colorResource(id = R.color.surface_light))
            .border(
                width = 1.dp,
                color = colorResource(id = R.color.border),
                shape = cardShape
            )
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ErrorOutline,
                        contentDescription = null,
                        tint = Color(0xFFF87171),
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "ACTION REQUIRED",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFF87171)
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(Color(0xFF7F1D1D))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Overdue",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFFCA5A5)
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OverdueBookCover(borrowing = borrowing)
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = borrowing.book.title,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colorResource(id = R.color.primary)
                    )
                    Text(
                        text = libraryName,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontSize = 12.sp,
                        color = colorResource(id = R.color.secondary)
                    )
                    Text(
                        text = formatOverdueLabel(borrowing.dueAt),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontSize = 12.sp,
                        color = Color(0xFFF87171)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(colorResource(id = R.color.accent))
                            .clickable { onBorrowingClick(borrowing.id) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Details",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = colorResource(id = R.color.primary)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun OverdueBookCover(borrowing: Borrowing) {
    val context = LocalContext.current
    val primary = borrowing.book.primaryColor()
    val secondary = borrowing.book.secondaryColor(primary)
    val baseBackground = colorResource(id = R.color.background)
    val gradientColors = if (primary.luminance() < 0.5f) {
        listOf(lerp(primary, baseBackground, 0.4f), lerp(secondary, baseBackground, 0.25f))
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

    Box(
        modifier = Modifier
            .width(64.dp)
            .height(92.dp)
            .clip(RoundedCornerShape(12.dp))
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
            modifier = Modifier.matchParentSize(),
            loading = {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(primary.copy(alpha = 0.45f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = Color.White,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(22.dp)
                    )
                }
            },
            error = {
                Box(
                    modifier = Modifier
                        .matchParentSize()
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
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        )
    }
}

internal fun Borrowing.isOverdueBorrowing(): Boolean {
    if (returning != null) return false
    val dueDate = parseDate(dueAt) ?: return false
    return dueDate.before(Date())
}

private fun formatOverdueLabel(dueAt: String?): String {
    val dueDate = parseDate(dueAt) ?: return "Due date unavailable"
    val now = Date()
    val diffMillis = now.time - dueDate.time
    if (diffMillis < 0L) {
        val output = SimpleDateFormat("MMM d", Locale.US)
        return "Due ${output.format(dueDate)}"
    }
    val daysOver = ceil(diffMillis.toDouble() / (1000L * 60L * 60L * 24L)).toInt()
    return when (daysOver) {
        0 -> "Due today"
        1 -> "Due yesterday"
        else -> "Due $daysOver days ago"
    }
}

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
