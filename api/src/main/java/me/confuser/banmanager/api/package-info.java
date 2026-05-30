/**
 * BanManager v8 public API.
 *
 * <p>This module ships the only contract plugin authors should integrate
 * against. Internals — the {@code me.confuser.banmanager.common.*}
 * implementation, the ORMLite entities, the platform plugins — make no
 * stability guarantees and may change at any minor release.</p>
 *
 * <h2>Resolution</h2>
 * <pre>{@code
 * BanManagerService bm = me.confuser.banmanager.api.BanManager.get();
 * }</pre>
 * or via the platform's native service manager. See
 * {@link me.confuser.banmanager.api.BanManagerService} for examples.
 *
 * <h2>Mutation model</h2>
 * <p>All write paths use mutable {@link me.confuser.banmanager.api.request
 * Request} objects. Pre-events carry the same {@code Request} instance so
 * handlers can modify reason, expires, silent flags, etc. before the
 * database write. Post-events carry immutable record DTOs.</p>
 *
 * <h2>Async</h2>
 * <p>Every method that touches the database returns
 * {@link java.util.concurrent.CompletableFuture}; a {@code *Sync} sibling is
 * provided for callers that are already off the main thread.</p>
 *
 * <p>For storage failures, sync methods throw
 * {@link me.confuser.banmanager.api.exception.BanManagerException}
 * (unchecked) — typically the
 * {@link me.confuser.banmanager.api.exception.StorageException} subtype —
 * and async methods complete the future exceptionally with the same type.
 * Cancellation is <strong>not</strong> an exception: when a pre-event
 * handler cancels an operation, both surfaces signal it by returning
 * {@link java.util.Optional#empty()} (create-style operations) or
 * {@code false} (delete-style operations) rather than throwing.</p>
 */
package me.confuser.banmanager.api;
