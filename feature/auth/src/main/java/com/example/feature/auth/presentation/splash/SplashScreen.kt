package com.example.feature.auth.presentation.splash

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.core.R
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    viewModel: SplashViewModel = viewModel(),
    onNavigateToAuth: () -> Unit = {},
    onNavigateToHome: () -> Unit = {}
) {
    LaunchedEffect(Unit) {
        delay(2000)
        viewModel.checkAuthState(
            onAuthenticated = onNavigateToHome,
            onUnauthenticated = onNavigateToAuth
        )
    }

    SplashContent()
}

@Composable
private fun SplashContent() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(id = R.color.accent)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                BookIcon()
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Librarease",
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "YOUR PERSONAL ARCHIVE",
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal,
                color = Color.White.copy(alpha = 0.9f),
                letterSpacing = 2.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun BookIcon() {
    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.size(48.dp)
    ) {
        Box(
            modifier = Modifier
                .width(8.dp)
                .height(36.dp)
                .rotate(-8f)
                .clip(RoundedCornerShape(1.dp))
                .background(colorResource(id = com.example.core.R.color.accent))
        )
        
        Spacer(modifier = Modifier.width(3.dp))
        
        Box(
            modifier = Modifier
                .width(8.dp)
                .height(36.dp)
                .clip(RoundedCornerShape(1.dp))
                .background(colorResource(id = com.example.core.R.color.accent))
        )
        
        Spacer(modifier = Modifier.width(3.dp))
        
        Box(
            modifier = Modifier
                .width(8.dp)
                .height(36.dp)
                .rotate(8f)
                .clip(RoundedCornerShape(1.dp))
                .background(colorResource(id = com.example.core.R.color.accent))
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun SplashScreenPreview() {
    SplashContent()
}
