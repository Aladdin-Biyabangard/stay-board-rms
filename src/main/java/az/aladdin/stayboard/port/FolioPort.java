package az.aladdin.stayboard.port;

import az.aladdin.stayboard.model.request.folio.AddFolioChargeRequest;
import az.aladdin.stayboard.model.request.folio.VoidFolioChargeRequest;
import az.aladdin.stayboard.model.response.folio.FolioChargeResponse;

public interface FolioPort {

    FolioChargeResponse addChargeByRoom(String roomNumber, AddFolioChargeRequest request);

    void voidCharge(Long chargeId, VoidFolioChargeRequest request);
}
