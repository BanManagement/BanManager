package me.confuser.banmanager.common.util;

import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/**
 * Utility class to manage JDBC driver registration.
 * Prevents BanManager's relocated drivers from leaking into the global DriverManager
 * where other plugins might accidentally use them.
 */
public class DriverManagerUtil {

  private static final String BM_DRIVER_PREFIX = "me.confuser.banmanager.common.";

  /**
   * Deregisters all BanManager relocated drivers from DriverManager.
   * This should be called after HikariCP pools are initialized, as HikariCP
   * caches the driver internally and doesn't need DriverManager for reconnections.
   */
  public static void deregisterRelocatedDrivers() {
    // Collect drivers to deregister first to avoid concurrent modification
    List<Driver> driversToDeregister = new ArrayList<>();
    Enumeration<Driver> drivers = DriverManager.getDrivers();

    while (drivers.hasMoreElements()) {
      Driver driver = drivers.nextElement();
      String driverClassName = driver.getClass().getName();

      if (driverClassName.startsWith(BM_DRIVER_PREFIX)) {
        driversToDeregister.add(driver);
      }
    }

    // Now deregister collected drivers
    for (Driver driver : driversToDeregister) {
      try {
        DriverManager.deregisterDriver(driver);
      } catch (SQLException e) {
        // Silently ignore - driver may have already been deregistered
      }
    }
  }
}
