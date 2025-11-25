package com.example.kemono.data.local;

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
  private volatile FavoriteDao _favoriteDao;

  private volatile CacheDao _cacheDao;

  private volatile DownloadDao _downloadDao;

  @Override
  @NonNull
  protected SupportSQLiteOpenHelper createOpenHelper(@NonNull final DatabaseConfiguration config) {
    final SupportSQLiteOpenHelper.Callback _openCallback = new RoomOpenHelper(config, new RoomOpenHelper.Delegate(3) {
      @Override
      public void createAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `favorite_creators` (`id` TEXT NOT NULL, `service` TEXT NOT NULL, `name` TEXT NOT NULL, `updated` TEXT NOT NULL, PRIMARY KEY(`id`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `cached_creators` (`id` TEXT NOT NULL, `service` TEXT NOT NULL, `name` TEXT NOT NULL, `updated` INTEGER NOT NULL, `indexed` INTEGER NOT NULL, `cachedAt` INTEGER NOT NULL, PRIMARY KEY(`id`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `cached_posts` (`id` TEXT NOT NULL, `user` TEXT NOT NULL, `service` TEXT NOT NULL, `title` TEXT, `content` TEXT, `published` TEXT, `fileJson` TEXT, `attachmentsJson` TEXT, `cachedAt` INTEGER NOT NULL, PRIMARY KEY(`id`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `downloaded_items` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `postId` TEXT NOT NULL, `creatorId` TEXT NOT NULL, `fileName` TEXT NOT NULL, `filePath` TEXT NOT NULL, `mediaType` TEXT NOT NULL, `downloadedAt` INTEGER NOT NULL)");
        db.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)");
        db.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, 'e49436cbdafa941e325eadc2669740c3')");
      }

      @Override
      public void dropAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS `favorite_creators`");
        db.execSQL("DROP TABLE IF EXISTS `cached_creators`");
        db.execSQL("DROP TABLE IF EXISTS `cached_posts`");
        db.execSQL("DROP TABLE IF EXISTS `downloaded_items`");
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
        final HashMap<String, TableInfo.Column> _columnsFavoriteCreators = new HashMap<String, TableInfo.Column>(4);
        _columnsFavoriteCreators.put("id", new TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsFavoriteCreators.put("service", new TableInfo.Column("service", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsFavoriteCreators.put("name", new TableInfo.Column("name", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsFavoriteCreators.put("updated", new TableInfo.Column("updated", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysFavoriteCreators = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesFavoriteCreators = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoFavoriteCreators = new TableInfo("favorite_creators", _columnsFavoriteCreators, _foreignKeysFavoriteCreators, _indicesFavoriteCreators);
        final TableInfo _existingFavoriteCreators = TableInfo.read(db, "favorite_creators");
        if (!_infoFavoriteCreators.equals(_existingFavoriteCreators)) {
          return new RoomOpenHelper.ValidationResult(false, "favorite_creators(com.example.kemono.data.model.FavoriteCreator).\n"
                  + " Expected:\n" + _infoFavoriteCreators + "\n"
                  + " Found:\n" + _existingFavoriteCreators);
        }
        final HashMap<String, TableInfo.Column> _columnsCachedCreators = new HashMap<String, TableInfo.Column>(6);
        _columnsCachedCreators.put("id", new TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCachedCreators.put("service", new TableInfo.Column("service", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCachedCreators.put("name", new TableInfo.Column("name", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCachedCreators.put("updated", new TableInfo.Column("updated", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCachedCreators.put("indexed", new TableInfo.Column("indexed", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCachedCreators.put("cachedAt", new TableInfo.Column("cachedAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysCachedCreators = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesCachedCreators = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoCachedCreators = new TableInfo("cached_creators", _columnsCachedCreators, _foreignKeysCachedCreators, _indicesCachedCreators);
        final TableInfo _existingCachedCreators = TableInfo.read(db, "cached_creators");
        if (!_infoCachedCreators.equals(_existingCachedCreators)) {
          return new RoomOpenHelper.ValidationResult(false, "cached_creators(com.example.kemono.data.model.CachedCreator).\n"
                  + " Expected:\n" + _infoCachedCreators + "\n"
                  + " Found:\n" + _existingCachedCreators);
        }
        final HashMap<String, TableInfo.Column> _columnsCachedPosts = new HashMap<String, TableInfo.Column>(9);
        _columnsCachedPosts.put("id", new TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCachedPosts.put("user", new TableInfo.Column("user", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCachedPosts.put("service", new TableInfo.Column("service", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCachedPosts.put("title", new TableInfo.Column("title", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCachedPosts.put("content", new TableInfo.Column("content", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCachedPosts.put("published", new TableInfo.Column("published", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCachedPosts.put("fileJson", new TableInfo.Column("fileJson", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCachedPosts.put("attachmentsJson", new TableInfo.Column("attachmentsJson", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCachedPosts.put("cachedAt", new TableInfo.Column("cachedAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysCachedPosts = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesCachedPosts = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoCachedPosts = new TableInfo("cached_posts", _columnsCachedPosts, _foreignKeysCachedPosts, _indicesCachedPosts);
        final TableInfo _existingCachedPosts = TableInfo.read(db, "cached_posts");
        if (!_infoCachedPosts.equals(_existingCachedPosts)) {
          return new RoomOpenHelper.ValidationResult(false, "cached_posts(com.example.kemono.data.model.CachedPost).\n"
                  + " Expected:\n" + _infoCachedPosts + "\n"
                  + " Found:\n" + _existingCachedPosts);
        }
        final HashMap<String, TableInfo.Column> _columnsDownloadedItems = new HashMap<String, TableInfo.Column>(7);
        _columnsDownloadedItems.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDownloadedItems.put("postId", new TableInfo.Column("postId", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDownloadedItems.put("creatorId", new TableInfo.Column("creatorId", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDownloadedItems.put("fileName", new TableInfo.Column("fileName", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDownloadedItems.put("filePath", new TableInfo.Column("filePath", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDownloadedItems.put("mediaType", new TableInfo.Column("mediaType", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDownloadedItems.put("downloadedAt", new TableInfo.Column("downloadedAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysDownloadedItems = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesDownloadedItems = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoDownloadedItems = new TableInfo("downloaded_items", _columnsDownloadedItems, _foreignKeysDownloadedItems, _indicesDownloadedItems);
        final TableInfo _existingDownloadedItems = TableInfo.read(db, "downloaded_items");
        if (!_infoDownloadedItems.equals(_existingDownloadedItems)) {
          return new RoomOpenHelper.ValidationResult(false, "downloaded_items(com.example.kemono.data.model.DownloadedItem).\n"
                  + " Expected:\n" + _infoDownloadedItems + "\n"
                  + " Found:\n" + _existingDownloadedItems);
        }
        return new RoomOpenHelper.ValidationResult(true, null);
      }
    }, "e49436cbdafa941e325eadc2669740c3", "00c482139b0e8020def79436f41f9768");
    final SupportSQLiteOpenHelper.Configuration _sqliteConfig = SupportSQLiteOpenHelper.Configuration.builder(config.context).name(config.name).callback(_openCallback).build();
    final SupportSQLiteOpenHelper _helper = config.sqliteOpenHelperFactory.create(_sqliteConfig);
    return _helper;
  }

  @Override
  @NonNull
  protected InvalidationTracker createInvalidationTracker() {
    final HashMap<String, String> _shadowTablesMap = new HashMap<String, String>(0);
    final HashMap<String, Set<String>> _viewTables = new HashMap<String, Set<String>>(0);
    return new InvalidationTracker(this, _shadowTablesMap, _viewTables, "favorite_creators","cached_creators","cached_posts","downloaded_items");
  }

  @Override
  public void clearAllTables() {
    super.assertNotMainThread();
    final SupportSQLiteDatabase _db = super.getOpenHelper().getWritableDatabase();
    try {
      super.beginTransaction();
      _db.execSQL("DELETE FROM `favorite_creators`");
      _db.execSQL("DELETE FROM `cached_creators`");
      _db.execSQL("DELETE FROM `cached_posts`");
      _db.execSQL("DELETE FROM `downloaded_items`");
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
    _typeConvertersMap.put(FavoriteDao.class, FavoriteDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(CacheDao.class, CacheDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(DownloadDao.class, DownloadDao_Impl.getRequiredConverters());
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
  public FavoriteDao favoriteDao() {
    if (_favoriteDao != null) {
      return _favoriteDao;
    } else {
      synchronized(this) {
        if(_favoriteDao == null) {
          _favoriteDao = new FavoriteDao_Impl(this);
        }
        return _favoriteDao;
      }
    }
  }

  @Override
  public CacheDao cacheDao() {
    if (_cacheDao != null) {
      return _cacheDao;
    } else {
      synchronized(this) {
        if(_cacheDao == null) {
          _cacheDao = new CacheDao_Impl(this);
        }
        return _cacheDao;
      }
    }
  }

  @Override
  public DownloadDao downloadDao() {
    if (_downloadDao != null) {
      return _downloadDao;
    } else {
      synchronized(this) {
        if(_downloadDao == null) {
          _downloadDao = new DownloadDao_Impl(this);
        }
        return _downloadDao;
      }
    }
  }
}
