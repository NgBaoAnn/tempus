package com.projectapp.tempus.data.schedule

import io.github.jan.supabase.postgrest.from
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.JsonNull
import com.projectapp.tempus.core.supabase.SupabaseClientProvider
import com.projectapp.tempus.data.schedule.dto.*
import io.github.jan.supabase.postgrest.query.Returning
class SupabaseScheduleRepository : ScheduleRepository {
    private val supabase = SupabaseClientProvider.client

    override suspend fun getAllSchedules(userId: String): List<ScheduleRow> {
        return supabase.from("schedule")
            .select { filter { eq("user_id", userId) } }
            .decodeList()
    }

    override suspend fun getScheduleItemsByDate(date: String): List<ScheduleItemRow> {
        return supabase.from("schedule_items")
            .select { filter { eq("date", date) } }
            .decodeList()
    }

    override suspend fun getEditedVersions(ids: List<String>): List<EditedVersionRow> {
        if (ids.isEmpty()) return emptyList()
        return supabase.from("edited_version")
            .select { filter { isIn("id", ids) } }
            .decodeList()
    }

    override suspend fun insertSchedule(row: Map<String, Any?>): ScheduleRow {
        // supabase-kt insert nhận JsonObject dễ nhất
        val body = buildJsonObject {
            row.forEach { (k, v) ->
                when (v) {
                    null -> put(k, JsonNull)
                    is String -> put(k, v)
                    is Number -> put(k, v)
                    is Boolean -> put(k, v)
                    else -> put(k, v.toString())
                }
            }
        }

        return supabase.from("schedule")
            .insert(body) { select() }
            .decodeSingle()
    }

    override suspend fun upsertScheduleItem(taskId: String, date: String, status: StatusType): ScheduleItemRow {
        // docs team: nếu chưa có dòng (task_id,date) -> insert; có rồi -> update
        // Cách đơn giản: query trước, rồi update/insert
        val existing = supabase.from("schedule_items")
            .select {
                filter {
                    eq("task_id", taskId)
                    eq("date", date)
                }
            }.decodeList<ScheduleItemRow>()

        return if (existing.isNotEmpty()) {
            val id = existing.first().id
            supabase.from("schedule_items")
                .update(
                    buildJsonObject {
                        put("status", status.name)
                        put("updated_at", "now()")
                    }
                ) { filter { eq("id", id) }
                    select()
                }
                .decodeSingle()
        } else {
            supabase.from("schedule_items")
                .insert(
                    buildJsonObject {
                        put("task_id", taskId)
                        put("date", date)
                        put("status", status.name)
                    }
                ) { select() }
                .decodeSingle()
        }
    }

    override suspend fun updateSchedule(taskId: String, fields: Map<String, Any?>): ScheduleRow {
        val body = buildJsonObject {
            fields.forEach { (k, v) ->
                when (v) {
                    null -> put(k, JsonNull)
                    is String -> put(k, v)
                    is Number -> put(k, v)
                    is Boolean -> put(k, v)
                    else -> put(k, v.toString())
                }
            }
        }

        return supabase.from("schedule")
            .update(body) { filter { eq("id", taskId) } }
            .decodeSingle()
    }

    override suspend fun insertEditedVersion(fields: Map<String, Any?>): EditedVersionRow {
        val body = buildJsonObject {
            fields.forEach { (k, v) ->
                when (v) {
                    null -> put(k, JsonNull)
                    is String -> put(k, v)
                    is Number -> put(k, v)
                    is Boolean -> put(k, v)
                    else -> put(k, v.toString())
                }
            }
        }

        return supabase.from("edited_version")
            .insert(body) { select() }
            .decodeSingle()
    }

    override suspend fun attachEditedVersionToDate(taskId: String, date: String, editedVersionId: String): ScheduleItemRow {
        val existing = supabase.from("schedule_items")
            .select { filter { eq("task_id", taskId); eq("date", date) } }
            .decodeList<ScheduleItemRow>()

        return if (existing.isNotEmpty()) {
            val id = existing.first().id
            supabase.from("schedule_items")
                .update(buildJsonObject { put("edited_version", editedVersionId) }) { filter { eq("id", id) } }
                .decodeSingle()
        } else {
            supabase.from("schedule_items")
                .insert(buildJsonObject {
                    put("task_id", taskId)
                    put("date", date)
                    put("status", "planned")
                    put("edited_version", editedVersionId)
                }) { select() }
                .decodeSingle()
        }
    }

    override suspend fun getScheduleById(id: String): ScheduleRow? {
        return supabase.from("schedule")
            .select { filter { eq("id", id) } }
            .decodeSingleOrNull()
    }

    override suspend fun deleteSchedule(id: String) {
        supabase.from("schedule").delete {
            filter { eq("id", id) }
        }
    }

}