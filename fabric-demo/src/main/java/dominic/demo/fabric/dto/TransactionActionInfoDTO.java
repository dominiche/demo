package dominic.demo.fabric.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class TransactionActionInfoDTO {

    private int responseStatus;
    private String responseMessage;
    //endorsement info

    private List<String> chainCodeInputArgs;
    private String txId;
    private String chaincodeName;

//    private List<String> txReadWriteSets;
}
