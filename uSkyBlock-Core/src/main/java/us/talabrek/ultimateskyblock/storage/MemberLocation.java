package us.talabrek.ultimateskyblock.storage;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

/**
 * A world location (i.e. home etc.)
 */
@Entity
public class MemberLocation {
    @Column
    private String world;

    @Column
    private double x;

    @Column
    private double y;

    @Column
    private double z;

    @Column
    private double pitch;

    @Column
    private double yaw;

    @Enumerated(EnumType.STRING)
    private LocationType type;

    @ManyToOne
    @JoinColumn(name="uuid")
    private Member owner;

    @ManyToOne
    @JoinColumn(name="name")
    private Island island;
}
