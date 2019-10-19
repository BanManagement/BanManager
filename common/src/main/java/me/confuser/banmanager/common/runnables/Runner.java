package me.confuser.banmanager.common.runnables;

import java.util.HashMap;

public class Runner implements Runnable {

  private final HashMap<String, BmRunnable> runners;
  private final long lastExecuted = 0L;

  public Runner(BmRunnable... runners) {
    this.runners = new HashMap<>();

    for (BmRunnable runner : runners) {
      this.runners.put(runner.getName(), runner);
    }
  }

  @Override
  public void run() {
    for (BmRunnable runner : runners.values()) {
      if (!runner.shouldExecute()) continue;

      runner.beforeRun();

      // Ensure runner exceptions are caught to still allow others to execute
      try {
        runner.run();
      } catch (Exception e) {
        e.printStackTrace();
      }

      runner.afterRun();
    }
  }

  public BmRunnable getRunner(String name) {
    return runners.get(name);
  }
}
