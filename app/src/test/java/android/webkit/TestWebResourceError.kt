package android.webkit

class TestWebResourceError(
    private val errorCode: Int,
    private val description: String
) : WebResourceError() {
    override fun getErrorCode(): Int = errorCode
    override fun getDescription(): CharSequence = description
}
