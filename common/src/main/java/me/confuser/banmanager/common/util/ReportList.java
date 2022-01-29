package me.confuser.banmanager.common.util;

import lombok.Getter;
import me.confuser.banmanager.common.CommonPlayer;
import me.confuser.banmanager.common.commands.CommonSender;
import me.confuser.banmanager.common.data.PlayerReportData;
import me.confuser.banmanager.common.kyori.text.event.ClickEvent;
import me.confuser.banmanager.common.kyori.text.serializer.legacy.LegacyComponentSerializer;

import java.util.List;

public class ReportList {

  @Getter
  private List<PlayerReportData> list;
  @Getter
  private long count;
  @Getter
  private long maxPage;

  public ReportList(List<PlayerReportData> list, long count, long maxPage) {
    this.list = list;
    this.count = count;
    this.maxPage = maxPage;
  }

  public void send(CommonSender sender, int page) {
    String dateTimeFormat = Message.getString("report.list.row.dateTimeFormat");

    Message.get("report.list.row.header")
        .set("page", page)
        .set("maxPage", getMaxPage())
        .set("count", getCount())
        .sendTo(sender);

    for (PlayerReportData report : getList()) {
      String message = Message.get("report.list.row.all")
          .set("id", report.getId())
          .set("state", report.getState().getName())
          .set("player", report.getPlayer().getName())
          .set("actor", report.getActor().getName())
          .set("reason", report.getReason())
          .set("created", DateUtils.format(dateTimeFormat, report.getCreated()))
          .set("updated", DateUtils.format(dateTimeFormat, report.getUpdated())).toString();

      if (sender.isConsole()) {
        sender.sendMessage(message);
      } else {
        ((CommonPlayer) sender).sendJSONMessage(
            LegacyComponentSerializer.legacy().deserialize(
                message, '&').clickEvent(ClickEvent.runCommand("/reports info " + report.getId()
            )));
      }
    }
  }
}
