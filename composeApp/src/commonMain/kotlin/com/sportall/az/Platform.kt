package com.sportall.az

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform