package com.robogames.RoboCupMS.Business.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.robogames.RoboCupMS.Business.Enum.ECategory;
import com.robogames.RoboCupMS.Business.Enum.ECompetitionMode;
import com.robogames.RoboCupMS.Business.Enum.EMatchState;
import com.robogames.RoboCupMS.Business.Enum.ETournamentPhase;
import com.robogames.RoboCupMS.Business.Object.BracketPreviewDTO;
import com.robogames.RoboCupMS.Business.Object.GroupPreviewDTO;
import com.robogames.RoboCupMS.Business.Object.MatchPreviewDTO;
import com.robogames.RoboCupMS.Business.Object.RobotMatchObj;
import com.robogames.RoboCupMS.Business.Object.RobotPreviewDTO;
import com.robogames.RoboCupMS.Business.Object.TournamentGenerateRequestDTO;
import com.robogames.RoboCupMS.Business.Object.TournamentPreviewDTO;
import com.robogames.RoboCupMS.Business.Object.TournamentPreviewDTO.PlaygroundInfoDTO;
import com.robogames.RoboCupMS.Business.Object.TournamentSaveRequestDTO;
import com.robogames.RoboCupMS.Entity.Discipline;
import com.robogames.RoboCupMS.Entity.Playground;
import com.robogames.RoboCupMS.Entity.Robot;
import com.robogames.RoboCupMS.Entity.RobotMatch;
import com.robogames.RoboCupMS.Repository.DisciplineRepository;
import com.robogames.RoboCupMS.Repository.RobotRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for generating tournament structures (groups + bracket)
 */
@Service
public class TournamentGeneratorService {

    @Autowired
    private DisciplineRepository disciplineRepository;

    @Autowired
    private RobotRepository robotRepository;

    @Autowired
    private MatchService matchService;

    /**
     * Generate tournament structure preview (does not save anything)
     * 
     * @param request Generation parameters
     * @return Tournament preview structure
     * @throws Exception on validation errors
     */
    public TournamentPreviewDTO generatePreview(TournamentGenerateRequestDTO request) throws Exception {
        // Validate discipline
        Optional<Discipline> disciplineOpt = disciplineRepository.findById(request.getDisciplineId());
        if (!disciplineOpt.isPresent()) {
            throw new Exception("Discipline not found");
        }
        Discipline discipline = disciplineOpt.get();

        // Check if discipline supports tournament mode
        if (discipline.getCompetitionMode() == null || 
            discipline.getCompetitionMode().getName() != ECompetitionMode.TOURNAMENT) {
            throw new Exception("This discipline does not support tournament mode");
        }

        // Get robots for this discipline and category
        List<Robot> robots = getRobotsForDisciplineAndCategory(
            discipline.getID(), 
            request.getCategory(), 
            request.getYear()
        );

        if (robots.isEmpty()) {
            throw new Exception("No confirmed robots found for this discipline and category");
        }

        // Validate parameters
        int groupCount = request.getGroupCount() != null ? request.getGroupCount() : 4;
        int advancingPerGroup = request.getAdvancingPerGroup() != null ? request.getAdvancingPerGroup() : 2;
        int playgroundCount = request.getPlaygroundCount() != null ? request.getPlaygroundCount() : 1;
        int matchTimeMinutes = request.getMatchTimeMinutes() != null ? request.getMatchTimeMinutes() : 
            (discipline.getTime() / 60);
        int availableTimeMinutes = request.getAvailableTimeMinutes() != null ? 
            request.getAvailableTimeMinutes() : 180;

        // Calculate minimum robots per group (must be at least advancingPerGroup + 1)
        int minRobotsPerGroup = advancingPerGroup + 1;
        
        // Validate group count
        if (groupCount < 1) {
            throw new Exception("Group count must be at least 1");
        }
        if (robots.size() < groupCount * minRobotsPerGroup) {
            throw new Exception(String.format(
                "Not enough robots (%d) for %d groups with %d advancing. Need at least %d robots.",
                robots.size(), groupCount, advancingPerGroup, groupCount * minRobotsPerGroup));
        }

        // Build preview
        TournamentPreviewDTO preview = new TournamentPreviewDTO();
        preview.setDisciplineId(discipline.getID());
        preview.setDisciplineName(discipline.getName());
        preview.setCategory(request.getCategory());
        preview.setYear(request.getYear());
        preview.setPlaygroundCount(playgroundCount);

        // Get available playgrounds
        List<PlaygroundInfoDTO> availablePlaygrounds = discipline.getPlaygrounds().stream()
            .map(p -> new PlaygroundInfoDTO(p.getID(), p.getName(), p.getNumber()))
            .collect(Collectors.toList());
        preview.setAvailablePlaygrounds(availablePlaygrounds);

        // Generate groups with specified count
        List<GroupPreviewDTO> groups = generateGroups(robots, groupCount, advancingPerGroup, 
            discipline.getID(), request.getCategory(), request.getYear(), availablePlaygrounds);
        preview.setGroups(groups);

        // Calculate bracket size (number of advancing robots)
        int bracketSize = groups.size() * advancingPerGroup;
        
        // Generate bracket structure with bye handling
        BracketPreviewDTO bracket = generateBracketWithByes(bracketSize, 
            discipline.getID(), request.getCategory(), request.getYear(), availablePlaygrounds);
        preview.setBracket(bracket);

        // Calculate totals
        int totalGroupMatches = groups.stream()
            .mapToInt(g -> g.getMatches().size())
            .sum();
        int totalBracketMatches = bracket.getRounds().stream()
            .mapToInt(r -> r.getMatches().size())
            .sum();
        
        preview.setTotalGroupMatches(totalGroupMatches);
        preview.setTotalBracketMatches(totalBracketMatches);

        // Estimate time
        int totalMatches = totalGroupMatches + totalBracketMatches;
        int estimatedTime = (int) Math.ceil((double) totalMatches * matchTimeMinutes / playgroundCount);
        preview.setEstimatedTimeMinutes(estimatedTime);

        // Warnings
        List<String> warnings = new ArrayList<>();
        if (estimatedTime > availableTimeMinutes) {
            warnings.add(String.format(
                "Estimated time (%d min) exceeds available time (%d min). Consider adding more playgrounds or reducing group size.",
                estimatedTime, availableTimeMinutes));
        }
        if (availablePlaygrounds.isEmpty()) {
            warnings.add("No playgrounds defined for this discipline. Please create playgrounds first.");
        }
        preview.setWarnings(warnings);

        return preview;
    }

