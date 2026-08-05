// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (C) 2026 DevTools Contributors

package org.devtools.app.tools

import android.util.Base64
import java.net.URLDecoder
import java.net.URLEncoder
import java.security.MessageDigest
import java.util.UUID

object ToolEngine {

    // --- Base64 ---
    fun base64Encode(input: String): String {
        return Base64.encodeToString(input.toByteArray(Charsets.UTF_8), Base64.NO_WRAP)
    }

    fun base64Decode(input: String): String {
        return try {
            String(Base64.decode(input.trim(), Base64.DEFAULT), Charsets.UTF_8)
        } catch (e: Exception) {
            "Error: Invalid Base64 input"
        }
    }

    // --- URL Encode/Decode ---
    fun urlEncode(input: String): String {
        return try {
            URLEncoder.encode(input, "UTF-8")
        } catch (e: Exception) {
            "Error: ${e.message}"
        }
    }

    fun urlDecode(input: String): String {
        return try {
            URLDecoder.decode(input, "UTF-8")
        } catch (e: Exception) {
            "Error: Invalid URL-encoded input"
        }
    }

    // --- HTML Entities ---
    fun htmlEncode(input: String): String {
        return input
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#39;")
    }

    fun htmlDecode(input: String): String {
        return input
            .replace("&amp;", "&")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&quot;", "\"")
            .replace("&#39;", "'")
            .replace("&#x27;", "'")
            .replace("&#x2F;", "/")
    }

    // --- Hex <-> Text ---
    fun textToHex(input: String): String {
        return input.toByteArray(Charsets.UTF_8).joinToString(" ") { "%02X".format(it) }
    }

    fun hexToText(input: String): String {
        return try {
            val cleaned = input.replace(" ", "").replace("0x", "").replace(",", "")
            val bytes = cleaned.chunked(2).map { it.toInt(16).toByte() }.toByteArray()
            String(bytes, Charsets.UTF_8)
        } catch (e: Exception) {
            "Error: Invalid hex input"
        }
    }

    // --- Binary <-> Text ---
    fun textToBinary(input: String): String {
        return input.toByteArray(Charsets.UTF_8).joinToString(" ") {
            Integer.toBinaryString(it.toInt() and 0xFF).padStart(8, '0')
        }
    }

    fun binaryToText(input: String): String {
        return try {
            val cleaned = input.replace(" ", "")
            val bytes = cleaned.chunked(8).map { it.toInt(2).toByte() }.toByteArray()
            String(bytes, Charsets.UTF_8)
        } catch (e: Exception) {
            "Error: Invalid binary input"
        }
    }

    // --- Hash Functions ---
    fun md5(input: String): String = hash(input, "MD5")
    fun sha1(input: String): String = hash(input, "SHA-1")
    fun sha256(input: String): String = hash(input, "SHA-256")
    fun sha512(input: String): String = hash(input, "SHA-512")

    private fun hash(input: String, algorithm: String): String {
        return try {
            val digest = MessageDigest.getInstance(algorithm)
            val hashBytes = digest.digest(input.toByteArray(Charsets.UTF_8))
            hashBytes.joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            "Error: ${e.message}"
        }
    }

    // --- UUID Generator ---
    fun generateUUID(): String = UUID.randomUUID().toString()

    // --- Unix Timestamp ---
    fun currentTimestamp(): String = (System.currentTimeMillis() / 1000).toString()

    fun timestampToDate(input: String): String {
        return try {
            val ts = input.trim().toLong()
            val millis = if (ts > 9999999999L) ts else ts * 1000
            java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z", java.util.Locale.US)
                .format(java.util.Date(millis))
        } catch (e: Exception) {
            "Error: Invalid timestamp"
        }
    }

    // --- JSON Formatter ---
    fun formatJson(input: String): String {
        return try {
            val trimmed = input.trim()
            if (trimmed.startsWith("{") || trimmed.startsWith("[")) {
                val obj = org.json.JSONTokener(trimmed).nextValue()
                when (obj) {
                    is org.json.JSONObject -> obj.toString(2)
                    is org.json.JSONArray -> obj.toString(2)
                    else -> "Error: Not a valid JSON object or array"
                }
            } else {
                "Error: Input must start with { or ["
            }
        } catch (e: Exception) {
            "Error: ${e.message}"
        }
    }

    // --- JWT Decoder ---
    fun decodeJwt(input: String): String {
        return try {
            val parts = input.trim().split(".")
            if (parts.size < 2) return "Error: Invalid JWT format (need at least 2 parts)"

            val header = String(Base64.decode(parts[0], Base64.URL_SAFE or Base64.NO_WRAP), Charsets.UTF_8)
            val payload = String(Base64.decode(parts[1], Base64.URL_SAFE or Base64.NO_WRAP), Charsets.UTF_8)

            val headerFormatted = try {
                org.json.JSONObject(header).toString(2)
            } catch (e: Exception) { header }

            val payloadFormatted = try {
                org.json.JSONObject(payload).toString(2)
            } catch (e: Exception) { payload }

            "=== HEADER ===\n$headerFormatted\n\n=== PAYLOAD ===\n$payloadFormatted"
        } catch (e: Exception) {
            "Error: ${e.message}"
        }
    }

    // --- Character Counter ---
    fun countChars(input: String): String {
        val chars = input.length
        val words = if (input.isBlank()) 0 else input.trim().split("\\s+".toRegex()).size
        val lines = if (input.isEmpty()) 0 else input.lines().size
        val bytes = input.toByteArray(Charsets.UTF_8).size
        return "Characters: $chars\nWords: $words\nLines: $lines\nBytes (UTF-8): $bytes"
    }
}
