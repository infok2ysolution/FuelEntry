package com.example.model

enum class UserRole(val displayName: String, val badge: String) {
    EMPLOYEE("Employee / User", "User View (Own Data Only)"),
    ADMIN_FINANCE("Admin / Finance", "Admin Full Access (Company Data)")
}

data class UserProfile(
    val id: String,
    val name: String,
    val role: UserRole,
    val department: String,
    val defaultVehicleName: String? = null
)

object AppUsers {
    val ADMIN_USER = UserProfile(
        id = "admin_01",
        name = "Corporate Finance & Admin",
        role = UserRole.ADMIN_FINANCE,
        department = "Finance & Fleet Accounts"
    )

    val EMPLOYEES = listOf(
        UserProfile("emp_01", "Ahmed Khan (Field Officer)", UserRole.EMPLOYEE, "Field Operations", "Honda CD 70 (Company Bike)"),
        UserProfile("emp_02", "Bilal Tariq (Dispatch)", UserRole.EMPLOYEE, "Logistics & Dispatch", "Yamaha YBR 125 (Courier Bike)"),
        UserProfile("emp_03", "Usman Farooq (Area Manager)", UserRole.EMPLOYEE, "Sales & Marketing", "Suzuki Alto 660cc (Sales Car)"),
        UserProfile("emp_04", "Zainab Malik (Operations)", UserRole.EMPLOYEE, "Operations", "Toyota Corolla GLi (Executive)"),
        UserProfile("emp_05", "Rashid Mehmood (Logistics)", UserRole.EMPLOYEE, "Supply Chain", "Suzuki Bolan (Delivery Van)")
    )

    val ALL_USERS = listOf(ADMIN_USER) + EMPLOYEES
}

enum class PaymentMethod(val displayName: String, val iconName: String) {
    BANK_TRANSFER("Online Bank Transfer (1Link / IBFT)", "AccountBalance"),
    EASYPAISA_JAZZCASH("JazzCash / Easypaisa Mobile Wallet", "PhoneAndroid"),
    CASH_VOUCHER("Petty Cash / Company Voucher", "Payments"),
    COMPANY_CHEQUE("Company Crossed Cheque", "ReceiptLong")
}

enum class FuelType(val displayName: String, val shortCode: String, val defaultBaseRatePkr: Double) {
    PETROL("Petrol / Super 92", "Petrol", 260.60),
    DIESEL("High Speed Diesel (HSD)", "Diesel", 265.80),
    HOBC("Hi-Octane / HOBC", "HOBC", 285.50);

    companion object {
        fun fromString(type: String): FuelType {
            return entries.firstOrNull {
                it.name.equals(type, ignoreCase = true) ||
                it.shortCode.equals(type, ignoreCase = true) ||
                it.displayName.contains(type, ignoreCase = true)
            } ?: PETROL
        }
    }
}

data class RouteStop(
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String,
    val distanceKm: Double,
    val notes: String = ""
)

data class CityDestinationPreset(
    val cityName: String,
    val landmark: String,
    val standardDistanceKm: Double
)

object PakistanPresets {
    val commonKarachiStops = listOf(
        CityDestinationPreset("Karachi", "Head Office (I.I. Chundrigar)", 0.0),
        CityDestinationPreset("Karachi", "Clifton / DHA Office Hub", 8.5),
        CityDestinationPreset("Karachi", "Port Qasim Industrial Area", 38.0),
        CityDestinationPreset("Karachi", "Korangi Industrial Sector", 19.5),
        CityDestinationPreset("Karachi", "S.I.T.E. Industrial Area", 14.0),
        CityDestinationPreset("Karachi", "North Nazimabad Branch", 16.2),
        CityDestinationPreset("Karachi", "Jinnah International Cargo Terminal", 22.0)
    )

    val commonLahoreStops = listOf(
        CityDestinationPreset("Lahore", "Gulberg Corporate Center", 0.0),
        CityDestinationPreset("Lahore", "Mall Road Regional Office", 7.0),
        CityDestinationPreset("Lahore", "Sundar Industrial Estate", 32.0),
        CityDestinationPreset("Lahore", "DHA Phase 5 Tech Hub", 14.5),
        CityDestinationPreset("Lahore", "Allama Iqbal Airport Terminal", 18.0),
        CityDestinationPreset("Lahore", "Multan Road Warehouse", 21.0)
    )

    val commonIslamabadStops = listOf(
        CityDestinationPreset("Islamabad", "Blue Area Corporate Tower", 0.0),
        CityDestinationPreset("Islamabad", "F-8 Markaz Branch", 6.5),
        CityDestinationPreset("Islamabad", "I-9 Industrial Area", 9.0),
        CityDestinationPreset("Islamabad", "Islamabad Airport Hub", 35.0),
        CityDestinationPreset("Rawalpindi", "Saddar Regional Branch", 16.0),
        CityDestinationPreset("Rawalpindi", "Chaklala Scheme III Site", 20.5)
    )

    val intercityPresets = listOf(
        CityDestinationPreset("Intercity", "Karachi to Hyderabad", 165.0),
        CityDestinationPreset("Intercity", "Lahore to Faisalabad", 185.0),
        CityDestinationPreset("Intercity", "Lahore to Gujranwala", 75.0),
        CityDestinationPreset("Intercity", "Islamabad to Peshawar", 185.0),
        CityDestinationPreset("Intercity", "Islamabad to Rawalpindi", 20.0),
        CityDestinationPreset("Intercity", "Multan to Khanewal", 48.0)
    )
}
