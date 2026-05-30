package me.confuser.banmanager.common.impl;

import me.confuser.banmanager.api.dto.HistoryEntry;
import me.confuser.banmanager.api.dto.IpBan;
import me.confuser.banmanager.api.dto.NameBan;
import me.confuser.banmanager.api.dto.Player;
import me.confuser.banmanager.api.dto.PlayerBan;
import me.confuser.banmanager.api.dto.PlayerMute;
import me.confuser.banmanager.api.dto.PlayerNameSummary;
import me.confuser.banmanager.api.dto.PlayerNote;
import me.confuser.banmanager.api.dto.PlayerSession;
import me.confuser.banmanager.api.dto.PlayerWarn;
import me.confuser.banmanager.common.data.IpBanData;
import me.confuser.banmanager.common.data.NameBanData;
import me.confuser.banmanager.common.data.PlayerBanData;
import me.confuser.banmanager.common.data.PlayerData;
import me.confuser.banmanager.common.data.PlayerHistoryData;
import me.confuser.banmanager.common.data.PlayerMuteData;
import me.confuser.banmanager.common.data.PlayerNoteData;
import me.confuser.banmanager.common.data.PlayerWarnData;
import me.confuser.banmanager.common.ipaddr.IPAddressString;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifies {@link EntityMappers} preserves all fields from internal entity
 * objects to API record DTOs, that {@code null} inputs propagate as {@code
 * null} (so callers can chain mappers safely), and that {@link Optional}
 * fields are populated correctly.
 */
public class EntityMappersTest {

  private static final UUID PLAYER_UUID = UUID.fromString("11111111-1111-1111-1111-111111111111");
  private static final UUID ACTOR_UUID = UUID.fromString("22222222-2222-2222-2222-222222222222");

  private static PlayerData newPlayerData(UUID uuid, String name, String ip) throws Exception {
    return new PlayerData(uuid, name, new IPAddressString(ip).toAddress(), 1_700_000_000L);
  }

  // -- player ---------------------------------------------------------------

  @Test
  public void playerMapsAllFields() throws Exception {
    PlayerData data = newPlayerData(PLAYER_UUID, "Alice", "203.0.113.42");

    Player mapped = EntityMappers.player(data);

    assertNotNull(mapped);
    assertEquals(PLAYER_UUID, mapped.uuid());
    assertEquals("Alice", mapped.name());
    assertEquals("203.0.113.42", mapped.ip().toCanonicalString());
    assertEquals(1_700_000_000L, mapped.lastSeen());
    assertEquals(Optional.empty(), mapped.locale(),
        "missing locale should map to Optional.empty()");
  }

  @Test
  public void playerLocalePreservesNonEmptyValue() throws Exception {
    PlayerData data = newPlayerData(PLAYER_UUID, "Alice", "203.0.113.42");
    data.setLocale("en_GB");

    Player mapped = EntityMappers.player(data);

    assertEquals(Optional.of("en_GB"), mapped.locale());
  }

  @Test
  public void playerLocaleCollapsesEmptyStringToEmptyOptional() throws Exception {
    PlayerData data = newPlayerData(PLAYER_UUID, "Alice", "203.0.113.42");
    data.setLocale("");

    Player mapped = EntityMappers.player(data);

    assertEquals(Optional.empty(), mapped.locale(),
        "empty-string locale must collapse to Optional.empty(), not Optional.of(\"\")");
  }

  @Test
  public void playerNullReturnsNull() {
    assertNull(EntityMappers.player(null));
  }

  // -- player ban ----------------------------------------------------------

  @Test
  public void playerBanMapsAllFields() throws Exception {
    PlayerData player = newPlayerData(PLAYER_UUID, "Alice", "203.0.113.42");
    PlayerData actor = newPlayerData(ACTOR_UUID, "ModBob", "203.0.113.43");
    PlayerBanData data = new PlayerBanData(player, actor, "spam", true, 9_000_000L);

    PlayerBan mapped = EntityMappers.playerBan(data);

    assertNotNull(mapped);
    assertEquals(PLAYER_UUID, mapped.player().uuid());
    assertEquals(ACTOR_UUID, mapped.actor().uuid());
    assertEquals("spam", mapped.reason());
    assertTrue(mapped.silent());
    assertEquals(9_000_000L, mapped.expires());
    assertFalse(mapped.isPermanent(), "non-zero expires should not be permanent");
  }

  @Test
  public void playerBanWithZeroExpiresIsPermanent() throws Exception {
    PlayerData player = newPlayerData(PLAYER_UUID, "Alice", "203.0.113.42");
    PlayerData actor = newPlayerData(ACTOR_UUID, "ModBob", "203.0.113.43");
    PlayerBanData data = new PlayerBanData(player, actor, "spam", false);

    PlayerBan mapped = EntityMappers.playerBan(data);

    assertTrue(mapped.isPermanent(), "zero expires should map to permanent=true");
    assertFalse(mapped.silent());
  }

