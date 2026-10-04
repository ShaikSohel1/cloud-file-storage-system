package com.sohel.cloudstorage.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import com.sohel.cloudstorage.entity.TeamEntity;
import com.sohel.cloudstorage.entity.TeamMemberEntity;
import com.sohel.cloudstorage.entity.UserEntity;

public interface TeamMemberRepository extends JpaRepository<TeamMemberEntity, UUID> {
    List<TeamMemberEntity> findByTeam(TeamEntity team);
    List<TeamMemberEntity> findByUser(UserEntity user);
    Optional<TeamMemberEntity> findByTeamAndUser(TeamEntity team, UserEntity user);
}
