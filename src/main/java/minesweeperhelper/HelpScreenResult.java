package minesweeperhelper;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.opencv.core.Mat;

public record HelpScreenResult(Mat image, Map<UUID, List<Board>> mapBoards) {
    
}
