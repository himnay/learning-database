package com.learning.database.repository;

import com.learning.database.entity.softdelete.NoteEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @SoftDelete on NoteEntity means:
 *   findAll()        → SELECT ... WHERE deleted = false (automatic)
 *   deleteById(id)   → UPDATE note SET deleted = true WHERE id = ?
 * No enableFilter(), no @SQLDelete SQL string, no deleted field on the entity.
 */
@Repository
public interface NoteRepository extends JpaRepository<NoteEntity, Long> {

    List<NoteEntity> findByTitleContainingIgnoreCase(String keyword);
}
