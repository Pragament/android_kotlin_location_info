fun readPincodeData(context: Context): List<PincodeLocation> {
    val result = mutableListOf<PincodeLocation>()
    val inputStream = context.assets.open("pincode_with_lat_long.csv")
    inputStream.bufferedReader().useLines { lines ->
        lines.drop(1).forEach { line ->
            val tokens = line.split(",")
            if (tokens.size >= 3) {
                val pincode = tokens[0]
                val lat = tokens[1].toDoubleOrNull()
                val lon = tokens[2].toDoubleOrNull()
                if (lat != null && lon != null) {
                    result.add(PincodeLocation(pincode, lat, lon))
                }
            }
        }
    }
    return result
}
