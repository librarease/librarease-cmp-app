package com.example.feature.books.presentation.bookdetail

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
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
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
import com.example.core.model.book.BookDetail
import com.example.core.model.book.BookPalette
import com.example.core.model.book.HslColor
import com.example.core.presentation.review.ReviewItem

@Composable
fun BookDetailScreen(
    uiState: BookDetailUiState,
    reviewsPreviewState: BookReviewsPreviewUiState,
    onBackClick: () -> Unit,
    onRetryClick: () -> Unit,
    onAddToWatchlistClick: () -> Unit,
    onViewAllReviewsClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(id = R.color.background))
    ) {
        when (uiState) {
            is BookDetailUiState.Loading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = Color(0xFF3B82F6)
                )
            }
            is BookDetailUiState.Error -> {
                ErrorState(
                    message = uiState.message,
                    onRetryClick = onRetryClick,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            is BookDetailUiState.Content -> {
                BookDetailContent(
                    book = uiState.book,
                    reviewsPreviewState = reviewsPreviewState,
                    onBackClick = onBackClick,
                    onAddToWatchlistClick = onAddToWatchlistClick,
                    onViewAllReviewsClick = onViewAllReviewsClick
                )
            }
        }
    }
}

@Composable
private fun BookDetailContent(
    book: BookDetail,
    reviewsPreviewState: BookReviewsPreviewUiState,
    onBackClick: () -> Unit,
    onAddToWatchlistClick: () -> Unit,
    onViewAllReviewsClick: () -> Unit
) {
    val title = book.title.ifBlank { "Untitled" }
    val authorName = book.author.ifBlank { "Unknown author" }
    val authorLine = buildString {
        append(authorName)
        book.year?.let { append(" • $it") }
    }
    val description = book.description.orEmpty()
    val libraryName = book.library?.name?.takeIf { it.isNotBlank() }
    val palette = book.colors
    val coverBackdrop = palette.coverBackdropColor()
    val background = colorResource(id = R.color.background)
    val primaryRaw = palette.primaryColor()
    val secondaryRaw = palette.secondaryColor(primaryRaw)
    val primary = primaryRaw.soften(background, 0.35f)
    val secondary = secondaryRaw.soften(background, 0.45f)
    val accentBlue = Color(0xFF3B82F6)
    val effectiveAvailability = book.stats?.borrowing == null

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        HeaderSection(
            title = title,
            authorLine = authorLine,
            coverUrl = book.coverUrl,
            libraryName = libraryName,
            coverBackdrop = coverBackdrop,
            gradient = Brush.verticalGradient(listOf(primary, secondary, background)),
            accent = accentBlue,
            onBackClick = onBackClick
        )

        Spacer(modifier = Modifier.height(18.dp))

        AvailabilityToggle(
            available = effectiveAvailability,
            accent = accentBlue
        )

        Spacer(modifier = Modifier.height(14.dp))

        WatchlistButton(
            onClick = onAddToWatchlistClick,
            accent = accentBlue
        )

        Spacer(modifier = Modifier.height(20.dp))

        AboutSection(
            description = description,
            accent = accentBlue
        )

        Spacer(modifier = Modifier.height(20.dp))

        ReviewsPreviewSection(
            state = reviewsPreviewState,
            accent = accentBlue,
            onViewAllReviewsClick = onViewAllReviewsClick
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun HeaderSection(
    title: String,
    authorLine: String,
    coverUrl: String?,
    libraryName: String?,
    coverBackdrop: Color,
    gradient: Brush,
    accent: Color,
    onBackClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(420.dp)
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
                .padding(horizontal = 32.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(210.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .background(coverBackdrop)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                BookCover(
                    coverUrl = coverUrl,
                    width = 150.dp,
                    height = 210.dp,
                    cornerRadius = 16.dp
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = title,
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = colorResource(id = R.color.primary),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = authorLine,
                fontSize = 14.sp,
                color = colorResource(id = R.color.secondary),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            if (!libraryName.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                LibraryPill(
                    text = libraryName,
                    accent = accent
                )
            }
        }
    }
}

@Composable
private fun LibraryPill(
    text: String,
    accent: Color
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(18.dp))
            .background(accent.copy(alpha = 0.16f))
            .border(1.dp, accent.copy(alpha = 0.45f), RoundedCornerShape(18.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(
            imageVector = Icons.Outlined.LocationOn,
            contentDescription = null,
            tint = accent,
            modifier = Modifier.size(14.dp)
        )
        Text(
            text = text.uppercase(),
            fontSize = 11.sp,
            color = accent,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun AvailabilityToggle(
    available: Boolean?,
    accent: Color
) {
    val border = colorResource(id = R.color.border)
    val surface = colorResource(id = R.color.surface_light)
    val isAvailable = available == true
    val isOnLoan = available == false

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(surface)
            .border(1.dp, border, RoundedCornerShape(20.dp))
            .height(44.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AvailabilityChip(
            label = "Available",
            selected = isAvailable,
            accent = accent,
            modifier = Modifier.weight(1f)
        )
        Box(
            modifier = Modifier
                .width(1.dp)
                .fillMaxSize()
                .background(border.copy(alpha = 0.6f))
        )
        AvailabilityChip(
            label = "On Loan",
            selected = isOnLoan,
            accent = accent,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun AvailabilityChip(
    label: String,
    selected: Boolean,
    accent: Color,
    modifier: Modifier = Modifier
) {
    val textColor = if (selected) accent else colorResource(id = R.color.secondary)
    val bgColor = if (selected) accent.copy(alpha = 0.3f) else Color.Transparent
    val borderColor = if (selected) accent.copy(alpha = 0.65f) else Color.Transparent
    val weight = if (selected) FontWeight.SemiBold else FontWeight.Medium

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(18.dp)),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (selected) {
                Icon(
                    imageVector = Icons.Outlined.Check,
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(14.dp)
                )
            }
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = weight,
                color = textColor
            )
        }
    }
}

@Composable
private fun WatchlistButton(
    onClick: () -> Unit,
    accent: Color
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = accent,
            contentColor = Color.White
        ),
        shape = RoundedCornerShape(22.dp)
    ) {
        Icon(
            imageVector = Icons.Outlined.BookmarkBorder,
            contentDescription = null,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "Add to Watchlist",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun AboutSection(
    description: String,
    accent: Color
) {
    var expanded by remember { mutableStateOf(false) }
    val showReadMore = description.length > 180
    val displayText = if (description.isNotBlank()) description else "No description available."

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(colorResource(id = R.color.surface_light))
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "About the book",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = colorResource(id = R.color.tertiary)
            )
            Spacer(modifier = Modifier.weight(1f))
            Icon(
                imageVector = Icons.Outlined.Info,
                contentDescription = null,
                tint = accent,
                modifier = Modifier.size(16.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = displayText,
            fontSize = 13.sp,
            color = colorResource(id = R.color.primary),
            maxLines = if (expanded) Int.MAX_VALUE else 4,
            overflow = TextOverflow.Ellipsis
        )
        if (showReadMore) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = if (expanded) "Read less" else "Read more",
                fontSize = 12.sp,
                color = accent,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.clickable { expanded = !expanded }
            )
        }
    }
}

@Composable
private fun ReviewsPreviewSection(
    state: BookReviewsPreviewUiState,
    accent: Color,
    onViewAllReviewsClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(colorResource(id = R.color.surface_light))
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Reviews",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = colorResource(id = R.color.tertiary)
            )
            Spacer(modifier = Modifier.weight(1f))
            if (state is BookReviewsPreviewUiState.Content) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(accent.copy(alpha = 0.16f))
                        .clickable { onViewAllReviewsClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
                        contentDescription = "View all reviews",
                        tint = accent,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        when (state) {
            is BookReviewsPreviewUiState.Loading -> {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    ReviewPlaceholder()
                    ReviewPlaceholder()
                }
            }
            is BookReviewsPreviewUiState.Empty -> {
                ReviewsEmptyText("No reviews yet.")
            }
            is BookReviewsPreviewUiState.Error -> {
                ReviewsEmptyText(state.message)
            }
            is BookReviewsPreviewUiState.Content -> {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    state.reviews.take(2).forEach { review ->
                        ReviewItem(review = review)
                    }
                }
            }
        }
    }
}

