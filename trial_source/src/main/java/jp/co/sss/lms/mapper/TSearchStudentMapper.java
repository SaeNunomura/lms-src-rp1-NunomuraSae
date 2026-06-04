package jp.co.sss.lms.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import jp.co.sss.lms.dto.CompanyDto;
import jp.co.sss.lms.dto.CourseDto;
import jp.co.sss.lms.dto.PlaceDto;
import jp.co.sss.lms.dto.SearchStudentDto;

/**
 * 勤怠情報確認（受講生一覧）テーブルマッパー
 * @author 布村沙英 -Task.57
 */
@Mapper
public interface TSearchStudentMapper {
	//コースIDとコース名を取得
	List<CourseDto> getCouseList(@Param("hiddenFlg") short hiddenFlg, @Param("deleteFlg") short deleteFlg);

	//会場IDから会場名を取得
	List<PlaceDto> getPlaceList(@Param("placeId") Integer placeId, @Param("deleteFlg") short deleteFlg);

	//企業IDと企業名を取得
	List<CompanyDto> getCompanyList(@Param("deleteFlg") short deleteFlg);

	//コースID・企業ID・ユーザー名・会場ID・ロール・削除フラグから、
	//LMSユーザーID・ユーザーID・ユーザー名・コースID・コース名・企業名・会場名を取得
	List<SearchStudentDto> getSearchStudentList(@Param("courseId") Integer courseId,
			@Param("companyId") Integer companyId,
			@Param("userName") String userName, @Param("placeId") Integer placeId, @Param("role") String role,
			@Param("deleteFlg") short deleteFlg);
}
