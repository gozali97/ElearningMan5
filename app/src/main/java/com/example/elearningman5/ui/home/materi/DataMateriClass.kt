package com.example.elearningman5.ui.home.materi

class DataMateriClass (
    private var id_materi: String?,
    private var jadwal_id: String?,
    private var nama_materi: String?,
    private var deskripsi: String?,
    private var path_file: String?,
    private var id_tugas: String?,
    private var created_at: String?,
//    private var dataImage: String?
) {
    fun getKeyMateri(): String? {
        return id_materi
    }

    fun getKeyNotif(): String? {
        return jadwal_id
    }

    fun getKeyTugas(): String? {
        return id_tugas
    }

    fun getMateri(): String? {
        return nama_materi
    }

    fun getDesc(): String? {
        return deskripsi
    }

    fun getFile(): String? {
        return path_file
    }

    fun getWaktu(): String? {
        return created_at
    }

//    fun getDataImage(): String? {
//        return dataImage
//    }
}