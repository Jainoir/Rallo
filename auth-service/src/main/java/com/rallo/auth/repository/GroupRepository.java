package com.rallo.auth.repository;

import com.rallo.auth.model.Group;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GroupRepository extends JpaRepository<Group, String> {
}
