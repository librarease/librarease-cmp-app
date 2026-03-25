package com.example.feature.home.presentation.home

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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.Subscriptions
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
import com.example.feature.home.domain.model.Subscription
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
internal fun SubscriptionsSection(
    uiState: HomeUiState,
    onSubscriptionClick: (String) -> Unit
) {
    if (uiState is HomeUiState.Idle) return

    val subscriptions = (uiState as? HomeUiState.Content)?.subscriptions.orEmpty()

    SectionHeader(title = "Your Memberships")
    Spacer(modifier = Modifier.height(12.dp))

    when {
        uiState is HomeUiState.Loading -> {
            EmptyCard(message = "Loading memberships...")
        }
        subscriptions.isEmpty() -> {
            EmptyCard(message = "You do not have active memberships")
        }
        else -> {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                subscriptions.forEach { subscription ->
                    SubscriptionCard(
                        subscription = subscription,
                        onSubscriptionClick = onSubscriptionClick
                    )
                }
            }
        }
    }
}

@Composable
private fun SubscriptionCard(
    subscription: Subscription,
    onSubscriptionClick: (String) -> Unit
) {
    val expiryLabel = formatExpiry(subscription.expiresAt)
    val status = subscription.statusLabel()
    val statusColor = subscription.statusColor()
    val statusBackground = statusColor.copy(alpha = 0.12f)
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSubscriptionClick(subscription.id) }
            .clip(RoundedCornerShape(14.dp))
            .background(colorResource(id = R.color.surface_light))
            .border(
                width = 1.dp,
                color = colorResource(id = R.color.border),
                shape = RoundedCornerShape(14.dp)
            )
            .padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(colorResource(id = R.color.border)),
                    contentAlignment = Alignment.Center
                ) {
                    val logoUrl = subscription.libraryLogoUrl
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
                            contentDescription = subscription.libraryName,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize(),
                            loading = {
                                CircularProgressIndicator(
                                    color = colorResource(id = R.color.accent),
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
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = subscription.membershipName.ifBlank { "Membership" },
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colorResource(id = R.color.primary),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = subscription.libraryName?.ifBlank { "Library" } ?: "Library",
                        fontSize = 12.sp,
                        color = colorResource(id = R.color.secondary),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Column(
                modifier = Modifier.widthIn(min = 92.dp),
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(statusBackground)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = status,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = statusColor
                    )
                }
                Text(
                    text = expiryLabel,
                    fontSize = 12.sp,
                    color = colorResource(id = R.color.secondary),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

private fun Subscription.statusLabel(): String {
    val now = java.util.Date()
    val expiry = parseDate(expiresAt) ?: return "Active"
    if (expiry.before(now)) return "Expired"

    val millisLeft = expiry.time - now.time
    val daysLeft = millisLeft / (1000L * 60L * 60L * 24L)
    return if (daysLeft <= 30) "Expiring" else "Active"
}

private fun Subscription.statusColor(): Color {
    return when (statusLabel().lowercase(Locale.US)) {
        "expired" -> Color(0xFFE53935)
        "expiring" -> Color(0xFFF57C00)
        else -> Color(0xFF2E7D32)
    }
}

private fun formatExpiry(value: String?): String {
    val date = parseDate(value) ?: return "Until --"
    val output = SimpleDateFormat("MMM yyyy", Locale.US)
    return "Until ${output.format(date)}"
}
