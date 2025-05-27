package etf.ri.rma.newsfeedapp.data

data class ImaggaTagResponse(
    val result: TagResult
)

data class TagResult(
    val tags: List<Tag>
)

data class Tag(
    val tag: String,
    val confidence: Double
)
