# ☕ Café Billing App

A lightweight, offline-first Android billing app for café/restaurant internal staff use.  
Built with **Kotlin + Jetpack Compose + MVVM + Room + Hilt**.

---

## 📱 Screens

| Screen | Description |
|---|---|
| **Order Screen** | Select items, adjust quantities, view cart, place order |
| **Bill Summary** | Full receipt with item breakdown, total, print/PDF option |
| **Menu Management** | Add, edit, delete dishes with search/filter |
| **Sales History** | Today's analytics + full order history with expandable cards |

---

## 🗂 Project Structure

```
app/src/main/java/com/cafe/billing/
│
├── CafeBillingApp.kt          # @HiltAndroidApp — Hilt entry point
├── MainActivity.kt            # Single activity, hosts NavGraph
│
├── data/
│   ├── models/
│   │   ├── MenuItem.kt        # @Entity — menu_items table
│   │   └── SalesOrder.kt      # @Entity — sales_orders table + TypeConverter
│   ├── dao/
│   │   ├── MenuItemDao.kt     # CRUD + search queries for menu items
│   │   └── SalesOrderDao.kt   # Insert/query/analytics for orders
│   ├── database/
│   │   └── CafeDatabase.kt    # Room database singleton
│   └── repository/
│       ├── MenuRepository.kt  # Menu data source of truth
│       └── SalesRepository.kt # Sales data + analytics helpers
│
├── di/
│   └── DatabaseModule.kt      # Hilt module: provides DB, DAOs
│
├── viewmodel/
│   ├── MenuViewModel.kt       # State for Menu Management screen
│   ├── OrderViewModel.kt      # State for Order screen + cart logic
│   ├── BillViewModel.kt       # Load order + trigger print
│   └── SalesHistoryViewModel.kt # Analytics + order history
│
├── ui/
│   ├── NavGraph.kt            # Navigation routes + NavHost
│   ├── theme/
│   │   └── Theme.kt           # Material 3 color scheme (light + dark)
│   ├── menu/
│   │   └── MenuManagementScreen.kt
│   ├── order/
│   │   └── OrderScreen.kt
│   ├── bill/
│   │   └── BillScreen.kt
│   └── history/
│       └── SalesHistoryScreen.kt
│
└── utils/
    ├── BillPrintAdapter.kt    # Android PrintDocumentAdapter (PDF/print)
    └── DateUtils.kt           # Shared date/currency formatters
```

---

## 🛠 Tech Stack

| Layer | Technology |
|---|---|
| Language | Kotlin |
| UI | Jetpack Compose + Material 3 |
| Architecture | MVVM (ViewModel + StateFlow) |
| Database | Room (SQLite) |
| DI | Hilt |
| Navigation | Jetpack Navigation Compose |
| Printing | Android PrintManager + custom PrintDocumentAdapter |
| JSON | Gson (for serializing cart items in DB) |

---

## 🚀 How to Build

### Prerequisites
- Android Studio Hedgehog (2023.1.1) or newer
- JDK 17
- Android SDK 35
- Min device: Android 8.0 (API 26)

### Steps
1. Clone or unzip the project
2. Open in Android Studio: `File → Open → CafeBillingApp`
3. Wait for Gradle sync to complete (first sync downloads ~200MB)
4. Run on device or emulator: `Run → Run 'app'`

---

## 💡 Usage Flow

```
Staff opens app
      ↓
Order Screen (home)
  → Search dishes
  → Tap + to add items to cart
  → Tap – to reduce quantity
  → Cart summary appears at bottom
  → Tap "Generate Bill"
      ↓
Bill Summary Screen
  → See full itemized receipt
  → Tap "Print / PDF" to print or save PDF
  → Tap "New Order" to return to order screen
      ↓
Sales History (via top bar icon)
  → Today's revenue / order count / avg order
  → All historical orders (expandable)
      ↓
Menu Management (via top bar icon)
  → Add / edit / delete dishes
  → Search filter
```

---

## 🖨 Printing

The app uses Android's built-in **PrintManager** framework:
- Opens the system print dialog
- Works with any **Wi-Fi / AirPrint / IPP** printer
- Includes **"Save as PDF"** option (no printer needed)
- For **thermal printers**: use the PDF output and send via a Bluetooth ESC/POS app

---

## 🌗 Dark Mode

The app automatically follows the system dark/light mode setting.  
Toggle via: `Settings → Display → Dark theme`

---

## 📦 Adding Sample Data

On first run, the menu will be empty. Add dishes via:
`Top bar → Menu icon → + button`

Example dishes to add:
| Name | Price |
|---|---|
| Masala Dosa | ₹60 |
| Filter Coffee | ₹25 |
| Idli (2 pcs) | ₹40 |
| Vada | ₹30 |
| Upma | ₹45 |

---

## 🔮 Future Improvements

- Table number tracking
- Multiple payment methods (cash/UPI/card)
- Export sales report as Excel/CSV
- Daily/weekly/monthly analytics charts
- Item categories (Beverages, Snacks, Meals)
- Kot (Kitchen Order Ticket) printing
- Staff login with PIN
