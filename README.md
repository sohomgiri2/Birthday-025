# A Little World Made for You — Android App

An interactive, offline-first Jetpack Compose experience designed with a **Nothing OS × Liquid Glass × 3D** midnight aesthetic.

**Developer:** Sohom Giri (`@itz_sg_063`)  
**Storage Engine:** SG DataVault (offline JSON + local media vault)

---

## Quick Configuration (`AppConfig.kt`)

Edit `app/src/main/java/com/example/config/AppConfig.kt` to customize:

- **Gayatri's Letter ("A Small Favor to Ask")**
  - `GAYATRI_LETTER_TEXT`
- **Secret Vault Room Password**
  - `SECRET_ROOM_PASSWORD` (default: `"2026"`)

> **Note:** Client-side password locks are a fun interactive privacy barrier for a gift app, not cryptographic security.

---

## Adding Photos, Letter Attachments, and Music

### 1. Numbered Photos (`photo1.png` ... `photo300.png`)
Drop numbered images into `app/src/main/assets/photos/`:
- `photo1.png`, `photo2.jpg`, `photo3.jpeg`, ... up to `photo300`
- Missing numbers are skipped automatically with no broken image placeholders.
- Users can also add photos directly inside the app via the zero-permission **Android Photo Picker**.

### 2. Letter Attachments
Drop `letter1.png`, `letter2.png`, `letter3.png` (or `.jpg`/`.jpeg`) into `app/src/main/assets/letters/`.

### 3. Local Music
Drop `.mp3`, `.m4a`, `.wav`, or `.ogg` tracks into `app/src/main/assets/music/` (e.g., `song1.mp3`, `song2.mp3`), or import local audio files directly in the Music Room. Built-in offline ambient synth tracks are included if no MP3 files are bundled.
