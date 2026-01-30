package com.projectapp.tempus.data.quote

import com.projectapp.tempus.data.quote.dto.QuoteDto
import com.projectapp.tempus.data.user.UserProfileCache
import kotlin.random.Random

/**
 * Cung cấp danh sách quotes local để fallback khi API thất bại
 */
object LocalQuotesProvider {
    
    private val quotesVi = listOf(
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
    
    private val quotesEn = listOf(
        QuoteDto("The journey of a thousand miles begins with one step.", "Lao Tzu"),
        QuoteDto("Success is the sum of small efforts, repeated day in and day out.", "Robert Collier"),
        QuoteDto("Do not wait. The time will never be just right.", "Napoleon Hill"),
        QuoteDto("The best way to predict the future is to create it.", "Peter Drucker"),
        QuoteDto("Every day is a new opportunity to change your life.", null),
        QuoteDto("Believe in yourself and all that you are.", null),
        QuoteDto("Difficulties cannot stop you, only giving up can.", null),
        QuoteDto("Persistence is the key to all success.", null),
        QuoteDto("Make each day your masterpiece.", "John Wooden"),
        QuoteDto("Focus on the journey, not just the destination.", null),
        QuoteDto("You are braver than you believe, stronger than you seem, and smarter than you think.", "A.A. Milne"),
        QuoteDto("Be the best version of yourself.", null),
        QuoteDto("Every small step counts.", null),
        QuoteDto("Today is a gift, cherish it.", null),
        QuoteDto("Failure is simply the opportunity to begin again, this time more intelligently.", "Henry Ford"),
        QuoteDto("Don't compare yourself to others, compare yourself to the person you were yesterday.", null),
        QuoteDto("Everything you want is on the other side of fear.", "Jack Canfield"),
        QuoteDto("Do what you can, with what you have, where you are.", "Theodore Roosevelt"),
        QuoteDto("The future belongs to those who believe in the beauty of their dreams.", "Eleanor Roosevelt"),
        QuoteDto("Change begins with a decision.", null),
        QuoteDto("Learn to rest, not to quit.", "Banksy"),
        QuoteDto("Every new day is a blank page.", null),
        QuoteDto("Don't let yesterday take up too much of today.", "Will Rogers"),
        QuoteDto("It does not matter how many times you fall, but how many times you rise.", null),
        QuoteDto("Opportunities don't happen, you create them.", "Chris Grosser"),
        QuoteDto("Live every day as if it were an adventure.", null),
        QuoteDto("Success is not a destination, but a journey.", null),
        QuoteDto("Be your own inspiration.", null),
        QuoteDto("Faith is taking the first step even when you don't see the whole staircase.", "Martin Luther King Jr."),
        QuoteDto("Start where you are. Use what you have. Do what you can.", "Arthur Ashe")
    )
    
    private fun getQuotes(languageCode: String): List<QuoteDto> {
        return if (languageCode == "en") quotesEn else quotesVi
    }
    
    /**
     * Lấy quote ngẫu nhiên dựa trên seed (thường là ngày)
     */
    fun getQuoteForSeed(seed: Int, languageCode: String): QuoteDto {
        val list = getQuotes(languageCode)
        val random = Random(seed)
        return list[random.nextInt(list.size)]
    }
    
    /**
     * Lấy quote theo index
     */
    fun getQuoteByIndex(index: Int, languageCode: String): QuoteDto {
        val list = getQuotes(languageCode)
        return list[index % list.size]
    }
    
    /**
     * Tổng số quotes
     */
    fun getTotalQuotes(languageCode: String): Int = getQuotes(languageCode).size
}