  @Test
  public void playerBanNullReturnsNull() {
    assertNull(EntityMappers.playerBan(null));
  }

  // -- player mute ---------------------------------------------------------

  @Test
  public void playerMuteMapsAllFields() throws Exception {
    PlayerData player = newPlayerData(PLAYER_UUID, "Alice", "203.0.113.42");
    PlayerData actor = newPlayerData(ACTOR_UUID, "ModBob", "203.0.113.43");
    PlayerMuteData data = new PlayerMuteData(player, actor, "loud", false, true, 5_000L);

    PlayerMute mapped = EntityMappers.playerMute(data);

    assertNotNull(mapped);
    assertEquals(PLAYER_UUID, mapped.player().uuid());
    assertEquals(ACTOR_UUID, mapped.actor().uuid());
    assertEquals("loud", mapped.reason());
    assertFalse(mapped.silent());
    assertTrue(mapped.soft());
    assertEquals(5_000L, mapped.expires());
  }

  @Test
  public void playerMuteNullReturnsNull() {
    assertNull(EntityMappers.playerMute(null));
  }

  // -- player warn ---------------------------------------------------------

  @Test
  public void playerWarnMapsAllFields() throws Exception {
    PlayerData player = newPlayerData(PLAYER_UUID, "Alice", "203.0.113.42");
    PlayerData actor = newPlayerData(ACTOR_UUID, "ModBob", "203.0.113.43");
    PlayerWarnData data = new PlayerWarnData(player, actor, "rule break", 2.0d, false);

    PlayerWarn mapped = EntityMappers.playerWarn(data);

    assertNotNull(mapped);
    assertEquals(PLAYER_UUID, mapped.player().uuid());
    assertEquals("rule break", mapped.reason());
    assertEquals(2.0d, mapped.points(), 0.0001);
    assertFalse(mapped.read());
  }

  @Test
  public void playerWarnNullReturnsNull() {
    assertNull(EntityMappers.playerWarn(null));
  }

  // -- ip ban ---------------------------------------------------------------

  @Test
  public void ipBanMapsAddressAndFields() throws Exception {
    PlayerData actor = newPlayerData(ACTOR_UUID, "ModBob", "203.0.113.43");
    IpBanData data = new IpBanData(
        new IPAddressString("203.0.113.42").toAddress(),
        actor,
        "open proxy",
        true);

    IpBan mapped = EntityMappers.ipBan(data);

    assertNotNull(mapped);
    assertEquals("203.0.113.42", mapped.ip().toCanonicalString(),
        "internal IP should be remapped to API IPAddress");
    assertEquals(ACTOR_UUID, mapped.actor().uuid());
    assertEquals("open proxy", mapped.reason());
    assertTrue(mapped.silent());
  }

  @Test
  public void ipBanNullReturnsNull() {
    assertNull(EntityMappers.ipBan(null));
  }

  // -- name ban ------------------------------------------------------------

  @Test
  public void nameBanMapsAllFields() throws Exception {
    PlayerData actor = newPlayerData(ACTOR_UUID, "ModBob", "203.0.113.43");
    NameBanData data = new NameBanData("BadName", actor, "offensive", false);

    NameBan mapped = EntityMappers.nameBan(data);

    assertNotNull(mapped);
    assertEquals("BadName", mapped.name());
    assertEquals("offensive", mapped.reason());
    assertEquals(ACTOR_UUID, mapped.actor().uuid());
    assertFalse(mapped.silent());
  }

  @Test
  public void nameBanNullReturnsNull() {
    assertNull(EntityMappers.nameBan(null));
  }

  // -- note ----------------------------------------------------------------

  @Test
  public void playerNoteMapsAllFields() throws Exception {
    PlayerData player = newPlayerData(PLAYER_UUID, "Alice", "203.0.113.42");
    PlayerData actor = newPlayerData(ACTOR_UUID, "ModBob", "203.0.113.43");
    PlayerNoteData data = new PlayerNoteData(player, actor, "watching", 1_700_001_234L);

    PlayerNote mapped = EntityMappers.playerNote(data);

    assertNotNull(mapped);
    assertEquals(PLAYER_UUID, mapped.player().uuid());
    assertEquals(ACTOR_UUID, mapped.actor().uuid());
    assertEquals("watching", mapped.message());
    assertEquals(1_700_001_234L, mapped.created());
  }

  @Test
  public void playerNoteNullReturnsNull() {
    assertNull(EntityMappers.playerNote(null));
  }

