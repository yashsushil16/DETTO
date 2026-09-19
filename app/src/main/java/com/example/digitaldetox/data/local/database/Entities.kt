package com.example.digitaldetox.data.local.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tracked_apps")
data class TrackedAppEntity(
    @PrimaryKey val packageName: String,
    val displayName: String,
    val isMonitored: Boolean = false,
    val isRestricted: Boolean = false,
    val dailyLimitMs: Long = 0L, // 0 = use global limit
    val addedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "restriction_rules")
data class RestrictionRuleEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val packageName: String,
    val strictnessLevel: String, // Gentle, Mindful, Strong
    val frictionSeconds: Int = 0,
    val isEnabled: Boolean = true
)

@Entity(tableName = "schedules")
data class ScheduleEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val startTimeMinutes: Int, // Minutes from midnight
    val endTimeMinutes: Int,
    val daysOfWeekBitmask: Int, // 1 for Sunday, 2 for Monday... (bitmask)
    val isEnabled: Boolean = true
)

@Entity(tableName = "usage_sessions")
data class UsageSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val packageName: String,
    val startTime: Long,
    val endTime: Long,
    val durationMs: Long,
    val dateString: String // YYYY-MM-DD
)

@Entity(tableName = "daily_usage_summaries")
data class DailyUsageSummaryEntity(
    @PrimaryKey val dateString: String,
    val totalUsageMs: Long = 0,
    val detoxTimeMs: Long = 0,
    val launchCount: Int = 0,
    val interventionCount: Int = 0,
    val successfulPausesCount: Int = 0,
    val focusScore: Int = 0
)

@Entity(tableName = "intervention_events")
data class InterventionEventEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val packageName: String,
    val timestamp: Long,
    val reason: String?,
    val actionTaken: String?, // breathing, game, back, continue
    val breathingCompleted: Boolean = false,
    val gameCompleted: Boolean = false,
    val continuedToApp: Boolean = false
)

@Entity(tableName = "garden_state")
data class GardenStateEntity(
    @PrimaryKey val id: Int = 1, // Single row
    val currentStage: String = "Seed", // Seed, Sprout, Plant, Tree, Garden, Forest
    val focusPoints: Int = 0,
    val totalDetoxDays: Int = 0,
    val currentStreak: Int = 0,
    val highestStreak: Int = 0,
    val lastUpdatedTimestamp: Long = System.currentTimeMillis()
)
