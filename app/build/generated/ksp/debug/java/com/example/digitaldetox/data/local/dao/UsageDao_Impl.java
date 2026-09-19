package com.example.digitaldetox.data.local.dao;

import android.database.Cursor;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.example.digitaldetox.data.local.database.DailyUsageSummaryEntity;
import com.example.digitaldetox.data.local.database.UsageSessionEntity;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class UsageDao_Impl implements UsageDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<UsageSessionEntity> __insertionAdapterOfUsageSessionEntity;

  private final EntityInsertionAdapter<DailyUsageSummaryEntity> __insertionAdapterOfDailyUsageSummaryEntity;

  public UsageDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfUsageSessionEntity = new EntityInsertionAdapter<UsageSessionEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `usage_sessions` (`id`,`packageName`,`startTime`,`endTime`,`durationMs`,`dateString`) VALUES (nullif(?, 0),?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final UsageSessionEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getPackageName());
        statement.bindLong(3, entity.getStartTime());
        statement.bindLong(4, entity.getEndTime());
        statement.bindLong(5, entity.getDurationMs());
        statement.bindString(6, entity.getDateString());
      }
    };
    this.__insertionAdapterOfDailyUsageSummaryEntity = new EntityInsertionAdapter<DailyUsageSummaryEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `daily_usage_summaries` (`dateString`,`totalUsageMs`,`detoxTimeMs`,`launchCount`,`interventionCount`,`successfulPausesCount`,`focusScore`) VALUES (?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final DailyUsageSummaryEntity entity) {
        statement.bindString(1, entity.getDateString());
        statement.bindLong(2, entity.getTotalUsageMs());
        statement.bindLong(3, entity.getDetoxTimeMs());
        statement.bindLong(4, entity.getLaunchCount());
        statement.bindLong(5, entity.getInterventionCount());
        statement.bindLong(6, entity.getSuccessfulPausesCount());
        statement.bindLong(7, entity.getFocusScore());
      }
    };
  }

  @Override
  public Object insertSession(final UsageSessionEntity session,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfUsageSessionEntity.insert(session);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object insertDailySummary(final DailyUsageSummaryEntity summary,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfDailyUsageSummaryEntity.insert(summary);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<UsageSessionEntity>> getSessionsForDate(final String dateString) {
    final String _sql = "SELECT * FROM usage_sessions WHERE dateString = ? ORDER BY startTime DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, dateString);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"usage_sessions"}, new Callable<List<UsageSessionEntity>>() {
      @Override
      @NonNull
      public List<UsageSessionEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfPackageName = CursorUtil.getColumnIndexOrThrow(_cursor, "packageName");
          final int _cursorIndexOfStartTime = CursorUtil.getColumnIndexOrThrow(_cursor, "startTime");
          final int _cursorIndexOfEndTime = CursorUtil.getColumnIndexOrThrow(_cursor, "endTime");
          final int _cursorIndexOfDurationMs = CursorUtil.getColumnIndexOrThrow(_cursor, "durationMs");
          final int _cursorIndexOfDateString = CursorUtil.getColumnIndexOrThrow(_cursor, "dateString");
          final List<UsageSessionEntity> _result = new ArrayList<UsageSessionEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final UsageSessionEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpPackageName;
            _tmpPackageName = _cursor.getString(_cursorIndexOfPackageName);
            final long _tmpStartTime;
            _tmpStartTime = _cursor.getLong(_cursorIndexOfStartTime);
            final long _tmpEndTime;
            _tmpEndTime = _cursor.getLong(_cursorIndexOfEndTime);
            final long _tmpDurationMs;
            _tmpDurationMs = _cursor.getLong(_cursorIndexOfDurationMs);
            final String _tmpDateString;
            _tmpDateString = _cursor.getString(_cursorIndexOfDateString);
            _item = new UsageSessionEntity(_tmpId,_tmpPackageName,_tmpStartTime,_tmpEndTime,_tmpDurationMs,_tmpDateString);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Flow<DailyUsageSummaryEntity> getDailySummary(final String dateString) {
    final String _sql = "SELECT * FROM daily_usage_summaries WHERE dateString = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, dateString);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"daily_usage_summaries"}, new Callable<DailyUsageSummaryEntity>() {
      @Override
      @Nullable
      public DailyUsageSummaryEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfDateString = CursorUtil.getColumnIndexOrThrow(_cursor, "dateString");
          final int _cursorIndexOfTotalUsageMs = CursorUtil.getColumnIndexOrThrow(_cursor, "totalUsageMs");
          final int _cursorIndexOfDetoxTimeMs = CursorUtil.getColumnIndexOrThrow(_cursor, "detoxTimeMs");
          final int _cursorIndexOfLaunchCount = CursorUtil.getColumnIndexOrThrow(_cursor, "launchCount");
          final int _cursorIndexOfInterventionCount = CursorUtil.getColumnIndexOrThrow(_cursor, "interventionCount");
          final int _cursorIndexOfSuccessfulPausesCount = CursorUtil.getColumnIndexOrThrow(_cursor, "successfulPausesCount");
          final int _cursorIndexOfFocusScore = CursorUtil.getColumnIndexOrThrow(_cursor, "focusScore");
          final DailyUsageSummaryEntity _result;
          if (_cursor.moveToFirst()) {
            final String _tmpDateString;
            _tmpDateString = _cursor.getString(_cursorIndexOfDateString);
            final long _tmpTotalUsageMs;
            _tmpTotalUsageMs = _cursor.getLong(_cursorIndexOfTotalUsageMs);
            final long _tmpDetoxTimeMs;
            _tmpDetoxTimeMs = _cursor.getLong(_cursorIndexOfDetoxTimeMs);
            final int _tmpLaunchCount;
            _tmpLaunchCount = _cursor.getInt(_cursorIndexOfLaunchCount);
            final int _tmpInterventionCount;
            _tmpInterventionCount = _cursor.getInt(_cursorIndexOfInterventionCount);
            final int _tmpSuccessfulPausesCount;
            _tmpSuccessfulPausesCount = _cursor.getInt(_cursorIndexOfSuccessfulPausesCount);
            final int _tmpFocusScore;
            _tmpFocusScore = _cursor.getInt(_cursorIndexOfFocusScore);
            _result = new DailyUsageSummaryEntity(_tmpDateString,_tmpTotalUsageMs,_tmpDetoxTimeMs,_tmpLaunchCount,_tmpInterventionCount,_tmpSuccessfulPausesCount,_tmpFocusScore);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Flow<List<DailyUsageSummaryEntity>> getRecentSummaries(final int limit) {
    final String _sql = "SELECT * FROM daily_usage_summaries ORDER BY dateString DESC LIMIT ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, limit);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"daily_usage_summaries"}, new Callable<List<DailyUsageSummaryEntity>>() {
      @Override
      @NonNull
      public List<DailyUsageSummaryEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfDateString = CursorUtil.getColumnIndexOrThrow(_cursor, "dateString");
          final int _cursorIndexOfTotalUsageMs = CursorUtil.getColumnIndexOrThrow(_cursor, "totalUsageMs");
          final int _cursorIndexOfDetoxTimeMs = CursorUtil.getColumnIndexOrThrow(_cursor, "detoxTimeMs");
          final int _cursorIndexOfLaunchCount = CursorUtil.getColumnIndexOrThrow(_cursor, "launchCount");
          final int _cursorIndexOfInterventionCount = CursorUtil.getColumnIndexOrThrow(_cursor, "interventionCount");
          final int _cursorIndexOfSuccessfulPausesCount = CursorUtil.getColumnIndexOrThrow(_cursor, "successfulPausesCount");
          final int _cursorIndexOfFocusScore = CursorUtil.getColumnIndexOrThrow(_cursor, "focusScore");
          final List<DailyUsageSummaryEntity> _result = new ArrayList<DailyUsageSummaryEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final DailyUsageSummaryEntity _item;
            final String _tmpDateString;
            _tmpDateString = _cursor.getString(_cursorIndexOfDateString);
            final long _tmpTotalUsageMs;
            _tmpTotalUsageMs = _cursor.getLong(_cursorIndexOfTotalUsageMs);
            final long _tmpDetoxTimeMs;
            _tmpDetoxTimeMs = _cursor.getLong(_cursorIndexOfDetoxTimeMs);
            final int _tmpLaunchCount;
            _tmpLaunchCount = _cursor.getInt(_cursorIndexOfLaunchCount);
            final int _tmpInterventionCount;
            _tmpInterventionCount = _cursor.getInt(_cursorIndexOfInterventionCount);
            final int _tmpSuccessfulPausesCount;
            _tmpSuccessfulPausesCount = _cursor.getInt(_cursorIndexOfSuccessfulPausesCount);
            final int _tmpFocusScore;
            _tmpFocusScore = _cursor.getInt(_cursorIndexOfFocusScore);
            _item = new DailyUsageSummaryEntity(_tmpDateString,_tmpTotalUsageMs,_tmpDetoxTimeMs,_tmpLaunchCount,_tmpInterventionCount,_tmpSuccessfulPausesCount,_tmpFocusScore);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
