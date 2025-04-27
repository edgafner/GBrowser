package com.github.gbrowser.services

import kotlinx.serialization.ExperimentalSerializationApi
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.util.*

@OptIn(ExperimentalSerializationApi::class)
class DateAsStringSerializerTest {

    @Test
    fun `test date format pattern`() {
        val dateFormat = DateAsStringSerializer.dateFormat
        Assertions.assertEquals("yyyy-MM-dd'T'HH:mm:ss'Z'", dateFormat.toPattern())
    }

    @Test
    fun `test date format with known date`() {
        val calendar = Calendar.getInstance()
        calendar.set(2023, Calendar.JANUARY, 15, 10, 30, 45)
        calendar.set(Calendar.MILLISECOND, 0)
        val date = calendar.time

        val formattedDate = DateAsStringSerializer.dateFormat.format(date)
        Assertions.assertEquals("2023-01-15T10:30:45Z", formattedDate)
    }

    @Test
    fun `test date parsing with known string`() {
        val dateString = "2023-01-15T10:30:45Z"
        val parsedDate = DateAsStringSerializer.dateFormat.parse(dateString)

        val calendar = Calendar.getInstance()
        calendar.time = parsedDate

        Assertions.assertEquals(2023, calendar.get(Calendar.YEAR))
        Assertions.assertEquals(Calendar.JANUARY, calendar.get(Calendar.MONTH))
        Assertions.assertEquals(15, calendar.get(Calendar.DAY_OF_MONTH))
        Assertions.assertEquals(10, calendar.get(Calendar.HOUR_OF_DAY))
        Assertions.assertEquals(30, calendar.get(Calendar.MINUTE))
        Assertions.assertEquals(45, calendar.get(Calendar.SECOND))
    }

    @Test
    fun `test descriptor property`() {
        val descriptor = DateAsStringSerializer.descriptor

        Assertions.assertEquals("Date", descriptor.serialName)
    }
}
