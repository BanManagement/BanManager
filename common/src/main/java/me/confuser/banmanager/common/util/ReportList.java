package me.confuser.banmanager.common.util;

import lombok.Getter;
import me.confuser.banmanager.common.commands.CommonSender;
import me.confuser.banmanager.common.data.PlayerReportData;
import me.confuser.banmanager.common.kyori.text.Component;

import java.util.ArrayList;
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
    String dateTimeFormat = Message.getRawTemplate("report.list.row.dateTimeFormat");

    Component header = Message.get("report.list.row.header")
        .set("page", page)
        .set("maxPage", getMaxPage())
        .set("count", getCount())
        .resolveComponent();

    List<Component> items = new ArrayList<>();

    for (PlayerReportData report : getList()) {
      Component row = Message.get("report.list.row.all")
          .set("id", report.getId())
          .set("state", report.getState().getName())
          .set("player", report.getPlayer().getName())
          .set("actor", report.getActor().getName())
          .set("reason", report.getReason())
          .set("created", DateUtils.format(dateTimeFormat, report.getCreated()))
          .set("updated", DateUtils.format(dateTimeFormat, report.getUpdated()))
          .resolveComponent();

      items.add(row);
    }

    PaginatedView view = new PaginatedView(items, "/reports list");
    view.send(sender, page, header, null);
  }
}
