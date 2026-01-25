package com.projectapp.tempus.data.schedule.dto

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

// ===================== RepeatType =====================
object RepeatTypeSerializer : KSerializer<RepeatType> {
    override val descriptor: SerialDescriptor = 
        PrimitiveSerialDescriptor("RepeatType", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: RepeatType) {
        encoder.encodeString(value.name)
    }

    override fun deserialize(decoder: Decoder): RepeatType {
        val stringValue = decoder.decodeString()
        return RepeatType.entries.firstOrNull { 
            it.name.equals(stringValue, ignoreCase = true) 
        } ?: RepeatType.once // fallback to 'once' if unknown
    }
}

@Serializable(with = RepeatTypeSerializer::class)
enum class RepeatType {
    @SerialName("once") once,
    @SerialName("daily") daily,
    @SerialName("weekly") weekly,
    @SerialName("monthly") monthly,
    @SerialName("custom") custom // Lặp theo các thứ tùy chọn (dùng với repeat_days)
}

// ===================== StatusType =====================
object StatusTypeSerializer : KSerializer<StatusType> {
    override val descriptor: SerialDescriptor = 
        PrimitiveSerialDescriptor("StatusType", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: StatusType) {
        encoder.encodeString(value.name)
    }

    override fun deserialize(decoder: Decoder): StatusType {
        val stringValue = decoder.decodeString()
        return StatusType.entries.firstOrNull { 
            it.name.equals(stringValue, ignoreCase = true) 
        } ?: StatusType.planned // fallback to 'planned' if unknown
    }
}

@Serializable(with = StatusTypeSerializer::class)
enum class StatusType {
    @SerialName("planned") planned,
    @SerialName("done") done,
    @SerialName("delete") delete
}

// ===================== SourceType =====================
object SourceTypeSerializer : KSerializer<SourceType> {
    override val descriptor: SerialDescriptor = 
        PrimitiveSerialDescriptor("SourceType", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: SourceType) {
        encoder.encodeString(value.name)
    }

    override fun deserialize(decoder: Decoder): SourceType {
        val stringValue = decoder.decodeString()
        return SourceType.entries.firstOrNull { 
            it.name.equals(stringValue, ignoreCase = true) 
        } ?: SourceType.manual // fallback to 'manual' if unknown
    }
}

@Serializable(with = SourceTypeSerializer::class)
enum class SourceType {
    @SerialName("manual") manual,
    @SerialName("ai") ai
}

// ===================== ScheduleLabel =====================
/**
 * Custom serializer cho ScheduleLabel để xử lý các giá trị không xác định từ database
 * Thay vì crash với JSON parse error, nó sẽ trả về UNKNOWN
 */
object ScheduleLabelSerializer : KSerializer<ScheduleLabel> {
    override val descriptor: SerialDescriptor = 
        PrimitiveSerialDescriptor("ScheduleLabel", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: ScheduleLabel) {
        encoder.encodeString(value.name)
    }

    override fun deserialize(decoder: Decoder): ScheduleLabel {
        val stringValue = decoder.decodeString()
        return ScheduleLabel.entries.firstOrNull { 
            it.name.equals(stringValue, ignoreCase = true) 
        } ?: ScheduleLabel.UNKNOWN
    }
}

@Serializable(with = ScheduleLabelSerializer::class)
enum class ScheduleLabel {
    @SerialName("wakeup") wakeup,
    @SerialName("eat") eat,
    @SerialName("exercise") exercise,
    @SerialName("rest") rest,
    @SerialName("water") water,
    @SerialName("book") book,
    @SerialName("sleep") sleep,
    @SerialName("clean") clean,
    @SerialName("cook") cook,
    @SerialName("garden") garden,
    @SerialName("unknown") UNKNOWN;

    companion object {
        fun fromDb(v: String?): ScheduleLabel {
            if (v.isNullOrBlank()) return book
            return entries.firstOrNull { it.name == v } ?: UNKNOWN
        }
    }
}
