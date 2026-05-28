package re.edu.hackathon.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductResponse {
    private Long id;
    private String code;
    private String name;
    private String sku;
    private String detail;
    private Integer stockQuantity;
    private BigDecimal salePrice;
    private Boolean deleted;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;


}