    /**
     * Get robots for specific discipline, category and year
     */
    private List<Robot> getRobotsForDisciplineAndCategory(Long disciplineId, ECategory category, Integer year) {
        return robotRepository.findAll().stream()
            .filter(r -> r.getDiscipline() != null && r.getDiscipline().getID().equals(disciplineId))
            .filter(r -> r.getConfirmed())
            .filter(r -> r.getTeamRegistration() != null)
            .filter(r -> r.getTeamRegistration().getCompetitionYear() == year)
            .filter(r -> r.getTeamRegistration().getCategory() == category)
            .collect(Collectors.toList());
    }

    /**
     * Generate groups with round-robin matches
     * @param robots List of all robots to distribute
     * @param groupCount Number of groups to create
     * @param advancingPerGroup Number of robots advancing from each group
     */
    private List<GroupPreviewDTO> generateGroups(
            List<Robot> robots, 
            int groupCount, 
            int advancingPerGroup,
            Long disciplineId,
            ECategory category,
            Integer year,
            List<PlaygroundInfoDTO> playgrounds) {
        
        List<GroupPreviewDTO> groups = new ArrayList<>();
        
        // Shuffle robots for random distribution
        List<Robot> shuffledRobots = new ArrayList<>(robots);
        Collections.shuffle(shuffledRobots);

        // Distribute robots evenly across the specified number of groups
        int baseSize = shuffledRobots.size() / groupCount;
        int extra = shuffledRobots.size() % groupCount;

        int robotIndex = 0;
        int matchIdCounter = 1;

        for (int g = 0; g < groupCount; g++) {
            GroupPreviewDTO group = new GroupPreviewDTO();
            String groupName = String.valueOf((char) ('A' + g));
            // Shorter group ID format: DISC_C_YEAR_A (e.g., ROBOS_H_2026_A)
            String groupId = String.format("%s_%s_%d_%s", 
                getDisciplineShortName(disciplineId), 
                getCategoryShortCode(category), 
                year, 
                groupName);
            
            group.setName(groupName);
            group.setGroupId(groupId);
            group.setAdvancingCount(advancingPerGroup);

            // Determine group size (distribute extra robots to first groups)
            int currentGroupSize = baseSize + (g < extra ? 1 : 0);
            
            // Get robots for this group
            List<RobotPreviewDTO> groupRobots = new ArrayList<>();
            for (int i = 0; i < currentGroupSize && robotIndex < shuffledRobots.size(); i++) {
                Robot r = shuffledRobots.get(robotIndex++);
                groupRobots.add(new RobotPreviewDTO(
                    r.getID(), 
                    r.getName(), 
                    r.getNumber(),
                    r.getTeamName()
                ));
            }
            group.setRobots(groupRobots);

            // Generate round-robin matches for this group
            List<MatchPreviewDTO> matches = new ArrayList<>();
            // Default playground - can be changed by frontend for each group
            Long playgroundId = playgrounds.isEmpty() ? null : playgrounds.get(g % playgrounds.size()).getId();
            group.setPlaygroundId(playgroundId);
            
            for (int i = 0; i < groupRobots.size(); i++) {
                for (int j = i + 1; j < groupRobots.size(); j++) {
                    MatchPreviewDTO match = new MatchPreviewDTO();
                    match.setTempId(String.format("GROUP_%s_M%d", groupName, matchIdCounter++));
                    match.setRobotA(groupRobots.get(i));
                    match.setRobotB(groupRobots.get(j));
                    match.setPhase(ETournamentPhase.GROUP_STAGE);
                    match.setGroup(groupId);
                    match.setPlaygroundId(playgroundId);
                    match.setRoundName(String.format("Group %s", groupName));
                    match.setMatchOrder(matches.size() + 1);
                    
                    // Set visual positions (simple grid layout)
                    match.setVisualX(g);
                    match.setVisualY(matches.size());
                    
                    matches.add(match);
                }
            }
            group.setMatches(matches);
            groups.add(group);
        }

        return groups;
    }

