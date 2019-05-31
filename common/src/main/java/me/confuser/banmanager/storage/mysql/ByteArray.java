package me.confuser.banmanager.storage.mysql;

import com.j256.ormlite.field.SqlType;
import com.j256.ormlite.field.types.ByteArrayType;

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
