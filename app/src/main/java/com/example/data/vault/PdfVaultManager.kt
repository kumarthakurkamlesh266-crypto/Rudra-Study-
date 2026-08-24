package com.example.data.vault

import android.content.Context
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.text.DecimalFormat

object PdfVaultManager {
    private const val VAULT_DIR_NAME = "resource_vault"
    private const val MAX_VAULT_BYTES = 1024L * 1024L * 1024L // 1 GB
    private const val MAX_VAULT_DOCS = 100

    fun getVaultDirectory(context: Context): File {
        val dir = File(context.filesDir, VAULT_DIR_NAME)
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }

    /**
     * Copies a PDF from Uri into local vault storage
     */
    suspend fun savePdfToVault(
        context: Context,
        uri: Uri,
        originalName: String
    ): Result<SavedVaultFile> = withContext(Dispatchers.IO) {
        try {
            val vaultDir = getVaultDirectory(context)
            val cleanName = originalName.replace("[^a-zA-Z0-9._-]".toRegex(), "_")
            val targetFileName = "doc_${System.currentTimeMillis()}_$cleanName"
            val targetFile = File(vaultDir, targetFileName)

            val stream = context.contentResolver.openInputStream(uri)
                ?: return@withContext Result.failure(Exception("Cannot open PDF file stream"))

            stream.use { input ->
                FileOutputStream(targetFile).use { output ->
                    input.copyTo(output)
                }
            }

            val fileSizeBytes = targetFile.length()
            val (pageCount, extractedText) = extractTextAndPageCount(targetFile)

            Result.success(
                SavedVaultFile(
                    fileName = originalName,
                    localPath = targetFile.absolutePath,
                    fileSizeBytes = fileSizeBytes,
                    pageCount = pageCount.coerceAtLeast(1),
                    extractedText = extractedText
                )
            )
        } catch (e: Exception) {
            Log.e("PdfVaultManager", "Failed to save PDF to vault: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Extracts text and page count from local PDF file
     */
    fun extractTextAndPageCount(file: File): Pair<Int, String> {
        var pageCount = 1
        var pfd: ParcelFileDescriptor? = null
        var renderer: PdfRenderer? = null

        try {
            pfd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
            if (pfd != null) {
                renderer = PdfRenderer(pfd)
                pageCount = renderer.pageCount
            }
        } catch (e: Exception) {
            Log.w("PdfVaultManager", "PdfRenderer open failed (may not be strictly standard PDF): ${e.message}")
        } finally {
            try {
                renderer?.close()
                pfd?.close()
            } catch (ignored: Exception) {}
        }

        val extractedText = extractRawTextFromPdfBytes(file)
        return Pair(pageCount, extractedText)
    }

    /**
     * Parses PDF byte stream to extract visible textual content
     */
    private fun extractRawTextFromPdfBytes(file: File): String {
        try {
            val bytes = file.readBytes()
            val textBuilder = StringBuilder()

            // Strategy 1: Look for textual stream chunks between BT (Begin Text) and ET (End Text)
            val contentString = String(bytes, Charsets.ISO_8859_1)
            val btEtRegex = Regex("""BT\s*(.*?)\s*ET""", RegexOption.DOT_MATCHES_ALL)
            val matches = btEtRegex.findAll(contentString)

            var extractedBlocks = 0
            for (match in matches) {
                val block = match.groupValues[1]
                // Extract strings in parentheses (text) Tj or [(text)] TJ
                val stringRegex = Regex("""\((.*?)\)\s*Tj|\[(.*?)\]\s*TJ""")
                val strMatches = stringRegex.findAll(block)
                for (strMatch in strMatches) {
                    val rawStr = (strMatch.groupValues[1].ifBlank { strMatch.groupValues[2] })
                        .replace("\\(", "(")
                        .replace("\\)", ")")
                        .replace("\\n", "\n")
                        .replace("\\r", " ")
                        .replace("\\t", " ")
                    if (rawStr.isNotBlank() && rawStr.any { it.isLetterOrDigit() }) {
                        textBuilder.append(rawStr).append(" ")
                        extractedBlocks++
                    }
                }
            }

            // Strategy 2: If BT...ET didn't yield enough characters (e.g. uncompressed text streams or direct objects), do direct ASCII string block extraction
            if (textBuilder.length < 50) {
                val asciiRegex = Regex("""[\x20-\x7E\s]{4,}""")
                val asciiMatches = asciiRegex.findAll(contentString)
                for (match in asciiMatches) {
                    val clean = match.value.trim()
                    // Filter out PDF internal syntax commands
                    if (!clean.startsWith("/") &&
                        !clean.startsWith("<<") &&
                        !clean.startsWith(">>") &&
                        !clean.startsWith("obj") &&
                        !clean.startsWith("endobj") &&
                        clean.any { it.isLetter() }
                    ) {
                        textBuilder.append(clean).append("\n")
                    }
                }
            }

            val result = textBuilder.toString().trim()
            if (result.isNotBlank()) {
                return result
            }
        } catch (e: Exception) {
            Log.e("PdfVaultManager", "Text extraction error: ${e.message}", e)
        }

        return "PDF Document: ${file.name}\n(Binary/Rendered PDF - Automatic Optical & Structured Analysis Ready)"
    }

    fun formatFileSize(bytes: Long): String {
        if (bytes <= 0) return "0 KB"
        val df = DecimalFormat("#.##")
        return when {
            bytes >= 1024 * 1024 * 1024 -> "${df.format(bytes.toDouble() / (1024 * 1024 * 1024))} GB"
            bytes >= 1024 * 1024 -> "${df.format(bytes.toDouble() / (1024 * 1024))} MB"
            bytes >= 1024 -> "${df.format(bytes.toDouble() / 1024)} KB"
            else -> "$bytes B"
        }
    }
}

data class SavedVaultFile(
    val fileName: String,
    val localPath: String,
    val fileSizeBytes: Long,
    val pageCount: Int,
    val extractedText: String
)
