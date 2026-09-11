package code.filipesz.springdeliveryengine.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Table(name = "codes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Code {

    @Id
    @Column(nullable = false, unique = true)
    private String code;

    @Column(nullable = false)
    private Integer discount;

    @Column(nullable = false)
    private Integer usesCount;
}