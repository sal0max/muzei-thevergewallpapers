package de.salomax.muzei.thevergewallpapers

import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.logging.HttpLoggingInterceptor.Level
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import retrofit2.Call
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.create
import retrofit2.http.GET
import java.io.IOException
import java.lang.reflect.Type

internal interface TheVergeService {

   companion object {
      private fun createService(): TheVergeService {
         val client = OkHttpClient.Builder()
               .addInterceptor(HttpLoggingInterceptor().apply {
                  level = if (BuildConfig.DEBUG) Level.BASIC else Level.NONE
               })
               .build()

         return Retrofit.Builder()
               .baseUrl("https://www.theverge.com/pages/")
               .client(client)
               .addConverterFactory(TheVergeWallpaperAdapter.Factory)
               .build()
               .create()
      }

      @Throws(IOException::class)
      internal fun getWallpapers(): List<Wallpaper>? {
         return createService().getWallpapers().execute().body()
      }
   }

   @GET("wallpapers")
   fun getWallpapers(): Call<List<Wallpaper>?>


   data class Wallpaper(
         val url: String,
         val title: String,
//         val orientation: Int, // 0=portrait 1=landscape
         val dimensions: String?)

   class TheVergeWallpaperAdapter : Converter<ResponseBody?, List<Wallpaper>?> {

      companion object {
         val Factory: Converter.Factory = object : Converter.Factory() {
            override fun responseBodyConverter(type: Type, annotations: Array<Annotation?>?, retrofit: Retrofit?): Converter<ResponseBody?, *> {
               return TheVergeWallpaperAdapter()
            }
         }
      }

      override fun convert(value: ResponseBody?): List<Wallpaper> {
         val document: Document = Jsoup.parse(value?.string())
         return document.select("a[href~=\\.(png|jpg)]")
               .filter { element ->
                  element.text().contains("Portrait", true)
                        || element.text().contains("Landscape", true)
                        || element.text().contains("Square", true)
               }.map { element ->
                  val url = element.attr("href")
                  val dimensions = Regex("\\d+ x \\d+").find(element.text())?.value
                  val fileName = url
                        .substring(url.lastIndexOf("/") + 1)
                        .replace("_", " ", true)
                        .replace(".0", "")
                        .replace(".1", "", true)
                        .replace(".png", "", true)
                        .replace(".jpg", "", true)
                        .replace("wallpaper", "", true)
                        .replace("the", "", true)
                        .replace("verge", "", true)
                        .trim()
                  Wallpaper(url, fileName, dimensions)
               }
      }
   }

}
