package me.confuser.banmanager.common.data;

import java.util.HashSet;

public class PlayerExemptionsData {

  private HashSet<String> exemptions = new HashSet<>();

  public PlayerExemptionsData(HashSet<String> exemptions) {
    this.exemptions = exemptions;
  }

  public boolean isExempt(String type) {
    return exemptions.contains(type);
  }
}
