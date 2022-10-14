package minesweeperhelper;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public record ProcessingData(Map<UUID, Map<BigDecimal, Map<BigDecimal, Map<BigDecimal, List<Grid>>>>> map,
                List<ScreenShotArea> listScreenShotAreas) {

}
