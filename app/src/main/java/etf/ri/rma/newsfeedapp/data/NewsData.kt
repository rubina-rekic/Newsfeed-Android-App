package etf.ri.rma.newsfeedapp.data
import etf.ri.rma.newsfeedapp.model.NewsItem

object NewsData {
    fun getAllNews(): List<NewsItem> {
        return listOf(

            NewsItem(
                id = "1",
                title = "Prva kora Zemlje imala je karakteristike današnjih kontinentalnih stijena",
                snippet = "Nova istraživanja sugeriraju da je prva kora Zemlje, formirana prije više od 4,5 milijardi godina, već nosila hemijske karakteristike koje povezujemo s modernim kontinentima. To znači da tragovi kontinentalne kore nisu trebali tektonske ploče da bi se formirali, što dovodi u pitanje dugogodišnju teoriju.",
                imageUrl = null,
                category = "Nauka/tehnologija",
                isFeatured = false,
                source = "SciTechDaily",
                publishedDate = "04-04-2025"
            ),
            NewsItem(
                id = "2",
                title = "Kapsula SpaceX lansirana u misiju orbitiranja oko polova Zemlje",
                snippet = "Svemirska kapsula Crew Dragon, u vlasništvu SpaceX-a, lansirana je u Zemljinu orbitu. Misija traje 3 do 5 dana, a astronauti će učestvovati u 22 istraživačka eksperimenta.",
                imageUrl = null,
                category = "Nauka/tehnologija",
                isFeatured = false,
                source = "Nezavisne novine",
                publishedDate ="01-04-2025"
            ),
            NewsItem(
                id = "3",
                title = "Naučnici razvili prvi All-in-one čip za kvantni internet",
                snippet = "Naučnici u Oak Ridge National Laboratory razvili su prvi čip koji integriše ključne kvantne fotonske komponente, čime je napravljen veliki korak ka skalabilnom kvantnom internetu. Čip omogućava prijenos kvantnih informacija preko postojećih optičkih vlakana.",
                imageUrl = null,
                category = "Nauka/tehnologija",
                isFeatured = true,
                source = "SciTechDaily",
                publishedDate = "05-03-2025"
            ),
            NewsItem(
                id = "4",
                title = "Luis Enrique: Trener koji je transformisao PSG",
                snippet = "Luis Enrique, menadžer Paris Saint-Germaina, doveo je ekipu do uzastopnih titula u Ligue 1. Nakon lične tragedije, njegova posvećenost i filozofija tima odvela je PSG do novih visina. Tim je postao kolektivna, kohezivna i otporna jedinica.",
                imageUrl = null,
                category = "Sport",
                isFeatured = true,
                source = "BBCC",
                publishedDate = "05-04-2025"
            ),
            NewsItem(
                id = "5",
                title = "Kalendar F1 utrka za 2025. godinu",
                snippet = "U 2025. sezoni F1 biće održane šest sprint trka. Format ostaje isti kao u 2024. godini, sa sprint kvalifikacijama u petak, trkom od 100 km u subotu i standardnom trkom u nedjelju. Sprint trke će se održati na pet različitih lokacija tokom sezone.",
                imageUrl = null,
                category = "Sport",
                isFeatured = true,
                source = "BBC",
                publishedDate = "20-03-2025"
            ),
            NewsItem(
                id = "6",
                title = "Mars pokriven otrovnom prašinom",
                snippet = "Nedavna istraživanja sugeriraju da Marsova prašina sadrži toksicne spojeve koji mogu ozbiljno oštetiti ljudsko zdravlje. Prašina koja sadrži silikate, perhlorate i teške metale, može uzrokovati respiratorne probleme, disfunkciju štitnjače i druge zdravstvene poteškoće. Stručnjaci savjetuju razvoj tehnologija koje će zaštititi astronaute tokom budućih misija.",
                imageUrl = null,
                category = "Nauka/tehnologija",
                isFeatured = true,
                source = "SciTechDaily",
                publishedDate = "02-04-2025"
            ),
            NewsItem(
                id = "7",
                title = "Đoković dolazi do finala Miamija i približava se 100. tituli",
                snippet = "Novak Đoković je pobijedio Grigora Dimitrova i stigao do svog prvog finala na Miami Openu od 2016. godine, ujedno se približavajući 100. ATP tituli u svojoj karijeri. U finalu će se susresti s češkim igračem Jakubom Mensikom.",
                imageUrl = null,
                category = "Sport",
                isFeatured = true,
                source = "BBC",
                publishedDate = "28-04-2025"
            ),
            NewsItem(
                id = "8",
                title = "Rubio upozorava Rusiju da je vrijeme za mirovne pregovore gotovo",
                snippet = "Američki državni tajnik Marco Rubio poslao je Putinu jasnu poruku: vrijeme za postizanje napretka u mirovnim pregovorima u Ukrajini ističe. Iako Trump i njegovi suradnici smatraju da Putin želi mir, saveznici sumnjaju u iskrenost njegovih namjera.",
                imageUrl = null,
                category = "Politika",
                isFeatured = true,
                source = "CNN",
                publishedDate = "02-04-2025"
            ),
            NewsItem(
                id = "9",
                title = "Što je izgradio FDR, Trump želi srušiti",
                snippet = "Donald Trump u svom drugom mandatu pokušava uništiti institucije koje je Franklin D. Roosevelt izgradio tijekom New Deala. Od ukidanja vladinih agencija do promjena trgovinskih odnosa, Trumpov pristup je suprotan Rooseveltovom.",
                imageUrl = null,
                category = "Politika",
                isFeatured = true,
                source = "CNNN",
                publishedDate = "07-04-2025"
            ),
            NewsItem(
                id = "10",
                title = "Fizičari sa UCLA otkrili misterozne spirale na čvrstim površinama",
                snippet = "Doktorski student sa Univerziteta u Kaliforniji, Los Angeles, Yilin Wong, otkrio je nevjerovatne spiralne uzorke na maloj germanijskoj ploči. Istraživanja su pokazala da se ovi uzorci spontano formiraju kroz hemijsku reakciju u interakciji sa mehaničkim silama, što predstavlja najveći napredak u proučavanju hemijskih obrazaca još od 1950-ih godina.",
                imageUrl = null,
                category = "Nauka/tehnologija",
                isFeatured = true,
                source = "SciTechDaily",
                publishedDate = "12-04-2025"
            ),
            NewsItem(
                id = "11",
                title = "Magnus protiv svijeta: Rekord sa 100.000 učesnika postavljen",
                snippet = "U iščekivanoj partiji Magnus protiv svijeta, GM Magnus Carlsen je napravio prvi potez na Chess.com, a događaj je postavio rekord sa 100.000 prijavljenih učesnika. Ovaj događaj je najposećeniji online šahovski meč u istoriji, koji je nadmašio prethodni rekord postavljen prošle godine.",
                imageUrl = null,
                category = "Sport",
                isFeatured = true,
                source = "Chess.com",
                publishedDate = "02-04-2025"
            ),
            NewsItem(
                id = "12",
                title = "Barcelona vs Real Betis: LaLiga",
                snippet = "Barcelona pokušava povećati prednost na šest bodova u LaLigi kada se sastanu sa Real Betisom u subotu. Ne zan se hoče li Barcelonin mladi igrač Lamine Yamal igrati utakmicu zbog povrede stopala.",
                imageUrl = null,
                category = "Sport",
                isFeatured = false,
                source = "Al Jazeera",
                publishedDate = "18-04-2025"
            ),
            NewsItem(
                id = "13",
                title = "Ukrajina se suočava s krizom oko eksploatacije rijetkih minerala",
                snippet = "Ukrajinski zvaničnici tvrde da njihova zemlja posjeduje značajne rezerve rijetkih minerala, ključnih za industriju, tehnologiju i odbranu. Ako bi Rusija preuzela kontrolu nad tim resursima, to bi moglo imati katastrofalne posljedice za saveznike Ukrajine.",
                imageUrl = null,
                category = "Politika",
                isFeatured = true,
                source = "N1 BiH",
                publishedDate = "02-04-2025"
            ),
            NewsItem(
                id = "14",
                title = "Real Madrid vs Valencia - Neočekivan preokret",
                snippet = "Valencia je šokirala Real Madrid sa pobjedom u 95. minuti, Hugo Duro je postigao pobjednički gol u sudijskoj nadoknadi nakon što je Rafa Mir odigrao savršen centaršut.",
                imageUrl = null,
                category = "Sport",
                isFeatured = false,
                source = "BBC",
                publishedDate = "02-03-2025"
            ),
            NewsItem(
                id = "15",
                title = "Zamjenik iranskog predsjednika smijenjen zbog luksuznog putovanja na Antarktiku",
                snippet = "Iranski predsjednik Masoud Pezeškian smijenio je svog zamjenika Šahrama Dabirija zbog luksuznog putovanja na Antarktiku tijekom proslave iranske nove godine, Nowruza, koje je izazvalo bijes u zemlji. Putovanje je opisano kao 'neopravdano' s obzirom na ekonomske izazove.",
                imageUrl = null,
                category = "Politika",
                isFeatured = false,
                source = "Index.hr",
                publishedDate = "08-04-2025"
            ),
            NewsItem(
                id = "16",
                title = "\"Zapanjujuće otkriće\": Tamna energija možda se mijenja, i to bi moglo promijeniti sve",
                snippet = "Najnovija istraživanja projekta DESI sugeriraju da tamna energija možda nije stalna, kako se dosad vjerovalo. Otkriće bi moglo dramatično promijeniti kozmologiju kakvu poznajemo.",
                imageUrl = null,
                category = "Nauka/tehnologija",
                isFeatured = true,
                source = "Indexx.hr",
                publishedDate = "02-04-2025"
            ),
            NewsItem(
                id = "17",
                title = "Parkinsonova bolest povezana s bakterijama. Mogla bi se liječiti?",
                snippet = "Nova studija povezuje Parkinsonovu bolest s bakterijama u crijevima koje omogućuju proizvodnju vitamina B, što sugerira da bi bolest mogla biti prevenirana ili liječena.",
                imageUrl = null,
                category = "Nauka/tehnologija",
                isFeatured = true,
                source = "Indeex.hr",
                publishedDate = "29-03-2025"
            ),
            NewsItem(
                id = "18",
                title = "Žvakaće gume otpuštaju stotine čestica mikroplastike u usta, pokazuje studija",
                snippet = "Studija pokazuje da žvakaće gume otpuštaju stotine čestica mikroplastike u usta, iako je istraživanje oprezno u procjeni njihovog utjecaja na zdravlje.",
                imageUrl = null,
                category = "Nauka/tehnologija",
                isFeatured = true,
                source = "SciTechDaily",
                publishedDate = "26-03-2025"
            ),
            NewsItem(
                id = "19",
                title = "Plenković: Nedopustiv je ishod u kojem Rusija dobija okupirani ukrajinski teritorij",
                snippet = "Premijer Andrej Plenković izjavio je da EU mora raditi na svojoj strateškoj autonomiji, te da je ishod u kojem Rusija zadrži okupirane teritorije Ukrajine nedopustiv.",
                imageUrl = null,
                category = "Politika",
                isFeatured = true,
                source = "Index.hr",
                publishedDate = "02-04-2025"
            ),
            NewsItem(
                id = "20",
                title = "Ćelije kože emitiraju električne impulse nakon povrede",
                snippet = "Oštećene ćelije kože, poput keratinocita prikazanih na elektronskoj mikrografiji sa strane, šalju električne impulse, otkriva nova studija. Ovaj signal može poslužiti kao signal koji poziva susjedne ćelije da započnu proces zacjeljivanja povrede.",
                imageUrl = null,
                category = "Nauka/tehnologija",
                isFeatured = false,
                source = "SciTechDailyyy",
                publishedDate = "02-04-2025"
            )

        ).sortedBy { it.id.toInt()}
    }
}