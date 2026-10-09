<div align="center">
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 512 512" width="180" height="180">
  <defs>
    <!-- Ombra morbida per dare profondità -->
    <filter id="drop-shadow" x="-10%" y="-10%" width="130%" height="130%">
      <feDropShadow dx="0" dy="12" stdDeviation="16" flood-color="#0f172a" flood-opacity="0.12"/>
      <feDropShadow dx="0" dy="4" stdDeviation="4" flood-color="#0f172a" flood-opacity="0.08"/>
    </filter>

    <!-- Gradiente cornice di legno -->
    <linearGradient id="frame-grad" x1="0%" y1="0%" x2="100%" y2="100%">
      <stop offset="0%" stop-color="#8b5a2b"/>
      <stop offset="50%" stop-color="#6f421b"/>
      <stop offset="100%" stop-color="#543113"/>
    </linearGradient>

    <!-- Superficie lavagna (ardesia scura) -->
    <linearGradient id="board-grad" x1="0%" y1="0%" x2="0%" y2="100%">
      <stop offset="0%" stop-color="#2a3439"/>
      <stop offset="100%" stop-color="#1e2528"/>
    </linearGradient>

    <!-- Vaschetta porta-gessetti -->
    <linearGradient id="tray-grad" x1="0%" y1="0%" x2="0%" y2="100%">
      <stop offset="0%" stop-color="#e2e8f0"/>
      <stop offset="100%" stop-color="#cbd5e1"/>
    </linearGradient>

    <!-- Gessetto -->
    <linearGradient id="chalk-grad" x1="0%" y1="0%" x2="100%" y2="0%">
      <stop offset="0%" stop-color="#ffffff"/>
      <stop offset="100%" stop-color="#f1f5f9"/>
    </linearGradient>
  </defs>

  <!-- Gruppo principale con ombra -->
  <g filter="url(#drop-shadow)">
    <!-- Supporto a cavalletto -->
    <path d="M 128 380 L 96 450 M 384 380 L 416 450" stroke="#543113" stroke-width="14" stroke-linecap="round"/>

    <!-- Cornice -->
    <rect x="56" y="64" width="400" height="300" rx="16" ry="16" fill="url(#frame-grad)"/>

    <!-- Superficie Lavagna -->
    <rect x="76" y="84" width="360" height="260" rx="8" ry="8" fill="url(#board-grad)"/>

    <!-- Disegni a gesso sulla lavagna -->
    <g fill="none" stroke="#f8fafc" stroke-linecap="round" stroke-linejoin="round">
      <!-- Lampadina con filamento a U pulito -->
      <g stroke-opacity="0.9">
        <!-- Bulbo esterno -->
        <path d="M 135 170 C 135 142, 185 142, 185 170 C 185 186, 173 194, 172 205 L 148 205 C 147 194, 135 186, 135 170 Z" stroke-width="4.5"/>
        <!-- Filettatura base -->
        <path d="M 151 213 L 169 213 M 153 220 L 167 220" stroke-width="4"/>
        <!-- Contatto inferiore -->
        <path d="M 157 226 C 157 228, 163 228, 163 226" stroke-width="3"/>
        <!-- Filamento curvo classico -->
        <path d="M 154 205 L 154 180 C 154 170, 166 170, 166 180 L 166 205" stroke-width="2.5" stroke-opacity="0.75"/>
      </g>
      <!-- Raggi luminosi -->
      <g stroke-width="3.5" stroke-opacity="0.8">
        <line x1="160" y1="130" x2="160" y2="122"/>
        <line x1="190" y1="142" x2="197" y2="135"/>
        <line x1="130" y1="142" x2="123" y2="135"/>
        <line x1="200" y1="170" x2="208" y2="170"/>
        <line x1="120" y1="170" x2="112" y2="170"/>
      </g>

      <!-- Grafico di crescita -->
      <path d="M 235 225 L 265 190 L 295 205 L 335 150" stroke-width="5" stroke-opacity="0.95"/>
      <path d="M 320 150 L 335 150 L 335 165" stroke-width="5" stroke-opacity="0.95"/>
      <path d="M 225 235 L 345 235" stroke-width="3" stroke-opacity="0.4" stroke-dasharray="4 4"/>

      <!-- Formula matematica -->
      <text x="132" y="275" font-family="system-ui, -apple-system, sans-serif" font-size="22" font-weight="600" fill="#f8fafc" fill-opacity="0.85" stroke="none">E = mc²</text>
    </g>

    <!-- Ripiano gessarola -->
    <rect x="90" y="352" width="332" height="12" rx="4" ry="4" fill="url(#tray-grad)"/>

    <!-- Gessetto e Cancellino -->
    <rect x="290" y="347" width="36" height="7" rx="2" ry="2" fill="url(#chalk-grad)"/>
    <rect x="140" y="343" width="44" height="11" rx="3" fill="#334155"/>
    <rect x="140" y="350" width="44" height="4" rx="1" fill="#94a3b8"/>
  </g>
