package re.edu.hackathon.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductPatchRequest {
    @Pattern(regexp = ".*\\S.*", message = "Mã sản phẩm không được để trống")
    @Size(max = 50, message = "Mã sản phẩm không được vượt quá 50 ký tự")
    private String code;

    @Pattern(regexp = ".*\\S.*", message = "Tên sản phẩm không được để trống")
    @Size(max = 255, message = "Tên sản phẩm không được vượt quá 255 ký tự")
    private String name;

    @Pattern(regexp = ".*\\S.*", message = "SKU không được để trống")
    @Size(max = 100, message = "SKU không được vượt quá 100 ký tự")
    private String sku;

    @Size(max = 2000, message = "Chi tiết sản phẩm không được vượt quá 2000 ký tự")
    private String detail;

    @Min(value = 0, message = "Số lượng tồn kho phải lớn hơn hoặc bằng 0")
    private Integer stockQuantity;

    @DecimalMin(value = "0.0", inclusive = true, message = "Đơn giá bán phải lớn hơn hoặc bằng 0")
    @Digits(integer = 13, fraction = 2, message = "Đơn giá bán chỉ được có tối đa 13 chữ số phần nguyên và 2 chữ số thập phân")
    private BigDecimal salePrice;

    public boolean isEmpty() {
        return code == null
                && name == null
                && sku == null
                && detail == null
                && stockQuantity == null
                && salePrice == null;
    }

}
