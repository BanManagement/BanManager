package me.confuser.banmanager.common.storage.mysql;


import me.confuser.banmanager.common.ormlite.field.SqlType;
import me.confuser.banmanager.common.ormlite.field.types.ByteArrayType;

public class ByteArray extends ByteArrayType {

      private static final ByteArray singleTon = new ByteArray();

      protected ByteArray() {
            super(SqlType.BYTE_ARRAY, new Class<?>[0]);
      }

      @Override
      public boolean isAppropriateId() {
            return true;
      }

      /* This cannot be lombok. */
      public static ByteArray getSingleton() {
            return singleTon;
      }
}
