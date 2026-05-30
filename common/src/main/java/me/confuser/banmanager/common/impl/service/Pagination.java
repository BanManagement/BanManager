package me.confuser.banmanager.common.impl.service;

import me.confuser.banmanager.api.Page;
import me.confuser.banmanager.common.data.PlayerData;
import me.confuser.banmanager.common.impl.AsyncSupport;
import me.confuser.banmanager.common.ormlite.dao.BaseDaoImpl;
import me.confuser.banmanager.common.ormlite.stmt.QueryBuilder;
import me.confuser.banmanager.common.storage.PlayerStorage;
import me.confuser.banmanager.common.util.UUIDUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

/**
 * Shared pagination helpers for service implementations. The historical
 * record DAOs all key on a {@code player_id} byte[] column, so the same
 * countOf + offset/limit pattern works for ban records, mute records, etc.
 *
 * <p>Page indices are zero-based on the API surface (matches Spring/JPA
 * conventions); the underlying ORMLite layer takes raw {@code offset/limit}
 * pairs.</p>
 */
final class Pagination {

  static final int MAX_PAGE_SIZE = 200;

  private Pagination() {}

  static <I, A> Page<A> recordsByPlayer(BaseDaoImpl<I, Integer> dao,
                                         PlayerStorage playerStorage,
                                         UUID player,
                                         int page,
                                         int size,
                                         Function<I, A> mapper) {
    if (page < 0) throw new IllegalArgumentException("page must be >= 0");
    if (size <= 0 || size > MAX_PAGE_SIZE) {
      throw new IllegalArgumentException("size must be in 1.." + MAX_PAGE_SIZE);
    }

    return AsyncSupport.sync(() -> {
      PlayerData target = playerStorage.queryForId(UUIDUtils.toBytes(player));
      if (target == null) {
        return Page.<A>empty(page, size);
      }

      QueryBuilder<I, Integer> count = dao.queryBuilder();
      count.where().eq("player_id", target);
      long total = count.countOf();

      QueryBuilder<I, Integer> q = dao.queryBuilder();
      q.where().eq("player_id", target);
      q.orderBy("created", false);
      q.offset((long) page * size);
      q.limit((long) size);

      List<I> rows = q.query();
      List<A> mapped = new ArrayList<>(rows.size());
      for (I row : rows) {
        mapped.add(mapper.apply(row));
      }

      return new Page<>(mapped, page, size, total);
    }, "Failed to page records for player " + player);
  }

  static <I, A> Page<A> recordsByColumn(BaseDaoImpl<I, Integer> dao,
                                         String column,
                                         Object value,
                                         int page,
                                         int size,
                                         Function<I, A> mapper) {
    if (page < 0) throw new IllegalArgumentException("page must be >= 0");
    if (size <= 0 || size > MAX_PAGE_SIZE) {
      throw new IllegalArgumentException("size must be in 1.." + MAX_PAGE_SIZE);
    }

    return AsyncSupport.sync(() -> {
      QueryBuilder<I, Integer> count = dao.queryBuilder();
      count.where().eq(column, value);
      long total = count.countOf();

      QueryBuilder<I, Integer> q = dao.queryBuilder();
      q.where().eq(column, value);
      q.orderBy("created", false);
      q.offset((long) page * size);
      q.limit((long) size);

      List<I> rows = q.query();
      List<A> mapped = new ArrayList<>(rows.size());
      for (I row : rows) {
        mapped.add(mapper.apply(row));
      }

      return new Page<>(mapped, page, size, total);
    }, "Failed to page records by " + column);
  }
}