</svg>
</div>

# 🧽 Lavagna

<p align="center">
  <b>Una lavagna bianca che funziona nel browser e su Android.</b>
</p>

<p align="center">
  <a href="https://lavagna.simonepagliari44.workers.dev/">🌐 Apri la web app</a>
  &nbsp;•&nbsp;
  <b>Versione:</b> 1.0
  &nbsp;•&nbsp;
  <b>Lingue:</b> 🇮🇹 Italiano · 🇬🇧 English
</p>

<p align="center">
  <img src="https://img.shields.io/badge/web-single%20HTML-file-0b57d0" alt="web">
  <img src="https://img.shields.io/badge/android-Kotlin%20%2B%20Compose-0b57d0" alt="android">
  <img src="https://img.shields.io/badge/licenza-libera-444746" alt="licenza">
</p>

> 📝 Un file HTML, nessuna dipendenza, nessuna build. Stessa identica esperienza su desktop, tablet e telefono.

---

## ✨ Funzioni

| 🛠️ Strumento | 🔵 Cosa fa |
|---|---|
| ✏️ **Penna** | Disegno a mano libera |
| 🔷 **Forme** | **2D**: rettangolo, quadrato, cerchio, ellisse, linea, freccia, triangoli, stelle, cuore, rombo, parallelogramma, trapezio, poligoni, croce — **3D**: cubo, parallelepipedo, cilindro, cono, piramide, sfera |
| 🧽 **Gomma** | Normale · Cancella tratto intero · Lazio selettivo |
| 🖱️ **Puntatore** | Normale · Lazio selettivo |
| 🪣 **Riempimento** | Colora le aree chiuse |
| 🎨 **Sfondo** | Cambia il colore della pagina |
| 🔤 **Testo** | Dimensione, grassetto, corsivo, allineamento, 50 font |
| 🎨 **Colori** | 21 colori predefiniti + personalizzati con selettore HSV |
| 🔍 **Zoom** | Pinch, panoramica, pulsanti e modalità zoom |
| 📄 **Pagine** | Pagine indipendenti con drawer laterale |
| ↩️ **Undo / Redo** | Storico illimitato |
| 💾 **Salva** | Esporta in **PNG**, **JPG** o **PDF** |

E in più:

- 📦 Selezione con maniglie di resize
- 👆 Doppio tap per modificare il testo
- ⏱️ Long press per il menu contestuale (duplica · porta in primo piano · porta in fondo · elimina)
- 🖍️ Multi-selezione con lazo
- 🔄 **Flip** degli oggetti trascinandoli oltre il proprio centro

---

## 🌐 Versione web

Il sito è pubblicato su **Cloudflare Workers**.

```
Sito Web/
└── index.html      # l'app completa: HTML + CSS + JavaScript
```

