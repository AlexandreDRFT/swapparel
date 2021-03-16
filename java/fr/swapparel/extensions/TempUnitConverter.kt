package fr.swapparel.extensions

object TempUnitConverter {

    @Throws(NumberFormatException::class)
    fun convertToCelsius(kelvin: String): Double {
        val inKelvin: Double
        try {
            inKelvin = java.lang.Double.parseDouble(kelvin)
        } catch (e: NumberFormatException) {
            throw e
        }

        return inKelvin - 273.15
    }

    @Throws(NumberFormatException::class)
    fun convertToFahrenheit(kelvin: String): Double {
        val inKelvin: Double
        try {
            inKelvin = java.lang.Double.parseDouble(kelvin)
        } catch (e: NumberFormatException) {
            throw e
        }

        return (inKelvin - 273.15) * 1.8000 + 32.00
    }
}
