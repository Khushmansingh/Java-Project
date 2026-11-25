package service;

import model.PointsEntry;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PointsTableService {
    private final Map<String, PointsEntry> entriesByTeamId = new HashMap<>();

    public PointsEntry getOrCreate(String teamId) {
        return entriesByTeamId.computeIfAbsent(teamId, PointsEntry::new);
    }

    public List<PointsEntry> sortedTable(Map<String, String> teamNameById) {
        return entriesByTeamId.values().stream()
                .sorted(Comparator.comparingInt(PointsEntry::getPoints).reversed()
                        .thenComparing(Comparator.comparingInt(PointsEntry::getScoreDifference).reversed())
                        .thenComparing(entry -> teamNameById.getOrDefault(entry.getTeamId(), ""), String.CASE_INSENSITIVE_ORDER))
                .collect(Collectors.toList());
    }

    public List<PointsEntry> findAll() { return new ArrayList<>(entriesByTeamId.values()); }
}
