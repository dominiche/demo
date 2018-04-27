package dominic.demo.fabric.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class BlockInfoDTO {

    private Long blockNumber;

    private String currentBlockHash;

    private String previousBlockHash;

    private List<EnvelopeInfoDTO> envelopeInfoDTOList;

    private List<String> blockContexts;
}
