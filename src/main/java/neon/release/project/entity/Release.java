package neon.release.project.entity;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.util.Date;

@Getter(AccessLevel.PUBLIC)
@Setter(AccessLevel.PUBLIC)
@Entity
@EntityListeners(AuditingEntityListener.class)
public class Release {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    private String name;

    private String description;

    @ManyToOne(optional = false, targetEntity = ReleaseStatus.class)
    private ReleaseStatus status;

    private Date releaseDate;

    @CreatedDate
    private Date createdAt;

    @LastModifiedDate
    private Date lastUpdateAt;
}
