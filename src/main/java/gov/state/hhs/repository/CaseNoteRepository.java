package gov.state.hhs.repository;

import gov.state.hhs.model.CaseNote;
import javax.ejb.Stateless;

/**
 * Data access for CaseNote entities.
 * Notes are always inserted directly via persist() rather than
 * cascaded through the parent ServiceApplication, to avoid
 * primary key conflicts with H2's auto-increment sequence.
 */
@Stateless
public class CaseNoteRepository extends BaseRepository<CaseNote, Long> {

    public CaseNoteRepository() {
        super(CaseNote.class);
    }

    public CaseNote saveNote(CaseNote note) {
        em.persist(note);
        em.flush();
        return note;
    }
}
