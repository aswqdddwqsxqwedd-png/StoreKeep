package com.example.storekeep.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.storekeep.model.Product;
import com.example.storekeep.model.StockOperation;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DatabaseHelper extends SQLiteOpenHelper {

    public static final String OP_IN = "ПРИХОД";
    public static final String OP_SALE = "ПРОДАЖА";
    public static final String OP_ADJUST = "ИЗМЕНЕНИЕ";

    private static final String DB_NAME = "storekeep.db";
    private static final int DB_VERSION = 4;

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(
                "CREATE TABLE users ("
                        + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                        + "password TEXT NOT NULL,"
                        + "nickname TEXT NOT NULL UNIQUE,"
                        + "email TEXT NOT NULL UNIQUE,"
                        + "phone TEXT NOT NULL UNIQUE)"
        );
        db.execSQL(
                "CREATE TABLE products ("
                        + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                        + "name TEXT NOT NULL,"
                        + "price REAL NOT NULL,"
                        + "quantity INTEGER NOT NULL,"
                        + "category TEXT)"
        );
        db.execSQL(
                "CREATE TABLE operations ("
                        + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                        + "product_id INTEGER NOT NULL,"
                        + "type TEXT NOT NULL,"
                        + "amount INTEGER NOT NULL,"
                        + "date INTEGER NOT NULL)"
        );
        insertDefaultUser(db);
        insertSampleProducts(db);
    }

    private void insertDefaultUser(SQLiteDatabase db) {
        ContentValues cv = new ContentValues();
        cv.put("password", "admin");
        cv.put("nickname", "admin");
        cv.put("email", "admin@storekeep.local");
        cv.put("phone", "000000000000");
        db.insert("users", null, cv);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            insertSampleProductsIfEmpty(db);
        }
        if (oldVersion < 3) {
            migrateDemoPricesToSom(db);
        }
        if (oldVersion < 4) {
            migrateUsersToV4(db);
        }
    }

    private void migrateUsersToV4(SQLiteDatabase db) {
        db.execSQL(
                "CREATE TABLE users_new ("
                        + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                        + "password TEXT NOT NULL,"
                        + "nickname TEXT NOT NULL UNIQUE,"
                        + "email TEXT NOT NULL UNIQUE,"
                        + "phone TEXT NOT NULL UNIQUE)"
        );
        boolean hasLoginColumn = false;
        try (Cursor ti = db.rawQuery("PRAGMA table_info(users)", null)) {
            while (ti.moveToNext()) {
                if ("login".equalsIgnoreCase(ti.getString(1))) {
                    hasLoginColumn = true;
                    break;
                }
            }
        }
        if (hasLoginColumn) {
            db.execSQL(
                    "INSERT INTO users_new (id, password, nickname, email, phone) "
                            + "SELECT id, password, login, lower(login) || '@storekeep.local', '000000000000' "
                            + "FROM users");
        }
        db.execSQL("DROP TABLE IF EXISTS users");
        db.execSQL("ALTER TABLE users_new RENAME TO users");
    }

    /** Перевод старых примеров с «рублёвых» значений в узбекские сумы. */
    private void migrateDemoPricesToSom(SQLiteDatabase db) {
        db.execSQL(
                "UPDATE products SET price = ? WHERE name = ? AND price < ?",
                new Object[]{18_500d, "Молоко 3,2%", 1_000d});
        db.execSQL(
                "UPDATE products SET price = ? WHERE name = ? AND price < ?",
                new Object[]{11_200d, "Хлеб бородинский", 1_000d});
    }

    /** Начальные товары при первой установке и при миграции на v2, если список пустой. */
    private void insertSampleProducts(SQLiteDatabase db) {
        long t = System.currentTimeMillis();
        insertProductRow(db, "Молоко 3,2%", 18_500, 24, "Продукты", t);
        insertProductRow(db, "Хлеб бородинский", 11_200, 18, "Продукты", t + 1);
    }

    private void insertSampleProductsIfEmpty(SQLiteDatabase db) {
        try (Cursor c = db.rawQuery("SELECT COUNT(*) FROM products", null)) {
            if (c.moveToFirst() && c.getInt(0) == 0) {
                insertSampleProducts(db);
            }
        }
    }

    private void insertProductRow(SQLiteDatabase db, String name, double price, int quantity,
                                  String category, long dateMillis) {
        ContentValues cv = new ContentValues();
        cv.put("name", name);
        cv.put("price", price);
        cv.put("quantity", quantity);
        cv.put("category", category);
        long id = db.insert("products", null, cv);
        if (id != -1 && quantity > 0) {
            insertOperation(db, id, OP_IN, quantity, dateMillis);
        }
    }

    public static String normalizePhone(String raw) {
        if (raw == null) return "";
        return raw.replaceAll("\\D", "");
    }

    /**
     * Вход по нику, почте (без учёта регистра) или номеру (только цифры).
     */
    public boolean authenticate(String identifier, String password) {
        if (identifier == null || password == null) return false;
        String id = identifier.trim();
        if (id.isEmpty() || password.isEmpty()) return false;
        String idEmail = id.toLowerCase(Locale.ROOT);
        String idPhone = normalizePhone(id);
        SQLiteDatabase db = getReadableDatabase();
        try (Cursor c = db.rawQuery(
                "SELECT id FROM users WHERE password = ? AND nickname = ? COLLATE NOCASE LIMIT 1",
                new String[]{password, id})) {
            if (c.moveToFirst()) return true;
        }
        try (Cursor c = db.rawQuery(
                "SELECT id FROM users WHERE password = ? AND LOWER(email) = ? LIMIT 1",
                new String[]{password, idEmail})) {
            if (c.moveToFirst()) return true;
        }
        if (!idPhone.isEmpty()) {
            try (Cursor c = db.rawQuery(
                    "SELECT id FROM users WHERE password = ? AND phone = ? LIMIT 1",
                    new String[]{password, idPhone})) {
                if (c.moveToFirst()) return true;
            }
        }
        return false;
    }

    /** Локальная регистрация: ник, почта, номер (хранится без + и пробелов), пароль. */
    public boolean registerUser(String nickname, String email, String phone, String password) {
        String n = nickname != null ? nickname.trim() : "";
        String e = email != null ? email.trim().toLowerCase(Locale.ROOT) : "";
        String p = normalizePhone(phone);
        if (n.isEmpty() || e.isEmpty() || p.isEmpty() || password == null || password.isEmpty()) {
            return false;
        }
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("nickname", n);
        cv.put("email", e);
        cv.put("phone", p);
        cv.put("password", password);
        return db.insert("users", null, cv) != -1;
    }

    public long insertProduct(String name, double price, int quantity, String category) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("name", name);
        cv.put("price", price);
        cv.put("quantity", quantity);
        cv.put("category", category == null ? "" : category);
        long id = db.insert("products", null, cv);
        if (id != -1 && quantity > 0) {
            insertOperation(db, id, OP_IN, quantity, System.currentTimeMillis());
        }
        return id;
    }

    public void updateProduct(long id, String name, double price, int newQuantity, String category,
                              int previousQuantity) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("name", name);
        cv.put("price", price);
        cv.put("quantity", newQuantity);
        cv.put("category", category == null ? "" : category);
        db.update("products", cv, "id = ?", new String[]{String.valueOf(id)});
        int diff = newQuantity - previousQuantity;
        if (diff != 0) {
            insertOperation(db, id, OP_ADJUST, Math.abs(diff), System.currentTimeMillis());
        }
    }

    public void deleteProduct(long productId) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete("operations", "product_id = ?", new String[]{String.valueOf(productId)});
        db.delete("products", "id = ?", new String[]{String.valueOf(productId)});
    }

    /** Все товары или поиск только по названию (как раньше). */
    public List<Product> getProducts(String nameQuery) {
        return getProducts(nameQuery, null);
    }

    /**
     * Список товаров с фильтром по названию (LIKE) и точным совпадению категории.
     * @param nameQuery подстрока имени или null/пусто = без фильтра по имени
     * @param category    ровно как в БД или null/пусто = все категории
     */
    public List<Product> getProducts(String nameQuery, String category) {
        SQLiteDatabase db = getReadableDatabase();
        StringBuilder sql = new StringBuilder(
                "SELECT id, name, price, quantity, category FROM products WHERE 1=1");
        List<String> argList = new ArrayList<>();
        if (nameQuery != null && !nameQuery.trim().isEmpty()) {
            sql.append(" AND name LIKE ?");
            argList.add("%" + nameQuery.trim() + "%");
        }
        if (category != null && !category.trim().isEmpty()) {
            sql.append(" AND category = ?");
            argList.add(category.trim());
        }
        sql.append(" ORDER BY name COLLATE NOCASE");
        String[] args = argList.isEmpty() ? null : argList.toArray(new String[0]);
        List<Product> list = new ArrayList<>();
        try (Cursor c = db.rawQuery(sql.toString(), args)) {
            while (c.moveToNext()) {
                list.add(new Product(
                        c.getLong(0),
                        c.getString(1),
                        c.getDouble(2),
                        c.getInt(3),
                        c.getString(4)
                ));
            }
        }
        return list;
    }

    /** Уникальные непустые категории для фильтра. */
    public List<String> getDistinctCategories() {
        SQLiteDatabase db = getReadableDatabase();
        List<String> list = new ArrayList<>();
        try (Cursor c = db.rawQuery(
                "SELECT DISTINCT TRIM(category) AS c FROM products "
                        + "WHERE category IS NOT NULL AND LENGTH(TRIM(category)) > 0 "
                        + "ORDER BY c COLLATE NOCASE",
                null)) {
            while (c.moveToNext()) {
                String cat = c.getString(0);
                if (cat != null && !cat.isEmpty()) list.add(cat);
            }
        }
        return list;
    }

    public Product getProduct(long id) {
        SQLiteDatabase db = getReadableDatabase();
        try (Cursor c = db.query("products",
                new String[]{"id", "name", "price", "quantity", "category"},
                "id = ?", new String[]{String.valueOf(id)}, null, null, null)) {
            if (c.moveToFirst()) {
                return new Product(
                        c.getLong(0),
                        c.getString(1),
                        c.getDouble(2),
                        c.getInt(3),
                        c.getString(4)
                );
            }
        }
        return null;
    }

    public boolean sellProduct(long productId, int amount) {
        if (amount <= 0) return false;
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            try (Cursor c = db.rawQuery(
                    "SELECT quantity FROM products WHERE id = ?",
                    new String[]{String.valueOf(productId)})) {
                if (!c.moveToFirst()) {
                    db.endTransaction();
                    return false;
                }
                int stock = c.getInt(0);
                if (stock < amount) {
                    db.endTransaction();
                    return false;
                }
                ContentValues cv = new ContentValues();
                cv.put("quantity", stock - amount);
                db.update("products", cv, "id = ?", new String[]{String.valueOf(productId)});
            }
            insertOperation(db, productId, OP_SALE, amount, System.currentTimeMillis());
            db.setTransactionSuccessful();
            return true;
        } finally {
            db.endTransaction();
        }
    }

    private void insertOperation(SQLiteDatabase db, long productId, String type, int amount, long date) {
        ContentValues cv = new ContentValues();
        cv.put("product_id", productId);
        cv.put("type", type);
        cv.put("amount", amount);
        cv.put("date", date);
        db.insert("operations", null, cv);
    }

    public List<StockOperation> getOperations() {
        SQLiteDatabase db = getReadableDatabase();
        String sql =
                "SELECT o.id, o.product_id, p.name, o.type, o.amount, o.date "
                        + "FROM operations o "
                        + "JOIN products p ON p.id = o.product_id "
                        + "ORDER BY o.date DESC";
        List<StockOperation> list = new ArrayList<>();
        try (Cursor c = db.rawQuery(sql, null)) {
            while (c.moveToNext()) {
                list.add(new StockOperation(
                        c.getLong(0),
                        c.getLong(1),
                        c.getString(2),
                        c.getString(3),
                        c.getInt(4),
                        c.getLong(5)
                ));
            }
        }
        return list;
    }

    /** Продано единиц по всем продажам (сумма amount для типа ПРОДАЖА). */
    public int getTotalSoldUnits() {
        SQLiteDatabase db = getReadableDatabase();
        try (Cursor c = db.rawQuery(
                "SELECT IFNULL(SUM(amount), 0) FROM operations WHERE type = ?",
                new String[]{OP_SALE})) {
            if (c.moveToFirst()) return c.getInt(0);
        }
        return 0;
    }

    /** Сумма остатков по складу. */
    public int getTotalStockUnits() {
        SQLiteDatabase db = getReadableDatabase();
        try (Cursor c = db.rawQuery("SELECT IFNULL(SUM(quantity), 0) FROM products", null)) {
            if (c.moveToFirst()) return c.getInt(0);
        }
        return 0;
    }

    /** Стоимость всех остатков на складе (цена × количество), сўм. */
    public double getInventoryValueSom() {
        SQLiteDatabase db = getReadableDatabase();
        try (Cursor c = db.rawQuery(
                "SELECT IFNULL(SUM(price * quantity), 0) FROM products", null)) {
            if (c.moveToFirst()) return c.getDouble(0);
        }
        return 0;
    }

    /**
     * Оценка выручки с продаж: для каждой продажи amount × текущая цена товара.
     * Если цены менялись после продажи — это приближение.
     */
    public double getSoldRevenueSom() {
        SQLiteDatabase db = getReadableDatabase();
        try (Cursor c = db.rawQuery(
                "SELECT IFNULL(SUM(o.amount * p.price), 0) FROM operations o "
                        + "JOIN products p ON p.id = o.product_id "
                        + "WHERE o.type = ?",
                new String[]{OP_SALE})) {
            if (c.moveToFirst()) return c.getDouble(0);
        }
        return 0;
    }
}