    /**
     * Generate bracket structure with bye handling for non-power-of-2 participant counts.
     * Some participants will get automatic advancement (bye) in the first round.
     */
    private BracketPreviewDTO generateBracketWithByes(
            int participantCount,
            Long disciplineId,
            ECategory category,
            Integer year,
            List<PlaygroundInfoDTO> playgrounds) {
        
        BracketPreviewDTO bracket = new BracketPreviewDTO();
        String bracketId = getBracketId(disciplineId, category, year);
        bracket.setBracketId(bracketId);
        bracket.setParticipantCount(participantCount);

        // Calculate bracket size (round up to nearest power of 2)
        int bracketSize = 1;
        while (bracketSize < participantCount) {
            bracketSize *= 2;
        }
        
        // Calculate number of byes (participants who advance automatically in round 1)
        int byeCount = bracketSize - participantCount;
        
        List<BracketPreviewDTO.BracketRoundDTO> rounds = new ArrayList<>();
        // Default playground for bracket - can be changed by frontend
        Long playgroundId = playgrounds.isEmpty() ? null : playgrounds.get(0).getId();
        bracket.setPlaygroundId(playgroundId);

        int currentRoundSize = bracketSize / 2;
        int roundNumber = 1;
        int matchIdCounter = 1;

        while (currentRoundSize >= 1) {
            BracketPreviewDTO.BracketRoundDTO round = new BracketPreviewDTO.BracketRoundDTO();
            round.setRoundNumber(roundNumber);
            
            // Determine round name
            String roundName;
            ETournamentPhase phase;
            if (currentRoundSize == 1) {
                roundName = "Finále";
                phase = ETournamentPhase.FINAL;
            } else if (currentRoundSize == 2) {
                roundName = "Semifinále";
                phase = ETournamentPhase.SEMIFINAL;
            } else if (currentRoundSize == 4) {
                roundName = "Čtvrtfinále";
                phase = ETournamentPhase.QUARTERFINAL;
            } else if (currentRoundSize == 8) {
                roundName = "Osmifinále";
                phase = ETournamentPhase.ROUND_OF_16;
            } else {
                roundName = String.format("Kolo %d", roundNumber);
                phase = ETournamentPhase.PRELIMINARY;
            }
            round.setName(roundName);

            List<MatchPreviewDTO> matches = new ArrayList<>();
            for (int i = 0; i < currentRoundSize; i++) {
                MatchPreviewDTO match = new MatchPreviewDTO();
                String tempId = String.format("BRACKET_R%d_M%d", roundNumber, matchIdCounter++);
                match.setTempId(tempId);
                match.setRobotA(null); // Will be filled when winners advance
                match.setRobotB(null);
                match.setPhase(phase);
                match.setGroup(bracketId);
                match.setPlaygroundId(playgroundId);
                match.setRoundName(roundName);
                match.setMatchOrder(i + 1);
                
                // In round 1, mark matches that are byes
                // Byes are distributed to top seeds (first matches in round 1)
                if (roundNumber == 1 && i < byeCount) {
                    match.setIsBye(true);
                }
                
                // Set visual positions for bracket visualization
                match.setVisualX(roundNumber - 1);
                match.setVisualY(i);

                matches.add(match);
            }
            round.setMatches(matches);
            rounds.add(round);

            currentRoundSize /= 2;
            roundNumber++;
        }

        // Fix nextMatchTempId references
        for (int r = 0; r < rounds.size() - 1; r++) {
            List<MatchPreviewDTO> currentMatches = rounds.get(r).getMatches();
            List<MatchPreviewDTO> nextMatches = rounds.get(r + 1).getMatches();
            
            for (int i = 0; i < currentMatches.size(); i++) {
                MatchPreviewDTO match = currentMatches.get(i);
                int nextMatchIndex = i / 2;
                if (nextMatchIndex < nextMatches.size()) {
                    match.setNextMatchTempId(nextMatches.get(nextMatchIndex).getTempId());
                }
            }
        }

        bracket.setRounds(rounds);
        bracket.setByeCount(byeCount);
        return bracket;
    }

