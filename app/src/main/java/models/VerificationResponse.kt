package models

data class VerificationResponse(
    val result: VerificationResult
)

data class VerificationResult(
    val verified: Boolean,
    val distance: Double,
    val max_threshold_to_verify: Double,
    val model: String,
    val similarity_metric: String
)