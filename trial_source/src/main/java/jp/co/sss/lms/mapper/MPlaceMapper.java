package jp.co.sss.lms.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import jp.co.sss.lms.dto.UserAttendanceDto;

@Mapper
public interface MPlaceMapper {

	List<UserAttendanceDto> getUserAttendanceDto(@Param("placeId") Integer placeId, @Param("from") String from,
			@Param("to") String to, @Param("deleteFlg") short deleteFlg);
}