    /**
     * Get short name for discipline (for group naming)
     */
    private String getDisciplineShortName(Long disciplineId) {
        Optional<Discipline> d = disciplineRepository.findById(disciplineId);
        if (d.isPresent()) {
            String name = d.get().getName().toUpperCase()
                .replaceAll("\\s+", "_")
                .replaceAll("[^A-Z0-9_]", "");
            // Return max 6 characters for shorter group IDs
            return name.length() > 6 ? name.substring(0, 6) : name;
        }
        return "D" + disciplineId;
    }

    /**
     * Get short category code (L for LOW_AGE, H for HIGH_AGE)
     */
    private String getCategoryShortCode(ECategory category) {
        return category == ECategory.LOW_AGE_CATEGORY ? "L" : "H";
    }

    /**
     * Generate group ID prefix for matching/searching groups
     * Format: DISC_C_YEAR_ (e.g., ROBOS_H_2026_)
     */
    private String getGroupPrefix(Long disciplineId, ECategory category, Integer year) {
        return String.format("%s_%s_%d_", 
            getDisciplineShortName(disciplineId), 
            getCategoryShortCode(category), 
            year);
    }

    /**
     * Generate bracket ID
     * Format: DISC_C_YEAR_BR (e.g., ROBOS_H_2026_BR)
     */
    private String getBracketId(Long disciplineId, ECategory category, Integer year) {
        return String.format("%s_%s_%d_BR", 
            getDisciplineShortName(disciplineId), 
            getCategoryShortCode(category), 
            year);
    }

