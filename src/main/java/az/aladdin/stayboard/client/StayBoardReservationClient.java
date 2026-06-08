package az.aladdin.stayboard.client;

import az.aladdin.stayboard.config.FeignAuthConfig;
import az.aladdin.stayboard.model.response.ReservationDetailResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(
        name = "stay-board",
        url = "${url.stay-board}",
        configuration = FeignAuthConfig.class
)
public interface StayBoardReservationClient {

    @GetMapping("/v1/reservations/{id}")
    ReservationDetailResponse getReservation(@PathVariable Long id);
}
