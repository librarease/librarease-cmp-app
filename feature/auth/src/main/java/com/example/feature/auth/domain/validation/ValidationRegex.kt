package com.example.feature.auth.domain.validation

object ValidationRegex {
    val EMAIL_REGEX = Regex(
        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    )
    
    val PASSWORD_REGEX = Regex(
        "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@\$!%*?&#^()\\-_+=\\[\\]{}|;:',.<>/~`])[A-Za-z\\d@\$!%*?&#^()\\-_+=\\[\\]{}|;:',.<>/~`]{8,}$"
    )
}