    /**
     * Save tournament structure (creates actual matches in database)
     * 
     * @param request Tournament structure from frontend
     * @return Map of tempId -> actual match ID
     * @throws Exception on errors
     */
    @Transactional
    public Map<String, Long> saveTournamentStructure(TournamentSaveRequestDTO request) throws Exception {
        // Validate discipline
        Optional<Discipline> disciplineOpt = disciplineRepository.findById(request.getDisciplineId());
        if (!disciplineOpt.isPresent()) {
            throw new Exception("Discipline not found");
        }

        Map<String, Long> tempIdToMatchId = new HashMap<>();
        List<Playground> playgrounds = disciplineOpt.get().getPlaygrounds();
        
        if (playgrounds.isEmpty()) {
            throw new Exception("No playgrounds available for this discipline");
        }

        // First pass: Create all matches without nextMatch references
        // Create group matches
        for (GroupPreviewDTO group : request.getGroups()) {
            // Use group's playgroundId as fallback for matches
            Long groupPlaygroundId = group.getPlaygroundId() != null ? 
                group.getPlaygroundId() : playgrounds.get(0).getID();
            
            for (MatchPreviewDTO matchPreview : group.getMatches()) {
                RobotMatchObj matchObj = new RobotMatchObj();
                // Priority: match playgroundId > group playgroundId > first playground
                matchObj.setPlaygroundID(matchPreview.getPlaygroundId() != null ? 
                    matchPreview.getPlaygroundId() : groupPlaygroundId);
                matchObj.setPhase(matchPreview.getPhase());
                matchObj.setGroup(matchPreview.getGroup());
                matchObj.setVisualX(matchPreview.getVisualX());
                matchObj.setVisualY(matchPreview.getVisualY());
                matchObj.setCompetitionYear(request.getYear());
                
                if (matchPreview.getRobotA() != null && matchPreview.getRobotA().getId() != null) {
                    matchObj.setRobotAID(matchPreview.getRobotA().getId());
                }
                if (matchPreview.getRobotB() != null && matchPreview.getRobotB().getId() != null) {
                    matchObj.setRobotBID(matchPreview.getRobotB().getId());
                }

                RobotMatch match = matchService.create(matchObj);
                tempIdToMatchId.put(matchPreview.getTempId(), match.getID());
            }
        }

        // Create bracket matches
        if (request.getBracket() != null) {
            // Use bracket's playgroundId as fallback for bracket matches
            Long bracketPlaygroundId = request.getBracket().getPlaygroundId() != null ?
                request.getBracket().getPlaygroundId() : playgrounds.get(0).getID();
            
            for (BracketPreviewDTO.BracketRoundDTO round : request.getBracket().getRounds()) {
                for (MatchPreviewDTO matchPreview : round.getMatches()) {
                    RobotMatchObj matchObj = new RobotMatchObj();
                    // Priority: match playgroundId > bracket playgroundId > first playground
                    matchObj.setPlaygroundID(matchPreview.getPlaygroundId() != null ? 
                        matchPreview.getPlaygroundId() : bracketPlaygroundId);
                    matchObj.setPhase(matchPreview.getPhase());
                    matchObj.setGroup(matchPreview.getGroup());
                    matchObj.setVisualX(matchPreview.getVisualX());
                    matchObj.setVisualY(matchPreview.getVisualY());
                    matchObj.setCompetitionYear(request.getYear());
                    
                    // Bracket matches start without robots
                    matchObj.setRobotAID(null);
                    matchObj.setRobotBID(null);

                    RobotMatch match = matchService.create(matchObj);
                    tempIdToMatchId.put(matchPreview.getTempId(), match.getID());
                }
            }

            // Second pass: Update nextMatch references for bracket
            for (BracketPreviewDTO.BracketRoundDTO round : request.getBracket().getRounds()) {
                for (MatchPreviewDTO matchPreview : round.getMatches()) {
                    if (matchPreview.getNextMatchTempId() != null) {
                        Long matchId = tempIdToMatchId.get(matchPreview.getTempId());
                        Long nextMatchId = tempIdToMatchId.get(matchPreview.getNextMatchTempId());
                        
                        if (matchId != null && nextMatchId != null) {
                            RobotMatchObj updateObj = new RobotMatchObj();
                            updateObj.setNextMatchID(nextMatchId);
                            matchService.update(matchId, updateObj);
                        }
                    }
                }
            }
        }

        return tempIdToMatchId;
    }

