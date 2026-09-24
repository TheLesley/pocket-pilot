package com.example.pocketpilot.testutil

import android.app.Application

/**
 * Bare Application used by Robolectric tests so the production
 * `PocketPilotApplication.onCreate()` (which wires WorkManager, Room, etc.)
 * does not run inside unit tests.
 */
class TestApplication : Application()
