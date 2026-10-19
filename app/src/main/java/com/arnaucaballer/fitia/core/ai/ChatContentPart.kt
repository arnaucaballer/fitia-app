package com.arnaucaballer.fitia.core.ai

sealed interface ChatContentPart {
    data class Text(val text: String) : ChatContentPart
    data class ImageBase64(val mimeType: String, val base64Data: String) : ChatContentPart
}
