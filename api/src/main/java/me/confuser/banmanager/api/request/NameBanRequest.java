package me.confuser.banmanager.api.request;

import java.util.Objects;
import java.util.UUID;

/**
 * Mutable request describing a name ban to create.
 */
public final class NameBanRequest {

  private String name;
  private UUID actor;
  private String reason = "";
  private long expires;
  private boolean silent;

  public NameBanRequest() {}

  public NameBanRequest(String name, UUID actor, String reason) {
    this.name = Objects.requireNonNull(name, "name");
    this.actor = Objects.requireNonNull(actor, "actor");
    this.reason = Objects.requireNonNull(reason, "reason");
  }

  public String name() { return name; }
  public NameBanRequest name(String name) { this.name = name; return this; }

  public UUID actor() { return actor; }
  public NameBanRequest actor(UUID actor) { this.actor = actor; return this; }

  public String reason() { return reason; }
  public NameBanRequest reason(String reason) { this.reason = reason; return this; }

  public long expires() { return expires; }
  public NameBanRequest expires(long expires) { this.expires = expires; return this; }

  public boolean silent() { return silent; }
  public NameBanRequest silent(boolean silent) { this.silent = silent; return this; }
}
