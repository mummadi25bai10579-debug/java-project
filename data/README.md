# Data Directory

This directory holds the persistent SQLite database file generated automatically by the application.

* **Database File:** `library.db` (auto-created on first application launch)
* **Storage Engine:** SQLite 3 via JDBC
* **Safety:** Foreign key constraints are enforced (`PRAGMA foreign_keys = ON;`).

If you wish to reset the application to a clean state, you can safely remove `data/library.db` and re-run the application.
