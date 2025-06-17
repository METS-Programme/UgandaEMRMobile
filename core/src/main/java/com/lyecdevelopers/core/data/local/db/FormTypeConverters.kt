package com.lyecdevelopers.core.data.local.db

import androidx.room.TypeConverter
import com.lyecdevelopers.core.model.o3.Meta
import com.lyecdevelopers.core.model.o3.Pages
import com.lyecdevelopers.core.model.o3.Programs
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

class FormTypeConverters {

    private val moshi: Moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()

    // ----- Meta -----
    @TypeConverter
    fun fromMeta(meta: Meta?): String? {
        return meta?.let { moshi.adapter(Meta::class.java).toJson(it) }
    }

    @TypeConverter
    fun toMeta(metaJson: String?): Meta? {
        return metaJson?.let { moshi.adapter(Meta::class.java).fromJson(it) }
    }

    // ----- List<Page> -----
    @TypeConverter
    fun fromPages(pages: List<Pages>?): String? {
        return pages?.let {
            val type = Types.newParameterizedType(List::class.java, Pages::class.java)
            moshi.adapter<List<Pages>>(type).toJson(it)
        }
    }

    @TypeConverter
    fun toPages(pagesJson: String?): List<Pages>? {
        return pagesJson?.let {
            val type = Types.newParameterizedType(List::class.java, Pages::class.java)
            moshi.adapter<List<Pages>>(type).fromJson(it)
        }
    }

    // ----- ConceptReferences -----
//    @TypeConverter
//    fun fromConceptReferences(concepts: Map<String, ConceptReference>?): String? {
//        return concepts?.let {
//            val type = Types.newParameterizedType(
//                Map::class.java, String::class.java, ConceptReference::class.java
//            )
//            moshi.adapter<Map<String, ConceptReference>>(type).toJson(it)
//        }
//    }
//
//    @TypeConverter
//    fun toConceptReferences(json: String?): Map<String, ConceptReference>? {
//        return json?.let {
//            val type = Types.newParameterizedType(
//                Map::class.java, String::class.java, ConceptReference::class.java
//            )
//            moshi.adapter<Map<String, ConceptReference>>(type).fromJson(it)
//        }
//    }

    // Optional: Programs
    @TypeConverter
    fun fromPrograms(programs: Programs?): String? {
        return programs?.let { moshi.adapter(Programs::class.java).toJson(it) }
    }

    @TypeConverter
    fun toPrograms(json: String?): Programs? {
        return json?.let { moshi.adapter(Programs::class.java).fromJson(it) }
    }
}

