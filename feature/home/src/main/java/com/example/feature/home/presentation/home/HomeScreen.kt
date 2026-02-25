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
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Subscriptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.SubcomposeAsyncImage
import coil3.imageLoader
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import com.example.core.R
import com.example.feature.home.domain.model.BorrowedBook
import com.example.feature.home.domain.model.Borrowing
import com.example.feature.home.domain.model.HslColor
import com.example.feature.home.domain.model.Membership
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

@Composable
@Suppress("UnusedParameter")
fun HomeScreen(
    uiState: HomeUiState,
    onRefresh: (String?) -> Unit = {},
    onBorrowingClick: (String) -> Unit = {},
    onSignOutClick: () -> Unit = {}
) {
    val scrollState = rememberScrollState()
    var selectedNavIndex by remember { mutableIntStateOf(0) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(id = R.color.background))
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
            BorrowingsSection(
                uiState = uiState,
                onRetry = { onRefresh(null) },
                onBorrowingClick = onBorrowingClick
            )

            Spacer(modifier = Modifier.height(24.dp))
            MembershipsSection(uiState = uiState)
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
private fun BorrowingsSection(
    uiState: HomeUiState,
    onRetry: () -> Unit,
    onBorrowingClick: (String) -> Unit
) {
    if (uiState is HomeUiState.Idle) return

    val borrowings = (uiState as? HomeUiState.Content)?.borrowings.orEmpty()
    HeaderWithCount(
        title = "Active Borrowings",
        count = borrowings.size
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
            if (borrowings.isEmpty()) {
                EmptyCard(message = "No borrowed books yet")
            } else {
                BorrowingsRow(
                    borrowings = borrowings,
                    onBorrowingClick = onBorrowingClick
                )
            }
        }
    }
}

@Composable
private fun MembershipsSection(uiState: HomeUiState) {
    if (uiState is HomeUiState.Idle) return

    val memberships = (uiState as? HomeUiState.Content)?.memberships.orEmpty()

    SectionHeader(title = "Subscribed Memberships")
    Spacer(modifier = Modifier.height(12.dp))

    when {
        uiState is HomeUiState.Loading -> {
            EmptyCard(message = "Loading memberships...")
        }
        memberships.isEmpty() -> {
            EmptyCard(message = "You do not have active memberships")
        }
        else -> {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                memberships.forEach { membership ->
                    MembershipCard(membership = membership)
                }
            }
        }
    }
}

@Composable
private fun HeaderWithCount(
    title: String,
    count: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            color = colorResource(id = R.color.primary)
        )
        Box(
            modifier = Modifier
                .clip(CircleShape)
                .background(colorResource(id = R.color.surface_light))
                .padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
            Text(
                text = count.toString(),
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp,
                color = colorResource(id = R.color.secondary)
            )
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
    val primary = borrowing.book.primaryColor()
    val secondary = borrowing.book.secondaryColor(primary)
    val detailsBackground = if (primary.luminance() < 0.5f) {
        Color(0xFFE7EDF2)
    } else {
        Color(0xFFF1ECE2)
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
                .height(190.dp)
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(primary, secondary)
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
                                    colors = listOf(primary.copy(alpha = 0.9f), secondary.copy(alpha = 0.85f))
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
                status = borrowing.statusLabel(),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(10.dp)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            Text(
                text = borrowing.book.title,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = colorResource(id = R.color.primary)
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = borrowing.book.author,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontSize = 13.sp,
                color = colorResource(id = R.color.secondary)
            )
            Spacer(modifier = Modifier.height(8.dp))
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
                    fontSize = 12.sp,
                    color = colorResource(id = R.color.secondary)
                )
            }
        }
    }
}

@Composable
private fun StatusBadge(
    status: String?,
    modifier: Modifier = Modifier
) {
    val normalizedStatus = status.orEmpty().trim().lowercase(Locale.US)
    if (normalizedStatus.isEmpty()) {
        return
    }

    val badgeColor = when (normalizedStatus) {
        "overdue" -> Color(0xFFE53935)
        "returned" -> Color(0xFF4CAF50)
        else -> Color(0xFFF57C00)
    }
    val statusLabel = normalizedStatus
        .replace('_', ' ')
        .replaceFirstChar { char ->
            if (char.isLowerCase()) char.titlecase(Locale.US) else char.toString()
        }

    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(badgeColor)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = statusLabel,
            color = Color.White,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun MembershipCard(membership: Membership) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(colorResource(id = R.color.surface_light))
            .border(
                width = 1.dp,
                color = colorResource(id = R.color.border),
                shape = RoundedCornerShape(14.dp)
            )
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = membership.name,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = colorResource(id = R.color.primary)
            )
            Text(
                text = membership.tier.ifBlank { "Standard" },
                fontSize = 11.sp,
                color = colorResource(id = R.color.secondary)
            )
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

