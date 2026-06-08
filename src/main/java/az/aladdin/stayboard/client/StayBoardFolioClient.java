package az.aladdin.stayboard.client;

import az.aladdin.stayboard.config.FeignAuthConfig;
import az.aladdin.stayboard.model.request.folio.AddFolioChargeRequest;
import az.aladdin.stayboard.model.request.folio.VoidFolioChargeRequest;
import az.aladdin.stayboard.model.response.folio.FolioChargeResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(
        name = "stay-board-folio",
        url = "${url.stay-board}",
        configuration = FeignAuthConfig.class
)
public interface StayBoardFolioClient {

    @PostMapping("/v1/folios/charges/by-room")
    FolioChargeResponse addChargeByRoom(
            @RequestParam("roomNumber") String roomNumber,
            @RequestBody AddFolioChargeRequest request
    );

    @PostMapping("/v1/folios/{chargeId}/void")
    void voidCharge(
            @PathVariable("chargeId") Long chargeId,
            @RequestBody VoidFolioChargeRequest request
    );
}
