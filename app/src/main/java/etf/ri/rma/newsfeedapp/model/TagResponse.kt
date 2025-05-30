package etf.ri.rma.newsfeedapp.model

data class ImaggaTagResponse(
    val result: Result
)

data class Result(
    val tags: List<Tag>
)

data class Tag(
    val tag: TagDetails
)

data class TagDetails(
    val en: String
)