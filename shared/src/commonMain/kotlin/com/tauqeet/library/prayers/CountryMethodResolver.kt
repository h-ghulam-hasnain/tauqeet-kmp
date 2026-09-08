package com.tauqeet.library.prayers

object CountryMethodResolver {
    fun resolve(countryCode: String?): CalculationMethod {
        if (countryCode == null || countryCode.trim().isEmpty()) return CalculationMethod.MWL
        return when (countryCode.trim().uppercase()) {
            "PK", "IN", "BD", "AF" -> CalculationMethod.KARACHI
            "EG", "SD", "LY", "SY", "LB", "JO", "PS" -> CalculationMethod.EGYPT
            "SA" -> CalculationMethod.UMM_AL_QURA
            "AE" -> CalculationMethod.DUBAI
            "QA" -> CalculationMethod.QATAR
            "KW" -> CalculationMethod.KUWAIT
            "BH", "OM" -> CalculationMethod.GULF
            "US", "CA" -> CalculationMethod.ISNA
            "TR" -> CalculationMethod.TURKEY
            "ID" -> CalculationMethod.KEMENAG
            "MY" -> CalculationMethod.JAKIM
            "SG", "BN" -> CalculationMethod.SINGAPORE
            "FR" -> CalculationMethod.FRANCE_12
            "RU" -> CalculationMethod.RUSSIA
            "MA", "DZ", "TN" -> CalculationMethod.NORTH_AFRICA
            "GB", "UK" -> CalculationMethod.UK_UNIFIED
            else -> CalculationMethod.MWL
        }
    }
}
