package assets.resources;

import enums.ResourceType;
import java.util.Map;

public record Reservation(String missionId, Map<ResourceType, Integer> amounts) {}