- 💻 **Nessuna installazione**: si apre nel browser, anche da telefono
- 💾 **Salvataggio**: la lavagna resta nel browser e si scarica con i pulsanti PNG / JPG / PDF
- 🚀 **Pubblicare una modifica** (opzionale, serve l'account Cloudflare):

  ```bash
  wrangler deploy "Sito Web/index.html" --name lavagna
  ```

---

## 🤖 Versione Android

Porting nativo in **Kotlin + Jetpack Compose**, con rendering su `Canvas` e **senza WebView**.

```
App/
├── index.html      # sorgente di riferimento della web app
└── Android/
    ├── app/src/main/java/com/simonecompany/lavagna/
    │   ├── MainActivity.kt
    │   ├── model/Models.kt          # Tool, ShapeType, oggetti, colori
    │   ├── board/                   # rendering, geometria, font
    │   ├── export/BoardExporter.kt  # PNG / JPG / PDF
    │   ├── vm/                      # BoardViewModel + serializzazione JSON
    │   └── ui/
    │       ├── BoardScreen.kt       # schermata e overlay
    │       ├── BoardSurface.kt      # canvas e gesture
    │       ├── components/          # toolbar, bottom bar, drawer, dialoghi
    │       └── theme/Theme.kt
    └── app/build/outputs/apk/debug/app-debug.apk
```

### 🔧 Requisiti

| 📦 | |
|---|---|
| 🛠️ **Build** | Android Studio (Ladybug o successivo) **oppure** JDK 17 + Android SDK 35 |
| 📱 **Dispositivo** | Android 8.0 (`minSdk 26`) |
| 🎯 **Target** | `compileSdk` / `targetSdk` 35 |
| 🧰 **Toolchain** | Gradle 8.11.1 (wrapper incluso) · AGP 8.7.2 · Kotlin 2.0.21 · Compose BOM 2024.10.01 |

### 🏗️ Compilare

```bash
cd App/Android
./gradlew assembleDebug        # 🔧 build debug
./gradlew lintDebug            # 🔍 controllo statico
```

📦 L'APK si genera in `app/build/outputs/apk/debug/app-debug.apk`.

Se il JDK di default non è il 17:

```bash
JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64 ./gradlew assembleDebug
```

### 📲 Installare su un telefono

Collega il telefono via USB con il 🛠️ debug USB attivo, poi:

```bash
adb devices                                                   # deve comparire "device"
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb shell monkey -p com.simonecompany.lavagna -c android.intent.category.LAUNCHER 1
```

Oppure da Android Studio: ▶️ **Run ▶ Run 'app'** con il telefono selezionato come destinazione.

### 💾 Salvataggio

La lavagna viene salvata in automatico in `filesDir/lavagna/board.json` quando l'app va in background, e ricaricata all'avvio. 🖼️ Le immagini e i PDF si esportano con il selettore file di Android (SAF), quindi si possono salvare dove preferisci.

---

## 🔗 Le due versioni insieme

L'app Android è un porting di `index.html`: stessa struttura degli strumenti, stesse scorciatoie da tastiera, stessi colori. 🎨

Il layout segue i valori del CSS della web app, con gli stessi 📐 breakpoint (900px, 600px, 420px): su schermi stretti le etichette dei pulsanti spariscono e restano solo le icone, come nel sito.

Per tenere allineate le due versioni:

| 📄 File | 📌 Contenuto |
|---|---|
| `ui/components/WebStyle.kt` | colori e misure presi dal CSS |
| `ui/Strings.kt` | 🇮🇹🇬🇧 traduzioni, stesse chiavi `data-i18n` del sito |
| `board/BoardRenderer.kt` | forme geometriche |

---

## 📝 Note

- ☀️ Tema solo chiaro, come il sito
- 🔒 Nessun account, nessun server: tutto resta sul dispositivo
- 🔀 Il sito web e l'app Android **non sono sincronizzati**: sono versioni indipendenti dello stesso progetto

<p align="center">
  Fatto con ☕ da <a href="https://github.com/simonepagliari44-cyber">Simone</a>
</p>