package vn.edu.fpt.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import vn.edu.fpt.dto.InstructorRequestDTO;
import vn.edu.fpt.entity.InstructorRequest;

/**
 * MapStruct mapper: InstructorRequest (entity) → InstructorRequestDTO.
 * <p>
 * Chỉ khai báo các field cần map thủ công từ nested object {@code user}.
 * Các field cùng tên (cvUrl, certificateUrl, description, rejectionReason,
 * status, createdAt) được MapStruct tự động map.
 * </p>
 */
@Mapper(componentModel = "spring")
public interface InstructorRequestMapper {

    @Mapping(target = "fullName",
             expression = "java(request.getUser().getFirstName() + \" \" + request.getUser().getLastName())")
    @Mapping(target = "email",     source = "user.email")
    @Mapping(target = "avatarUrl", source = "user.avatarUrl")
    @Mapping(target = "bio",       source = "user.bio")
    InstructorRequestDTO toDto(InstructorRequest request);
}
