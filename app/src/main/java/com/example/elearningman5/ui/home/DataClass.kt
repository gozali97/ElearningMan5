package com.example.elearningman5.ui.home

class DataClass(
    private var kode_jadwal: String?,
    private var nama_kelas: String?,
    private var nama_mapel: String?,
    private var name: String?,
//    private var dataImage: String?
) {

    fun getKey(): String? {
        return kode_jadwal
    }

    fun getKelas(): String? {
        return nama_kelas
    }

    fun getMapel(): String? {
        return nama_mapel
    }

    fun getGuru(): String? {
        return name
    }

//    fun getDataImage(): String? {
//        return dataImage
//    }
}