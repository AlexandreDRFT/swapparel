package fr.swapparel.temp

class MigrationApparel {

    /////////////////////
    //Chiffre de gauche : l'index MINIMUM, soit la température MAXIMUM
    //Chiffre de droite : la distance , donc eg. 2 et maxTemp=0; la fringue va de 30°+ jusqu'à 2-0=2, donc cat.2, donc 10-20°
    fun checkIfMigrated(i: Int): Boolean { //false if not migrated (has only one number)
        val length = if (i != 0)
            (Math.log10(i.toDouble()) + 1).toInt()
        else
            1
        return length == 2
    }

    fun migrateHeaviness(i: Int): Int {
        //If the max value was a ZERO, we can't put it first so we put NINE !!!!!!!!
        return if (i != 0)
            Integer.valueOf(i.toString() + (1).toString())
        else
            Integer.valueOf(9.toString() + (1).toString())
    }

    fun isTargetInRange(targetHeaviness: Int, heaviness: Int): Boolean {
        return getMinTempFromHeaviness(targetHeaviness) <= getMinTempFromHeaviness(heaviness) && getMaxTempFromHeaviness(targetHeaviness) >= getMaxTempFromHeaviness(heaviness)
    }

    fun hasCollisionToHeaviness(h1: Int, h2: Int): Boolean {
        var hasCollision = false
        var index = getMaxTempFromHeaviness(h1)
        while (!hasCollision && index <= getMinTempFromHeaviness(h1)) {
            hasCollision = isTargetInRange(index, h2)
            index++
        }
        return hasCollision
    }

    fun getMaxTempFromHeaviness(i: Int): Int { //Retourne l'INDEX minimum soit la temperature MAXIMUM
            val result = Integer.parseInt(Integer.toString(i).substring(0, 1))
            return if (result == 9)
                0
            else
                result
    }

    fun getMinTempFromHeaviness(i: Int): Int { //Retourne la minTemperature
        //On vérifie si les changements n'ont pas eu le temps d'être ajoutés
        val distance = if (checkIfMigrated(i))
            Integer.parseInt(Integer.toString(i).substring(1, 2))
        else
            Integer.parseInt(Integer.toString(migrateHeaviness(i)).substring(1, 2))
        return getMaxTempFromHeaviness(i) + distance
    }

    fun convertDistanceToHeaviness(maxTemp: Int, distance: Int): Int {
        return if (maxTemp != 0)
            Integer.valueOf(maxTemp.toString() + distance.toString())
        else
            Integer.valueOf(9.toString() + distance.toString())
    }

    fun convertOptimumsToHeaviness(maxTemp: Int, minTemp: Int): Int {
        return if (maxTemp != 0)
            Integer.valueOf(maxTemp.toString() + Math.abs(minTemp - maxTemp))
        else
            Integer.valueOf(9.toString() + Math.abs(minTemp - maxTemp))
    }
}