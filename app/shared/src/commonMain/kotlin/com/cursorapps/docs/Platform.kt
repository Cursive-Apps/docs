package com.cursorapps.docs

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform