package me.confuser.banmanager.common.data;

import java.util.List;
import java.util.Map;

/**
 * Immutable description of a webhook to send for a punishment event.
 *
 * <p>The same shape is used both for raw, parsed webhooks loaded from
 * {@code webhooks.yml} (with placeholders such as {@code [player]} still in
 * the headers/payload) and for the resolved webhook that is dispatched over
 * HTTP. Treating both as the same value class avoids near-duplicate records
 * and keeps the placeholder substitution a pure transformation.</p>
 *
 * <p>{@code headers} are defensively copied at construction time so callers
 * may pass mutable maps without leaking them into the record's identity.</p>
 */
public record Webhook(String name, String url, String method,
                      Map<String, String> headers, String payload,
                      boolean ignoreSilent) {
  public Webhook {
    headers = headers == null ? Map.of() : Map.copyOf(headers);
    if (payload == null) payload = "";
  }

  /**
   * Returns a copy of this webhook with new {@code headers} and {@code payload}.
   * Use this when applying placeholder substitutions to a raw config webhook.
   */
  public Webhook withResolved(Map<String, String> resolvedHeaders, String resolvedPayload) {
    return new Webhook(name, url, method, resolvedHeaders, resolvedPayload, ignoreSilent);
  }

  /** True if this webhook has no body to send (GET/DELETE methods). */
  public boolean hasBody() {
    return !"GET".equals(method) && !"DELETE".equals(method);
  }

  /** Convenience for callers that only need a stable list-of-empty default. */
  public static List<Webhook> empty() {
    return List.of();
  }
}
