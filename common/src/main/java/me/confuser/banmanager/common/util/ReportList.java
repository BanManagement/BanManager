package me.confuser.banmanager.common.util;

import lombok.Getter;
import me.confuser.banmanager.common.CommonPlayer;
import me.confuser.banmanager.common.commands.CommonSender;
import me.confuser.banmanager.common.data.PlayerReportData;
import net.kyori.text.event.ClickEvent;
import net.kyori.text.serializer.gson.GsonComponentSerializer;
import net.kyori.text.serializer.legacy.LegacyComponentSerializer;
import org.apache.commons.lang3.time.FastDateFormat;

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
    FastDateFormat dateFormatter = FastDateFormat.getInstance(dateTimeFormat);

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
          .set("created", dateFormatter
              .format(report.getCreated() * 1000L))
          .set("updated", dateFormatter
              .format(report.getUpdated() * 1000L)).toString();

      if (sender.isConsole()) {
        sender.sendMessage(message);
      } else {
        ((CommonPlayer) sender).sendJSONMessage(
            GsonComponentSerializer.INSTANCE.serialize(
                LegacyComponentSerializer.legacy().deserialize(
                    message, '&').clickEvent(ClickEvent.runCommand("/reports info " + report.getId()
                ))));
      }
    }
  }
}
