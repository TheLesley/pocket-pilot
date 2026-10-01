package com.example.pocketpilot.feature.finance.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.pocketpilot.feature.auth.data.local.dao.AccountDao
import com.example.pocketpilot.feature.auth.data.local.entity.AccountEntity
import com.example.pocketpilot.feature.finance.data.local.converter.FinanceTypeConverters
import com.example.pocketpilot.feature.finance.data.local.dao.BudgetDao
import com.example.pocketpilot.feature.finance.data.local.dao.SavingsGoalDao
import com.example.pocketpilot.feature.finance.data.local.dao.TransactionDao
import com.example.pocketpilot.feature.finance.data.local.entity.BudgetEntity
import com.example.pocketpilot.feature.finance.data.local.entity.SavingsGoalEntity
import com.example.pocketpilot.feature.finance.data.local.entity.TransactionEntity

@Database(
    entities = [
        TransactionEntity::class,
        BudgetEntity::class,
        SavingsGoalEntity::class,
        AccountEntity::class
    ],
    version = 4,
    exportSchema = false
)
@TypeConverters(FinanceTypeConverters::class)
abstract class PocketPilotDatabase : RoomDatabase() {

    abstract fun transactionDao(): TransactionDao
    abstract fun budgetDao(): BudgetDao
    abstract fun savingsGoalDao(): SavingsGoalDao
    abstract fun accountDao(): AccountDao

    companion object {
        private const val DATABASE_NAME = "pocketpilot.db"

        /**
         * Introduces the offline-first sync bookkeeping columns on every
         * syncable table. Existing rows are backfilled with `SYNCED` because
         * the pre-Phase-11 database only held server-canonical data (there was
         * no offline write path yet), so nothing needs to be uploaded.
         */
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                val syncableTables = listOf("transactions", "budgets", "savings_goals")
                for (table in syncableTables) {
                    db.execSQL(
                        "ALTER TABLE $table ADD COLUMN sync_status TEXT NOT NULL DEFAULT 'SYNCED'"
                    )
                    db.execSQL(
                        "ALTER TABLE $table ADD COLUMN local_updated_at INTEGER NOT NULL DEFAULT 0"
                    )
                    db.execSQL(
                        "ALTER TABLE $table ADD COLUMN last_sync_error TEXT"
                    )
                    db.execSQL(
                        "CREATE INDEX IF NOT EXISTS index_${table}_sync_status " +
                            "ON $table(sync_status)"
                    )
                }
            }
        }

        /**
         * Adds secondary indices that back hot sort/filter columns: the
         * transaction list can sort by amount, the budget list sorts by
         * `starts_at`, and the savings-goal list sorts by `created_at`. Without
         * these, SQLite falls back to a full table scan + in-memory sort once
         * the tables grow past a few hundred rows.
         */
        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_transactions_amount_minor_units " +
                        "ON transactions(amount_minor_units)"
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_budgets_starts_at " +
                        "ON budgets(starts_at)"
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_budgets_category_id " +
                        "ON budgets(category_id)"
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_savings_goals_created_at " +
                        "ON savings_goals(created_at)"
                )
            }
        }

        /**
         * Introduces the `auth_accounts` table used by the local-first auth
         * data source. Empty on first migrate — accounts are only inserted as
         * users sign up, so no backfill is required.
         */
        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS auth_accounts (
                        id TEXT NOT NULL PRIMARY KEY,
                        email TEXT NOT NULL,
                        display_name TEXT,
                        password_hash TEXT NOT NULL,
                        salt TEXT NOT NULL,
                        created_at INTEGER NOT NULL,
                        reset_code TEXT
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    "CREATE UNIQUE INDEX IF NOT EXISTS index_auth_accounts_email " +
                        "ON auth_accounts(email)"
                )
            }
        }

        fun create(context: Context): PocketPilotDatabase = Room.databaseBuilder(
            context.applicationContext,
            PocketPilotDatabase::class.java,
            DATABASE_NAME
        )
            .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
            .build()
    }
}
