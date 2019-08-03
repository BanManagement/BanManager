package me.confuser.banmanager.common.util;

import lombok.Getter;
import me.confuser.banmanager.common.data.PlayerReportData;

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
