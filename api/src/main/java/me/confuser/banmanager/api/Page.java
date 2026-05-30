package me.confuser.banmanager.api;

import java.util.List;
import java.util.Objects;

/**
 * Immutable page of results. Replaces every {@code CloseableIterator<T>}
 * return on the public API surface so consumers never have to remember to
 * {@code close()}.
 *
 * @param items the items in this page (never {@code null})
 * @param page the zero-indexed page number this {@code Page} represents
 *             (must be {@code >= 0})
 * @param size the requested page size (must be {@code > 0}); always reflects
 *             what the caller asked for, never the number of items actually
 *             returned
 * @param total the total number of records matching the query, or {@code -1}
 *              when the storage layer cannot cheaply compute it
 * @param <T> element type
 */
public record Page<T>(List<T> items, int page, int size, long total) {

  public Page {
    Objects.requireNonNull(items, "items");
    if (page < 0) throw new IllegalArgumentException("page must be >= 0");
    if (size <= 0) throw new IllegalArgumentException("size must be > 0");
    items = List.copyOf(items);
  }

  /**
   * @return {@code true} if a {@link #page() page+1} call may yield more results
   */
  public boolean hasMore() {
    if (total < 0) {
      return items.size() == size;
    }
    return ((long) (page + 1) * size) < total;
  }

  /**
   * Empty page that preserves the caller's pagination request. Use when a
   * lookup precondition is not satisfied (e.g. unknown player) — the
   * {@code page}/{@code size} round-trip lets clients render the correct
   * pager UI without re-asking.
   *
   * @param page zero-indexed page number that was requested
   * @param size page size that was requested ({@code > 0})
   */
  public static <T> Page<T> empty(int page, int size) {
    return new Page<>(List.of(), page, size, 0L);
  }
}
