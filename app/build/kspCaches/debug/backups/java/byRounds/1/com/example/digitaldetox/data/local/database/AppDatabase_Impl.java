package com.example.digitaldetox.data.local.database;

import androidx.annotation.NonNull;
import androidx.room.DatabaseConfiguration;
import androidx.room.InvalidationTracker;
import androidx.room.RoomDatabase;
import androidx.room.RoomOpenHelper;
import androidx.room.migration.AutoMigrationSpec;
import androidx.room.migration.Migration;
import androidx.room.util.DBUtil;
import androidx.room.util.TableInfo;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.SupportSQLiteOpenHelper;
import com.example.digitaldetox.data.local.dao.GardenDao;
import com.example.digitaldetox.data.local.dao.GardenDao_Impl;
import com.example.digitaldetox.data.local.dao.InterventionDao;
import com.example.digitaldetox.data.local.dao.InterventionDao_Impl;
import com.example.digitaldetox.data.local.dao.ScheduleDao;
import com.example.digitaldetox.data.local.dao.ScheduleDao_Impl;
import com.example.digitaldetox.data.local.dao.TrackedAppDao;
import com.example.digitaldetox.data.local.dao.TrackedAppDao_Impl;
import com.example.digitaldetox.data.local.dao.UsageDao;
import com.example.digitaldetox.data.local.dao.UsageDao_Impl;
import java.lang.Class;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class AppDatabase_Impl extends AppDatabase {
  private volatile TrackedAppDao _trackedAppDao;

  private volatile UsageDao _usageDao;

  private volatile InterventionDao _interventionDao;

  private volatile GardenDao _gardenDao;

  private volatile ScheduleDao _scheduleDao;

  @Override
  @NonNull
  protected SupportSQLiteOpenHelper createOpenHelper(@NonNull final DatabaseConfiguration config) {
    final SupportSQLiteOpenHelper.Callback _openCallback = new RoomOpenHelper(config, new RoomOpenHelper.Delegate(2) {
      @Override
      public void createAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `tracked_apps` (`packageName` TEXT NOT NULL, `displayName` TEXT NOT NULL, `isMonitored` INTEGER NOT NULL, `isRestricted` INTEGER NOT NULL, `dailyLimitMs` INTEGER NOT NULL, `addedAt` INTEGER NOT NULL, PRIMARY KEY(`packageName`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `restriction_rules` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `packageName` TEXT NOT NULL, `strictnessLevel` TEXT NOT NULL, `frictionSeconds` INTEGER NOT NULL, `isEnabled` INTEGER NOT NULL)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `schedules` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `startTimeMinutes` INTEGER NOT NULL, `endTimeMinutes` INTEGER NOT NULL, `daysOfWeekBitmask` INTEGER NOT NULL, `isEnabled` INTEGER NOT NULL)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `usage_sessions` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `packageName` TEXT NOT NULL, `startTime` INTEGER NOT NULL, `endTime` INTEGER NOT NULL, `durationMs` INTEGER NOT NULL, `dateString` TEXT NOT NULL)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `daily_usage_summaries` (`dateString` TEXT NOT NULL, `totalUsageMs` INTEGER NOT NULL, `detoxTimeMs` INTEGER NOT NULL, `launchCount` INTEGER NOT NULL, `interventionCount` INTEGER NOT NULL, `successfulPausesCount` INTEGER NOT NULL, `focusScore` INTEGER NOT NULL, PRIMARY KEY(`dateString`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `intervention_events` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `packageName` TEXT NOT NULL, `timestamp` INTEGER NOT NULL, `reason` TEXT, `actionTaken` TEXT, `breathingCompleted` INTEGER NOT NULL, `gameCompleted` INTEGER NOT NULL, `continuedToApp` INTEGER NOT NULL)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `garden_state` (`id` INTEGER NOT NULL, `currentStage` TEXT NOT NULL, `focusPoints` INTEGER NOT NULL, `totalDetoxDays` INTEGER NOT NULL, `currentStreak` INTEGER NOT NULL, `highestStreak` INTEGER NOT NULL, `lastUpdatedTimestamp` INTEGER NOT NULL, PRIMARY KEY(`id`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)");
        db.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, '4d5084fc2ebe61c75b93d023375ff6fa')");
      }

      @Override
      public void dropAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS `tracked_apps`");
        db.execSQL("DROP TABLE IF EXISTS `restriction_rules`");
        db.execSQL("DROP TABLE IF EXISTS `schedules`");
        db.execSQL("DROP TABLE IF EXISTS `usage_sessions`");
        db.execSQL("DROP TABLE IF EXISTS `daily_usage_summaries`");
        db.execSQL("DROP TABLE IF EXISTS `intervention_events`");
        db.execSQL("DROP TABLE IF EXISTS `garden_state`");
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onDestructiveMigration(db);
          }
        }
      }

      @Override
      public void onCreate(@NonNull final SupportSQLiteDatabase db) {
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onCreate(db);
          }
        }
      }

      @Override
      public void onOpen(@NonNull final SupportSQLiteDatabase db) {
        mDatabase = db;
        internalInitInvalidationTracker(db);
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onOpen(db);
          }
        }
      }

      @Override
      public void onPreMigrate(@NonNull final SupportSQLiteDatabase db) {
        DBUtil.dropFtsSyncTriggers(db);
      }

      @Override
      public void onPostMigrate(@NonNull final SupportSQLiteDatabase db) {
      }

      @Override
      @NonNull
      public RoomOpenHelper.ValidationResult onValidateSchema(
          @NonNull final SupportSQLiteDatabase db) {
        final HashMap<String, TableInfo.Column> _columnsTrackedApps = new HashMap<String, TableInfo.Column>(6);
        _columnsTrackedApps.put("packageName", new TableInfo.Column("packageName", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTrackedApps.put("displayName", new TableInfo.Column("displayName", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTrackedApps.put("isMonitored", new TableInfo.Column("isMonitored", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTrackedApps.put("isRestricted", new TableInfo.Column("isRestricted", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTrackedApps.put("dailyLimitMs", new TableInfo.Column("dailyLimitMs", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTrackedApps.put("addedAt", new TableInfo.Column("addedAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysTrackedApps = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesTrackedApps = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoTrackedApps = new TableInfo("tracked_apps", _columnsTrackedApps, _foreignKeysTrackedApps, _indicesTrackedApps);
        final TableInfo _existingTrackedApps = TableInfo.read(db, "tracked_apps");
        if (!_infoTrackedApps.equals(_existingTrackedApps)) {
          return new RoomOpenHelper.ValidationResult(false, "tracked_apps(com.example.digitaldetox.data.local.database.TrackedAppEntity).\n"
                  + " Expected:\n" + _infoTrackedApps + "\n"
                  + " Found:\n" + _existingTrackedApps);
        }
        final HashMap<String, TableInfo.Column> _columnsRestrictionRules = new HashMap<String, TableInfo.Column>(5);
        _columnsRestrictionRules.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsRestrictionRules.put("packageName", new TableInfo.Column("packageName", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsRestrictionRules.put("strictnessLevel", new TableInfo.Column("strictnessLevel", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsRestrictionRules.put("frictionSeconds", new TableInfo.Column("frictionSeconds", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsRestrictionRules.put("isEnabled", new TableInfo.Column("isEnabled", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysRestrictionRules = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesRestrictionRules = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoRestrictionRules = new TableInfo("restriction_rules", _columnsRestrictionRules, _foreignKeysRestrictionRules, _indicesRestrictionRules);
        final TableInfo _existingRestrictionRules = TableInfo.read(db, "restriction_rules");
        if (!_infoRestrictionRules.equals(_existingRestrictionRules)) {
          return new RoomOpenHelper.ValidationResult(false, "restriction_rules(com.example.digitaldetox.data.local.database.RestrictionRuleEntity).\n"
                  + " Expected:\n" + _infoRestrictionRules + "\n"
                  + " Found:\n" + _existingRestrictionRules);
        }
        final HashMap<String, TableInfo.Column> _columnsSchedules = new HashMap<String, TableInfo.Column>(6);
        _columnsSchedules.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSchedules.put("name", new TableInfo.Column("name", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSchedules.put("startTimeMinutes", new TableInfo.Column("startTimeMinutes", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSchedules.put("endTimeMinutes", new TableInfo.Column("endTimeMinutes", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSchedules.put("daysOfWeekBitmask", new TableInfo.Column("daysOfWeekBitmask", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSchedules.put("isEnabled", new TableInfo.Column("isEnabled", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysSchedules = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesSchedules = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoSchedules = new TableInfo("schedules", _columnsSchedules, _foreignKeysSchedules, _indicesSchedules);
        final TableInfo _existingSchedules = TableInfo.read(db, "schedules");
        if (!_infoSchedules.equals(_existingSchedules)) {
          return new RoomOpenHelper.ValidationResult(false, "schedules(com.example.digitaldetox.data.local.database.ScheduleEntity).\n"
                  + " Expected:\n" + _infoSchedules + "\n"
                  + " Found:\n" + _existingSchedules);
        }
        final HashMap<String, TableInfo.Column> _columnsUsageSessions = new HashMap<String, TableInfo.Column>(6);
        _columnsUsageSessions.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUsageSessions.put("packageName", new TableInfo.Column("packageName", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUsageSessions.put("startTime", new TableInfo.Column("startTime", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUsageSessions.put("endTime", new TableInfo.Column("endTime", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUsageSessions.put("durationMs", new TableInfo.Column("durationMs", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUsageSessions.put("dateString", new TableInfo.Column("dateString", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysUsageSessions = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesUsageSessions = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoUsageSessions = new TableInfo("usage_sessions", _columnsUsageSessions, _foreignKeysUsageSessions, _indicesUsageSessions);
        final TableInfo _existingUsageSessions = TableInfo.read(db, "usage_sessions");
        if (!_infoUsageSessions.equals(_existingUsageSessions)) {
          return new RoomOpenHelper.ValidationResult(false, "usage_sessions(com.example.digitaldetox.data.local.database.UsageSessionEntity).\n"
                  + " Expected:\n" + _infoUsageSessions + "\n"
                  + " Found:\n" + _existingUsageSessions);
        }
        final HashMap<String, TableInfo.Column> _columnsDailyUsageSummaries = new HashMap<String, TableInfo.Column>(7);
        _columnsDailyUsageSummaries.put("dateString", new TableInfo.Column("dateString", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDailyUsageSummaries.put("totalUsageMs", new TableInfo.Column("totalUsageMs", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDailyUsageSummaries.put("detoxTimeMs", new TableInfo.Column("detoxTimeMs", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDailyUsageSummaries.put("launchCount", new TableInfo.Column("launchCount", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDailyUsageSummaries.put("interventionCount", new TableInfo.Column("interventionCount", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDailyUsageSummaries.put("successfulPausesCount", new TableInfo.Column("successfulPausesCount", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDailyUsageSummaries.put("focusScore", new TableInfo.Column("focusScore", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysDailyUsageSummaries = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesDailyUsageSummaries = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoDailyUsageSummaries = new TableInfo("daily_usage_summaries", _columnsDailyUsageSummaries, _foreignKeysDailyUsageSummaries, _indicesDailyUsageSummaries);
        final TableInfo _existingDailyUsageSummaries = TableInfo.read(db, "daily_usage_summaries");
        if (!_infoDailyUsageSummaries.equals(_existingDailyUsageSummaries)) {
          return new RoomOpenHelper.ValidationResult(false, "daily_usage_summaries(com.example.digitaldetox.data.local.database.DailyUsageSummaryEntity).\n"
                  + " Expected:\n" + _infoDailyUsageSummaries + "\n"
                  + " Found:\n" + _existingDailyUsageSummaries);
        }
        final HashMap<String, TableInfo.Column> _columnsInterventionEvents = new HashMap<String, TableInfo.Column>(8);
        _columnsInterventionEvents.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsInterventionEvents.put("packageName", new TableInfo.Column("packageName", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsInterventionEvents.put("timestamp", new TableInfo.Column("timestamp", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsInterventionEvents.put("reason", new TableInfo.Column("reason", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsInterventionEvents.put("actionTaken", new TableInfo.Column("actionTaken", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsInterventionEvents.put("breathingCompleted", new TableInfo.Column("breathingCompleted", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsInterventionEvents.put("gameCompleted", new TableInfo.Column("gameCompleted", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsInterventionEvents.put("continuedToApp", new TableInfo.Column("continuedToApp", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysInterventionEvents = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesInterventionEvents = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoInterventionEvents = new TableInfo("intervention_events", _columnsInterventionEvents, _foreignKeysInterventionEvents, _indicesInterventionEvents);
        final TableInfo _existingInterventionEvents = TableInfo.read(db, "intervention_events");
        if (!_infoInterventionEvents.equals(_existingInterventionEvents)) {
          return new RoomOpenHelper.ValidationResult(false, "intervention_events(com.example.digitaldetox.data.local.database.InterventionEventEntity).\n"
                  + " Expected:\n" + _infoInterventionEvents + "\n"
                  + " Found:\n" + _existingInterventionEvents);
        }
        final HashMap<String, TableInfo.Column> _columnsGardenState = new HashMap<String, TableInfo.Column>(7);
        _columnsGardenState.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsGardenState.put("currentStage", new TableInfo.Column("currentStage", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsGardenState.put("focusPoints", new TableInfo.Column("focusPoints", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsGardenState.put("totalDetoxDays", new TableInfo.Column("totalDetoxDays", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsGardenState.put("currentStreak", new TableInfo.Column("currentStreak", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsGardenState.put("highestStreak", new TableInfo.Column("highestStreak", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsGardenState.put("lastUpdatedTimestamp", new TableInfo.Column("lastUpdatedTimestamp", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysGardenState = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesGardenState = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoGardenState = new TableInfo("garden_state", _columnsGardenState, _foreignKeysGardenState, _indicesGardenState);
        final TableInfo _existingGardenState = TableInfo.read(db, "garden_state");
        if (!_infoGardenState.equals(_existingGardenState)) {
          return new RoomOpenHelper.ValidationResult(false, "garden_state(com.example.digitaldetox.data.local.database.GardenStateEntity).\n"
                  + " Expected:\n" + _infoGardenState + "\n"
                  + " Found:\n" + _existingGardenState);
        }
        return new RoomOpenHelper.ValidationResult(true, null);
      }
    }, "4d5084fc2ebe61c75b93d023375ff6fa", "39883f14c1fe32d4407e9254174e9013");
    final SupportSQLiteOpenHelper.Configuration _sqliteConfig = SupportSQLiteOpenHelper.Configuration.builder(config.context).name(config.name).callback(_openCallback).build();
    final SupportSQLiteOpenHelper _helper = config.sqliteOpenHelperFactory.create(_sqliteConfig);
    return _helper;
  }

  @Override
  @NonNull
  protected InvalidationTracker createInvalidationTracker() {
    final HashMap<String, String> _shadowTablesMap = new HashMap<String, String>(0);
    final HashMap<String, Set<String>> _viewTables = new HashMap<String, Set<String>>(0);
    return new InvalidationTracker(this, _shadowTablesMap, _viewTables, "tracked_apps","restriction_rules","schedules","usage_sessions","daily_usage_summaries","intervention_events","garden_state");
  }

  @Override
  public void clearAllTables() {
    super.assertNotMainThread();
    final SupportSQLiteDatabase _db = super.getOpenHelper().getWritableDatabase();
    try {
      super.beginTransaction();
      _db.execSQL("DELETE FROM `tracked_apps`");
      _db.execSQL("DELETE FROM `restriction_rules`");
      _db.execSQL("DELETE FROM `schedules`");
      _db.execSQL("DELETE FROM `usage_sessions`");
      _db.execSQL("DELETE FROM `daily_usage_summaries`");
      _db.execSQL("DELETE FROM `intervention_events`");
      _db.execSQL("DELETE FROM `garden_state`");
      super.setTransactionSuccessful();
    } finally {
      super.endTransaction();
      _db.query("PRAGMA wal_checkpoint(FULL)").close();
      if (!_db.inTransaction()) {
        _db.execSQL("VACUUM");
      }
    }
  }

  @Override
  @NonNull
  protected Map<Class<?>, List<Class<?>>> getRequiredTypeConverters() {
    final HashMap<Class<?>, List<Class<?>>> _typeConvertersMap = new HashMap<Class<?>, List<Class<?>>>();
    _typeConvertersMap.put(TrackedAppDao.class, TrackedAppDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(UsageDao.class, UsageDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(InterventionDao.class, InterventionDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(GardenDao.class, GardenDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(ScheduleDao.class, ScheduleDao_Impl.getRequiredConverters());
    return _typeConvertersMap;
  }

  @Override
  @NonNull
  public Set<Class<? extends AutoMigrationSpec>> getRequiredAutoMigrationSpecs() {
    final HashSet<Class<? extends AutoMigrationSpec>> _autoMigrationSpecsSet = new HashSet<Class<? extends AutoMigrationSpec>>();
    return _autoMigrationSpecsSet;
  }

  @Override
  @NonNull
  public List<Migration> getAutoMigrations(
      @NonNull final Map<Class<? extends AutoMigrationSpec>, AutoMigrationSpec> autoMigrationSpecs) {
    final List<Migration> _autoMigrations = new ArrayList<Migration>();
    return _autoMigrations;
  }

  @Override
  public TrackedAppDao trackedAppDao() {
    if (_trackedAppDao != null) {
      return _trackedAppDao;
    } else {
      synchronized(this) {
        if(_trackedAppDao == null) {
          _trackedAppDao = new TrackedAppDao_Impl(this);
        }
        return _trackedAppDao;
      }
    }
  }

  @Override
  public UsageDao usageDao() {
    if (_usageDao != null) {
      return _usageDao;
    } else {
      synchronized(this) {
        if(_usageDao == null) {
          _usageDao = new UsageDao_Impl(this);
        }
        return _usageDao;
      }
    }
  }

  @Override
  public InterventionDao interventionDao() {
    if (_interventionDao != null) {
      return _interventionDao;
    } else {
      synchronized(this) {
        if(_interventionDao == null) {
          _interventionDao = new InterventionDao_Impl(this);
        }
        return _interventionDao;
      }
    }
  }

  @Override
  public GardenDao gardenDao() {
    if (_gardenDao != null) {
      return _gardenDao;
    } else {
      synchronized(this) {
        if(_gardenDao == null) {
          _gardenDao = new GardenDao_Impl(this);
        }
        return _gardenDao;
      }
    }
  }

  @Override
  public ScheduleDao scheduleDao() {
    if (_scheduleDao != null) {
      return _scheduleDao;
    } else {
      synchronized(this) {
        if(_scheduleDao == null) {
          _scheduleDao = new ScheduleDao_Impl(this);
        }
        return _scheduleDao;
      }
    }
  }
}
