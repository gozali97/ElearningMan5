package com.example.elearningman5

import android.annotation.SuppressLint
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs

class FormatWaktu {
}

@SuppressLint("SimpleDateFormat")
fun String?.String2Date(formatDate: String): Date? {
    try {
        return this?.let { SimpleDateFormat(formatDate).parse(it) }
    } catch (e: ParseException) {
        e.printStackTrace()
    }

    return null
}

// Method menghitung selisih dua waktu source: https://firmanhid.wordpress.com/2014/04/06/menghitung-selisih-tanggal-dan-waktu-dalam-java/
fun SelisihDateTime(waktuSatu: Date, waktuDua: Date): String {

    val selisihMS = abs(waktuSatu.time - waktuDua.time)
    val selisihDetik = selisihMS / 1000 % 60
    val selisihMenit = selisihMS / (60 * 1000) % 60
    val selisihJam = selisihMS / (60 * 60 * 1000) % 24
    val selisihHari = selisihMS / (24 * 60 * 60 * 1000)

    if (selisihHari.toInt() != 0) {
        return "$selisihHari Hari"
    } else if (selisihJam.toInt() != 0)
        return "$selisihJam Jam"

    return "$selisihMenit Menit $selisihDetik detik"
}