@Composable
private fun GreetingHeader() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text = "Welcome back,",
                fontSize = 14.sp,
                color = colorResource(id = R.color.secondary)
            )
            Text(
                text = "Reader",
                fontSize = 24.sp,
                fontWeight = FontWeight.SemiBold,
                color = colorResource(id = R.color.primary)
            )
        }

        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(colorResource(id = R.color.surface_light)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "R",
                fontWeight = FontWeight.Bold,
                color = colorResource(id = R.color.primary)
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
            .background(colorResource(id = R.color.surface_light))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Outlined.Search,
            contentDescription = "Search",
            tint = colorResource(id = R.color.secondary)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "Search titles, authors, ISBN...",
            color = colorResource(id = R.color.secondary),
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
            color = colorResource(id = R.color.primary)
        )
        if (action != null) {
            Text(
                text = action,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = colorResource(id = R.color.secondary)
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

    val glassColor = colorResource(id = R.color.surface_light).copy(alpha = 0.78f)
    val glassBorder = colorResource(id = R.color.border).copy(alpha = 0.7f)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp))
            .background(glassColor)
            .border(
                width = 1.dp,
                color = glassBorder,
                shape = RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp)
            )
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

private fun BorrowedBook.primaryColor(): Color {
    val tone = colors?.vibrant
        ?: colors?.muted
        ?: colors?.darkVibrant
        ?: colors?.lightVibrant
        ?: colors?.darkMuted
        ?: colors?.lightMuted

    return tone?.toComposeColor() ?: Color(0xFF8A84A6)
}

private fun BorrowedBook.secondaryColor(primary: Color): Color {
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

    val utcParser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }
    val dateOnlyParser = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    val output = SimpleDateFormat("MMM d", Locale.US)

    val parsed = runCatching { utcParser.parse(dueDate) }.getOrNull()
        ?: runCatching { dateOnlyParser.parse(dueDate) }.getOrNull()

    return if (parsed != null) {
        "Due ${output.format(parsed)}"
    } else {
        "Due ${dueDate.take(10)}"
    }
}

private fun Borrowing.statusLabel(): String {
    return when {
        returning != null -> "Returned"
        isOverdue() -> "Overdue"
        else -> "Borrowed"
    }
}

private fun Borrowing.isOverdue(): Boolean {
    val dueDate = parseDate(dueAt) ?: return false
    return dueDate.before(java.util.Date())
}

private fun parseDate(value: String?): java.util.Date? {
    if (value.isNullOrBlank()) return null

    val utcParser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }
    val dateOnlyParser = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    return runCatching { utcParser.parse(value) }.getOrNull()
        ?: runCatching { dateOnlyParser.parse(value) }.getOrNull()
}

private data class BottomNavItem(
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun HomeScreenPreview() {
    HomeScreen(
        uiState = HomeUiState.Content(
            borrowings = listOf(
                Borrowing(
                    id = "1",
                    book = BorrowedBook(
                        id = "book-1",
                        title = "The Art of Solitude",
                        author = "Stephen Batchelor",
                        year = 2024,
                        code = "BK-101",
                        coverUrl = null,
                        colors = null
                    ),
                    bookId = "book-1",
                    borrowedAt = "2026-02-10T12:00:00Z",
                    createdAt = "2026-02-10T12:00:00Z",
                    dueAt = "2026-03-01T12:00:00Z",
                    updatedAt = "2026-02-10T12:00:00Z",
                    staffId = "staff-1",
                    subscriptionId = "sub-1",
                    returning = null
                ),
                Borrowing(
                    id = "2",
                    book = BorrowedBook(
                        id = "book-2",
                        title = "Sapiens",
                        author = "Yuval Noah Harari",
                        year = 2011,
                        code = "BK-102",
                        coverUrl = null,
                        colors = null
                    ),
                    bookId = "book-2",
                    borrowedAt = "2026-02-10T12:00:00Z",
                    createdAt = "2026-02-10T12:00:00Z",
                    dueAt = "2026-02-15T12:00:00Z",
                    updatedAt = "2026-02-10T12:00:00Z",
                    staffId = "staff-1",
                    subscriptionId = "sub-1",
                    returning = null
                )
            ),
            memberships = listOf(
                Membership(
                    id = "m1",
                    name = "Contemporary Romance Library",
                    tier = "Premium",
                    status = "active",
                    expiresAt = "2026-06-01"
                )
            )
        )
    )
}
