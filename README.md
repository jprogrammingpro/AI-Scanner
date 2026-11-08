# AI Scanner

Android-App zum Scannen von Dokumenten mit Offline-KI: Texterkennung, Erkennung von Telefonnummern, E-Mails, IBAN und Steuernummern mit Kopieren-Symbol, Export als PDF, CSV, TXT, JSON, Excel und Word, dazu Übersetzung und Zusammenfassung direkt auf dem Handy.

## Stand

Phase 1 (Grundgerüst): App startet direkt in der Kamera (Modus Dokument), mehrseitiger Scan, Dokumentenliste mit Suche und Bild-Import, Dokument-Ansicht mit Seiten, Werkzeuge, Auswertung und Einstellungen. Design in Orange, Hell- und Dunkelmodus folgen dem System, Deutsch und Englisch.

## APK installieren

Jeder Push baut über GitHub Actions eine Debug-APK. Unter **Actions → Android build → letzter Lauf → Artifacts** die Datei `ai-scanner-debug-apk` herunterladen, entpacken und die `app-debug.apk` auf dem Handy öffnen (Installation aus unbekannten Quellen erlauben).

## Technik

Kotlin, Jetpack Compose (Material 3), CameraX, Room, DataStore, Coil. Mindestversion Android 8.0 (API 26).