    /**
     * Start the final bracket by moving winners from groups
     * 
     * @param disciplineId Discipline ID
     * @param category Category
     * @param year Competition year
     * @param advancingPerGroup Number of robots advancing from each group
     * @throws Exception on errors
     */
    @Transactional
    public void startFinalBracket(Long disciplineId, ECategory category, Integer year, Integer advancingPerGroup) throws Exception {
        // Get discipline
        Optional<Discipline> disciplineOpt = disciplineRepository.findById(disciplineId);
        if (!disciplineOpt.isPresent()) {
            throw new Exception("Discipline not found");
        }
        Discipline discipline = disciplineOpt.get();

        // Find all group matches for this discipline/category/year
        String groupPrefix = getGroupPrefix(disciplineId, category, year);
        String bracketId = getBracketId(disciplineId, category, year);

        // Get all matches
        List<RobotMatch> allMatches = matchService.allByYear(year);
        
        // Find group matches
        List<RobotMatch> groupMatches = allMatches.stream()
            .filter(m -> m.getGroup() != null && m.getGroup().startsWith(groupPrefix))
            .collect(Collectors.toList());

        // Calculate standings for each group
        Map<String, List<RobotStanding>> groupStandings = new HashMap<>();
        
        for (RobotMatch match : groupMatches) {
            String groupName = match.getGroup();
            groupStandings.computeIfAbsent(groupName, k -> new ArrayList<>());
            
            if (match.getState().getName() == EMatchState.DONE) {
                updateStandings(groupStandings.get(groupName), match, discipline.getHighScoreWin());
            }
        }

        // Get winners from each group
        List<Robot> winners = new ArrayList<>();
        for (Map.Entry<String, List<RobotStanding>> entry : groupStandings.entrySet()) {
            List<RobotStanding> standings = entry.getValue();
            // Sort by wins (descending), then by score difference
            standings.sort((a, b) -> {
                if (b.wins != a.wins) return b.wins - a.wins;
                return Float.compare(b.scoreDiff, a.scoreDiff);
            });
            
            // Take top N from each group
            int count = Math.min(advancingPerGroup, standings.size());
            for (int i = 0; i < count; i++) {
                winners.add(standings.get(i).robot);
            }
        }

        // Shuffle winners for random bracket placement
        Collections.shuffle(winners);

        // Find bracket matches (first round)
        List<RobotMatch> bracketMatches = allMatches.stream()
            .filter(m -> bracketId.equals(m.getGroup()))
            .filter(m -> m.getPhaseName() == ETournamentPhase.PRELIMINARY || 
                        m.getPhaseName() == ETournamentPhase.QUARTERFINAL)
            .filter(m -> m.getRobotA() == null || m.getRobotB() == null)
            .sorted((a, b) -> {
                int xCompare = Integer.compare(
                    a.getVisualX() != null ? a.getVisualX() : 0, 
                    b.getVisualX() != null ? b.getVisualX() : 0);
                if (xCompare != 0) return xCompare;
                return Integer.compare(
                    a.getVisualY() != null ? a.getVisualY() : 0, 
                    b.getVisualY() != null ? b.getVisualY() : 0);
            })
            .collect(Collectors.toList());

        // Assign winners to bracket
        int winnerIndex = 0;
        for (RobotMatch match : bracketMatches) {
            if (winnerIndex >= winners.size()) break;
            
            if (match.getRobotA() == null && winnerIndex < winners.size()) {
                matchService.assignRobots(match.getID(), winners.get(winnerIndex++).getID(), null);
            }
            if (match.getRobotB() == null && winnerIndex < winners.size()) {
                matchService.assignRobots(match.getID(), null, winners.get(winnerIndex++).getID());
            }
        }
    }

    /**
     * Helper class for tracking robot standings
     */
    private static class RobotStanding {
        Robot robot;
        int wins = 0;
        int losses = 0;
        float scoreDiff = 0;

        RobotStanding(Robot robot) {
            this.robot = robot;
        }
    }

    /**
     * Update standings based on match result
     */
    private void updateStandings(List<RobotStanding> standings, RobotMatch match, Boolean highScoreWin) {
        Robot robotA = match.getRobotA();
        Robot robotB = match.getRobotB();
        Float scoreA = match.getScoreA();
        Float scoreB = match.getScoreB();

        if (robotA == null || robotB == null || scoreA == null || scoreB == null) {
            return;
        }

        RobotStanding standingA = standings.stream()
            .filter(s -> s.robot.getID().equals(robotA.getID()))
            .findFirst()
            .orElseGet(() -> {
                RobotStanding s = new RobotStanding(robotA);
                standings.add(s);
                return s;
            });

        RobotStanding standingB = standings.stream()
            .filter(s -> s.robot.getID().equals(robotB.getID()))
            .findFirst()
            .orElseGet(() -> {
                RobotStanding s = new RobotStanding(robotB);
                standings.add(s);
                return s;
            });

        boolean aWins = highScoreWin ? scoreA > scoreB : scoreA < scoreB;
        
        if (aWins) {
            standingA.wins++;
            standingB.losses++;
        } else if (scoreA.equals(scoreB)) {
            // Draw - no change
        } else {
            standingB.wins++;
            standingA.losses++;
        }

        standingA.scoreDiff += (scoreA - scoreB);
        standingB.scoreDiff += (scoreB - scoreA);
    }

