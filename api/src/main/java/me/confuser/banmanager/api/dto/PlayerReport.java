package me.confuser.banmanager.api.dto;

import java.util.Objects;
import java.util.Optional;

/**
 * Report filed against a player.
 *
 * @param assignee the staff member currently handling the report; empty
 *                 when nobody has claimed it
 * @param state the workflow state ({@code Open}, {@code Assigned} etc.)
 */
public record PlayerReport(
    int id,
    Player player,
    Player actor,
    Optional<Player> assignee,
    ReportState state,
    String reason,
    long created,
    long updated
) {

  public PlayerReport {
    Objects.requireNonNull(player, "player");
    Objects.requireNonNull(actor, "actor");
    Objects.requireNonNull(assignee, "assignee");
    Objects.requireNonNull(state, "state");
    Objects.requireNonNull(reason, "reason");
  }
}