@Composable
private fun ReviewPlaceholder() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(colorResource(id = R.color.background).copy(alpha = 0.42f))
            .border(1.dp, colorResource(id = R.color.border), RoundedCornerShape(14.dp))
            .padding(14.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(14.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(colorResource(id = R.color.border))
        )
        Spacer(modifier = Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth(0.7f)
                .height(14.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(colorResource(id = R.color.border))
        )
    }
}

@Composable
private fun ReviewsEmptyText(message: String) {
    Text(
        text = message,
        color = colorResource(id = R.color.secondary),
        fontSize = 13.sp,
        lineHeight = 18.sp
    )
}

@Composable
private fun ErrorState(
    message: String,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
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
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = "Tap to retry",
            color = Color(0xFF3B82F6),
            fontSize = 12.sp,
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(colorResource(id = R.color.surface))
                .padding(horizontal = 12.dp, vertical = 6.dp)
                .clickable(onClick = onRetryClick)
        )
    }
}

@Composable
private fun BookCover(
    coverUrl: String?,
    width: androidx.compose.ui.unit.Dp,
    height: androidx.compose.ui.unit.Dp,
    cornerRadius: androidx.compose.ui.unit.Dp
) {
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
            .size(width = width, height = height)
            .shadow(8.dp, RoundedCornerShape(cornerRadius))
            .clip(RoundedCornerShape(cornerRadius)),
        loading = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(colorResource(id = R.color.surface_light))
            )
        },
        error = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(colorResource(id = R.color.surface_light))
            )
        }
    )
}

private fun BookPalette?.coverBackdropColor(): Color {
    val tone = this?.muted
        ?: this?.lightMuted
        ?: this?.vibrant
        ?: this?.lightVibrant
        ?: this?.darkMuted
        ?: this?.darkVibrant

    return tone?.toComposeColor() ?: Color(0xFF1F2937)
}

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
