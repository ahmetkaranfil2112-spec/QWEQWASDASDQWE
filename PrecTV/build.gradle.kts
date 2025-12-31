version = 1

cloudstream {
    authors     = listOf("PrecTV")
    language    = "tr"
    description = "PrecTV - Turkish streaming service"

    /**
     * Status int as the following:
     * 0: Down
     * 1: Ok
     * 2: Slow
     * 3: Beta only
    **/
    status  = 1
    tvTypes = listOf("TvSeries", "Movie")
    iconUrl = "https://www.google.com/s2/favicons?domain=m.prectv60.lol&sz=%size%"
}