  // -- session ------------------------------------------------------------

  @Test
  public void playerSessionMapsIpToOptionalAddress() throws Exception {
    PlayerData player = newPlayerData(PLAYER_UUID, "Alice", "203.0.113.42");
    PlayerHistoryData history = new PlayerHistoryData(
        player,
        new IPAddressString("203.0.113.42").toAddress(),
        1_700_000_100L,
        1_700_000_200L);

    PlayerSession mapped = EntityMappers.playerSession(history);

    assertNotNull(mapped);
    assertEquals("Alice", mapped.name());
    assertTrue(mapped.ip().isPresent(), "ip should round-trip into Optional.present");
    assertEquals("203.0.113.42", mapped.ip().get().toCanonicalString());
    assertEquals(1_700_000_100L, mapped.join());
    assertEquals(1_700_000_200L, mapped.leave());
  }

  @Test
  public void playerSessionNullReturnsNull() {
    assertNull(EntityMappers.playerSession(null));
  }

  // -- history entry ------------------------------------------------------

  @Test
  public void historyEntryReplacesNullStringsWithEmptyToHonourNonNullContract() {
    me.confuser.banmanager.common.data.HistoryEntry internal =
        new me.confuser.banmanager.common.data.HistoryEntry(
            42, "ban", "console", 1_700_000_000L, null, null);

    HistoryEntry mapped = EntityMappers.historyEntry(internal);

    assertNotNull(mapped);
    assertEquals(42, mapped.id());
    assertEquals("ban", mapped.type());
    assertEquals("console", mapped.actor());
    assertEquals(1_700_000_000L, mapped.created());
    assertEquals("", mapped.reason(),
        "null reason should be flattened to empty string for null-free record");
    assertEquals("", mapped.meta(),
        "null meta should be flattened to empty string for null-free record");
  }

  @Test
  public void historyEntryPreservesNonNullStrings() {
    me.confuser.banmanager.common.data.HistoryEntry internal =
        new me.confuser.banmanager.common.data.HistoryEntry(
            42, "warn", "console", 1_700_000_000L, "spam", "{}");

    HistoryEntry mapped = EntityMappers.historyEntry(internal);

    assertEquals("spam", mapped.reason());
    assertEquals("{}", mapped.meta());
  }

  @Test
  public void historyEntryNullReturnsNull() {
    assertNull(EntityMappers.historyEntry(null));
  }

  // -- name summary -------------------------------------------------------

  @Test
  public void playerNameSummaryRoundTripsRecord() {
    me.confuser.banmanager.common.data.PlayerNameSummary internal =
        new me.confuser.banmanager.common.data.PlayerNameSummary("Alice", 100L, 200L);

    PlayerNameSummary mapped = EntityMappers.playerNameSummary(internal);

    assertNotNull(mapped);
    assertEquals("Alice", mapped.name());
    assertEquals(100L, mapped.firstSeen());
    assertEquals(200L, mapped.lastSeen());
  }

  @Test
  public void playerNameSummaryNullReturnsNull() {
    assertNull(EntityMappers.playerNameSummary(null));
  }

  // -- mapper invariants --------------------------------------------------

  @Test
  public void mapperReturnsNewInstanceEachInvocation() throws Exception {
    PlayerData data = newPlayerData(PLAYER_UUID, "Alice", "203.0.113.42");

    Player a = EntityMappers.player(data);
    Player b = EntityMappers.player(data);

    assertNotNull(a);
    assertNotNull(b);
    assertEquals(a, b);
    // Records compare by value but identity should not be cached: that would
    // tie API DTO lifetimes to entity reuse and leak mutable internal state.
    assertFalse(a == b,
        "EntityMappers must not memoise — DTOs should be cheap, fresh values");
  }

  @Test
  public void mapperDoesNotShareReferencesWithSource() throws Exception {
    PlayerData data = newPlayerData(PLAYER_UUID, "Alice", "203.0.113.42");

    Player mapped = EntityMappers.player(data);

    // The IP gets converted into the API class via canonical-string round
    // trip, so it must NOT alias the internal one.
    assertNotNull(mapped);
    assertFalse(((Object) mapped.ip()) == ((Object) data.getIp()),
        "mapped IP must be a freshly constructed API IPAddress, not aliased");
  }

  @Test
  public void mapperPlayerUuidIsByValueIdentity() throws Exception {
    PlayerData data = newPlayerData(PLAYER_UUID, "Alice", "203.0.113.42");

    Player mapped = EntityMappers.player(data);

    assertSame(PLAYER_UUID, mapped.uuid(),
        "UUID is immutable and getUUID() returns the cached instance — sharing it is safe");
  }
}
