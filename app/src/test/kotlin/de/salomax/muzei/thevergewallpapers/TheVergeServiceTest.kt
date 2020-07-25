package de.salomax.muzei.thevergewallpapers

import de.salomax.muzei.thevergewallpapers.TheVergeService.*
import org.junit.Test

import org.junit.Assert.*

class TheVergeServiceTest {

   @Test
   fun testService() {
      val result = TheVergeService.getWallpapers()

      // check that at least some wallpapers can be found
      assertNotNull(result)
      assertTrue(result!!.isNotEmpty())

      // check some known wallpapers
      assertTrue(result.contains(
            Wallpaper(
                  url = "https://cdn.vox-cdn.com/uploads/chorus_asset/file/10675413/The_Verge_Seismic_Wallpaper_Portrait.0.png",
                  title = "Seismic  Portrait",
                  dimensions = "2160 x 3840"
            ))
      )
      assertTrue(result.contains(
            Wallpaper(
                  url = "https://cdn.vox-cdn.com/uploads/chorus_asset/file/19299326/Pixel4_Wallpaper.0.jpg",
                  title = "Pixel4",
                  dimensions = "2160 x 3840"
            ))
      )
      assertTrue(result.contains(
            Wallpaper(
                  url = "https://cdn.vox-cdn.com/uploads/chorus_asset/file/11369233/The_Verge_Processor_Portrait_02.0.jpg",
                  title = "Processor Portrait 02",
                  dimensions = "2160 x 3840"
            ))
      )

      // print all results
      //result.forEach(::println)
   }

}
