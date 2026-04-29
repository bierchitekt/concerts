package com.bierchitekt.concerts;

import com.fasterxml.jackson.annotation.JsonProperty;

record WhatsappMessage(
        String chatId,
        String text,
        @JsonProperty("reply_to") String replyTo,
        boolean linkPreview,
        boolean linkPreviewHighQuality,
        String session) {
}