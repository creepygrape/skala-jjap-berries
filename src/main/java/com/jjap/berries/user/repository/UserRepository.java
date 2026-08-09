package com.jjap.berries.user.repository;

import com.jjap.berries.user.domain.User;
import com.jjap.berries.user.domain.UserRole;
import com.jjap.berries.user.domain.UserStatus;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, Long> {

  boolean existsByEmail(String email);

  boolean existsByNickname(String nickname);

  boolean existsByNicknameAndIdNot(String nickname, Long id);

  Optional<User> findByEmail(String email);

  @Query(
      """
      select account from User account
      where (:role is null or account.role = :role)
        and (:status is null or account.status = :status)
        and (
          :keyword is null
          or lower(account.email) like lower(concat('%', :keyword, '%'))
          or lower(account.nickname) like lower(concat('%', :keyword, '%'))
        )
      """)
  Page<User> search(
      @Param("role") UserRole role,
      @Param("status") UserStatus status,
      @Param("keyword") String keyword,
      Pageable pageable);
}
