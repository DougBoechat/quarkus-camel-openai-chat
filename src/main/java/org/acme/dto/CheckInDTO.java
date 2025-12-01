package org.acme.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class CheckInDTO {

    // Request DTO
    public static class CheckInRequest {
        private String userId;

        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate checkinDate;

        private String emotion;
        private Integer energyLevel;
        private Integer sleepQuality;
        private String notes;

        // Getters e Setters
        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        public LocalDate getCheckinDate() {
            return checkinDate;
        }

        public void setCheckinDate(LocalDate checkinDate) {
            this.checkinDate = checkinDate;
        }

        public String getEmotion() {
            return emotion;
        }

        public void setEmotion(String emotion) {
            this.emotion = emotion;
        }

        public Integer getEnergyLevel() {
            return energyLevel;
        }

        public void setEnergyLevel(Integer energyLevel) {
            this.energyLevel = energyLevel;
        }

        public Integer getSleepQuality() {
            return sleepQuality;
        }

        public void setSleepQuality(Integer sleepQuality) {
            this.sleepQuality = sleepQuality;
        }

        public String getNotes() {
            return notes;
        }

        public void setNotes(String notes) {
            this.notes = notes;
        }
    }

    // Stats DTO
    public static class CheckInStats {
        private Double averageEnergy;
        private Double averageSleep;
        private Integer totalCheckIns;
        private Integer streak;

        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate lastCheckIn;

        // Getters e Setters
        public Double getAverageEnergy() {
            return averageEnergy;
        }

        public void setAverageEnergy(Double averageEnergy) {
            this.averageEnergy = averageEnergy;
        }

        public Double getAverageSleep() {
            return averageSleep;
        }

        public void setAverageSleep(Double averageSleep) {
            this.averageSleep = averageSleep;
        }

        public Integer getTotalCheckIns() {
            return totalCheckIns;
        }

        public void setTotalCheckIns(Integer totalCheckIns) {
            this.totalCheckIns = totalCheckIns;
        }

        public Integer getStreak() {
            return streak;
        }

        public void setStreak(Integer streak) {
            this.streak = streak;
        }

        public LocalDate getLastCheckIn() {
            return lastCheckIn;
        }

        public void setLastCheckIn(LocalDate lastCheckIn) {
            this.lastCheckIn = lastCheckIn;
        }
    }
}