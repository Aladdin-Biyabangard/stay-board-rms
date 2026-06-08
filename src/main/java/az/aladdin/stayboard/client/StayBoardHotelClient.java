package az.aladdin.stayboard.client;

import az.aladdin.stayboard.config.FeignAuthConfig;
import az.aladdin.stayboard.model.response.HotelTimezoneResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(
        name = "stay-board-hotel",
        url = "${url.stay-board}",
        configuration = FeignAuthConfig.class
)
public interface StayBoardHotelClient {

    @GetMapping("/v1/hotels/current")
    HotelTimezoneResponse getCurrentHotel();

    @GetMapping("/v1/hotels/{id}")
    HotelTimezoneResponse getHotel(@PathVariable Long id);
}
