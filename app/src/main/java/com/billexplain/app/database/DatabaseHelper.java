package com.billexplain.app.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.billexplain.app.models.Bill;
import com.billexplain.app.models.Transaction;
import com.billexplain.app.models.User;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "billexplain.db";
    private static final int DB_VERSION = 1;

    // Tables
    private static final String TABLE_USERS = "users";
    private static final String TABLE_BILLS = "bills";
    private static final String TABLE_TRANSACTIONS = "transactions";

    // Users columns
    public static final String COL_USER_ID = "id";
    public static final String COL_USER_NAME = "name";
    public static final String COL_USER_EMAIL = "email";
    public static final String COL_USER_PASSWORD = "password";
    public static final String COL_USER_METER = "meter_number";
    public static final String COL_USER_PREMIUM = "is_premium";
    public static final String COL_USER_PROVIDER = "provider";

    // Bills columns
    public static final String COL_BILL_ID = "id";
    public static final String COL_BILL_USER_ID = "user_id";
    public static final String COL_BILL_MONTH = "month";
    public static final String COL_BILL_YEAR = "year";
    public static final String COL_BILL_AMOUNT = "amount";
    public static final String COL_BILL_GENERATION = "generation_charge";
    public static final String COL_BILL_TRANSMISSION = "transmission_charge";
    public static final String COL_BILL_DISTRIBUTION = "distribution_charge";
    public static final String COL_BILL_OTHER = "other_charges";
    public static final String COL_BILL_STATUS = "status";
    public static final String COL_BILL_DUE_DATE = "due_date";

    // Transactions columns
    public static final String COL_TXN_ID = "id";
    public static final String COL_TXN_USER_ID = "user_id";
    public static final String COL_TXN_NAME = "name";
    public static final String COL_TXN_DATE = "date";
    public static final String COL_TXN_AMOUNT = "amount";
    public static final String COL_TXN_METHOD = "method";
    public static final String COL_TXN_TYPE = "type";

    private static DatabaseHelper instance;

    public static synchronized DatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseHelper(context.getApplicationContext());
        }
        return instance;
    }

    private DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_USERS + " (" +
                COL_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_USER_NAME + " TEXT NOT NULL, " +
                COL_USER_EMAIL + " TEXT UNIQUE NOT NULL, " +
                COL_USER_PASSWORD + " TEXT NOT NULL, " +
                COL_USER_METER + " TEXT, " +
                COL_USER_PROVIDER + " TEXT DEFAULT 'MERALCO', " +
                COL_USER_PREMIUM + " INTEGER DEFAULT 0)");

        db.execSQL("CREATE TABLE " + TABLE_BILLS + " (" +
                COL_BILL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_BILL_USER_ID + " INTEGER, " +
                COL_BILL_MONTH + " TEXT, " +
                COL_BILL_YEAR + " INTEGER, " +
                COL_BILL_AMOUNT + " REAL, " +
                COL_BILL_GENERATION + " REAL, " +
                COL_BILL_TRANSMISSION + " REAL, " +
                COL_BILL_DISTRIBUTION + " REAL, " +
                COL_BILL_OTHER + " REAL, " +
                COL_BILL_STATUS + " TEXT DEFAULT 'Paid', " +
                COL_BILL_DUE_DATE + " TEXT)");

        db.execSQL("CREATE TABLE " + TABLE_TRANSACTIONS + " (" +
                COL_TXN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_TXN_USER_ID + " INTEGER, " +
                COL_TXN_NAME + " TEXT, " +
                COL_TXN_DATE + " TEXT, " +
                COL_TXN_AMOUNT + " REAL, " +
                COL_TXN_METHOD + " TEXT, " +
                COL_TXN_TYPE + " TEXT DEFAULT 'bill')");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_BILLS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TRANSACTIONS);
        onCreate(db);
    }

    // ── USER OPERATIONS ──

    public long registerUser(String name, String email, String password, String meter, String provider) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_USER_NAME, name);
        cv.put(COL_USER_EMAIL, email.toLowerCase());
        cv.put(COL_USER_PASSWORD, password);
        cv.put(COL_USER_METER, meter);
        cv.put(COL_USER_PROVIDER, provider);
        cv.put(COL_USER_PREMIUM, 0);
        long id = db.insert(TABLE_USERS, null, cv);
        if (id > 0) seedUserData(db, id, name);
        return id;
    }

    public User loginUser(String email, String password) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(TABLE_USERS, null,
                COL_USER_EMAIL + "=? AND " + COL_USER_PASSWORD + "=?",
                new String[]{email.toLowerCase(), password}, null, null, null);
        User user = null;
        if (c.moveToFirst()) user = cursorToUser(c);
        c.close();
        return user;
    }

    public User getUserById(int id) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(TABLE_USERS, null, COL_USER_ID + "=?",
                new String[]{String.valueOf(id)}, null, null, null);
        User user = null;
        if (c.moveToFirst()) user = cursorToUser(c);
        c.close();
        return user;
    }

    public boolean emailExists(String email) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(TABLE_USERS, new String[]{COL_USER_ID},
                COL_USER_EMAIL + "=?", new String[]{email.toLowerCase()}, null, null, null);
        boolean exists = c.getCount() > 0;
        c.close();
        return exists;
    }

    public boolean upgradeToPremium(int userId) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_USER_PREMIUM, 1);
        int rows = db.update(TABLE_USERS, cv, COL_USER_ID + "=?",
                new String[]{String.valueOf(userId)});
        if (rows > 0) {
            // Record premium subscription transaction
            ContentValues txn = new ContentValues();
            txn.put(COL_TXN_USER_ID, userId);
            txn.put(COL_TXN_NAME, "Premium Subscription");
            txn.put(COL_TXN_DATE, "Apr 27, 2026 · Monthly");
            txn.put(COL_TXN_AMOUNT, 59.0);
            txn.put(COL_TXN_METHOD, "GCash");
            txn.put(COL_TXN_TYPE, "premium");
            db.insert(TABLE_TRANSACTIONS, null, txn);
        }
        return rows > 0;
    }

    private User cursorToUser(Cursor c) {
        User u = new User();
        u.id = c.getInt(c.getColumnIndexOrThrow(COL_USER_ID));
        u.name = c.getString(c.getColumnIndexOrThrow(COL_USER_NAME));
        u.email = c.getString(c.getColumnIndexOrThrow(COL_USER_EMAIL));
        u.meterNumber = c.getString(c.getColumnIndexOrThrow(COL_USER_METER));
        u.isPremium = c.getInt(c.getColumnIndexOrThrow(COL_USER_PREMIUM)) == 1;
        u.provider = c.getString(c.getColumnIndexOrThrow(COL_USER_PROVIDER));
        return u;
    }

    // ── BILL OPERATIONS ──

    public List<Bill> getBillsForUser(int userId) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(TABLE_BILLS, null, COL_BILL_USER_ID + "=?",
                new String[]{String.valueOf(userId)}, null, null, COL_BILL_ID + " DESC");
        List<Bill> bills = new ArrayList<>();
        while (c.moveToNext()) bills.add(cursorToBill(c));
        c.close();
        return bills;
    }

    public Bill getLatestBill(int userId) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(TABLE_BILLS, null, COL_BILL_USER_ID + "=?",
                new String[]{String.valueOf(userId)}, null, null,
                COL_BILL_ID + " DESC", "1");
        Bill bill = null;
        if (c.moveToFirst()) bill = cursorToBill(c);
        c.close();
        return bill;
    }

    private Bill cursorToBill(Cursor c) {
        Bill b = new Bill();
        b.id = c.getInt(c.getColumnIndexOrThrow(COL_BILL_ID));
        b.month = c.getString(c.getColumnIndexOrThrow(COL_BILL_MONTH));
        b.year = c.getInt(c.getColumnIndexOrThrow(COL_BILL_YEAR));
        b.totalAmount = c.getFloat(c.getColumnIndexOrThrow(COL_BILL_AMOUNT));
        b.generationCharge = c.getFloat(c.getColumnIndexOrThrow(COL_BILL_GENERATION));
        b.transmissionCharge = c.getFloat(c.getColumnIndexOrThrow(COL_BILL_TRANSMISSION));
        b.distributionCharge = c.getFloat(c.getColumnIndexOrThrow(COL_BILL_DISTRIBUTION));
        b.otherCharges = c.getFloat(c.getColumnIndexOrThrow(COL_BILL_OTHER));
        b.status = c.getString(c.getColumnIndexOrThrow(COL_BILL_STATUS));
        b.dueDate = c.getString(c.getColumnIndexOrThrow(COL_BILL_DUE_DATE));
        return b;
    }

    public void markBillAsPaid(int billId) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_BILL_STATUS, "Paid");
        db.update(TABLE_BILLS, cv, COL_BILL_ID + "=?",
                new String[]{String.valueOf(billId)});
    }

    // ── TRANSACTION OPERATIONS ──

    public List<Transaction> getTransactionsForUser(int userId) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(TABLE_TRANSACTIONS, null, COL_TXN_USER_ID + "=?",
                new String[]{String.valueOf(userId)}, null, null, COL_TXN_ID + " DESC");
        List<Transaction> list = new ArrayList<>();
        while (c.moveToNext()) {
            Transaction t = new Transaction();
            t.id = c.getInt(c.getColumnIndexOrThrow(COL_TXN_ID));
            t.name = c.getString(c.getColumnIndexOrThrow(COL_TXN_NAME));
            t.date = c.getString(c.getColumnIndexOrThrow(COL_TXN_DATE));
            t.amount = c.getFloat(c.getColumnIndexOrThrow(COL_TXN_AMOUNT));
            t.method = c.getString(c.getColumnIndexOrThrow(COL_TXN_METHOD));
            t.type = c.getString(c.getColumnIndexOrThrow(COL_TXN_TYPE));
            list.add(t);
        }
        c.close();
        return list;
    }

    public void addTransaction(int userId, String name, String date, float amount, String method, String type) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_TXN_USER_ID, userId);
        cv.put(COL_TXN_NAME, name);
        cv.put(COL_TXN_DATE, date);
        cv.put(COL_TXN_AMOUNT, amount);
        cv.put(COL_TXN_METHOD, method);
        cv.put(COL_TXN_TYPE, type);
        db.insert(TABLE_TRANSACTIONS, null, cv);
    }

    // ── SEED DATA ──
    private void seedUserData(SQLiteDatabase db, long userId, String name) {
        // Insert OLDEST first so DESC query returns NEWEST (April) at top
        insertBill(db, userId, "November", 2025, 2650, 970, 405, 610, 665, "Paid", "Nov 25, 2025");
        insertBill(db, userId, "December", 2025, 2780, 1000, 415, 625, 740, "Paid", "Dec 28, 2025");
        insertBill(db, userId, "January",  2026, 2540, 920, 400, 600, 620, "Paid", "Jan 24, 2026");
        insertBill(db, userId, "February", 2026, 3010, 1100, 435, 660, 815, "Paid", "Feb 22, 2026");
        insertBill(db, userId, "March",    2026, 2890, 1050, 420, 640, 780, "Paid", "Mar 26, 2026");
        insertBill(db, userId, "April",    2026, 3250, 1200, 450, 680, 920, "Due",  "Apr 27, 2026");

        // Transactions oldest first
        insertTxn(db, userId, "November 2025 Bill", "Nov 25, 2025 · Via GCash", 2650, "GCash", "bill");
        insertTxn(db, userId, "December 2025 Bill", "Dec 28, 2025 · Via BPI",   2780, "BPI",   "bill");
        insertTxn(db, userId, "January 2026 Bill",  "Jan 24, 2026 · Via GCash", 2540, "GCash", "bill");
        insertTxn(db, userId, "February 2026 Bill", "Feb 22, 2026 · Via Maya",  3010, "Maya",  "bill");
        insertTxn(db, userId, "March 2026 Bill",     "Mar 26, 2026 · Via BPI",   2890, "BPI",   "bill");
        insertTxn(db, userId, "April 2026 Bill",     "Apr 25, 2026 · Via GCash", 3250, "GCash", "bill");
    }

    private void insertBill(SQLiteDatabase db, long userId, String month, int year,
                            float total, float gen, float trans, float dist, float other,
                            String status, String dueDate) {
        ContentValues cv = new ContentValues();
        cv.put(COL_BILL_USER_ID, userId);
        cv.put(COL_BILL_MONTH, month);
        cv.put(COL_BILL_YEAR, year);
        cv.put(COL_BILL_AMOUNT, total);
        cv.put(COL_BILL_GENERATION, gen);
        cv.put(COL_BILL_TRANSMISSION, trans);
        cv.put(COL_BILL_DISTRIBUTION, dist);
        cv.put(COL_BILL_OTHER, other);
        cv.put(COL_BILL_STATUS, status);
        cv.put(COL_BILL_DUE_DATE, dueDate);
        db.insert(TABLE_BILLS, null, cv);
    }

    private void insertTxn(SQLiteDatabase db, long userId, String name, String date,
                           float amount, String method, String type) {
        ContentValues cv = new ContentValues();
        cv.put(COL_TXN_USER_ID, userId);
        cv.put(COL_TXN_NAME, name);
        cv.put(COL_TXN_DATE, date);
        cv.put(COL_TXN_AMOUNT, amount);
        cv.put(COL_TXN_METHOD, method);
        cv.put(COL_TXN_TYPE, type);
        db.insert(TABLE_TRANSACTIONS, null, cv);
    }
}
