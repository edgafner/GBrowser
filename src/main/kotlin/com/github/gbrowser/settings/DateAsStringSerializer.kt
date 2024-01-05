package com.github.gbrowser.settings

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.text.SimpleDateFormat
import java.util.*

object DateAsStringSerializer : KSerializer<Date> {
  private val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")

  override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Date", PrimitiveKind.STRING)

  override fun serialize(encoder: Encoder, value: Date) {
    encoder.encodeString(dateFormat.format(value))
  }

  override fun deserialize(decoder: Decoder): Date {
    return dateFormat.parse(decoder.decodeString())
  }
}