package com.example.data

data class BookMetadata(
    val index: Int, // 1 to 66
    val englishName: String,
    val urduName: String,
    val section: BibleSection,
    val chaptersCount: Int
)

enum class BibleSection(val displayNameUrdu: String, val displayNameEnglish: String) {
    TAWREET("توریت (Tawreet)", "Tawreet"),
    ZABOOR("زبور و کتب (Zaboor)", "Zaboor / Writings"),
    ANBIYA("انبیا (Anbiya)", "Anbiya / Prophets"),
    INJEEL("انجیل (Injeel)", "Injeel / New Testament")
}

object BibleData {
    val books = listOf(
        BookMetadata(1, "Genesis", "پیدائش", BibleSection.TAWREET, 50),
        BookMetadata(2, "Exodus", "خروج", BibleSection.TAWREET, 40),
        BookMetadata(3, "Leviticus", "احبار", BibleSection.TAWREET, 27),
        BookMetadata(4, "Numbers", "گنتی", BibleSection.TAWREET, 36),
        BookMetadata(5, "Deuteronomy", "استثنا", BibleSection.TAWREET, 34),
        
        BookMetadata(6, "Joshua", "یشوع", BibleSection.ZABOOR, 24),
        BookMetadata(7, "Judges", "قضات", BibleSection.ZABOOR, 21),
        BookMetadata(8, "Ruth", "روت", BibleSection.ZABOOR, 4),
        BookMetadata(9, "1 Samuel", "1 سموئیل", BibleSection.ZABOOR, 31),
        BookMetadata(10, "2 Samuel", "2 سموئیل", BibleSection.ZABOOR, 24),
        BookMetadata(11, "1 Kings", "1 سلاطین", BibleSection.ZABOOR, 22),
        BookMetadata(12, "2 Kings", "2 سلاطین", BibleSection.ZABOOR, 25),
        BookMetadata(13, "1 Chronicles", "1 تواریخ", BibleSection.ZABOOR, 29),
        BookMetadata(14, "2 Chronicles", "2 تواریخ", BibleSection.ZABOOR, 36),
        BookMetadata(15, "Ezra", "عزرا", BibleSection.ZABOOR, 10),
        BookMetadata(16, "Nehemiah", "نحمیاہ", BibleSection.ZABOOR, 13),
        BookMetadata(17, "Esther", "ایستر", BibleSection.ZABOOR, 10),
        BookMetadata(18, "Job", "ایوب", BibleSection.ZABOOR, 42),
        BookMetadata(19, "Psalms", "زبور", BibleSection.ZABOOR, 150),
        BookMetadata(20, "Proverbs", "امثال", BibleSection.ZABOOR, 31),
        BookMetadata(21, "Ecclesiastes", "واعظ", BibleSection.ZABOOR, 12),
        BookMetadata(22, "Song of Solomon", "غزل الغزلات", BibleSection.ZABOOR, 8),
        
        BookMetadata(23, "Isaiah", "یسعیاہ", BibleSection.ANBIYA, 66),
        BookMetadata(24, "Jeremiah", "یرمیاہ", BibleSection.ANBIYA, 52),
        BookMetadata(25, "Lamentations", "نوحہ", BibleSection.ANBIYA, 5),
        BookMetadata(26, "Ezekiel", "حزقیال", BibleSection.ANBIYA, 48),
        BookMetadata(27, "Daniel", "دانیال", BibleSection.ANBIYA, 12),
        BookMetadata(28, "Hosea", "ہوشع", BibleSection.ANBIYA, 14),
        BookMetadata(29, "Joel", "یوایل", BibleSection.ANBIYA, 3),
        BookMetadata(30, "Amos", "عاموس", BibleSection.ANBIYA, 9),
        BookMetadata(31, "Obadiah", "عبدیاہ", BibleSection.ANBIYA, 1),
        BookMetadata(32, "Jonah", "یونس", BibleSection.ANBIYA, 4),
        BookMetadata(33, "Micah", "میکاہ", BibleSection.ANBIYA, 7),
        BookMetadata(34, "Nahum", "ناحوم", BibleSection.ANBIYA, 3),
        BookMetadata(35, "Habakkuk", "حبقوق", BibleSection.ANBIYA, 3),
        BookMetadata(36, "Zephaniah", "صفنیاہ", BibleSection.ANBIYA, 3),
        BookMetadata(37, "Haggai", "حجی", BibleSection.ANBIYA, 2),
        BookMetadata(38, "Zechariah", "زکریا", BibleSection.ANBIYA, 14),
        BookMetadata(39, "Malachi", "ملاکی", BibleSection.ANBIYA, 4),
        
        BookMetadata(40, "Matthew", "متی", BibleSection.INJEEL, 28),
        BookMetadata(41, "Mark", "مرقس", BibleSection.INJEEL, 16),
        BookMetadata(42, "Luke", "لوقا", BibleSection.INJEEL, 24),
        BookMetadata(43, "John", "یوحنا", BibleSection.INJEEL, 21),
        BookMetadata(44, "Acts", "اعمال", BibleSection.INJEEL, 28),
        BookMetadata(45, "Romans", "رومیوں", BibleSection.INJEEL, 16),
        BookMetadata(46, "1 Corinthians", "1 کرنتھیوں", BibleSection.INJEEL, 16),
        BookMetadata(47, "2 Corinthians", "2 کرنتھیوں", BibleSection.INJEEL, 13),
        BookMetadata(48, "Galatians", "گلتیوں", BibleSection.INJEEL, 6),
        BookMetadata(49, "Ephesians", "افسیوں", BibleSection.INJEEL, 6),
        BookMetadata(50, "Philippians", "فلپیوں", BibleSection.INJEEL, 4),
        BookMetadata(51, "Colossians", "کلسیوں", BibleSection.INJEEL, 4),
        BookMetadata(52, "1 Thessalonians", "1 تھسلنیکیوں", BibleSection.INJEEL, 5),
        BookMetadata(53, "2 Thessalonians", "2 تھسلنیکیوں", BibleSection.INJEEL, 3),
        BookMetadata(54, "1 Timothy", "1 تیمتھیس", BibleSection.INJEEL, 6),
        BookMetadata(55, "2 Timothy", "2 تیمتھیس", BibleSection.INJEEL, 4),
        BookMetadata(56, "Titus", "ططس", BibleSection.INJEEL, 3),
        BookMetadata(57, "Philemon", "فلیمون", BibleSection.INJEEL, 1),
        BookMetadata(58, "Hebrews", "عبرانیوں", BibleSection.INJEEL, 13),
        BookMetadata(59, "James", "یعقوب", BibleSection.INJEEL, 5),
        BookMetadata(60, "1 Peter", "1 پطرس", BibleSection.INJEEL, 5),
        BookMetadata(61, "2 Peter", "2 پطرس", BibleSection.INJEEL, 3),
        BookMetadata(62, "1 John", "1 یوحنا", BibleSection.INJEEL, 5),
        BookMetadata(63, "2 John", "2 یوحنا", BibleSection.INJEEL, 1),
        BookMetadata(64, "3 John", "3 یوحنا", BibleSection.INJEEL, 1),
        BookMetadata(65, "Jude", "یہوداہ", BibleSection.INJEEL, 1),
        BookMetadata(66, "Revelation", "مکاشفہ", BibleSection.INJEEL, 22)
    )

    fun getBookByIndex(index: Int): BookMetadata? {
        return books.find { it.index == index }
    }

    fun getBookByEnglishName(name: String): BookMetadata? {
        return books.find { it.englishName.equals(name, ignoreCase = true) }
    }
}

enum class BibleMode { ENGLISH, URDU, BOTH }

// Active reading models for the UI
data class Verse(
    val number: Int,
    val textUrdu: String,
    val textEnglish: String
)
