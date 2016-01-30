package me.confuser.banmanager.storage.conversion;

public interface IConverter {

  public void importPlayerBans();

  public void importPlayerMutes();

  public void importPlayerWarnings();

  public void importIpBans();

  public void importIpRangeBans();
}
