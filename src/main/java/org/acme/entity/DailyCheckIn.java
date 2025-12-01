package org.acme.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "daily_checkin")
public class DailyCheckIn extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "checkin_date", nullable = false)
    private LocalDate checkinDate;

    @Column(name = "emotion")
    private String emotion; // Uma única emoção

    @Column(name = "energy_level", nullable = false)
    private Integer energyLevel; // 1-10

    @Column(name = "sleep_quality", nullable = false)
    private Integer sleepQuality; // 1-10

    @Column(name = "notes", length = 1000)
    private String notes;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Métodos de consulta customizados
    public static DailyCheckIn findByUserIdAndDate(String userId, LocalDate date) {
        return find("userId = ?1 and checkinDate = ?2", userId, date).firstResult();
    }

    public static List<DailyCheckIn> findByUserId(String userId) {
        return find("userId = ?1 order by checkinDate desc", userId).list();
    }

    public static List<DailyCheckIn> findByUserIdLastDays(String userId, int days) {
        LocalDate startDate = LocalDate.now().minusDays(days);
        return find("userId = ?1 and checkinDate >= ?2 order by checkinDate desc", userId, startDate).list();
    }

    public static List<DailyCheckIn> findByUserIdBetweenDates(String userId, LocalDate startDate, LocalDate endDate) {
        return find("userId = ?1 and checkinDate between ?2 and ?3 order by checkinDate desc",
                userId, startDate, endDate).list();
    }

    public static boolean hasCheckInToday(String userId) {
        return findByUserIdAndDate(userId, LocalDate.now()) != null;
    }

    // Getters e Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}