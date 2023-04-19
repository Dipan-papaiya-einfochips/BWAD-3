package kaz.bpmandroid

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform