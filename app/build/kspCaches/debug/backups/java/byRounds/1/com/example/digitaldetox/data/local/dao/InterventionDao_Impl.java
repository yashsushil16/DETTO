package com.example.digitaldetox.data.local.dao;

import android.database.Cursor;
import androidx.annotation.NonNull;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.example.digitaldetox.data.local.database.InterventionEventEntity;
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
public final class InterventionDao_Impl implements InterventionDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<InterventionEventEntity> __insertionAdapterOfInterventionEventEntity;

  public InterventionDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfInterventionEventEntity = new EntityInsertionAdapter<InterventionEventEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `intervention_events` (`id`,`packageName`,`timestamp`,`reason`,`actionTaken`,`breathingCompleted`,`gameCompleted`,`continuedToApp`) VALUES (nullif(?, 0),?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final InterventionEventEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getPackageName());
        statement.bindLong(3, entity.getTimestamp());
        if (entity.getReason() == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.getReason());
        }
        if (entity.getActionTaken() == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.getActionTaken());
        }
        final int _tmp = entity.getBreathingCompleted() ? 1 : 0;
        statement.bindLong(6, _tmp);
        final int _tmp_1 = entity.getGameCompleted() ? 1 : 0;
        statement.bindLong(7, _tmp_1);
        final int _tmp_2 = entity.getContinuedToApp() ? 1 : 0;
        statement.bindLong(8, _tmp_2);
      }
    };
  }

  @Override
  public Object insertEvent(final InterventionEventEntity event,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfInterventionEventEntity.insert(event);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<InterventionEventEntity>> getRecentEvents(final int limit) {
    final String _sql = "SELECT * FROM intervention_events ORDER BY timestamp DESC LIMIT ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, limit);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"intervention_events"}, new Callable<List<InterventionEventEntity>>() {
      @Override
      @NonNull
      public List<InterventionEventEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfPackageName = CursorUtil.getColumnIndexOrThrow(_cursor, "packageName");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final int _cursorIndexOfReason = CursorUtil.getColumnIndexOrThrow(_cursor, "reason");
          final int _cursorIndexOfActionTaken = CursorUtil.getColumnIndexOrThrow(_cursor, "actionTaken");
          final int _cursorIndexOfBreathingCompleted = CursorUtil.getColumnIndexOrThrow(_cursor, "breathingCompleted");
          final int _cursorIndexOfGameCompleted = CursorUtil.getColumnIndexOrThrow(_cursor, "gameCompleted");
          final int _cursorIndexOfContinuedToApp = CursorUtil.getColumnIndexOrThrow(_cursor, "continuedToApp");
          final List<InterventionEventEntity> _result = new ArrayList<InterventionEventEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final InterventionEventEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpPackageName;
            _tmpPackageName = _cursor.getString(_cursorIndexOfPackageName);
            final long _tmpTimestamp;
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
            final String _tmpReason;
            if (_cursor.isNull(_cursorIndexOfReason)) {
              _tmpReason = null;
            } else {
              _tmpReason = _cursor.getString(_cursorIndexOfReason);
            }
            final String _tmpActionTaken;
            if (_cursor.isNull(_cursorIndexOfActionTaken)) {
              _tmpActionTaken = null;
            } else {
              _tmpActionTaken = _cursor.getString(_cursorIndexOfActionTaken);
            }
            final boolean _tmpBreathingCompleted;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfBreathingCompleted);
            _tmpBreathingCompleted = _tmp != 0;
            final boolean _tmpGameCompleted;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfGameCompleted);
            _tmpGameCompleted = _tmp_1 != 0;
            final boolean _tmpContinuedToApp;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfContinuedToApp);
            _tmpContinuedToApp = _tmp_2 != 0;
            _item = new InterventionEventEntity(_tmpId,_tmpPackageName,_tmpTimestamp,_tmpReason,_tmpActionTaken,_tmpBreathingCompleted,_tmpGameCompleted,_tmpContinuedToApp);
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
