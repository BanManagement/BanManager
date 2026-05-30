package me.confuser.banmanager.common.impl.service;

import me.confuser.banmanager.api.dto.Player;
import me.confuser.banmanager.common.data.PlayerData;

import java.util.UUID;
import java.util.concurrent.Executor;

/**
 * Shared fixtures for the {@code *ServiceImpl} contract tests. Keeps each
 * test focused on the behaviour under test rather than re-deriving the
 * shaded/unshaded IP and {@link PlayerData} plumbing in every file.
 */
final class ServiceTestFixtures {

  private ServiceTestFixtures() {}

  /**
   * Inline executor so {@code async(...)} work runs deterministically on the
   * calling thread — no thread-pool teardown to fight and no flaky
   * {@code get()} timeouts under CI load.
   */
  static Executor synchronousExecutor() {
    return Runnable::run;
  }

  /** Internal (shaded) {@link PlayerData} entity returned by mocked storage. */
  static PlayerData playerEntity(UUID uuid, String name) {
    try {
      return new PlayerData(uuid, name,
          new me.confuser.banmanager.common.ipaddr.IPAddressString("203.0.113.42").toAddress());
    } catch (me.confuser.banmanager.common.ipaddr.AddressStringException e) {
      throw new IllegalStateException("invalid fixture player IP", e);
    }
  }

  /** Public-API {@link Player} DTO used as the {@code actor} on unban/unmute calls. */
  static Player playerDto(UUID uuid, String name) {
    return new Player(uuid, name, apiIp("203.0.113.43"), 1_700_000_000L);
  }

  /** Unshaded public-API {@link inet.ipaddr.IPAddress} from a host literal. */
  static inet.ipaddr.IPAddress apiIp(String literal) {
    inet.ipaddr.IPAddress address = new inet.ipaddr.IPAddressString(literal).getAddress();
    if (address == null) {
      throw new IllegalArgumentException("invalid fixture IP: " + literal);
    }
    return address;
  }
}
