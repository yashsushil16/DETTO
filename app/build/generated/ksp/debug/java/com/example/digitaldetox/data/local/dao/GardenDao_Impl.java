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
import com.example.digitaldetox.data.local.database.GardenStateEntity;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class GardenDao_Impl implements GardenDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<GardenStateEntity> __insertionAdapterOfGardenStateEntity;

  public GardenDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfGardenStateEntity = new EntityInsertionAdapter<GardenStateEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `garden_state` (`id`,`currentStage`,`focusPoints`,`totalDetoxDays`,`currentStreak`,`highestStreak`,`lastUpdatedTimestamp`) VALUES (?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final GardenStateEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getCurrentStage());
        statement.bindLong(3, entity.getFocusPoints());
        statement.bindLong(4, entity.getTotalDetoxDays());
        statement.bindLong(5, entity.getCurrentStreak());
        statement.bindLong(6, entity.getHighestStreak());
        statement.bindLong(7, entity.getLastUpdatedTimestamp());
      }
    };
  }

  @Override
  public Object insertOrUpdateGardenState(final GardenStateEntity state,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfGardenStateEntity.insert(state);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<GardenStateEntity> getGardenState() {
    final String _sql = "SELECT * FROM garden_state WHERE id = 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"garden_state"}, new Callable<GardenStateEntity>() {
      @Override
      @Nullable
      public GardenStateEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfCurrentStage = CursorUtil.getColumnIndexOrThrow(_cursor, "currentStage");
          final int _cursorIndexOfFocusPoints = CursorUtil.getColumnIndexOrThrow(_cursor, "focusPoints");
          final int _cursorIndexOfTotalDetoxDays = CursorUtil.getColumnIndexOrThrow(_cursor, "totalDetoxDays");
          final int _cursorIndexOfCurrentStreak = CursorUtil.getColumnIndexOrThrow(_cursor, "currentStreak");
          final int _cursorIndexOfHighestStreak = CursorUtil.getColumnIndexOrThrow(_cursor, "highestStreak");
          final int _cursorIndexOfLastUpdatedTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "lastUpdatedTimestamp");
          final GardenStateEntity _result;
          if (_cursor.moveToFirst()) {
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            final String _tmpCurrentStage;
            _tmpCurrentStage = _cursor.getString(_cursorIndexOfCurrentStage);
            final int _tmpFocusPoints;
            _tmpFocusPoints = _cursor.getInt(_cursorIndexOfFocusPoints);
            final int _tmpTotalDetoxDays;
            _tmpTotalDetoxDays = _cursor.getInt(_cursorIndexOfTotalDetoxDays);
            final int _tmpCurrentStreak;
            _tmpCurrentStreak = _cursor.getInt(_cursorIndexOfCurrentStreak);
            final int _tmpHighestStreak;
            _tmpHighestStreak = _cursor.getInt(_cursorIndexOfHighestStreak);
            final long _tmpLastUpdatedTimestamp;
            _tmpLastUpdatedTimestamp = _cursor.getLong(_cursorIndexOfLastUpdatedTimestamp);
            _result = new GardenStateEntity(_tmpId,_tmpCurrentStage,_tmpFocusPoints,_tmpTotalDetoxDays,_tmpCurrentStreak,_tmpHighestStreak,_tmpLastUpdatedTimestamp);
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

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
