package com.bierchitekt.concerts;

import com.bierchitekt.concerts.persistence.ConcertEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ConcertMapper {

        List<ConcertDTO> toConcertDto(List<ConcertEntity> concerts);


        @Mapping(target = "id", ignore = true)
        @Mapping(target = "notified", ignore = true)
        ConcertEntity toConcertEntity(ConcertDTO concertDTO);
}
