package etf.ri.rma.newsfeedapp.data
import etf.ri.rma.newsfeedapp.model.NewsItem

object NewsData {
    fun getAllNews(): List<NewsItem> {
        return listOf(
                NewsItem(
                    uuid = "42acb8c8-54ca-41b6-b703-fd3789f0385b",
                    title = "Slushy start: warmer winter a downer for ski slopes",
                    snippet = "The first dustings of winter snow have arrived on Australian mountaintops but long-range weather forecasts suggest it could be slow start to the ski season.\n\nSk...",
                    imageUrl = "https://michaelwest.com.au/wp-content/uploads/2025/06/2cae69ef-2833-449b-a3e7-3ee326eea1b0.jpg",
                    category = "general", // uzimamo prvu kategoriju iz liste
                    isFeatured = false, // podaci to ne navode, postavljamo na false
                    source = "michaelwest.com.au",
                    publishedDate = "05-05-2025"
                ),
            NewsItem(
                uuid = "66d23d1c-12c8-4365-b580-1513c74d7641",
                title = "Russia warns US about Golden Dome scheme",
                snippet = "Washington’s plans for a global anti-ballistic missile system are eroding strategic stability, the Foreign Ministry has says\n\nThe US is taking a “reckless a...",
                imageUrl = "https://mf.b37mrtl.ru/files/2025.05/article/6835a41520302772cf325714.jpg",
                category = "general",
                isFeatured = false,
                source = "rt.com",
                publishedDate = "27-05-2025"
            ),
            NewsItem(
                uuid = "5fc60db3-a631-4076-8f1d-36f41472f3c6",
                title = "There are two Gen Zs",
                snippet = "is a senior politics reporter at Vox, where he covers the Democratic Party. He joined Vox in 2022 after reporting on national and international politics for the...",
                imageUrl = "https://platform.vox.com/wp-content/uploads/sites/2/2025/05/gettyimages-2158175237.jpg?quality=90&strip=all&crop=0%2C10.728733235956%2C100%2C78.542533528088&w=1200",
                category = "general",
                isFeatured = false,
                source = "vox.com",
                publishedDate = "27-05-2025"
            ),
            NewsItem(
                uuid = "1da7b726-5ec3-4658-b66c-a6732f74443a",
                title = "£7.5bn of Universal Credit went unclaimed in one year – yet Labour is cutting people’s benefits?",
                snippet = "An estimated £7.5 billion in Universal Credit benefits remain unclaimed in 2023 due to Department for Work and Pensions (DWP) complicated eligibility criteria,...",
                imageUrl = "https://www.thecanary.co/wp-content/uploads/2025/05/Untitled-design-2025-05-27T123844.474.jpg",
                category = "general",
                isFeatured = false,
                source = "thecanary.co",
                publishedDate = "27-05-2025"
            ),
            NewsItem(
                uuid = "5ec86be8-faeb-459b-b2ef-cb39a9dba4a2",
                title = "NASA warns! Massive 25-storey building sized asteroid is set to pass close to Earth on May 28 - know the speed, time and other key details",
                snippet = "Asteroid 2025 JR is moving towards Earth on May 28\n\n\n\nAsteroid 2025 JR: Date, time, speed and other details\n\nParameter\n\nDetails\n\nName\n\nAsteroid 2025 JR\n\nClosest...",
                imageUrl = "https://static.toiimg.com/thumb/msid-121437326,width-1070,height-580,imgsize-49492,resizemode-75,overlay-toi_sw,pt-32,y_pad-40/photo.jpg",
                category = "science",
                isFeatured = false,
                source = "timesofindia.indiatimes.com",
                publishedDate = "27-05-2025"
            ),
            NewsItem(
                uuid = "72bd09dc-f28c-48d7-a64f-8fc36c4a46cf",
                title = "1,000-foot Tsunami warning! Cascadia megaquake could wipe parts of America off the map",
                snippet = "1000 foot Tsunami warning\n\nCascadia fault: A 600-mile earthquake risk under the US\n\nThe dual threat: Earthquake + climate change\n\nCoastal land could sink by up ...",
                imageUrl = "https://static.toiimg.com/thumb/msid-121437037,width-1070,height-580,imgsize-81916,resizemode-75,overlay-toi_sw,pt-32,y_pad-40/photo.jpg",
                category = "science",
                isFeatured = false,
                source = "timesofindia.indiatimes.com",
                publishedDate = "27-05-2025"
            ),
            NewsItem(
                uuid = "20659a1c-7754-48e6-a8fa-07db93d2041b",
                title = "Stanotte nuovo volo per Starship, test chiave verso Luna e Marte",
                snippet = "E’ previsto alle 1:30 italiane del 28 maggio il nono volo di prova di Starship, la nave di SpaceX progettata per le future missioni verso Luna e Marte. Un tes...",
                imageUrl = "https://www.ansa.it/webimages/img_1129x635/2025/5/27/e2876922503dbd6b16798b76f3529d94.jpeg",
                category = "science",
                isFeatured = false,
                source = "ansa.it",
                publishedDate = "27-05-2025"
            ),
            NewsItem(
                uuid = "99729e6e-a48e-49bc-a089-0e927d7f274e",
        title = "Summer transfers each top Premier League club should make",
        snippet = "Open Extended Reactions\n\nThe Premier League is in a weird spot, and it's a particular kind of weirdness that could make this one of the most active summers of t...",
        imageUrl = "https://a3.espncdn.com/combiner/i?img=%2Fphoto%2F2025%2F0404%2Fr1473457_1296x729_16%2D9.jpg",
        category = "sports",
        isFeatured = false,
        source = "espn.com",
        publishedDate = "2025-05-27T12:13:07.000000Z"
        ),
            NewsItem(
                uuid = "7d3a1a0e-5a79-4e10-825e-5cf2064ca1b3",
        title = "Jose Mourinho expects to stay at Fenerbahce for second season",
        snippet = "Open Extended Reactions\n\nJose Mourinho is expecting to be at Fenerbahce next season. Ahmad Mora/Getty Images\n\nFenerbahce coach Jose Mourinho has said he expects...",
        imageUrl = "https://a2.espncdn.com/combiner/i?img=%2Fphoto%2F2024%2F1210%2Fr1426287_1296x729_16%2D9.jpg",
        category = "sports",
        isFeatured = false,
        source = "espn.com",
        publishedDate = "2025-05-27T12:11:45.000000Z"
        ),
            NewsItem(
                uuid = "9f489659-123a-4249-90ee-cc666a1d2c72",
                title = "Ruben Amorim: Missing Champions League could benefit Man United next season",
                snippet = "Ruben Amorim speaks about how not playing in the Champions League could benefit Manchester United next season. (0:53)\n\nAmorim: Not being in the UCL could be an ...",
                imageUrl = "https://a3.espncdn.com/combiner/i?img=%2Fphoto%2F2025%2F0527%2Fr1498904_1296x729_16%2D9.jpg",
                category = "sports",
                isFeatured = false,
                source = "espn.com",
                publishedDate = "2025-05-27T12:11:45.000000Z"
            )


        )
        return emptyList();
    }
}