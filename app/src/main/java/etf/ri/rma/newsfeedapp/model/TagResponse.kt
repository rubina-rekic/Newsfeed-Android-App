package etf.ri.rma.newsfeedapp.model

data class ImagaTagInfo(
    val en: String
)
data class ImaggaTag(
    val tag: ImagaTagInfo
)

data class ImaggaTagResult(
    val result: ImaggaResult
)

data class ImaggaResult(
    val tags: List<ImaggaTag>
)