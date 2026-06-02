package com.example.core.presentation.review

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.R
import com.example.core.model.review.Review
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

@Composable
fun ReviewItem(
    review: Review,
    modifier: Modifier = Modifier,
    showRating: Boolean = false
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(colorResource(id = R.color.background).copy(alpha = 0.42f))
            .border(
                width = 1.dp,
                color = colorResource(id = R.color.border),
                shape = RoundedCornerShape(14.dp)
            )
            .padding(14.dp)
    ) {
        if (showRating && review.rating != null) {
            RatingStars(rating = review.rating)
            Spacer(modifier = Modifier.height(10.dp))
        }

        Text(
            text = review.comment.takeDisplayComment(),
            color = colorResource(id = R.color.primary),
            fontSize = 14.sp,
            lineHeight = 20.sp
        )

        val meta = listOfNotNull(
            review.reviewerName?.takeIf { it.isNotBlank() },
            formatReviewDate(review.createdAt)
        )

        if (meta.isNotEmpty()) {
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = meta.joinToString(" • "),
                color = colorResource(id = R.color.secondary),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun RatingStars(
    rating: Double,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(5) { index ->
            Icon(
                imageVector = if (index < rating.toInt()) Icons.Filled.Star else Icons.Outlined.StarOutline,
                contentDescription = null,
                tint = if (index < rating.toInt()) Color(0xFFFBBF24) else colorResource(id = R.color.secondary),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

fun String?.takeDisplayComment(): String {
    return this?.takeIf { it.isNotBlank() } ?: "No comment provided."
}

fun formatReviewDate(value: String?): String? {
    val date = parseReviewDate(value) ?: return value?.takeIf { it.isNotBlank() }?.take(10)
    return SimpleDateFormat("MMM d, yyyy", Locale.US).format(date)
}

private fun parseReviewDate(value: String?): java.util.Date? {
    if (value.isNullOrBlank()) return null

    val utcParser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }
    val dateTimeParser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
    val dateOnlyParser = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    return runCatching { utcParser.parse(value) }.getOrNull()
        ?: runCatching { dateTimeParser.parse(value) }.getOrNull()
        ?: runCatching { dateOnlyParser.parse(value) }.getOrNull()
}
