package by.andd3dfx.sitesparsing.onlinerby.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
@AllArgsConstructor
public class CpuItem {

    private String name;
    private String fullName;
    private String url;
    private double price;

    private int coresAmount;
    private double frequency;

    private double usefulness;

    @Override
    public String toString() {
        return "CpuItem{" +
            "name='" + name + '\'' +
            ", url='" + url + '\'' +
            ", price=" + price +
            ", cores=" + coresAmount +
            ", frequency=" + frequency +
            ", revUsefulness=" + usefulness +
            '}';
    }
}
