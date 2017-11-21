package us.talabrek.ultimateskyblock.storage;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.UUID;

/**
 * A player in the uSkyBlock world
 */
@Entity
public class Member {
    @Id
    private UUID uuid;
    @Column
    private String name;
    @Column
    private String displayName;
}
