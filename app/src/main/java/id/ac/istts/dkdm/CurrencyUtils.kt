package id.ac.istts.dkdm

import java.text.NumberFormat
import java.util.*

object CurrencyUtils {
    // Menambahkan method kedalam class Long
    // Sehingga dapat dipanggil langsung pada object dengan class Long

    fun Long.toRupiah():String{
        // digunakan untuk mengubah format angka menjadi format uang rupiah
        val numberFormat = NumberFormat.getCurrencyInstance(Locale("in","ID"))
        return numberFormat.format(this).substring(2).replace(",00", "")
    }
}