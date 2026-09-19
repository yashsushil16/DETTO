package com.example.digitaldetox.data.local.dao;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.example.digitaldetox.data.local.database.TrackedAppEntity;
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
public final class TrackedAppDao_Impl implements TrackedAppDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<TrackedAppEntity> __insertionAdapterOfTrackedAppEntity;

  private final EntityDeletionOrUpdateAdapter<TrackedAppEntity> __deletionAdapterOfTrackedAppEntity;

  private final EntityDeletionOrUpdateAdapter<TrackedAppEntity> __updateAdapterOfTrackedAppEntity;

  public TrackedAppDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfTrackedAppEntity = new EntityInsertionAdapter<TrackedAppEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `tracked_apps` (`packageName`,`displayName`,`isMonitored`,`isRestricted`,`dailyLimitMs`,`addedAt`) VALUES (?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final TrackedAppEntity entity) {
        statement.bindString(1, entity.getPackageName());
        statement.bindString(2, entity.getDisplayName());
        final int _tmp = entity.isMonitored() ? 1 : 0;
        statement.bindLong(3, _tmp);
        final int _tmp_1 = entity.isRestricted() ? 1 : 0;
        statement.bindLong(4, _tmp_1);
        statement.bindLong(5, entity.getDailyLimitMs());
        statement.bindLong(6, entity.getAddedAt());
      }
    };
    this.__deletionAdapterOfTrackedAppEntity = new EntityDeletionOrUpdateAdapter<TrackedAppEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `tracked_apps` WHERE `packageName` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final TrackedAppEntity entity) {
        statement.bindString(1, entity.getPackageName());
      }
    };
    this.__updateAdapterOfTrackedAppEntity = new EntityDeletionOrUpdateAdapter<TrackedAppEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `tracked_apps` SET `packageName` = ?,`displayName` = ?,`isMonitored` = ?,`isRestricted` = ?,`dailyLimitMs` = ?,`addedAt` = ? WHERE `packageName` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final TrackedAppEntity entity) {
        statement.bindString(1, entity.getPackageName());
        statement.bindString(2, entity.getDisplayName());
        final int _tmp = entity.isMonitored() ? 1 : 0;
        statement.bindLong(3, _tmp);
        final int _tmp_1 = entity.isRestricted() ? 1 : 0;
        statement.bindLong(4, _tmp_1);
        statement.bindLong(5, entity.getDailyLimitMs());
        statement.bindLong(6, entity.getAddedAt());
        statement.bindString(7, entity.getPackageName());
      }
    };
  }

  @Override
  public Object insertApp(final TrackedAppEntity app,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfTrackedAppEntity.insert(app);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteApp(final TrackedAppEntity app,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __deletionAdapterOfTrackedAppEntity.handle(app);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object updateApp(final TrackedAppEntity app,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfTrackedAppEntity.handle(app);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<TrackedAppEntity>> getAllTrackedApps() {
    final String _sql = "SELECT * FROM tracked_apps ORDER BY displayName ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"tracked_apps"}, new Callable<List<TrackedAppEntity>>() {
      @Override
      @NonNull
      public List<TrackedAppEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfPackageName = CursorUtil.getColumnIndexOrThrow(_cursor, "packageName");
          final int _cursorIndexOfDisplayName = CursorUtil.getColumnIndexOrThrow(_cursor, "displayName");
          final int _cursorIndexOfIsMonitored = CursorUtil.getColumnIndexOrThrow(_cursor, "isMonitored");
          final int _cursorIndexOfIsRestricted = CursorUtil.getColumnIndexOrThrow(_cursor, "isRestricted");
          final int _cursorIndexOfDailyLimitMs = CursorUtil.getColumnIndexOrThrow(_cursor, "dailyLimitMs");
          final int _cursorIndexOfAddedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "addedAt");
          final List<TrackedAppEntity> _result = new ArrayList<TrackedAppEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final TrackedAppEntity _item;
            final String _tmpPackageName;
            _tmpPackageName = _cursor.getString(_cursorIndexOfPackageName);
            final String _tmpDisplayName;
            _tmpDisplayName = _cursor.getString(_cursorIndexOfDisplayName);
            final boolean _tmpIsMonitored;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsMonitored);
            _tmpIsMonitored = _tmp != 0;
            final boolean _tmpIsRestricted;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsRestricted);
            _tmpIsRestricted = _tmp_1 != 0;
            final long _tmpDailyLimitMs;
            _tmpDailyLimitMs = _cursor.getLong(_cursorIndexOfDailyLimitMs);
            final long _tmpAddedAt;
            _tmpAddedAt = _cursor.getLong(_cursorIndexOfAddedAt);
            _item = new TrackedAppEntity(_tmpPackageName,_tmpDisplayName,_tmpIsMonitored,_tmpIsRestricted,_tmpDailyLimitMs,_tmpAddedAt);
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
  public Flow<List<TrackedAppEntity>> getMonitoredApps() {
    final String _sql = "SELECT * FROM tracked_apps WHERE isMonitored = 1 ORDER BY displayName ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"tracked_apps"}, new Callable<List<TrackedAppEntity>>() {
      @Override
      @NonNull
      public List<TrackedAppEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfPackageName = CursorUtil.getColumnIndexOrThrow(_cursor, "packageName");
          final int _cursorIndexOfDisplayName = CursorUtil.getColumnIndexOrThrow(_cursor, "displayName");
          final int _cursorIndexOfIsMonitored = CursorUtil.getColumnIndexOrThrow(_cursor, "isMonitored");
          final int _cursorIndexOfIsRestricted = CursorUtil.getColumnIndexOrThrow(_cursor, "isRestricted");
          final int _cursorIndexOfDailyLimitMs = CursorUtil.getColumnIndexOrThrow(_cursor, "dailyLimitMs");
          final int _cursorIndexOfAddedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "addedAt");
          final List<TrackedAppEntity> _result = new ArrayList<TrackedAppEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final TrackedAppEntity _item;
            final String _tmpPackageName;
            _tmpPackageName = _cursor.getString(_cursorIndexOfPackageName);
            final String _tmpDisplayName;
            _tmpDisplayName = _cursor.getString(_cursorIndexOfDisplayName);
            final boolean _tmpIsMonitored;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsMonitored);
            _tmpIsMonitored = _tmp != 0;
            final boolean _tmpIsRestricted;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsRestricted);
            _tmpIsRestricted = _tmp_1 != 0;
            final long _tmpDailyLimitMs;
            _tmpDailyLimitMs = _cursor.getLong(_cursorIndexOfDailyLimitMs);
            final long _tmpAddedAt;
            _tmpAddedAt = _cursor.getLong(_cursorIndexOfAddedAt);
            _item = new TrackedAppEntity(_tmpPackageName,_tmpDisplayName,_tmpIsMonitored,_tmpIsRestricted,_tmpDailyLimitMs,_tmpAddedAt);
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
  public Flow<List<TrackedAppEntity>> getRestrictedApps() {
    final String _sql = "SELECT * FROM tracked_apps WHERE isRestricted = 1 ORDER BY displayName ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"tracked_apps"}, new Callable<List<TrackedAppEntity>>() {
      @Override
      @NonNull
      public List<TrackedAppEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfPackageName = CursorUtil.getColumnIndexOrThrow(_cursor, "packageName");
          final int _cursorIndexOfDisplayName = CursorUtil.getColumnIndexOrThrow(_cursor, "displayName");
          final int _cursorIndexOfIsMonitored = CursorUtil.getColumnIndexOrThrow(_cursor, "isMonitored");
          final int _cursorIndexOfIsRestricted = CursorUtil.getColumnIndexOrThrow(_cursor, "isRestricted");
          final int _cursorIndexOfDailyLimitMs = CursorUtil.getColumnIndexOrThrow(_cursor, "dailyLimitMs");
          final int _cursorIndexOfAddedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "addedAt");
          final List<TrackedAppEntity> _result = new ArrayList<TrackedAppEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final TrackedAppEntity _item;
            final String _tmpPackageName;
            _tmpPackageName = _cursor.getString(_cursorIndexOfPackageName);
            final String _tmpDisplayName;
            _tmpDisplayName = _cursor.getString(_cursorIndexOfDisplayName);
            final boolean _tmpIsMonitored;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsMonitored);
            _tmpIsMonitored = _tmp != 0;
            final boolean _tmpIsRestricted;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsRestricted);
            _tmpIsRestricted = _tmp_1 != 0;
            final long _tmpDailyLimitMs;
            _tmpDailyLimitMs = _cursor.getLong(_cursorIndexOfDailyLimitMs);
            final long _tmpAddedAt;
            _tmpAddedAt = _cursor.getLong(_cursorIndexOfAddedAt);
            _item = new TrackedAppEntity(_tmpPackageName,_tmpDisplayName,_tmpIsMonitored,_tmpIsRestricted,_tmpDailyLimitMs,_tmpAddedAt);
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
  public Object getApp(final String packageName,
      final Continuation<? super TrackedAppEntity> $completion) {
    final String _sql = "SELECT * FROM tracked_apps WHERE packageName = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, packageName);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<TrackedAppEntity>() {
      @Override
      @Nullable
      public TrackedAppEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfPackageName = CursorUtil.getColumnIndexOrThrow(_cursor, "packageName");
          final int _cursorIndexOfDisplayName = CursorUtil.getColumnIndexOrThrow(_cursor, "displayName");
          final int _cursorIndexOfIsMonitored = CursorUtil.getColumnIndexOrThrow(_cursor, "isMonitored");
          final int _cursorIndexOfIsRestricted = CursorUtil.getColumnIndexOrThrow(_cursor, "isRestricted");
          final int _cursorIndexOfDailyLimitMs = CursorUtil.getColumnIndexOrThrow(_cursor, "dailyLimitMs");
          final int _cursorIndexOfAddedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "addedAt");
          final TrackedAppEntity _result;
          if (_cursor.moveToFirst()) {
            final String _tmpPackageName;
            _tmpPackageName = _cursor.getString(_cursorIndexOfPackageName);
            final String _tmpDisplayName;
            _tmpDisplayName = _cursor.getString(_cursorIndexOfDisplayName);
            final boolean _tmpIsMonitored;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsMonitored);
            _tmpIsMonitored = _tmp != 0;
            final boolean _tmpIsRestricted;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsRestricted);
            _tmpIsRestricted = _tmp_1 != 0;
            final long _tmpDailyLimitMs;
            _tmpDailyLimitMs = _cursor.getLong(_cursorIndexOfDailyLimitMs);
            final long _tmpAddedAt;
            _tmpAddedAt = _cursor.getLong(_cursorIndexOfAddedAt);
            _result = new TrackedAppEntity(_tmpPackageName,_tmpDisplayName,_tmpIsMonitored,_tmpIsRestricted,_tmpDailyLimitMs,_tmpAddedAt);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
