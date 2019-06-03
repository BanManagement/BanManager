package me.confuser.banmanager.common.commands.report;

import lombok.Getter;
import me.confuser.banmanager.data.PlayerReportData;

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
}