    /**
     * Get group standings for a specific group
     * 
     * @param groupId Group identifier
     * @param year Competition year
     * @return List of standings sorted by rank
     */
    public List<Map<String, Object>> getGroupStandings(String groupId, Integer year) throws Exception {
        List<RobotMatch> matches = matchService.getByGroup(year, groupId);
        
        if (matches.isEmpty()) {
            return new ArrayList<>();
        }

        // Determine highScoreWin from discipline
        Boolean highScoreWin = true;
        if (!matches.isEmpty() && matches.get(0).getPlayground() != null) {
            highScoreWin = matches.get(0).getPlayground().getDiscipline().getHighScoreWin();
        }

        List<RobotStanding> standings = new ArrayList<>();
        
        for (RobotMatch match : matches) {
            if (match.getState().getName() == EMatchState.DONE) {
                updateStandings(standings, match, highScoreWin);
            }
        }

        // Sort standings
        standings.sort((a, b) -> {
            if (b.wins != a.wins) return b.wins - a.wins;
            return Float.compare(b.scoreDiff, a.scoreDiff);
        });

        // Convert to response format
        List<Map<String, Object>> result = new ArrayList<>();
        int rank = 1;
        for (RobotStanding s : standings) {
            Map<String, Object> entry = new HashMap<>();
            entry.put("rank", rank++);
            entry.put("robotId", s.robot.getID());
            entry.put("robotName", s.robot.getName());
            entry.put("robotNumber", s.robot.getNumber());
            entry.put("teamName", s.robot.getTeamName());
            entry.put("wins", s.wins);
            entry.put("losses", s.losses);
            entry.put("scoreDiff", s.scoreDiff);
            result.add(entry);
        }

        return result;
    }

    /**
     * Check if tournament structure already exists
     */
    public boolean tournamentExists(Long disciplineId, ECategory category, Integer year) {
        String groupPrefix = getGroupPrefix(disciplineId, category, year);
        
        List<RobotMatch> matches = matchService.allByYear(year);
        return matches.stream()
            .anyMatch(m -> m.getGroup() != null && m.getGroup().startsWith(groupPrefix));
    }

    /**
     * Delete tournament structure
     */
    @Transactional
    public void deleteTournamentStructure(Long disciplineId, ECategory category, Integer year) throws Exception {
        String groupPrefix = getGroupPrefix(disciplineId, category, year);
        String bracketId = getBracketId(disciplineId, category, year);
        
        List<RobotMatch> matches = matchService.allByYear(year);
        
        for (RobotMatch match : matches) {
            if (match.getGroup() != null && 
                (match.getGroup().startsWith(groupPrefix) || match.getGroup().equals(bracketId))) {
                matchService.remove(match.getID());
            }
        }
    }

