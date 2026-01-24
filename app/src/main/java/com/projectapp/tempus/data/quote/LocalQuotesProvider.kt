package com.projectapp.tempus.data.quote

import com.projectapp.tempus.data.quote.dto.QuoteDto
import kotlin.random.Random

/**
 * Cung cấp danh sách quotes local để fallback khi API thất bại
 */
object LocalQuotesProvider {
    
    private val quotes = listOf(
        QuoteDto("Hành trình ngàn dặm bắt đầu từ một bước chân.", "Lão Tử"),
        QuoteDto("Thành công là tổng của những nỗ lực nhỏ, lặp đi lặp lại ngày này qua ngày khác.", "Robert Collier"),
        QuoteDto("Đừng chờ đợi. Thời điểm sẽ không bao giờ hoàn hảo.", "Napoleon Hill"),
        QuoteDto("Cách tốt nhất để dự đoán tương lai là tạo ra nó.", "Peter Drucker"),
        QuoteDto("Mỗi ngày là một cơ hội mới để thay đổi cuộc sống của bạn.", null),
        QuoteDto("Hãy tin vào bản thân và tất cả những gì bạn có thể làm.", null),
        QuoteDto("Khó khăn không cản được ta, chỉ có bỏ cuộc mới khiến ta thất bại.", null),
        QuoteDto("Sự kiên trì là chìa khóa của mọi thành công.", null),
        QuoteDto("Hãy biến mỗi ngày thành kiệt tác của bạn.", "John Wooden"),
        QuoteDto("Tập trung vào hành trình, không chỉ đích đến.", null),
        QuoteDto("Bạn mạnh mẽ hơn bạn nghĩ, dũng cảm hơn bạn tin, và thông minh hơn bạn biết.", "A.A. Milne"),
        QuoteDto("Hãy là phiên bản tốt nhất của chính mình.", null),
        QuoteDto("Mỗi bước tiến nhỏ đều quan trọng.", null),
        QuoteDto("Ngày hôm nay là món quà, hãy trân trọng nó.", null),
        QuoteDto("Thất bại là cơ hội để bắt đầu lại thông minh hơn.", "Henry Ford"),
        QuoteDto("Đừng so sánh bản thân với người khác, hãy so với chính mình ngày hôm qua.", null),
        QuoteDto("Mọi thứ bạn muốn đều nằm ở phía bên kia của nỗi sợ.", "Jack Canfield"),
        QuoteDto("Hãy làm những gì bạn có thể, với những gì bạn có, ở nơi bạn đang đứng.", "Theodore Roosevelt"),
        QuoteDto("Tương lai thuộc về những người tin vào vẻ đẹp của giấc mơ.", "Eleanor Roosevelt"),
        QuoteDto("Sự thay đổi bắt đầu từ một quyết định.", null),
        QuoteDto("Học cách nghỉ ngơi, không phải bỏ cuộc.", "Banksy"),
        QuoteDto("Mỗi ngày mới là một trang giấy trắng.", null),
        QuoteDto("Đừng để ngày hôm qua chiếm quá nhiều ngày hôm nay.", "Will Rogers"),
        QuoteDto("Điều quan trọng không phải là bạn ngã bao nhiêu lần, mà là bạn đứng dậy bao nhiêu lần.", null),
        QuoteDto("Cơ hội không tự đến, bạn phải tạo ra nó.", "Chris Grosser"),
        QuoteDto("Hãy sống mỗi ngày như thể đó là một cuộc phiêu lưu.", null),
        QuoteDto("Thành công không phải là đích đến, mà là hành trình.", null),
        QuoteDto("Hãy là nguồn cảm hứng cho chính mình.", null),
        QuoteDto("Niềm tin là bước đầu tiên dù bạn chưa thấy cả con đường.", "Martin Luther King Jr."),
        QuoteDto("Hãy bắt đầu từ nơi bạn đang đứng. Sử dụng những gì bạn có. Làm những gì bạn có thể.", "Arthur Ashe")
    )
    
    /**
     * Lấy quote ngẫu nhiên dựa trên seed (thường là ngày)
     */
    fun getQuoteForSeed(seed: Int): QuoteDto {
        val random = Random(seed)
        return quotes[random.nextInt(quotes.size)]
    }
    
    /**
     * Lấy quote theo index
     */
    fun getQuoteByIndex(index: Int): QuoteDto {
        return quotes[index % quotes.size]
    }
    
    /**
     * Tổng số quotes
     */
    val totalQuotes: Int get() = quotes.size
}
