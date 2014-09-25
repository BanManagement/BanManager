package me.confuser.banmanager.storage.mysql;

import com.j256.ormlite.field.SqlType;
import com.j256.ormlite.field.types.ByteArrayType;
import lombok.Getter;

public class ByteArray extends ByteArrayType {

      @Getter
      private static final ByteArray singleTon = new ByteArray();

      protected ByteArray() {
            super(SqlType.BYTE_ARRAY, new Class<?>[0]);
      }

      @Override
      public boolean isAppropriateId() {
            return true;
      }

}
