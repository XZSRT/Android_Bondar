package com.example.android;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "PasswordManager.db";
    private static final int DATABASE_VERSION = 1;

    // Таблица пользователей
    private static final String TABLE_USERS = "users";
    private static final String COLUMN_USER_ID = "user_id";
    private static final String COLUMN_USERNAME = "username";
    private static final String COLUMN_PASSWORD = "password";

    // Таблица паролей
    private static final String TABLE_PASSWORDS = "passwords";
    private static final String COLUMN_PASSWORD_ID = "password_id";
    private static final String COLUMN_SERVICE = "service";
    private static final String COLUMN_LOGIN = "login";
    private static final String COLUMN_PASSWORD_VALUE = "password_value";
    private static final String COLUMN_NOTES = "notes";
    private static final String COLUMN_USER_FK = "user_fk";

    // Ключ для шифрования
    private static final String ENCRYPTION_KEY = "ThisIsASecretKey";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Создание таблицы пользователей
        String CREATE_USERS_TABLE = "CREATE TABLE " + TABLE_USERS + "("
                + COLUMN_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_USERNAME + " TEXT UNIQUE,"
                + COLUMN_PASSWORD + " TEXT" + ")";
        db.execSQL(CREATE_USERS_TABLE);

        // Создание таблицы паролей
        String CREATE_PASSWORDS_TABLE = "CREATE TABLE " + TABLE_PASSWORDS + "("
                + COLUMN_PASSWORD_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_SERVICE + " TEXT,"
                + COLUMN_LOGIN + " TEXT,"
                + COLUMN_PASSWORD_VALUE + " TEXT,"
                + COLUMN_NOTES + " TEXT,"
                + COLUMN_USER_FK + " INTEGER,"
                + "FOREIGN KEY(" + COLUMN_USER_FK + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_USER_ID + ")" + ")";
        db.execSQL(CREATE_PASSWORDS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PASSWORDS);
        onCreate(db);
    }

    // Методы для работы с пользователями
    public boolean addUser(String username, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_USERNAME, username);
        values.put(COLUMN_PASSWORD, password);

        long result = db.insert(TABLE_USERS, null, values);
        return result != -1;
    }

    public boolean checkUser(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] columns = {COLUMN_USER_ID};
        String selection = COLUMN_USERNAME + " = ?" + " AND " + COLUMN_PASSWORD + " = ?";
        String[] selectionArgs = {username, password};

        Cursor cursor = db.query(TABLE_USERS, columns, selection, selectionArgs, null, null, null);
        int count = cursor.getCount();
        cursor.close();
        return count > 0;
    }

    public boolean checkUsernameExists(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] columns = {COLUMN_USER_ID};
        String selection = COLUMN_USERNAME + " = ?";
        String[] selectionArgs = {username};

        Cursor cursor = db.query(TABLE_USERS, columns, selection, selectionArgs, null, null, null);
        int count = cursor.getCount();
        cursor.close();
        return count > 0;
    }

    public int getUserId(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] columns = {COLUMN_USER_ID};
        String selection = COLUMN_USERNAME + " = ?";
        String[] selectionArgs = {username};

        Cursor cursor = db.query(TABLE_USERS, columns, selection, selectionArgs, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            int columnIndex = cursor.getColumnIndex(COLUMN_USER_ID);
            if (columnIndex != -1) {
                int id = cursor.getInt(columnIndex);
                cursor.close();
                return id;
            }
            cursor.close();
        }
        return -1;
    }

    // Методы для работы с паролями
    public boolean addPassword(int userId, String service, String login, String password, String notes) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        try {
            String encryptedPassword = encrypt(password);
            values.put(COLUMN_SERVICE, service);
            values.put(COLUMN_LOGIN, login);
            values.put(COLUMN_PASSWORD_VALUE, encryptedPassword);
            values.put(COLUMN_NOTES, notes);
            values.put(COLUMN_USER_FK, userId);

            long result = db.insert(TABLE_PASSWORDS, null, values);
            return result != -1;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<PasswordModel> getAllPasswords(int userId) {
        List<PasswordModel> passwordList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String[] columns = {
                COLUMN_PASSWORD_ID,
                COLUMN_SERVICE,
                COLUMN_LOGIN,
                COLUMN_PASSWORD_VALUE,
                COLUMN_NOTES
        };

        String selection = COLUMN_USER_FK + " = ?";
        String[] selectionArgs = {String.valueOf(userId)};

        Cursor cursor = db.query(TABLE_PASSWORDS, columns, selection, selectionArgs, null, null, COLUMN_SERVICE + " ASC");

        if (cursor != null && cursor.moveToFirst()) {
            int idIndex = cursor.getColumnIndex(COLUMN_PASSWORD_ID);
            int serviceIndex = cursor.getColumnIndex(COLUMN_SERVICE);
            int loginIndex = cursor.getColumnIndex(COLUMN_LOGIN);
            int passwordIndex = cursor.getColumnIndex(COLUMN_PASSWORD_VALUE);
            int notesIndex = cursor.getColumnIndex(COLUMN_NOTES);

            do {
                PasswordModel password = new PasswordModel();

                if (idIndex != -1) password.setId(cursor.getInt(idIndex));
                if (serviceIndex != -1) password.setService(cursor.getString(serviceIndex));
                if (loginIndex != -1) password.setLogin(cursor.getString(loginIndex));

                try {
                    if (passwordIndex != -1) {
                        String encryptedPassword = cursor.getString(passwordIndex);
                        password.setPassword(decrypt(encryptedPassword));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    password.setPassword("");
                }

                if (notesIndex != -1) password.setNotes(cursor.getString(notesIndex));
                passwordList.add(password);
            } while (cursor.moveToNext());
        }

        if (cursor != null) {
            cursor.close();
        }
        return passwordList;
    }

    public boolean deletePassword(int passwordId) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_PASSWORDS, COLUMN_PASSWORD_ID + " = ?", new String[]{String.valueOf(passwordId)}) > 0;
    }

    // Методы для шифрования/дешифрования
    private String encrypt(String data) throws Exception {
        Key key = generateKey();
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, key);
        byte[] encryptedValue = cipher.doFinal(data.getBytes());
        return Base64.encodeToString(encryptedValue, Base64.DEFAULT);
    }

    private String decrypt(String data) throws Exception {
        Key key = generateKey();
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, key);
        byte[] decodedValue = Base64.decode(data, Base64.DEFAULT);
        byte[] decryptedValue = cipher.doFinal(decodedValue);
        return new String(decryptedValue);
    }

    private Key generateKey() {
        return new SecretKeySpec(ENCRYPTION_KEY.getBytes(), "AES");
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        if (!db.isReadOnly()) {
            // Enable foreign key constraints
            db.execSQL("PRAGMA foreign_keys=ON;");
        }
    }
}