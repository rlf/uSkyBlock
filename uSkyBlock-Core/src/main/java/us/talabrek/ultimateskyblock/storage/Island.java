package us.talabrek.ultimateskyblock.storage;

import javax.persistence.Column;
import javax.persistence.Entity;

/**
 * The generic data-interface for an island
 */
@Entity
public class Island {
    @Column
    private String name;

    @Column
    private int x;

    @Column
    private int z;

    @Column
    private String schematic;

    @Column
    private double score;
}
