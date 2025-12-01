package org.acme.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.acme.dto.CheckInDTO;
import org.acme.dto.CheckInResponseDTO;
import org.acme.entity.DailyCheckIn;
import org.jboss.logging.Logger;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class CheckInService {

    private static final Logger LOG = Logger.getLogger(CheckInService.class);

    @Inject
    ObjectMapper objectMapper;

    @Transactional
    public CheckInResponseDTO createOrUpdateCheckIn(CheckInDTO.CheckInRequest request) {
        LOG.infof("Creating/Updating check-in for user %s on date %s", request.getUserId(), request.getCheckinDate());

        // Verificar se já existe check-in para esse dia
        DailyCheckIn checkIn = DailyCheckIn.findByUserIdAndDate(request.getUserId(), request.getCheckinDate());

        if (checkIn == null) {
            checkIn = new DailyCheckIn();
            checkIn.setUserId(request.getUserId());
            checkIn.setCheckinDate(request.getCheckinDate());
        }

        // Atualizar dados
        checkIn.setEmotion(request.getEmotion());
        checkIn.setEnergyLevel(request.getEnergyLevel());
        checkIn.setSleepQuality(request.getSleepQuality());
        checkIn.setNotes(request.getNotes());

        checkIn.persist();

        return toResponse(checkIn);
    }

    public CheckInResponseDTO getTodayCheckIn(String userId) {
        DailyCheckIn checkIn = DailyCheckIn.findByUserIdAndDate(userId, LocalDate.now());
        return checkIn != null ? toResponse(checkIn) : null;
    }

    public CheckInResponseDTO getCheckInByDate(String userId, LocalDate date) {
        DailyCheckIn checkIn = DailyCheckIn.findByUserIdAndDate(userId, date);
        return checkIn != null ? toResponse(checkIn) : null;
    }

    @Transactional
    public List<CheckInResponseDTO> getCheckInHistory(String userId, Integer days) {
        List<DailyCheckIn> checkIns;

        if (days != null && days > 0) {
            checkIns = DailyCheckIn.findByUserIdLastDays(userId, days);
        } else {
            checkIns = DailyCheckIn.findByUserId(userId);
        }

        return checkIns.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public CheckInDTO.CheckInStats getCheckInStats(String userId, Integer days) {
        List<DailyCheckIn> checkIns = days != null && days > 0
                ? DailyCheckIn.findByUserIdLastDays(userId, days)
                : DailyCheckIn.findByUserId(userId);

        CheckInDTO.CheckInStats stats = new CheckInDTO.CheckInStats();

        if (checkIns.isEmpty()) {
            return stats;
        }

        // Calcular médias
        double sumEnergy = 0, sumSleep = 0;
        int count = 0;

        for (DailyCheckIn checkIn : checkIns) {
            sumEnergy += checkIn.getEnergyLevel();
            sumSleep += checkIn.getSleepQuality();
            count++;
        }

        stats.setAverageEnergy(Math.round(sumEnergy / count * 10.0) / 10.0);
        stats.setAverageSleep(Math.round(sumSleep / count * 10.0) / 10.0);
        stats.setTotalCheckIns(count);
        stats.setLastCheckIn(checkIns.get(0).getCheckinDate());

        // Calcular streak (dias consecutivos)
        stats.setStreak(calculateStreak(checkIns));

        return stats;
    }

    private Integer calculateStreak(List<DailyCheckIn> checkIns) {
        if (checkIns.isEmpty()) {
            return 0;
        }

        int streak = 0;
        LocalDate expectedDate = LocalDate.now();

        for (DailyCheckIn checkIn : checkIns) {
            if (checkIn.getCheckinDate().equals(expectedDate)) {
                streak++;
                expectedDate = expectedDate.minusDays(1);
            } else {
                break;
            }
        }

        return streak;
    }

    @Transactional
    public boolean deleteCheckIn(String userId, LocalDate date) {
        DailyCheckIn checkIn = DailyCheckIn.findByUserIdAndDate(userId, date);
        if (checkIn != null) {
            checkIn.delete();
            return true;
        }
        return false;
    }

    private CheckInResponseDTO toResponse(DailyCheckIn checkIn) {
        CheckInResponseDTO response = new CheckInResponseDTO();

        response.setId(checkIn.getId());
        response.setUserId(checkIn.getUserId());
        response.setCheckinDate(checkIn.getCheckinDate());
        response.setEmotion(checkIn.getEmotion());
        response.setEnergyLevel(checkIn.getEnergyLevel());
        response.setSleepQuality(checkIn.getSleepQuality());
        response.setNotes(checkIn.getNotes());
        response.setCreatedAt(checkIn.getCreatedAt());
        response.setUpdatedAt(checkIn.getUpdatedAt());

        return response;
    }
}