package com.example.feature.home.presentation.borrowingdetail

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.AutoStories
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
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
import com.example.core.R
import com.example.core.model.book.BookDetail
import com.example.core.model.book.BookPalette
import com.example.core.model.book.HslColor
import com.example.core.model.book.LibraryInfo
import com.example.feature.home.domain.model.Borrowing
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

@Composable
fun BorrowingDetailScreen(
    uiState: BorrowingDetailUiState,
    onBackClick: () -> Unit,
    onRetryClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(id = R.color.background))
    ) {
        when (uiState) {
            is BorrowingDetailUiState.Loading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = colorResource(id = R.color.accent)
                )
            }
            is BorrowingDetailUiState.Error -> {
                ErrorState(
                    message = uiState.message,
                    onRetryClick = onRetryClick,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            is BorrowingDetailUiState.Content -> {
                BorrowingDetailContent(
                    borrowing = uiState.borrowing,
                    bookDetail = uiState.bookDetail,
                    isBookLoading = uiState.isBookLoading,
                    onBackClick = onBackClick
                )
            }
        }
    }
}

@Composable
private fun BorrowingDetailContent(
    borrowing: Borrowing,
    bookDetail: BookDetail?,
    isBookLoading: Boolean,
    onBackClick: () -> Unit
) {
    val title = bookDetail?.title?.ifBlank { borrowing.book.title } ?: borrowing.book.title
    val author = bookDetail?.author?.ifBlank { borrowing.book.author } ?: borrowing.book.author
    val year = bookDetail?.year ?: borrowing.book.year
    val coverUrl = bookDetail?.coverUrl ?: borrowing.book.coverUrl
    val description = bookDetail?.description.orEmpty()
    val library = bookDetail?.library
    val stats = bookDetail?.stats
    val palette = bookDetail?.colors ?: borrowing.book.colors

    val background = colorResource(id = R.color.background)
    val primaryRaw = palette.primaryColor()
    val secondaryRaw = palette.secondaryColor(primaryRaw)
    val primary = primaryRaw.soften(background, 0.35f)
    val secondary = secondaryRaw.soften(background, 0.45f)
    val overlayTextColor = if (primary.luminance() < 0.5f) Color(0xFFEFF4F8) else Color(0xFF2D2D2D)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        HeaderSection(
            title = title,
            author = author,
            year = year,
            coverUrl = coverUrl,
            overlayTextColor = overlayTextColor,
            gradient = Brush.verticalGradient(listOf(primary, secondary, colorResource(id = R.color.background))),
            onBackClick = onBackClick
        )

        Spacer(modifier = Modifier.height(18.dp))

        BorrowingMetaCard(
            borrowedAt = borrowing.borrowedAt,
            dueAt = borrowing.dueAt,
            isOverdue = borrowing.isOverdue()
        )

        Spacer(modifier = Modifier.height(14.dp))

        BorrowedFromCard(library = library)

        Spacer(modifier = Modifier.height(18.dp))

        AboutSection(description = description, isLoading = isBookLoading)

        Spacer(modifier = Modifier.height(18.dp))

        StatsRow(
            borrowCount = stats?.borrowCount,
            rating = stats?.rating,
            reviewCount = stats?.reviewCount,
            isLoading = isBookLoading
        )

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
private fun HeaderSection(
    title: String,
    author: String,
    year: Int?,
    coverUrl: String?,
    overlayTextColor: Color,
    gradient: Brush,
    onBackClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(360.dp)
            .background(gradient)
    ) {
        Box(
            modifier = Modifier
                .statusBarsPadding()
                .padding(start = 20.dp, top = 12.dp)
                .clip(CircleShape)
                .background(colorResource(id = R.color.surface_light).copy(alpha = 0.6f))
                .clickable { onBackClick() }
                .padding(10.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                contentDescription = "Back",
                tint = colorResource(id = R.color.primary)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 32.dp, vertical = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            val context = LocalContext.current
            val imageRequest = remember(coverUrl) {
                ImageRequest.Builder(context)
                    .data(coverUrl)
                    .memoryCachePolicy(CachePolicy.ENABLED)
                    .diskCachePolicy(CachePolicy.ENABLED)
                    .build()
            }

            SubcomposeAsyncImage(
                model = imageRequest,
                contentDescription = title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .width(190.dp)
                    .height(240.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(colorResource(id = R.color.surface_light)),
                loading = {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(colorResource(id = R.color.surface_light)),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = colorResource(id = R.color.accent),
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            )

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = title,
                fontSize = 26.sp,
                fontWeight = FontWeight.SemiBold,
                color = overlayTextColor,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(6.dp))

            val authorLine = if (year != null) "$author · $year" else author
            Text(
                text = authorLine,
                fontSize = 14.sp,
                color = overlayTextColor.copy(alpha = 0.75f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun BorrowingMetaCard(
    borrowedAt: String?,
    dueAt: String?,
    isOverdue: Boolean
) {
    Box(
        modifier = Modifier
            .padding(horizontal = 20.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(Color.White)
            .border(
                width = 1.dp,
                color = colorResource(id = R.color.border),
                shape = RoundedCornerShape(18.dp)
            )
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = "Borrowing Details",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = colorResource(id = R.color.tertiary)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                MetaDateItem(
                    label = "Borrowed",
                    value = formatDateOnly(borrowedAt)
                )
                MetaDateItem(
                    label = "Due",
                    value = formatDateOnly(dueAt),
                    highlight = isOverdue
                )
            }
        }
    }
}

@Composable
private fun MetaDateItem(
    label: String,
    value: String,
    highlight: Boolean = false
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Icon(
                imageVector = Icons.Outlined.DateRange,
                contentDescription = null,
                tint = colorResource(id = R.color.tertiary),
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = label,
                fontSize = 12.sp,
                color = colorResource(id = R.color.tertiary)
            )
        }
        Text(
            text = value,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (highlight) Color(0xFFF57C00) else colorResource(id = R.color.primary)
        )
    }
}

@Composable
private fun BorrowedFromCard(library: LibraryInfo?) {
    Box(
        modifier = Modifier
            .padding(horizontal = 20.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(Color.White)
            .border(
                width = 1.dp,
                color = colorResource(id = R.color.border),
                shape = RoundedCornerShape(18.dp)
            )
            .padding(16.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(colorResource(id = R.color.surface_light)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Home,
                    contentDescription = null,
                    tint = colorResource(id = R.color.tertiary)
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Borrowed from",
                    fontSize = 12.sp,
                    color = colorResource(id = R.color.tertiary)
                )
                Text(
                    text = library?.name?.ifBlank { "Library" } ?: "Library",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colorResource(id = R.color.primary)
                )
            }
        }
    }
}

@Composable
private fun AboutSection(
    description: String,
    isLoading: Boolean
) {
    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
        Text(
            text = "About This Book",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = colorResource(id = R.color.tertiary)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = if (description.isNotBlank()) description else if (isLoading) "Loading description..." else "No description available.",
            fontSize = 14.sp,
            color = colorResource(id = R.color.primary)
        )
    }
}

@Composable
private fun StatsRow(
    borrowCount: Int?,
    rating: Double?,
    reviewCount: Int?,
    isLoading: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatsCard(
            label = "Borrows",
            value = if (borrowCount != null) borrowCount.toString() else placeholderOrDash(isLoading),
            icon = Icons.Outlined.AutoStories,
            modifier = Modifier.weight(1f)
        )
        StatsCard(
            label = "Rating",
            value = if (rating != null) String.format(Locale.US, "%.1f", rating) else placeholderOrDash(isLoading),
            icon = Icons.Outlined.Star,
            modifier = Modifier.weight(1f)
        )
        StatsCard(
            label = "Reviews",
            value = if (reviewCount != null) reviewCount.toString() else placeholderOrDash(isLoading),
            icon = Icons.Outlined.AutoStories,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun StatsCard(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(colorResource(id = R.color.surface_light))
            .border(
                width = 1.dp,
                color = colorResource(id = R.color.border),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(vertical = 16.dp, horizontal = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = colorResource(id = R.color.tertiary)
            )
            Text(
                text = value,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = colorResource(id = R.color.primary)
            )
            Text(
                text = label,
                fontSize = 12.sp,
                color = colorResource(id = R.color.secondary)
            )
        }
    }
}

@Composable
private fun ErrorState(
    message: String,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(colorResource(id = R.color.surface_light))
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = message,
            color = colorResource(id = R.color.secondary),
            fontSize = 13.sp
        )
        Box(
            modifier = Modifier
                .clip(CircleShape)
                .clickable { onRetryClick() }
                .background(colorResource(id = R.color.background))
                .padding(8.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.Refresh,
                contentDescription = "Retry",
                tint = colorResource(id = R.color.primary)
            )
        }
    }
}

private fun Borrowing.isOverdue(): Boolean {
    val dueDateTime = parseIso(dueAt) ?: return false
    return dueDateTime.before(java.util.Date())
}

private fun parseIso(value: String?): java.util.Date? {
    if (value.isNullOrBlank()) return null

    val utcParser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }
    val dateOnlyParser = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    return runCatching { utcParser.parse(value) }.getOrNull()
        ?: runCatching { dateOnlyParser.parse(value) }.getOrNull()
}

private fun formatDateOnly(value: String?): String {
    val date = parseIso(value) ?: return value.orEmpty()
    val output = SimpleDateFormat("MMM d, yyyy", Locale.US)
    return output.format(date)
}

private fun placeholderOrDash(isLoading: Boolean): String = if (isLoading) "..." else "-"

private fun BookPalette?.primaryColor(): Color {
    val tone = this?.darkVibrant
        ?: this?.darkMuted
        ?: this?.vibrant
        ?: this?.lightVibrant
        ?: this?.muted
        ?: this?.lightMuted

    return tone?.toComposeColor() ?: Color(0xFF5E6A7A)
}

private fun BookPalette?.secondaryColor(primary: Color): Color {
    val tone = this?.darkMuted ?: this?.muted ?: this?.darkVibrant
    return tone?.toComposeColor()?.copy(alpha = 0.85f) ?: primary.copy(alpha = 0.8f)
}

private fun HslColor.toComposeColor(): Color {
    return Color.hsl(
        hue = (h.coerceIn(0f, 1f) * 360f),
        saturation = s.coerceIn(0f, 1f),
        lightness = l.coerceIn(0f, 1f)
    )
}

private fun Color.soften(background: Color, amount: Float): Color {
    return lerp(this, background, amount.coerceIn(0f, 1f))
}
