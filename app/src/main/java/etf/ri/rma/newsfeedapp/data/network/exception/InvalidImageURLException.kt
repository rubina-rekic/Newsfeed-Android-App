package etf.ri.rma.newsfeedapp.data.network.exception

class InvalidImageURLException(message : String = "URL slike nije validan") : Exception(message){
}

class NetworkException(message: String, cause: Throwable? = null) : Exception(message, cause)

class ImageTaggingException(message: String, cause: Throwable? = null) : Exception(message, cause)