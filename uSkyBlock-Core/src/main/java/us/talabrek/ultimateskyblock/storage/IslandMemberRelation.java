package us.talabrek.ultimateskyblock.storage;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;

/**
 * A relation between an island and a member
 */
@Entity
public class IslandMemberRelation {
    @ManyToMany
    @JoinColumn(name="name")
    private Island island;

    @ManyToMany
    @JoinColumn(name="uuid")
    private Member member;

    @Column
    @Enumerated(EnumType.STRING)
    private MemberRelationType type;
}
