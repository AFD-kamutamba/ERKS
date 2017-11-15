package afd.ers.db;

import android.provider.BaseColumns;

public class ERSList {
    public static final String DB_NAME = "ERS_table.db";
    public static final int DB_VERSION = 1;


    public class ItemEntry implements BaseColumns {
        public static final String TABLE = "Items";

        public static final String COL_cat = "category";

        public static final String COL_name = "name";

        public static final String COL_price = "price";

        public static final String COL_stock = "stock";

        public static final String COL_picture = "picture";
    }

    public class RecordEntry implements BaseColumns {
        public static final String TABLE = "Records";

        public static final String COL_item_ID = "itemID";

        public static final String COL_price = "price";

        public static final String COL_amount = "amount";

        public static final String COL_stock_after_record = "stockAfterRecord";

        public static final String COL_date = "date";
    }

    public class EmployeeEntry implements BaseColumns {
        public static final String TABLE = "Employees";

        public static final String COL_name = "name";

        public static final String COL_password = "password";
    }

    public class ClockInEntry implements BaseColumns {
        public static final String TABLE = "ClockIn";

        public static final String COL_name = "name";

        public static final String COL_date = "date";

        public static final String COL_start_hour = "startHour";

        public static final String COL_end_hour = "endHour";

        public static final String COL_start_money = "startMoney";

        public static final String COL_end_money = "endMoney";

        public static final String COL_difference = "difference";
    }
}