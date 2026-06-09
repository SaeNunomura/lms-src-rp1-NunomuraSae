package jp.co.sss.lms.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import jp.co.sss.lms.entity.MPlace;

@Mapper
public interface TPlaceIdUserAttendanceMapper {

	MPlace findByPlaceId(@Param("placeId") Integer placeId, @Param("hiddenFlg") short hiddenFlg,
			@Param("deleteFlg") short deleteFlg);
}
