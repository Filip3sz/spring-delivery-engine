package code.filipesz.springdeliveryengine.dto;

public record CodeResponse(
        String code,
        Integer discount,
        Integer usesCount
) {
}
