package me.confuser.banmanager.storage.conversion;

public interface IConverter {

  void importPlayerBans();

  void importPlayerMutes();

  void importPlayerWarnings();

  void importIpBans();

  void importIpRangeBans();
}
