package com.learning.database.softdelete.repository;

import com.learning.database.softdelete.entity.NoteEntity;

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

    /**
     * Finds all non-deleted notes whose title contains the given keyword, case-insensitively.
     * Derived query: {@code WHERE title LIKE %keyword% AND deleted = false}, the
     * deleted-filter is applied automatically via {@code @SoftDelete}.
     *
     * @param keyword substring to search for within note titles, case-insensitive
     * @return list of matching notes, empty if none found
     */
    List<NoteEntity> findByTitleContainingIgnoreCase(String keyword);
}