    /**
     * Get tournament status overview with groups, standings, and match progress
     * 
     * @param disciplineId Discipline ID
     * @param category Category
     * @param year Competition year
     * @param advancingPerGroup Number of robots advancing from each group
     * @return Map with tournament status information
     */
    public Map<String, Object> getTournamentStatus(Long disciplineId, ECategory category, Integer year, Integer advancingPerGroup) throws Exception {
        String groupPrefix = getGroupPrefix(disciplineId, category, year);
        String bracketId = getBracketId(disciplineId, category, year);
        
        List<RobotMatch> allMatches = matchService.allByYear(year);
        
        // Filter matches for this tournament
        List<RobotMatch> tournamentMatches = allMatches.stream()
            .filter(m -> m.getGroup() != null && 
                        (m.getGroup().startsWith(groupPrefix) || m.getGroup().equals(bracketId)))
            .collect(Collectors.toList());
        
        if (tournamentMatches.isEmpty()) {
            Map<String, Object> result = new HashMap<>();
            result.put("exists", false);
            return result;
        }

        // Determine highScoreWin from discipline
        Boolean highScoreWin = true;
        if (!tournamentMatches.isEmpty() && tournamentMatches.get(0).getPlayground() != null) {
            highScoreWin = tournamentMatches.get(0).getPlayground().getDiscipline().getHighScoreWin();
        }

        // Group matches by group name
        Map<String, List<RobotMatch>> matchesByGroup = tournamentMatches.stream()
            .filter(m -> !m.getGroup().equals(bracketId))
            .collect(Collectors.groupingBy(RobotMatch::getGroup));

        // Bracket matches
        List<RobotMatch> bracketMatches = tournamentMatches.stream()
            .filter(m -> m.getGroup().equals(bracketId))
            .collect(Collectors.toList());

        List<Map<String, Object>> groups = new ArrayList<>();
        int totalGroupMatches = 0;
        int completedGroupMatches = 0;

        for (Map.Entry<String, List<RobotMatch>> entry : matchesByGroup.entrySet()) {
            String groupId = entry.getKey();
            List<RobotMatch> groupMatches = entry.getValue();
            
            // Extract group letter from groupId (last character before any suffix)
            String groupName = groupId.substring(groupId.lastIndexOf('_') + 1);
            
            int total = groupMatches.size();
            int completed = (int) groupMatches.stream()
                .filter(m -> m.getState().getName() == EMatchState.DONE)
                .count();
            
            totalGroupMatches += total;
            completedGroupMatches += completed;

            // Calculate standings for this group
            List<RobotStanding> standings = new ArrayList<>();
            for (RobotMatch match : groupMatches) {
                if (match.getState().getName() == EMatchState.DONE) {
                    updateStandings(standings, match, highScoreWin);
                }
            }

            // Sort standings
            standings.sort((a, b) -> {
                if (b.wins != a.wins) return b.wins - a.wins;
                return Float.compare(b.scoreDiff, a.scoreDiff);
            });

            // Convert to response format with advancing flag
            List<Map<String, Object>> standingsList = new ArrayList<>();
            int rank = 1;
            for (RobotStanding s : standings) {
                Map<String, Object> standingEntry = new HashMap<>();
                standingEntry.put("rank", rank);
                standingEntry.put("robotId", s.robot.getID());
                standingEntry.put("robotName", s.robot.getName());
                standingEntry.put("robotNumber", s.robot.getNumber());
                standingEntry.put("teamName", s.robot.getTeamName());
                standingEntry.put("wins", s.wins);
                standingEntry.put("losses", s.losses);
                standingEntry.put("scoreDiff", s.scoreDiff);
                standingEntry.put("advancing", rank <= advancingPerGroup);
                standingsList.add(standingEntry);
                rank++;
            }

            Map<String, Object> groupInfo = new HashMap<>();
            groupInfo.put("groupId", groupId);
            groupInfo.put("groupName", groupName);
            groupInfo.put("totalMatches", total);
            groupInfo.put("completedMatches", completed);
            groupInfo.put("remainingMatches", total - completed);
            groupInfo.put("standings", standingsList);
            groups.add(groupInfo);
        }

        // Sort groups by name
        groups.sort((a, b) -> ((String) a.get("groupName")).compareTo((String) b.get("groupName")));

        // Bracket info
        int totalBracketMatches = bracketMatches.size();
        int completedBracketMatches = (int) bracketMatches.stream()
            .filter(m -> m.getState().getName() == EMatchState.DONE)
            .count();

        // Check if final bracket has started (any bracket match has robot assigned)
        boolean finalStarted = bracketMatches.stream()
            .anyMatch(m -> m.getRobotA() != null || m.getRobotB() != null);

        Map<String, Object> result = new HashMap<>();
        result.put("exists", true);
        result.put("groups", groups);
        result.put("totalGroupMatches", totalGroupMatches);
        result.put("completedGroupMatches", completedGroupMatches);
        result.put("remainingGroupMatches", totalGroupMatches - completedGroupMatches);
        result.put("totalBracketMatches", totalBracketMatches);
        result.put("completedBracketMatches", completedBracketMatches);
        result.put("remainingBracketMatches", totalBracketMatches - completedBracketMatches);
        result.put("advancingPerGroup", advancingPerGroup);
        result.put("finalStarted", finalStarted);

        return result;
    }
}
