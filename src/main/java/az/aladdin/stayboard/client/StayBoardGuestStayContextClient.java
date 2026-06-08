package az.aladdin.stayboard.client;

import az.aladdin.stayboard.config.FeignAuthConfig;
import az.aladdin.stayboard.model.response.GuestStayContextResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(
        name = "stay-board-guest-stay-context",
        url = "${url.stay-board}",
        configuration = FeignAuthConfig.class
)
public interface StayBoardGuestStayContextClient {

    @GetMapping("/v1/guest-portal/guest-auth/stay-context")
    GuestStayContextResponse getStayContext();
}
