package vn.edu.fpt.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import vn.edu.fpt.dto.InstructorRequestDTO;
import vn.edu.fpt.entity.InstructorRequest;

/**
 * MapStruct mapper: InstructorRequest (entity) → InstructorRequestDTO.
 * <p>
 * Các field lấy từ nested object {@code user} và {@code reviewedBy}
 * được map qua expression để ghép chuỗi hoặc handle null an toàn.
 * </p>
 */
@Mapper(componentModel = "spring")
public interface InstructorRequestMapper {

    @Mapping(target = "fullName",
            expression = "java(request.getUser().getFirstName() + \" \" + request.getUser().getLastName())")
    @Mapping(target = "email",        source = "user.email")
    @Mapping(target = "avatarUrl",    source = "user.avatarUrl")
    @Mapping(target = "bio",          source = "user.bio")
    @Mapping(target = "cvUrl",        source = "cvUrl")
    @Mapping(target = "certificateUrl", source = "certificateUrl")
    @Mapping(target = "description",  source = "description")
    @Mapping(target = "rejectionReason", source = "rejectionReason")
    @Mapping(target = "status",       source = "status")
    @Mapping(target = "createdAt",    source = "createdAt")
    InstructorRequestDTO toDto(InstructorRequest request);
}
