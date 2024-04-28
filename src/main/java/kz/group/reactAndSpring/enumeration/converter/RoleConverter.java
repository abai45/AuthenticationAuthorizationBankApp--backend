package kz.group.reactAndSpring.enumeration.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import kz.group.reactAndSpring.enumeration.AuthorityEnum;

import java.util.stream.Stream;

@Converter(autoApply = true)
public class RoleConverter implements AttributeConverter<AuthorityEnum, String> {

    @Override
    public String convertToDatabaseColumn(AuthorityEnum authorityEnum) {
        if(authorityEnum == null) {
            return null;
        }
        return authorityEnum.getValue();
    }

    @Override
    public AuthorityEnum convertToEntityAttribute(String dbData) {
        if(dbData == null) {
            return null;
        }
        return Stream.of(AuthorityEnum.values())
                .filter(authorityEnum -> authorityEnum.getValue().equals(dbData))
                .findFirst()
                .orElseThrow(IllegalArgumentException::new);
    }
}
