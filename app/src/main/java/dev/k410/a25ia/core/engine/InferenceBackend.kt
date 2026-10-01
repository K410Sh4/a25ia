package dev.k410.a25ia.core.engine

import dev.k410.a25ia.core.model.InferenceRequest
import dev.k410.a25ia.core.model.InferenceResult

interface InferenceBackend {
    val id: String
    suspend fun generate(request: InferenceRequest): InferenceResult
}
