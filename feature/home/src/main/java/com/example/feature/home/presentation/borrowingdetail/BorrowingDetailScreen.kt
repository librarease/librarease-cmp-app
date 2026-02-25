package com.example.feature.home.presentation.borrowingdetail

import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.example.core.R
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
                    onBackClick = onBackClick
                )
            }
        }
    }
}

@Composable
private fun BorrowingDetailContent(
    borrowing: Borrowing,
    onBackClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable { onBackClick() }
                    .background(colorResource(id = R.color.surface_light))
                    .padding(8.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = "Back",
                    tint = colorResource(id = R.color.primary)
                )
            }
            Text(
                text = borrowing.statusLabel(),
                fontSize = 13.sp,
                color = Color.White,
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(borrowing.statusColor())
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        AsyncImage(
            model = borrowing.book.coverUrl,
            contentDescription = borrowing.book.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(colorResource(id = R.color.surface_light))
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = borrowing.book.title,
            fontSize = 24.sp,
            fontWeight = FontWeight.SemiBold,
            color = colorResource(id = R.color.primary),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = borrowing.book.author,
            fontSize = 15.sp,
            color = colorResource(id = R.color.secondary)
        )

        Spacer(modifier = Modifier.height(18.dp))

        DetailLine(label = "Borrowing ID", value = borrowing.id)
        DetailLine(label = "Book ID", value = borrowing.bookId)
        DetailLine(label = "Borrowed At", value = formatIso(borrowing.borrowedAt))
        DetailLine(label = "Due At", value = formatIso(borrowing.dueAt))
        DetailLine(label = "Created At", value = formatIso(borrowing.createdAt))
        DetailLine(label = "Updated At", value = formatIso(borrowing.updatedAt))
        DetailLine(label = "Subscription ID", value = borrowing.subscriptionId.orEmpty())

        if (borrowing.returning != null) {
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = "Returning",
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
                color = colorResource(id = R.color.primary)
            )
            Spacer(modifier = Modifier.height(8.dp))
            DetailLine(label = "Returned At", value = formatIso(borrowing.returning.returnedAt))
            DetailLine(label = "Fine", value = borrowing.returning.fine.toString())
        }
    }
}

@Composable
private fun DetailLine(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            color = colorResource(id = R.color.secondary),
            fontSize = 13.sp
        )
        Text(
            text = value.ifBlank { "-" },
            color = colorResource(id = R.color.primary),
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(start = 10.dp)
        )
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

private fun Borrowing.statusLabel(): String {
    return when {
        returning != null -> "Returned"
        isOverdue() -> "Overdue"
        else -> "Borrowed"
    }
}

private fun Borrowing.statusColor(): Color {
    return when {
        returning != null -> Color(0xFF4CAF50)
        isOverdue() -> Color(0xFFE53935)
        else -> Color(0xFFF57C00)
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

private fun formatIso(value: String?): String {
    val date = parseIso(value) ?: return value.orEmpty()
    val output = SimpleDateFormat("MMM d, yyyy h:mm a", Locale.US)
    return output.format(date)
}
