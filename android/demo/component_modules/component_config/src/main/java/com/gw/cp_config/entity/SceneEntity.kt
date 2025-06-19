package com.gw.cp_config.entity

import com.jwkj.lib_json_kit.IJsonEntity
import java.io.Serializable

/**
 * Author: yanzheng@gwell.cc
 * Time: 2023/9/6 20:31
 * Description: SceneEntity
 */
data class SceneEntity(

    /**
     * "hans": ["看家防盗", "照顾老人", "看宝宝", "看宠物", "店铺", "车库", "仓库", "院子", "其他"],
     * "hant": ["看家防盜", "照顧老人", "看寶寶", "看寵物", "店鋪", "車庫", "倉庫", "庭院", "其他"],
     * "en":   ["Keep an eye on house and prevent theft", "Look after the elder", "Look after babies", "Look after pets", "Shop", "Garage", "Warehouse", "Courtyard", "Other"],
     * "vi":   ["Trang Chủ", "Ông già", "đứa bé", "vật nuôi", "cửa tiệm", "nhà để xe", "Kho", "sân", "khác"],
     * "th":   ["บ้าน", "ชายชรา", "ทารก", "สัตว์เลี้ยง", "ร้านค้า", "โรงรถ", "คลังสินค้า", "ลาน", "อื่น ๆ"]
     * "ko": ["우리집", "거실", "아기방", "애완 동물", "매장", "차고", "창고", "정원", "기타"],
     * 	"ja": ["ホーム", "老人", "赤ちゃん", "ペット", "ショップ", "ガレージ", "倉庫", "中庭", "その他"],
     * 	"id": ["Rumah", "Pria tua", "Bayi", "Membelai", "Toko", "Garasi", "Gudang", "Halaman", "lain"],
     * 	"android_in": ["Rumah", "Pria tua", "Bayi", "Membelai", "Toko", "Garasi", "Gudang", "Halaman", "lain"],
     * 	"ms": ["Rumah", "Orang tua", "Bayi", "Haiwan", "Kedai", "Garaj", "Gudang", "Halaman", "yang lain"]
     */
    val hans: List<String>? = null,
    val hant: List<String>? = null,
    val en: List<String>? = null,
    val vi: List<String>? = null,
    val th: List<String>? = null,
    val ko: List<String>? = null,
    val ja: List<String>? = null,
    val android_in: List<String>? = null,
    val ms: List<String>? = null,
    val id: List<String>? = null,


) : Serializable, IJsonEntity